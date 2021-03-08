package com.hss01248.image.bigimage;

import android.view.ViewGroup;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import am.util.viewpager.adapter.RecyclePagerAdapter;

/**
 * Created by Administrator on 2017/4/30.
 */

public class MyRecyclePagerAdapter extends RecyclePagerAdapter<MyPagerViewHolder> {

    //private int itemCount = 5;
    List<String> urls;
    private Map<String, File> cachedFiles;

    public MyRecyclePagerAdapter(List<String> urls) {
        this.urls = urls;
        cachedFiles = new HashMap<>();
    }


    @Override
    public int getItemCount() {
        return urls.size();
    }

    @Override
    public MyPagerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //一般同viewType的Holder创建不会超过四个
        return new MyPagerViewHolder(parent).init(cachedFiles);
    }

    @Override
    public void onBindViewHolder(MyPagerViewHolder holder, int position) {
        //处理不同页面的不同数据
        holder.setData(urls.get(position));
    }

    public void changeDatas(List<String> urls) {
        this.urls.clear();
        this.urls.addAll(urls);
        notifyDataSetChanged();
    }

    @Override
    public void onViewRecycled(MyPagerViewHolder holder) {
        super.onViewRecycled(holder);
    }
}
