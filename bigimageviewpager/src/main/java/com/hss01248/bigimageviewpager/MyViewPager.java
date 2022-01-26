package com.hss01248.bigimageviewpager;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class MyViewPager extends ViewPager {
    public MyViewPager(@NonNull Context context) {
        super(context);
    }

    public MyViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        }catch (Throwable throwable){
            throwable.printStackTrace();
            return false;
        }
    }


    int preOrientation;
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation != preOrientation){
            preOrientation = newConfig.orientation;
            if(onOrientationChangeListener != null){
                onOrientationChangeListener.onChage(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE);
            }
        }
    }

    public void setOnOrientationChangeListener(OnOrientationChangeListener onOrientationChangeListener) {
        this.onOrientationChangeListener = onOrientationChangeListener;
    }

    OnOrientationChangeListener onOrientationChangeListener;
    public interface OnOrientationChangeListener{
        void onChage(boolean isLandscape);
    }
}
