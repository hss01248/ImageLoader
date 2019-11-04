package com.hss01248.glideloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.renderscript.RSRuntimeException;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.hss01248.image.MyUtil;

import jp.wasabeef.glide.transformations.internal.FastBlur;
import jp.wasabeef.glide.transformations.internal.RSBlur;

/**
 * time:2019/11/4
 * author:hss
 * desription:
 */
public class BlurTransform implements Transformation<Bitmap> {

    private Context mContext;
    private BitmapPool mBitmapPool;

    private int mRadius;
    public BlurTransform(Context context, int radius) {
        mContext = context.getApplicationContext();
        mBitmapPool = Glide.get(mContext).getBitmapPool();
        mRadius = radius;
    }




    @Override
    public Resource<Bitmap> transform(Resource<Bitmap> resource, int outWidth, int outHeight) {
        Bitmap source = resource.get();

        int width = source.getWidth();
        int height = source.getHeight();

        int samplesize = calculateInSampleSize(width,height,outWidth,outHeight)*2;


        int scaledWidth = width / samplesize;
        int scaledHeight = height / samplesize;

        Bitmap bitmap = mBitmapPool.get(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        canvas.scale(1 / (float) samplesize, 1 / (float) samplesize);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(source, 0, 0, paint);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            try {
                bitmap = RSBlur.blur(mContext, bitmap, mRadius);
            } catch (RSRuntimeException e) {
                bitmap = FastBlur.blur(bitmap, mRadius, true);
            }
        } else {
            bitmap = FastBlur.blur(bitmap, mRadius, true);
        }

        return BitmapResource.obtain(bitmap, mBitmapPool);
    }

    @Override
    public String getId() {
         return "BlurTransform(radius=" + mRadius + ", sampling=" + 5 + ")";
    }


    public static int calculateInSampleSize(int width,int height, int reqWidth, int reqHeight) {

        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            //计算图片高度和我们需要高度的最接近比例值
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            //宽度比例值
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            //取比例值中的较大值作为inSampleSize
            inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }
}
