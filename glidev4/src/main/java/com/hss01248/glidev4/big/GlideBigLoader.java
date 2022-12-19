/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Piasy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.hss01248.glidev4.big;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.github.piasy.biv.event.CacheHitEvent;
import com.github.piasy.biv.event.ErrorEvent;
import com.github.piasy.biv.loader.BigLoader;
import com.github.piasy.biv.view.BigImageView;
import com.google.gson.Gson;
import com.hss01248.glidev4.R;
import com.hss01248.image.config.GlobalConfig;
import com.hss01248.media.metadata.MetaDataUtil;
import com.hss01248.media.metadata.MetaInfo;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;

/**
 * Created by Piasy{github.com/Piasy} on 09/11/2016.
 */

public final class GlideBigLoader implements BigLoader {
    private final RequestManager mRequestManager;
    private final ExecutorService executor;

    private GlideBigLoader(Context context, OkHttpClient okHttpClient) {
        mRequestManager = Glide.with(context);
        executor = Executors.newCachedThreadPool();
    }

    public static GlideBigLoader with(Context context) {
        return with(context, null);
    }

    public static GlideBigLoader with(Context context, OkHttpClient okHttpClient) {
        return new GlideBigLoader(context, okHttpClient);
    }

    @Override
    public void loadImage(final Uri uri) {
        final String url = uri.toString();
        Log.w("load big image:", url);
        final String finalUrl = url;

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    File resource = mRequestManager
                            .asFile()
                            .load(new ProgressableGlideUrl(finalUrl))
                            .submit().get();
                    if (resource.exists() && resource.isFile() && resource.length() > 100) {
                        LogUtils.i("glide onResourceReady", "onResourceReady  --" + resource.getAbsolutePath());
                        EventBus.getDefault().post(new CacheHitEvent(resource, finalUrl));
                        if(GlobalConfig.debug){
                            MetaInfo metaData2 = MetaDataUtil.getMetaData2(Uri.fromFile(resource));
                            LogUtils.json(new Gson().newBuilder().setPrettyPrinting().create().toJson(metaData2));
                        }

                    } else {
                        LogUtils.w(" glide onloadfailed", "onLoadFailed  --" + finalUrl);
                        EventBus.getDefault().post(new ErrorEvent(finalUrl));
                    }
                } catch (Throwable throwable) {
                    LogUtils.w(finalUrl,throwable);
                    EventBus.getDefault().post(new ErrorEvent(finalUrl));

                }
            }
        });
    }

    @Override
    public View showThumbnail(BigImageView parent, Uri thumbnail, int scaleType) {
        ImageView thumbnailView = (ImageView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ui_glide_thumbnail, parent, false);
        switch (scaleType) {
            case BigImageView.INIT_SCALE_TYPE_CENTER_CROP:
                thumbnailView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                break;
            case BigImageView.INIT_SCALE_TYPE_CENTER_INSIDE:
                thumbnailView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            default:
                break;
        }
        mRequestManager
                .load(thumbnail)
                .into(thumbnailView);
        return thumbnailView;
    }

    @Override
    public void prefetch(Uri uri) {
        mRequestManager
                .load(uri)
                .preload();
    }
}
