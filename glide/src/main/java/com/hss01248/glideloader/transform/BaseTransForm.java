package com.hss01248.glideloader.transform;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.hss01248.image.MyUtil;
import com.hss01248.image.config.GlobalConfig;

import java.security.PublicKey;

public  class BaseTransForm  {


    public static  Bitmap scale(Resource<Bitmap> resource, int outWidth, int outHeight){
        Bitmap source = resource.get();

        int width = source.getWidth();
        int height = source.getHeight();

        int samplesize = MyUtil.calculateScaleRatio(width,height,outWidth,outHeight,1);



        int scaledWidth = width / samplesize;
        int scaledHeight = height / samplesize;

        Bitmap bitmap = Glide.get(GlobalConfig.context).getBitmapPool().get(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        canvas.scale(1 / (float) samplesize, 1 / (float) samplesize);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(source, 0, 0, paint);
        return bitmap;
    }


}
