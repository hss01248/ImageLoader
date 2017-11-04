package com.hss01248.imageloaderdemo;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.elvishew.xlog.XLog;
import com.hss01248.dialog.MyActyManager;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.frescoloader.FrescoLoader;
import com.hss01248.image.ImageLoader;
import com.squareup.leakcanary.LeakCanary;

import es.dmoral.toasty.MyToast;

/**
 * Created by Administrator on 2017/3/15 0015.
 */

public class BaseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ImageLoader.init(getApplicationContext(), 100,new FrescoLoader());
        LeakCanary.install(this);
        MyToast.init(this,true,true);

       // PhotoUtil.init(getApplicationContext(),new GlideIniter());//第二个参数根据具体依赖库而定
        StyledDialog.init(this);
        //Logger.initialize(new Settings());
        XLog.init();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                MyActyManager.getInstance().setCurrentActivity(activity);

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
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
