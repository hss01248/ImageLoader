package com.hss.downloader;

import android.os.Environment;
import android.text.TextUtils;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public interface IDownload {

    void download(String url, @NonNull String filePath, @NonNull Map<String,String> headers, IDownloadCallback callback);

    default void download(String url, @NonNull String filePath,  IDownloadCallback callback){
        download(url,filePath,new HashMap<>(),callback);
    }

    default void download(String url, @NonNull File dir,  IDownloadCallback callback){
      String  fileName = URLUtil.guessFileName(url,"","");
        download(url,new File(dir,fileName).getAbsolutePath(),new HashMap<>(),callback);
    }

    default void download(String url,  IDownloadCallback callback,@Nullable String fileName){
        if(TextUtils.isEmpty(fileName)){
            fileName = URLUtil.guessFileName(url,"","");
        }
        String path = new File(getSaveDir(),fileName).getAbsolutePath();
        download(url,path,callback);
    }

   default void download(String url, IDownloadCallback callback){
       download(url,callback,"");
   }

   default File getSaveDir(){
       File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
       dir.mkdirs();
       LogUtils.i("sd卡是否有写权限: "+ dir.canWrite()+", "+dir.getAbsolutePath());
       if(dir.canWrite()){
           File file = new File(dir, AppUtils.getAppName());
           file.mkdirs();
           return file;
       }else {
          return Utils.getApp().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
       }
   }

}
