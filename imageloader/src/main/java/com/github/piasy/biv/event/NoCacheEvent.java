package com.github.piasy.biv.event;

import java.io.File;

/**
 * Created by Administrator on 2017/4/11 0011.
 */

public class NoCacheEvent {
    public File file;
    public String url;
    public NoCacheEvent(File file,String url) {
        this.file = file;
        this.url = url;
    }
}
