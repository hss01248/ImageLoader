package com.hss01248.imageloaderdemo.download;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hss01248.downloader_m3u8.M3u8Downloader;
import com.hss01248.imageloaderdemo.R;

/**
 * @Despciption todo
 * @Author hss
 * @Date 23/11/2023 17:49
 * @Version 1.0
 */
public class DownloadDemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_demo);
    }

    public void m3u8(View view) {
        // DownloadUtil.startDownload("游戏测试视频4","http://videoconverter.vivo.com.cn/201706/655_1498479540118.mp4.main.m3u8");
        String url = "http://videoconverter.vivo.com.cn/201706/655_1498479540118.mp4.main.m3u8";
        M3u8Downloader.start(url);
    }
}
