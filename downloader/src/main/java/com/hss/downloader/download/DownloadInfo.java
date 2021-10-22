package com.hss.downloader.download;

import androidx.annotation.Keep;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Keep
@Entity
public class DownloadInfo {

    public static final int STATUS_FAIL = -1;
    public static final int STATUS_ORIGINAL = -2;
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_DOWNLOADING = 0;
    @Id
    public String url;

    @Deprecated
    public String filePath;
    /**
     * 成功: 1 下载中 0 下载失败 -1, 初始状态 -2
     */
    public int status = -2;

    /**
     * 文件名
     */
    public String name;

    public String dir;
    public String errMsg;

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
