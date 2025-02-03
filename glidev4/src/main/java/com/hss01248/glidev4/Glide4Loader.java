package com.hss01248.glidev4;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.renderscript.RSRuntimeException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.disklrucache.DiskLruCache;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.load.resource.gif.GifOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.EmptySignature;
import com.bumptech.glide.util.LruCache;
import com.bumptech.glide.util.Util;
import com.google.gson.Gson;
import com.hss01248.glidebase.drawable.AutoRotateDrawable;
import com.hss01248.glidev4.big.ProgressableGlideUrl;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.MyUtil;
import com.hss01248.image.config.GlobalConfig;
import com.hss01248.image.config.ScaleMode;
import com.hss01248.image.config.ShapeMode;
import com.hss01248.image.config.SingleConfig;
import com.hss01248.image.interfaces.FileGetter;
import com.hss01248.image.interfaces.ILoader;
import com.hss01248.image.utils.ThreadPoolFactory;
import com.hss01248.imagedebugger.IImageSource;
import com.hss01248.imagedebugger.IImgLocalPathGetter;
import com.hss01248.imagedebugger.ImageViewDebugger;
import com.hss01248.media.metadata.FileTypeUtil;
import com.hss01248.media.metadata.MetaDataUtil;
import com.hss01248.media.metadata.MetaInfo;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import jp.wasabeef.glide.transformations.CropCircleWithBorderTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import jp.wasabeef.glide.transformations.internal.FastBlur;
import jp.wasabeef.glide.transformations.internal.RSBlur;

/**
 * Created by Administrator on 2017/3/27 0027.
 * 参考:
 * https://mrfu.me/2016/02/28/Glide_Series_Roundup/
 * <p>
 * 填坑指南:
 * https://www.cnblogs.com/bylijian/p/6908813.html
 * <p>
 * glide 使用roundcorner+ center crop
 */

public class Glide4Loader extends ILoader {


    @Override
    public void init(Context context, int cacheSizeInM) {//glide默认最大容量250MB的文件缓存

        /*Glide.get(context)
                .setMemoryCategory(MemoryCategory.NORMAL);*/
        //BigImageViewer.initialize(GlideBigLoader.with(context, null));
        GlobalConfig.cacheFolderName = DiskCache.Factory.DEFAULT_DISK_CACHE_DIR;
        GlobalConfig.cacheMaxSize = DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE / 1024 / 1024;

    }


    @Override
    public void requestAsBitmap(final SingleConfig config) {
        config.setBitmapListener(new CompressGlideCacheToWebPWrapListener(config));
        RequestManager requestManager = Glide.with(config.getContext());
        int width = config.getWidth();
        int height = config.getHeight();
        if (width <= 0) {
            width = Target.SIZE_ORIGINAL;
        }
        if (height <= 0) {
            height = Target.SIZE_ORIGINAL;
        }

        RequestBuilder<Bitmap> builder = requestManager.asBitmap().addListener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                MyUtil.printException(e);
                config.getBitmapListener().onFail(MyUtil.realException(e));
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                config.getBitmapListener().onSuccess(resource);
                return false;
            }
        });
        RequestOptions options = buildOptions(config);
        getDrawableTypeRequest(config, builder)
                .apply(options)
                .into(width, height);
    }


    @Override
    public void requestForNormalDiaplay(final SingleConfig config) {
        config.setBitmapListener(new CompressGlideCacheToWebPWrapListener(config));
        RequestBuilder builder = getDrawableTypeRequest(config, null);

        if (builder == null) {
            return;
        }
        RequestOptions requestOptions = buildOptions(config);

        if (config.getLoadingResId() != 0) {
            Drawable drawable = new AutoRotateDrawable(config.getContext().getResources().getDrawable(config.getLoadingResId()), 1500);
            requestOptions = requestOptions.placeholder(drawable);
            if (config.getTarget() instanceof ImageView) {
                ImageView imageView = (ImageView) config.getTarget();
                imageView.setScaleType(MyUtil.getScaleTypeForImageView(config.getLoadingScaleType(), false));
            }
        } else if (MyUtil.shouldSetPlaceHolder(config)) {
            requestOptions = requestOptions.placeholder(config.getPlaceHolderResId());
            if (config.getTarget() instanceof ImageView) {
                ImageView imageView = (ImageView) config.getTarget();
                imageView.setScaleType(MyUtil.getScaleTypeForImageView(config.getPlaceHolderScaleType(), false));
            }
        }


        /*if(config.getWidth()>0 && config.getHeight()>0){
            requestOptions.override(config.getWidth(),config.getHeight());
        }*/

        if (config.getErrorResId() > 0) {
            requestOptions = requestOptions.error(config.getErrorResId());
        }


        if (config.getTarget() instanceof ImageView) {
            final ImageView imageView = (ImageView) config.getTarget();
            imageView.setTag(R.drawable.im_item_list_opt, config);
            //requestOptions.dontAnimate();
            builder = builder.apply(requestOptions);


            //gif

            if (config.getUrl() != null && config.getUrl().contains(".gif") && config.isUseThirdPartyGifLoader()) {
                if (imageView.getDrawable() == null) {
                    // 解决每次刷新显示PlaceHolder/LoadingResId,快速多次刷新,会一闪一闪
                    if (config.getLoadingResId() != 0) {
                        Drawable drawable = new AutoRotateDrawable(config.getContext().getResources().getDrawable(config.getLoadingResId()), 1500);
                        imageView.setImageDrawable(drawable);
                    } else if (MyUtil.shouldSetPlaceHolder(config)) {
                        imageView.setImageDrawable(imageView.getContext().getResources().getDrawable(config.getPlaceHolderResId()));
                    }
                }
                Glide.with(config.getContext())
                        .load(config.getUrl())
                        .downloadOnly(new SimpleTarget<File>() {
                            @Override
                            public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                                if (!config.equals(imageView.getTag(R.drawable.im_item_list_opt))) {
                                    return;
                                }
                                String type = FileTypeUtil.getType(resource);

                                pl.droidsonroids.gif.GifDrawable gifDrawable2 = null;
                                try {
                                    gifDrawable2 = new pl.droidsonroids.gif.GifDrawable(resource);
                                    imageView.setScaleType(MyUtil.getScaleTypeForImageView(config.getScaleMode(), false));
                                    imageView.setImageDrawable(gifDrawable2);
                                    if (config.getImageListener() != null) {
                                        config.getImageListener().onSuccess(gifDrawable2, null,
                                                gifDrawable2.getIntrinsicWidth(), gifDrawable2.getIntrinsicHeight());
                                    }
                                } catch (Throwable e) {
                                    if (config.getErrorResId() > 0) {
                                        imageView.setScaleType(MyUtil.getScaleTypeForImageView(config.getErrorScaleType(), false));
                                        imageView.setImageDrawable(imageView.getContext().getResources().getDrawable(config.getErrorResId()));
                                    }
                                    if (config.getImageListener() != null) {
                                        config.getImageListener().onFail(e == null ? new Throwable("unexpected error") : e);
                                    }

                                }
                                warn(imageView, gifDrawable2);
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);
                                if (!config.equals(imageView.getTag(R.drawable.im_item_list_opt))) {
                                    return;
                                }
                                if (config.getErrorResId() > 0) {
                                    imageView.setImageDrawable(imageView.getContext().getResources().getDrawable(config.getErrorResId()));
                                }
                                warn(imageView, errorDrawable);
                            }
                        });
                return;
            }

            config.loadStartTime = System.currentTimeMillis();
            builder.listener(new RequestListener() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                    if (GlobalConfig.debug) {
                        Log.d("onException", "thread :" + Thread.currentThread().getName() + ",onException");
                        Log.d("onException", "model :" + model);
                        Log.d("onException", "Target :" + target);
                        Log.d("onException", "isFirstResource :" + isFirstResource);
                        String desc = MyUtil.printException(e);
                        if (config.equals(imageView.getTag(R.drawable.im_item_list_opt))) {
                            if (!model.toString().startsWith("http")) {
                                Log.w("onException", config.toString());
                            }
                            config.setErrorDes(desc);
                            config.cost = System.currentTimeMillis() - config.loadStartTime;
                        }

                    }
                    if (target instanceof ImageViewTarget) {
                        ImageViewTarget view = (ImageViewTarget) target;
                        ImageView imageView1 = (ImageView) view.getView();
                        imageView1.setScaleType(MyUtil.getScaleTypeForImageView(config.getErrorScaleType(), false));
                    }

                    if (config.getImageListener() != null) {
                        config.getImageListener().onFail(e == null ? new Throwable("unexpected error") : MyUtil.realException(e));
                    }
                    return false;
                }

                @Override
                public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                    if (target instanceof ImageViewTarget) {
                        ImageViewTarget view = (ImageViewTarget) target;
                        ImageView imageView1 = (ImageView) view.getView();
                        imageView1.setScaleType(MyUtil.getScaleTypeForImageView(config.getScaleMode(), true, true));
                    }


                    if (GlobalConfig.debug) {
                        Log.d("onResourceReady", "thread :" + Thread.currentThread().getName() + ",onResourceReady");
                        Log.d("onResourceReady", "resource :" + resource);

                        if (config.equals(imageView.getTag(R.drawable.im_item_list_opt))) {
                            config.cost = System.currentTimeMillis() - config.loadStartTime;
                        }
                    }
                    warn(imageView, resource);
                    /*if(resource instanceof Bitmapdr){
                        GlideBitmapDrawable drawable = (GlideBitmapDrawable) resource;
                        Bitmap bitmap = drawable.getBitmap();
                        if(GlobalConfig.debug){
                            Log.d("onResourceReady",MyUtil.printBitmap(bitmap));
                        }
                    }else */
                    if (resource instanceof GifDrawable) {
                        GifDrawable gifDrawable = (GifDrawable) resource;
                        //Grow heap (frag case) to 74.284MB for 8294412-byte allocation
                        Log.w("onResourceReady", "gif :" + gifDrawable.getIntrinsicWidth() + "x" + gifDrawable.getIntrinsicHeight() + "x" + gifDrawable.getFrameCount());
                    } else {
                        if (GlobalConfig.debug) {
                            Log.e("onResourceReady", resource + "");
                        }

                    }
                    if (GlobalConfig.debug) {
                        Log.d("onResourceReady", "model :" + model);
                        Log.d("onResourceReady", "Target :" + target);
                        Log.d("onResourceReady", "isFromMemoryCache :");
                        Log.d("onResourceReady", "isFirstResource :" + isFirstResource);
                    }

                    if (config.getImageListener() != null) {
                        /*if(resource instanceof GlideBitmapDrawable){
                            GlideBitmapDrawable drawable = (GlideBitmapDrawable) resource;
                            Bitmap bitmap = drawable.getBitmap();
                            config.getImageListener().onSuccess(drawable,bitmap,bitmap.getWidth(),bitmap.getHeight());
                        }else */
                        if (resource instanceof GifDrawable) {
                            final GifDrawable gifDrawable = (GifDrawable) resource;
                            Bitmap firstFrame = gifDrawable.getFirstFrame();
                            int width = 0;
                            int height = 0;
                            if (firstFrame != null) {
                                width = firstFrame.getWidth();
                                height = firstFrame.getHeight();
                            }
                            config.getImageListener().onSuccess(gifDrawable, firstFrame, width, height);
                            if (gifDrawable.getFrameCount() > 8) {
                                if (GlobalConfig.debug) {
                                    Log.e("onResourceReady gif", "gif frame count too many:" + gifDrawable.getFrameCount());
                                }

                            }
                            //Grow heap (frag case) to 74.284MB for 8294412-byte allocation
                            if (GlobalConfig.debug) {
                                Log.w("onResourceReady", "gif :" + gifDrawable.getIntrinsicWidth() + "x" + gifDrawable.getIntrinsicHeight() + "x" + gifDrawable.getFrameCount());
                            }

                            if (config.isUseThirdPartyGifLoader()) {
                                ImageLoader.getActualLoader().getFileFromDiskCache((String) model, new FileGetter() {
                                    @Override
                                    public void onSuccess(File file, int width, int height) {
                                        if (!config.equals(imageView.getTag(R.drawable.im_item_list_opt))) {
                                            return;
                                        }
                                        pl.droidsonroids.gif.GifDrawable gifDrawable2 = null;
                                        try {
                                            gifDrawable2 = new pl.droidsonroids.gif.GifDrawable(file);
                                            imageView.setImageDrawable(gifDrawable2);
                                            gifDrawable.stop();
                                        } catch (Throwable e) {
                                            if (GlobalConfig.debug) {
                                                MyUtil.printException(e);
                                            }
                                            imageView.setImageDrawable(gifDrawable);
                                        }
                                        warn(imageView, gifDrawable2);
                                    }

                                    @Override
                                    public void onFail(Throwable e) {
                                        if (!config.equals(imageView.getTag(R.drawable.im_item_list_opt))) {
                                            return;
                                        }
                                        if (GlobalConfig.debug) {
                                            MyUtil.printException(e);
                                        }

                                        imageView.setImageDrawable(gifDrawable);
                                        warn(imageView, gifDrawable);
                                    }
                                });
                                return true;
                            }
                        } else {
                            if (GlobalConfig.debug) {
                                Log.e("onResourceReady", resource + "");
                            }

                            if (resource instanceof Drawable) {
                                config.getImageListener().onSuccess((Drawable) resource, null, 0, 0);
                            } else {
                                config.getImageListener().onSuccess(null, null, 0, 0);
                                if (GlobalConfig.debug) {
                                    Log.e("onResourceReady!!!", resource + ", not instance of drawable");
                                }

                            }

                        }
                    }
                    return false;
                }
            }).into(imageView);

        }
    }

    private void warn(ImageView imageView, Object resource) {
        if (resource == null || imageView == null) {
            return;
        }
        if (!GlobalConfig.debug) {
            return;
        }
        if (resource instanceof BitmapDrawable) {

        } else if (resource instanceof GifDrawable) {

        } else if (resource instanceof pl.droidsonroids.gif.GifDrawable) {

        }

    }


    private RequestOptions buildOptions(SingleConfig config) {
        RequestOptions options = new RequestOptions()
                .format(DecodeFormat.PREFER_RGB_565)
                .set(GifOptions.DECODE_FORMAT, DecodeFormat.DEFAULT)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                //.skipMemoryCache(GlobalConfig.debug)
                .transform(getBitmapTransFormations(config));
        return options;
    }

    public File getCacheFile(String url) {
        OriginalKey originalKey = new OriginalKey(url, EmptySignature.obtain());
        SafeKeyGenerator safeKeyGenerator = new SafeKeyGenerator();
        String safeKey = safeKeyGenerator.getSafeKey(originalKey);
        try {
            DiskLruCache diskLruCache = DiskLruCache.open(
                    new File(GlobalConfig.context.getCacheDir(), DiskCache.Factory.DEFAULT_DISK_CACHE_DIR),
                    1, 1, DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE);
            DiskLruCache.Value value = diskLruCache.get(safeKey);
            if (value != null) {
                return value.getFile(0);
            }
        } catch (IOException e) {
            MyUtil.printException(e);
        }
        return null;
    }


    @Override
    public void debug(final SingleConfig config) {
        if (config.getTarget() instanceof ImageView) {
            final ImageView imageView = (ImageView) config.getTarget();
            ImageViewDebugger.enableDebug(imageView, new IImageSource() {
                @Override
                public String getUri() {
                    Object o = imageView.getTag(R.drawable.im_item_list_opt);
                    SingleConfig singleConfig = (SingleConfig) o;
                    return singleConfig.getSourceString();
                }

                @Override
                public String getErrorDes() {
                    Object o = imageView.getTag(R.drawable.im_item_list_opt);
                    SingleConfig singleConfig = (SingleConfig) o;
                    return singleConfig.getErrorDes();
                }

                @Override
                public void getLocalFilePath(final IImgLocalPathGetter getter) {
                    getFileFromDiskCache(getUri(), new FileGetter() {
                        @Override
                        public void onSuccess(File file, int width, int height) {
                            getter.onGet(file);
                        }

                        @Override
                        public void onFail(Throwable e) {
                            getter.onError(e);

                        }
                    });

                }
            });
/*

            imageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() != MotionEvent.ACTION_DOWN) {
                        return false;
                    }

                    Object o = v.getTag(R.drawable.im_item_list_opt);
                    if (!(o instanceof SingleConfig)) {
                        return false;
                    }
                    if (event.getX() > MyUtil.dip2px(20) || event.getY() > MyUtil.dip2px(20)) {
                        return false;
                    }

                    SingleConfig singleConfig = (SingleConfig) o;






                    showPop((ImageView) v, singleConfig);
                    return false;
                }
            });*/

        }
    }

    private int getUseableResId(SingleConfig config) {
        if (config.getPlaceHolderResId() != 0) {
            config.setScaleMode(config.getPlaceHolderScaleType());
            return config.getPlaceHolderResId();
        }/*else if(config.getErrorResId() != 0){
            config.setScaleMode(config.getErrorScaleType());
            return config.getErrorResId();
        }*/
        return 0;
    }

    @NonNull
    private GlideUrl getGlideUrl(final SingleConfig config) {
        return new GlideUrl(config.getUrl()) {
            @Override
            public String getCacheKey() {
                if (!TextUtils.isEmpty(config.urlForCacheKey)) {
                    return config.urlForCacheKey;
                }
                return super.getCacheKey();
            }
        };
    }

    @Nullable
    private RequestBuilder getDrawableTypeRequest(SingleConfig config, RequestBuilder requestManager) {
        if (requestManager == null) {
            String url = config.getSourceString();
            if (!TextUtils.isEmpty(url)) {
                if (url.contains("?")) {
                    url = url.substring(0, url.indexOf("?"));
                }
                if (url.endsWith(".gif")) {
                    // https://s5.gifyu.com
                    //requestManager = Glide.with(config.getContext()).asGif();
                    requestManager = Glide.with(config.getContext()).asDrawable();
                } else {
                    requestManager = Glide.with(config.getContext()).asDrawable();
                }
            } else {
                requestManager = Glide.with(config.getContext()).asDrawable();
            }
        }

        RequestBuilder request = null;

        if (!TextUtils.isEmpty(config.getSourceString())) {
            if (config.getSourceString().startsWith("http")) {
                request = requestManager.load(getGlideUrl(config));
            } else if (config.getSourceString().startsWith("/storage/")) {
                //兼容avif
                request = requestManager.load(new File(config.getSourceString()));
            } else {
                request = requestManager.load(config.getSourceString());
            }
        } else if (config.getResId() != 0) {
            request = requestManager.load(config.getResId());
        } else if (config.getBytes() != null) {
            request = requestManager.load(config.getBytes());
        } else {
            //request= requestManager.load("http://www.baidu.com/1.jpg");//故意失败
            int resId = getUseableResId(config);
            if (resId != 0) {
                request = requestManager.load(resId);
            } else {
                request = requestManager.load("");
            }
        }
       /* if(!TextUtils.isEmpty(config.getUrl()) && config.getUrl().contains(".gif")){
            request.diskCacheStrategy(DiskCacheStrategy.ALL);//只缓存result
        }else{
            request.diskCacheStrategy(DiskCacheStrategy.SOURCE);//只缓存原图
        }*/

        request.diskCacheStrategy(DiskCacheStrategy.ALL);//只缓存原图

        return request;
    }

    private Transformation[] getBitmapTransFormations(SingleConfig config) {

        Transformation[] forms = null;
        int shapeMode = config.getShapeMode();
        List<Transformation> transformations = new ArrayList<>();

        if (config.isCropFace()) {
            // transformations.add(new FaceCenterCrop());//脸部识别
        }

        if (config.getScaleMode() == ScaleMode.CENTER_CROP) {
            transformations.add(new CenterCrop());
        } else {
            transformations.add(new FitCenter());
        }


        if (config.isNeedBlur()) {
            transformations.add(new BlurTransformation(config.getBlurRadius()));
        }

        switch (shapeMode) {
            case ShapeMode.RECT:

                if (config.getBorderWidth() > 0) {

                }
                break;
            case ShapeMode.RECT_ROUND:
            case ShapeMode.RECT_ROUND_ONLY_TOP:

                RoundedCornersTransformation.CornerType cornerType = RoundedCornersTransformation.CornerType.ALL;
                if (shapeMode == ShapeMode.RECT_ROUND_ONLY_TOP) {
                    cornerType = RoundedCornersTransformation.CornerType.TOP;
                }
                /*transformations.add(new BorderRoundTransformation2(config.getContext(),
                        config.getRectRoundRadius(), 0,config.getBorderWidth(),
                        config.getContext().getResources().getColor(config.getBorderColor()),0x0b1100));*/

                /*if(config.getBorderWidth() > 0 && config.getBorderColor() != 0){
                    transformations.add(new BorderRoundTransformation(config.getContext(),
                            config.getRectRoundRadius(), 0,config.getBorderWidth(),
                            config.getContext().getResources().getColor(config.getBorderColor()),0x0b1100));
                }else {*/
                transformations.add(new RoundedCornersTransformation(
                        config.getRectRoundRadius(), config.getBorderWidth(), cornerType));
                // }

                break;
            case ShapeMode.OVAL:
                if (config.getBorderWidth() > 0 && config.getBorderColor() != 0) {
                    transformations.add(new CropCircleWithBorderTransformation(
                            config.getBorderWidth(), config.getContext().getResources().getColor(config.getBorderColor())));
                } else {
                    transformations.add(new CropCircleTransformation());
                }


                break;
            default:
                break;
        }

        if (!transformations.isEmpty()) {
            forms = new Transformation[transformations.size()];
            for (int i = 0; i < transformations.size(); i++) {
                forms[i] = transformations.get(i);
            }
            return forms;
        }
        return forms;

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
        ThreadPoolFactory.getNormalPool().execute(new Runnable() {
            @Override
            public void run() {
                Glide.get(ImageLoader.context).clearDiskCache();
            }
        });


       /* File dir = new File(BigLoader.context.getCacheDir(), DiskCache.Factory.DEFAULT_DISK_CACHE_DIR);
        if(dir!=null && dir.exists()){
            MyUtil.deleteFolderFile(dir.getAbsolutePath(),false);
        }*/
    }

    @Override
    public void clearMomoryCache() {
        Glide.get(ImageLoader.context).clearMemory();
    }

    @Override
    public long getCacheSize() {
        return MyUtil.getCacheSize();

    }

    @Override
    public void clearCacheByUrl(String url) {
    }

    @Override
    public void clearMomoryCache(View view) {
        Glide.with(view).clear(view);
    }

    @Override
    public void clearMomoryCache(String url) {


    }

    /**
     * glide中只能异步,可以用CacheHitEvent+ url去接收
     *
     * @param url
     * @return
     */
    @Override
    public File getFileFromDiskCache(final String url) {
        return null;
    }

    @Override
    public void getFileFromDiskCache(final String url, final FileGetter getter) {
        if(TextUtils.isEmpty(url)){
            getter.onFail(new IOException("url is empty"));
            return;
        }
        final File file = new File(url);
        if (file.exists() && file.isFile() && file.length() > 0) {
            int[] wh = MyUtil.getImageWidthHeight(url);
            getter.onSuccess(file, wh[0], wh[1]);
            return;
        }

        ThreadPoolFactory.getDownLoadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    getter.onStart();
                    final File resource = Glide.with(GlobalConfig.context)
                            .asFile()
                            .load(new ProgressableGlideUrl(url))
                            .submit().get();
                    if (resource.exists() && resource.isFile() && resource.length() > 0) {
                        //Log.i("glide onResourceReady", "onResourceReady2  --" + resource.getAbsolutePath());
                        if (GlobalConfig.debug) {
                            MetaInfo metaData2 = MetaDataUtil.getMetaData2(Uri.fromFile(resource));
                            LogUtils.json(new Gson().newBuilder().setPrettyPrinting().create().toJson(metaData2));
                          /*  LubanUtil.init(Utils.getApp(),true,null);
                            //todo bug: jpg质量在75以下不转webp, webp重复压缩
                            File file1 = Luban.with(Utils.getApp())
                                    .ignoreBy(30)
                                    .targetQuality(75)
                                    .targetFormat(Bitmap.CompressFormat.WEBP)
                                    .keepExif(true)
                                    .noResize(true)
                                    .setTargetDir(resource.getParent())
                                    .get(resource.getAbsolutePath());
                            FileUtils.copy(file1, resource, new FileUtils.OnReplaceListener() {
                                @Override
                                public boolean onReplace(File srcFile, File destFile) {
                                    return true;
                                }
                            });
                            LogUtils.json(new Gson().newBuilder().setPrettyPrinting().create().toJson(metaData2));*/
                        }

                        final int[] wh = MyUtil.getImageWidthHeight(resource.getAbsolutePath());
                        MyUtil.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                getter.onSuccess(resource, wh[0], wh[1]);
                            }
                        });
                    } else {
                        Log.w(" glide onloadfailed", "onLoadFailed  --" + url);
                        MyUtil.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                getter.onFail(new Throwable("file not exist"));
                            }
                        });
                    }
                } catch (final Throwable throwable) {
                    //throwable.printStackTrace();
                    // call GlideException#logRootCauses(String) for more detail
                    MyUtil.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            getter.onFail(MyUtil.realException(throwable));
                        }
                    });

                }
            }
        });

    }

    /**
     * 无法同步判断
     * 参见:https://github.com/bumptech/glide/issues/639
     * <p>
     * 4.0以上可用：
     * val file: File? = try {
     * Glide.with(view.context).downloadOnly().load(url).apply(RequestOptions().onlyRetrieveFromCache(true)).submit().get()
     * } catch (e: Exception) {
     * e.printStackTrace()
     * null
     * }
     * https://github.com/bumptech/glide/issues/2972
     *
     * @param url
     * @return
     */
    @Override
    public boolean isCached(String url) {


        OriginalKey originalKey = new OriginalKey(url, EmptySignature.obtain());
        SafeKeyGenerator safeKeyGenerator = new SafeKeyGenerator();
        String safeKey = safeKeyGenerator.getSafeKey(originalKey);
        try {
            DiskLruCache diskLruCache = DiskLruCache.open(new File(GlobalConfig.context.getCacheDir(),
                    DiskCache.Factory.DEFAULT_DISK_CACHE_DIR), 1, 1, DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE);
            DiskLruCache.Value value = diskLruCache.get(safeKey);
            if (value != null && value.getFile(0).exists() && value.getFile(0).length() > 30) {
                return true;
            }
        } catch (Throwable e) {
            MyUtil.printException(e);
        }
        return false;

    }

    @Override
    public void trimMemory(int level) {
        Glide.get(GlobalConfig.context).onTrimMemory(level);
    }

    @Override
    public void onLowMemory() {
        Glide.get(GlobalConfig.context).onLowMemory();
    }

    @Override
    public void download(String url, FileGetter getter) {
        getFileFromDiskCache(url, getter);
    }


    public static Bitmap blur(Bitmap source, int mRadius, boolean recycleOriginal) {
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
        if (recycleOriginal) {
            source.recycle();
        }

        return bitmap;
    }


    private static class OriginalKey implements Key {

        private final String id;
        private final Key signature;

        public OriginalKey(String id, Key signature) {
            this.id = id;
            this.signature = signature;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            OriginalKey that = (OriginalKey) o;

            if (!id.equals(that.id)) {
                return false;
            }
            if (!signature.equals(that.signature)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + signature.hashCode();
            return result;
        }

        @Override
        public void updateDiskCacheKey(MessageDigest messageDigest) {
            try {
                messageDigest.update(id.getBytes(STRING_CHARSET_NAME));
            } catch (UnsupportedEncodingException e) {
                MyUtil.printException(e);
            }
            signature.updateDiskCacheKey(messageDigest);
        }
    }

    private static class SafeKeyGenerator {
        private final LruCache<Key, String> loadIdToSafeHash = new LruCache<Key, String>(1000);

        public String getSafeKey(Key key) {
            String safeKey;
            synchronized (loadIdToSafeHash) {
                safeKey = loadIdToSafeHash.get(key);
            }
            if (safeKey == null) {
                try {
                    MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                    key.updateDiskCacheKey(messageDigest);
                    safeKey = Util.sha256BytesToHex(messageDigest.digest());
                } catch (NoSuchAlgorithmException e) {
                    MyUtil.printException(e);
                }
                synchronized (loadIdToSafeHash) {
                    loadIdToSafeHash.put(key, safeKey);
                }
            }
            return safeKey;
        }
    }


}
