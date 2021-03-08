package com.hss01248.image.memory;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;


/**
 * time:2019/10/22
 * author:hss
 * desription:
 */
public class ImgItem extends BaseQuickAdapter<Bitmap, BaseViewHolder> {


    public ImgItem(int layoutResId, @Nullable List<Bitmap> data) {
        super(layoutResId, data);
    }

    public ImgItem(@Nullable List<Bitmap> data) {
        super(data);
    }

    public ImgItem(int layoutResId) {
        super(layoutResId);
    }


    @Override
    protected void convert(@NonNull BaseViewHolder helper, Bitmap item) {
        helper.setImageBitmap(R.id.iv, item);
        helper.setText(R.id.tv, ImageMemoryHookManager.getInfo(item));
    }
}
