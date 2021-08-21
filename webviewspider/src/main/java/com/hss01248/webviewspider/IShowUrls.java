package com.hss01248.webviewspider;

import android.content.Context;

import androidx.annotation.Nullable;

import java.util.List;

public interface IShowUrls {


    /** api project(':imagelist')
     *  ImageListView listView = new ImageListView(SpiderWebviewActivity.this);
     *                     listView.showUrls(parser.getClass().getSimpleName(),list, getExternalFilesDir(parser.getClass().getSimpleName()).getAbsolutePath(),false);
     *                     ImageMediaCenterUtil.showViewAsDialog(listView);
     * @param context
     * @param pageTitle
     * @param urls
     * @param downloadDir
     * @param hideDir
     */
    void showUrls(Context context,String pageTitle, final List<String> urls, @Nullable String downloadDir, boolean hideDir);
}
