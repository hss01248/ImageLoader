package com.hss01248.imageloaderdemo;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.github.piasy.biv.view.BigImageView;
import com.hss01248.image.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/24 0024.
 */

public class MyAdapter extends PagerAdapter {
    List<String> urls;
    List<BigImageView> mViews ;

    public MyAdapter(List<String> urls){
        this.urls = urls;
        mViews = new ArrayList<BigImageView>(4);
    }


    @Override
    public int getCount() {
        return urls.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {


        BigImageView imageView = null;
        int i = position % 4;
        if(mViews.size()==0 || mViews.size()<=i){
            imageView = new BigImageView(container.getContext());
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewPager.LayoutParams.MATCH_PARENT,ViewPager.LayoutParams.MATCH_PARENT);
            imageView.setLayoutParams(params);
            mViews.add(imageView);
        }else {
            imageView = mViews.get(i);
        }


         String url = urls.get(position);
        ImageLoader.loadBigImage(imageView,url);
        container.addView(imageView);
        return imageView;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //int i = position % 4;
       // BigImageView imageView = mViews.get(i);
        container.removeView((View) object);

    }
}
