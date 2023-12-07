package com.hss01248.imageloaderdemo.download;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hss01248.downloader_m3u8.M3u8Downloader;
import com.hss01248.imageloaderdemo.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        String name = "动漫-浪客剑心第一集";
        String url = "https://vip.ffzy-online6.com/20231121/21873_abbe0da9/index.m3u8";
        // DownloadUtil.startDownload("游戏测试视频4","http://videoconverter.vivo.com.cn/201706/655_1498479540118.mp4.main.m3u8");
        //String url = "https://ydd.yqk88.com/m3u82/share/11939/615501/20231031/060132/360/index.m3u8?sign=1f95631fff0a0adcded151a7d0015607&t=1701083053";

        List<String> adPaths = new ArrayList<>();
        //video2/slice/hls/1697917950041/25
        adPaths.add("video2/slice/hls/");
        M3u8Downloader.start(adPaths,"动漫-浪客剑心",name,url,new HashMap<>());
    }
}
