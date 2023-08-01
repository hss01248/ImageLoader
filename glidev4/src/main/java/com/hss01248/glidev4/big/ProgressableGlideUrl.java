package com.hss01248.glidev4.big;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;

import java.net.URL;

/**
 * time:2019/11/10
 * author:hss
 * desription:
 */
public class ProgressableGlideUrl extends GlideUrl {


    public ProgressableGlideUrl(URL url) {
        super(url);
    }

    public ProgressableGlideUrl(String url) {
        super(url);
    }

    public ProgressableGlideUrl(URL url, Headers headers) {
        super(url, headers);
    }

    public ProgressableGlideUrl(String url, Headers headers) {
        super(url, headers);
    }

    @Override
    public String getCacheKey() {
        //String url = super.getCacheKey().replace(OkHttpProgressResponseBody.KEY_PREGRESS, "");
        //Log.d("getCacheKey", url);
        return super.getCacheKey();
    }
}
