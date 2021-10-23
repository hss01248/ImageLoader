package com.hss01248.imagelist;

import com.blankj.utilcode.util.Utils;
import com.hss.downloader.IDownload;
import com.hss.downloader.IDownloadCallback;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

public class FileDownloaderImpl implements IDownload {

    public FileDownloaderImpl() {
        FileDownloader.setup(Utils.getApp());
    }

    @Override
    public void download(String url, String filePath, IDownloadCallback callback) {
        FileDownloader.getImpl().create(url)
                .setPath(filePath)
                .setListener(new FileDownloadListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        callback.onStart(url);
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        callback.progress(url,soFarBytes,totalBytes);
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                       callback.onSuccess(url);

                    }

                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        callback.onFail(url,e.getMessage(),e);
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {

                    }
                }).start();
    }
}
