package com.hss01248.image.bigimage;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.github.piasy.biv.view.BigImageView;
import com.hss01248.image.ImageLoader;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/3/24 0024.
 *
 * 逻辑有误,有bug
 */

public class PagerAdapterForBigImage extends PagerAdapter {
    List<String> urls;
    private LinkedList<BigImageView> mViewCache = null;
    private Map<String,File> cachedFiles;


    public PagerAdapterForBigImage(List<String> urls){
        this.urls = urls;
        mViewCache = new LinkedList<>();
        cachedFiles = new HashMap<>();
        /*for(int i =0;i< 4 ;i++){
            BigImageView  imageView = new BigImageView(context);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewPager.LayoutParams.MATCH_PARENT,ViewPager.LayoutParams.MATCH_PARENT);
            imageView.setLayoutParams(params);
            mViewCache.add(imageView);
        }*/
    }

    public void changeDatas(List<String> urls){
       if(urls!=null){
           this.urls.clear();
           this.urls.addAll(urls);
           notifyDataSetChanged();
       }
    }


    public void onPageSelected(int i) {

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

        Log.e("instantiateItem","postion:"+position);
        BigImageView imageView = null;


        if(mViewCache.size() < 4){
            imageView = new BigImageView(container.getContext());
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewPager.LayoutParams.MATCH_PARENT,ViewPager.LayoutParams.MATCH_PARENT);
            imageView.setLayoutParams(params);
            imageView.setCachedFileMap(cachedFiles);
            mViewCache.add(imageView);
        }else {
            imageView = mViewCache.removeFirst();
        }

        Log.e("instantiateItem",imageView.toString());

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

        Log.e("destroyItem","postion------------------:"+position);
       // BigImageView imageView = mViews.get(i);
        //GlobalConfig.getLoader().clearMomoryCache((View) object);
        BigImageView contentView = (BigImageView) object;
        container.removeView(contentView);
        this.mViewCache.addLast(contentView);


    }


}
