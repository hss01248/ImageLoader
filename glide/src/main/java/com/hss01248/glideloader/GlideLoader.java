package com.hss01248.glideloader;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.disklrucache.DiskLruCache;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SquaringDrawable;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.EmptySignature;
import com.bumptech.glide.util.LruCache;
import com.bumptech.glide.util.Util;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.view.BigImageView;
import com.hss01248.glideloader.big.GlideBigLoader;
import com.hss01248.glideloader.transform.BorderRoundTransformation;
import com.hss01248.glideloader.transform.CropCircleWithBorderTransformation;
import com.hss01248.image.ImageLoadFailException;
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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
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

public class GlideLoader extends ILoader {
    @Override
    public void init(Context context, int cacheSizeInM) {//glide默认最大容量250MB的文件缓存

        /*Glide.get(context)
                .setMemoryCategory(MemoryCategory.NORMAL);*/
        BigImageViewer.initialize(GlideBigLoader.with(context,MyUtil.getClient(GlobalConfig.ignoreCertificateVerify)));
        GlobalConfig.cacheFolderName = DiskCache.Factory.DEFAULT_DISK_CACHE_DIR;
        GlobalConfig.cacheMaxSize =  DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE/1024/1024;

    }



    @Override
    public void requestAsBitmap(final SingleConfig config) {
        SimpleTarget target = null;
        if(config.getWidth()>0 && config.getHeight()>0){
            target = new SimpleTarget<Bitmap>(config.getWidth(),config.getHeight()) {
                @Override
                public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                    // do something with the bitmap
                    // for demonstration purposes, let's just set it to an ImageView
                    // BitmapPool mBitmapPool = Glide.get(BigLoader.context).getBitmapPool();
                    //bitmap = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight())
                        /*if(config.isNeedBlur()){
                            bitmap = blur(bitmap,config.getBlurRadius(),false);
                            Glide.get(GlobalConfig.context).getBitmapPool().put(bitmap);
                        }
                        if(config.getShapeMode() == ShapeMode.OVAL){
                            bitmap = MyUtil.cropCirle(bitmap,false);
                            Glide.get(GlobalConfig.context).getBitmapPool().put(bitmap);
                        }else if(config.getShapeMode() == ShapeMode.RECT_ROUND){
                            bitmap = MyUtil.rectRound(bitmap,config.getRectRoundRadius(),0);
                            Glide.get(GlobalConfig.context).getBitmapPool().put(bitmap);
                        }*/

                    config.getBitmapListener().onSuccess(bitmap);
                }

                @Override
                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                    super.onLoadFailed(e, errorDrawable);
                    config.getBitmapListener().onFail(e);
                    MyUtil.handleException(new ImageLoadFailException(config.getUsableString(),e));
                }
            };
        }else {
            target = new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                    // do something with the bitmap
                    // for demonstration purposes, let's just set it to an ImageView
                    // BitmapPool mBitmapPool = Glide.get(BigLoader.context).getBitmapPool();
                    //bitmap = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight())
                        /*if(config.isNeedBlur()){
                            bitmap = blur(bitmap,config.getBlurRadius(),false);
                            Glide.get(GlobalConfig.context).getBitmapPool().put(bitmap);
                        }
                        if(config.getShapeMode() == ShapeMode.OVAL){
                            bitmap = MyUtil.cropCirle(bitmap,false);
                            Glide.get(GlobalConfig.context).getBitmapPool().put(bitmap);
                        }else if(config.getShapeMode() == ShapeMode.RECT_ROUND){
                            bitmap = MyUtil.rectRound(bitmap,config.getRectRoundRadius(),0);
                            Glide.get(GlobalConfig.context).getBitmapPool().put(bitmap);
                        }*/

                    config.getBitmapListener().onSuccess(bitmap);
                }

                @Override
                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                    super.onLoadFailed(e, errorDrawable);
                    config.getBitmapListener().onFail(e);
                    MyUtil.handleException(new ImageLoadFailException(config.getUsableString(),e));
                }
            };
        }

        RequestManager requestManager =  Glide.with(config.getContext());
        int width = config.getWidth();
        int height = config.getHeight();
        if(width <=0){
            width = Target.SIZE_ORIGINAL;
        }
        if(height <=0){
            height = Target.SIZE_ORIGINAL;
        }
        getDrawableTypeRequest(config, requestManager)
                .asBitmap()
                .override(width, height)
                .transform(getBitmapTransFormations(config))
                .approximate().into(target);
    }

    @Override
    public void requestForNormalDiaplay(final SingleConfig config) {
        final RequestManager requestManager =  Glide.with(config.getContext());
        final DrawableTypeRequest request = getDrawableTypeRequest(config, requestManager);

        if(request ==null){
            return;
        }

        DrawableRequestBuilder builder = request.thumbnail(1.0f);
        if(config.getLoadingResId() != 0){
            Drawable drawable = new AutoRotateDrawable(config.getContext().getResources().getDrawable(config.getLoadingResId()), 1500);
            builder.placeholder(drawable);
            if(config.getTarget() instanceof ImageView){
                ImageView imageView = (ImageView) config.getTarget();
                imageView.setScaleType(MyUtil.getScaleTypeForImageView(config.getLoadingScaleType(),false));
            }
        }else if(MyUtil.shouldSetPlaceHolder(config)){
            builder.placeholder(config.getPlaceHolderResId());
            if(config.getTarget() instanceof ImageView){
                ImageView imageView = (ImageView) config.getTarget();
                imageView.setScaleType(MyUtil.getScaleTypeForImageView(config.getPlaceHolderScaleType(),false));
            }
        }


        if(config.getWidth()>0 && config.getHeight()>0){
            builder.override(config.getWidth(),config.getHeight());
        }
        builder = builder .bitmapTransform(getBitmapTransFormations(config));

        if(config.getErrorResId() >0){
            builder.error(config.getErrorResId());
        }



        if(config.getTarget() instanceof ImageView){
            final ImageView imageView = (ImageView) config.getTarget();
            imageView.setTag(R.drawable.im_item_list_opt,config);

            builder.dontAnimate();


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
                            public void onResourceReady(File file, GlideAnimation<? super File> glideAnimation) {
                                if(!config.equals(imageView.getTag(R.drawable.im_item_list_opt))){
                                    return;
                                }
                                pl.droidsonroids.gif.GifDrawable gifDrawable2 = null;
                                try {
                                    gifDrawable2 = new pl.droidsonroids.gif.GifDrawable(file);
                                    imageView.setScaleType(MyUtil.getScaleTypeForImageView(config.getScaleMode(),true,false));
                                    imageView.setImageDrawable(gifDrawable2);
                                    if(config.getImageListener() != null){
                                        config.getImageListener().onSuccess(gifDrawable2,null,
                                                gifDrawable2.getIntrinsicWidth(),gifDrawable2.getIntrinsicHeight());
                                    }
                                } catch (Throwable e) {
                                    if(config.getErrorResId() >0){
                                        imageView.setScaleType(MyUtil.getScaleTypeForImageView(config.getErrorScaleType(),false));
                                        imageView.setImageDrawable(imageView.getContext().getResources().getDrawable(config.getErrorResId()));
                                    }
                                    if(config.getImageListener() != null){
                                        config.getImageListener().onFail(e == null ? new Throwable("unexpected error") : e);
                                    }
                                    MyUtil.handleException(new ImageLoadFailException(config.getUsableString(),e));


                                }
                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                super.onLoadFailed(e, errorDrawable);

                                if(!config.equals(imageView.getTag(R.drawable.im_item_list_opt))){
                                    return;
                                }
                                if(config.getErrorResId() >0){
                                    imageView.setScaleType(MyUtil.getScaleTypeForImageView(config.getErrorScaleType(),false));
                                    imageView.setImageDrawable(imageView.getContext().getResources().getDrawable(config.getErrorResId()));
                                }
                                if(config.getImageListener() != null){
                                    config.getImageListener().onFail(e == null ? new Throwable("unexpected error") : e);
                                }
                                MyUtil.handleException(new ImageLoadFailException(config.getUrl(),e));
                            }
                        });
                return;
            }

            config.loadStartTime = System.currentTimeMillis();
            builder.listener(MyUtil.getProxy(new RequestListener() {
                @Override
                public boolean onException(Exception e, Object model, Target target, boolean isFirstResource) {
                    if (!isSame(config, imageView, model, target)) {
                        return false;
                    }
                    String path = "";
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
                    MyUtil.handleException(new ImageLoadFailException(model.toString(),e));
                    return false;
                }

                @Override
                public boolean onResourceReady(Object resource, final Object model, Target target,
                                               boolean isFromMemoryCache, boolean isFirstResource) {
                    if(!isSame(config,imageView,model,target)){
                        return false;
                    }

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

                    if(resource instanceof GlideBitmapDrawable){
                        GlideBitmapDrawable drawable = (GlideBitmapDrawable) resource;
                        Bitmap bitmap = drawable.getBitmap();
                        if(GlobalConfig.debug){
                            Log.d("onResourceReady",MyUtil.printBitmap(bitmap));
                        }
                    }else if(resource instanceof GifDrawable){
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
                        Log.d("onResourceReady","isFromMemoryCache :"+isFromMemoryCache);
                        Log.d("onResourceReady","isFirstResource :"+isFirstResource);
                    }

                    if(config.getImageListener() != null ) {
                        if(resource instanceof GlideBitmapDrawable){
                            GlideBitmapDrawable drawable = (GlideBitmapDrawable) resource;
                            Bitmap bitmap = drawable.getBitmap();
                            config.getImageListener().onSuccess(drawable,bitmap,bitmap.getWidth(),bitmap.getHeight());
                        }else if(resource instanceof GifDrawable){
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
                                            imageView.setImageDrawable(gifDrawable);
                                            MyUtil.handleException(new ImageLoadFailException(config.getUsableString(),e));
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
            })).into(imageView);
            // builder.into(viewTarget);
        }
    }

    private boolean isSame(SingleConfig config, ImageView imageView, Object model, Target target) {
        return config.equals(imageView.getTag(R.drawable.im_item_list_opt));
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
                    if(event.getX() > MyUtil.dip2px(40) || event.getY() > MyUtil.dip2px(40)){
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
        if(drawable instanceof GlideBitmapDrawable){
            GlideBitmapDrawable glideBitmapDrawable = (GlideBitmapDrawable) drawable;
            Bitmap bitmap = glideBitmapDrawable.getBitmap();
            desc += MyUtil.printBitmap(bitmap)+"\n";
            if (MyUtil.isBitmapTooLarge(bitmap.getWidth(),bitmap.getHeight(), v)) {
                textView.setTextColor(Color.RED);
            }
        }else if(drawable instanceof BitmapDrawable){
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            desc += MyUtil.printBitmap(bitmap)+"\n";
            if (MyUtil.isBitmapTooLarge(bitmap.getWidth(),bitmap.getHeight(), v)) {
                textView.setTextColor(Color.RED);
            }
        }else if (drawable instanceof SquaringDrawable){
            SquaringDrawable bitmap = (SquaringDrawable) drawable;
            desc += "\nSquaringDrawable, w:"+bitmap.getIntrinsicWidth() +",h:"+bitmap.getIntrinsicHeight()+"\n";
            if (MyUtil.isBitmapTooLarge(bitmap.getIntrinsicWidth(),bitmap.getIntrinsicHeight(), v)) {
                textView.setTextColor(Color.RED);
            }
        }else if(drawable instanceof GifDrawable){
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

        textView.setText(desc);

        getFileFromDiskCache(config.getUsableString(), new FileGetter() {
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
    private DrawableTypeRequest getDrawableTypeRequest(SingleConfig config, RequestManager requestManager) {
        DrawableTypeRequest request = null;
        if(!TextUtils.isEmpty(config.getUrl())){
            request= requestManager.load(MyUtil.appendUrl(config.getUrl()));
        }else if(!TextUtils.isEmpty(config.getFilePath())){
            request= requestManager.load(config.getFilePath());
        }else if(!TextUtils.isEmpty(config.getContentProvider())){
            request= requestManager.loadFromMediaStore(Uri.parse(config.getContentProvider()));
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
       /* if(!TextUtils.isEmpty(config.getUrl()) && config.getUrl().contains(".gif")){
            request.diskCacheStrategy(DiskCacheStrategy.ALL);//只缓存result
        }else{
            request.diskCacheStrategy(DiskCacheStrategy.SOURCE);//只缓存原图
        }*/

        request.diskCacheStrategy(DiskCacheStrategy.SOURCE);//只缓存原图

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
            transformations.add(new CenterCrop(config.getContext()));
        }else{
            transformations.add(new FitCenter(config.getContext()));
        }


        if(config.isNeedBlur()){
            transformations.add(new BlurTransformation(config.getContext(), config.getBlurRadius()));
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

                if(config.getBorderWidth() > 0 && config.getBorderColor() != 0){
                    transformations.add(new BorderRoundTransformation(config.getContext(),
                            config.getRectRoundRadius(), 0,config.getBorderWidth(),
                            config.getContext().getResources().getColor(config.getBorderColor()),0x0b1100));
                }else {
                    transformations.add(new RoundedCornersTransformation(config.getContext(),
                            config.getRectRoundRadius(),config.getBorderWidth(), cornerType));
                }

                break;
            case ShapeMode.OVAL:
                if(config.getBorderWidth() > 0 && config.getBorderColor() != 0){
                    transformations.add( new CropCircleWithBorderTransformation(config.getContext(),
                            config.getBorderWidth(),config.getContext().getResources().getColor(config.getBorderColor())));
                }else {
                    transformations.add( new CropCircleTransformation(config.getContext()));
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
        Glide.clear(view);
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
    public void getFileFromDiskCache(final String url,  FileGetter getter) {
        getter = MyUtil.getProxy(getter);
        File file = new File(url);
        if(file.exists() && file.isFile()){
            int[] wh = MyUtil.getImageWidthHeight(url);
            getter.onSuccess(file,wh[0],wh[1]);
            return;
        }
        final FileGetter finalGetter = getter;
        MyUtil.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                Glide.with(ImageLoader.context)
                        .load(url)
                        .downloadOnly(new SimpleTarget<File>() {
                            @Override
                            public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                                Log.d("onResourceReady","thread :"+Thread.currentThread().getName() +",downloadOnly");
                                if(resource.exists() && resource.isFile() ){//&& resource.length() > 70
                                    int[] wh = MyUtil.getImageWidthHeight(resource.getAbsolutePath());
                                    finalGetter.onSuccess(resource,wh[0],wh[1]);
                                }else {
                                    finalGetter.onFail(new Throwable("resource not exist"));
                                }
                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                finalGetter.onFail(e);
                            }
                        });
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
        Glide.with(GlobalConfig.context).onTrimMemory(level);
    }

    @Override
    public void onLowMemory() {
        Glide.with(GlobalConfig.context).onLowMemory();
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
        public void updateDiskCacheKey(MessageDigest messageDigest) throws UnsupportedEncodingException {
            messageDigest.update(id.getBytes(STRING_CHARSET_NAME));
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
                } catch (UnsupportedEncodingException e) {
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
