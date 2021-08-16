package com.hss01248.bigimageviewpager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.shizhefei.view.largeimage.factory.InputStreamBitmapDecoderFactory;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLDecoder;
import java.util.Locale;

import me.jessyan.progressmanager.ProgressListener;
import me.jessyan.progressmanager.ProgressManager;
import me.jessyan.progressmanager.body.ProgressInfo;

public class UrlLoader {

    public interface LoadListener{
        void onLoad(String path);

        void onProgress(int progress);

        void onFail(Throwable throwable);
    }

    static Handler handler = new Handler(Looper.getMainLooper());

    public static void download(Context context, ImageView ivHelper,String url,LoadListener listener){

        ProgressManager.getInstance().addResponseListener(url, new ProgressListener() {
            @Override
            public void onProgress(ProgressInfo progressInfo) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            listener.onProgress(progressInfo.getPercent());
                            //tvProgress.setText(progressInfo.getPercent()+"% , speed: "+(progressInfo.getSpeed()/1024/8)+"KB/s");
                        }catch (Throwable throwable){
                            throwable.printStackTrace();
                        }

                    }
                });

            }

            @Override
            public void onError(long id, Exception e) {
               if(e != null){
                   e.printStackTrace();
               }

            }
        });






        Glide.with(context)
                .load(url)
                .priority(Priority.IMMEDIATE)
                .listener(new RequestListener< Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        android.util.Log.d("GLIDE", String.format(Locale.ROOT,
                                "onException(%s, %s, %s, %s)", e, model, target, isFirstResource), e);

                        Glide.with(context)
                                .load(url)
                                // .priority(Priority.HIGH)
                                .downloadOnly(new SimpleTarget<File>() {
                                    @Override
                                    public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                                        listener.onLoad(resource.getAbsolutePath());
                                    }

                                    @Override
                                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                        super.onLoadFailed(errorDrawable);
                                        listener.onFail(e);

                                    }
                                });
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        android.util.Log.v("GLIDE", String.format(Locale.ROOT,
                                "onResourceReady(%s, %s, %s, %s)", resource, model, target, isFirstResource));
                        Glide.with(context)
                                .load(url)
                                // .priority(Priority.HIGH)
                                .downloadOnly(new SimpleTarget<File>() {
                                    @Override
                                    public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                                        listener.onLoad(resource.getAbsolutePath());
                                    }

                                    @Override
                                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                        super.onLoadFailed(errorDrawable);

                                    }
                                });
                        return false;
                    }
                })
                .into(ivHelper);

    }
}
