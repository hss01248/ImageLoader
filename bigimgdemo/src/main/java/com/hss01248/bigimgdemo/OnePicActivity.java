package com.hss01248.bigimgdemo;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hss01248.bigimageviewpager.LargeImageViewer;
import com.hss01248.bigimageviewpager.MyLargeImageView;

public class OnePicActivity extends AppCompatActivity {

    String gifUrl;
    String jpgUrl;
    String jpgUrlPortrait;
    MyLargeImageView imageView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         gifUrl =  "https://oss-kodo.hss01248.tech/testimg/1600862676552-54801d6475b29239e55e90cf9666e388c3feef09464efcbea9a8593ff4f27321.gif";

         jpgUrl = "https://oss-kodo.hss01248.tech/testimg/1601361105382-一亿像素-麦田.jpg";

        jpgUrlPortrait =  "https://oss-kodo.hss01248.tech/testimg/long-img.jpg";
        setContentView(R.layout.activity_onepic);
         imageView = findViewById(R.id.iv_large);

        //imageView.loadUri(new Random().nextBoolean());
    }

    public void gif(View view) {
        imageView.loadUri(gifUrl);
        LargeImageViewer.showOne(gifUrl);
    }

    public void jpg(View view) {
        //imageView.loadUri(jpgUrl);
        LargeImageViewer.showOne(jpgUrl);
    }

    public void jpgPortrait(View view) {
        LargeImageViewer.showOne(jpgUrlPortrait);
    }
}
