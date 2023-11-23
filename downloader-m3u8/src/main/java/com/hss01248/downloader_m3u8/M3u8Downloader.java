package com.hss01248.downloader_m3u8;

import com.hss.downloader.api.DownloadApi;
import com.hss.downloader.callback.DefaultSilentDownloadCallback;
import com.hss.downloader.callback.DefaultUIDownloadCallback;

/**
 * @Despciption todo
 * @Author hss
 * @Date 23/11/2023 17:44
 * @Version 1.0
 */
public class M3u8Downloader {

    public static void start(String url){
        DownloadApi.create(url)
                .callback(new DefaultUIDownloadCallback(new DefaultSilentDownloadCallback(){
                    @Override
                    public void onSuccess(String url, String realPath) {
                        super.onSuccess(url, realPath);
                    }
                }));
    }
}
