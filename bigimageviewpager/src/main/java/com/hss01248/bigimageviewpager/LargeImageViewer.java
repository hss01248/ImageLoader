package com.hss01248.bigimageviewpager;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
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


    public static void setOnRightMenuClickedListener(OnRightMenuClickedListener onRightMenuClickedListener) {
        LargeImageViewer.onRightMenuClickedListener = onRightMenuClickedListener;
    }

    public static OnRightMenuClickedListener getOnRightMenuClickedListener() {
        return onRightMenuClickedListener;
    }

    static OnRightMenuClickedListener onRightMenuClickedListener;

    public static void fadeToGone(View view, long duration){
        // 创建alpha动画，从1.0（完全不透明）到0.0（完全透明）
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setDuration(duration); // 动画时长为1秒
        // 设置动画监听器
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // 动画开始时的处理
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // 动画重复时的处理
            }
        });

        // 开启动画
        view.startAnimation(alphaAnimation);
    }

    public static void fadeToVisiable(View view, long duration){
        // 创建alpha动画，从1.0（完全不透明）到0.0（完全透明）
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(duration); // 动画时长为1秒
        // 设置动画监听器
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // 动画开始时的处理
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // 动画重复时的处理
            }
        });
        view.setVisibility(View.VISIBLE);
        // 开启动画
        view.startAnimation(alphaAnimation);
    }

    public static void showOne(String path){
        ContainerActivity2.start(new Consumer<Pair<ContainerActivity2, ContainerViewHolderWithTitleBar>>() {
            @Override
            public void accept(Pair<ContainerActivity2, ContainerViewHolderWithTitleBar> pair) throws Exception {

                MyLargeImageViewBySubSamplingView largeImageView = new MyLargeImageViewBySubSamplingView(pair.first);
                largeImageView.loadUri(path,true);
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
        largeImageView.loadUri(path,true);
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
                MyLargeImageViewBySubSamplingView imageView = null;
                if(cacheList.isEmpty()){
                    imageView = new MyLargeImageViewBySubSamplingView(context);
                }else {
                    imageView = (MyLargeImageViewBySubSamplingView) cacheList.remove(0);
                }
                String url = uris.get(position);
                url = getBigImageUrl(url);
                imageView.loadUri(url,true);
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
