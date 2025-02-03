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
    MyLargeImageView imageView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         gifUrl =  "/storage/emulated/0/Pictures/news_article/234b8108f0828c35da509917c62d4fb5.jpg";

         jpgUrl = "http://img6.16fan.com/attachments/wenzhang/201805/18/152660818127263ge.jpeg";
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
}
