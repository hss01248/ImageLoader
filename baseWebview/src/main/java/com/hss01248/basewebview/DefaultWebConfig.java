package com.hss01248.basewebview;

import com.hss01248.basewebview.download.SystemDownloader;

/**
 * @Despciption todo
 * @Author hss
 * @Date 09/10/2022 12:21
 * @Version 1.0
 */
public class DefaultWebConfig implements WebviewInit {
    @Override
    public Class html5ActivityClass() {
        return BaseWebviewActivity.class;
    }

    @Override
    public IDownloader getIDownloader() {
        return new SystemDownloader();
    }



}
