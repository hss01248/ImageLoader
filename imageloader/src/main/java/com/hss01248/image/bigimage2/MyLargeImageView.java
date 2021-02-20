package com.hss01248.image.bigimage2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.util.AttributeSet;

import com.shizhefei.view.largeimage.LargeImageView;

public class MyLargeImageView extends LargeImageView {
    PaintFlagsDrawFilter pfd;
    public MyLargeImageView(Context context) {
        this(context, null);
    }

    public MyLargeImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyLargeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setDrawFilter(pfd);
        super.onDraw(canvas);
    }
}
