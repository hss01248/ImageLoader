package com.github.piasy.biv.event;

import java.io.File;

/**
 * Created by Administrator on 2017/4/11 0011.
 */

public class CacheHitEvent {
    public File file;
    public String url;

    public CacheHitEvent(File file,String url) {
        this.file = file;
        this.url = url;
    }
}
