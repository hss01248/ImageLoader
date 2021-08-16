package com.hss01248.bigimgdemo;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hss01248.bigimageviewpager.MyLargeImageView;

import java.util.Random;

public class OnePicActivity extends AppCompatActivity {

    String gifUrl;
    String jpgUrl;
    MyLargeImageView imageView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         gifUrl =  "https://i.pinimg.com/originals/99/a0/11/99a01123f4f5ec82d359289b5dee2e8a.gif";

         jpgUrl = "http://img6.16fan.com/attachments/wenzhang/201805/18/152660818127263ge.jpeg";
        setContentView(R.layout.activity_onepic);
         imageView = findViewById(R.id.iv_large);

        //imageView.loadUri(new Random().nextBoolean());
    }

    public void gif(View view) {
        imageView.loadUri(gifUrl);
    }

    public void jpg(View view) {
        imageView.loadUri(jpgUrl);
    }
}
