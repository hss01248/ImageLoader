package com.hss01248.webviewspider;

import android.content.Context;

import androidx.annotation.Nullable;

import com.hss01248.webviewspider.spider.ListToDetailImgsInfo;

import java.util.List;
import java.util.Map;

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
    void showUrls(Context context,String pageTitle, final List<String> urls, @Nullable String downloadDir, boolean hideDir,boolean downloadImmediately);

    void showUrls(Context context, String pageTitle, Map<String,List<String>> titlesToImags, final List<String> urls, @Nullable String downloadDir, boolean hideDir,boolean downloadImmediately);

    void showFolder(Context context,String absolutePath);
}
