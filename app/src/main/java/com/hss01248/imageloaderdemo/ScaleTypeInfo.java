package com.hss01248.imageloaderdemo;

import android.widget.ImageView;

/**
 * Created by Administrator on 2017/11/5.
 */

public class ScaleTypeInfo {

    ImageView.ScaleType scaleType;
    String scaleTypeStr;
    int resId;

    public ScaleTypeInfo(ImageView.ScaleType scaleType, String scaleTypeStr, int resId) {
        this.scaleType = scaleType;
        this.scaleTypeStr = scaleTypeStr;
        this.resId = resId;
    }
}
