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

package com.github.piasy.biv.view;

import android.Manifest;
import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.RequiresPermission;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.FrameLayout;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.event.CacheHitEvent;
import com.github.piasy.biv.event.CacheHitEvent2;
import com.github.piasy.biv.event.ErrorEvent;
import com.github.piasy.biv.event.ProgressEvent;
import com.github.piasy.biv.indicator.ProgressIndicator;
import com.github.piasy.biv.indicator.ProgressPieIndicator;
import com.github.piasy.biv.loader.BigLoader;
import com.hss01248.image.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Piasy{github.com/Piasy} on 06/11/2016.
 *
 * Use FrameLayout for extensibility.
 */

public class BigImageView extends FrameLayout  implements BigImageHierarchy{
    public static final int INIT_SCALE_TYPE_CENTER_INSIDE = 1;
    public static final int INIT_SCALE_TYPE_CENTER_CROP = 2;
    public static final int INIT_SCALE_TYPE_AUTO = 3;

    private final SubsamplingScaleImageView mImageView;
    private View progressView;
    private View errorView;
    private View mThumbnailView;
    private View placeHolder;

    private final BigLoader mImageLoader;
    private  Map<String,File> mTempImages;

   // private View progressView;


    private ImageSaveCallback mImageSaveCallback;
    private File mCurrentImageFile;
    private Uri mThumbnail;

    private ProgressIndicator mProgressIndicator;
    private final ProgressNotifyRunnable mProgressNotifyRunnable
            = new ProgressNotifyRunnable() {
        @Override
        public void run() {
            if (mProgressIndicator != null) {
                mProgressIndicator.onProgress(mProgress);
                notified();
            }
        }
    };
    private DisplayOptimizeListener mDisplayOptimizeListener;
    private int mInitScaleType;
    private boolean mOptimizeDisplay;
    int currentState;
    public static final int STATE_NONE = 0;
    public static final int STATE_STATRTED = 1;
    public static final int STATE_PROGRESSING = 2;
    public static final int STATE_CONTENT = 3;
    public static final int STATE_ERROR = 4;

    public void updateHierachy(){

    }

    public BigImageView(Context context) {
        this(context, null);
    }

    public BigImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BigImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.BigImageView, defStyleAttr, 0);
        mInitScaleType = array.getInteger(R.styleable.BigImageView_initScaleType,
                INIT_SCALE_TYPE_CENTER_INSIDE);
        mOptimizeDisplay = array.getBoolean(R.styleable.BigImageView_optimizeDisplay, true);
        array.recycle();

        mImageView = new SubsamplingScaleImageView(context, attrs);
        addView(mImageView);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mImageView.setLayoutParams(params);
        mImageView.setMinimumTileDpi(160);
        errorView = View.inflate(context,R.layout.error_view,null);
        LayoutParams params2 = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        errorView.setLayoutParams(params2);
        addView(errorView);

        placeHolder = View.inflate(context,R.layout.view_placeholder,null);
        LayoutParams params3 = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        placeHolder.setLayoutParams(params3);
        addView(placeHolder);

        setProgressIndicator(new ProgressPieIndicator());

        setOptimizeDisplay(mOptimizeDisplay);
        setInitScaleType(mInitScaleType);

        mImageLoader = BigImageViewer.imageLoader();
        mTempImages = new HashMap<>();


       // EventBus.getDefault().register(this);
    }

    public void setCachedFileMap(Map<String,File> cachedFileMap){
        mTempImages = cachedFileMap;
    }


    @Override
    public void setOnClickListener(OnClickListener listener) {
        mImageView.setOnClickListener(listener);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener listener) {
        mImageView.setOnLongClickListener(listener);
    }

    public void setInitScaleType(int initScaleType) {
        mInitScaleType = initScaleType;
        switch (initScaleType) {
            case INIT_SCALE_TYPE_CENTER_CROP:
                mImageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
                break;
            case INIT_SCALE_TYPE_AUTO:
                mImageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM);
                break;
            case INIT_SCALE_TYPE_CENTER_INSIDE:
            default:
                mImageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE);
                break;
        }
        if (mDisplayOptimizeListener != null) {
            mDisplayOptimizeListener.setInitScaleType(initScaleType);
        }
    }

    public void setOptimizeDisplay(boolean optimizeDisplay) {
        mOptimizeDisplay = optimizeDisplay;
        if (mOptimizeDisplay) {
            mDisplayOptimizeListener = new DisplayOptimizeListener(mImageView);
            mImageView.setOnImageEventListener(mDisplayOptimizeListener);
        } else {
            mDisplayOptimizeListener = null;
            mImageView.setOnImageEventListener(null);
        }
    }

    public void setImageSaveCallback(ImageSaveCallback imageSaveCallback) {
        mImageSaveCallback = imageSaveCallback;
    }

    public void setErrorView(View error){
        if(error !=null && errorView ==null){
           errorView = error;
            addView(errorView);
        }

    }

    public void setProgressIndicator(ProgressIndicator progressIndicator) {
        if(progressIndicator==null){
            mProgressIndicator =null;
            if(progressView!=null){
                removeView(progressView);
            }

        }


        if( progressIndicator.equals(mProgressIndicator) ){
            return;
        }

        if(mProgressIndicator ==null){
            mProgressIndicator = progressIndicator;
            progressView = mProgressIndicator.getView(this);
            if(progressView.getParent() !=null){
              ViewGroup viewGroup = (ViewGroup) progressView.getParent();
                viewGroup.removeView(progressView);
            }
            addView(progressView);
        }
        //如果原先有,直接用原先的来更新进度条

    }

    public String currentImageFile() {
        return mCurrentImageFile == null ? "" : mCurrentImageFile.getAbsolutePath();
    }

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void saveImageIntoGallery() {
        if (mCurrentImageFile == null) {
            if (mImageSaveCallback != null) {
                mImageSaveCallback.onFail(new IllegalStateException("image not downloaded yet"));
            }

            return;
        }

        try {
            String result = MediaStore.Images.Media.insertImage(getContext().getContentResolver(),
                    mCurrentImageFile.getAbsolutePath(), mCurrentImageFile.getName(), "");
            if (mImageSaveCallback != null) {
                if (!TextUtils.isEmpty(result)) {
                    mImageSaveCallback.onSuccess(result);
                } else {
                    mImageSaveCallback.onFail(new RuntimeException("saveImageIntoGallery fail"));
                }
            }
        } catch (FileNotFoundException e) {
            if (mImageSaveCallback != null) {
                mImageSaveCallback.onFail(e);
            }
        }
    }


    private String url;

    public void showImage(Uri uri) {
        showImage(Uri.EMPTY, uri);
    }

    public void showImage(Uri thumbnail, Uri uri) {
        //Log.d("BigImageView", "showImage new url: "+ uri.toString());
       // Log.d("BigImageView", "showImage  old url: " + this.url);

       /* if(TextUtils.isEmpty(this.url) || !this.url.equals(uri.toString())){
            onStart();
        }*/

        this.url = uri.toString();
        mThumbnail = thumbnail;


        if(mTempImages.containsKey(this.url)){
            Log.e("dd","mTempImages.containsKey(this.url),show content");
            //onStart();
            showContent(mTempImages.get(this.url));
        }else {
            onStart();
            mImageLoader.loadImage(uri);
        }



        //EventBus.getDefault().post(new StartEvent(url));
        //currentState = STATE_STATRTED;

    }



    private void doOnFinish() {
        if (mOptimizeDisplay) {
            AnimationSet set = new AnimationSet(true);
            AlphaAnimation animation = new AlphaAnimation(1, 0);
            animation.setDuration(500);
            animation.setFillAfter(true);
            set.addAnimation(animation);
            if (mThumbnailView != null) {
                mThumbnailView.setAnimation(set);
            }

            if (mProgressIndicator != null) {
                mProgressIndicator.onFinish();
            }

            if (progressView != null) {
                progressView.setAnimation(set);
            }

            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mThumbnailView != null) {
                        mThumbnailView.setVisibility(GONE);
                    }
                    if (progressView != null) {
                        progressView.setVisibility(GONE);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        } else {
            if (mProgressIndicator != null) {
                mProgressIndicator.onFinish();
            }
            if (mThumbnailView != null) {
                mThumbnailView.setVisibility(GONE);
            }
            if (progressView != null) {
                progressView.setVisibility(GONE);
            }
        }
    }




    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);




    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
       // EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().unregister(this);

    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProgressEvent(ProgressEvent event){
        //Log.d("BigImageView", "onProgressEvent---event url: " + event.url);
       // Log.d("BigImageView", "onProgressEvent---old url: " + this.url);
        if(this.url.equals(event.url)){
            showProgress(event.progress);
            if(event.progress ==100){
               // doOnFinish();//动画
                //showContent(null);

            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onErrorEvent(ErrorEvent event){
        Log.d("BigImageView", "onFailEvent---event url: " + event.url);
        Log.d("BigImageView", "onFailEvent---old url: " + this.url);
        if(this.url.equals(event.url)){
            showError();
        }


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCacheHitEvent(CacheHitEvent event){
        Log.d("BigImageView", "onCacheHitEvent---event url: " + event.url);
        Log.d("BigImageView", "onCacheHitEvent---old url: " + this.url);
        if(this.url.equals(event.url) && event.file!=null && event.file.exists() && event.file.length() > 100){
            mCurrentImageFile = event.file;
            mTempImages.put(this.url,event.file);
            showContent(event.file);
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCacheHitEvent(CacheHitEvent2 event){
        Log.d("BigImageView", "onCacheHitEvent---event url: " + event.url);
        Log.d("BigImageView", "onCacheHitEvent---old url: " + this.url);
        if(this.url.equals(event.url) ){
            mImageView.setImage(ImageSource.uri(event.uri));
            showContent(null);

        }
    }

    /*@Subscribe(threadMode = ThreadMode.MAIN)
    public void onNoCacheEvent(NoCacheEvent event){
        if(this.url.equals(event.url)){
            onCacheMiss(event.file);
        }
    }*/

    @Override
    public void showContent(File image) {


      /*  if(!mTempImages.containsKey(this.url)){
            mTempImages.put(this.url,image);
        }*/
        mImageView.setVisibility(VISIBLE);
        if(image!=null)
        mImageView.setImage(ImageSource.uri(Uri.fromFile(image)));
        Log.d("BigImageView", "mImageView.setImage: " + image.getAbsolutePath());


            currentState = STATE_CONTENT;
            if(progressView!=null)
                progressView.setVisibility(GONE);
            if(mThumbnailView!=null)
                mThumbnailView.setVisibility(GONE);
            if(errorView!=null)
                errorView.setVisibility(GONE);
            if(placeHolder!=null){
                placeHolder.setVisibility(GONE);
            }
    }

    @Override
    public void showProgress(int progress) {
        if(currentState != STATE_PROGRESSING){
            currentState = STATE_PROGRESSING;
            mImageView.setVisibility(GONE);
            if(progressView!=null)
                progressView.setVisibility(VISIBLE);
            if(mThumbnailView!=null)
                mThumbnailView.setVisibility(GONE);
            if(errorView!=null)
                errorView.setVisibility(GONE);
            if(placeHolder!=null){
                placeHolder.setVisibility(GONE);
            }
        }
        //todo 更新进度条
        mProgressIndicator.onProgress(progress);

    }

    @Override
    public void showError() {

            currentState = STATE_ERROR;
            mImageView.setVisibility(GONE);
            if(progressView!=null)
            progressView.setVisibility(GONE);
            if(mThumbnailView!=null)
            mThumbnailView.setVisibility(GONE);
            if(errorView!=null)
            errorView.setVisibility(VISIBLE);
            if(placeHolder!=null){
                placeHolder.setVisibility(GONE);
            }


        //todo 点击重试

    }

    @Override
    public void showThumbnail() {

            currentState = STATE_STATRTED;
        mImageView.setVisibility(GONE);
        if(progressView!=null)
            progressView.setVisibility(GONE);
        if(mThumbnailView!=null)
            mThumbnailView.setVisibility(VISIBLE);
        if(errorView!=null)
            errorView.setVisibility(GONE);
        if(placeHolder!=null){
            placeHolder.setVisibility(GONE);
        }

    }

    @Override
    public void onStart() {

            currentState = STATE_STATRTED;
            mImageView.setVisibility(GONE);
            if(progressView!=null)
                progressView.setVisibility(GONE);
            if(mThumbnailView!=null)
                mThumbnailView.setVisibility(GONE);
            if(errorView!=null)
                errorView.setVisibility(GONE);
            if(placeHolder!=null){
                placeHolder.setVisibility(VISIBLE);
            }

    }
}
