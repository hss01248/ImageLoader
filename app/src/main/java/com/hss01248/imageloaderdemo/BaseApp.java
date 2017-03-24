package com.hss01248.imageloaderdemo;

import android.app.Application;

import com.hss01248.image.ImageLoader;

/**
 * Created by Administrator on 2017/3/15 0015.
 */

public class BaseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ImageLoader.init(getApplicationContext(), 40);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        ImageLoader.trimMemory(level);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        ImageLoader.clearAllMemoryCaches();
    }
}
