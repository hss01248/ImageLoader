package com.hss.downloader.download;

import androidx.annotation.Keep;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Keep
@Entity
public class SubFolderCount {

    @Id
    public String dirPath;
    public Integer count;
    @Generated(hash = 1472328589)
    public SubFolderCount(String dirPath, Integer count) {
        this.dirPath = dirPath;
        this.count = count;
    }
    @Generated(hash = 348968327)
    public SubFolderCount() {
    }
    public String getDirPath() {
        return this.dirPath;
    }
    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }
    public Integer getCount() {
        return this.count;
    }
    public void setCount(Integer count) {
        this.count = count;
    }
}
