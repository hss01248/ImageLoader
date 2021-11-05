package com.hss01248.webviewspider.spider;

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
    public String folderName() {
        return "google";
    }

    @Override
    public boolean interceptImage(String url) {
        return false;
    }
}
