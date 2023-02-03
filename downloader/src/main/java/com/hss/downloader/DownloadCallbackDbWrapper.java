package com.hss.downloader;

import com.blankj.utilcode.util.LogUtils;
import com.hss.downloader.download.DownloadInfo;
import com.hss.downloader.download.DownloadInfoUtil;

import java.io.File;

public class DownloadCallbackDbWrapper implements IDownloadCallback{

    public DownloadCallbackDbWrapper(IDownloadCallback callback) {
        this.callback = callback;
    }

    IDownloadCallback callback;

    @Override
    public boolean onBefore(String url, String realPath) {
        callback.onBefore(url, realPath);
        DownloadInfo load = DownloadInfoUtil.getDao().load(url);
        if(load != null ){
            load.updateTime = System.currentTimeMillis();
            if(load.downloadSuccess()){
                File file = new File(load.filePath);
                if(file.exists() ){
                    DownloadInfoUtil.getDao().update(load);
                    callback.onSuccess(url,load.filePath);
                    return false;
                }
            }else {
                DownloadInfoUtil.getDao().update(load);
            }
        }else {
            load = new DownloadInfo();
            load.createTime = System.currentTimeMillis();
            load.updateTime = System.currentTimeMillis();
            load.status = DownloadInfo.STATUS_ORIGINAL;
            load.url = url;
            load.filePath = realPath;
            load.name = realPath.substring(realPath.lastIndexOf("/")+1);
            load.dir = realPath.substring(0,realPath.lastIndexOf("/"));
            DownloadInfoUtil.getDao().insert(load);
        }
        return true;
    }

    @Override
    public void onStart(String url, String realPath) {
        callback.onStart(url, realPath);
        DownloadInfo load = DownloadInfoUtil.getDao().load(url);
        if(load == null){
            LogUtils.w("download info in db == null , "+ url);
            return;
        }
        load.status = DownloadInfo.STATUS_DOWNLOADING;
        DownloadInfoUtil.getDao().update(load);
    }

    @Override
    public void onSuccess(String url, String realPath) {
        callback.onSuccess(url, realPath);
        DownloadInfo load = DownloadInfoUtil.getDao().load(url);
        if(load == null){
            LogUtils.w("download info in db == null , "+ url);
            return;
        }
        load.status = DownloadInfo.STATUS_SUCCESS;
        DownloadInfoUtil.getDao().update(load);
        //File file = new File(realPath);
        //压缩图片

        //if(file.exists())
    }

    static long lastTime = 0;
    @Override
    public void progress(String url, String realPath, long currentOffset, long totalLength) {
        callback.progress(url, realPath, currentOffset, totalLength);

        if(System.currentTimeMillis() - lastTime < 1000){
            //每秒更新一下数据库
            return;
        }
        lastTime = System.currentTimeMillis();
        DownloadInfo load = DownloadInfoUtil.getDao().load(url);
        if(load == null){
            LogUtils.w("download info in db == null , "+ url);
            return;
        }
        load.status = DownloadInfo.STATUS_DOWNLOADING;
        load.currentOffset = currentOffset;
        load.totalLength = totalLength;
        DownloadInfoUtil.getDao().update(load);
    }

    @Override
    public void onFail(String url, String realPath, String msg, Throwable throwable) {
        callback.onFail(url, realPath, msg, throwable);

        DownloadInfo load = DownloadInfoUtil.getDao().load(url);
        if(load == null){
            LogUtils.w("download info in db == null , "+ url);
            return;
        }
        load.status = DownloadInfo.STATUS_FAIL;
       load.errMsg = msg;
        DownloadInfoUtil.getDao().update(load);
    }
}
