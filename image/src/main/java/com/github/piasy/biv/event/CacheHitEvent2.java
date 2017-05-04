package com.github.piasy.biv.event;

import android.net.Uri;

/**
 * Created by Administrator on 2017/4/11 0011.
 */

public class CacheHitEvent2 {
    public Uri uri;
    public String url;

    public CacheHitEvent2(Uri uri, String url) {
        this.uri = uri;
        this.url = url;
    }

}
