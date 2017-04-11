package com.github.piasy.biv.event;

/**
 * Created by Administrator on 2017/4/11 0011.
 */

public class ProgressEvent {
   public int progress;
    public boolean hasFinish;
    public String url;

    public ProgressEvent(int progress, boolean hasFinish,String url) {
        this.progress = progress;
        this.hasFinish = hasFinish;
        this.url = url;
    }
}
