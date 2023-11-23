package com.hss.downloader.callback;


import com.blankj.utilcode.util.LogUtils;
import com.hss.downloader.IDownloadCallback;

/**
 * @author: Administrator
 * @date: 2023/2/4
 * @desc: //todo
 */
public class DefaultSilentDownloadCallback implements IDownloadCallback {



    @Override
    public void onSuccess(String url, String realPath) {

        LogUtils.d("下载成功:",url,realPath);
    }


    @Override
    public void onFail(String url, String realPath, String msg, Throwable throwable) {
        LogUtils.w("下载失败:",url,realPath,msg,throwable);
    }
}
