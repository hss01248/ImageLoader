package com.hss01248.image.memory;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import kale.adapter.item.AdapterItem;

/**
 * time:2019/10/22
 * author:hss
 * desription:
 */
public class ImgItem implements AdapterItem<Bitmap> {
    TextView tv;
    ImageView iv;
    @Override
    public int getLayoutResId() {
        return R.layout.img_item_show;
    }

    @Override
    public void bindViews(@NonNull View root) {
        iv = root.findViewById(R.id.iv);
        tv = root.findViewById(R.id.tv);

    }

    @Override
    public void setViews() {

    }

    @Override
    public void handleData(Bitmap bitmap, int position) {
        tv.setText(ImageMemoryHookManager.getInfo(bitmap));
        iv.setImageBitmap(bitmap);

    }
}
