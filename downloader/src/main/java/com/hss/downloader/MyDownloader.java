package com.hss.downloader;

import android.app.ProgressDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hss.downloader.download.DownloadInfo;
import com.hss.downloader.download.DownloadInfoUtil;
import com.hss.downloader.download.DownloadResultEvent;
import com.hss.downloader.download.TurboCompressor;
import com.hss.downloader.download.db.DownloadInfoDao;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.StatusUtil;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.liulishuo.okdownload.core.dispatcher.DownloadDispatcher;
import com.liulishuo.okdownload.core.listener.DownloadListener1;
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyDownloader {


    public static void fixDbWhenUpdate(){
        if(true){
            return;
        }
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
                updateBatch(batchSize,count);
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

    private static void updateBatch(int batchSize, long count) {
        LogUtils.d("开始修复一批,待修复:"+count);
        List<DownloadInfo> list = DownloadInfoUtil.getDao().queryBuilder()
                .where(DownloadInfoDao.Properties.FilePath.isNotNull())
                .offset(0).limit(batchSize).list();
        if(list.isEmpty()){
            LogUtils.d("修复完成");
            return;
        }
        for (DownloadInfo info : list) {
            if(!info.filePath.startsWith("/storage/")){
                LogUtils.d("非文件路径:"+info.filePath);
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
            updateBatch(batchSize, count);
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
        DownloadDispatcher.setMaxParallelRunningCount(6);
        ThreadUtils.executeByIo(new ThreadUtils.Task<List<DownloadInfo>>() {
            @Override
            public List<DownloadInfo> doInBackground() throws Throwable {
                List<DownloadInfo> list = DownloadInfoUtil.getDao().queryBuilder()
                        .where(DownloadInfoDao.Properties.Status.notEq(DownloadInfo.STATUS_SUCCESS)).list();
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
        DownloadDispatcher.setMaxParallelRunningCount(6);
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
                        load.name = info.name;
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
        DownloadTask task = new DownloadTask.Builder(info.url, info.dir,info.name)
                // the minimal interval millisecond for callback progress
                .setMinIntervalMillisCallbackProcess(100)
                .setConnectionCount(1)
                // do re-download even if the task has already been completed in the past.
                .setPassIfAlreadyCompleted(true)
                .build();
        task.enqueue(new DownloadListener1() {
            @Override
            public void taskStart(@NonNull DownloadTask task, @NonNull Listener1Assist.Listener1Model model) {
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
            public void retry(@NonNull DownloadTask task, @NonNull ResumeFailedCause cause) {

            }

            @Override
            public void connected(@NonNull DownloadTask task, int blockCount, long currentOffset, long totalLength) {
                runOnBack(new Runnable(){

                    @Override
                    public void run() {
                        info.totalLength = totalLength;
                        info.status = DownloadInfo.STATUS_DOWNLOADING;
                        DownloadInfoUtil.getDao().update(info);
                        EventBus.getDefault().post(info);
                    }
                });

            }

            @Override
            public void progress(@NonNull DownloadTask task, long currentOffset, long totalLength) {
                info.currentOffset = currentOffset;
                info.status = DownloadInfo.STATUS_DOWNLOADING;
                EventBus.getDefault().post(info);
            }

            @Override
            public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause, @Nullable Exception realCause, @NonNull Listener1Assist.Listener1Model model) {

                LogUtils.i(cause.name()+" ,"+task.getUrl()+" ,"+realCause);
                runOnBack(new Runnable() {
                    @Override
                    public void run() {
                        if(cause.equals(EndCause.COMPLETED)){
                            info.status = DownloadInfo.STATUS_SUCCESS;
                            if(task.getFile() != null){
                                info.totalLength = task.getFile().length();
                            }
                            try {
                                DownloadInfoUtil.getDao().update(info);
                            }catch (Throwable throwable){
                                throwable.printStackTrace();
                            }
                            compressImage(info);
                        }else {
                            String des = cause.name();
                            if(realCause != null){
                                realCause.printStackTrace();
                                des = des+","+cause.getClass().getSimpleName()+" "+realCause.getMessage();
                            }
                            info.status = DownloadInfo.STATUS_FAIL;
                            info.errMsg = des;
                            DownloadInfoUtil.getDao().update(info);
                        }
                        EventBus.getDefault().post(info);
                        EventBus.getDefault().post(new DownloadResultEvent(cause.equals(EndCause.COMPLETED)));
                    }
                });

            }
        });
    }

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
        runOnBack(new Runnable() {
            @Override
            public void run() {
               boolean compressed =  TurboCompressor.compressOriginal(info.dir+"/"+info.name,80);
               if(compressed){
                   info.totalLength = new File(info.dir,info.name).length();
                   EventBus.getDefault().post(info);
               }
            }
        });

    }
}
