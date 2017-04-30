package com.hss01248.image.bigimage;

import android.view.ViewGroup;

import com.github.piasy.biv.view.BigImageView;
import com.hss01248.image.ImageLoader;

import java.io.File;
import java.util.Map;

import am.util.viewpager.adapter.RecyclePagerAdapter;

/**
 * Created by Administrator on 2017/4/30.
 */

public class MyPagerViewHolder extends RecyclePagerAdapter.PagerViewHolder {
    BigImageView imageView;
    public MyPagerViewHolder(ViewGroup parent) {
        super(new BigImageView(parent.getContext()));
        imageView = (BigImageView) itemView;
    }

    public MyPagerViewHolder init(Map<String,File> caches){
        imageView.setCachedFileMap(caches);
        return this;
    }

    public void setData(String url){
        ImageLoader.loadBigImage(imageView,url);
    }



}
