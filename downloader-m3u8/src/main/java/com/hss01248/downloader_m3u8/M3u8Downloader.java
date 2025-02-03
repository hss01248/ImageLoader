package com.hss01248.downloader_m3u8;

import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.lindstrom.m3u8.model.MediaPlaylist;
import io.lindstrom.m3u8.model.MediaSegment;
import io.lindstrom.m3u8.parser.MediaPlaylistParser;
import io.reactivex.functions.Consumer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @Despciption todo
 * @Author hss
 * @Date 23/11/2023 17:44
 * @Version 1.0
 */
public class M3u8Downloader {

    static final String dirName = "m3u8Content";


    public static void checkPermission(Runnable onGranted) {
        String permission = Permission.MANAGE_EXTERNAL_STORAGE;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            //Android 11（API 级别 30）
            permission = Permission.WRITE_EXTERNAL_STORAGE;

            //请注意，在搭载 Android 10（API 级别 29）或更高版本的设备上，
            // 您的应用可以提供明确定义的媒体集合，例如 MediaStore.Downloads，而无需请求任何存储相关权限
        }
        XXPermissions.with(ActivityUtils.getTopActivity())
                .permission(permission)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        onGranted.run();
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        OnPermissionCallback.super.onDenied(permissions, never);
                        ToastUtils.showLong("需要允许存储权限");
                    }
                });
    }

    public static void getFileLengthByHeader(String url, Map<String, String> headers, Consumer<Long> onResult) {
        ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<Long>() {
            @Override
            public Long doInBackground() throws Throwable {
                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .followRedirects(true)
                        .followSslRedirects(true)
                        .retryOnConnectionFailure(true).build();
                //todo 忽略证书校验
                Request.Builder builder = new Request.Builder()
                        .url(url).head();
                if (headers != null && !headers.isEmpty()) {
                    for (String s : headers.keySet()) {
                        String val = headers.get(s);
                        if (!TextUtils.isEmpty(s) && !TextUtils.isEmpty(val)) {
                            builder.addHeader(s, val);
                        } else {
                            LogUtils.w("null key or val in map", s, val);
                        }
                    }
                }
                Response execute = okHttpClient.newCall(builder.build()).execute();
                if (execute.isSuccessful()) {
                    String header = execute.header("Content-Length");
                    if (!TextUtils.isEmpty(header)) {
                        return Long.parseLong(header);
                    }
                }
                return null;
            }

            @Override
            public void onSuccess(Long result) {
                try {
                    onResult.accept(result);
                } catch (Exception e) {
                    LogUtils.w(e);
                }
            }

            @Override
            public void onFail(Throwable t) {
                //super.onFail(t);
                LogUtils.w(url, t);
                try {
                    onResult.accept(null);
                } catch (Exception e) {
                    LogUtils.w(e);
                }
            }
        });
    }


    /**
     * 暂时只支持下载纯list,不支持下载main(包含二级m3u8)
     * 默认都是视频
     * 需要先请求管理外部存储权限
     *
     * @param adPaths
     * @param name
     * @param url
     */
    public static void start(List<String> adPaths, String subDirName, String name, String url, Map<String, String> headers) {
        if (!url.startsWith("http")) {
            ToastUtils.showLong("不支持的协议: \n" + url);
            return;
        }
        if (!url.contains(".m3u8") && !url.contains(".mp4") && !url.contains(".mkv")) {
            ToastUtils.showLong("不支持的格式: \n" + url);
            return;
        }
        checkPermission(new Runnable() {
            @Override
            public void run() {

            }
        });
        String fileName = URLUtil.guessFileName(url, "", "");
        String fileNameRemote = name + "-" + fileName;
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppUtils.getAppName());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!TextUtils.isEmpty(subDirName)) {
            dir = new File(dir, subDirName);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
        File downloadedFile = new File(dir, fileNameRemote);

        if (url.contains(".mp4") || url.contains(".mkv")) {
            //直接下载
            if (downloadedFile.exists() && downloadedFile.length() > 0) {
                getFileLengthByHeader(url, headers, new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (aLong == null) {
                            downloadPureFile(adPaths, subDirName, name, fileName, downloadedFile, url, headers);
                        } else {
                            long len = aLong;
                            if (downloadedFile.length() == len) {
                                ToastUtils.showLong("之前已下载成功");
                            } else {
                                //重新下载. 断点续传由下载引擎支持,外部不处理
                                downloadPureFile(adPaths, subDirName, name, fileName, downloadedFile, url, headers);
                            }
                        }
                    }
                });
                return;
            }
        }
        downloadPureFile(adPaths, subDirName, name, fileName, downloadedFile, url, headers);

    }

    private static void downloadPureFile(List<String> adPaths, String subDirName, String name, String fileName,
                                         File downloadedFile, String url, Map<String, String> headers) {
        File finalDir = downloadedFile.getParentFile();
        BaseDownloadTask task = FileDownloader.getImpl()
                .create(url);

        if (headers != null && !headers.isEmpty()) {
            for (String s : headers.keySet()) {
                String val = headers.get(s);
                if (!TextUtils.isEmpty(s) && !TextUtils.isEmpty(val)) {
                    task.addHeader(s, val);
                } else {
                    LogUtils.w("null key or val in map", s, val);
                }
            }
        }
        task.setPath(downloadedFile.getAbsolutePath(), false)
                .setListener(new FileDownloadSampleListener() {
                    @Override
                    protected void completed(BaseDownloadTask task) {
                        super.completed(task);
                        if (url.contains(".mp4") || url.contains(".mkv")) {
                            ToastUtils.showLong("下载成功: \n" + downloadedFile.getAbsolutePath());
                        } else {
                            parseM3u8Response(downloadedFile, url, adPaths, finalDir, name, fileName);
                        }


                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        super.error(task, e);
                    }
                }).start();
    }

    private static void parseM3u8Response(File m3u8FileRemote, String url, List<String> adPaths, File finalDir, String name, String fileName) {
        MediaPlaylist remoteList = parseMediaPlayList(m3u8FileRemote);
        if (remoteList == null) {
            return;
        }
        remoteList = filter(remoteList, url, adPaths);
        File m3u8FileLocal = new File(finalDir, name + "-local-" + fileName);
        if (m3u8FileLocal.exists()) {
            MediaPlaylist localList = parseMediaPlayList(m3u8FileLocal);
            if (localList != null) {
                if (localList.mediaSegments().size() != remoteList.mediaSegments().size()) {
                    LogUtils.w("本地和远程mediaSegments长度不一致!!!");
                }
                //更新localList:
                List<MediaSegment> segments = new ArrayList<>(localList.mediaSegments().size());
                int i = -1;
                for (MediaSegment mediaSegment : localList.mediaSegments()) {
                    i++;
                    if (mediaSegment.uri().startsWith("/storage/emulated/")) {
                        segments.add(mediaSegment);
                    } else {
                        //远程url更新为新的
                        if (i < remoteList.mediaSegments().size()) {
                            segments.add(remoteList.mediaSegments().get(i));
                        } else {
                            segments.add(mediaSegment);
                        }

                    }
                }
                //locallist写到文件中
                localList = MediaPlaylist.builder()
                        .from(localList)
                        .mediaSegments(segments)
                        .build();
                boolean success = writeList(localList, m3u8FileLocal);
                if (!success) {
                    LogUtils.w("本地写m3u8 file失败: ", m3u8FileLocal);
                }
                downloadSegments(name, remoteList, m3u8FileLocal);
                return;
              /*if(success){
                  return;
              }*/
            }
        }
        //remotelist写到localFile中
        writeList(remoteList, m3u8FileLocal);
        downloadSegments(name, remoteList, m3u8FileLocal);
    }

    private static boolean writeList(MediaPlaylist localList, File m3u8FileLocal) {
        MediaPlaylistParser parser = new MediaPlaylistParser();
        String s = parser.writePlaylistAsString(localList);
        return FileIOUtils.writeFileFromString(m3u8FileLocal, s);
    }

    private static MediaPlaylist parseMediaPlayList(File file) {
        try {
            MediaPlaylistParser parser = new MediaPlaylistParser();
            MediaPlaylist playlist = parser.readPlaylist(file.getAbsolutePath());
            return playlist;
        } catch (Throwable throwable) {
            LogUtils.w(throwable, file);
            return null;
        }
    }


    private static MediaPlaylist filter(MediaPlaylist playlist, String url, List<String> adPaths) {
        String preffix = url;
        if (preffix.contains("?")) {
            preffix = preffix.substring(0, preffix.indexOf("?"));
        }
        preffix = preffix.substring(0, preffix.lastIndexOf("/") + 1);
        List<MediaSegment> segments = new ArrayList<>();
        out:
        for (MediaSegment mediaSegment : playlist.mediaSegments()) {
            //LogUtils.d(mediaSegment.duration(),mediaSegment.uri(),mediaSegment.segmentKey());

            for (String adPath : adPaths) {
                if (mediaSegment.uri().contains(adPath)) {
                    //过滤广告
                    continue out;
                }
                if (mediaSegment.uri().startsWith("https")) {
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
        if (segments.isEmpty()) {
            LogUtils.i("没有需要覆写的uri,拷贝文件就行");

        } else {

        }
        MediaPlaylist playlist2 = MediaPlaylist.builder()
                .from(playlist)
                .mediaSegments(segments)
                .build();
        return playlist2;
    }


    private static void downloadSegments(String name, MediaPlaylist playlist, File m3u8FileLocal) {
        List<MediaSegment> segments = playlist.mediaSegments();
        File subDir = new File(m3u8FileLocal.getParentFile(), dirName);
        subDir.mkdirs();
        MediaPlaylistParser parser = new MediaPlaylistParser();
        int i = -1;
        for (MediaSegment segment : segments) {
            i++;
            String path = segment.uri();
            if (path.contains("?")) {
                path = path.substring(0, path.indexOf("?"));
            }
            path = path.substring(path.lastIndexOf("/") + 1);
            path = name + "-" + i + "-" + path;
            File file1 = new File(subDir, path);
            int finalI = i;
            FileDownloader.getImpl()
                    .create(segment.uri())
                    .setPath(file1.getAbsolutePath(), false)
                    .setSyncCallback(true)
                    .setAutoRetryTimes(1)
                    .setListener(new FileDownloadSampleListener() {
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
                            LogUtils.d("下载成功一个ts,更新到m3u8文件中", file1.getAbsolutePath());
                            if (!b) {
                                LogUtils.d("下载成功一个ts,更新到m3u8文件中--> m3u8更新失败", file1.getAbsolutePath());
                            }
                        }

                        @Override
                        protected void error(BaseDownloadTask task, Throwable e) {
                            super.error(task, e);
                            LogUtils.w(segment.uri(), e);
                        }
                    }).start();

        }
        LogUtils.d("所有文件下载成功", m3u8FileLocal.getAbsolutePath());
    }
}
