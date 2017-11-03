package com.hss01248.imageloaderdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.hss01248.image.ImageLoader;
import com.hss01248.image.config.SingleConfig;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.iv_url)
    ImageView ivUrl;
    @Bind(R.id.iv_file)
    ImageView ivFile;
    @Bind(R.id.iv_res)
    ImageView ivRes;
    @Bind(R.id.iv_url_blur)
    ImageView ivUrlBlur;

    @Bind(R.id.btn_bigpic)
    Button btnBigpic;
    @Bind(R.id.btn_bigpic_viewpager)
    Button btnBigpicViewpager;
    @Bind(R.id.activity_main)
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
            }
        }, 1000);
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
                .asCircle(R.color.colorAccent)
               //.blur(40)
                .asBitmap(new SingleConfig.BitmapListener() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {

                        ivUrl.setImageBitmap(bitmap);
                        Log.e("ee","ivUrl  bitmap.config:" + bitmap.getConfig());
                        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            ivUrl.setBackground(new BitmapDrawable(bitmap));
                        }*/
                    }

                    @Override
                    public void onFail() {

                    }
                });
                //.into(ivUrl);


        ImageLoader.with(this)
                .placeHolder(R.mipmap.ic_launcher,false)
                //.res(R.drawable.thegif)
                .url("https://pic1.zhimg.com/v2-7868c606d6ddddbdd56f0872e514925c_b.jpg")
                .widthHeight(100, 80)
                .rectRoundCorner(5, R.color.colorPrimary)
               // .blur(5)
                .asBitmap(new SingleConfig.BitmapListener() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        Log.e("bitmap", bitmap.getWidth() + "---height:" + bitmap.getHeight() + "--" + bitmap.toString());

                        ivRes.setImageBitmap(bitmap);
                        Log.e("dd","ivRes  bitmap.config:" + bitmap.getConfig());
                    }

                    @Override
                    public void onFail() {
                        Log.e("bitmap", "fail");

                    }
                });
        //.into(ivRes);

        ImageLoader.with(this)
                .cropFace()
                .widthHeight(100,100)
                .asCircle(R.color.colorAccent)
                .url("https://github.com/aryarohit07/PicassoFaceDetectionTransformation/raw/master/images/original_image1.jpg?raw=true")
                .into(ivUrlBlur);


    }

    @OnClick({R.id.btn_bigpic, R.id.btn_bigpic_viewpager,R.id.btn_recycle,R.id.btn_fresco})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_bigpic:{
                Intent intent = new Intent(this,BigImageActy.class);
                startActivity(intent);
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
            case R.id.btn_fresco:
                Intent intent = new Intent(this,FrescoActy.class);
                startActivity(intent);
                break;
            default:break;
        }
    }


        /*Intent intent = new Intent(this,BigImageActy.class);
        startActivity(intent);*/

}
