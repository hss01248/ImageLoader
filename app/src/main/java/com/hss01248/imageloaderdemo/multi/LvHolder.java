package com.hss01248.imageloaderdemo.multi;

import android.app.Activity;
import android.widget.ImageView;

import com.hss01248.adapter.SuperLvHolder;
import com.hss01248.image.ImageLoader;
import com.hss01248.imageloaderdemo.R;

import butterknife.Bind;

/**
 * Created by huangshuisheng on 2017/9/28.
 */

public class LvHolder extends SuperLvHolder<String> {
    @Bind(R.id.item_iv)
    ImageView itemIv;

    public LvHolder(Activity context) {
        super(context);
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
            .loading(R.drawable.loading)*/
            .into(itemIv);
    }
}
