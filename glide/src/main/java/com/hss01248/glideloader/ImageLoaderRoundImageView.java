
package com.hss01248.glideloader;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.hss01248.image.MyUtil;


/**
 * 圆角ImageView,支持设置的selector
 *
 * @author daixiaogang
 * @version 1.0
 * @since 2019-06-27
 */
public class ImageLoaderRoundImageView extends AppCompatImageView {

    private Context context;

    /**
     * 是否显示为圆形，如果为圆形则设置的圆角无效
     */
    private boolean isCircle;

    private int borderWidth;

    private int borderColor = Color.WHITE;

    private int cornerRadius;
   private float leftTop, rightTop,  rightBottom,  leftBottom;

    private Xfermode xfermode;

    private int width;

    private int height;

    private float radius;

    private float[] borderRadii;

    private float[] srcRadii;

    private RectF srcRectf;

    private RectF borderRectf;

    private Paint paint;

    private Path path;

    private Path srcPath;

    public ImageLoaderRoundImageView(Context context) {
        this(context, null);
    }

    public ImageLoaderRoundImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageLoaderRoundImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ImageLoaderRoundView, 0, 0);
        for (int i = 0; i < ta.getIndexCount(); i++) {
            int attr = ta.getIndex(i);
            if (attr == R.styleable.ImageLoaderRoundView_is_circle) {
                isCircle = ta.getBoolean(attr, isCircle);
            } else if (attr == R.styleable.ImageLoaderRoundView_border_width) {
                borderWidth = ta.getDimensionPixelSize(attr, borderWidth);
            } else if (attr == R.styleable.ImageLoaderRoundView_border_color) {
                borderColor = ta.getColor(attr, borderColor);
            } else if (attr == R.styleable.ImageLoaderRoundView_corner_radius) {
                cornerRadius = ta.getDimensionPixelSize(attr, cornerRadius);
            }
        }
        ta.recycle();
        
        borderRadii = new float[8];
        srcRadii = new float[8];

        borderRectf = new RectF();
        srcRectf = new RectF();

        paint = new Paint();
        path = new Path();

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            xfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
        } else {
            xfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
            srcPath = new Path();
        }
        if(cornerRadius>0){
            setCornerRadius(cornerRadius);
        }
        calculateRadii();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;

        initBorderRectf();
        initSrcRectf();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.saveLayer(srcRectf, null, Canvas.ALL_SAVE_FLAG);
        float sx = 1.0f * (width - 2 * borderWidth) / width;
        float sy = 1.0f * (height - 2 * borderWidth) / height;
        canvas.scale(sx, sy, width / 2.0f, height / 2.0f);
        super.onDraw(canvas);
        paint.reset();
        path.reset();
        if (isCircle) {
            path.addCircle(width / 2.0f, height / 2.0f, radius, Path.Direction.CCW);
        } else {
            path.addRoundRect(srcRectf, srcRadii, Path.Direction.CCW);
        }
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setXfermode(xfermode);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            canvas.drawPath(path, paint);
        } else {
            srcPath.reset();
            srcPath.addRect(srcRectf, Path.Direction.CCW);
            srcPath.op(path, Path.Op.DIFFERENCE);
            canvas.drawPath(srcPath, paint);
        }
        paint.setXfermode(null);
        canvas.restore();
        drawBorders(canvas);
    }

    private void drawBorders(Canvas canvas) {
        if (isCircle) {
            if (borderWidth > 0) {
                drawCircleBorder(canvas, borderWidth, borderColor, radius - borderWidth / 2.0f);
            }
        } else {
            if (borderWidth > 0) {
                drawRectFBorder(canvas, borderWidth, borderColor, borderRectf, borderRadii);
            }
        }
    }

    private void drawCircleBorder(Canvas canvas, int borderWidth, int borderColor, float radius) {
        initBorderPaint(borderWidth, borderColor);
        path.addCircle(width / 2.0f, height / 2.0f, radius, Path.Direction.CCW);
        canvas.drawPath(path, paint);
    }

    private void drawRectFBorder(Canvas canvas, int borderWidth, int borderColor, RectF rectF, float[] radii) {
        initBorderPaint(borderWidth, borderColor);
        path.addRoundRect(rectF, radii, Path.Direction.CCW);
        canvas.drawPath(path, paint);
    }

    private void initBorderPaint(int borderWidth, int borderColor) {
        path.reset();
        paint.setStrokeWidth(borderWidth);
        paint.setColor(borderColor);
        paint.setStyle(Paint.Style.STROKE);
    }

    /**
     * 计算外边框的RectF
     */
    private void initBorderRectf() {
        if (!isCircle) {
            borderRectf.set(borderWidth / 2.0f, borderWidth / 2.0f, width - borderWidth / 2.0f,
                    height - borderWidth / 2.0f);
        }
    }

    /**
     * 计算图片原始区域的RectF
     */
    private void initSrcRectf() {
        if (isCircle) {
            radius = Math.min(width, height) / 2.0f;
            srcRectf.set(width / 2.0f - radius, height / 2.0f - radius, width / 2.0f + radius, height / 2.0f + radius);
        } else {
            srcRectf.set(0, 0, width, height);
        }
    }

    /**
     * 计算RectF的圆角半径
     */
    private void calculateRadii() {
        if (isCircle) {
            return;
        }
        if (getMaxCorner() > 0) {
            borderRadii[0] = leftTop;
            borderRadii[1] = leftTop;
            borderRadii[2] = rightTop;
            borderRadii[3] = rightTop;
            borderRadii[4] = rightBottom;
            borderRadii[5] = rightBottom;
            borderRadii[6] = leftBottom;
            borderRadii[7] = leftBottom;


            srcRadii[0] = leftTop - borderWidth / 2.0f;
            srcRadii[1] = leftTop - borderWidth / 2.0f;
            srcRadii[2] = rightTop- borderWidth / 2.0f;
            srcRadii[3] = rightTop- borderWidth / 2.0f;
            srcRadii[4] = rightBottom- borderWidth / 2.0f;
            srcRadii[5] = rightBottom- borderWidth / 2.0f;
            srcRadii[6] = leftBottom- borderWidth / 2.0f;
            srcRadii[7] = leftBottom- borderWidth / 2.0f;




            /*for (int i = 0; i < borderRadii.length; i++) {
                borderRadii[i] = cornerRadius;
                srcRadii[i] = cornerRadius - borderWidth / 2.0f;
            }*/
        }
    }

    private void calculateRadiiAndRectf(boolean reset) {
        if (reset) {
            cornerRadius = 0;
            leftTop = 0;
            leftBottom = 0;
            rightBottom = 0;
            rightTop = 0;
        }
        calculateRadii();
        initBorderRectf();
        invalidate();
    }

    /**
     * 是否圆形展示
     */
    public void isCircle(boolean isCircle) {
        this.isCircle = isCircle;
        initSrcRectf();
        invalidate();
    }

    public float getMaxCorner(){
        return  Math.max(rightBottom,Math.max(leftBottom,Math.max(leftTop,rightTop)));
    }

    /**
     * 设置圆角
     *
     * @param cornerRadius 圆角/dp
     */
    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = MyUtil.dip2px(cornerRadius);
        this.leftTop = cornerRadius;
        this.rightTop = cornerRadius;
        this.leftBottom = cornerRadius;
        this.rightBottom = cornerRadius;
        calculateRadiiAndRectf(false);
    }
    public void setCornerRadius(float leftTop, float rightTop,  float leftBottom,float rightBottom) {
        this.leftTop = MyUtil.dip2px(leftTop);
        this.rightTop = MyUtil.dip2px(rightTop);
        this.leftBottom = MyUtil.dip2px(leftBottom);
        this.rightBottom = MyUtil.dip2px(rightBottom);
        calculateRadiiAndRectf(false);
    }

    /**
     * 外边框
     *
     * @param borderWidth 宽度/dp
     */
    public void setBorderWidth(int borderWidth) {
        this.borderWidth = MyUtil.dip2px(borderWidth);
        calculateRadiiAndRectf(false);
    }

    /**
     * 设置边框颜色
     *
     * @param borderColor color
     */
    public void setBorderColor(@ColorInt int borderColor) {
        this.borderColor = borderColor;
        invalidate();
    }

}
