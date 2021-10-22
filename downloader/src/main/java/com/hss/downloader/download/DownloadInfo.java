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
    public long totalLength;
    public long createTime;

    public transient boolean selected;
    public transient boolean isInSelectMode;

    @Generated(hash = 691565714)
    public DownloadInfo(String url, String filePath, int status, String name,
            String dir, String errMsg, long totalLength, long createTime) {
        this.url = url;
        this.filePath = filePath;
        this.status = status;
        this.name = name;
        this.dir = dir;
        this.errMsg = errMsg;
        this.totalLength = totalLength;
        this.createTime = createTime;
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
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDir() {
        return this.dir;
    }
    public void setDir(String dir) {
        this.dir = dir;
    }
    public String getErrMsg() {
        return this.errMsg;
    }
    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
    public long getTotalLength() {
        return this.totalLength;
    }
    public void setTotalLength(long totalLength) {
        this.totalLength = totalLength;
    }
    public long getCreateTime() {
        return this.createTime;
    }
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
