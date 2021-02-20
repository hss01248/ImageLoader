package com.hss01248.image.bigimage2;

import android.view.ViewGroup;

import com.github.piasy.biv.view.BigImageView;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.bigimage.MyPagerViewHolder;

import java.io.File;
import java.util.Map;

import am.util.viewpager.adapter.RecyclePagerAdapter;

/**
 * Created by Administrator on 2017/4/30.
 */

public class MyPagerViewHolder2 extends RecyclePagerAdapter.PagerViewHolder {
  public   MyLargeImageHolder imageViewHolder;
    public MyPagerViewHolder2(ViewGroup parent) {
        super(parent);
    }

    public MyPagerViewHolder2 init(Map<String,File> caches){
       // imageView.setCachedFileMap(caches);
        return this;
    }

    public void setData(String url){
        imageViewHolder.loadImage(url);
    }



}
