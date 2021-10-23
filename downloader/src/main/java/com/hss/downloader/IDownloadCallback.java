package com.hss.downloader;



public interface IDownloadCallback {

    void onStart(String url);

    void onSuccess(String url);

    void progress(String url, long currentOffset, long totalLength);

    void onFail(String url,String msg,Throwable throwable);
}
