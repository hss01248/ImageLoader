package com.hss01248.glideloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.MemoryCategory;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.view.BigImageView;
import com.hss01248.glideloader.big.GlideImageLoader;
import com.hss01248.image.MyUtil;
import com.hss01248.image.config.GlobalConfig;
import com.hss01248.image.config.ScaleMode;
import com.hss01248.image.config.ShapeMode;
import com.hss01248.image.config.SingleConfig;
import com.hss01248.image.interfaces.ILoader;

import java.io.File;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Created by Administrator on 2017/3/27 0027.
 * 参考:
 * https://mrfu.me/2016/02/28/Glide_Series_Roundup/
 */

public class GlideLoader implements ILoader {
    @Override
    public void init(Context context, int cacheSizeInM) {//glide默认最大容量250MB的文件缓存
        Glide.get(context).setMemoryCategory(MemoryCategory.NORMAL);
        GlideBuilder builder = new GlideBuilder(context);
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, cacheSizeInM*1024*1024));
        BigImageViewer.initialize(GlideImageLoader.with(context));

    }

    @Override
    public void request(final SingleConfig config) {
        if(config.isAsBitmap()){
            SimpleTarget target = new SimpleTarget<Bitmap>(config.getWidth(),config.getHeight()) {
                @Override
                public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                    // do something with the bitmap
                    // for demonstration purposes, let's just set it to an ImageView
                    config.getBitmapListener().onSuccess(bitmap);
                }
            };
            RequestManager requestManager =  Glide.with(config.getContext());
            DrawableTypeRequest request = getDrawableTypeRequest(config, requestManager);
            request.override(config.getWidth(),config.getHeight());
            setShapeModeAndBlur(config, request);
            request.asBitmap().into(target);

        }else {

            if(config.getTarget() instanceof BigImageView){
                MyUtil.viewBigImage(config);
                return;
            }


            RequestManager requestManager =  Glide.with(config.getContext());
            DrawableTypeRequest request = getDrawableTypeRequest(config, requestManager);

            if(request ==null){
                return;
            }
            if(MyUtil.shouldSetPlaceHolder(config)){
                request.placeholder(config.getPlaceHolderResId());
            }

            int scaleMode = config.getScaleMode();
            switch (scaleMode){
                case ScaleMode.CENTER_CROP:
                    request.centerCrop();
                    break;
                case ScaleMode.CENTER_INSIDE:
                    request.centerCrop();
                    break;
                case ScaleMode.FIT_CENTER:
                    request.fitCenter();
                    break;
                case ScaleMode.FIT_XY:
                    request.centerCrop();
                    break;
                case ScaleMode.FIT_END:
                    request.centerCrop();
                    break;
                case ScaleMode.FOCUS_CROP:
                    request.centerCrop();
                    break;
                case ScaleMode.CENTER:
                    request.centerCrop();
                    break;
                case ScaleMode.FIT_START:
                    request.centerCrop();
                    break;

                default:
                    request.centerCrop();
                    break;
            }


            setShapeModeAndBlur(config, request);

            request.override(config.getWidth(),config.getHeight())
                    .thumbnail( 0.1f );


            if(config.getErrorResId() >0){
                request.error(config.getErrorResId());
            }

            if(config.getTarget() instanceof ImageView){
                request.into((ImageView) config.getTarget());
            }


        }


    }

    @Nullable
    private DrawableTypeRequest getDrawableTypeRequest(SingleConfig config, RequestManager requestManager) {
        DrawableTypeRequest request = null;
        if(!TextUtils.isEmpty(config.getUrl())){
            request= requestManager.load(MyUtil.appendUrl(config.getUrl()));
        }else if(!TextUtils.isEmpty(config.getFilePath())){
            request= requestManager.load(MyUtil.appendUrl(config.getFilePath()));
        }else if(!TextUtils.isEmpty(config.getContentProvider())){
            request= requestManager.loadFromMediaStore(Uri.parse(config.getContentProvider()));
        }else if(config.getResId()>0){
            request= requestManager.load(config.getResId());
        }
        return request;
    }

    private void setShapeModeAndBlur(SingleConfig config, DrawableTypeRequest request) {
        int shapeMode = config.getShapeMode();
        Transformation[] transformation =  new Transformation[2];
        if(config.isNeedBlur()){
            transformation[0]=new BlurTransformation(config.getContext(), config.getBlurRadius());
        }

        switch (shapeMode){
            case ShapeMode.RECT:

                if(config.getBorderWidth()>0){

                }
                break;
            case ShapeMode.RECT_ROUND:
                transformation[1] =
                new RoundedCornersTransformation(config.getContext(), config.getRectRoundRadius(), 0, RoundedCornersTransformation.CornerType.ALL);

                if(config.getBorderWidth()>0){

                }
                if(config.isGif() && config.getRoundOverlayColor()>0){

                }
                break;
            case ShapeMode.OVAL:
                transformation[1] =  new CropCircleTransformation(config.getContext());
                if(config.getBorderWidth()>0){

                }
                if(config.isGif() && config.getRoundOverlayColor()>0){

                }
                break;
        }

        if(transformation[0] !=null && transformation[1]!=null){
            request.bitmapTransform(transformation);
        }else if(transformation[0] !=null && transformation[1]==null){
            request.bitmapTransform(transformation[0]);
        }else if(transformation[0] ==null && transformation[1]!=null){
            request.bitmapTransform(transformation[1]);
        }
    }

    @Override
    public void pause() {
        Glide.with(GlobalConfig.context).pauseRequestsRecursive();

    }

    @Override
    public void resume() {
        Glide.with(GlobalConfig.context).resumeRequestsRecursive();
    }

    @Override
    public void clearDiskCache() {

    }

    @Override
    public void clearCacheByUrl(String url) {

    }

    @Override
    public void clearMomoryCache(View view) {
        Glide.clear(view);
    }

    @Override
    public void clearMomoryCache(String url) {

    }

    @Override
    public File getFileFromDiskCache(String url) {
        return null;
    }

    /**
     * 参见:https://github.com/bumptech/glide/issues/639
     * @param url
     * @return
     */
    @Override
    public boolean isCached(String url) {
        return false;
    }

    @Override
    public void trimMemory(int level) {
        Glide.with(GlobalConfig.context).onTrimMemory(level);
    }

    @Override
    public void clearAllMemoryCaches() {
        Glide.with(GlobalConfig.context).onLowMemory();
    }
}
