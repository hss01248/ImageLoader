package com.hss01248.image;

import android.os.Build;

import androidx.annotation.RequiresApi;

/**
 * time:2020/1/14
 * author:hss
 * desription:
 */
public class ImageLoadFailException extends Exception {
    public ImageLoadFailException() {
    }

    public ImageLoadFailException(String message) {
        super(message);
    }

    public ImageLoadFailException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImageLoadFailException(Throwable cause) {
        super(cause);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ImageLoadFailException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
