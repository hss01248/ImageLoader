package com.hss01248.basewebviewdemo;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.hss01248.basewebview.BaseWebviewActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void geoLocation(View view) {
        BaseWebviewActivity.start(this,"https://www.runoob.com/try/try.php?filename=tryhtml5_geolocation");
    }

    public void showOpenFilePicker(View view) {
        BaseWebviewActivity.start(this,"https://www.zhangxinxu.com/study/202108/button-picker-file-upload.php");
    }

    public void jsInputFile(View view) {
        BaseWebviewActivity.start(this,"https://developer.mozilla.org/zh-CN/docs/Web/HTML/Element/Input/file");
    }

    public void jsInputFile2(View view) {
        BaseWebviewActivity.start(this,"https://test-ec-mall.xxx.com/jstest/input_test.html");
    }

    public void jsPop(View view) {
        BaseWebviewActivity.start(this,"https://www.runoob.com/js/js-popup.html");
    }

    public void playVideo(View view) {
        BaseWebviewActivity.start(this,"https://www.runoob.com/tags/tag-video.html");
    }

    public void windowHistory(View view) {
        BaseWebviewActivity.start(this,"https://www.runoob.com/js/js-window-history.html");
    }
    public void windowLocation(View view) {
        BaseWebviewActivity.start(this,"https://www.runoob.com/js/js-window-location.html");
    }

    public void windowOpen(View view) {
        //BaseWebviewActivity.start(this,"https://www.w3schools.com/jsref/met_win_close.asp");
        BaseWebviewActivity.start(this,"https://test-ec-mall.xxxx.com/jstest/index.html");
    }
}