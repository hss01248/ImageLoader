package com.hss01248.image.bigimage2;

import android.view.ViewGroup;


import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import am.util.viewpager.adapter.RecyclePagerAdapter;

/**
 * Created by Administrator on 2017/4/30.
 */

public class MyRecyclePagerAdapter2 extends RecyclePagerAdapter<MyPagerViewHolder2> {

    //private int itemCount = 5;
    List<String> urls;
    private Map<String, File> cachedFiles;

    public MyRecyclePagerAdapter2(List<String> urls) {
        this.urls = urls;
        cachedFiles = new HashMap<>();
    }


    @Override
    public int getItemCount() {
        return urls.size();
    }

    @Override
    public MyPagerViewHolder2 onCreateViewHolder(ViewGroup parent, int viewType) {
        //一般同viewType的Holder创建不会超过四个
        MyLargeImageHolder holder = new MyLargeImageHolder(parent.getContext(), parent);
        MyPagerViewHolder2 holder2 = new MyPagerViewHolder2(holder.root);
        holder2.imageViewHolder = holder;
        return holder2;
    }

    @Override
    public void onBindViewHolder(MyPagerViewHolder2 holder, int position) {
        //处理不同页面的不同数据
        holder.setData(urls.get(position));
    }

    public void changeDatas(List<String> urls) {
        this.urls.clear();
        this.urls.addAll(urls);
        notifyDataSetChanged();
    }

    @Override
    public void onViewRecycled(MyPagerViewHolder2 holder) {
        super.onViewRecycled(holder);
    }
}
