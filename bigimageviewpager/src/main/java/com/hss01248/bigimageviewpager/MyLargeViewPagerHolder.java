package com.hss01248.bigimageviewpager;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.LifecycleOwner;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.hss.utils.enhance.viewholder.mvvm.BaseViewHolder;
import com.hss01248.bigimageviewpager.databinding.RlPagerImagsBinding;
import com.hss01248.fullscreendialog.FullScreenDialogUtil;
import com.hss01248.media.metadata.MetaDataUtil;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyLargeViewPagerHolder extends BaseViewHolder<RlPagerImagsBinding, Pair<Integer, List<String>>> {
    public MyLargeViewPagerHolder(Context context) {
        super(context);
    }

    boolean showInfo = true;
    int currentPosition;
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
        /*containerViewHolderWithTitleBar.getBinding().realTitleBar.setRightTitle("● ● ●");
        containerViewHolderWithTitleBar.getBinding().realTitleBar.setRightTitleColor(Color.WHITE);
        containerViewHolderWithTitleBar.getBinding().realTitleBar.getRightView().setTextSize(10);*/
        containerViewHolderWithTitleBar.showRightMoreIcon(true);
        containerViewHolderWithTitleBar.getBinding().realTitleBar.getRightView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> metaData = MetaDataUtil.getMetaData( LargeImageViewer.getBigImageUrl(pair.second.get(currentPosition)));
                FullScreenDialogUtil.showMap("meta data",metaData);
            }
        });

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
