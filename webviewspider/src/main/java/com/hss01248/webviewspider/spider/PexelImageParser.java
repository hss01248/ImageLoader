package com.hss01248.webviewspider.spider;

import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.ToastUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class PexelImageParser implements IHtmlParser{
    @Override
    public String entranceUrl() {
        return "https://www.pexels.com/search/landscape/";
    }

    @Override
    public List<String> parseDetailPage(String html) {
        final ArrayList<String> urls = new ArrayList<>();
        try {
            Element element0 = Jsoup.parse(html).body();
            Elements elements = element0.select("div.BreakpointGrid_item__RSMyf > article > a > img");
            /*if(elements ==null || elements.isEmpty()){
                elements = element0.select("img");
            }*/
            //body > div.page-wrap > div.search > div.search__grid > div.photos > div:nth-child(2) > div:nth-child(8) > article > a.js-photo-link.photo-item__link > img
            if(elements == null || elements .isEmpty()){
                ToastUtils.showShort("no images!,please retry");
            }

            for (Element element: elements){
                String url = element.attr("src");

                Log.d("image",url);
                if(!TextUtils.isEmpty(url) ){
                    urls.add(url);
                }
            }
            if(urls.isEmpty()){
                ToastUtils.showShort("no images222!");
                return urls;
                //loadSource();
            }else {
                return urls;
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
        return urls;
    }

    @Override
    public String folderName() {
        return "pexels";
    }

    @Override
    public String subfolderName(String title, String url) {
       if(url.contains("/search/")){
           String type = url.substring(url.indexOf("/search/") +"/search/".length());
           if(type.endsWith("/")){
               type = type.substring(0,type.length()-1);
           }
           return URLDecoder.decode(type);
       }else {
           return "default";
       }
    }

    @Override
    public boolean interceptImage(String url) {
        return false;
    }
}
