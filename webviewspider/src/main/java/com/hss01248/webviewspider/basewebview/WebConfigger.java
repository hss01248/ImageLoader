package com.hss01248.webviewspider.basewebview;

import android.content.Context;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.blankj.utilcode.util.NetworkUtils;

import uk.co.alt236.webviewdebug.DebugWebChromeClient;
import uk.co.alt236.webviewdebug.DebugWebViewClient;

public class WebConfigger {

    public static WebviewInit getInit() {
        return init;
    }

    static WebviewInit init;

    public static void init(WebviewInit init){
        WebConfigger.init = init;
    }




     static void config(WebView webView){
        init(webView);
        setDownloader(webView);
    }

    private static void setDownloader(WebView webView) {
        webView.setDownloadListener(new WebviewDownlader());
    }

    public static void syncCookie(WebView webView, String url) {
        CookieSyncManager.createInstance(webView.getContext());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        //cookieManager.removeSessionCookie();// 移除旧的[可以省略]
        /*List<HttpCookie> cookies = new PersistentCookieStore(context).getCookies();// 获取Cookie[可以是其他的方式获取]
        for (int i = 0; i < cookies.size(); i++) {
            HttpCookie cookie = cookies.get(i);
            String value = cookie.getName() + "=" + cookie.getValue();
            cookieManager.setCookie(url, value);
        }*/
        CookieSyncManager.getInstance().sync();// To get instant sync instead of waiting for the timer to trigger, the host can call this.
    }

    static void init(WebView webView) {
        WebSettings mWebSettings = webView.getSettings();
        mWebSettings.setDefaultTextEncodingName("utf-8");//字符编码UTF-8
        //支持获取手势焦点，输入用户名、密码或其他
        webView.requestFocusFromTouch();


        mWebSettings.setSupportZoom(false);//不支持缩放
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView,true);
        }
        //mWebSettings.setTextZoom(100);
        //设置自适应屏幕，两者合用
        mWebSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        mWebSettings.setUseWideViewPort(true); //将图片调整到适合webView的大小

        mWebSettings.setNeedInitialFocus(true); //当webView调用requestFocus时为webView设置节点

        mWebSettings.setLoadsImagesAutomatically(true);
        mWebSettings.setBlockNetworkImage(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }



        //调用JS方法.安卓版本大于17,加上注解 @JavascriptInterface
        mWebSettings.setJavaScriptEnabled(true);//支持javascript
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true);//支持通过js打开新的窗口
        mWebSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);//提高渲染等级
        mWebSettings.setSupportMultipleWindows(true);
        mWebSettings.setEnableSmoothTransition(true);
        webView.setFitsSystemWindows(true);
        //缓存数据 (localStorage)
        //有时候网页需要自己保存一些关键数据,Android WebView 需要自己设置

        /*if(WebDebugger.debug){
            mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        }else {
            if (NetworkUtils.isConnected()) {
                mWebSettings.setCacheMode(WebSettings.LOAD_DEFAULT);//根据cache-control决定是否从网络上取数据。
            } else {
                mWebSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);//没网，则从本地获取，即离线加载
            }
        }*/



        mWebSettings.setAllowFileAccess(true);
        mWebSettings.setSaveFormData(true);
        mWebSettings.setDomStorageEnabled(true);
        mWebSettings.setDatabaseEnabled(true);
        mWebSettings.setAppCacheEnabled(true);
        String appCachePath = webView.getContext().getCacheDir().getAbsolutePath();
        mWebSettings.setAppCachePath(appCachePath);
        //html中的_bank标签就是新建窗口打开，有时会打不开，需要加以下
        //然后 复写 WebChromeClient的onCreateWindow方法

        // for remote debug
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(WebDebugger.debug);
        }

    }


}
