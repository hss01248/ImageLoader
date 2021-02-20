package com.hss01248.image.bigimage2;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class MyViewPagerAdapter3 extends PagerAdapter {

    List<String> sources = new ArrayList<>();

    public MyViewPagerAdapter3(List<String> sources) {
        this.sources.addAll(sources);
    }

    @Override
    public int getCount() {
        return sources.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        MyLargeImageHolder holder = new MyLargeImageHolder(container.getContext(),container);
        container.addView(holder.root);
        holder.loadImage(sources.get(position));
        return holder.root;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        //super.destroyItem(container, position, object);
        container.removeView((View) object);
    }

    public void changeDatas(List<String> urls) {
        sources.clear();
        sources.addAll(urls);
        notifyDataSetChanged();
    }
}
