package com.hss01248.glidev4;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.renderscript.RSRuntimeException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


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
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;

import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;

import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.EmptySignature;
import com.bumptech.glide.util.LruCache;
import com.bumptech.glide.util.Util;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.event.CacheHitEvent;
import com.github.piasy.biv.event.ErrorEvent;
import com.github.piasy.biv.view.BigImageView;

import com.hss01248.glidev4.big.GlideBigLoader;
import com.hss01248.glidev4.big.ProgressableGlideUrl;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.MyUtil;
import com.hss01248.image.config.GlobalConfig;
import com.hss01248.image.config.ScaleMode;
import com.hss01248.image.config.ShapeMode;
import com.hss01248.image.config.SingleConfig;
import com.hss01248.glidebase.drawable.AutoRotateDrawable;
import com.hss01248.image.interfaces.FileGetter;
import com.hss01248.image.interfaces.ILoader;
import com.hss01248.image.utils.ThreadPoolFactory;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
 *
 * 填坑指南:
 * https://www.cnblogs.com/bylijian/p/6908813.html
 *
 * glide 使用roundcorner+ center crop
 */

public class Glide4Loader extends ILoader {

    private  ExecutorService executor;
    @Override
    public void init(Context context, int cacheSizeInM) {//glide默认最大容量250MB的文件缓存

        /*Glide.get(context)
                .setMemoryCategory(MemoryCategory.NORMAL);*/
        BigImageViewer.initialize(GlideBigLoader.with(context,null));
        GlobalConfig.cacheFolderName = DiskCache.Factory.DEFAULT_DISK_CACHE_DIR;
        GlobalConfig.cacheMaxSize =  DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE/1024/1024;

    }



    @Override
    public void requestAsBitmap(final SingleConfig config) {
        RequestManager requestManager =  Glide.with(config.getContext());
        int width = config.getWidth();
        int height = config.getHeight();
        if(width <=0){
            width = Target.SIZE_ORIGINAL;
        }
        if(height <=0){
            height = Target.SIZE_ORIGINAL;
        }

        RequestBuilder<Bitmap> builder =  requestManager.asBitmap().addListener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                if(e !=null){
                    e.printStackTrace();
                }
                config.getBitmapListener().onFail(e);
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                config.getBitmapListener().onSuccess(resource);
                return false;
            }
        });
        RequestOptions options =  buildOptions(config);
        getDrawableTypeRequest(config,  builder)
                .apply(options)
                .into(width,height);
    }



    @Override
    public void requestForNormalDiaplay(final SingleConfig config) {

        final RequestBuilder builder = getDrawableTypeRequest(config,null);

        if(builder ==null){
            return;
        }
        RequestOptions requestOptions = buildOptions(config);

        if(config.getLoadingResId() != 0){
            Drawable drawable = new AutoRotateDrawable(config.getContext().getResources().getDrawable(config.getLoadingResId()), 1500);
            requestOptions.placeholder(drawable);
            if(config.getTarget() instanceof ImageView){
                ImageView imageView = (ImageView) config.getTarget();
                imageView.setScaleType(MyUtil.getScaleTypeForImageView(config.getLoadingScaleType(),false));
            }
        }else if(MyUtil.shouldSetPlaceHolder(config)){
            requestOptions.placeholder(config.getPlaceHolderResId());
            if(config.getTarget() instanceof ImageView){
                ImageView imageView = (ImageView) config.getTarget();
                imageView.setScaleType(MyUtil.getScaleTypeForImageView(config.getPlaceHolderScaleType(),false));
            }
        }


        if(config.getWidth()>0 && config.getHeight()>0){
            requestOptions.override(config.getWidth(),config.getHeight());
        }

        if(config.getErrorResId() >0){
            requestOptions.error(config.getErrorResId());
        }



        if(config.getTarget() instanceof ImageView){
            final ImageView imageView = (ImageView) config.getTarget();
            imageView.setTag(R.drawable.im_item_list_opt,config);
            requestOptions.dontAnimate();
            builder.apply(requestOptions);



            //gif
            if(config.getUrl() != null && config.getUrl().contains(".gif") && config.isUseThirdPartyGifLoader()){
                if(config.getLoadingResId() != 0){
                    Drawable drawable = new AutoRotateDrawable(config.getContext().getResources().getDrawable(config.getLoadingResId()), 1500);
                    imageView.setImageDrawable(drawable);
                }else if(MyUtil.shouldSetPlaceHolder(config)){
                    imageView.setImageDrawable(imageView.getContext().getResources().getDrawable(config.getPlaceHolderResId()));
                }

                Glide.with(config.getContext())
                        .load(config.getUrl())
                        .downloadOnly(new SimpleTarget<File>() {
                            @Override
                            public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                                if(!config.equals(imageView.getTag(R.drawable.im_item_list_opt))){
                                    return;
                                }
                                pl.droidsonroids.gif.GifDrawable gifDrawable2 = null;
                                try {
                                    gifDrawable2 = new pl.droidsonroids.gif.GifDrawable(resource);
                                    imageView.setScaleType(MyUtil.getScaleTypeForImageView(config.getScaleMode(),false));
                                    imageView.setImageDrawable(gifDrawable2);
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                    if(config.getErrorResId() >0){
                                        imageView.setImageDrawable(imageView.getContext().getResources().getDrawable(config.getErrorResId()));
                                    }

                                }
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);
                                if(!config.equals(imageView.getTag(R.drawable.im_item_list_opt))){
                                    return;
                                }
                                if(config.getErrorResId() >0){
                                    imageView.setImageDrawable(imageView.getContext().getResources().getDrawable(config.getErrorResId()));
                                }
                            }
                        });
                return;
            }

            config.loadStartTime = System.currentTimeMillis();
            builder.listener(new RequestListener() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                    if(GlobalConfig.debug){
                        Log.d("onException","thread :"+Thread.currentThread().getName() +",onException");
                        Log.d("onException","model :"+model);
                        Log.d("onException","Target :"+target);
                        Log.d("onException","isFirstResource :"+isFirstResource);
                        if(config.equals(imageView.getTag(R.drawable.im_item_list_opt)) ){
                            if(!model.toString().startsWith("http")){
                                Log.w("onException",config.toString());
                            }
                            config.setErrorDes(MyUtil.printException(e));
                            config.cost  = System.currentTimeMillis() - config.loadStartTime;
                        }
                        if(e != null){
                            e.printStackTrace();
                        }
                    }
                    if(target instanceof ImageViewTarget){
                        ImageViewTarget view = (ImageViewTarget) target;
                        ImageView imageView1 = (ImageView) view.getView();
                        imageView1.setScaleType(MyUtil.getScaleTypeForImageView(config.getErrorScaleType(),false));
                    }

                    if(config.getImageListener() != null){
                        config.getImageListener().onFail(e == null ? new Throwable("unexpected error") : e);
                    }
                    return false;
                }

                @Override
                public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                    if(target instanceof ImageViewTarget){
                        ImageViewTarget view = (ImageViewTarget) target;
                        ImageView imageView1 = (ImageView) view.getView();
                        imageView1.setScaleType(MyUtil.getScaleTypeForImageView(config.getScaleMode(),true,true));
                    }


                    if(GlobalConfig.debug){
                        Log.d("onResourceReady","thread :"+Thread.currentThread().getName() +",onResourceReady");
                        Log.d("onResourceReady","resource :"+resource);

                        if(config.equals(imageView.getTag(R.drawable.im_item_list_opt)) ){
                            config.cost  = System.currentTimeMillis() - config.loadStartTime;
                        }
                    }

                    /*if(resource instanceof Bitmapdr){
                        GlideBitmapDrawable drawable = (GlideBitmapDrawable) resource;
                        Bitmap bitmap = drawable.getBitmap();
                        if(GlobalConfig.debug){
                            Log.d("onResourceReady",MyUtil.printBitmap(bitmap));
                        }
                    }else */if(resource instanceof GifDrawable){
                        GifDrawable gifDrawable = (GifDrawable) resource;
                        //Grow heap (frag case) to 74.284MB for 8294412-byte allocation
                        Log.w("onResourceReady","gif :"+gifDrawable.getIntrinsicWidth()+"x"+gifDrawable.getIntrinsicHeight()+"x"+gifDrawable.getFrameCount());
                    }else{
                        if(GlobalConfig.debug){
                            Log.e("onResourceReady",resource+"");
                        }

                    }
                    if(GlobalConfig.debug){
                        Log.d("onResourceReady","model :"+model);
                        Log.d("onResourceReady","Target :"+target);
                        Log.d("onResourceReady","isFromMemoryCache :");
                        Log.d("onResourceReady","isFirstResource :"+isFirstResource);
                    }

                    if(config.getImageListener() != null ) {
                        /*if(resource instanceof GlideBitmapDrawable){
                            GlideBitmapDrawable drawable = (GlideBitmapDrawable) resource;
                            Bitmap bitmap = drawable.getBitmap();
                            config.getImageListener().onSuccess(drawable,bitmap,bitmap.getWidth(),bitmap.getHeight());
                        }else */if(resource instanceof GifDrawable){
                            final GifDrawable gifDrawable = (GifDrawable) resource;
                            config.getImageListener().onSuccess(gifDrawable,gifDrawable.getFirstFrame(),gifDrawable.getFirstFrame().getWidth(),gifDrawable.getFirstFrame().getHeight());
                            if(gifDrawable.getFrameCount()> 8){
                                if(GlobalConfig.debug){
                                    Log.e("onResourceReady gif","gif frame count too many:"+gifDrawable.getFrameCount());
                                }

                            }
                            //Grow heap (frag case) to 74.284MB for 8294412-byte allocation
                            if(GlobalConfig.debug){
                                Log.w("onResourceReady","gif :"+gifDrawable.getIntrinsicWidth()+"x"+gifDrawable.getIntrinsicHeight()+"x"+gifDrawable.getFrameCount());
                            }

                            if(config.isUseThirdPartyGifLoader()){
                                ImageLoader.getActualLoader().getFileFromDiskCache((String) model, new FileGetter() {
                                    @Override
                                    public void onSuccess(File file, int width, int height) {
                                        if(!config.equals(imageView.getTag(R.drawable.im_item_list_opt))){
                                            return;
                                        }
                                        pl.droidsonroids.gif.GifDrawable gifDrawable2 = null;
                                        try {
                                            gifDrawable2 = new pl.droidsonroids.gif.GifDrawable(file);
                                            imageView.setImageDrawable(gifDrawable2);
                                            gifDrawable.stop();
                                        } catch (Throwable e) {
                                            if(GlobalConfig.debug){
                                                e.printStackTrace();
                                            }
                                            imageView.setImageDrawable(gifDrawable);
                                        }
                                    }

                                    @Override
                                    public void onFail(Throwable e) {
                                        if(!config.equals(imageView.getTag(R.drawable.im_item_list_opt))){
                                            return;
                                        }
                                        if(GlobalConfig.debug){
                                            e.printStackTrace();
                                        }

                                        imageView.setImageDrawable(gifDrawable);
                                    }
                                });
                                return true;
                            }
                        }else {
                            if(GlobalConfig.debug){
                                Log.e("onResourceReady",resource+"");
                            }

                            if(resource instanceof Drawable){
                                config.getImageListener().onSuccess((Drawable)resource,null,0,0);
                            }else{
                                config.getImageListener().onSuccess(null,null,0,0);
                                if(GlobalConfig.debug){
                                    Log.e("onResourceReady!!!",resource+", not instance of drawable");
                                }

                            }

                        }
                    }
                    return false;
                }
            }).into(imageView);

        }
    }


    private RequestOptions buildOptions(SingleConfig config) {
        RequestOptions options = new RequestOptions()
                .format(DecodeFormat.PREFER_RGB_565)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .transform(getBitmapTransFormations(config));
        return options;
    }

    public File getCacheFile(String url) {
        OriginalKey originalKey = new OriginalKey(url, EmptySignature.obtain());
        SafeKeyGenerator safeKeyGenerator = new SafeKeyGenerator();
        String safeKey = safeKeyGenerator.getSafeKey(originalKey);
        try {
            DiskLruCache diskLruCache = DiskLruCache.open(new File(GlobalConfig.context.getCacheDir(), DiskCache.Factory.DEFAULT_DISK_CACHE_DIR), 1, 1, DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE);
            DiskLruCache.Value value = diskLruCache.get(safeKey);
            if (value != null) {
                return value.getFile(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void debug(final SingleConfig config) {
        if(config.getTarget() instanceof ImageView) {
             ImageView imageView = (ImageView) config.getTarget();
            imageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() != MotionEvent.ACTION_DOWN){
                        return false;
                    }

                    Object o = v.getTag(R.drawable.im_item_list_opt);
                    if(!(o instanceof SingleConfig)){
                        return false;
                    }
                    if(event.getX() > MyUtil.dip2px(20) || event.getY() > MyUtil.dip2px(20)){
                        return false;
                    }

                    SingleConfig singleConfig = (SingleConfig) o;
                    showPop((ImageView)v,singleConfig);
                    return false;
                }
            });

        }
    }

    private void showPop(ImageView v, final SingleConfig config) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext());
        Context context;
        ScrollView scrollView = new ScrollView(v.getContext());
        LinearLayout linearLayout = new LinearLayout(v.getContext());
        scrollView.addView(linearLayout);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        final TextView textView = new TextView(v.getContext());
        String desc = config.getUrl()+"\n\n";
        desc += "load cost :"+config.cost+"ms\n\n";

        if(!"null".equals(config.getErrorDes()) && !TextUtils.isEmpty(config.getErrorDes())){
            desc += config.getErrorDes()+"\n\n";
        }

        Drawable drawable = v.getDrawable();
       /* if(drawable instanceof GlideBitmapDrawable){
            GlideBitmapDrawable glideBitmapDrawable = (GlideBitmapDrawable) drawable;
            Bitmap bitmap = glideBitmapDrawable.getBitmap();
            desc += MyUtil.printBitmap(bitmap)+"\n";
            if (MyUtil.isBitmapTooLarge(bitmap.getWidth(),bitmap.getHeight(), v)) {
                textView.setTextColor(Color.RED);
            }
        }else */if(drawable instanceof BitmapDrawable){
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            desc += MyUtil.printBitmap(bitmap)+"\n";
            if (MyUtil.isBitmapTooLarge(bitmap.getWidth(),bitmap.getHeight(), v)) {
                textView.setTextColor(Color.RED);
            }
        }else /*if (drawable instanceof SquaringDrawable){
            SquaringDrawable bitmap = (SquaringDrawable) drawable;
            desc += "\nSquaringDrawable, w:"+bitmap.getIntrinsicWidth() +",h:"+bitmap.getIntrinsicHeight()+"\n";
            if (MyUtil.isBitmapTooLarge(bitmap.getIntrinsicWidth(),bitmap.getIntrinsicHeight(), v)) {
                textView.setTextColor(Color.RED);
            }
        }else*/ if(drawable instanceof GifDrawable){
            GifDrawable gifDrawable = (GifDrawable) drawable;
            //Grow heap (frag case) to 74.284MB for 8294412-byte allocation
            desc +="gif :"+gifDrawable.getIntrinsicWidth()+"x"+gifDrawable.getIntrinsicHeight()+"x"+gifDrawable.getFrameCount();

            if (MyUtil.isBitmapTooLarge(gifDrawable.getIntrinsicWidth(),gifDrawable.getIntrinsicHeight(), v)) {
                textView.setTextColor(Color.RED);
            }
            if(gifDrawable.getFrameCount() > 10){
                desc += "\nframeCount is too many!!!!!!!!\n";
                textView.setTextColor(Color.parseColor("#8F0005"));
            }

        }else {
            desc += "drawable:"+drawable;
        }

        desc += "\n" + MyUtil.printImageView(v);

        getFileFromDiskCache(config.getUrl(), new FileGetter() {
            @Override
            public void onSuccess(File file, int width, int height) {
                String text = textView.getText().toString();
                text += "\n\n" + MyUtil.printExif(file.getAbsolutePath());
                textView.setText(text);
            }

            @Override
            public void onFail(Throwable e) {
                String text = textView.getText().toString();
                text += "\n\n get cache file failed :\n" ;
                if(e != null){
                    text +=  e.getClass().getName()+" "+e.getMessage();
                }

                textView.setText(text);
            }
        });



        textView.setText(desc);

        textView.setPadding(20,20,20,20);

        ImageView imageView =  new ImageView(v.getContext());
        imageView.setImageDrawable(drawable);
        linearLayout.addView(imageView);
        linearLayout.addView(textView);
        linearLayout.setPadding(10,30,10,20);



        dialog.setView(scrollView);
        dialog.setPositiveButton("拷贝链接", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MyUtil.copyText(config.getUrl());
                Toast.makeText(textView.getContext(),"已拷贝链接",Toast.LENGTH_SHORT).show();
            }
        });
        dialog.setNegativeButton("拷贝，并在浏览器中打开此链接", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    MyUtil.copyText(config.getUrl());
                    Toast.makeText(textView.getContext(),"已拷贝链接",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    Uri content_url = Uri.parse(config.getUrl());
                    intent.setData(content_url);
                    textView.getContext().startActivity(intent);
                }catch (Throwable e){
                    e.printStackTrace();
                }

            }
        });
        dialog.show();

    }

    private int getUseableResId(SingleConfig config) {
        if(config.getPlaceHolderResId() != 0){
            config.setScaleMode(config.getPlaceHolderScaleType());
            return config.getPlaceHolderResId();
        }/*else if(config.getErrorResId() != 0){
            config.setScaleMode(config.getErrorScaleType());
            return config.getErrorResId();
        }*/
        return 0;
    }

    @Nullable
    private RequestBuilder getDrawableTypeRequest(SingleConfig config, RequestBuilder requestManager) {
        RequestBuilder request = null;
        if(requestManager  != null){
            if(!TextUtils.isEmpty(config.getUrl())){
                request= requestManager.load(MyUtil.appendUrl(config.getUrl()));
            }else if(!TextUtils.isEmpty(config.getFilePath())){
                request= requestManager.load(config.getFilePath());
            }else if(!TextUtils.isEmpty(config.getContentProvider())){
                request= requestManager.load(Uri.parse(config.getContentProvider()));
            }else if(config.getResId() != 0){
                request= requestManager.load(config.getResId());
            }else if(config.getBytes() != null){
                request = requestManager.load(config.getBytes());
            } else {
                //request= requestManager.load("http://www.baidu.com/1.jpg");//故意失败
                int resId = getUseableResId(config);
                if(resId != 0){
                    request = requestManager.load(resId);
                }else {
                    request= requestManager.load("");
                }
            }
        }else {
            RequestManager requestManager1 = Glide.with(config.getContext());
            if(!TextUtils.isEmpty(config.getUrl())){
                request= requestManager1.load(MyUtil.appendUrl(config.getUrl()));
            }else if(!TextUtils.isEmpty(config.getFilePath())){
                request= requestManager1.load(config.getFilePath());
            }else if(!TextUtils.isEmpty(config.getContentProvider())){
                request= requestManager1.load(Uri.parse(config.getContentProvider()));
            }else if(config.getResId() != 0){
                request= requestManager1.load(config.getResId());
            }else if(config.getBytes() != null){
                request = requestManager1.load(config.getBytes());
            } else {
                //request= requestManager.load("http://www.baidu.com/1.jpg");//故意失败
                int resId = getUseableResId(config);
                if(resId != 0){
                    request = requestManager1.load(resId);
                }else {
                    request= requestManager1.load("");
                }
            }
        }

        return request;
    }

    private Transformation[] getBitmapTransFormations(SingleConfig config) {

        Transformation[] forms = null;
        int shapeMode = config.getShapeMode();
        List<Transformation> transformations = new ArrayList<>();

        if(config.isCropFace()){
            // transformations.add(new FaceCenterCrop());//脸部识别
        }

        if(config.getScaleMode() == ScaleMode.CENTER_CROP){
            transformations.add(new CenterCrop());
        }else{
            transformations.add(new FitCenter());
        }


        if(config.isNeedBlur()){
            transformations.add(new BlurTransformation( config.getBlurRadius()));
        }

        switch (shapeMode){
            case ShapeMode.RECT:

                if(config.getBorderWidth()>0){

                }
                break;
            case ShapeMode.RECT_ROUND:
            case ShapeMode.RECT_ROUND_ONLY_TOP:

                RoundedCornersTransformation.CornerType cornerType = RoundedCornersTransformation.CornerType.ALL;
                if(shapeMode == ShapeMode.RECT_ROUND_ONLY_TOP){
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
                            config.getRectRoundRadius(),config.getBorderWidth(), cornerType));
               // }

                break;
            case ShapeMode.OVAL:
                if(config.getBorderWidth() > 0 && config.getBorderColor() != 0){
                    transformations.add( new CropCircleWithBorderTransformation(
                            config.getBorderWidth(),config.getContext().getResources().getColor(config.getBorderColor())));
                }else {
                    transformations.add( new CropCircleTransformation());
                }


                break;
            default:break;
        }

        if(!transformations.isEmpty()){
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
     * @param url
     * @return
     */
    @Override
    public File getFileFromDiskCache(final String url) {
        return null;
    }

    @Override
    public void getFileFromDiskCache(final String url, final FileGetter getter) {
        if(executor == null){
            executor = Executors.newCachedThreadPool();
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final File resource =    Glide.with(GlobalConfig.context)
                            .asFile()
                            .load(new ProgressableGlideUrl(url))
                            .submit().get();
                    if(resource.exists() && resource.isFile() && resource.length() > 50){
                        Log.i("glide onResourceReady","onResourceReady  --"+ resource.getAbsolutePath());
                        final int[] wh = MyUtil.getImageWidthHeight(resource.getAbsolutePath());
                        MyUtil.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                getter.onSuccess(resource,wh[0],wh[1]);
                            }
                        });


                    }else {
                        Log.w(" glide onloadfailed","onLoadFailed  --"+ url);
                        MyUtil.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                getter.onFail(new Throwable("file not exist"));
                            }
                        });
                    }
                }catch (final Throwable throwable){
                    throwable.printStackTrace();
                    MyUtil.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            getter.onFail(throwable);
                        }
                    });

                }
            }
        });

    }

    /**
     * 无法同步判断
     * 参见:https://github.com/bumptech/glide/issues/639
     *
     * 4.0以上可用：
     * val file: File? = try {
             Glide.with(view.context).downloadOnly().load(url).apply(RequestOptions().onlyRetrieveFromCache(true)).submit().get()
             } catch (e: Exception) {
             e.printStackTrace()
             null
             }
     *https://github.com/bumptech/glide/issues/2972
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
               DiskLruCache diskLruCache = DiskLruCache.open(new File(GlobalConfig.context.getCacheDir(), DiskCache.Factory.DEFAULT_DISK_CACHE_DIR), 1, 1, DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE);
               DiskLruCache.Value value = diskLruCache.get(safeKey);
               if (value != null && value.getFile(0).exists() && value.getFile(0).length() > 30) {
                   return true;
               }
           } catch (Exception e) {
               e.printStackTrace();
           }
           return false;

    }

    @Override
    public void trimMemory(int level) {
        Glide.get(GlobalConfig.context).onTrimMemory(level );
    }

    @Override
    public void onLowMemory() {
        Glide.get(GlobalConfig.context).onLowMemory();
    }

    @Override
    public void download(String url, FileGetter getter) {
        getFileFromDiskCache(url,getter);
    }


    public static Bitmap blur(Bitmap source,int mRadius,boolean recycleOriginal){
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
                e.printStackTrace();
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
                    e.printStackTrace();
                }
                synchronized (loadIdToSafeHash) {
                    loadIdToSafeHash.put(key, safeKey);
                }
            }
            return safeKey;
        }
    }



}
