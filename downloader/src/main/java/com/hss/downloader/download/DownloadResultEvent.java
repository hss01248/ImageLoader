package com.hss.downloader.download;

public class DownloadResultEvent {

    public DownloadResultEvent(boolean success) {
        this.success = success;
    }

    public boolean success;
}
