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

package com.hss01248.glideloader.big;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.github.piasy.biv.event.CacheHitEvent;
import com.github.piasy.biv.event.ErrorEvent;
import com.github.piasy.biv.loader.ImageLoader;
import com.github.piasy.biv.view.BigImageView;
import com.hss01248.glideloader.R;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Observable;

import okhttp3.OkHttpClient;

/**
 * Created by Piasy{github.com/Piasy} on 09/11/2016.
 */

public final class GlideImageLoader implements ImageLoader {
    private final RequestManager mRequestManager;
    private Observable observable;

    private GlideImageLoader(Context context, OkHttpClient okHttpClient) {
        observable = new Observable();
        GlideProgressSupport.init(Glide.get(context), okHttpClient);
        mRequestManager = Glide.with(context);
    }

    public static GlideImageLoader with(Context context) {
        return with(context, null);
    }

    public static GlideImageLoader with(Context context, OkHttpClient okHttpClient) {
        return new GlideImageLoader(context, okHttpClient);
    }

    @Override
    public void loadImage(final Uri uri) {
        mRequestManager
                .load(uri)
                .downloadOnly(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                        EventBus.getDefault().post(new CacheHitEvent(resource,uri.toString()));
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        super.onLoadCleared(placeholder);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        EventBus.getDefault().post(new ErrorEvent(uri.toString()));
                    }
                });

        /*new ImageDownloadTarget(uri.toString()) {
                    @Override
                    public void onResourceReady(File image,
                            GlideAnimation<? super File> glideAnimation) {
                        // we don't need delete this image file, so it behaves live cache hit
                        callback.onCacheHit(image);
                    }



                    @Override
                    public void onDownloadStart() {
                        callback.onStart();
                    }

                    @Override
                    public void onProgress(int progress) {
                        callback.onProgress(progress);
                    }

                    @Override
                    public void onDownloadFinish() {
                        callback.onFinish();
                    }
                }*/
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
                .downloadOnly(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource,
                            GlideAnimation<? super File> glideAnimation) {
                        // not interested in result
                    }
                });
    }
}
