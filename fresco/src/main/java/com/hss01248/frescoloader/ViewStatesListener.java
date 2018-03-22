package com.hss01248.frescoloader;

import android.view.View;

import com.facebook.drawee.view.DraweeHolder;

/**
 * Created by huangshuisheng on 2018/3/22.
 */

public class ViewStatesListener implements View.OnAttachStateChangeListener{
    private DraweeHolder holder;
    public ViewStatesListener(DraweeHolder holder){
        this.holder=holder;
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        this.holder.onAttach();
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        this.holder.onDetach();
    }
}
