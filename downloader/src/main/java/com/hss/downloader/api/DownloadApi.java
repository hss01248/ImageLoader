package com.hss.downloader.api;

import android.os.Environment;
import android.text.TextUtils;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.Utils;
import com.hss.downloader.IDownload;
import com.hss.downloader.IDownloadCallback;
import com.hss.downloader.OkDownloadImpl2;
import com.hss.downloader.callback.DefaultUIDownloadCallback;
import com.hss.downloader.callback.DownloadCallbackDbDecorator;
import com.hss.downloader.callback.ExeOnMainDownloadCallback;
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

    public DownloadApi setShowDefaultLoadingAndToast(boolean showDefaultLoadingAndToast) {
        this.showDefaultLoadingAndToast = showDefaultLoadingAndToast;
        return this;
    }

    private boolean showDefaultLoadingAndToast = false;

    public DownloadApi setNeedCheckDbBeforeStart(boolean needCheckDbBeforeStart) {
        this.needCheckDbBeforeStart = needCheckDbBeforeStart;
        return this;
    }

    private boolean needCheckDbBeforeStart = true;

    public boolean isForceReDownload() {
        return forceReDownload;
    }

    public void setForceReDownload(boolean forceReDownload) {
        this.forceReDownload = forceReDownload;
    }

    private boolean forceReDownload;
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
        return (dir+"/"+name).replace("//","/");
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public IDownloadCallback getCallback() {
        return callback;
    }

    private IDownloadCallback callback;
   public static IDownload2 downlodImpl = new OkDownloadImpl2();

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
        if(showDefaultLoadingAndToast){
            this.callback = new DownloadCallbackDbDecorator(new ExeOnMainDownloadCallback(new DefaultUIDownloadCallback(callback)));
        }else {
            this.callback = new DownloadCallbackDbDecorator(callback);
        }



        ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<File>() {
            @Override
            public File doInBackground() throws Throwable {
                beforStart();
                String path = getRealPath();
                LogUtils.d("prepare to download",path);
                callback.onBefore(url,path,forceReDownload);
                if(!needCheckDbBeforeStart){
                    //批量下载的,在list api里批量判断
                    return null;
                }
                // 优化判断逻辑: 判断一个文件是否下载过,是否强制下载
                return DownloadCallbackDbDecorator.shouldStartRealDownload(url,path,forceReDownload);
            }

            @Override
            public void onSuccess(File result) {
                if(result == null){
                    downlodImpl.download(DownloadApi.this);
                }else {
                    callback.onSuccess(url,result.getAbsolutePath());
                }
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                callback.onFail(url,getRealPath(),t.getMessage(),t);
            }
        });
    }

    private boolean beforStart() {
        if(TextUtils.isEmpty(name)){
            name = IDownload.getFileName(url);
        }
        name = DownloadInfoUtil.getLeagalFileName(name);

        if(!noChangeDir){
            dir = determinDir(dir);
        }
        //解决同个文件夹下文件name重名问题:
        File file = new File(dir,name);
        if(file.exists()){
            String end = "bin";
            String realName = name;
            if(name.contains(".")){
                end = name.substring(name.lastIndexOf(".")+1);
                realName = name.substring(0,name.lastIndexOf("."));
            }
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                file = new File(dir,realName+"-"+i+"."+end);
                if(!file.exists()){
                    name = file.getName();
                    break;
                }
            }
        }
        return true;
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
