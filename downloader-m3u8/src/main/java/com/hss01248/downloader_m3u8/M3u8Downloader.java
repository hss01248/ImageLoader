package com.hss01248.downloader_m3u8;

import android.os.Environment;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.LogUtils;
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


    /**
     * 暂时只支持下载纯list,不支持下载main(包含二级m3u8)
     * 默认都是视频
     * 需要先请求管理外部存储权限
     * @param adPaths
     * @param name
     * @param url
     */
    public static void start(List<String> adPaths,String subDirName,String name,String url){
        String fileName = URLUtil.guessFileName(url, "", "");
       String fileNameRemote = name+"-"+fileName;
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppUtils.getAppName());
        if(!dir.exists()){
            dir.mkdirs();
        }
        if(!TextUtils.isEmpty(subDirName)){
            dir = new File(dir,subDirName);
            if(!dir.exists()){
                dir.mkdirs();
            }
        }
        File m3u8FileRemote = new File(dir,fileNameRemote);

        File finalDir = dir;
        FileDownloader.getImpl()
                        .create(url)
                        .setPath(m3u8FileRemote.getAbsolutePath(),false)
                        .setListener(new FileDownloadSampleListener(){
                            @Override
                            protected void completed(BaseDownloadTask task) {
                                super.completed(task);
                                MediaPlaylist remoteList = parseMediaPlayList(m3u8FileRemote);
                                if(remoteList == null){
                                    return;
                                }
                                remoteList = wash(remoteList,url,adPaths);
                                File m3u8FileLocal = new File(finalDir,name+"-local-"+fileName);
                                if(m3u8FileLocal.exists()){
                                    MediaPlaylist localList = parseMediaPlayList(m3u8FileLocal);
                                    if(localList != null){
                                        if(localList.mediaSegments().size() != remoteList.mediaSegments().size()){
                                            LogUtils.w("本地和远程mediaSegments长度不一致!!!");
                                        }
                                        //更新localList:
                                        List<MediaSegment> segments = new ArrayList<>(localList.mediaSegments().size());
                                        int i = -1;
                                        for (MediaSegment mediaSegment : localList.mediaSegments()) {
                                            i++;
                                            if(mediaSegment.uri().startsWith("/storage/emulated/")){
                                                segments.add(mediaSegment);
                                            }else {
                                                //远程url更新为新的
                                                if(i< remoteList.mediaSegments().size()){
                                                    segments.add(remoteList.mediaSegments().get(i));
                                                }else {
                                                    segments.add(mediaSegment);
                                                }

                                            }
                                        }
                                        //locallist写到文件中
                                        localList = MediaPlaylist.builder()
                                                .from(localList)
                                                .mediaSegments(segments)
                                                .build();
                                      boolean success =   writeList(localList,m3u8FileLocal);
                                      if(!success){
                                          LogUtils.w("本地写m3u8 file失败: ",m3u8FileLocal);
                                      }
                                        downloadSegments(name,remoteList,m3u8FileLocal);
                                      return;
                                      /*if(success){
                                          return;
                                      }*/
                                    }
                                }
                                //remotelist写到localFile中
                                writeList(remoteList,m3u8FileLocal);
                                downloadSegments(name,remoteList,m3u8FileLocal);

                            }

                            @Override
                            protected void error(BaseDownloadTask task, Throwable e) {
                                super.error(task, e);
                            }
                        }).start();
    }

    private static boolean writeList(MediaPlaylist localList, File m3u8FileLocal) {
        MediaPlaylistParser parser = new MediaPlaylistParser();
        String s = parser.writePlaylistAsString(localList);
        return FileIOUtils.writeFileFromString(m3u8FileLocal, s);
    }

    private static MediaPlaylist parseMediaPlayList(File file){
        try{
            MediaPlaylistParser parser = new MediaPlaylistParser();
            MediaPlaylist playlist = parser.readPlaylist(file.getAbsolutePath());
            return  playlist;
        }catch (Throwable throwable){
            LogUtils.w(throwable,file);
            return  null;
        }
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

    private static MediaPlaylist wash(MediaPlaylist playlist,String url,List<String> adPaths){
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
        MediaPlaylist playlist2 = MediaPlaylist.builder()
                .from(playlist)
                .mediaSegments(segments)
                .build();
        return  playlist2;
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
        MediaPlaylist playlist2 = MediaPlaylist.builder()
                .from(playlist)
                .mediaSegments(segments)
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
        //下载每个分片
        downloadSegments(name, playlist,  newFile);

// Write playlist to standard out
       // System.out.println(parser.writePlaylistAsString(updated));
    }

    private static void downloadSegments(String name,  MediaPlaylist playlist, File m3u8FileLocal) {
        List<MediaSegment> segments = playlist.mediaSegments();
        File subDir = new File(m3u8FileLocal.getParentFile(),dirName);
        subDir.mkdirs();
        MediaPlaylistParser parser = new MediaPlaylistParser();
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
                                    .from(segment)
                                    .uri(file1.getAbsolutePath())
                                    .build();
                            segments.set(finalI, build);
                            //每下载完一个,就更新一次文件
                            String s = parser.writePlaylistAsString(playlist);
                            boolean b = FileIOUtils.writeFileFromString(m3u8FileLocal, s);
                            LogUtils.d("下载成功一个ts,更新到m3u8文件中",file1.getAbsolutePath());
                            if(!b){
                                LogUtils.d("下载成功一个ts,更新到m3u8文件中--> m3u8更新失败",file1.getAbsolutePath());
                            }
                        }

                        @Override
                        protected void error(BaseDownloadTask task, Throwable e) {
                            super.error(task, e);
                            LogUtils.w(segment.uri(),e);
                        }
                    }).start();

        }
        LogUtils.d("所有文件下载成功", m3u8FileLocal.getAbsolutePath());
    }
}
