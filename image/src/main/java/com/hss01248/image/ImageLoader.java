package com.hss01248.image;

import android.content.Context;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.view.BigImageView;
import com.hss01248.image.bigimage.PagerAdapterForBigImage;
import com.hss01248.image.config.GlobalConfig;
import com.hss01248.image.config.SingleConfig;

import java.io.File;
import java.util.List;


/**
 * Created by Administrator on 2017/3/14 0014.
 */

public class ImageLoader {

    public static Context context;

    /**
     * 初始化
     * @param context
     * @param cacheSizeInM 缓存文件夹最大多少
     */
    public static void init(final Context context, int cacheSizeInM){
        ImageLoader.context = context;
        GlobalConfig.context = context;
        GlobalConfig.getLoader().init(context,cacheSizeInM);
    }

    /**
     * 加载普通图片
     * @param context
     * @return
     */
    public static SingleConfig.ConfigBuilder with(Context context){
        return new SingleConfig.ConfigBuilder(context);
    }

    /**
     * 加载大图.暂时不支持缩略图
     * @param imageView
     * @param url
     */
    public static void loadBigImage(BigImageView imageView,String url){//,String thumbnail
        new SingleConfig.ConfigBuilder(context).url(url).into(imageView);
    }

    /**
     *   加载多张大图.支持动态更新urls
     * @param viewPager new出来的或者从xml中解析出来的
     * @param urls 图片路径
     */
    public static void loadBigImages(ViewPager viewPager, List<String> urls){//,String thumbnail
       // ViewPager viewPager = new ViewPager(context);
        if( viewPager.getAdapter()==null  ){
            PagerAdapter adapter = new PagerAdapterForBigImage(urls);
            viewPager.setAdapter(adapter);
        }else if (viewPager.getAdapter() instanceof PagerAdapterForBigImage){
            PagerAdapterForBigImage adapterForBigImage = (PagerAdapterForBigImage) viewPager.getAdapter();
            adapterForBigImage.changeDatas(urls);
        }else {
            throw new RuntimeException("用于加载大图的viewPager应该专用,其adapter不要自己设置");
        }

    }

    /**
     * 图片保存到相册
     * @param url
     */
    private static void saveImageIntoGallery(String url){
      File file=  GlobalConfig.getLoader().getFileFromDiskCache(url);
        if(file!=null && file.exists()){
            //todo 拷贝文件到picture文件夹中
        }

    }

    /**
     * 预先下载图片(辅助功能)
     * @param urls
     */
    public static void prefech(String... urls){
        Uri[] uris = new Uri[urls.length];
        for (int i = 0; i < uris.length; i++) {
            uris[i] = Uri.parse(urls[i]);
        }
        BigImageViewer.prefetch(uris);
    }

    public static void trimMemory(int level){
        GlobalConfig.getLoader().trimMemory(level);
    }

    public static void  clearAllMemoryCaches(){
        GlobalConfig.getLoader().clearAllMemoryCaches();
    }


}
