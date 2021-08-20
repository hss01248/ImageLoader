package com.hss01248.webviewspider.basewebview;

import android.content.Context;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import uk.co.alt236.webviewdebug.DebugWebChromeClient;
import uk.co.alt236.webviewdebug.DebugWebViewClient;

public class WebConfigger {

    public static void config(WebView webView,String url){
        syncCookie(webView.getContext(),url);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
    }

    private static void syncCookie(Context context, String url) {
        CookieSyncManager.createInstance(context);
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


}
