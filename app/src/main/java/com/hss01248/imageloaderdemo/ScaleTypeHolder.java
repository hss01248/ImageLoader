package com.hss01248.imageloaderdemo;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hss01248.adapter.SuperLvAdapter;
import com.hss01248.adapter.SuperLvHolder;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.config.ScaleMode;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

import butterknife.BindView;


/**
 * Created by Administrator on 2017/11/5.
 */

public class ScaleTypeHolder extends SuperLvHolder<ScaleTypeInfo, Activity> {

    //@BindView(R.id.iv)
    ImageView iv;
    //@BindView(R.id.tv)
    TextView tv;
    RoundedImageView roundedImageView;
    com.github.siyamed.shapeimageview.RoundedImageView shapeIv;

    public ScaleTypeHolder(Activity context) {
        super(context);
    }

    @Override
    protected void findViewsById(View view) {
        iv = view.findViewById(R.id.iv);
        tv = view.findViewById(R.id.tv);
        roundedImageView = view.findViewById(R.id.iv2);
        shapeIv = view.findViewById(R.id.iv3);

    }

    @Override
    protected int setLayoutRes() {
        return R.layout.item_scale;
    }


    @Override
    public void assingDatasAndEvents(Activity activity, ScaleTypeInfo scaleTypeInfo) {

    }

    @Override
    public void assingDatasAndEvents(Activity activity, ScaleTypeInfo scaleTypeInfo, int position, boolean isLast, boolean isListViewFling, List datas, SuperLvAdapter superAdapter) {
        //super.assingDatasAndEvents(activity, bean, position, isLast, isListViewFling, datas, superAdapter);
        /* iv.setScaleType(scaleTypeInfo.scaleType);
        iv.setImageResource(scaleTypeInfo.resId);*/
        ImageLoader.with(activity)
                .scale(position + 1)
                .res(scaleTypeInfo.resId)
                .into(iv);

        roundedImageView.setImageResource(scaleTypeInfo.resId);
        roundedImageView.setScaleType(scaleTypeInfo.scaleType);

        shapeIv.setImageResource(scaleTypeInfo.resId);
        shapeIv.setScaleType(scaleTypeInfo.scaleType);

        tv.setText(scaleTypeInfo.scaleTypeStr);
    }
}
