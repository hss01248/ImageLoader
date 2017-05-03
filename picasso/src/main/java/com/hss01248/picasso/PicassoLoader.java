package com.hss01248.picasso;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.renderscript.RSRuntimeException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.github.piasy.biv.view.BigImageView;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.MyUtil;
import com.hss01248.image.config.GlobalConfig;
import com.hss01248.image.config.ScaleMode;
import com.hss01248.image.config.ShapeMode;
import com.hss01248.image.config.SingleConfig;
import com.hss01248.image.interfaces.ILoader;
import com.hss01248.image.utils.ThreadPoolFactory;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.picasso.transformations.BlurTransformation;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;
import jp.wasabeef.picasso.transformations.internal.FastBlur;
import jp.wasabeef.picasso.transformations.internal.RSBlur;

import static com.squareup.picasso.MemoryPolicy.NO_CACHE;
import static com.squareup.picasso.MemoryPolicy.NO_STORE;

/**
 * Created by Administrator on 2017/5/3 0003.
 *
 * 内存优化: http://blog.csdn.net/ashqal/article/details/48005833
 */

public class PicassoLoader implements ILoader {

    private static final String PICASSO = "picasso";
    private List<String> paths = new ArrayList<>();

    @Override
    public void init(Context context, int cacheSizeInM) {//Picasso默认最大容量250MB的文件缓存
       // Picasso.get(context).setMemoryCategory(MemoryCategory.NORMAL);
        //BigImageViewer.initialize(PicassoImageLoader.with(context,MyUtil.getClient(GlobalConfig.ignoreCertificateVerify)));
        Picasso picasso = new Picasso.Builder(context)
                .downloader(new OkHttp3Downloader(MyUtil.getClient(GlobalConfig.ignoreCertificateVerify)))
                .build();
    }

    @Override
    public void request(final SingleConfig config) {


            if(config.getTarget() instanceof BigImageView){
                MyUtil.viewBigImage(config);
                return;
            }



            final RequestCreator request = getDrawableTypeRequest(config);
            request.tag(PICASSO).config(Bitmap.Config.RGB_565);

            if(request ==null){
                return;
            }
            if(MyUtil.shouldSetPlaceHolder(config)){
                request.placeholder(config.getPlaceHolderResId());
            }
            if(config.getErrorResId() >0){
                request.error(config.getErrorResId());
            }

            if(!config.isAsBitmap()){
                int scaleMode = config.getScaleMode();
                switch (scaleMode){
                    case ScaleMode.CENTER_CROP:
                        request.centerCrop();
                        break;
                    case ScaleMode.CENTER_INSIDE:
                        request.centerInside();
                        break;
                    case ScaleMode.FIT_CENTER:
                        request.centerCrop();
                        break;
                    case ScaleMode.FIT_XY:
                        request.fit();
                        break;
                    case ScaleMode.FIT_END:
                        request.fit();
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
            }


            request.resize(config.getWidth(),config.getHeight()).onlyScaleDown();
        if(config.getWidth() >1000 || config.getHeight() >1000){
            request.memoryPolicy(NO_CACHE, NO_STORE);
        }

            setShapeModeAndBlur(config, request);

            if(config.isAsBitmap()){
                request.fetch(new Callback() {
                    @Override
                    public void onSuccess() {

                        ThreadPoolFactory.getDownLoadPool().execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    final Bitmap bitmap = request.get();
                                    MyUtil.runOnUIThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            config.getBitmapListener().onSuccess(bitmap);
                                        }
                                    });


                                } catch (IOException e) {
                                    e.printStackTrace();
                                    MyUtil.runOnUIThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            config.getBitmapListener().onFail();
                                        }
                                    });

                                }
                            }
                        });
                    }

                    @Override
                    public void onError() {
                        config.getBitmapListener().onFail();

                    }
                });
                return;
            }


            if(config.getTarget() instanceof ImageView){
                request.into((ImageView) config.getTarget());
            }





    }

    @Nullable
    private RequestCreator getDrawableTypeRequest(SingleConfig config) {

        RequestCreator request = null;
        Picasso picasso = Picasso.with(GlobalConfig.context);
        if(!TextUtils.isEmpty(config.getUrl())){
            request= picasso.load(MyUtil.appendUrl(config.getUrl()));
            paths.add(config.getUrl());
        }else if(!TextUtils.isEmpty(config.getFilePath())){
            request= picasso.load(new File(config.getFilePath()));
            paths.add(config.getFilePath());
        }else if(!TextUtils.isEmpty(config.getContentProvider())){
            request= picasso.load(Uri.parse(config.getContentProvider()));
            paths.add(config.getContentProvider());
        }else if(config.getResId()>0){
            request= picasso.load(config.getResId());
            paths.add(config.getResId()+"");
        }
        return request;
    }

    private void setShapeModeAndBlur(SingleConfig config, RequestCreator request) {
        int shapeMode = config.getShapeMode();
        List<Transformation> transformations = new ArrayList<>();

        if(config.getScaleMode() == ScaleMode.FACE_CROP){
            // transformations.add(new FaceCenterCrop(config.getWidth(), config.getHeight()));//脸部识别
        }

        if(config.isNeedBlur()){
            transformations.add(new BlurTransformation(GlobalConfig.context,config.getBlurRadius()));
        }


        switch (shapeMode){
            case ShapeMode.RECT:

                if(config.getBorderWidth()>0){

                }
                break;
            case ShapeMode.RECT_ROUND:
                transformations.add(new RoundedCornersTransformation( config.getRectRoundRadius(), 0, RoundedCornersTransformation.CornerType.ALL));

                if(config.getBorderWidth()>0){

                }
                if(config.isGif() && config.getRoundOverlayColor()>0){

                }
                break;
            case ShapeMode.OVAL:
                transformations.add(new CropCircleTransformation());
                if(config.getBorderWidth()>0){

                }
                if(config.isGif() && config.getRoundOverlayColor()>0){

                }
                break;
        }

        request.transform(transformations);
    }

    @Override
    public void pause() {
        Picasso.with(GlobalConfig.context).pauseTag(PICASSO);

    }

    @Override
    public void resume() {
        Picasso.with(GlobalConfig.context).resumeTag(PICASSO);
    }


    /**
     * 缓存目录: picasso-cache
     */
    @Override
    public void clearDiskCache() {


        File dir = new File(GlobalConfig.context.getCacheDir(), "picasso-cache");
        if(dir!=null && dir.exists()){
            MyUtil.deleteFolderFile(dir.getAbsolutePath(),false);
        }
    }

    @Override
    public void clearMomoryCache() {
        for(String path : paths){
            Picasso.with(GlobalConfig.context).invalidate(path);
        }

    }

    @Override
    public long getCacheSize() {
        File dir = new File(ImageLoader.context.getCacheDir(), "picasso-cache");
        if(dir!=null && dir.exists()){
            return MyUtil.getFolderSize(dir);
        }else {
            return 0;
        }

    }

    @Override
    public void clearCacheByUrl(String url) {
        Picasso.with(GlobalConfig.context).invalidate(url);
    }

    @Override
    public void clearMomoryCache(View view) {

    }

    @Override
    public void clearMomoryCache(String url) {
        Picasso.with(GlobalConfig.context).invalidate(url);
    }

    /**
     *
     * http://blog.csdn.net/u014592587/article/details/47070075
     * @param url
     * @return
     */
    @Override
    public File getFileFromDiskCache(final String url) {
        Picasso.with(ImageLoader.context)
                .load(url)
                .fetch(new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                    }
                });
        return null;
    }

    /**
     * 无法同步判断
     * 参见:https://github.com/bumptech/Picasso/issues/639
     * @param url
     * @return
     */
    @Override
    public boolean isCached(String url) {
        return false;
    }

    @Override
    public void trimMemory(int level) {
        clearMomoryCache();

    }

    @Override
    public void onLowMemory() {
        clearMomoryCache();
    }



    public static Bitmap blur(Bitmap source, int mRadius, boolean recycleOriginal){
        int mSampling = 1;
        int width = source.getWidth();
        int height = source.getHeight();
        int scaledWidth = width / mSampling;
        int scaledHeight = height / mSampling;
        Bitmap bitmap
                = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.scale(1 / (float) mSampling, 1 / (float) mSampling);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(source, 0, 0, paint);






        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            try {
                bitmap = RSBlur.blur(ImageLoader.context, bitmap, mRadius);
            } catch (RSRuntimeException e) {
                bitmap = FastBlur.blur(bitmap, mRadius, true);
            }
        } else {
            bitmap = FastBlur.blur(bitmap, mRadius, true);
        }
        if(recycleOriginal){
            //source.recycle();
        }

        return bitmap;
    }
}
