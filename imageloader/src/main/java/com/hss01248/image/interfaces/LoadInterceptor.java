package com.hss01248.image.interfaces;

import com.hss01248.image.config.SingleConfig;

public interface LoadInterceptor {


    /**
     * @param config
     * @return 是否继续下去
     */
    boolean intercept(SingleConfig config);
}
