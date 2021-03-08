package com.hss01248.imagelist;

/**
 * time:2019/11/30
 * author:hss
 * desription:
 */
public interface NormalCallback<T> {

    void onSuccess(T t, Object extra);

    void onFail(Throwable e);

    void onCancel();
}
