package com.hss01248.basewebview.download;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.hss01248.basewebview.IDownloader;

import java.io.File;

/**
 * @Despciption todo
 * @Author hss
 * @Date 25/11/2022 10:38
 * @Version 1.0
 */
public class SystemDownloader implements IDownloader {
    @Override
    public void doDownload(String url, String name, String dir) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        //设置在什么网络情况下进行下载
        //request.setAllowedNetworkTypes(Request.NETWORK_WIFI);
        //设置通知栏标题
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setTitle(name);
        request.setDescription("正在下载");
        //request.setAllowedOverRoaming(false);//漫游
        //设置文件存放目录
        //request.setDestinationUri(OpenUri.fromFile(Utils.getApp(),new File(dir)));
        request.setDestinationUri(Uri.fromFile(new File(dir)));
        DownloadManager downManager = (DownloadManager) Utils.getApp().getSystemService(Context.DOWNLOAD_SERVICE);
        long id = downManager.enqueue(request);
        ToastUtils.showLong("已加入系统下载队列,可在通知栏查看");
        completeListener(id, name, new Runnable() {
            @Override
            public void run() {
                Uri uri = downManager.getUriForDownloadedFile(id);
                LogUtils.i("download success",uri,name,id);
                //content://downloads/all_downloads/1453
                ToastUtils.showLong( "任务:" + id + ", 文件: "+name+" 下载完成!");
            }
        });

        //request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, "mydown");
    }

    // 根据DownloadManager下载的Id，查询DownloadManager某个Id的下载任务状态。
    private void queryStatus(long id, DownloadManager downManager) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id);
        Cursor cursor = downManager.query(query);

        String statusMsg = "";
        if (cursor.moveToFirst()) {
            //已经下载文件大小
            int downloaded = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            //下载文件的总大小
            int total = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_PAUSED:
                    statusMsg = "STATUS_PAUSED";
                case DownloadManager.STATUS_PENDING:
                    statusMsg = "STATUS_PENDING";
                case DownloadManager.STATUS_RUNNING:
                    statusMsg = "STATUS_RUNNING";
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    statusMsg = "STATUS_SUCCESSFUL";
                    break;
                case DownloadManager.STATUS_FAILED:
                    statusMsg = "STATUS_FAILED";
                    break;

                default:
                    statusMsg = "未知状态";
                    break;
            }

            ToastUtils.showLong(statusMsg);
        }
    }

    private void completeListener(final long id, String name, @Nullable  Runnable completeCallback) {

        // 注册广播监听系统的下载完成事件。
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        BroadcastReceiver  broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long ID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (ID == id) {
                    Utils.getApp().unregisterReceiver(this);
                    if(completeCallback != null){
                        completeCallback.run();
                    }
                }
            }
        };
        Utils.getApp().registerReceiver(broadcastReceiver, intentFilter);
    }

}
