package com.hss01248.image.bigimage;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.github.piasy.biv.view.BigImageView;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.config.GlobalConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/24 0024.
 */

public class PagerAdapterForBigImage extends PagerAdapter {
    List<String> urls;
    List<BigImageView> mViews ;
    private static final int CACHE_SIZE = 4;

    public PagerAdapterForBigImage(List<String> urls){
        this.urls = urls;
        mViews = new ArrayList<BigImageView>(CACHE_SIZE);
    }

    public void changeDatas(List<String> urls){
       if(urls!=null){
           this.urls.clear();
           this.urls.addAll(urls);
           notifyDataSetChanged();
       }
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
        int i = position % CACHE_SIZE;
        Log.e("instantiateItem","postion:"+position+"---i:"+i);
        if(mViews.size()<=i){
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
        if(imageView.getParent()!=null){//多加一层判断,比较保险
            ViewGroup viewGroup = (ViewGroup) imageView.getParent();
            viewGroup.removeView(imageView);
        }
        container.addView(imageView);
        return imageView;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        int i = position % CACHE_SIZE;
        Log.e("destroyItem","postion------------------:"+position+"---i:"+i);
       // BigImageView imageView = mViews.get(i);
        GlobalConfig.getLoader().clearMomoryCache((View) object);
        container.removeView((View) object);


    }


}
