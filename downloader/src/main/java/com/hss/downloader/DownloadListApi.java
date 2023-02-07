package com.hss.downloader;

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

    public DownloadListApi setDir(String dir) {
        this.dir = DownloadApi.determinDir(dir);
        return this;
    }

    public DownloadListApi addHeader(String key,String val) {
        this.headers.put(key, val);
        return this;
    }

    private Map<String,String> headers = new HashMap<>();

    public void start(){
        //todo 一万条时,loading,子线程写数据库
        for (String url : urls) {
            DownloadApi downloadApi = DownloadApi.create(url)
                    .setDirForce(dir);
            if(!headers.isEmpty()){
                for (String s : headers.keySet()) {
                    downloadApi.addHeader(s,headers.get(s));
                }
            }
            downloadApi.callback(new DefaultSilentDownloadCallback());
        }
    }


}
