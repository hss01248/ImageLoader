package com.hss.downloader.api;

import com.blankj.utilcode.util.LogUtils;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

/**
 * @Despciption todo
 * @Author hss
 * @Date 03/02/2023 18:49
 * @Version 1.0
 */
public class DownloadImplByFileDownloader implements IDownload2 {
    @Override
    public void download(DownloadApi api) {
        String filePath = api.getRealPath();
        BaseDownloadTask task = FileDownloader.getImpl().create(api.getUrl())
                .setPath(filePath);
        if (api.getHeaders() != null && !api.getHeaders().isEmpty()) {
            for (String s : api.getHeaders().keySet()) {
                task.addHeader(s, api.getHeaders().get(s));
            }
        }
        task.addHeader(
                "User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36");
        task
                .setListener(new FileDownloadListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        api.getCallback().onStart(api.getUrl(), filePath);
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        api.getCallback().onProgress(api.getUrl(), filePath,soFarBytes, totalBytes);
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        api.getCallback().onSuccess(api.getUrl(),filePath);

                    }

                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        api.getCallback().onFail(api.getUrl(),filePath, e.getMessage(), e);
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {
                        LogUtils.w(task);

                    }
                }).start();
    }
}
