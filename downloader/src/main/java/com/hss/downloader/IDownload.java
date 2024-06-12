package com.hss.downloader;

import android.os.Environment;
import android.text.TextUtils;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.hss.downloader.download.DownloadInfo;
import com.hss.downloader.download.DownloadInfoUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public interface IDownload {

    void download(String url, @NonNull String filePath, @NonNull Map<String,String> headers, IDownloadCallback callback);

    void stopDownload(String url);

    /**
     * 解决问题:
     * 文件名过长
     * 同一个文件夹下文件数量过大: 大于1000
     * @param url
     * @param filePath
     * @param headers
     * @param callback
     */
   default void prepareDownload(String url, @NonNull String filePath, @NonNull Map<String,String> headers, IDownloadCallback callback){
       DownloadInfo load = DownloadInfoUtil.getDao().load(url);
       if(load != null ){
           load.updateTime = System.currentTimeMillis();
           if(load.downloadSuccess()){
               File file = new File(load.filePath);
               if(file.exists() ){
                   DownloadInfoUtil.getDao().update(load);
                   callback.onSuccess(url,load.filePath);
                   return;
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
           load.filePath = filePath;
           load.name = filePath.substring(filePath.lastIndexOf("/")+1);
           load.dir = filePath.substring(0,filePath.lastIndexOf("/"));
           DownloadInfoUtil.getDao().insert(load);
       }
       download(url, filePath, headers, callback);
   }

    default void download(String url, @NonNull String filePath,  IDownloadCallback callback){
        download(url,filePath,new HashMap<>(),callback);
    }

    default void download(String url, @NonNull File dir,  IDownloadCallback callback){
      String  fileName = getFileName(url);
        fileName = DownloadInfoUtil.getLeagalFileName(fileName);
        download(url,new File(dir,fileName).getAbsolutePath(),new HashMap<>(),callback);
    }

    static String getFileName(String url,String contentDisposition, String mimetype){
        String  fileName = URLUtil.guessFileName(url,contentDisposition,mimetype);
        String suffix = "";
        if(fileName.endsWith(".bin") ){
            suffix = genExt(url);
            if(!TextUtils.isEmpty(suffix)){
                fileName = fileName.substring(0,fileName.length()-3)+suffix;
            }
        }
        return fileName;
    }
    static String getFileName(String url){
        return getFileName(url,"","");
    }

    static String genExt(String url) {
        if(url.contains("?")){
            url = url.substring(0,url.indexOf("?"));
        }
        if(url.contains("/")&& !url.endsWith("/")){
            url = url.substring(url.lastIndexOf("/")+1);
            if(url.contains(".")){
                url = url.substring(url.lastIndexOf(".")+1);
                return url;
            }
        }
        return "";
    }

    default void download(String url,  IDownloadCallback callback,@Nullable String fileName){
        if(TextUtils.isEmpty(fileName)){
            fileName =  getFileName(url);;
        }
        fileName = DownloadInfoUtil.getLeagalFileName(fileName);
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
