package com.hss01248.bigimageviewpager;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.LifecycleOwner;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.hss.utils.enhance.viewholder.mvvm.BaseViewHolder;
import com.hss01248.bigimageviewpager.databinding.RlPagerImagsBinding;

import java.util.ArrayList;
import java.util.List;

public class MyLargeViewPagerHolder extends BaseViewHolder<RlPagerImagsBinding, Pair<Integer, List<String>>> {
    public MyLargeViewPagerHolder(Context context) {
        super(context);
    }

    @Override
    protected void initDataAndEventInternal(LifecycleOwner lifecycleOwner, Pair<Integer, List<String>> pair) {
        List<String> uris = new ArrayList<>(pair.second.size());
        uris.addAll(pair.second);

        MyViewPager viewPager = binding.myViewPager;

        PagerAdapter pagerAdapter = new PagerAdapter() {
            List<View> cacheList = new ArrayList<>();

            @Override
            public int getCount() {
                return uris.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                MyLargeImageView imageView = null;
                if(cacheList.isEmpty()){
                    imageView = new MyLargeImageView(context);
                }else {
                    imageView = (MyLargeImageView) cacheList.remove(0);
                }
                String url = uris.get(position);
                url = LargeImageViewer.getBigImageUrl(url);
                imageView.loadUri(url);
                container.addView(imageView);
                return imageView;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((View) object);
                cacheList.add((View)object);
            }
        };
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                showInfo(position, uris);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });



        viewPager.setAdapter(pagerAdapter);
        pagerAdapter.notifyDataSetChanged();

        viewPager.setCurrentItem(pair.first);

        showInfo(pair.first,uris);


    }

    private void showInfo(int position, List<String> uris) {
        String text = position +"/"+ uris.size();
        containerViewHolderWithTitleBar.getBinding().realTitleBar.setRightTitle(text);
        containerViewHolderWithTitleBar.getBinding().realTitleBar.setRightTitleColor(Color.WHITE);
        String path = uris.get(position);
        String name = path.substring(path.lastIndexOf("/")+1);
        if(name.contains("?")){
            name = name.substring(0,name.indexOf("?"));
        }
        containerViewHolderWithTitleBar.getBinding().realTitleBar.setTitle(name);
    }
}
