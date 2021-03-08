package com.hss01248.frescoloader;

import android.graphics.Bitmap;

import com.facebook.cache.common.CacheKey;
import com.facebook.common.references.CloseableReference;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.imagepipeline.bitmaps.SimpleBitmapReleaser;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.CloseableStaticBitmap;
import com.facebook.imagepipeline.image.ImmutableQualityInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.config.ScaleMode;

/**
 * Created by huangshuisheng on 2017/9/28.
 */

public class FrescoUtil {

    public static ScalingUtils.ScaleType getActualScaleType(int scaleMode) {
        switch (scaleMode) {
            case ScaleMode.CENTER_CROP:
                return ScalingUtils.ScaleType.CENTER_CROP;

            case ScaleMode.CENTER_INSIDE:
                return ScalingUtils.ScaleType.CENTER_INSIDE;

            case ScaleMode.FIT_CENTER:
                return ScalingUtils.ScaleType.FIT_CENTER;

            case ScaleMode.FIT_XY:
                return ScalingUtils.ScaleType.FIT_XY;

            case ScaleMode.FIT_END:
                return ScalingUtils.ScaleType.FIT_END;

            case ScaleMode.FOCUS_CROP:
                return ScalingUtils.ScaleType.FOCUS_CROP;

            case ScaleMode.CENTER:
                return ScalingUtils.ScaleType.CENTER;

            case ScaleMode.FIT_START:
                return ScalingUtils.ScaleType.FIT_START;

            default:
                return ScalingUtils.ScaleType.CENTER_CROP;

        }
    }

    public static void putIntoPool(Bitmap bitmap, String uriString) {
        final ImageRequest requestBmp = ImageRequest.fromUri(uriString); // 赋值

// 获得 Key
        CacheKey cacheKey = DefaultCacheKeyFactory.getInstance().getBitmapCacheKey(requestBmp, ImageLoader.context);

// 获得 closeableReference
        CloseableReference<CloseableImage> closeableReference = CloseableReference.<CloseableImage>of(
                new CloseableStaticBitmap(bitmap,
                        SimpleBitmapReleaser.getInstance(),
                        ImmutableQualityInfo.FULL_QUALITY, 0));
// 存入 Fresco
        Fresco.getImagePipelineFactory().getBitmapMemoryCache().cache(cacheKey, closeableReference);
    }
}
