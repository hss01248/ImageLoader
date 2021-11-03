package com.hss01248.webviewspider.basewebview;

import android.content.DialogInterface;
import android.os.Environment;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;

import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.hss.downloader.DownloadUrls;
import com.hss.downloader.MyDownloader;
import com.hss.downloader.download.DownloadInfo;

import java.util.ArrayList;
import java.util.List;

public class WebviewDownlader implements DownloadListener {
    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        LogUtils.i(url,userAgent,contentDisposition,mimetype,contentLength);
        String name = URLUtil.guessFileName(url,contentDisposition,mimetype);
        String str = "是否从\n"+url+"下载文件:\n"+name+"?\n文件大小预计:"+ ConvertUtils.byte2FitMemorySize(contentLength,1);

        AlertDialog dialog  = new AlertDialog.Builder(ActivityUtils.getTopActivity())
                .setTitle("文件下载")
                .setMessage(str)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doDownload(url,name);

                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();


    }

    private void doDownload(String url, String name) {
        List<DownloadUrls> downloadInfos = new ArrayList<>();
        DownloadUrls info = new DownloadUrls();
        info.url = url;
        info.name = name;
        info.dir = Utils.getApp().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        downloadInfos.add(info);
        MyDownloader.download(downloadInfos);
    }
}
