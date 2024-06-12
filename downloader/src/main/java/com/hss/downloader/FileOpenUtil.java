package com.hss.downloader;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.hss01248.apkinstaller.ApkInstallUtil;
import com.hss01248.apkinstaller.InstallCallback;
import com.hss01248.bigimageviewpager.LargeImageViewer;
import com.hss01248.openuri2.OpenUri2;
import com.hss01248.toast.MyToast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @Despciption todo
 * @Author hss
 * @Date 23/02/2022 11:47
 * @Version 1.0
 */
public class FileOpenUtil {


    public static List<String> paths = new ArrayList<>();

    public static void open(String filePath){
        if(TextUtils.isEmpty(filePath)){
            ToastUtils.showShort("路径为空");
            return;
        }
        if(filePath.startsWith("http://") || filePath.startsWith("https://")){
            ToastUtils.showShort("网络url,请先下载");
            //DownloadApi.create(filePath).callback(new DefaultSilentDownloadCallback());
            return;
        }
        if(filePath.endsWith(".apk")){
            installApk(filePath);
            return;
        }
        String mimeType = getMineType2(filePath);
        if(filePath.endsWith("jpeg") || filePath.endsWith("JPG")){
            mimeType = "image/jpeg";
        }
        if(mimeType.contains("image") ){
            int position = paths.indexOf(filePath);
            if(position < 0){
                LargeImageViewer.showOne(filePath);
            }else {
                LargeImageViewer.showInBatch(paths,position);
            }
            return;
        }
        //调用系统intent来打开
        oepnFileByIntent(filePath);


        /*if(mimeType.contains("video") ){
            MyToast.show("todo");
            return;
        }*/
    }

    private static void oepnFileByIntent(String filePath) {
        try{
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = OpenUri2.fromFile(Utils.getApp(), new File(filePath));
            intent.setData(uri);
            OpenUri2.addPermissionR(intent);
            ActivityUtils.getTopActivity().startActivity(intent);
        }catch (Throwable throwable){
            MyToast.error(throwable.getMessage());
        }
    }

    public static String getMineType2(String filePath) {
        int index = filePath.lastIndexOf(".");
        String suffix = "";
        if(index > 0){
            suffix = filePath.substring(index+1).toLowerCase();
        }
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
        if(type ==null || type.endsWith("")){
            if(filePath.endsWith("jpeg")){
                type = "image/jpeg";
            }else {
                type = "";
            }
        }
        return type;
    }

    private static void installApk(String filePath) {
        ApkInstallUtil.checkAndInstallApk(filePath, new InstallCallback() {
            @Override
            public void onError(String code, String msg) {
                MyToast.error(msg);
                oepnFileByIntent(filePath);
            }
        });

    }
}
