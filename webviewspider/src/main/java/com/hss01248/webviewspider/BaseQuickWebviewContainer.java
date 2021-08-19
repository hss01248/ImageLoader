package com.hss01248.webviewspider;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.just.agentweb.AgentWeb;

public class BaseQuickWebviewContainer extends LinearLayout implements DefaultLifecycleObserver {


    public BaseQuickWebviewContainer(Context context) {
        super(context);
        init(context);
    }

    public BaseQuickWebviewContainer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BaseQuickWebviewContainer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BaseQuickWebviewContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        titleBar = new TitleBar(context);
        LinearLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,50);
        titleBar.setLayoutParams(params);
        addView(titleBar);
        titleBar.setLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    String url  = "";
    AgentWeb mAgentWeb;
    TitleBar titleBar;



    public void addLifecycle(LifecycleOwner lifecycleOwner){
        lifecycleOwner.getLifecycle().addObserver(this);
    }

    public void loadUrl(String url){
        this.url = url;
        if(mAgentWeb == null){
            initWebview();
        }
    }

    private void initWebview() {

    }

    private WebViewClient mWebViewClient;
    private WebChromeClient mWebChromeClient;
    private void initWebView() {

        //WebViewClient
        mWebViewClient=new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                //do you  work
                BaseQuickWebviewContainer.this.url = url;

                    //view.getSettings().setBlockNetworkImage(website.interceptImage(url));

            }




            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return super.shouldInterceptRequest(view, request);
            }


            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {

                return super.shouldInterceptRequest(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //currentUrl = url;

                // 获取页面内容

            }
        };
//WebChromeClient
        mWebChromeClient=new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                //do you work
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                Log.d("onReceivedTitle",title);

            }
        };

        debugWebViewClient = new DebugWebViewClient(mWebViewClient);
        debugWebViewClient.setLoggingEnabled(true);
        debugWebViewClient.setJsDebugPannelEnable(true);

        DebugWebChromeClient chromeClient = new DebugWebChromeClient(mWebChromeClient);
        chromeClient.setLoggingEnabled(true);



        preAgentWeb = AgentWeb.with(get)//传入Activity or Fragment
                .setAgentWebParent(this,
                        new LinearLayout.LayoutParams(-1, -1))//传入AgentWeb 的父控件 ，如果父控件为 RelativeLayout ， 那么第二参数需要传入 RelativeLayout.LayoutParams ,第一个参数和第二个参数应该对应。
                .useDefaultIndicator()// 使用默认进度条
                .defaultProgressBarColor() // 使用默认进度条颜色
                .setReceivedTitleCallback(new ChromeClientCallbackManager.ReceivedTitleCallback() {
                    @Override
                    public void onReceivedTitle(WebView view, String title) {
                        titleBar.setTitle(title);

                    }
                }) //设置 Web 页面的 title 回调
                .setWebChromeClient(chromeClient)
                .setWebViewClient(debugWebViewClient)
                .createAgentWeb()//
                .ready();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

    }
    AgentWeb.PreAgentWeb preAgentWeb;
    WebView webView;

    DebugWebViewClient debugWebViewClient;

    private void go(String url){
        if(mAgentWeb == null){
            mAgentWeb = preAgentWeb.go(url);
            webView = mAgentWeb.getWebCreator().getWebView();
            syncCookie(getContext(),url);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }else {
            mAgentWeb.getUrlLoader().loadUrl(url);
        }

    }

    private void syncCookie(Context context, String url) {
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







    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        if(mAgentWeb != null){
            mAgentWeb.getWebLifeCycle().onResume();
        }
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        if(mAgentWeb != null){
            mAgentWeb.getWebLifeCycle().onPause();
        }
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        if(mAgentWeb != null){
            mAgentWeb.getWebLifeCycle().onDestroy();
        }
    }

    public boolean onBackPressed(){
        if(mAgentWeb == null){
            return false;
        }
        return mAgentWeb.back();
    }


}
