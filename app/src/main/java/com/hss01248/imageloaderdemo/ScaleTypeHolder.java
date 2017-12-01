package com.hss01248.imageloaderdemo;

import android.app.Activity;
import android.widget.ImageView;
import android.widget.TextView;

import com.hss01248.adapter.SuperLvHolder;

import butterknife.Bind;

/**
 * Created by Administrator on 2017/11/5.
 */

public class ScaleTypeHolder extends SuperLvHolder<ScaleTypeInfo> {

    @Bind(R.id.iv)
    ImageView iv;
    @Bind(R.id.tv)
    TextView tv;

    public ScaleTypeHolder(Activity context) {
        super(context);
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
