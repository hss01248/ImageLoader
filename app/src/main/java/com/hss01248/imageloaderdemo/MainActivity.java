package com.hss01248.imageloaderdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import com.facebook.drawee.view.SimpleDraweeView;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.config.SingleConfig;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.iv_url)
    SimpleDraweeView ivUrl;
    @Bind(R.id.iv_file)
    SimpleDraweeView ivFile;
    @Bind(R.id.iv_res)
    SimpleDraweeView ivRes;
    @Bind(R.id.iv_url_blur)
    SimpleDraweeView ivUrlBlur;
    @Bind(R.id.btn_bigpic)
    Button btnBigpic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                show();
            }
        }, 1000);
    }

    private void show() {


        ImageLoader.with(this)
                .url("https://pic1.zhimg.com/v2-7868c606d6ddddbdd56f0872e514925c_b.jpg")
                // .url("http://img.yxbao.com/news/image/201703/13/7bda462477.gif")
                // .res(R.drawable.thegif)
                .placeHolder(R.mipmap.ic_launcher)
                .widthHeight(250, 150)
                .asCircle(R.color.colorPrimary)
                .into(ivUrl);


        ImageLoader.with(this)
                .placeHolder(R.mipmap.ic_launcher)
                .res(R.drawable.thegif)
                //.url("https://pic1.zhimg.com/v2-7868c606d6ddddbdd56f0872e514925c_b.jpg")
                .widthHeight(250, 150)
                .rectRoundCorner(15, R.color.colorPrimary)
                .asBitmap(new SingleConfig.BitmapListener() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        Log.e("bitmap", bitmap.getWidth() + "---height:" + bitmap.getHeight() + "--" + bitmap.toString());
                    }

                    @Override
                    public void onFail() {
                        Log.e("bitmap", "fail");

                    }
                });
        //.into(ivRes);


    }

    @OnClick(R.id.btn_bigpic)
    public void onClick() {
        Intent intent = new Intent(this,BigImageActy.class);
        startActivity(intent);
    }
}
