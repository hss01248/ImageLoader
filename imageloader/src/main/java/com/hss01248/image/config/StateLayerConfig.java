package com.hss01248.image.config;

/**
 * Created by huangshuisheng on 2017/12/29.
 */

public class StateLayerConfig {

    private int loadingScaleType = GlobalConfig.loadingScaleType;

    private int loadingResId;
    private boolean userDefaultLoadingRes;

    //UI:
    private int placeHolderResId = GlobalConfig.placeHolderResId;
    private boolean reuseable;//当前view是不是可重用的

    private int placeHolderScaleType = GlobalConfig.placeHolderScaleType;


    private int errorScaleType = GlobalConfig.errorScaleType;
    private int errorResId = GlobalConfig.errorResId;

    private int retryScaleType = GlobalConfig.errorScaleType;
    private int retryResId = GlobalConfig.errorResId;


}
