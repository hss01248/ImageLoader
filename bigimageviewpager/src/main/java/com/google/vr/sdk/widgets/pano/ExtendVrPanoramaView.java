package com.google.vr.sdk.widgets.pano;

import android.content.Context;
import android.util.AttributeSet;

import com.google.vr.sdk.widgets.common.VrWidgetRenderer;

/**
 * @Despciption todo
 * @Author hss
 * @Date 11/07/2023 11:06
 * @Version 1.0
 */
public class ExtendVrPanoramaView extends VrPanoramaView {
    public ExtendVrPanoramaView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExtendVrPanoramaView(Context context) {
        super(context);
    }

    @Override
    protected VrPanoramaRenderer createRenderer(Context context, VrWidgetRenderer.GLThreadScheduler glThreadScheduler, float xMetersPerPixel, float yMetersPerPixel) {
        return super.createRenderer(context, glThreadScheduler, xMetersPerPixel*5f, yMetersPerPixel*5f);
    }
}
