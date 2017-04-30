package com.github.piasy.biv.view;

import java.io.File;

/**
 * Created by Administrator on 2017/4/30.
 */

public interface BigImageHierarchy {














    public void showContent(File image);
    public void showProgress(int progress);
    public void showError();
    public void showThumbnail();

    void onStart();


}
