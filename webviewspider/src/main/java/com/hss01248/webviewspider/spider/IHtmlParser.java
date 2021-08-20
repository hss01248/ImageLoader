package com.hss01248.webviewspider.spider;

import java.util.List;

public interface IHtmlParser {


   default String resetTitle(String title){
       return title;
   }

    String entranceUrl();


    List<String> parseTargetImagesInHtml(String html);
}
