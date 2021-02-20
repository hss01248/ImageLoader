package com.hss01248.media.bigimage;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.List;

public class BigImageViewer {

    /**
     *   加载多张大图.支持动态更新urls
     * @param viewPager new出来的或者从xml中解析出来的
     * @param urls 图片路径
     */
    public static void loadBigImages(ViewPager viewPager, List<String> urls){//,String thumbnail
        viewPager.setOffscreenPageLimit(1);
        //强制让左右缓存一个
        // ViewPager viewPager = new ViewPager(context);
       /* if( viewPager.getAdapter()==null  ){
            PagerAdapter adapter = new MyRecyclePagerAdapter2(urls);
            viewPager.setAdapter(adapter);
        }else if (viewPager.getAdapter() instanceof MyRecyclePagerAdapter2){
            MyRecyclePagerAdapter2 adapterForBigImage = (MyRecyclePagerAdapter2) viewPager.getAdapter();
            adapterForBigImage.changeDatas(urls);
        }else {
            throw new RuntimeException("用于加载大图的viewPager应该专用,其adapter不要自己设置");
        }*/


        /*if( viewPager.getAdapter()==null  ){
            PagerAdapter adapter = new MyViewPagerAdapter3(urls);
            viewPager.setAdapter(adapter);
        }else if (viewPager.getAdapter() instanceof MyViewPagerAdapter3){
            MyViewPagerAdapter3 adapterForBigImage = (MyViewPagerAdapter3) viewPager.getAdapter();
            adapterForBigImage.changeDatas(urls);
        }else {
            throw new RuntimeException("用于加载大图的viewPager应该专用,其adapter不要自己设置");
        }*/
    }
}
