package com.hss01248.webviewspider.spider;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.ValueCallback;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.hss.downloader.download.DownloadInfo;
import com.hss.downloader.download.DownloadInfoUtil;
import com.hss01248.basewebview.BaseQuickWebview;
import com.hss01248.basewebview.WebPageInfo;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ListParser {

   public static void parseListAndDetail(Context context, WebPageInfo listWebPageInfo, IHtmlParser htmlParser, ValueCallback<ListToDetailImgsInfo> callback, ValueCallback<String> progressCallback){
       ListToDetailImgsInfo listToDetailImgsInfo = new ListToDetailImgsInfo();
       listToDetailImgsInfo.listUrl = listWebPageInfo.url;



       try {

           htmlParser.parList(context,listWebPageInfo,listToDetailImgsInfo,callback);

           Iterator<String> iterator = listToDetailImgsInfo.detailUrls.iterator();
           Iterator<String> titleIterrator = listToDetailImgsInfo.detailTitles.iterator();
           while (iterator.hasNext()){
               String path = iterator.next();
               try {
                   titleIterrator.next();
               }catch (Throwable e){
                   e.printStackTrace();
               }

               DownloadInfo load = DownloadInfoUtil.getDao().load(path);
               if(load != null){
                   iterator.remove();
                   try {
                       titleIterrator.remove();
                   }catch (Throwable throwable){
                       throwable.printStackTrace();
                   }

               }
           }
           final int[] count = {listToDetailImgsInfo.detailUrls.size()};
           //一百个url,限制并发数为1
           int idx = 0;
           loadHtml(context,listToDetailImgsInfo,  htmlParser,callback, listToDetailImgsInfo.imagUrls, listToDetailImgsInfo.detailUrls, listToDetailImgsInfo.detailTitles, count, idx,progressCallback);

       }catch (Throwable throwable){
           throwable.printStackTrace();
           // callback.onReceiveValue(listToDetailImgsInfo);
       }
   }

   static Handler handler = new Handler(Looper.getMainLooper());

    private static void loadHtml(Context context, ListToDetailImgsInfo listToDetailImgsInfo, IHtmlParser htmlParser,ValueCallback<ListToDetailImgsInfo> callback,
                          List<String> urls, List<String> paths, List<String> titles, int[] count, int idx, ValueCallback<String> progressCallback) {
        //需要单独webview
                    /*Document doc = Jsoup.connect(url1)
                            .timeout(15000)
                            .header("Refer", url)
                            .userAgent(System.getProperty("http.agent"))
                            .get();
                   String htmlStr =  doc.outerHtml();*/
        try {
            if(idx == paths.size()){
                callback.onReceiveValue(listToDetailImgsInfo);
                return;
            }
            int sleep = (new Random().nextInt(3)+1)*1000;
            Log.v("caol","sleep "+sleep/1000+"s,then go :"+ idx+", "+paths.get(idx));// Index: 54, Size: 54
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Handler handler0 = new Handler(Looper.getMainLooper());
                    Runnable runnable0  = new Runnable(){

                        @Override
                        public void run() {
                            LogUtils.w("caol","30s timeout parse html source "+sleep/1000+"s,then go :"+ idx+", "+paths.get(idx));// Index: 54, Size: 54
                            count[0]--;
                            progressCallback.onReceiveValue(idx+"/"+paths.size()+",图片数量:"+urls.size());
                            //if(count[0] ==paths.size() -2){//测试
                            if(count[0] == 1){
                                callback.onReceiveValue(listToDetailImgsInfo);
                            }else {
                                loadHtml(context,listToDetailImgsInfo,htmlParser, callback, urls, paths, titles, count, idx+1, progressCallback);
                            }
                        }
                    };
                    handler0.postDelayed(runnable0,30000);
                            BaseQuickWebview.loadHtml(context, paths.get(idx),htmlParser.delayAfterOnFinished(), new ValueCallback<WebPageInfo>() {
                        @Override
                        public void onReceiveValue(WebPageInfo info) {
                            try {
                                LogUtils.d("caol"," parse html source less then 30s,  removeCallbacks:"+idx+", "+paths.get(idx));
                                handler0.removeCallbacks(runnable0);
                                count[0]--;
                                List<String> imageUrls = htmlParser.parseDetailPage(info.htmlSource);
                                Log.v("caol",titles.get(idx)+"-imagesize "+imageUrls.size()+",position:"+ idx +",dao:"+count[0]);
                                listToDetailImgsInfo.titlesToImags.put(titles.get(idx),imageUrls);
                                urls.addAll(imageUrls);
                            }catch (Throwable throwable){
                                LogUtils.w(throwable);
                            }

                            ThreadUtils.getIoPool().execute(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        DownloadInfo load = new DownloadInfo();
                                        load.url = info.url;
                                        load.name = "page-"+titles.get(idx);
                                        load.status = DownloadInfo.STATUS_SUCCESS;
                                        DownloadInfoUtil.getDao().insertOrReplace(load);
                                    }catch (Throwable throwable){
                                        throwable.printStackTrace();
                                    }
                                }
                            });


                            progressCallback.onReceiveValue(idx+"/"+paths.size()+",图片数量:"+urls.size());
                            //if(count[0] ==paths.size() -2){//测试
                            if(count[0] == 1){
                                callback.onReceiveValue(listToDetailImgsInfo);
                            }else {
                                loadHtml(context,listToDetailImgsInfo,htmlParser, callback, urls, paths, titles, count, idx+1, progressCallback);
                            }
                        }
                    });
                }
            },sleep);
        } catch (Exception e) {
            LogUtils.w(e);
            //loadHtml(context,listToDetailImgsInfo,htmlParser, callback, urls, paths, titles, count, idx+1, progressCallback);
        }

    }
}
