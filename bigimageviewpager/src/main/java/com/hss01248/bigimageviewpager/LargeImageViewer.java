package com.hss01248.bigimageviewpager;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.hss.utils.enhance.viewholder.ContainerActivity2;
import com.hss.utils.enhance.viewholder.mvvm.ContainerViewHolderWithTitleBar;
import com.hss01248.fullscreendialog.FullScreenDialogUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;

@Keep
public class LargeImageViewer {



    public static void showOne(String path){
        ContainerActivity2.start(new Consumer<Pair<ContainerActivity2, ContainerViewHolderWithTitleBar>>() {
            @Override
            public void accept(Pair<ContainerActivity2, ContainerViewHolderWithTitleBar> pair) throws Exception {

                MyLargeImageViewBySubSamplingView largeImageView = new MyLargeImageViewBySubSamplingView(pair.first);
                largeImageView.loadUri(path);
                pair.second.getBinding().llRoot.setBackgroundColor(Color.BLACK);
                pair.second.getBinding().rlContainer.addView(largeImageView);

                pair.second.setTitleBarTransplantAndRelative(true);
                pair.second.getBinding().realTitleBar.setLineDrawable(new ColorDrawable(Color.TRANSPARENT));

                String name = path.substring(path.lastIndexOf("/")+1);
                if(name.contains("?")){
                    name = name.substring(0,name.indexOf("?"));
                }
                pair.second.getBinding().realTitleBar.setTitle(name);
            }
        });
    }

    public static void showInBatch(List<String> paths,int position){

        ContainerActivity2.start(new Consumer<Pair<ContainerActivity2, ContainerViewHolderWithTitleBar>>() {
            @Override
            public void accept(Pair<ContainerActivity2, ContainerViewHolderWithTitleBar> pair) throws Exception {

                MyLargeViewPagerHolder holder = new MyLargeViewPagerHolder(pair.first);

                holder.setContainerViewHolderWithTitleBar(pair.second);

                pair.second.getBinding().rlContainer.addView(holder.getRootView());
                pair.second.setTitleBarTransplantAndRelative(true);
                pair.second.getBinding().realTitleBar.setLineDrawable(new ColorDrawable(Color.TRANSPARENT));
                holder.init(new Pair<>(position,paths));

                //View viewPager = showBig(pair.first,null,paths,position);
                pair.second.getBinding().llRoot.setBackgroundColor(Color.BLACK);
               /* UltimateBarX.navigationBar(pair.first).color(Color.BLACK)
                        .light(true)
                        .apply();*/
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    pair.first.getWindow().setNavigationBarColor(Color.BLACK);
                }

                //pair.second.getBinding().rlContainer.addView(viewPager);
            }
        });
    }

    @Deprecated
    public static void showInDialog(String path){
        LogUtils.d("path to load: "+ path);
        MyLargeImageViewBySubSamplingView largeImageView = new MyLargeImageViewBySubSamplingView(ActivityUtils.getTopActivity());
        FullScreenDialogUtil.showFullScreen(largeImageView);
        largeImageView.loadUri(path);
    }




    @Deprecated
    public static View showBig(final Context context,
                                    @Nullable  MyViewPager viewPager,
                                    final List<String> uris0,
                                    int position) {
        if(viewPager == null){
            viewPager = new MyViewPager(context);
        }

        RelativeLayout relativeLayout = new RelativeLayout(context);
        relativeLayout.addView(viewPager);
        TextView textView = new TextView(context);
        textView.setTextColor(Color.WHITE);
        int padding = SizeUtils.dp2px(20);
        textView.setPadding(padding,SizeUtils.dp2px(40),padding,padding);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams
                (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        relativeLayout.addView(textView,layoutParams);

        List<String> uris = new ArrayList<>(uris0.size());
        uris.addAll(uris0);

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
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                String text = position+"/"+uris0.size();
                textView.setText(text);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });



        viewPager.setAdapter(pagerAdapter);
        pagerAdapter.notifyDataSetChanged();

        viewPager.setCurrentItem(position);
        String text = position+"/"+uris0.size();
        textView.setText(text);
        return relativeLayout;
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
