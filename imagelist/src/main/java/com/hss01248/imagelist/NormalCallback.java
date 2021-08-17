package com.hss01248.imagelist;

import java.util.List;

/**
 * time:2019/11/30
 * author:hss
 * desription:
 */
public interface NormalCallback<T> {

    void onSuccess(T t, Object extra);

    default void onFirst50Success(T t, Object extra){}

    //void onSuccessEnd(List<T> t, Object extra);

    void onFail(Throwable e);

    void onCancel();
}
