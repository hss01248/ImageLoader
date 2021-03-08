package com.hss01248.glideloader.big;

import android.net.Uri;
import android.util.Log;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
import com.github.piasy.biv.progress.OkHttpProgressResponseBody;

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
        String url = super.getCacheKey().replace(OkHttpProgressResponseBody.KEY_PREGRESS, "");
        Log.d("getCacheKey", url);
        return url;
    }
}
