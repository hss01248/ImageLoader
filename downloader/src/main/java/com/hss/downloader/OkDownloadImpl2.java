package com.hss.downloader;

import androidx.annotation.NonNull;

import com.liulishuo.filedownloader2.OkhttpDownloadUtil;

import java.util.Map;

/**
 * @Despciption todo
 * @Author hss
 * @Date 8/27/24 7:22 PM
 * @Version 1.0
 */
public class OkDownloadImpl2 implements IDownload{
    @Override
    public void download(String url, @NonNull String filePath, @NonNull Map<String, String> headers, IDownloadCallback callback) {
        OkhttpDownloadUtil.downLoad(url, filePath, false, false,
                false, headers, null, new com.liulishuo.filedownloader2.IDownloadCallback() {
                    @Override
                    public void onProgress(String url, String path, long total, long alreadyReceived) {
                        callback.onProgress(url,path,alreadyReceived,total);
                    }

                    @Override
            public void onSuccess(String url, String path) {
                callback.onSuccess(url, path);
            }

            @Override
            public void onFailed(String url, String path, String code, String msg, Throwable e) {
                callback.onFail(url,path,code+" "+msg,e);
            }
        });
    }

    @Override
    public void stopDownload(String url) {
        OkhttpDownloadUtil.pauseOrStop(url);
    }
}
