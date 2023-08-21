package com.hss01248.bigimageviewpager;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.hss01248.fullscreendialog.FullScreenDialog;

import java.util.ArrayList;
import java.util.List;

public class LargeImageViewer {



    public static void showInDialog(String path){
        LogUtils.d("path to load: "+ path);
        FullScreenDialog dialog = new FullScreenDialog(ActivityUtils.getTopActivity());
        MyLargeImageViewBySubSamplingView largeImageView = new MyLargeImageViewBySubSamplingView(ActivityUtils.getTopActivity());
        dialog.setContentView(largeImageView);
        dialog.show();
        largeImageView.loadUri(path);
    }



    public static ViewPager showBig(final Context context,
                                    @Nullable  MyViewPager viewPager,
                                    final List<String> uris0,
                                    int position) {
        if(viewPager == null){
            viewPager = new MyViewPager(context);
        }

        List<String> uris = new ArrayList<>(uris0.size());
        uris.addAll(uris0);
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
                String url = uris.get(position);
                url = getBigImageUrl(url);
                imageView.loadUri(url);
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

    public static String getBigImageUrl(String url) {
        if(TextUtils.isEmpty(url)){
            return "";
        }
        if(!url.contains("?")){
            return url;
        }
        String param = url.substring(url.indexOf("?")+1);
        if(param.contains("&w=") || param.contains("width=") || param.contains("&h=") || param.contains("height=")){
            return url.substring(0,url.indexOf("?"));
        }
        return url;
    }
}
