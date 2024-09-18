package com.hss01248.imageloaderdemo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.multidex.MultiDexApplication;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.Utils;
import com.elvishew.xlog.XLog;
import com.hss.downloader.DownloadList;
import com.hss.downloader.ILargeImagesViewer;
import com.hss01248.basewebview.BaseWebviewActivity;
import com.hss01248.basewebview.WebConfigger;
import com.hss01248.basewebview.WebviewInit;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dokit.IDokitConfig;
import com.hss01248.dokit.MyDokit;
import com.hss01248.flipper.DBAspect;
import com.hss01248.glidev4.Glide4Loader;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.config.GlobalConfig;
import com.hss01248.image.config.SingleConfig;
import com.hss01248.image.interfaces.LoadInterceptor;
import com.hss01248.imagelist.album.IViewInit;
import com.hss01248.imagelist.album.ImageListView;
import com.hss01248.imagelist.album.ImageMediaCenterUtil;
import com.hss01248.webviewspider.IShowUrls;
import com.hss01248.webviewspider.SpiderWebviewActivity;
import com.liulishuo.filedownloader.FileDownloader;

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
        FileDownloader.setupOnApplicationOnCreate(this);
        /*StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());*/
        Utils.init(this);
        ImageLoader.init(getApplicationContext(), 500, new Glide4Loader());
        GlobalConfig.debug = true;

        //Glance.INSTANCE.initialize(new MyDBContext(this));

        MyDokit.setConfig(new IDokitConfig() {
            @Override
            public void loadUrl(Context context, String url) {
                BaseWebviewActivity.start(ActivityUtils.getTopActivity(),url);
            }

            @Override
            public void report(Object o) {
                if(o instanceof Throwable){
                    //XReporter.reportException((Throwable) o);
                }
            }
        });
        WebConfigger.init(new WebviewInit() {
            @Override
            public Class html5ActivityClass() {
                return BaseWebviewActivity.class;
            }

        });


        DBAspect.addDB(getFile("imgdownload.db"));
        //XReporter.init(this,"7ac352d904",true);
        //4f7a08bf-1fa1-453f-870d-da59f0131c02
        //UmengUtil.init(this,"6163f5bbac9567566e91bb94","bugly",1,"", BuildConfig.DEBUG);
        //NotifyUtil.init(this);
        DownloadList.setLargeImagesViewer(new ILargeImagesViewer() {
            @Override
            public void showBig(Context context, List<String> uris0, int position) {
                ImageMediaCenterUtil.showBigImag(context, uris0, position);
            }

            @Override
            public void viewDir(Context context, String dir, String file) {
                ImageMediaCenterUtil.showViewAsActivity(context, new IViewInit() {
                    @Override
                    public View init(Activity activity) {
                        ImageListView listView = new ImageListView(activity);
                        listView.showImagesInDir(dir);
                        return listView;
                    }
                });
            }
        });
        SpiderWebviewActivity.setShowUrls(new IShowUrls() {
            @Override
            public void showUrls(Context context, String pageTitle, List<String> urls, @Nullable String downloadDir, boolean hideDir,boolean downloadImmediately) {

                ImageMediaCenterUtil.showViewAsActivityOrDialog(context, false, new IViewInit() {
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



                ImageMediaCenterUtil.showViewAsActivityOrDialog(context, false, new IViewInit() {
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



                ImageMediaCenterUtil.showViewAsActivityOrDialog(context, false, new IViewInit() {
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
