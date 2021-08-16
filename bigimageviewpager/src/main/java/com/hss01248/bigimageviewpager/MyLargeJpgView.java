package com.hss01248.bigimageviewpager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.shizhefei.view.largeimage.LargeImageView;

public class MyLargeJpgView extends LargeImageView {
    PaintFlagsDrawFilter pfd;

    public float getMaxScaleRatio() {
        return maxScaleRatio;
    }

    public void setMaxScaleRatio(float maxScaleRatio) {
        this.maxScaleRatio = maxScaleRatio;
    }

    float maxScaleRatio = MyLargeImageView.defaultMaxScaleRatio;
    public MyLargeJpgView(Context context) {
        this(context, null);
    }

    public MyLargeJpgView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyLargeJpgView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        setCriticalScaleValueHook(new CriticalScaleValueHook() {
            @Override
            public float getMinScale(LargeImageView largeImageView, int imageWidth, int imageHeight, float suggestMinScale) {

                return 1.0f;
            }

            @Override
            public float getMaxScale(LargeImageView largeImageView, int imageWidth, int imageHeight, float suggestMaxScale) {
                final int layoutWidth = getMeasuredWidth();
                if (imageWidth > layoutWidth) {
                    return 1.0f * imageWidth / layoutWidth * maxScaleRatio;
                } else {
                    return maxScaleRatio;
                }
            }
        });
    }
     int layoutWidth = getMeasuredWidth();
     int layoutHeight = getMeasuredHeight();

     public interface OnScaleChangeListener{
         void onScaleChanged(float percent,float scale);
     }

    public void setOnScaleChangeListener(OnScaleChangeListener onScaleChangeListener) {
        this.onScaleChangeListener = onScaleChangeListener;
    }

    OnScaleChangeListener onScaleChangeListener;


    int oldPercent =  100;
    @Override
    protected void onDraw(Canvas canvas) {
         layoutWidth = getMeasuredWidth();
         layoutHeight = getMeasuredHeight();

       float nowWidth =  layoutWidth * getScale();
       int realWith = getImageWidth();
       int percent = 100;
       if(realWith > 0){
           percent = Math.round(nowWidth * 100f / realWith );
           if(onScaleChangeListener != null){
               if(oldPercent != percent){
                   onScaleChangeListener.onScaleChanged(percent,getScale());
                   oldPercent = percent;
               }
           }
       }
        Log.w("large","onDraw ,percent: "+percent+"% , scale:"+getScale());
        canvas.setDrawFilter(pfd);
        super.onDraw(canvas);
    }

}
