package com.hss01248.image.interfaces;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by huangshuisheng on 2018/3/21.
 */

public interface ImageListener {

    void onSuccess(@NonNull Drawable drawable, @Nullable Bitmap bitmap, int bWidth, int bHeight);

    //void onSuccess(InputStream is);

    void onFail(Throwable e);
}
