package com.hss01248.imagelist.album;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

/**
 * Created by Darshan on 4/18/2015.
 */
public class Image implements Parcelable {
    public long id;
    public String name;
    public String path;
    public long fileSize;
    public long addDate;
    public long modifiedTime;
    public long width;
    public long height;
    public int oritation;
    public String mimeType;
    public boolean isDir;

    public Image(long id, String name, String path, long fileSize, long addDate,
                 long modifiedTime, long width, long height, String mimeType) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.fileSize = fileSize;
        this.addDate = addDate;
        this.modifiedTime = modifiedTime;
        this.width = width;
        this.height = height;
        this.mimeType = mimeType;
    }

    public boolean isSelected;
    public int quality = -1;

    public Image(long id, String name, String path, boolean isSelected) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.isSelected = isSelected;
    }

    public void initFileSize() {
        if (fileSize == 0) {
            this.fileSize = new File(path).length();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(path);
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel source) {
            return new Image(source);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    private Image(Parcel in) {
        id = in.readLong();
        name = in.readString();
        path = in.readString();
    }
}
