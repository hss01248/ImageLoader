package com.hss01248.image.interfaces;

import java.io.File;

/**
 * Created by Administrator on 2017/5/3.
 */

public interface FileGetter {

    void onSuccess(File file, int width, int height);

    //void onSuccess(InputStream is);

    void onFail(Throwable e);

    default void onProgress(long currentOffset, long totalLength) {
    }

    default void onStart(){

    }

}
