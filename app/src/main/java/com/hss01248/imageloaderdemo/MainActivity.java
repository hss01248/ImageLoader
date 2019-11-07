package com.hss01248.imageloaderdemo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.executor.FifoPriorityThreadPoolExecutor;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.animation.GlideAnimationFactory;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.interfaces.FileGetter;
import com.hss01248.image.memory.ImageMemoryHookManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import pl.droidsonroids.gif.GifDrawable;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.iv_url)
    ImageView ivUrl;
    @BindView(R.id.iv_file)
    ImageView ivFile;
    @BindView(R.id.iv_res)
    ImageView ivRes;
    @BindView(R.id.iv_url_blur)
    ImageView ivUrlBlur;

    @BindView(R.id.btn_bigpic)
    Button btnBigpic;
    @BindView(R.id.btn_bigpic_viewpager)
    Button btnBigpicViewpager;
    @BindView(R.id.activity_main)
    ScrollView activityMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
       // GlideFaceDetector.initialize(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                show();

                Debug.startMethodTracing("sp-read");
                SharedPreferences sharedPreferences = getSharedPreferences("ky", Context.MODE_PRIVATE);
                sharedPreferences.getString("shss","ddddd");
                Debug.stopMethodTracing();
            }
        }, 5000);

        /*Debug.startMethodTracing("sp-write");
        SharedPreferences sharedPreferences = getSharedPreferences("ky", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("shss","1233444hhhhh").commit();
        //sharedPreferences.getString("shss","ddddd");
        Debug.stopMethodTracing();*/

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //GlideFaceDetector.releaseDetector();
    }

    private void show() {

        List<String> datas = new ArrayList<>();

        ImageLoader.with(this)
                .url("https://pic1.zhimg.com/v2-7868c606d6ddddbdd56f0872e514925c_b.jpg")
                // .url("http://img.yxbao.com/news/image/201703/13/7bda462477.gif")
                // .res(R.drawable.thegif)
                .placeHolder(R.mipmap.ic_launcher,false)
                .widthHeight(250, 150)
                .asCircle(R.color.colorAccent);
               //.blur(40)
                /*.asBitmap(new SingleConfig.BitmapListener() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {

                        ivUrl.setImageBitmap(bitmap);
                        Log.e("ee","ivUrl  bitmap.config:" + bitmap.getConfig());
                        *//*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            ivUrl.setBackground(new BitmapDrawable(bitmap));
                        }*//*
                    }

                    @Override
                    public void onFail(Throwable e) {

                    }
                });*/
               // .into(ivUrl);


        ImageLoader.with(this)
                .placeHolder(R.mipmap.ic_launcher,false)
                .res(R.drawable.thegif)
                //.url("https://pic1.zhimg.com/v2-7868c606d6ddddbdd56f0872e514925c_b.jpg")
                .widthHeight(100, 80)
                .rectRoundCorner(5, R.color.colorPrimary);
               // .blur(5)
                /*.asBitmap(new SingleConfig.BitmapListener() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        Log.e("bitmap", bitmap.getWidth() + "---height:" + bitmap.getHeight() + "--" + bitmap.toString());

                        ivRes.setImageBitmap(bitmap);
                        Log.e("dd","ivRes  bitmap.config:" + bitmap.getConfig());
                    }

                    @Override
                    public void onFail(Throwable e) {
                        Log.e("bitmap", "fail");

                    }
                });*/
        //.into(ivRes);

        ImageLoader.with(this)
            .widthHeight(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)
            .url("https://pic1.zhimg.com/v2-7868c606d6ddddbdd56f0872e514925c_b.jpg")
            .rectRoundCorner(5,R.color.bg_white);
            //.into(ivFile);

        ImageLoader.with(this)
                .cropFace()
                .widthHeight(100,100)
                .asCircle(R.color.colorAccent)
                .blur(10)
                .url("http://img3.ynet.com/2018/03/22/071135542b5deabc409e36af01290c89_600x-_90.jpg");
                //.into(ivUrlBlur);


        Glide.with(MainActivity.this)
                .load("http://img3.ynet.com/2018/03/22/071135542b5deabc409e36af01290c89_600x-_90.jpg")
                //.load("https://c-ssl.duitang.com/uploads/blog/201407/04/20140704234425_j5zHS.thumb.700_0.gif")
               // .asGif()
                //.asBitmap()
                .bitmapTransform(new CenterCrop(this),new RoundedCornersTransformation(this,30,0))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .skipMemoryCache(true)
                .into(ivUrl);
               /* .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {

                        ImageLoader.getActualLoader().getFileFromDiskCache(model, new FileGetter() {
                            @Override
                            public void onSuccess(File file, int width, int height) {
                                GifDrawable gifDrawable = null;
                                try {
                                    gifDrawable = new GifDrawable(file);
                                    ivUrl.setImageDrawable(gifDrawable);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFail(Throwable e) {

                            }
                        });
                        return true;
                    }
                });*/
               /* .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {



                        ImageLoader.getActualLoader().getFileFromDiskCache(model, new FileGetter() {
                            @Override
                            public void onSuccess(File file, int width, int height) {
                                GifDrawable gifDrawable = null;
                                try {
                                    gifDrawable = new GifDrawable(file);
                                    ivFile.setImageDrawable(gifDrawable);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFail(Throwable e) {

                            }
                        });
                        return true;
                    }
                })*/
               // .into(ivUrl);

        /*Glide.with(this).load("https://img.zcool.cn/community/01f1bc58413d49a8012060c80de125.gif")
                .downloadOnly(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                        GifDrawable gifDrawable = null;
                        try {
                            gifDrawable = new GifDrawable(resource);
                            ivFile.setImageDrawable(gifDrawable);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }
                });*/


    }

    @OnClick({R.id.btn_bigpic, R.id.btn_bigpic_viewpager,R.id.btn_recycle,R.id.btn_fresco,R.id.btn_scale})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_bigpic:{
                /*Intent intent = new Intent(this,BigImageActy.class);
                startActivity(intent);*/
                FrescoFaceCropActivity.launch(this);
            }

                break;
            case R.id.btn_bigpic_viewpager:{
                Intent intent = new Intent(this,ViewpagerActy.class);
                startActivity(intent);
            }
                break;
            case R.id.btn_recycle:{
                Intent intent = new Intent(this,RecycleViewActy.class);
                startActivity(intent);
            }
            break;
            case R.id.btn_fresco:
                Intent intent = new Intent(this,ConfigAllActy.class);
                startActivity(intent);
                break;
            case R.id.btn_scale:
                Intent intent6 = new Intent(this,ScaleTypeActy.class);
                startActivity(intent6);

                break;
            default:break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //ImageMemoryHookManager.show(this);
    }

    /*Intent intent = new Intent(this,BigImageActy.class);
        startActivity(intent);*/

}
