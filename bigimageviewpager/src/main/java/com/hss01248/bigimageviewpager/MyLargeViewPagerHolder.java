package com.hss01248.bigimageviewpager;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.LifecycleOwner;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.blankj.utilcode.util.LogUtils;
import com.hss.utils.enhance.viewholder.mvvm.BaseViewHolder;
import com.hss01248.bigimageviewpager.databinding.RlPagerImagsBinding;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MyLargeViewPagerHolder extends BaseViewHolder<RlPagerImagsBinding, Pair<Integer, List<String>>> {
    public MyLargeViewPagerHolder(Context context) {
        super(context);
    }

    boolean showInfo = true;
    int currentPosition;

    Map<Integer,View> viewMap = new TreeMap<>();
    @Override
    protected void initDataAndEventInternal(LifecycleOwner lifecycleOwner, Pair<Integer, List<String>> pair) {
        currentPosition = pair.first;
        List<String> uris = new ArrayList<>(pair.second.size());
        uris.addAll(pair.second);
        if(pair.second.size()==1){
            binding.llProgress.setVisibility(View.GONE);
            containerViewHolderWithTitleBar.getBinding().realTitleBar.setTitle("");
        }



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
                LogUtils.d(position,"instantiateItem",cacheList.size(),viewMap.size());
                MyLargeImageViewBySubSamplingView imageView = null;
                if(cacheList.isEmpty()){
                    imageView = new MyLargeImageViewBySubSamplingView(context);
                }else {
                    imageView = (MyLargeImageViewBySubSamplingView) cacheList.remove(0);
                }
                String url = uris.get(position);
                url = LargeImageViewer.getBigImageUrl(url);
                imageView.loadUri(url,viewMap.isEmpty());
                container.addView(imageView);
                viewMap.put(position,imageView);

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(showInfo){
                            binding.llProgress.setVisibility(View.GONE);
                            containerViewHolderWithTitleBar.getBinding().realTitleBar.setVisibility(View.GONE);
                        }else{
                            if(pair.second.size()>1){
                                binding.llProgress.setVisibility(View.VISIBLE);
                            }
                            containerViewHolderWithTitleBar.getBinding().realTitleBar.setVisibility(View.VISIBLE);
                        }
                        showInfo = !showInfo;
                    }
                });
                return imageView;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((View) object);
                cacheList.add((View)object);
                viewMap.remove(position);
            }
        };
        ViewPager.OnPageChangeListener listener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                showInfo(position, uris);
                LogUtils.d(position,"onPageSelected");
                MyLargeImageViewBySubSamplingView imageView = (MyLargeImageViewBySubSamplingView) viewMap.get(position);
                if(imageView !=null){
                    imageView.loadUri(uris.get(position),true);
                }else {
                    LogUtils.w("imageView ==null",position);
                }
                //前后两个播放器停止
                int pre = position-1;
                MyLargeImageViewBySubSamplingView imageViewPre = (MyLargeImageViewBySubSamplingView) viewMap.get(pre);
                if(imageViewPre !=null){
                    imageViewPre.pausePlayer();
                }

                int next = position+1;
                MyLargeImageViewBySubSamplingView imageViewNext= (MyLargeImageViewBySubSamplingView) viewMap.get(next);
                if(imageViewNext !=null){
                    imageViewNext.pausePlayer();
                }


            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
        viewPager.setOnPageChangeListener(listener);



        viewPager.setAdapter(pagerAdapter);
        pagerAdapter.notifyDataSetChanged();

        //一般来说是先instantiateItem后onPageSelected,
        // 但setCurrentItem这个方法会先触发onPageSelected,后触发instantiateItem
        viewPager.setCurrentItem(pair.first);

        showInfo(pair.first,uris);



        binding.seekBar.setMax(uris.size());

        binding.seekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                viewPager.setCurrentItem(seekBar.getProgress());

            }
        });
        containerViewHolderWithTitleBar.getBinding().realTitleBar.setTitle("");
        containerViewHolderWithTitleBar.showRightMoreIcon(true);
        containerViewHolderWithTitleBar.getBinding().realTitleBar.getRightView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(LargeImageViewer.getOnRightMenuClickedListener()!=null){
                    LargeImageViewer.getOnRightMenuClickedListener().onClicked(v,
                            LargeImageViewer.getBigImageUrl(pair.second.get(currentPosition)),
                            pair.second,currentPosition);
                }
                /*Map<String, String> metaData = MetaDataUtil.getMetaData( LargeImageViewer.getBigImageUrl(pair.second.get(currentPosition)));
                FullScreenDialogUtil.showMap("meta data",metaData);*/
            }
        });

        //重新播放的按钮:


    }

    private void showInfo(int position, List<String> uris) {
        currentPosition = position;
        binding.seekBar.setProgress(position);
        String text = (position+1) +"/"+ uris.size();
        binding.tvProgress.setText(text);

       /* String path = uris.get(position);
        String name = path.substring(path.lastIndexOf("/")+1);
        if(name.contains("?")){
            name = name.substring(0,name.indexOf("?"));
        }*/

    }
}
