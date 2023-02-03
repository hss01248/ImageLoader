package com.hss.downloader;



public interface IDownloadCallback {

    void onStart(String url,String realPath);

    void onSuccess(String url,String realPath);

    void progress(String url,String realPath ,long currentOffset, long totalLength);

    void onFail(String url,String realPath,String msg,Throwable throwable);
}
