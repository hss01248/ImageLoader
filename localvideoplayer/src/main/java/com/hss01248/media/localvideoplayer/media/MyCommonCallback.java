package com.hss01248.media.localvideoplayer.media;

import androidx.annotation.Nullable;

public interface MyCommonCallback<T> {
    void onSuccess(T t);

    void onError(String code, String msg,@Nullable Throwable throwable);
}
