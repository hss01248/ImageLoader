package com.hss.downloader;

import android.app.ProgressDialog;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hss.downloader.download.CompressEvent;
import com.hss.downloader.download.DownloadInfo;
import com.hss.downloader.download.DownloadInfoUtil;
import com.hss.downloader.event.DownloadResultEvent;

import com.hss.downloader.download.db.DownloadInfoDao;
import com.hss01248.img.compressor.ImageCompressor;
import com.liulishuo.okdownload.StatusUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyDownloader {




    public static void fixDbWhenUpdate(){
       /* if(true){
            return;
        }*/
        ProgressDialog dialog = new ProgressDialog(ActivityUtils.getTopActivity());
        dialog.setMessage("修复上个版本的数据库...");
        dialog.show();
        ThreadUtils.executeByIo(new ThreadUtils.Task<Object>() {
            @Override
            public Object doInBackground() throws Throwable {
                long count = DownloadInfoUtil.getDao().queryBuilder()
                        .where(DownloadInfoDao.Properties.FilePath.isNotNull())
                        .count();
                int batchSize = 200;
                updateBatch(batchSize,count,0);
                return null;
            }

            @Override
            public void onSuccess(Object result) {
                dialog.dismiss();
                ToastUtils.showShort("修复完成");
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {

            }
        });
    }

    private static void updateBatch(int batchSize, long count,int batchIdx) {
        LogUtils.d("开始修复一批,待修复:"+count);
        List<DownloadInfo> list = DownloadInfoUtil.getDao().queryBuilder()
                .where(DownloadInfoDao.Properties.FilePath.isNotNull())
                .offset(batchIdx*batchSize).limit(batchSize).list();
        if(list.isEmpty()){
            LogUtils.d("修复完成");
            return;
        }
        for (DownloadInfo info : list) {
            if(!info.filePath.startsWith("/storage/")){
               // LogUtils.d("非文件路径:"+info.filePath);
                continue;
            }
            File file = new File(info.filePath);
            info.name = file.getName();
            info.dir = file.getParentFile().getAbsolutePath();
            info.filePath = null;
            if(info.status == DownloadInfo.STATUS_DOWNLOADING){
                //info.status = DownloadInfo.STATUS_ORIGINAL;
            }else if(info.status == DownloadInfo.STATUS_ORIGINAL){
                info.status = DownloadInfo.STATUS_SUCCESS;
            }
            if(file.exists()){
                info.totalLength = file.length();
                info.createTime = file.lastModified();
            }

        }
        try {
            //LogUtils.d("入库修复:"+count);
            DownloadInfoUtil.getDao().updateInTx(list);
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }
        count -= list.size();
        if(count >0){
            //LogUtils.d("又修复了一批,待修复:"+count);
            updateBatch(batchSize, count,batchIdx+1);
        }else {
            LogUtils.d("修复完成");
        }

    }


    public static void showDownloadPage(){
        new DownloadList().showList(ActivityUtils.getTopActivity(),null);
    }

    public static void continueDownload(){
        ProgressDialog dialog = new ProgressDialog(ActivityUtils.getTopActivity());
        dialog.setMessage("查询数据库...");
        dialog.show();
        ThreadUtils.executeByIo(new ThreadUtils.Task<List<DownloadInfo>>() {
            @Override
            public List<DownloadInfo> doInBackground() throws Throwable {
                List<DownloadInfo> list = DownloadInfoUtil.getDao().queryBuilder()
                        .where(DownloadInfoDao.Properties.Status.notEq(DownloadInfo.STATUS_SUCCESS))
                        .limit(2000)
                        .orderDesc(DownloadInfoDao.Properties.CreateTime).list();
                if(list ==null || list.isEmpty()){
                    ToastUtils.showShort("no results");
                    return new ArrayList<>();
                }

                for (DownloadInfo downloadInfo : list) {
                    startDownload(downloadInfo);
                }
                return list;
            }

            @Override
            public void onSuccess(List<DownloadInfo> result) {
                dialog.dismiss();
                new DownloadList().showList(ActivityUtils.getTopActivity(),result);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {

            }
        });
    }


    public static void download(List<DownloadUrls> urls){
        //入库
        //开始下载
        ThreadUtils.executeByIo(new ThreadUtils.Task<List<DownloadInfo>>() {
            @Override
            public List<DownloadInfo> doInBackground() throws Throwable {
                List<DownloadInfo> toShow = new ArrayList<>();
               /* List<DownloadInfo> toAdd = new ArrayList<>();
                List<DownloadInfo> toUpdate = new ArrayList<>();*/
                for (DownloadUrls info : urls) {
                    DownloadInfo load = DownloadInfoUtil.getDao().load(info.url);
                    if(load != null && load.status == DownloadInfo.STATUS_SUCCESS){
                        toShow.add(load);
                        continue;
                    }
                    if(load == null){
                        load = new DownloadInfo();
                        load.url = info.url;
                        load.status = DownloadInfo.STATUS_ORIGINAL;
                        load.dir = info.dir;
                        load.name = info.name.replace("/","-");//避免多级目录
                        load.createTime = System.currentTimeMillis();
                        //兼具去重功能  功能优先于性能
                        try {
                            DownloadInfoUtil.getDao().insert(load);
                        }catch (Throwable throwable){
                            throwable.printStackTrace();
                        }


                    }else {
                        load.dir = info.dir;
                        load.name = info.name;
                        if(load.status == DownloadInfo.STATUS_DOWNLOADING){
                            StatusUtil.Status status = StatusUtil.getStatus(info.url, load.dir, load.name);
                            if( status == StatusUtil.Status.RUNNING
                                    || status == StatusUtil.Status.PENDING){
                                toShow.add(load);
                                continue;
                            }
                        }
                        load.status = DownloadInfo.STATUS_ORIGINAL;
                        load.createTime = System.currentTimeMillis();
                        try {
                            DownloadInfoUtil.getDao().update(load);
                        }catch (Throwable throwable){
                            throwable.printStackTrace();
                        }

                    }
                    try {
                        startDownload(load);
                        toShow.add(load);
                    }catch (Throwable throwable){
                        throwable.printStackTrace();
                    }

                }
               /* try {
                    DownloadInfoUtil.getDao().updateInTx(toUpdate);
                }catch (Throwable throwable){
                    throwable.printStackTrace();
                }
                try {
                    DownloadInfoUtil.getDao().insertInTx(toAdd);
                }catch (Throwable throwable){
                    throwable.printStackTrace();
                }*/

                //开始下载
                /*toAdd.addAll(toUpdate);
                for (DownloadInfo info : toAdd) {
                    try {
                        startDownload(info);
                    }catch (Throwable throwable){
                        throwable.printStackTrace();
                    }
                }*/
                return toShow;
            }

            @Override
            public void onSuccess(List<DownloadInfo> result) {
                new DownloadList().showList(ActivityUtils.getTopActivity(),result);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {
                t.printStackTrace();

            }
        });

    }

    public static void startDownload(DownloadInfo info) {
         if(download == null){
             LogUtils.w("download == null");
             return;
         }
         download.download(info.url, info.dir + "/" + info.name, new IDownloadCallback() {
             @Override
             public void onStart(String url) {
                 runOnBack(new Runnable(){

                     @Override
                     public void run() {
                         info.status = DownloadInfo.STATUS_DOWNLOADING;
                         DownloadInfoUtil.getDao().update(info);
                         EventBus.getDefault().post(info);
                     }
                 });
             }

             @Override
             public void onSuccess(String url) {

                 info.status = DownloadInfo.STATUS_SUCCESS;
                 try {
                     DownloadInfoUtil.getDao().update(info);
                 }catch (Throwable throwable){
                     throwable.printStackTrace();
                 }

                 //EventBus.getDefault().post(info);
                 EventBus.getDefault().post(new DownloadResultEvent(true));
                 compressImage(info);
             }

             @Override
             public void progress(String url, long currentOffset, long totalLength) {
                 info.currentOffset = currentOffset;
                 info.totalLength = totalLength;
                 info.status = currentOffset == totalLength ? DownloadInfo.STATUS_SUCCESS : DownloadInfo.STATUS_DOWNLOADING;
                 EventBus.getDefault().post(info);
             }

             @Override
             public void onFail(String url, String msg, Throwable throwable) {
                 LogUtils.w(throwable);
                 info.status = DownloadInfo.STATUS_FAIL;
                 info.errMsg = msg;
                 DownloadInfoUtil.getDao().update(info);

                 EventBus.getDefault().post(info);
                 EventBus.getDefault().post(new DownloadResultEvent(false));
             }
         });
    }

    public static void setDownload(IDownload download) {
        MyDownloader.download = download;
    }

    static   IDownload download = new OkDownloadImpl();
    private static void runOnBack(Runnable runnable) {
        ThreadUtils.executeByIo(new ThreadUtils.Task<Object>() {
            @Override
            public Object doInBackground() throws Throwable {
                runnable.run();
                return null;
            }

            @Override
            public void onSuccess(Object result) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {
                t.printStackTrace();

            }
        });
    }

    private static void compressImage(DownloadInfo info) {

        File file = new File(info.dir,info.name);
        long start = System.currentTimeMillis();
        info.isCompressing = true;
        EventBus.getDefault().post(info);
        ImageCompressor.compressToAvifAsync(file.getAbsolutePath(), true, false,
                new ImageCompressor.Callback() {
            @Override
            public void onResult(File compressed, boolean hasCompressed) {
                CompressEvent event = new CompressEvent();
                event.success = compressed.length() != file.length();
                event.timeCost = System.currentTimeMillis() - start;
                event.after = compressed.length();
                event.origianl = file.length();
                EventBus.getDefault().post(event);



                info.isCompressing = false;
                info.name = compressed.getName();
                info.dir = compressed.getParentFile().getAbsolutePath();
                info.totalLength = compressed.length();
                EventBus.getDefault().post(info);
                DownloadInfoUtil.getDao().update(info);
            }
        });

    }
}
