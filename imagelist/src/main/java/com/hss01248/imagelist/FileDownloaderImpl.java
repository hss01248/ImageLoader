package com.hss01248.imagelist;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.Utils;
import com.hss.downloader.IDownload;
import com.hss.downloader.IDownloadCallback;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.util.Map;

public class FileDownloaderImpl implements IDownload {

    public FileDownloaderImpl() {
        FileDownloader.setup(Utils.getApp());
    }

    @Override
    public void download(String url, @Nullable String filePath, @Nullable Map<String, String> headers, IDownloadCallback callback) {
        BaseDownloadTask task = FileDownloader.getImpl().create(url)
                .setPath(filePath);
        if (headers != null && !headers.isEmpty()) {
            for (String s : headers.keySet()) {
                task.addHeader(s, headers.get(s));
            }
        }
        task
                .setListener(new FileDownloadListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        callback.onStart(url,filePath);
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        callback.progress(url, filePath,soFarBytes, totalBytes);
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        callback.onSuccess(url,filePath);

                    }

                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        callback.onFail(url,filePath, e.getMessage(), e);
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {

                    }
                }).start();
    }
}
