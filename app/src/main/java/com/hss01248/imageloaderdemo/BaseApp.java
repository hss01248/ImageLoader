package com.hss01248.imageloaderdemo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.multidex.MultiDexApplication;

import com.elvishew.xlog.XLog;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.view.BigImageView;
import com.glance.guolindev.Glance;
import com.hjq.permissions.XXPermissions;
import com.hss.downloader.MyDownloader;
import com.hss01248.dialog.MyActyManager;
import com.hss01248.dialog.StyledDialog;

import com.hss01248.flipper.DBAspect;
import com.hss01248.glidev4.Glide4Loader;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.config.GlobalConfig;
import com.hss01248.image.config.SingleConfig;
import com.hss01248.image.interfaces.LoadInterceptor;

import com.hss01248.imagelist.album.IViewInit;
import com.hss01248.imagelist.album.ImageListView;
import com.hss01248.imagelist.album.ImageMediaCenterUtil;
import com.hss01248.notifyutil.NotifyUtil;
import com.hss01248.webviewspider.IShowUrls;
import com.hss01248.webviewspider.SpiderWebviewActivity;
import com.squareup.leakcanary.LeakCanary;

import java.io.File;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.MyToast;

/**
 * Created by Administrator on 2017/3/15 0015.
 */

public class BaseApp extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        ImageLoader.init(getApplicationContext(), 500, new Glide4Loader());
        GlobalConfig.debug = true;
        XXPermissions.setScopedStorage(true);
        //Glance.INSTANCE.initialize(new MyDBContext(this));

        DBAspect.addDB(getFile("imgdownload.db"));

        NotifyUtil.init(this);
        SpiderWebviewActivity.setShowUrls(new IShowUrls() {
            @Override
            public void showUrls(Context context, String pageTitle, List<String> urls, @Nullable String downloadDir, boolean hideDir,boolean downloadImmediately) {

                ImageMediaCenterUtil.showViewAsActivityOrDialog(context, true, new IViewInit() {
                    @Override
                    public View init(Activity activity) {
                        ImageListView listView = new ImageListView(context);
                        listView.showUrls(pageTitle,urls, downloadDir,hideDir,downloadImmediately);
                        return listView;
                    }
                });
            }

            @Override
            public void showUrls(Context context, String pageTitle, Map<String, List<String>> titlesToImags,
                                 List<String> urls, @Nullable String downloadDir, boolean hideDir,boolean downloadImmediately) {



                ImageMediaCenterUtil.showViewAsActivityOrDialog(context, true, new IViewInit() {
                    @Override
                    public View init(Activity activity) {
                        ImageListView listView = new ImageListView(context);
                        listView.showUrlsFromMap(pageTitle,titlesToImags,urls, downloadDir,hideDir,downloadImmediately);
                        return listView;
                    }
                });
            }

            @Override
            public void showFolder(Context context, String absolutePath) {



                ImageMediaCenterUtil.showViewAsActivityOrDialog(context, true, new IViewInit() {
                    @Override
                    public View init(Activity activity) {
                        ImageListView listView = new ImageListView(context);
                        listView.showImagesInDir(absolutePath);
                        return listView;
                    }
                });
            }
        });

       // ImageMemoryHookManager.hook(this);
        GlobalConfig.interceptor = new LoadInterceptor() {
            @Override
            public boolean intercept(SingleConfig config) {

                XLog.w(config.toString());
                if (config.getWidth() > 0 || config.getHeight() > 0) {
                    if (!TextUtils.isEmpty(config.getUrl())) {
                        if (config.getUrl().contains("w=") || config.getUrl().contains("h=")) {
                            return false;
                        }
                        String line = "";
                        if (config.getWidth() > 0) {
                            line += "w=" + config.getWidth();
                            if (config.getHeight() > 0) {
                                line += "&";
                            }
                        }
                        if (config.getHeight() > 0) {
                            line += "h=" + config.getHeight();
                        }

                        if (config.getUrl().contains("?")) {
                            config.setUrl(config.getUrl() + "&" + line);
                        } else {
                            config.setUrl(config.getUrl() + "?" + line);
                        }
                    }
                }
                XLog.w(config.toString());
                return false;
            }
        };
        // GlobalConfig.placeHolderResId = R.drawable.im_item_list_opt;
        // GlobalConfig.errorResId = R.drawable.im_item_list_opt_error;
        //BigImageViewer.initialize(GlideBigLoader.with(this));
        //GlobalConfig.setBigImageDark(false);
        LeakCanary.install(this);
        MyToast.init(this, true, true);

        // PhotoUtil.init(getApplicationContext(),new GlideIniter());//第二个参数根据具体依赖库而定
        StyledDialog.init(this);
        //Logger.initialize(new Settings());
        XLog.init();
        //ImageMemoryHookManager.hook(this);

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

    private File getFile(String name){
        String dbDir=android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        dbDir += "/.yuv/databases";//数据库所在目录
        String dbPath = dbDir+"/"+name;//数据库路径
        File file = new File(dbPath);
        return file;
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
