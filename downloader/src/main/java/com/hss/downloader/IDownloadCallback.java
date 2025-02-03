package com.hss.downloader;



public interface IDownloadCallback {



    default void onBefore(String url, String realPath, boolean forceRedownload){

    }

   default void onStart(String url,String realPath){

   }

    void onSuccess(String url,String realPath);

  default   void onProgress(String url, String realPath , long currentOffset, long totalLength,long speed){

  }

    void onFail(String url,String realPath,String msg,Throwable throwable);
}
