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
    @Generated(hash = 501294909)
    public DownloadInfo(String url, String filePath) {
        this.url = url;
        this.filePath = filePath;
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
}
