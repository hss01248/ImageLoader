package com.hss01248.bigimageviewpager;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.util.AttributeSet;
import android.util.Log;

import com.shizhefei.view.largeimage.LargeImageView;

@Deprecated
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
                //这里控制默认显示时的大小,类似scaletype的作用:
                /*int layoutWidth = largeImageView.getMeasuredWidth();
                int layoutHeight = largeImageView.getMeasuredHeight();
                float suggestMinScale2 = layoutHeight *1.0f/imageHeight;
                float suggestMinScale3 = layoutWidth *1.0f/imageWidth;
                LogUtils.d("height scale: "+ suggestMinScale2, "width scale: "+suggestMinScale3);*/

                //否则就按默认的,铺满宽度

                return suggestMinScale;
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
         void onScaleChanged(int percent,float scale);
     }

    public void setOnScaleChangeListener(OnScaleChangeListener onScaleChangeListener) {
        this.onScaleChangeListener = onScaleChangeListener;
    }

    OnScaleChangeListener onScaleChangeListener;


    int oldPercent =  100;
    float realPercentNow = 0f;
    @Override
    protected void onDraw(Canvas canvas) {
         layoutWidth = getMeasuredWidth();
         layoutHeight = getMeasuredHeight();

       float nowWidth =  layoutWidth * getScale();
       int realWith = getImageWidth();
       int percent = 100;
       if(realWith > 0){
           realPercentNow = nowWidth * 1.0f / realWith;
           percent = Math.round(realPercentNow * 100f  );
           if(onScaleChangeListener != null){
               if(oldPercent != percent){
                   onScaleChangeListener.onScaleChanged(percent,getScale());
                   oldPercent = percent;
               }
           }
       }
        Log.v("large","onDraw ,percent: "+percent+"% , scale:"+getScale());
        canvas.setDrawFilter(pfd);
        super.onDraw(canvas);
    }

    long last = 0;
    public void setOritation(boolean isLandscape, boolean fromConfigChange){
       if(layoutHeight ==0 && layoutWidth ==0){
           return;
       }
        //layoutHeight = getMeasuredHeight();
      // layoutWidth = getMeasuredWidth();
        if(imageWidth ==0 && imageHeight ==0){
            return;
        }
        if(!fromConfigChange){
            if(last != 0 && last - System.currentTimeMillis() < 1000){
                return;
            }
            last = System.currentTimeMillis();
        }

       float scaleWidth = 1.0f;
        float scaleHeight = 1.0f;
        scaleHeight =  layoutHeight* 1.0f / imageHeight;
        scaleWidth =  layoutWidth* 1.0f / imageWidth;

       Log.d("measure","view: "+ layoutWidth+"x"+layoutHeight+", iamge:"+imageWidth+"x"+imageHeight+", scaleWidth:"+
               scaleWidth+",scaleHeight"+scaleHeight+", minScale:"+ getMinScale()+", maxScale:"+ getMaxScale()+
               ", maxratio:"+ getMaxScaleRatio()+", isLandscape:"+ isLandscape+",currentScale:"+getScale()+",realPercentNow:"+realPercentNow);

       //getScale()
        /*if(imageHeight*1.0f/imageWidth > layoutHeight*1.0f/layoutWidth){
            setScale(layoutHeight *1.0f/imageHeight);
        }else {
            setScale(layoutWidth *1.0f/imageWidth);
        }*/


        if(isLandscape){
            if(!fromConfigChange){
                setScale(scaleHeight/scaleWidth);
            }else {
                setScale(getMinScale()/scaleHeight);
            }
           // smoothScale(scaleHeight/scaleWidth,0,0);

        }else {
            if(fromConfigChange){
                setScale(scaleHeight/scaleWidth);
            }else {
                setScale(1.0f);
            }
        }

        //setScale(getMinScale());
    }

    int imageWidth;
    int imageHeight;
    @Override
    public void onLoadImageSize(int imageWidth, int imageHeight) {
        super.onLoadImageSize(imageWidth, imageHeight);
        preOrientation = getContext().getResources().getConfiguration().orientation;
        if(preOrientation == Configuration.ORIENTATION_LANDSCAPE){
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;
            setOritation(true,false);

        }else {
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;
            setOritation(false,false);
        }
    }

    int preOrientation;
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("onConfigurationChanged",newConfig.orientation+"--");
        if(newConfig.orientation != preOrientation){
            preOrientation = newConfig.orientation;
            setOritation(preOrientation == Configuration.ORIENTATION_LANDSCAPE,true);
        }
    }
}
