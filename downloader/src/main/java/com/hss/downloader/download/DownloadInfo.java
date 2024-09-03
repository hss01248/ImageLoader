package com.hss.downloader.download;

import androidx.annotation.Keep;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;

@Keep
@Entity
public class DownloadInfo {

    public static final int STATUS_FAIL = -1;
    public static final int STATUS_ORIGINAL = 0;
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_DOWNLOADING = 2;
    @Id
    public String url;
    @Index

    public String filePath;
    /**
     * 成功: 1 下载中 0 下载失败 -1, 初始状态 -2
     */
    public int status = 0;

    public  boolean downloadSuccess(){
        return status == STATUS_SUCCESS;
    }

    public  boolean downloadFailed(){
        return status == STATUS_FAIL;
    }
    public String getUrl() {
        return this.url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilePath() {
        if(filePath ==null || "".equals(filePath)){
            genFilePath();
        }
        return this.filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
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
    public long getUpdateTime() {
        return this.updateTime;
    }
    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
    /**
     * 文件名
     */
    public String name;

    public String dir;
    public String errMsg;
    public long totalLength;
    public long createTime ;

    @Index
    public long updateTime ;

    public transient boolean selected;
    public transient boolean isInSelectMode;
    public transient long currentOffset;
    public transient boolean isCompressing;

    public transient long speed;

    @Generated(hash = 1249356660)
    public DownloadInfo(String url, String filePath, int status, String name,
            String dir, String errMsg, long totalLength, long createTime,
            long updateTime) {
        this.url = url;
        this.filePath = filePath;
        this.status = status;
        this.name = name;
        this.dir = dir;
        this.errMsg = errMsg;
        this.totalLength = totalLength;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
    @Generated(hash = 327086747)
    public DownloadInfo() {
    }


    public void genFilePath() {
        this.filePath = dir+"/"+name;
    }
}
