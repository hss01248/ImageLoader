package com.hss.downloader;

import androidx.annotation.NonNull;

import com.hss.downloader.api.DownloadApi;
import com.hss.downloader.api.IDownload2;
import com.hss01248.download_okhttp.OkhttpDownloadUtil;
import com.liulishuo.filedownloader2.AndroidDownloader;
import com.liulishuo.filedownloader2.DownloadCallbackOnMainThreadWrapper;

import java.util.HashMap;
import java.util.Map;

/**
 * @Despciption todo
 * @Author hss
 * @Date 8/27/24 7:22 PM
 * @Version 1.0
 */
public class OkDownloadImpl2 implements IDownload, IDownload2 {

    public static void setUserAgent(String userAgent) {
        OkDownloadImpl2.userAgent = userAgent;
    }

   volatile static String userAgent;
    @Override
    public void download(String url, @NonNull String filePath, @NonNull Map<String, String> headers, IDownloadCallback callback) {
        callback.onStart(url,filePath);
        if(headers == null){
            headers = new HashMap<>();
        }
        if(!headers.containsKey("User-Agent")){
            if(userAgent ==null){
                userAgent = System.getProperty("http.agent");
            }
            headers.put("User-Agent", userAgent);
        }

        AndroidDownloader.prepareDownload(url)
                        .filePath(filePath)
                        .headers(headers)
                        .start(new DownloadCallbackOnMainThreadWrapper(new com.hss01248.download_okhttp.IDownloadCallback() {
                            @Override
                            public void onSuccess(String url, String path) {
                                callback.onSuccess(url, path);
                            }

                            @Override
                            public void onFailed(String url, String path, String code, String msg, Throwable e) {
                                callback.onFail(url,path,code+" "+msg,e);
                            }
                            @Override
                            public void onProgress(String url, String path, long total, long alreadyReceived,long speed) {
                                callback.onProgress(url,path,alreadyReceived,total,speed);
                            }


                        }));

    }

    @Override
    public void stopDownload(String url) {
        OkhttpDownloadUtil.pauseOrStop(url);
    }

    @Override
    public void download(DownloadApi api) {
        AndroidDownloader.prepareDownload(api.getUrl())
                .filePath(api.getRealPath())
                .headers(api.getHeaders())
                .start(new DownloadCallbackOnMainThreadWrapper(new com.hss01248.download_okhttp.IDownloadCallback() {
                    @Override
                    public void onCodeStart(String url, String path) {
                        com.hss01248.download_okhttp.IDownloadCallback.super.onCodeStart(url, path);
                        api.getCallback().onStart(url, path);
                    }

                    @Override
                    public void onStartReal(String url, String path) {
                        com.hss01248.download_okhttp.IDownloadCallback.super.onStartReal(url, path);
                    }

                    @Override
                    public void onSuccess(String url, String path) {
                        api.getCallback().onSuccess(url,path);
                    }

                    @Override
                    public void onFailed(String url, String path, String code, String msg, Throwable e) {
                        api.getCallback().onFail(url,path,code+" "+msg,e);
                    }
                    @Override
                    public void onProgress(String url, String path, long total, long alreadyReceived,long speed) {
                        api.getCallback().onProgress(url, path, total, alreadyReceived, speed);
                    }

                    @Override
                    public void onCancel(String url, String path) {
                        com.hss01248.download_okhttp.IDownloadCallback.super.onCancel(url, path);
                    }
                }));
    }
}
