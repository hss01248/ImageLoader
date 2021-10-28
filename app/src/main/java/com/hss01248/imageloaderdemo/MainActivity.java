package com.hss01248.imageloaderdemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PermissionUtils;

import com.bumptech.glide.Glide;

import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.elvishew.xlog.XLog;
import com.facebook.drawee.view.SimpleDraweeView;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hss.downloader.MyDownloader;

import com.hss01248.image.ImageLoader;
import com.hss01248.image.dataforphotoselet.ImgDataSeletor;
import com.hss01248.imagelist.album.IViewInit;
import com.hss01248.imagelist.album.ImageListView;
import com.hss01248.imagelist.album.ImageMediaCenterUtil;
import com.hss01248.img.compressor.ImageCompressor;
import com.hss01248.ui.pop.list.PopList;
import com.hss01248.webviewspider.SpiderWebviewActivity;

import org.devio.takephoto.wrap.TakeOnePhotoListener;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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

    @BindView(R.id.btn_album)
    Button btnAlbum;
    @BindView(R.id.btn_dir)
    Button btnDir;

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
                sharedPreferences.getString("shss", "ddddd");
                Debug.stopMethodTracing();
            }
        }, 5000);

        /*Debug.startMethodTracing("sp-write");
        SharedPreferences sharedPreferences = getSharedPreferences("ky", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("shss","1233444hhhhh").commit();
        //sharedPreferences.getString("shss","ddddd");
        Debug.stopMethodTracing();*/

        XXPermissions.with(this)
                .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        Toast.makeText(MainActivity.this, all+"-These permissions are : "+ Arrays.toString(permissions.toArray()), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void show() {

        List<String> datas = new ArrayList<>();

        ImageLoader.with(this)
                .url("https://pic1.zhimg.com/v2-7868c606d6ddddbdd56f0872e514925c_b.jpg")
                // .url("http://img.yxbao.com/news/image/201703/13/7bda462477.gif")
                // .res(R.drawable.thegif)
                .placeHolder(R.mipmap.ic_launcher, false)
                .widthHeight(250, 150);
                //.asCircle(R.color.colorAccent).into(ivUrl);
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
                .placeHolder(R.mipmap.ic_launcher, false)
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
                .widthHeight(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                .url("https://pic1.zhimg.com/v2-7868c606d6ddddbdd56f0872e514925c_b.jpg")
                .rectRoundCorner(5, R.color.bg_white);
        //.into(ivFile);

        ImageLoader.with(this)
                .cropFace()
                .widthHeight(100, 100)
                .asCircle(R.color.colorAccent)
                .blur(10)
                .url("http://img3.ynet.com/2018/03/22/071135542b5deabc409e36af01290c89_600x-_90.jpg");
        //.into(ivUrlBlur);


    }

    @OnClick({R.id.btn_bigpic, R.id.btn_bigpic_viewpager, R.id.btn_recycle, R.id.btn_fresco, R.id.btn_scale
            , R.id.btn_album, R.id.btn_dir})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_bigpic: {
                /*Intent intent = new Intent(this,BigImageActy.class);
                startActivity(intent);*/
                FrescoFaceCropActivity.launch(this);
            }

            break;
            case R.id.btn_album: {
                /*oid.providers.media.MediaProvider uri content://media/external/images/media from
                pid=31500, uid=10576 requires android.permission.READ_EXTERNAL_STORAGE, or grantUriPermission()*/


                PermissionUtils.permission(Manifest.permission.WRITE_EXTERNAL_STORAGE).callback(new PermissionUtils.SingleCallback() {
                    @Override
                    public void callback(boolean isAllGranted, @NonNull List<String> granted, @NonNull List<String> deniedForever, @NonNull List<String> denied) {

                        ImageMediaCenterUtil.showViewAsActivity(MainActivity.this, new IViewInit() {
                            @Override
                            public View init(Activity activity) {
                                ImageListView view1 = new ImageListView(activity);
                                view1.showAllAlbums();
                                return view1;
                            }
                        });

                    }
                }).request();


            }

            break;
            case R.id.btn_dir: {


                ImageMediaCenterUtil.showViewAsActivity(view.getContext(), new IViewInit() {
                    @Override
                    public View init(Activity activity) {
                        ImageListView view1 = new ImageListView(activity);
                       // ImageMediaCenterUtil.showViewAsDialog(view1);
                        view1.showImagesInDir(Environment.getExternalStorageDirectory().getAbsolutePath());
                        return view1;
                    }
                });
            }

            break;
            case R.id.btn_bigpic_viewpager: {
                Intent intent = new Intent(this, ViewpagerActy.class);
                startActivity(intent);
            }
            break;
            case R.id.btn_recycle: {
                Intent intent = new Intent(this, RecycleViewActy.class);
                startActivity(intent);
            }
            break;
            case R.id.btn_fresco:
                Intent intent = new Intent(this, ConfigAllActy.class);
                startActivity(intent);
                break;
            case R.id.btn_scale:
                Intent intent6 = new Intent(this, ScaleTypeActy.class);
                startActivity(intent6);

                break;
            default:
                break;
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        //ImageMemoryHookManager.show(this);



    }



    public void loadUrl(View view) {
        ImageLoader.with(this)
                .load("https://pic1.zhimg.com/v2-7868c606d6ddddbdd56f0872e514925c_b.jpg")
                // .url("http://img.yxbao.com/news/image/201703/13/7bda462477.gif")
                // .res(R.drawable.thegif)
                //.placeHolder(R.mipmap.ic_launcher, false)
                .loadingDefault()
                //.widthHeight(250, 150)
                //.asCircle(R.color.colorAccent)
                .into(ivUrl);
    }

    public void loadFile(View view) {
        ImgDataSeletor.startPickOneWitchDialog(this, new TakeOnePhotoListener() {
            @Override
            public void onSuccess(String path) {

                //TurboCompressor.compressOriginal(path,70);
                File file = ImageCompressor.compressToAvif(path,false,false);
               /* ImageLoader.with(MainActivity.this)
                        .file(file.getAbsolutePath())
                        // .url("http://img.yxbao.com/news/image/201703/13/7bda462477.gif")
                        // .res(R.drawable.thegif)
                        // .placeHolder(R.mipmap.ic_launcher, false)
                        //.widthHeight(250, 150)
                        //.asCircle(R.color.colorAccent)
                        .into(ivUrl);*/
               /* Glide.with(MainActivity.this)
                        .load(file)
                        .into(ivUrl);*/

                LogUtils.i("avif compress result: "+file.getAbsolutePath());
                Glide.with(MainActivity.this)
                        .asBitmap()
                        .load(file)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                LogUtils.d("bitmap size:"+resource.getWidth()+"x"+resource.getHeight());
                                ivUrl.setImageBitmap(resource);
                            }
                        });
                ivUrl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        List<String> paths = new ArrayList<>();
                        paths.add(path);
                        paths.add(file.getAbsolutePath());
                        ImageMediaCenterUtil.showBigImag(MainActivity.this,paths,0);
                    }
                });
            }

            @Override
            public void onFail(String path, String msg) {

            }

            @Override
            public void onCancel() {

            }
        });
    }

    public void goWebSpider(View view) {
        List<String> menus = SpiderWebviewActivity.getSpiders();
        menus.add("浏览下载列表");
        menus.add("修复升级前的数据");
        menus.add("继续下载未完成的图片");

        PopList.showPop(this, -1, view, menus, new PopList.OnItemClickListener() {
            @Override
            public void onClick(int position, String str) {
                if(position == menus.size()-1){
                    MyDownloader.continueDownload();
                    //ImgDownloader.downladUrlsInDB(MainActivity.this,new File(SpiderWebviewActivity.getSaveDir("继续下载","")));
                }else if(position == 1) {
                    MyDownloader.showDownloadPage();
                }else if(position == 2) {
                    MyDownloader.fixDbWhenUpdate();
                }else {
                    SpiderWebviewActivity.start(MainActivity.this,str);
                }

            }
        });


    }

    public void copyDB(View view) {
       /*File db =  getDatabasePath("imgdownload.db");
       File file2 = new File(Environment.getExternalStorageDirectory(),"/.yuv/databases/imgdownload.db");
        FileUtils.copy(db,file2);
        File db2 =  getDatabasePath("imgdownload.db-journal");
        File file3 = new File(Environment.getExternalStorageDirectory(),"/.yuv/databases/imgdownload.db-journal");
        FileUtils.copy(db2,file3);



        if(file2.exists() && file2.length() > 10){
            ToastUtils.showShort("copy db success!!");
        }else {
            ToastUtils.showShort("copy db fail!!");
        }*/
    }

    /*Intent intent = new Intent(this,BigImageActy.class);
        startActivity(intent);*/

}
