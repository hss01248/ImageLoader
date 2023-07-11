package com.hss01248.bigimageviewpager.pano;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.io.File;
import java.io.FileInputStream;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * @Despciption todo
 * @Author hss
 * @Date 20/02/2023 16:10
 * @Version 1.0
 */
public class MyPanoActivity extends AppCompatActivity {

    MyPanoView panoView;

    String path;
    public static void start(String path){
        Intent intent = new Intent(ActivityUtils.getTopActivity(),MyPanoActivity.class);
        intent.putExtra("path",path);
        ActivityUtils.getTopActivity().startActivity(intent);
    }



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         panoView = new MyPanoView(this);
        setContentView(panoView);
        path = getIntent().getStringExtra("path");
        loadImage();
        BarUtils.transparentStatusBar(this);
    }

    private void loadImage() {
        Disposable subscribe = Observable.just(path)
                .subscribeOn(Schedulers.io())
                .map(new Function<String, Bitmap>() {
                    @Override
                    public Bitmap apply(String uri) throws Exception {
                        Bitmap bitmap = null;
                        if (uri.startsWith("content://") || uri.startsWith("file://")) {
                            bitmap =  BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.parse(uri)));
                        }else {
                            bitmap =  BitmapFactory.decodeStream(new FileInputStream(new File(uri)));
                        }
                        //放大两倍,然保存为临时文件:
                        //Bitmap bitmap1 = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth()*0.5f), (int) (bitmap.getHeight()*0.5f),true);
                        //bitmap.recycle();
                        return bitmap;
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(Bitmap bitmap) throws Exception {

                        panoView.loadBitmap(bitmap);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        ToastUtils.showLong(throwable.getMessage());
                        LogUtils.w(throwable);
                    }
                });

    }
}
