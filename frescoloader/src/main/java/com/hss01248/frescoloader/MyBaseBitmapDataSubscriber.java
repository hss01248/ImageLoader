package com.hss01248.frescoloader;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;

/**
 * Created by Administrator on 2017/5/1.
 */

public abstract class MyBaseBitmapDataSubscriber extends BaseDataSubscriber<CloseableReference<CloseableImage>> {
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

        try {
            onNewResultImpl(bitmap);
        } finally {
            //CloseableReference.closeSafely(closeableImageRef);
        }
    }

    /**
     * The bitmap provided to this method is only guaranteed to be around for the lifespan of the
     * method.
     *
     * <p>The framework will free the bitmap's memory after this method has completed.
     * @param bitmap

     */
    protected abstract void onNewResultImpl(@Nullable Bitmap bitmap);
}
