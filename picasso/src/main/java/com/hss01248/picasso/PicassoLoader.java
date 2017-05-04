package com.hss01248.picasso;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.view.BigImageView;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.MyUtil;
import com.hss01248.image.config.GlobalConfig;
import com.hss01248.image.config.ScaleMode;
import com.hss01248.image.config.ShapeMode;
import com.hss01248.image.config.SingleConfig;
import com.hss01248.image.interfaces.FileGetter;
import com.hss01248.image.interfaces.ILoader;
import com.hss01248.image.utils.ThreadPoolFactory;
import com.hss01248.picasso.big.PicassoBigLoader;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import jp.wasabeef.picasso.transformations.BlurTransformation;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;

import static com.squareup.picasso.MemoryPolicy.NO_CACHE;
import static com.squareup.picasso.MemoryPolicy.NO_STORE;

/**
 * Created by Administrator on 2017/5/3 0003.
 *
 * 内存优化: http://blog.csdn.net/ashqal/article/details/48005833
 */

public class PicassoLoader implements ILoader {

    private static final String TAG_PICASSO = "picasso";
    private List<String> paths = new ArrayList<>();
    private Picasso picasso;
    private OkHttpClient client;
    private static volatile int count;
    private static ConcurrentHashMap<String,File> fileCache = new ConcurrentHashMap<>();



    private Picasso getPicasso(){
        if(picasso ==null){
            client = MyUtil.getClient(GlobalConfig.ignoreCertificateVerify);
            picasso =  new Picasso.Builder(GlobalConfig.context)
                    .downloader(new OkHttp3Downloader(client))
                    .build();
        }
        return picasso;
    }

    @Override
    public void init(Context context, int cacheSizeInM) {//Picasso默认最大容量250MB的文件缓存
       // Picasso.get(context).setMemoryCategory(MemoryCategory.NORMAL);
        //BigImageViewer.initialize(PicassoImageLoader.with(context,MyUtil.getClient(GlobalConfig.ignoreCertificateVerify)));
        getPicasso();
        BigImageViewer.initialize(new PicassoBigLoader(client));

    }

    @Override
    public void request(final SingleConfig config) {


            if(config.getTarget() instanceof BigImageView){
                MyUtil.viewBigImage(config);
                return;
            }



            final RequestCreator request = getDrawableTypeRequest(config);
            request.tag(TAG_PICASSO).config(Bitmap.Config.RGB_565);

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
        Picasso picasso = getPicasso();
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

        if(config.isCropFace()){
             //transformations.add(new FaceCenterCrop(config.getWidth(), config.getHeight()));//脸部识别
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
        Picasso.with(GlobalConfig.context).pauseTag(TAG_PICASSO);

    }

    @Override
    public void resume() {
        Picasso.with(GlobalConfig.context).resumeTag(TAG_PICASSO);
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
       PicassoBigLoader.clearCache();
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
     *
     * https://github.com/square/picasso/issues/1025
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

    @Override
    public void getFileFromDiskCache(final String url, final FileGetter getter) {
        if(!url.startsWith("http")){
            return;
        }
        ThreadPoolFactory.getDownLoadPool().execute(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder().url(url).build();
                client.newCall(request).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        MyUtil.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                getter.onFail();
                            }
                        });


                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if(!response.isSuccessful()){
                            MyUtil.runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    getter.onFail();
                                }
                            });

                            return;
                        }
                        File dir = new File(GlobalConfig.context.getCacheDir(),"picassobig");
                        if(!dir.exists()){
                            dir.mkdirs();
                        }
                        final File file = new File(dir, count%30+"-tmp.jpg");
                        BufferedSource source = response.body().source();
                        Sink sink = Okio.sink(file);
                        source.readAll(sink);
                        source.close();
                        sink.close();
                        count++;

                        MyUtil.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                getter.onSuccess(file);
                            }
                        });
                    }
                });
            }
        });

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




}
