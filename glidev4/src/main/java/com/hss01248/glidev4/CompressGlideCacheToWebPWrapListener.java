package com.hss01248.glidev4;

import android.graphics.Bitmap;
import android.net.Uri;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.Utils;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.hss01248.image.config.SingleConfig;
import com.hss01248.media.metadata.MetaDataUtil;
import com.hss01248.media.metadata.MetaInfo;

import java.io.File;

/**
 * @Despciption todo
 * @Author hss
 * @Date 19/12/2022 17:41
 * @Version 1.0
 */
public class CompressGlideCacheToWebPWrapListener implements SingleConfig.BitmapListener {

    public CompressGlideCacheToWebPWrapListener(SingleConfig config) {
        this.config = config;
    }

    SingleConfig config;

    @Override
    public void onSuccess(Bitmap bitmap) {
        if(config.getBitmapListener() != null){
            config.getBitmapListener().onSuccess(bitmap);
        }
        ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<Boolean>() {
            @Override
            public Boolean doInBackground() throws Throwable {

                return compress(config);
            }

            @Override
            public void onSuccess(Boolean result) {

            }
        });

    }

    private Boolean compress(SingleConfig config) {
        String sourceString = config.getSourceString();
        if(!sourceString.startsWith("http")){
            return false;
        }
        try {
            File file = Glide.with(Utils.getApp())
                    .downloadOnly()
                    .load(config.getSourceString())
                    .submit()//也可以设置宽高
                    .get();
            LogUtils.d("glide cache file: "+ file.getAbsolutePath());
            MetaInfo metaData2 = MetaDataUtil.getMetaData2(Uri.fromFile(file));
            LogUtils.json(new Gson().newBuilder().setPrettyPrinting().create().toJson(metaData2));

            //todo 压缩为webp
        } catch (Exception e) {
            LogUtils.w(e);
            return false;
        }
        return true;
    }

    @Override
    public void onFail(Throwable e) {
        if(config.getBitmapListener() != null){
            config.getBitmapListener().onFail(e);
        }
    }
}
