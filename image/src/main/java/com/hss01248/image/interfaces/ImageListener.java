package com.hss01248.image.interfaces;

import android.graphics.Bitmap;

/**
 * Created by huangshuisheng on 2018/3/21.
 */

public interface ImageListener {

    void onSuccess(String filePath, int width, int height, Bitmap bitmap,int bWidth,int bHeight);

    //void onSuccess(InputStream is);

    void onFail(Throwable e);
}
