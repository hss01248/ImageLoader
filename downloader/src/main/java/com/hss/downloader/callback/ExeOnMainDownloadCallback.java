package com.hss.downloader.callback;

import com.blankj.utilcode.util.ThreadUtils;
import com.hss.downloader.IDownloadCallback;

/**
 * @Despciption todo
 * @Author hss
 * @Date 08/02/2023 09:50
 * @Version 1.0
 */
public class ExeOnMainDownloadCallback implements IDownloadCallback {

    public ExeOnMainDownloadCallback(IDownloadCallback callback) {
        this.callback = callback;
    }

    IDownloadCallback callback;
    @Override
    public void onBefore(String url, String realPath, boolean forceRedownload) {
        exeOnMain(new Runnable(){
            @Override
            public void run() {
                callback.onBefore(url, realPath, forceRedownload);
            }
        });
    }

    private void exeOnMain(Runnable runnable) {
        ThreadUtils.getMainHandler().post(runnable);
    }

    @Override
    public void onStart(String url, String realPath) {
        exeOnMain(new Runnable(){

            @Override
            public void run() {
                callback.onStart(url, realPath);
            }
        });
    }

    @Override
    public void onSuccess(String url, String realPath) {
        exeOnMain(new Runnable(){

            @Override
            public void run() {
                callback.onSuccess(url, realPath);
            }
        });
    }

    @Override
    public void onProgress(String url, String realPath, long currentOffset, long totalLength,long speed) {
        exeOnMain(new Runnable(){
            @Override
            public void run() {
                callback.onProgress(url,realPath,currentOffset,totalLength,speed);
            }
        });
    }

    @Override
    public void onFail(String url, String realPath, String msg, Throwable throwable) {
        exeOnMain(new Runnable() {
            @Override
            public void run() {
                callback.onFail(url, realPath, msg, throwable);
            }
        });
    }
}
