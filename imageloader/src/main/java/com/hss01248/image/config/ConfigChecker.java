package com.hss01248.image.config;

import android.text.TextUtils;
import android.util.Log;

import com.hss01248.image.utils.ContextUtil;

import java.io.File;

/**
 * Created by huangshuisheng on 2018/3/22.
 */

public class ConfigChecker {
    public static boolean check(SingleConfig config) {
        if (!checkImageSource(config)) {
            return false;
        }
        if (!isContextUsable(config)) {
            Log.w("imageloader", "context is not usable:" + config.getContext());
            return false;
        }
        adjustWidthAndHeight(config);
        return true;
    }

    private static boolean isContextUsable(SingleConfig config) {
        Object obj = config.getContext();
        return ContextUtil.isUseable(obj);
    }


    /*
    根据view上设置的宽高来决定最终的resize option
     */
    private static void adjustWidthAndHeight(SingleConfig config) {

    }

    private static boolean checkImageSource(SingleConfig config) {
        if (!TextUtils.isEmpty(config.getSourceString())) {
            return true;
        }


        if (config.getResId() != 0) {
            return true;
        }
        if (config.getBytes() != null) {
            return true;
        }

        return true;
    }
}
