package com.hss01248.webviewspider.spider;

import android.content.Context;
import android.webkit.ValueCallback;

import com.hss01248.basewebview.WebPageInfo;

import java.util.List;

public interface IHtmlParser {


   default String resetDetailTitle(String title){
       return title;
   }

    String entranceUrl();


    List<String> parseDetailPage(String html);

    String folderName();

  default   long delayAfterOnFinished(){
        return 0;
    }

   default String subfolderName(String title,String url){
        return "";
    }

   default boolean hiddenFolder(){
        return false;
    }

    default boolean usePcAgent(){
       return false;
    }



    @Deprecated
    default void parseListAndDetail(Context context, WebPageInfo listWebPageInfo, ValueCallback<ListToDetailImgsInfo> infoCallback, ValueCallback<String> progressCallback){
        infoCallback.onReceiveValue(new ListToDetailImgsInfo());
    }

    default void parList(Context context, WebPageInfo listWebPageInfo,ListToDetailImgsInfo listToDetailImgsInfo, ValueCallback<ListToDetailImgsInfo> infoCallback){
        infoCallback.onReceiveValue(new ListToDetailImgsInfo());
    }


    boolean interceptImage(String url);
}
