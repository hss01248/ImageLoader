package com.hss01248.bigimageviewpager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.util.AttributeSet;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

/**
 * @Despciption todo
 * @Author hss
 * @Date 01/08/2023 15:24
 * @Version 1.0
 */
public class MyLargeJpgViewBySubsumplingView extends SubsamplingScaleImageView {

    PaintFlagsDrawFilter pfd;
    public MyLargeJpgViewBySubsumplingView(Context context, AttributeSet attr) {
        super(context, attr);
    }
    public MyLargeJpgViewBySubsumplingView(Context context) {
        super(context);
        init2();
    }

    private void init2() {
        pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    }



    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setDrawFilter(pfd);
        super.onDraw(canvas);
    }
}
