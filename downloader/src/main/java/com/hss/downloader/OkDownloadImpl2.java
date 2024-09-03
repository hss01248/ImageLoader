package com.hss.downloader;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.LogUtils;
import com.hss01248.download_okhttp.OkhttpDownloadUtil;
import com.liulishuo.filedownloader2.AndroidDownloader;
import com.liulishuo.filedownloader2.DownloadCallbackOnMainThreadWrapper;

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
        callback.onStart(url,filePath);
        AndroidDownloader.prepareDownload(url)
                        .filePath(filePath)
                        .headers(headers)
                        .start(new DownloadCallbackOnMainThreadWrapper(new com.hss01248.download_okhttp.IDownloadCallback() {
                            @Override
                            public void onSuccess(String url, String path) {
                                callback.onSuccess(url, path);
                            }

                            @Override
                            public void onFailed(String url, String path, String code, String msg, Throwable e) {
                                callback.onFail(url,path,code+" "+msg,e);
                            }
                            long lastReceived = 0;
                            long lastProgressTime = 0;
                            @Override
                            public void onProgress(String url, String path, long total, long alreadyReceived) {
                                if(lastReceived ==0){
                                    lastReceived = alreadyReceived;
                                    lastProgressTime = System.currentTimeMillis();
                                    callback.onProgress(url,path,alreadyReceived,total,0L);
                                }else {
                                    long changed = alreadyReceived - lastReceived;
                                    long speed = changed*1000 /(System.currentTimeMillis() - lastProgressTime);
                                    lastProgressTime = System.currentTimeMillis();
                                    lastReceived = alreadyReceived;
                                    callback.onProgress(url,path,alreadyReceived,total,speed);

                                }
                            }

                            @Override
                            public void onSpeed(String url, String path, long speed) {
                                //callback.onSpeed(url, path, speed);
                                LogUtils.d("speed---> "+speed/1024+"KB/s");
                            }
                        }));

    }

    @Override
    public void stopDownload(String url) {
        OkhttpDownloadUtil.pauseOrStop(url);
    }
}
