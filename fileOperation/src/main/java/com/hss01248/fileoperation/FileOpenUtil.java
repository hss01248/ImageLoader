package com.hss01248.fileoperation;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ReflectUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.hss01248.apkinstaller.ApkInstallUtil;
import com.hss01248.apkinstaller.InstallCallback;
import com.hss01248.openuri2.OpenUri2;

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




    public static void open(String filePath,@Nullable List<String> paths){
        if(TextUtils.isEmpty(filePath)){
            ToastUtils.showShort("路径为空");
            return;
        }

        if(filePath.endsWith(".apk") && !filePath.startsWith("http")){
            installApk(filePath);
            return;
        }
        String mimeType = FileTypeUtil2.getTypeByFileName(filePath);
        if(filePath.endsWith("jpeg") || filePath.endsWith("JPG")){
            mimeType = "image/jpeg";
        }
        if(paths ==null || paths.isEmpty()){
            paths = new ArrayList<>();
            paths.add(filePath);
        }
        int position = paths.indexOf(filePath);
        if(position < 0){
            paths = new ArrayList<>();
            paths.add(filePath);
            position = 0;
            //LargeImageViewer.showOne(filePath);
        }
        if(("image".equals(mimeType))){
            try {
                ReflectUtils.reflect("com.hss01248.bigimageviewpager.LargeImageViewer")
                        .method("showInBatch",paths,position)
                        .get();
            }catch (Throwable throwable){
                ToastUtils.showLong(throwable.getMessage());
            }
        }else if("video".equals(mimeType)){
            //VideoPlayUtil.playList( List<String> sources, int currentPosition)
            try {
                ReflectUtils.reflect("com.hss01248.media.localvideoplayer.VideoPlayUtil")
                        .method("playList",paths,position)
                        .get();
            }catch (Throwable throwable){
                ToastUtils.showLong(throwable.getMessage());
            }
        }else{
            //调用系统intent来打开
            oepnFileByIntent(filePath);
        }
    }



    private static void oepnFileByIntent(String filePath) {
        try{
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(new File(filePath).exists()){
                Uri uri = OpenUri2.fromFile(Utils.getApp(), new File(filePath));
                intent.setData(uri);
                OpenUri2.addPermissionR(intent);
            }else {
                Uri uri = Uri.parse(filePath);
                intent.setData(uri);
            }
            ActivityUtils.getTopActivity().startActivity(intent);
        }catch (Throwable throwable){
            ToastUtils.showShort(throwable.getMessage());
        }
    }

    private static void installApk(String filePath) {
        ApkInstallUtil.checkAndInstallApk(filePath, new InstallCallback() {
            @Override
            public void onError(String code, String msg) {
                ToastUtils.showLong(msg);
                oepnFileByIntent(filePath);
            }
        });

    }
}
