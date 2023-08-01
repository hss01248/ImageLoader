package com.hss01248.image;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.hss01248.image.config.GlobalConfig;
import com.hss01248.image.config.SingleConfig;
import com.hss01248.image.interfaces.ILoader;

import java.io.File;
import java.util.List;
import java.util.Stack;


/**
 * Created by Administrator on 2017/3/14 0014.
 */

public class ImageLoader {

    public static Context context;

    static Handler getHandler() {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        return handler;
    }

    static Handler handler;

    public static void setConfig(ILoaderConfig config) {
        ImageLoader.config = config;
    }

    public static ILoaderConfig config;

    public interface ILoaderConfig {
        Stack<Activity> getActivityStack();
    }


    public static void init(final Context context, int cacheSizeInM, ILoader imageLoader) {
        ImageLoader.context = context;
        GlobalConfig.init(context, cacheSizeInM, imageLoader);
        handler = new Handler(Looper.getMainLooper());
        imageLoader.init(context, 150);
    }

    public static ILoader getActualLoader() {
        return GlobalConfig.getLoader();
    }

    /**
     * 加载普通图片
     *
     * @param context
     * @return
     */
    public static SingleConfig.ConfigBuilder with(Context context) {
        return new SingleConfig.ConfigBuilder(context);
    }

    /**
     * 加载大图.暂时不支持缩略图
     *
     * @param imageView
     * @param path      支持content,filepath,网络的url.如果是网络图片,请拼接上http协议名和主机
     */
    public static void loadBigImage(View imageView, String path) {//,String thumbnail
        if (path.startsWith("content:")) {
            new SingleConfig.ConfigBuilder(context).content(path).into(imageView);
        } else if (path.startsWith("http")) {
            new SingleConfig.ConfigBuilder(context).url(path).into(imageView);
        } else {
            new SingleConfig.ConfigBuilder(context).file(path).into(imageView);
        }
    }

    /**
     * 自带ui
     *
     * @param desc
     * @param path
     */
    public static void previewBigImgInDialog(String desc, String path) {

    }


    /**
     * 加载多张大图.支持动态更新urls
     *
     * @param viewPager new出来的或者从xml中解析出来的
     * @param urls      图片路径
     */
    public static void loadBigImages(ViewPager viewPager, List<String> urls) {//,String thumbnail
        viewPager.setOffscreenPageLimit(1);
        //强制让左右缓存一个
        // ViewPager viewPager = new ViewPager(context);
       /* if( viewPager.getAdapter()==null  ){
            PagerAdapter adapter = new MyRecyclePagerAdapter2(urls);
            viewPager.setAdapter(adapter);
        }else if (viewPager.getAdapter() instanceof MyRecyclePagerAdapter2){
            MyRecyclePagerAdapter2 adapterForBigImage = (MyRecyclePagerAdapter2) viewPager.getAdapter();
            adapterForBigImage.changeDatas(urls);
        }else {
            throw new RuntimeException("用于加载大图的viewPager应该专用,其adapter不要自己设置");
        }*/


      /*  if (viewPager.getAdapter() == null) {
            PagerAdapter adapter = new MyViewPagerAdapter3(urls);
            viewPager.setAdapter(adapter);
        } else if (viewPager.getAdapter() instanceof MyViewPagerAdapter3) {
            MyViewPagerAdapter3 adapterForBigImage = (MyViewPagerAdapter3) viewPager.getAdapter();
            adapterForBigImage.changeDatas(urls);
        } else {
            throw new RuntimeException("用于加载大图的viewPager应该专用,其adapter不要自己设置");
        }*/
    }

    public static void loadBigImages(RecyclerView recyclerView, List<String> urls) {
       /* recyclerView.setAdapter(new RecycleAdapterForBigImage(urls));
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());*/
    }

    /**
     * 图片保存到相册
     *
     * @param url
     */
    private static void saveImageIntoGallery(String url) {
        File file = GlobalConfig.getLoader().getFileFromDiskCache(url);
        if (file != null && file.exists()) {
            //todo 拷贝文件到picture文件夹中
        }

    }

    /**
     * 预先下载图片(辅助功能)
     *
     * @param urls
     */
    public static void prefech(String... urls) {
        Uri[] uris = new Uri[urls.length];
        for (int i = 0; i < uris.length; i++) {
            uris[i] = Uri.parse(urls[i]);
        }
        //BigImageViewer.prefetch(uris);
    }

    public static void trimMemory(int level) {
        GlobalConfig.getLoader().trimMemory(level);
    }

    public static void clearAllMemoryCaches() {
        GlobalConfig.getLoader().onLowMemory();
    }


}
