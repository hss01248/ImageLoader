package com.hss01248.imagedebugger;

import java.io.File;

public abstract class IImgLocalPathGetter {

    public IImgLocalPathGetter(String url) {
        this.url = url;
    }

    public String url;

   public abstract void onGet(File file);

    public abstract  void onError(Throwable throwable);
}
