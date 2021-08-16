package com.hss01248.bigimgdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.hss01248.bigimageviewpager.MyLargeImageView;
import com.hss01248.imagelist.album.ImageListView;
import com.hss01248.imagelist.album.ImageMediaCenterUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},90);
        }

    }

    public void viewOne(View view) {
        startActivity(new Intent(this,OnePicActivity.class));
    }

    public void viewAlbum(View view) {
        ImageListView view1 = new ImageListView(MainActivity.this);
        ImageMediaCenterUtil.showViewAsDialog(view1);
        view1.showAllAlbums();

    }
}