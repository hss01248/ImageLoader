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

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.bumptech.glide.request.target.SimpleTarget;
import com.github.piasy.biv.indicator.DownloadStateEvent;
import com.github.piasy.biv.loader.ImageLoader;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Piasy{github.com/Piasy} on 12/11/2016.
 */

public abstract class ImageDownloadTarget extends SimpleTarget<File> implements Observer {
    private final String mUrl;
    private final Observable observable;
    private ImageLoader.Callback callback;

    protected ImageDownloadTarget(String url, Observable observable, ImageLoader.Callback callback) {
        mUrl = url;
        this.observable = observable;
        this.callback = callback;
    }

    @Override
    public void onLoadCleared(Drawable placeholder) {
        super.onLoadCleared(placeholder);
        //GlideProgressSupport.forget(mUrl);
    }

    @Override
    public void onLoadStarted(Drawable placeholder) {
        super.onLoadStarted(placeholder);
       // GlideProgressSupport.expect(mUrl, this);
        observable.addObserver(this);

    }

    @Override
    public void onLoadFailed(Exception e, Drawable errorDrawable) {
        super.onLoadFailed(e, errorDrawable);
        //GlideProgressSupport.forget(mUrl);
        callback.onFail();
        observable.deleteObserver(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //GlideProgressSupport.forget(mUrl);
        Log.e("loader","onDestroy");
        observable.deleteObserver(this);

    }

    @Override
    public void onStop() {
        super.onStop();
       // GlideProgressSupport.forget(mUrl);
        Log.e("loader","onStop");

    }

    @Override
    public void update(Observable o, Object arg) {
        if(arg instanceof DownloadStateEvent){
            DownloadStateEvent stateEvent = (DownloadStateEvent) arg;
            if(stateEvent.url.equals(mUrl)){
                if(stateEvent.type== DownloadStateEvent.STATE_PROGRESS){
                    callback.onProgress(stateEvent.progress);
                }else if(stateEvent.type==DownloadStateEvent.STATE_FINISH){
                    callback.onFinish();
                }else if(stateEvent.type==DownloadStateEvent.STATE_START){
                    callback.onStart();
                }else if(stateEvent.type==DownloadStateEvent.STATE_FAIL){
                    callback.onFail();
                }

            }
        }

    }
}
