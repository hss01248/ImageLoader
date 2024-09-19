package com.hss01248.motion_photos_android;

import android.app.Application;
import android.content.Context;

import androidx.startup.Initializer;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.hss01248.motion_photos.MotionPhotoUtil;
import com.hss01248.videocompress.VideoCompressUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @Despciption todo
 * @Author hss
 * @Date 09/05/2022 14:41
 * @Version 1.0
 */
public class MotionPhotoInit implements Initializer<String> {
    @Override
    public String create(Context context) {
        LogUtils.d("init:"+this);
        if(context instanceof Application){
            Application application = (Application) context;

        }
        MotionPhotoUtil.setMotion(new AndroidMotionImpl());
        VideoCompressUtil.init(Utils.getApp(), AppUtils.isAppDebug(),AppUtils.isAppDebug());
        //VideoCompressUtil.setCompressor(new FFmpegCompressImpl());
        return "MotionPhotoInit";
    }


    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return new ArrayList<>();
    }
}
