package com.hss01248.downloader_m3u8;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hss.downloader.api.DownloadApi;
import com.hss.downloader.callback.DefaultSilentDownloadCallback;
import com.hss.downloader.callback.DefaultUIDownloadCallback;
import com.hss01248.downloader_m3u8.m3u8.M3U8;
import com.hss01248.downloader_m3u8.m3u8.M3U8Utils;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import io.lindstrom.m3u8.model.MasterPlaylist;
import io.lindstrom.m3u8.model.MediaPlaylist;
import io.lindstrom.m3u8.model.MediaSegment;
import io.lindstrom.m3u8.model.Variant;
import io.lindstrom.m3u8.parser.MasterPlaylistParser;
import io.lindstrom.m3u8.parser.MediaPlaylistParser;

/**
 * @Despciption todo
 * @Author hss
 * @Date 23/11/2023 17:44
 * @Version 1.0
 */
public class M3u8Downloader {

    static final String dirName = "m3u8Content";


    public static void start(List<String> adPaths,String name,String url){
        DownloadApi.create(url)
                .setNeedCheckDbBeforeStart(false)
                .callback(new DefaultUIDownloadCallback(new DefaultSilentDownloadCallback(){
                    @Override
                    public void onSuccess(String url, String realPath) {
                        super.onSuccess(url, realPath);
                        ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<Object>() {
                            @Override
                            public Object doInBackground() throws Throwable {
                                try {
                                    //parseM3u8MainFile(url,realPath);
                                    //parseM3u8MainFile2(url,realPath);
                                    parseM3u8ListFile(adPaths,name,url,realPath);
                                } catch (Throwable e) {
                                    LogUtils.w(url,realPath,e);
                                    ToastUtils.showLong("下载失败: "+e.getMessage());
                                }
                                return null;
                            }

                            @Override
                            public void onSuccess(Object result) {

                            }
                        });

                    }
                }));
    }

    private static void parseM3u8MainFile(String url, String realPath) throws Throwable{
        MasterPlaylistParser parser = new MasterPlaylistParser();

// Parse playlist
        MasterPlaylist playlist = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            playlist = parser.readPlaylist(Paths.get(realPath));
        }
        for (Variant variant : playlist.variants()) {
            LogUtils.d(variant);
            //String url2 = variant.
            downloadPlayListItemM3u8File(variant,url,realPath);
        }


// Update playlist version
        MasterPlaylist updated = MasterPlaylist.builder()
                .from(playlist)
                .version(2)
                .build();

// Write playlist to standard out
        //System.out.println(parser.writePlaylistAsString(updated));
    }

    private static void downloadPlayListItemM3u8File(Variant variant, String url, String realPath) {

    }

    private static void parseM3u8MainFile2(String url, String realPath) throws Throwable{
        M3U8 m3U8 = M3U8Utils.parseLocalM3U8File(new File(realPath));
        LogUtils.d(m3U8);
    }

    private static void parseM3u8ListFile(List<String> adPaths,String name,String url, String realPath) throws Throwable{
        MediaPlaylistParser parser = new MediaPlaylistParser();

// Parse playlist
        MediaPlaylist playlist = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            playlist = parser.readPlaylist(Paths.get(realPath));
        }
        String preffix = url;
        if(preffix.contains("?")){
            preffix = preffix.substring(0,preffix.indexOf("?"));
        }
        preffix = preffix.substring(0,preffix.lastIndexOf("/")+1);
        List<MediaSegment> segments = new ArrayList<>();
       out: for (MediaSegment mediaSegment : playlist.mediaSegments()) {
         //LogUtils.d(mediaSegment.duration(),mediaSegment.uri(),mediaSegment.segmentKey());

            for (String adPath : adPaths) {
                if(mediaSegment.uri().contains(adPath)){
                    //过滤广告
                    continue out;
                }
                if(mediaSegment.uri().startsWith("https")){
                    //本身是https开头,就不用拼接了
                    segments.add(mediaSegment);
                    continue out;
                }
            }
            MediaSegment build = MediaSegment.builder()
                    .duration(mediaSegment.duration())
                    //更新path:
                    .uri(preffix + mediaSegment.uri())
                    .build();
            segments.add(build);
        }
        if(segments.isEmpty()){
            LogUtils.w("没有需要覆写的uri,拷贝文件就行");

        }else {

        }
        // 辣鸡无比的api设计,还得自己拷贝一遍
        MediaPlaylist playlist2 = MediaPlaylist.builder()
                .addAllMediaSegments(segments)
                .addAllComments(playlist.comments())
                .addAllPartialSegments(playlist.partialSegments())
                .targetDuration(playlist.targetDuration())
                .playlistType(playlist.playlistType())
                //.iFramesOnly()
                .build();



        File file = new File(realPath);
        File dir = new File(file.getParentFile(),name);
        dir.mkdirs();
        //todo 以路径为key,判定是否为同一个文件
        File newFile = new File(dir,name+"-"+file.getName());
        if(newFile.exists()){
            //读取,继续之前未完成的下载: 新的远程url替换掉之前的url:




        }else {
            //写到文件中
            String s = parser.writePlaylistAsString(playlist2);
            boolean b = FileIOUtils.writeFileFromString(newFile, s);
            if(b){
                //file.delete();
            }
        }


        File subDir = new File(dir,dirName);
        subDir.mkdirs();
        //下载每个分片


        List<MediaSegment> segments2 = new ArrayList<>(segments);


        int i = -1;
        for (MediaSegment segment : segments) {
            i++;
            String path = segment.uri();
            if(path.contains("?")){
                path = path.substring(0,path.indexOf("?"));
            }
            path = path.substring(path.lastIndexOf("/")+1);
            path = name +"-"+i+"-"+path;
            File file1 = new File(subDir,path);
            int finalI = i;
            MediaPlaylist finalPlaylist = playlist;
            FileDownloader.getImpl()
                    .create(segment.uri())
                    .setPath(file1.getAbsolutePath(),false)
                    .setSyncCallback(true)
                    .setAutoRetryTimes(1)
                    .setListener(new FileDownloadSampleListener(){
                        @Override
                        protected void completed(BaseDownloadTask task) {
                            super.completed(task);
                            MediaSegment build = MediaSegment.builder()
                                    .duration(segment.duration())
                                    //更新path:
                                    //.uri(preffix + mediaSegment.uri())
                                    .uri(file1.getAbsolutePath())
                                    .build();
                            segments2.remove(finalI);
                            segments2.add(finalI,build);
                            //每下载完一个,就更新一次文件
                            //辣鸡api设计: io.lindstrom:m3u8-parser



                            MediaPlaylist playlist2 = MediaPlaylist.builder()
                                    .addAllMediaSegments(segments2)
                                    .addAllComments(finalPlaylist.comments())
                                    .addAllPartialSegments(finalPlaylist.partialSegments())
                                    .targetDuration(finalPlaylist.targetDuration())
                                    .playlistType(finalPlaylist.playlistType())
                                    //.iFramesOnly()
                                    .build();
                            //写到文件中
                            String s = parser.writePlaylistAsString(playlist2);
                            FileIOUtils.writeFileFromString(newFile, s);
                            LogUtils.d("下载成功一个ts,更新到m3u8文件中",file1.getAbsolutePath());

                        }

                        @Override
                        protected void error(BaseDownloadTask task, Throwable e) {
                            super.error(task, e);
                            LogUtils.w(segment.uri(),e);
                        }
                    }).start();

        }
        LogUtils.d("所有文件下载成功",dir.getAbsolutePath());

// Write playlist to standard out
       // System.out.println(parser.writePlaylistAsString(updated));
    }
}
