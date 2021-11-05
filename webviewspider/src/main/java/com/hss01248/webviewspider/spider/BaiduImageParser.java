package com.hss01248.webviewspider.spider;

import android.content.Context;
import android.webkit.ValueCallback;

import com.hss01248.webviewspider.basewebview.WebPageInfo;

import java.util.ArrayList;
import java.util.List;

public class BaiduImageParser implements IHtmlParser{
    @Override
    public String entranceUrl() {
        return "https://image.baidu.com/";
    }

    @Override
    public List<String> parseDetailPage(String html) {
        return new ArrayList<>();
    }

    @Override
    public void parList(Context context, WebPageInfo listWebPageInfo, ListToDetailImgsInfo listToDetailImgsInfo, ValueCallback<ListToDetailImgsInfo> infoCallback) {

    }

    @Override
    public String folderName() {
        return "baidu";
    }

    @Override
    public boolean interceptImage(String url) {
        return false;
    }
}
