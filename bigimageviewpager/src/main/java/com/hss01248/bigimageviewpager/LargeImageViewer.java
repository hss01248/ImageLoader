package com.hss01248.bigimageviewpager;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class LargeImageViewer {



    public static void showInDialog(String path){
        LogUtils.d("path to load: "+ path);
        Dialog dialog = new Dialog(ActivityUtils.getTopActivity());

        MyLargeImageViewBySubSamplingView largeImageView = new MyLargeImageViewBySubSamplingView(ActivityUtils.getTopActivity());
        dialog.setContentView(largeImageView);
        setDialogToFullScreen(dialog);
        dialog.show();
        largeImageView.loadUri(path);
    }

    public static void setDialogToFullScreen(Dialog dialog){
        Window window = dialog.getWindow();
        //etStyle(STYLE_NORMAL, R.style.Dialog_FullScreen);
        window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        //window.requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setDimAmount(0f);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.WHITE);
        }

        //window.getDecorView().setSystemUiVisibility(Activi.getSystemUiVisibility());//获取视口全屏大小
        //window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //这个flag表示window负责绘制状态栏的背景当设置了这个flag,系统状态栏会变透明,同时这个相应的区域会被填满 getStatusBarColor() and getNavigationBarColor()的颜色

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //window.setStatusBarContrastEnforced(true);
        }
        WindowManager.LayoutParams attributes = window.getAttributes();
        if(attributes == null){
            attributes = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT);
        }else {
            attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
            attributes.height = WindowManager.LayoutParams.MATCH_PARENT;
        }
        window.setAttributes(attributes);

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
                url = getBigImageUrl(url);
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

        //todo 另外,最终activity destory时,手动回收view里的bitmap

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
