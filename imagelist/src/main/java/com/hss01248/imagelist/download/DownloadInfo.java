package com.hss01248.imagelist.download;

import androidx.annotation.Keep;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Keep
@Entity
public class DownloadInfo {

    @Id
    public String url;
    public String filePath;

    public static final int STATUS_FAIL = -1;
    public static final int STATUS_INIT = 2;
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_DOWNLOADING = 3;
    /**
     * 成功: 1 下载中 0 下载失败 -1
     */
    public int status;
    @Generated(hash = 970613150)
    public DownloadInfo(String url, String filePath, int status) {
        this.url = url;
        this.filePath = filePath;
        this.status = status;
    }
    @Generated(hash = 327086747)
    public DownloadInfo() {
    }
    public String getUrl() {
        return this.url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getFilePath() {
        return this.filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    @Override
    public String toString() {
        return "DownloadInfo{" +
                "url='" + url + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }
    public int getStatus() {
        return this.status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
}
