package com.hss01248.frescoloader;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.hss01248.frescoloader.gif.GifUtils;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.MyUtil;

import java.io.File;

/**
 * Created by Administrator on 2017/5/1.
 */

public abstract class MyBaseBitmapDataSubscriber extends BaseDataSubscriber<CloseableReference<CloseableImage>> {

    String finalUrl; int width; int height;

    public MyBaseBitmapDataSubscriber(String finalUrl, int width, int height) {
        this.finalUrl = finalUrl;
        this.width = width;
        this.height = height;
    }

    @Override
    public void onNewResult(DataSource<CloseableReference<CloseableImage>> dataSource) {
        // isFinished() should be checked before calling onNewResultImpl(), otherwise
        // there would be a race condition: the final data source result might be ready before
        // we call isFinished() here, which would lead to the loss of the final result
        // (because of an early dataSource.close() call).
        final boolean shouldClose = dataSource.isFinished();
        try {
            onNewResultImpl(dataSource);
        } finally {
            if (shouldClose) {
               // dataSource.close();
            }
        }
    }



    @Override
    public void onNewResultImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
        if (!dataSource.isFinished()) {
            return;
        }

        CloseableReference<CloseableImage> closeableImageRef = dataSource.getResult();
        Bitmap bitmap = null;
        if (closeableImageRef != null &&
                closeableImageRef.get() instanceof CloseableBitmap) {
            bitmap = ((CloseableBitmap) closeableImageRef.get()).getUnderlyingBitmap();
        }


        if(bitmap!=null ){
            if(bitmap.isRecycled()){
                onFailure(dataSource);
            }else {
                onNewResultImpl(bitmap,dataSource);
            }
            return;
        }

        //如果bitmap为空
        File cacheFile  = ImageLoader.getActualLoader().getFileFromDiskCache(finalUrl);
        if(cacheFile ==null){
            onFailure(dataSource);
            return;
        }
        //还要判断文件是不是gif格式的
        if (!"gif".equalsIgnoreCase(MyUtil.getRealType(cacheFile))){
            onFailure(dataSource);
            return;
        }
        Bitmap bitmapGif = GifUtils.getBitmapFromGifFile(cacheFile);//拿到gif第一帧的bitmap
        if(width>0 && height >0) {
            bitmapGif = MyUtil.compressBitmap(bitmapGif, true, width, height);//将bitmap压缩到指定宽高。
        }

        if (bitmapGif != null) {
            onNewResultImpl(bitmap,dataSource);
        } else {
            onFailure(dataSource);
        }




       /* try {
            onNewResultImpl(bitmap);
        } finally {
            //CloseableReference.closeSafely(closeableImageRef);
        }*/
    }

    /**
     * The bitmap provided to this method is only guaranteed to be around for the lifespan of the
     * method.
     *
     * <p>The framework will free the bitmap's memory after this method has completed.
     * @param bitmap

     */
    protected abstract void onNewResultImpl(@Nullable Bitmap bitmap,DataSource<CloseableReference<CloseableImage>> dataSource);
}
