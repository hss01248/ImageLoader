package com.hss.downloader.api;

import android.text.TextUtils;
import android.webkit.URLUtil;

import com.blankj.utilcode.util.ThreadUtils;
import com.hss.downloader.IDownload;
import com.hss.downloader.callback.DefaultSilentDownloadCallback;
import com.hss.downloader.download.DownloadInfo;
import com.hss.downloader.download.DownloadInfoUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Administrator
 * @date: 2023/2/7
 * @desc: //todo
 */
public class DownloadListApi {

    private List<String> urls;
   // private Map<String,Do> urls;

    private String dir;

    public DownloadListApi setCommonFileNamePrefix(String commonFileNamePrefix) {
        this.commonFileNamePrefix = commonFileNamePrefix;
        return this;
    }

    public DownloadListApi setWithIndexNumber(boolean withIndexNumber) {
        this.withIndexNumber = withIndexNumber;
        return this;
    }

    private boolean withIndexNumber = true;

    private String commonFileNamePrefix;

    private DownloadListApi(List<String> urls) {
        this.urls = urls;
    }

    public static DownloadListApi create(List<String> urls){
        return new DownloadListApi(urls);
    }

    public DownloadListApi setDir(String dir) {
        this.dir = dir;
        return this;
    }

    public DownloadListApi addHeader(String key,String val) {
        this.headers.put(key, val);
        return this;
    }

    private Map<String,String> headers = new HashMap<>();

    public void start(){
        // 一万条时,loading,子线程写数据库
        ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<List<String>>() {
            @Override
            public List<String> doInBackground() throws Throwable {
                DownloadListApi.this.dir = DownloadApi.determinDir(dir);
                int i = 0;
                for (String url : urls) {
                    i++;
                    DownloadInfo load = DownloadInfoUtil.getDao().load(url);
                    if(load != null && load.status == DownloadInfo.STATUS_SUCCESS){
                        load.updateTime = System.currentTimeMillis();
                        DownloadInfoUtil.getDao().update(load);
                        continue;
                    }
                    String name = IDownload.getFileName(url);
                    if(TextUtils.isEmpty(commonFileNamePrefix)){
                        name = commonFileNamePrefix+"_";
                    }
                    name = DownloadInfoUtil.getLeagalFileName(name);
                    if(withIndexNumber){
                        name = name+"_"+i;
                    }

                    DownloadApi downloadApi = DownloadApi.create(url)
                            .setNeedCheckDbBeforeStart(false)
                            .setName(name)
                            .setDirForce(dir);
                    if(!headers.isEmpty()){
                        for (String s : headers.keySet()) {
                            downloadApi.addHeader(s,headers.get(s));
                        }
                    }
                    downloadApi.callback(new DefaultSilentDownloadCallback());
                }
                return null;
            }

            @Override
            public void onSuccess(List<String> result) {

            }
        });


    }


}
