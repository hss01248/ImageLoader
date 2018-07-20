package com.github.piasy.biv.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.hss01248.image.R;

/**
 * Author riverlet.liu
 * Email: riverlet.liu@qq.com
 * Date: 2017/11/21.
 * Despribe:
 */

public class CircleLoadingView extends View {
    /**
     * 画笔
     */
    private Paint paint;

    /**
     * 开始颜色
     */
    private int startColor;
    /**
     * 结束颜色
     */
    private int endColor;
    /**
     * 圆环范围
     */
    private RectF rect;
    /**
     * 着色器
     */
    private Shader shader;
    /**
     * 着色器的变换矩阵
     */
    private Matrix matrix;
    /**
     * 圆环厚度f
     */
    private float circleThicknessRatio;
    /**
     * 属性东环改变的属性，0.0~1.0
     */
    private float rate = 0.0f;
    /**
     * 属性动画对象
     */
    private ObjectAnimator animator;


    public CircleLoadingView(Context context) {
        super(context);
        init(context, null);
    }

    public CircleLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircleLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleLoadingView);
        startColor = typedArray.getColor(R.styleable.CircleLoadingView_startColor, 0x88666666);
        endColor = typedArray.getColor(R.styleable.CircleLoadingView_endColor, 0x00000000);
        circleThicknessRatio = (float) typedArray.getDimension(R.styleable.CircleLoadingView_circleThicknessRatio, 0.1f);
        typedArray.recycle();
        paint = new Paint();
        paint.setAntiAlias(true);
        //paint.setStrokeWidth(circleThicknessRatio);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);

        rect = new RectF();

        animator = ObjectAnimator.ofFloat(this, "rate", 0.0f, 1.0f);
        animator.setDuration(800);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(-1);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        initRect(getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initRect(w, h);
    }

    private void initRect(float width, float height) {
        if (width > height) {
            rect.left = (width - height) / 2;
            rect.right = width - rect.left;
            rect.top = 0;
            rect.bottom = height;
        } else {
            rect.left = 0;
            rect.right = width;
            rect.top = (height - width) / 2;
            rect.bottom = height - rect.top;
        }

       int  circleThickness = (int) (width*circleThicknessRatio);
        paint.setStrokeWidth(circleThickness);

        rect.left = rect.left + circleThickness / 2;
        rect.right = rect.right - circleThickness / 2;
        rect.top = rect.top + circleThickness / 2;
        rect.bottom = rect.bottom - circleThickness / 2;

        //因为这个圆环是顺时针旋转的，所有endColor, startColor在shader上反过来写了
        shader = new SweepGradient(width / 2, height / 2, endColor, startColor);
        matrix = new Matrix();
        matrix.setRotate(-90 + 20,width/2,height/2);//着色器初始位置，12点钟方法加20度，收尾留一点空隙
        shader.setLocalMatrix(matrix);
        paint.setShader(shader);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        canvas.drawArc(rect, -90f + 30 + 360 * rate, 330f, false, paint);
    }


    private void refresh() {
        if (paint != null && matrix != null && shader != null) {
            matrix.setRotate(-90 + 20 + 360 * rate, getWidth() / 2, getHeight() / 2);
            shader.setLocalMatrix(matrix);
            paint.setShader(shader);
            invalidate();
        }
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
        refresh();
    }

    public float getCircleThicknessRatio() {
        return circleThicknessRatio;
    }

    public void setCircleThicknessRatio(float circleThicknessRatio) {
        this.circleThicknessRatio = circleThicknessRatio;
    }


    public void setStartColor(int startColor) {
        this.startColor = startColor;
    }

    public void setEndColor(int endColor) {
        this.endColor = endColor;
    }


    private void animStart() {
        if (animator != null && !animator.isStarted()) {
            animator.start();
        }
    }

    private void animStop() {
        if (animator != null && animator.isStarted()) {
            animator.cancel();
        }
    }

    public int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        animStart();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        animStop();
    }
}
