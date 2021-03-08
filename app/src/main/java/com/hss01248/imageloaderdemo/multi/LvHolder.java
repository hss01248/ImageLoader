package com.hss01248.imageloaderdemo.multi;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;

import com.hss01248.adapter.SuperLvHolder;
import com.hss01248.image.ImageLoader;
import com.hss01248.imageloaderdemo.R;

import butterknife.BindView;


/**
 * Created by huangshuisheng on 2017/9/28.
 */

public class LvHolder extends SuperLvHolder<String, Activity> {
    //@BindView(R.id.item_iv)
    ImageView itemIv;

    public LvHolder(Activity context) {
        super(context);
    }

    @Override
    protected void findViewsById(View view) {
        itemIv = view.findViewById(R.id.item_iv);
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.item_iv;
    }

    @Override
    public void assingDatasAndEvents(Activity activity, String s) {
        ImageLoader.with(activity)
                .widthHeight(333, 333)
                /* .placeHolder(R.drawable.default_placeholder_300x300,false)
                 .error(R.drawable.warning)
                 .loading2(R.drawable.loading2)*/
                .into(itemIv);
    }
}
