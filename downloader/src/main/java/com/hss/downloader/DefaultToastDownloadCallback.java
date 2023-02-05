package com.hss.downloader;

import com.blankj.utilcode.util.ToastUtils;

/**
 * @author: Administrator
 * @date: 2023/2/4
 * @desc: //todo
 */
public class DefaultToastDownloadCallback implements IDownloadCallback{


    @Override
    public void onSuccess(String url, String realPath) {
        ToastUtils.showLong("文件下载成功\n"+url+"\n-->\n"+realPath);
    }


    @Override
    public void onFail(String url, String realPath, String msg, Throwable throwable) {
        ToastUtils.showLong("文件下载失败:\n"+msg+"\n"+url+"\n-->\n"+realPath);
    }
}
