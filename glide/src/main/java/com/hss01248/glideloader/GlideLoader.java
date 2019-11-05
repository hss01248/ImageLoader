package com.hss01248.glideloader;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SquaringDrawable;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.util.LruCache;
import com.bumptech.glide.util.Util;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.view.BigImageView;
import com.hss01248.glideloader.big.GlideBigLoader;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.MyUtil;
import com.hss01248.image.config.GlobalConfig;
import com.hss01248.image.config.ShapeMode;
import com.hss01248.image.config.SingleConfig;
import com.hss01248.glideloader.drawable.AutoRotateDrawable;
import com.hss01248.image.interfaces.FileGetter;
import com.hss01248.image.interfaces.ILoader;
import com.hss01248.image.utils.ThreadPoolFactory;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
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

public class GlideLoader implements ILoader {
    @Override
    public void init(Context context, int cacheSizeInM) {//glide默认最大容量250MB的文件缓存

        /*Glide.get(context)
                .setMemoryCategory(MemoryCategory.NORMAL);*/
        BigImageViewer.initialize(GlideBigLoader.with(context,MyUtil.getClient(GlobalConfig.ignoreCertificateVerify)));
        GlobalConfig.cacheFolderName = DiskCache.Factory.DEFAULT_DISK_CACHE_DIR;
        GlobalConfig.cacheMaxSize =  DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE/1024/1024;

    }

    @Override
    public void request(final SingleConfig config) {
        if(config.isAsBitmap()){
            SimpleTarget target = null;
            if(config.getWidth()>0 && config.getHeight()>0){
                target = new SimpleTarget<Bitmap>(config.getWidth(),config.getHeight()) {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                        // do something with the bitmap
                        // for demonstration purposes, let's just set it to an ImageView
                        // BitmapPool mBitmapPool = Glide.get(BigLoader.context).getBitmapPool();
                        //bitmap = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight())
                        if(config.isNeedBlur()){
                            bitmap = blur(bitmap,config.getBlurRadius(),false);
                            Glide.get(GlobalConfig.context).getBitmapPool().put(bitmap);
                        }
                        if(config.getShapeMode() == ShapeMode.OVAL){
                            bitmap = MyUtil.cropCirle(bitmap,false);
                            Glide.get(GlobalConfig.context).getBitmapPool().put(bitmap);
                        }else if(config.getShapeMode() == ShapeMode.RECT_ROUND){
                            bitmap = MyUtil.rectRound(bitmap,config.getRectRoundRadius(),0);
                            Glide.get(GlobalConfig.context).getBitmapPool().put(bitmap);
                        }

                        config.getBitmapListener().onSuccess(bitmap);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        if(e !=null){
                            e.printStackTrace();
                        }
                        config.getBitmapListener().onFail(e);
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
                        if(config.isNeedBlur()){
                            bitmap = blur(bitmap,config.getBlurRadius(),false);
                            Glide.get(GlobalConfig.context).getBitmapPool().put(bitmap);
                        }
                        if(config.getShapeMode() == ShapeMode.OVAL){
                            bitmap = MyUtil.cropCirle(bitmap,false);
                            Glide.get(GlobalConfig.context).getBitmapPool().put(bitmap);
                        }else if(config.getShapeMode() == ShapeMode.RECT_ROUND){
                            bitmap = MyUtil.rectRound(bitmap,config.getRectRoundRadius(),0);
                            Glide.get(GlobalConfig.context).getBitmapPool().put(bitmap);
                        }

                        config.getBitmapListener().onSuccess(bitmap);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        if(e !=null){
                            e.printStackTrace();
                        }
                        config.getBitmapListener().onFail(e);
                    }
                };
            }

            RequestManager requestManager =  Glide.with(config.getContext());
            DrawableTypeRequest request = getDrawableTypeRequest(config, requestManager);
            if(config.getWidth()>0 && config.getHeight()>0){
                request.override(config.getWidth(),config.getHeight());
            }

           // setShapeModeAndBlur(config, request);
            request.asBitmap().approximate().into(target);

        }else {

            if(config.getTarget() instanceof BigImageView){
                MyUtil.viewBigImage(config);
                return;
            }

            final RequestManager requestManager =  Glide.with(config.getContext());
             final DrawableTypeRequest request = getDrawableTypeRequest(config, requestManager);

            if(request ==null){
                return;
            }
            DrawableRequestBuilder builder = request.thumbnail(1.0f);
            if(config.getLoadingResId() != 0){
                Drawable drawable = new AutoRotateDrawable(config.getContext().getResources().getDrawable(config.getLoadingResId()), 1500);
                builder.placeholder(drawable);
            }else if(MyUtil.shouldSetPlaceHolder(config)){
                builder.placeholder(config.getPlaceHolderResId());
            }


            if(config.getWidth()>0 && config.getHeight()>0){
                builder.override(config.getWidth(),config.getHeight());
            }
          builder =   setShapeModeAndBlur(config, builder);
            if(config.getErrorResId() >0){
                builder.error(config.getErrorResId());
            }
            if(config.getTarget() instanceof ImageView){
                final ImageView imageView = (ImageView) config.getTarget();

                builder.dontAnimate();

                builder.listener(new RequestListener() {
                    @Override
                    public boolean onException(Exception e, Object model, Target target, boolean isFirstResource) {
                        Log.d("onException","thread :"+Thread.currentThread().getName() +",onException");
                        Log.d("onException","model :"+model);
                        Log.d("onException","Target :"+target);
                        Log.d("onException","isFirstResource :"+isFirstResource);
                        if(e != null){
                            e.printStackTrace();
                        }


                        if(config.getImageListener() != null){
                            config.getImageListener().onFail(e == null ? new Throwable("unexpected error") : e);
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Object resource, Object model, Target target, boolean isFromMemoryCache, boolean isFirstResource) {


                        Log.d("onResourceReady","thread :"+Thread.currentThread().getName() +",onResourceReady");
                        Log.d("onResourceReady","resource :"+resource);
                        if(resource instanceof GlideBitmapDrawable){
                            GlideBitmapDrawable drawable = (GlideBitmapDrawable) resource;
                            Bitmap bitmap = drawable.getBitmap();
                            Log.i("onResourceReady","drawable :"+drawable.getIntrinsicWidth()+"x"+drawable.getIntrinsicHeight()+"," +
                                    "bitmap:"+bitmap.getWidth()+"x"+bitmap.getHeight());
                        }else if(resource instanceof GifDrawable){
                            GifDrawable gifDrawable = (GifDrawable) resource;
                            //Grow heap (frag case) to 74.284MB for 8294412-byte allocation
                            Log.w("onResourceReady","gif :"+gifDrawable.getIntrinsicWidth()+"x"+gifDrawable.getIntrinsicHeight()+"x"+gifDrawable.getFrameCount());
                        }
                        Log.d("onResourceReady","model :"+model);
                        Log.d("onResourceReady","Target :"+target);
                        Log.d("onResourceReady","isFromMemoryCache :"+isFromMemoryCache);
                        Log.d("onResourceReady","isFirstResource :"+isFirstResource);
                        //imageView.setScaleType(MyUtil.getScaleTypeForImageView(config.getScaleMode(),true));
                       /* if(config.getImageListener() != null && !TextUtils.isEmpty(config.getUrl())) {
                            getFileFromDiskCache(config.getUrl(), new FileGetter() {
                                @Override
                                public void onSuccess(File file, int width, int height) {
                                    config.getImageListener().onSuccess(file.getAbsolutePath(), width, height, null, 0, 0);
                                }

                                @Override
                                public void onFail(Throwable e) {

                                }
                            });
                        }*/
                        return false;
                    }
                }).into(imageView);
               // builder.into(viewTarget);
            }
        }
    }

    @Override
    public void debug(final SingleConfig config) {
        if(config.getTarget() instanceof ImageView) {
             ImageView imageView = (ImageView) config.getTarget();
            /* imageView.setOnLongClickListener(new View.OnLongClickListener() {
                 @Override
                 public boolean onLongClick(View v) {
                     showPop((ImageView)v,config);
                     return true;
                 }
             });*/
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPop((ImageView)v,config);
                }
            });

        }
    }

    private void showPop(ImageView v, final SingleConfig config) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext());
        final TextView textView = new TextView(v.getContext());
        String desc = config.getUrl()+"\n";
        desc += v.getScaleType().name()+"\n";
        Drawable drawable = v.getDrawable();
        if(drawable instanceof GlideBitmapDrawable){
            GlideBitmapDrawable glideBitmapDrawable = (GlideBitmapDrawable) drawable;
            desc += "bitmap, w:"+glideBitmapDrawable.getIntrinsicWidth() +",h:"+glideBitmapDrawable.getIntrinsicHeight();
            Bitmap bitmap = glideBitmapDrawable.getBitmap();
            if(bitmap != null){
                desc += "\nconfig:"+bitmap.getConfig().name()+",size:"+MyUtil.formatFileSize(bitmap.getByteCount());
            }else {

            }
        }else if(drawable instanceof BitmapDrawable){
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if(bitmap != null){
                desc += "bitmap, w:"+bitmap.getWidth() +",h:"+bitmap.getHeight();
                desc += "\nconfig:"+bitmap.getConfig().name()+",size:"+MyUtil.formatFileSize(bitmap.getByteCount());
            }


        }else if (drawable instanceof SquaringDrawable){
            SquaringDrawable bitmap = (SquaringDrawable) drawable;
            desc += "SquaringDrawable, w:"+bitmap.getIntrinsicWidth() +",h:"+bitmap.getIntrinsicHeight();
        }else {
            desc += "drawable:"+drawable;
        }

        desc += "\nimageview:"+v.getMeasuredWidth() +" x " + v.getMeasuredHeight();

        textView.setText(desc);

        textView.setPadding(20,20,20,20);
        dialog.setView(textView);
        dialog.setPositiveButton("拷贝链接", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MyUtil.copyText(config.getUrl());
                Toast.makeText(textView.getContext(),"已拷贝链接",Toast.LENGTH_SHORT).show();
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
        request.diskCacheStrategy(DiskCacheStrategy.SOURCE);//只缓存原图
        return request;
    }

    private DrawableRequestBuilder setShapeModeAndBlur(SingleConfig config, DrawableRequestBuilder builder) {
        int shapeMode = config.getShapeMode();
        List<Transformation> transformations = new ArrayList<>();

        if(config.isCropFace()){
            // transformations.add(new FaceCenterCrop());//脸部识别
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
                /*if(config.getScaleMode() == ScaleMode.CENTER_CROP){
                    transformations.add(new CenterCrop(config.getContext()));
                }*/
                RoundedCornersTransformation.CornerType cornerType = RoundedCornersTransformation.CornerType.ALL;
                if(shapeMode == ShapeMode.RECT_ROUND_ONLY_TOP){
                    cornerType = RoundedCornersTransformation.CornerType.TOP;
                }
                transformations.add(new RoundedCornersTransformation(config.getContext(),
                        config.getRectRoundRadius(), 0, cornerType));

                if(config.getBorderWidth()>0){

                }
                if(config.isGif() && config.getRoundOverlayColor()>0){

                }
                break;
            case ShapeMode.OVAL:
                transformations.add( new CropCircleTransformation(config.getContext()));
                if(config.getBorderWidth()>0){

                }
                if(config.isGif() && config.getRoundOverlayColor()>0){

                }
                break;
            default:break;
        }

        if(!transformations.isEmpty()){
            Transformation[] forms = new Transformation[transformations.size()];
            for (int i = 0; i < transformations.size(); i++) {
                forms[i] = transformations.get(i);
            }
           return builder.bitmapTransform(forms);
        }
        return builder;

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
    public void getFileFromDiskCache(final String url, final FileGetter getter) {
        MyUtil.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                Glide.with(ImageLoader.context)
                        .load(url)
                        .downloadOnly(new SimpleTarget<File>() {
                            @Override
                            public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                                if(resource.exists() && resource.isFile() ){//&& resource.length() > 70
                                    int[] wh = MyUtil.getImageWidthHeight(resource.getAbsolutePath());
                                    getter.onSuccess(resource,wh[0],wh[1]);
                                }else {
                                    getter.onFail(new Throwable("resource not exist"));
                                }
                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                getter.onFail(e);
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


          /* OriginalKey originalKey = new OriginalKey(url, EmptySignature.obtain());
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
           }*/
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
