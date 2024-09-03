package com.hss.downloader.callback;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.hss.downloader.IDownloadCallback;
import com.hss.downloader.download.DownloadInfo;
import com.hss.downloader.download.DownloadInfoUtil;
import com.hss01248.img.compressor.ImageCompressor;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

public class DownloadCallbackDbDecorator implements IDownloadCallback {

    public DownloadCallbackDbDecorator(IDownloadCallback callback) {
        this.callback = callback;
    }

    IDownloadCallback callback;

    @Override
    public void onBefore(String url, String realPath, boolean forceRedownload) {
        callback.onBefore(url, realPath, forceRedownload);
    }

    public static File shouldStartRealDownload(String url, String realPath, boolean forceRedownload){
        DownloadInfo load = DownloadInfoUtil.getDao().load(url);
        if(load != null ){
            load.updateTime = System.currentTimeMillis();
            if(load.downloadSuccess() && !forceRedownload){
                File file = new File(load.filePath);
                if(file.exists() ){
                    load.status = DownloadInfo.STATUS_SUCCESS;
                    DownloadInfoUtil.getDao().update(load);
                    //callback.onSuccess(url,load.filePath);
                    return file;
                    //return ;
                }else {
                    LogUtils.w("下载成功过,但文件不存在",load);
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
        return null;
    }

    private void exeOnIo(Runnable runnable) {
        ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<Object>() {
            @Override
            public Object doInBackground() throws Throwable {
                runnable.run();
                return null;
            }

            @Override
            public void onSuccess(Object result) {

            }
        });
    }

    @Override
    public void onStart(String url, String realPath) {
        callback.onStart(url, realPath);
        exeOnIo(new Runnable(){

            @Override
            public void run() {
                DownloadInfo load = DownloadInfoUtil.getDao().load(url);
                if(load == null){
                    LogUtils.w("download info in db == null , "+ url);
                    return;
                }
                load.status = DownloadInfo.STATUS_DOWNLOADING;
                DownloadInfoUtil.getDao().update(load);
                EventBus.getDefault().post(load);
            }
        });

    }

    @Override
    public void onSuccess(String url, String realPath) {

        exeOnIo(new Runnable() {
            @Override
            public void run() {
                //压缩图片
                File compress = ImageCompressor.compress(realPath, false, false);
                if(!compress.exists() || compress.length() ==0){
                    LogUtils.e("file not exist after compress");
                    onFail(url,compress.getAbsolutePath(),"compress failed,file not exist",new Throwable("xxx"));
                    return;
                }
                callback.onSuccess(url, compress.getAbsolutePath());
                DownloadInfo load = DownloadInfoUtil.getDao().load(url);
                if(load == null){
                    LogUtils.w("download info in db == null onSuccess, "+ url);
                }else {
                    load.status = DownloadInfo.STATUS_SUCCESS;
                    load.filePath = realPath;
                    load.totalLength = compress.length();
                    load.currentOffset = compress.length();
                    DownloadInfoUtil.getDao().update(load);
                    EventBus.getDefault().post(load);
                }
            }
        });

    }

     long lastTime = 0;
    @Override
    public void onProgress(String url, String realPath, long currentOffset, long totalLength,long speed) {
        if(currentOffset == totalLength){
            lastTime = 0;
        }
        if(System.currentTimeMillis() - lastTime < 1000){
            //每秒更新一次进度
            return;
        }
        lastTime = System.currentTimeMillis();
        exeOnIo(new Runnable() {
            @Override
            public void run() {
                DownloadInfo load = DownloadInfoUtil.getDao().load(url);
                if(load == null){
                    LogUtils.w("download info in db == null , "+ url);
                    return;
                }
                load.status = DownloadInfo.STATUS_DOWNLOADING;
                load.currentOffset = currentOffset;
                load.totalLength = totalLength;
                DownloadInfoUtil.getDao().update(load);
                EventBus.getDefault().post(load);
            }
        });
        callback.onProgress(url, realPath, currentOffset, totalLength,speed);
    }

    @Override
    public void onFail(String url, String realPath, String msg, Throwable throwable) {


        exeOnIo(new Runnable() {
            @Override
            public void run() {
                DownloadInfo load = DownloadInfoUtil.getDao().load(url);
                if(load == null){
                    LogUtils.w("download info in db == null , "+ url);
                    return;
                }
                load.status = DownloadInfo.STATUS_FAIL;
                load.errMsg = msg;
                DownloadInfoUtil.getDao().update(load);
                EventBus.getDefault().post(load);
            }
        });
        callback.onFail(url, realPath, msg, throwable);

    }
}
