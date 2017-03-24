package com.hss01248.image;

import android.content.Context;
import android.net.Uri;

import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.view.BigImageView;
import com.hss01248.image.config.GlobalConfig;
import com.hss01248.image.config.SingleConfig;

import java.io.File;


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
