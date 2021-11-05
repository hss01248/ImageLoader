package com.hss01248.webviewspider.spider;

import android.text.TextUtils;

import com.blankj.utilcode.util.LogUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
//https://www.jianshu.com/p/b378994c8484
public class ToutiaoImageParser implements IHtmlParser{
    @Override
    public String entranceUrl() {
        return "https://so.toutiao.com/search?keyword=%E6%B7%B1%E5%9C%B3&pd=atlas&source=input&original_source=&in_ogs=";
    }
    //https://so.toutiao.com/search?dvpf=pc&source=input&keyword=%E6%B7%B1%E5%9C%B3

    @Override
    public List<String> parseDetailPage(String html) {

        final ArrayList<String> urls = new ArrayList<>();
        try {
            Element element0 = Jsoup.parse(html).body();
            ///dic.cs-image-img cs-image-fill  'data-backup-src'
            //https://p3-search.byteimg.com/img/labis/0597604662ce7ba8b1079c49553e0f58~480x480.JPEG
            //https://p3-search.byteimg.com/img/labis/0597604662ce7ba8b1079c49553e0f58~0x0.JPEG
            Elements elements = element0.select("div.cs-image-img.cs-image-fill");
            //选择多个class,则不要有空格
            if(elements == null || elements.isEmpty()){
                LogUtils.w("toutiao image empty!!");
            }else {
                for (Element element : elements) {
                   String url =  element.attr("data-backup-src");
                   if(!TextUtils.isEmpty(url)){
                       LogUtils.d("小图 url : ", url);
                       int idx0 = url.lastIndexOf("~");
                       int idx1 = url.lastIndexOf(".");
                       if(idx0> 0 && idx1 > 0){
                           url = url.substring(0,idx0+1) + "0x0"+url.substring(idx1);
                           LogUtils.d("推算 原图 url : ", url);
                           urls.add(url);
                       }else {
                           LogUtils.w("图片url不符合规则: ");
                       }
                   }else {
                       LogUtils.w("div的 data-backup-src 属性为空",element);
                   }
                }
            }
        }catch (Throwable throwable){
            LogUtils.w(throwable);
        }
        return urls;
    }

    @Override
    public String folderName() {
        return "toutiao";
    }

    @Override
    public boolean interceptImage(String url) {
        return false;
    }
}
