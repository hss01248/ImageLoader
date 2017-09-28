package com.hss01248.frescoloader;

import com.facebook.drawee.drawable.ScalingUtils;
import com.hss01248.image.config.ScaleMode;

/**
 * Created by huangshuisheng on 2017/9/28.
 */

public class FrescoUtil {
    
    public static ScalingUtils.ScaleType getActualScaleType(int scaleMode){
        switch (scaleMode){
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
}
