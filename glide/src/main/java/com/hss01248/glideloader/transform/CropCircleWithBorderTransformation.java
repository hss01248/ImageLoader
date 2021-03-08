package com.hss01248.glideloader.transform;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

/**
 * time:2019/11/7
 * author:hss
 * desription:
 * https://github.com/jeasonlzy/ViewCore/blob/master/view-core/src/main/java/com/lzy/widget/CircleImageView.java
 * <p>
 * https://www.jianshu.com/p/bf578f230cfc
 */
public class CropCircleWithBorderTransformation extends BitmapTransformation {
    private static final int VERSION = 1;
    private static final String ID = "jp.wasabeef.glide.transformations.CropCircleWithBorderTransformation." + VERSION;

    private Paint mBorderPaint;
    private float mBorderWidth;
    private int borderColor;

    public CropCircleWithBorderTransformation(Context context) {
        super(context);
    }

    public CropCircleWithBorderTransformation(Context context, int borderWidth, int borderColor) {
        super(context);
        mBorderWidth = Resources.getSystem().getDisplayMetrics().density * borderWidth;

        mBorderPaint = new Paint();
        mBorderPaint.setDither(true);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(borderColor);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mBorderWidth);
        this.borderColor = borderColor;
    }


    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        return circleCrop(pool, toTransform);
    }

    private Bitmap circleCrop(BitmapPool pool, Bitmap source) {
        if (source == null) return null;

        int size = (int) (Math.min(source.getWidth(), source.getHeight()) - (mBorderWidth / 2));
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;
        // TODO this could be acquired from the pool too
        Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);
        Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
        if (result == null) {
            result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);
        if (mBorderPaint != null) {
            float borderRadius = r - mBorderWidth / 2;
            canvas.drawCircle(r, r, borderRadius, mBorderPaint);
        }
        return result;

    }


    @Override
    public boolean equals(Object o) {
        return o instanceof CropCircleWithBorderTransformation &&
                ((CropCircleWithBorderTransformation) o).mBorderWidth == mBorderWidth &&
                ((CropCircleWithBorderTransformation) o).borderColor == borderColor;
    }

    @Override
    public int hashCode() {
        return (int) (ID.hashCode() + mBorderWidth * 100 + borderColor + 10);
    }

    @Override
    public String getId() {
        return ID + mBorderWidth * 100 + borderColor;
    }
}
