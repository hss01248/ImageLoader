package com.hss01248.myblog;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.hss01248.basewebview.BaseWebviewActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BaseWebviewActivity.start(this,"https://github.com/hss01248/notebook3/tree/master/docs");
        finish();

    }
}