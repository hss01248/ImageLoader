package com.hss01248.bigimageviewpager;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.List;

public class LargeImageViewer {




    public static ViewPager showBig(final Context context,MyViewPager viewPager, final List<String> uris, int position) {
        if(viewPager == null){
            viewPager = new MyViewPager(context);
        }
        // ImageLoader.loadBigImages(viewPager, urls);
        PagerAdapter pagerAdapter = new PagerAdapter() {
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
                MyLargeImageView imageView = new MyLargeImageView(context);
                imageView.loadUri(uris.get(position));
                container.addView(imageView);
                return imageView;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((View) object);
            }
        };

        viewPager.setAdapter(pagerAdapter);
        pagerAdapter.notifyDataSetChanged();

        /*viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                String text = (position + 1) + " / " + urls.size() + "\n";
                textView.setText(text);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileInfo(viewPager, urls, context);
            }
        });*/
        viewPager.setCurrentItem(position);
        return viewPager;
    }
}
