package com.hss01248.fileoperation;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ReflectUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.hss01248.apkinstaller.ApkInstallUtil;
import com.hss01248.apkinstaller.InstallCallback;
import com.hss01248.bigimageviewpager.LargeImageViewer;
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



    public static void openBeforeFilter(String filePath,@Nullable List<String> paths){
        if(paths==null ){
            paths = new ArrayList<>();
            open(filePath, paths);
            return;
        }
        if(paths.isEmpty()){
            open(filePath, paths);
            return;
        }
        int type = FileTypeUtil2.getTypeIntByFileName(filePath);
        List<String> paths2 = new ArrayList<>(paths.size());
        if(FileTypeUtil2.isImageOrVideo(filePath)){
            for (String o : paths) {
                if(FileTypeUtil2.getTypeIntByFileName(o)==type){
                    paths2.add(o);
                }
            }
        }
        open(filePath,paths2);

    }

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
            mimeType = "image";
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
            LargeImageViewer.showInBatch(paths,position);
        }else if("video".equals(mimeType)){
            try {
                ReflectUtils.reflect("com.hss01248.media.localvideoplayer.VideoPlayUtil")
                        .method("startPreviewInList",ActivityUtils.getTopActivity(),paths,position)
                        .get();
            }catch (Throwable throwable){
                ToastUtils.showShort(throwable.getMessage());
            }

            //VideoPlayUtil.startPreviewInList(ActivityUtils.getTopActivity(),paths,position);
        }else{
            //调用系统intent来打开
            oepnFileByIntent(filePath);
        }
    }



    private static void oepnFileByIntent(String filePath) {
        try{
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = null;
            if(new File(filePath).exists()){
                 uri = OpenUri2.fromFile(Utils.getApp(), new File(filePath));
                intent.setData(uri);
                OpenUri2.addPermissionR(intent);
            }else {
                 uri = Uri.parse(filePath);
                intent.setData(uri);
            }
            String name = new File(filePath).getName();
            if(name.contains(".")){
                name = name.substring(name.lastIndexOf(".")+1);
            }
            name = name.toLowerCase();
            String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(name);
            if(!TextUtils.isEmpty(type)){
                intent.setDataAndType(uri, type);
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
