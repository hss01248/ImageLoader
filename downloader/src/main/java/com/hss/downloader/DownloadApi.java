package com.hss.downloader;

import android.os.Environment;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.hss.downloader.download.DownloadInfoUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @Despciption todo
 * @Author hss
 * @Date 03/02/2023 18:07
 * @Version 1.0
 */
public class DownloadApi {

    private String url;
    private String dir;
    private String name;
    private boolean noChangeDir;
    private Map<String,String> headers = new HashMap<>();

    public String getUrl() {
        return url;
    }

    public String getDir() {
        return dir;
    }

    public String getName() {
        return name;
    }

    public String getRealPath() {
        return (dir+File.pathSeparator+name).replace(File.pathSeparator+File.pathSeparator,File.pathSeparator);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public IDownloadCallback getCallback() {
        return callback;
    }

    private IDownloadCallback callback;
   public static IDownload2 downlodImpl = new DownloadImplByFileDownloader();

    public DownloadApi setDir(String dir) {
        this.dir = dir;
        return this;
    }
    public DownloadApi setDirForce(String dir) {
        this.dir = dir;
        noChangeDir = true;
        return this;
    }

    public DownloadApi setName(String name) {
        this.name = name;
        return this;
    }

    public DownloadApi addHeader(String key,String val) {
        this.headers.put(key, val);
        return this;
    }

    public void callback(IDownloadCallback callback) {
        this.callback = new DownloadCallbackDbWrapper(callback);
        if(beforStart()){
            downlodImpl.download(this);
        }
    }

    private boolean beforStart() {
        if(TextUtils.isEmpty(name)){
            name = URLUtil.guessFileName(url,"","");
        }
        name = DownloadInfoUtil.getLeagalFileName(name);

        if(!noChangeDir){
            dir = determinDir(dir);
        }
        return callback.onBefore(url,getRealPath());
    }

    public static String determinDir(String dir ) {
        if(TextUtils.isEmpty(dir)){
            dir = getSaveDir().getAbsolutePath();
        }
        File folder = new File(dir);
        folder.mkdirs();
        String[] list = folder.list();
        //一级目录下最多500个文件/文件夹
        if(list != null && list.length> 500){
            for (int i = 0; i < 10000; i++) {
                //最多10000个文件夹
                File subFolder = new File(folder,folder.getName()+"_"+i);
                if(!subFolder.exists()){
                    subFolder.mkdirs();
                    dir = subFolder.getAbsolutePath();
                    return dir;
                }
                String[] list1 = subFolder.list();
                //二级目录下最多1000个文件
                if(list1 == null || list1.length < 1000){
                    dir = subFolder.getAbsolutePath();
                    return dir;
                }
            }
        }
        return dir;
    }



    public static DownloadApi create(String url){
       return new DownloadApi(url);
   }

    private DownloadApi(String url) {
        this.url = url;
    }

    static File getSaveDir(){
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
