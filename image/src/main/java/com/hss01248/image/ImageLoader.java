package com.hss01248.image;

import android.content.Context;

import com.hss01248.image.config.SingleConfig;
import com.hss01248.image.fresco.FrescoUtil;


/**
 * Created by Administrator on 2017/3/14 0014.
 */

public class ImageLoader {

    public static Context context;







    public static void init(final Context context, int cacheSizeInM){
        ImageLoader.context = context;

        FrescoUtil.init(context,cacheSizeInM);
    }

    public static SingleConfig with(Context context){
        return new SingleConfig(context);

    }




}
