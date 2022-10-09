package com.hss01248.basewebview;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;

import com.blankj.utilcode.util.Utils;
import com.hss01248.openuri.OpenUri;

import java.io.File;

/**
 * @Despciption todo
 * @Author hss
 * @Date 09/10/2022 12:21
 * @Version 1.0
 */
public class DefaultWebConfig implements WebviewInit{
    @Override
    public Class html5ActivityClass() {
        return BaseWebviewActivity.class;
    }

    @Override
    public IDownloader getIDownloader() {
        return new IDownloader() {
            @Override
            public void doDownload(String url, String name, String dir) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                //设置在什么网络情况下进行下载
                //request.setAllowedNetworkTypes(Request.NETWORK_WIFI);
                //设置通知栏标题
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                request.setTitle(name);
                request.setDescription("正在下载");
                request.setAllowedOverRoaming(false);
                //设置文件存放目录
                request.setDestinationUri(OpenUri.fromFile(Utils.getApp(),new File(dir)));
                DownloadManager downManager = (DownloadManager)Utils.getApp().getSystemService(Context.DOWNLOAD_SERVICE);
               long id= downManager.enqueue(request);

                //request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, "mydown");

            }
        };
    }
}
