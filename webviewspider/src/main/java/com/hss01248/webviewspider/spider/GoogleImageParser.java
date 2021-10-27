package com.hss01248.webviewspider.spider;

import java.util.ArrayList;
import java.util.List;

public class GoogleImageParser implements IHtmlParser{
    @Override
    public String entranceUrl() {
        return "https://www.google.com/search?q=%E5%A4%A9%E7%A9%BA";
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
