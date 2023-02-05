package com.hss.downloader;



public interface IDownloadCallback {


    /**
     *
     * @param url
     * @param realPath
     * @return 是否要真的发起下载
     */
    default boolean onBefore(String url,String realPath){
        return true;
    }

   default void onStart(String url,String realPath){

   }

    void onSuccess(String url,String realPath);

  default   void progress(String url,String realPath ,long currentOffset, long totalLength){

  }

    void onFail(String url,String realPath,String msg,Throwable throwable);
}
