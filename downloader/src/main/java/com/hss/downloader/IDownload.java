package com.hss.downloader;

public interface IDownload {

    void download(String url,String filePath,IDownloadCallback callback);

}
