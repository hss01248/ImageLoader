package com.hss01248.glideloader.transform;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;

/**
 * time:2019/11/10
 * author:hss
 * desription:
 */
public class BorderRoundTransformation2 implements Transformation<Bitmap> {
    /**
     * 用一个整形表示哪些边角需要加圆角边框
     * 例如：0b1000,表示左上角需要加圆角边框
     * 0b1110 表示左上右上右下需要加圆角边框
     * 0b0000表示不加圆形边框
     */


    private BitmapPool mBitmapPool;
    private int mRadius; //圆角半径
    private int mMargin; //边距

    private int mBorderWidth;//边框宽度
    private int mBorderColor;//边框颜色
    private int mCornerPos; //圆角位置


    public BorderRoundTransformation2(Context context, int radius, int margin, int mBorderWidth, int mBorderColor, int position) {
        mBitmapPool = Glide.get(context).getBitmapPool();
        mRadius = radius;
        mMargin = margin;
        this.mBorderColor = mBorderColor;
        this.mBorderWidth = mBorderWidth;
        this.mCornerPos = position;
    }

    @Override
    public Resource<Bitmap> transform(Resource<Bitmap> resource, int outWidth, int outHeight) {
        Bitmap source = resource.get();

        int width = source.getWidth();
        int height = source.getHeight();

        Bitmap bitmap = mBitmapPool.get(width, height, Bitmap.Config.ARGB_8888);
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }

        /*Canvas canvas = new Canvas(bitmap);//新建一个空白的bitmap
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));//设置要绘制的图形

        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);//设置边框样式
        borderPaint.setColor(mBorderColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(mBorderWidth);

        drawRoundRect(canvas, paint, width, height, borderPaint);*/

        bitmap = getRoundBitmapByShader(bitmap, bitmap.getWidth(), bitmap.getHeight(), mRadius, mBorderColor);


        return BitmapResource.obtain(bitmap, mBitmapPool);
    }


    /**
     * 通过BitmapShader 圆角边框
     *
     * @param bitmap
     * @param outWidth
     * @param outHeight
     * @param radius
     * @param boarder
     * @return
     */
    public static Bitmap getRoundBitmapByShader(Bitmap bitmap, int outWidth, int outHeight, int radius, int boarder) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float widthScale = outWidth * 1f / width;
        float heightScale = outHeight * 1f / height;

        Matrix matrix = new Matrix();
        matrix.setScale(widthScale, heightScale);
        //创建输出的bitmap
        Bitmap desBitmap = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888);
        //创建canvas并传入desBitmap，这样绘制的内容都会在desBitmap上
        Canvas canvas = new Canvas(desBitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //创建着色器
        BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        //给着色器配置matrix
        bitmapShader.setLocalMatrix(matrix);
        paint.setShader(bitmapShader);
        //创建矩形区域并且预留出border
        RectF rect = new RectF(boarder, boarder, outWidth - boarder, outHeight - boarder);
        //把传入的bitmap绘制到圆角矩形区域内
        canvas.drawRoundRect(rect, radius, radius, paint);

        if (boarder > 0) {
            //绘制boarder
            Paint boarderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            boarderPaint.setColor(Color.GREEN);
            boarderPaint.setStyle(Paint.Style.STROKE);
            boarderPaint.setStrokeWidth(boarder);
            canvas.drawRoundRect(rect, radius, radius, boarderPaint);
        }
        return desBitmap;
    }


    private void drawRoundRect(Canvas canvas, Paint paint, float width, float height, Paint borderPaint) {
        float right = width - mMargin;
        float bottom = height - mMargin;
        float halfBorder = mBorderWidth / 2;
        Path path = new Path();

        float[] pos = new float[8];
        int shift = mCornerPos;

        int index = 3;

        while (index >= 0) {//设置四个边角的弧度半径
            pos[2 * index + 1] = ((shift & 1) > 0) ? mRadius : 0;
            pos[2 * index] = ((shift & 1) > 0) ? mRadius : 0;
            shift = shift >> 1;
            index--;
        }


        path.addRoundRect(new RectF(mMargin + halfBorder, mMargin + halfBorder, right - halfBorder, bottom - halfBorder),
                pos
                , Path.Direction.CW);

        canvas.drawPath(path, paint);//绘制要加载的图形

        canvas.drawPath(path, borderPaint);//绘制边框

    }


    @Override
    public String getId() {

        //这里一定要是设置一个独一无二的ID，要不然重用会导致第二次调用不起效果，最好加上相应的变量参数，保证唯一性
        return "RoundedTransformation(radius=" + mRadius + ", margin=" + mMargin + ", mBorderWidth" + mBorderWidth + ", mBorderColor" + mBorderColor + "mCornerPos" + mCornerPos + ")";
    }

}
