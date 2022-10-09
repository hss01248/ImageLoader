package com.hss01248.basewebview;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;

import java.net.URLDecoder;

import uk.co.alt236.webviewdebug.DebugWebChromeClient;
import uk.co.alt236.webviewdebug.DebugWebViewClient;

public class WebDebugger {

    public static boolean debug = true;
    DebugWebChromeClient debugWebChromeClient;
    DebugWebViewClient debugWebViewClient;

    TextView debugView;
    boolean showCookie;
    boolean enableUrlLine;
    long startMills;
    long cost;

    public  void setWebviewDebug(WebView webview){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(WebDebugger.debug);
        }
        if(!WebDebugger.debug){
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WebChromeClient webChromeClient = webview.getWebChromeClient();

            if(! (webChromeClient instanceof DebugWebChromeClient)){
                 debugWebChromeClient = new DebugWebChromeClient(webChromeClient);
                debugWebChromeClient.setLoggingEnabled(true);
                debugWebChromeClient.setLogKeyEventsEnabled(true);
                webview.setWebChromeClient(debugWebChromeClient);
            }

            WebViewClient webViewClient = webview.getWebViewClient();
            if(! (webViewClient instanceof DebugWebViewClient)){
                 debugWebViewClient = new DebugWebViewClient(webViewClient){
                     @Override
                     public void onPageStarted(WebView view, String url, Bitmap facIcon) {
                         super.onPageStarted(view, url, facIcon);
                         startMills = System.currentTimeMillis();
                         cost = 0;
                         if(debugView != null){
                             debugView.setText(url+"\nstart loading...");
                         }
                     }

                     @Override
                     public void onPageFinished(WebView view, String url) {
                         super.onPageFinished(view, url);
                         cost = System.currentTimeMillis() - startMills;
                         if(debugView != null){
                             debugView.setText(url+"\ncost:"+cost+"ms\n");
                         }
                     }

                     @Override
                     public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                         super.onReceivedError(view, request, error);
                     }

                     @Override
                     public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                         super.onReceivedError(view, errorCode, description, failingUrl);
                         cost = System.currentTimeMillis() - startMills;
                         if(debugView != null){
                             debugView.setText(failingUrl+"\ncost:"+cost+"ms\nerror:"+errorCode+",des:"+description+"\n");
                         }
                     }

                     @Override
                     public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                         super.onReceivedHttpError(view, request, errorResponse);
                     }

                     @Override
                     public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                         super.onReceivedSslError(view, handler, error);

                     }
                 };
                debugWebViewClient.setLoggingEnabled(true);
                //debugWebViewClient.setJsDebugPannelEnable(true);
                webview.setWebViewClient(debugWebViewClient);
            }
        }
    }

    public  void setWebviewDebugDebugLine(WebView webview){
        if (!debug) {
            return;
        }
        /*if(titleBar != null){
            titleBar.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    enableUrlLine = !enableUrlLine;
                    return true;
                }
            });
        }*/
        Activity activity = getActivityFromContext(webview.getContext());
        if(activity == null){
            return;
        }

        FrameLayout layout2 = activity.findViewById(android.R.id.content);
        if (layout2 == null) {
            return;
        }
        if (debugView == null) {
            debugView = new TextView(webview.getContext());
            debugView.setTextColor(Color.BLUE);
            int dp = SizeUtils.dp2px(20);
            debugView.setPadding(dp, dp/3, dp, dp);
            debugView.setTextSize(11);
            layout2.addView(debugView,
                    new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
            debugView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (showCookie) {
                        showCookie = false;
                        String text = debugView.getText().toString().trim();
                        if (text.contains("\ncookie:")) {
                            text = text.substring(0, text.indexOf("\ncookie:"));
                            debugView.setText(text);
                        }
                    } else {
                        showCookie = true;
                        String cookie = getCookieStr(webview);
                        String text = debugView.getText().toString().trim();
                        if (!text.contains("\ncookie:")) {
                            text = text + cookie;
                            debugView.setText(text);
                        }
                    }
                }
            });

            debugView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    debugView.setVisibility(View.GONE);
                    return true;
                }
            });
        } else {
            debugView.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(debugView.getText().toString())) {
                debugView.setText(webview.getUrl());
            }
        }
    }

    public static Activity getActivityFromContext(Context context) {
        if (context == null) {
            return null;
        }
        if (context instanceof Activity) {
            return (Activity) context;
        }
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }



    String getCookieStr(WebView webView) {
        try {
            String cookie = showCookie ? "\ncookie: " + CookieManager.getInstance()
                    .getCookie(webView.getUrl()) : "";
            cookie = URLDecoder.decode(cookie);
            cookie = cookie.replaceAll(";", ";\n");
            return cookie;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
