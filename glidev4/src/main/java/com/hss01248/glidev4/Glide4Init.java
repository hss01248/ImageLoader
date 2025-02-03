package com.hss01248.glidev4;

import android.app.Application;
import android.content.Context;

import androidx.startup.Initializer;

import com.blankj.utilcode.util.LogUtils;
import com.hss01248.image.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * @Despciption todo
 * @Author hss
 * @Date 09/05/2022 14:41
 * @Version 1.0
 */
public class Glide4Init implements Initializer<String> {
    @Override
    public String create(Context context) {
        LogUtils.d("init:"+this);
        if(context instanceof Application){
            Application application = (Application) context;

        }
        ImageLoader.init(context,1024,new Glide4Loader());
        return "Glide4Init";
    }


    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return new ArrayList<>();
    }
}
