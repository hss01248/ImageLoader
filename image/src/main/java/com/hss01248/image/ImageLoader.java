package com.hss01248.image;

import android.content.Context;

import com.hss01248.image.config.GlobalConfig;
import com.hss01248.image.config.SingleConfig;




/**
 * Created by Administrator on 2017/3/14 0014.
 */

public class ImageLoader {

    //public static Context context;

    public static void init(final Context context, int cacheSizeInM){
        //ImageLoader.context = context;

        GlobalConfig.context = context;
        GlobalConfig.getLoader().init(context,cacheSizeInM);


    }

    public static SingleConfig.ConfigBuilder with(Context context){
        return new SingleConfig.ConfigBuilder(context);
    }


}
