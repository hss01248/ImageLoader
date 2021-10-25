package com.hss.downloader.event;

public class DownloadResultEvent {

    public DownloadResultEvent(boolean success) {
        this.success = success;
    }

    public boolean success;
}
