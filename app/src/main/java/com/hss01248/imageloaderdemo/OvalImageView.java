package com.hss01248.imageloaderdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * time:2019/10/20
 * author:hss
 * desription:
 */
public class OvalImageView extends ImageView {
    /*圆角的半径，依次为左上角xy半径，右上角，右下角，左下角*/
    private float[] rids = {10.0f, 10.0f, 10.0f, 10.0f, 0.0f, 0.0f, 0.0f, 0.0f,};

    RectF rectF;
    Path path;

    public OvalImageView(Context context) {
        super(context);
    }


    public OvalImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public OvalImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     * 画图
     * by Hankkin at:2015-08-30 21:15:53
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if (path == null) {
            path = new Path();
        }

        int w = this.getWidth();
        int h = this.getHeight();
        /*向路径中添加圆角矩形。radii数组定义圆角矩形的四个圆角的x,y半径。radii长度必须为8*/
        if (rectF == null) {
            rectF = new RectF(0, 0, w, h);
        }
        path.addRoundRect(rectF, rids, Path.Direction.CW);
        canvas.clipPath(path);
        super.onDraw(canvas);
    }

}
