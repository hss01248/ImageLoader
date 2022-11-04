package com.hss01248.webviewspider.spider;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.ValueCallback;

import com.blankj.utilcode.util.LogUtils;
import com.hss01248.basewebview.WebPageInfo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class BaiduImageParser implements IHtmlParser{

    static String Host = "https://image.baidu.com";
    @Override
    public String entranceUrl() {
        return "https://image.baidu.com/";
    }

    @Override
    public boolean usePcAgent() {
        return true;
    }

    @Override
    public long delayAfterOnFinished() {
        return 3000;
    }

    @Override
    public List<String> parseDetailPage(String html) {
        //img.sfc-image-content-ssr-img  src
        final ArrayList<String> urls = new ArrayList<>();
        try {
            Element element0 = Jsoup.parse(html).body();
            ///img.sfc-image-content-ssr-img  src
            //https://p3-search.byteimg.com/img/labis/0597604662ce7ba8b1079c49553e0f58~480x480.JPEG
            //https://p3-search.byteimg.com/img/labis/0597604662ce7ba8b1079c49553e0f58~0x0.JPEG
            Elements elements = element0.select("img.currentImg");
            //选择多个class,则不要有空格
            if(elements == null || elements.isEmpty()){
                LogUtils.w("baidu big image empty!!");
            }else {
                for (Element element : elements) {
                    String url =  element.attr("src");
                    if(!TextUtils.isEmpty(url)){
                        LogUtils.d("big 图 url : ", url);
                        urls.add(url);
                    }else {
                        LogUtils.w("big img的 src 属性为空",element);
                    }
                }
            }
        }catch (Throwable throwable){
            LogUtils.w(throwable);
        }
        return urls;
    }

    @Override
    public void parList(Context context, WebPageInfo listWebPageInfo, ListToDetailImgsInfo listToDetailImgsInfo, ValueCallback<ListToDetailImgsInfo> infoCallback) {
        final ArrayList<String> urls = new ArrayList<>();
        try {
            Element element0 = Jsoup.parse(listWebPageInfo.htmlSource).body();
            ///img.sfc-image-content-ssr-img  src
            //https://p3-search.byteimg.com/img/labis/0597604662ce7ba8b1079c49553e0f58~480x480.JPEG
            //https://p3-search.byteimg.com/img/labis/0597604662ce7ba8b1079c49553e0f58~0x0.JPEG
            Elements elements = element0.select("a.imgitem-title");
            //选择多个class,则不要有空格
            if(elements == null || elements.isEmpty()){
                LogUtils.w("baidu image empty!!");
            }else {
                for (Element element : elements) {
                    String url =  element.attr("href");
                    String title = element.attr("title");
                    if(!TextUtils.isEmpty(url)){
                        LogUtils.d("图 url : ", url);
                        urls.add(Host + url);
                        listToDetailImgsInfo.detailUrls.add(Host+url);
                        listToDetailImgsInfo.detailTitles.add(title);
                    }else {
                        LogUtils.w("img的 src 属性为空",element);
                    }
                }
            }
        }catch (Throwable throwable){
            LogUtils.w(throwable);
        }


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
