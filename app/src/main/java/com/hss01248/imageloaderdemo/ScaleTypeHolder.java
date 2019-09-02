package com.hss01248.imageloaderdemo;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hss01248.adapter.SuperLvHolder;

import butterknife.BindView;


/**
 * Created by Administrator on 2017/11/5.
 */

public class ScaleTypeHolder extends SuperLvHolder<ScaleTypeInfo,Activity> {

    //@BindView(R.id.iv)
    ImageView iv;
    //@BindView(R.id.tv)
    TextView tv;

    public ScaleTypeHolder(Activity context) {
        super(context);
    }

    @Override
    protected void findViewsById(View view) {
        iv = view.findViewById(R.id.iv);
        tv = view.findViewById(R.id.tv);

    }

    @Override
    protected int setLayoutRes() {
        return R.layout.item_scale;
    }



    @Override
    public void assingDatasAndEvents(Activity activity, ScaleTypeInfo scaleTypeInfo) {
        iv.setScaleType(scaleTypeInfo.scaleType);
        tv.setText(scaleTypeInfo.scaleTypeStr);
        iv.setImageResource(scaleTypeInfo.resId);
    }
}
