package com.hss01248.basewebview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hss01248.basewebview.databinding.TitlebarForWebviewBinding;
import com.hss01248.basewebview.dom.FileChooseImpl;
import com.hss01248.basewebview.dom.JsCreateNewWinImpl;
import com.hss01248.basewebview.dom.JsPermissionImpl;
import com.hss01248.basewebview.menus.DefaultMenus;
import com.hss01248.pagestate.PageStateConfig;
import com.hss01248.pagestate.PageStateManager;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.AgentWebUIControllerImplBase;
import com.just.agentweb.WebViewClient;

import org.apache.commons.lang3.StringEscapeUtils;



public class BaseQuickWebview extends LinearLayout implements DefaultLifecycleObserver {



    String currentUrl = "";

    public AgentWeb getAgentWeb() {
        return mAgentWeb;
    }

    AgentWeb mAgentWeb;
    long delayAfterOnFinish;
    AgentWeb.PreAgentWeb preAgentWeb;

    public void setShowRightMenus(IShowRightMenus showRightMenus) {
        this.showRightMenus = showRightMenus;
    }

    IShowRightMenus showRightMenus = new DefaultMenus();

    public WebView getWebView() {
        return webView;
    }

    WebView webView;

    public String getCurrentTitle() {
        return currentTitle;
    }

    String currentTitle;

    public String getCurrentUrl() {
        return currentUrl;
    }

    WebDebugger debugger;
    String source;

    public WebPageInfo getInfo() {
        return info;
    }

    WebPageInfo info;

    public BaseQuickWebview(Context context) {
        super(context);
        init(context);
    }

    public BaseQuickWebview(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BaseQuickWebview(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BaseQuickWebview(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        info = new WebPageInfo();
        initTitlebar(context);

        Activity activity = WebDebugger.getActivityFromContext(context);
        if(activity instanceof LifecycleOwner){
            LifecycleOwner owner = (LifecycleOwner) activity;
            addLifecycle(owner);
        }
        initWebView();
    }

    public TitlebarForWebviewBinding getTitleBar() {
        return titleBar;
    }

    TitlebarForWebviewBinding titleBar;

    public WebViewTitlebarHolder getTitlebarHolder() {
        return titlebarHolder;
    }

    WebViewTitlebarHolder titlebarHolder;
    private void initTitlebar(Context context) {

        Activity activity = WebDebugger.getActivityFromContext(context);
        BarUtils.setStatusBarColor(activity, Color.WHITE);
        BarUtils.setStatusBarLightMode(activity,true);

        titlebarHolder = new WebViewTitlebarHolder(this);
        titleBar = titlebarHolder.binding;
        titleBar.getRoot().setPadding(0,BarUtils.getStatusBarHeight(),0,0);

        addView(titleBar.getRoot());

        titlebarHolder.assignDataAndEventReal(this);

    }

    protected void showMenu() {
        if(showRightMenus != null){
            showRightMenus.showMenus(webView,this);
        }else {
            ToastUtils.showLong("show menu");
        }
    }

    public void getSource(ValueCallback<String> valueCallback){
        if(!TextUtils.isEmpty(source)){
            valueCallback.onReceiveValue(source);
            return;
        }
        loadSource(valueCallback);
    }

    public static BaseQuickWebview loadHtml(Context context,String url,long delayAfterOnFinish,ValueCallback<WebPageInfo> sourceLoadListener){
        BaseQuickWebview quickWebview = new BaseQuickWebview(context);
        quickWebview.needBlockImageLoad = true;
        quickWebview.delayAfterOnFinish = delayAfterOnFinish;
/*
        Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setTextView(quickWebview);
        dialog.show();
        WindowManager.LayoutParams attributes = dialog.getWindow().getAttributes();
        attributes.height = ScreenUtils.getAppScreenHeight()/2;
        attributes.gravity = Gravity.BOTTOM;
        dialog.getWindow().setAttributes(attributes);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });*/



        quickWebview.setSourceLoadListener(new ValueCallback<WebPageInfo>() {
            @Override
            public void onReceiveValue(WebPageInfo value) {
                sourceLoadListener.onReceiveValue(value);
                if(quickWebview.getAgentWeb() != null){
                    quickWebview.getAgentWeb().destroy();
                }
            }
        });
        quickWebview.loadUrl(url);
        return quickWebview;

    }
    ValueCallback<WebPageInfo> sourceLoadListener;


    public void setNeedBlockImageLoad(boolean needBlockImageLoad) {
        this.needBlockImageLoad = needBlockImageLoad;
    }

    boolean needBlockImageLoad;
    public void setSourceLoadListener(ValueCallback<WebPageInfo> sourceLoadListener){
       this.sourceLoadListener = sourceLoadListener;
    }


    public void loadSource(ValueCallback<String> valueCallback){
        if(webView == null){
            Log.w("loadSource","webview is null");
            return;
        }
//        if(TextUtils.isEmpty(source)){
//            valueCallback.onReceiveValue(source);
//            return;
//        }
        //String script = "javascript:document.getElementsByTagName('html')[0].innerHTML";
        String script = "javascript:document.getElementsByTagName('body')[0].innerHTML";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //在主线程执行,耗时好几s
            webView.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    source = StringEscapeUtils.unescapeJava(value);
                    if(source.startsWith("\"")){
                        source = source.substring(1);
                    }
                    if(source.endsWith("\"")){
                        source = source.substring(0,source.length()-1);
                    }
                    //source = "<html>"+source +"</html>";
                    source = "<body>"+source +"</body>";
                    LogUtils.v(source);
                    info.htmlSource = source;
                    valueCallback.onReceiveValue(source);
                }
            });
        }
    }

    boolean hasAdd;
    private void addLifecycle(LifecycleOwner lifecycleOwner){
        if(hasAdd){
            return;
        }
        lifecycleOwner.getLifecycle().addObserver(this);
        hasAdd = true;
    }

    public void loadUrl(String url){
        if(url.startsWith("http")){
            go(url);
        }else {
            //调用百度/谷歌搜索
            String url2 = "https://www.baidu.com/s?wd="+url;
            go(url2);
        }
    }



    PageStateManager stateManager;

    public JsCreateNewWinImpl jsCreateNewWin = new JsCreateNewWinImpl();

    private void initWebView() {
        preAgentWeb = AgentWeb.with((Activity) getContext())//传入Activity or Fragment
                .setAgentWebParent(this,
                        new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                //传入AgentWeb 的父控件 ，如果父控件为 RelativeLayout ， 那么第二参数需要传入 RelativeLayout.LayoutParams ,第一个参数和第二个参数应该对应。
                .useDefaultIndicator()// 使用默认进度条
                .setAgentWebUIController(new AgentWebUIControllerImplBase(){
                    @Override
                    public void onMainFrameError(WebView view, int errorCode, String description, String failingUrl) {
                        super.onMainFrameError(view, errorCode, description, failingUrl);
                        if(stateManager != null){
                            stateManager.showError(errorCode+"\n"+description+"\n on url:"+failingUrl);
                        }
                    }

                    @Override
                    public void onShowMainFrame() {
                        super.onShowMainFrame();
                        if(stateManager != null){
                            stateManager.showContent();
                        }

                    }
                })
                .setWebViewClient(new WebViewClient(){
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                        //return new OkhttpProxyForWebview().shouldInterceptRequest(view,request);
                        return  super.shouldInterceptRequest(view, request);
                    }

                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        super.onPageStarted(view, url, favicon);
                        source = "";
                        currentUrl = url;
                        currentTitle = "";
                        info.htmlSource = "";
                        info.url = url;
                        info.title = "";
                        if(needBlockImageLoad){
                            view.getSettings().setBlockNetworkImage(needBlockImageLoad);
                        }
                    }

                    /**
                     * onReceivedHttpError+main frame后,也会走到onPageFinished
                     * @param view
                     * @param url
                     */
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loadSource(new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String value) {
                                        if(sourceLoadListener != null){
                                            sourceLoadListener.onReceiveValue(info);
                                        }
                                    }
                                });
                            }
                        },delayAfterOnFinish);
                        if("about:blank".equals(url)){
                            if(stateManager != null){
                                stateManager.showError("not url : \n"+originalUrl );
                            }
                        }

                    }
                    @Override
                    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                        super.onReceivedError(view, errorCode, description, failingUrl);
                        if(stateManager != null){
                            stateManager.showError(errorCode+"\n"+description+"\n on url:"+failingUrl);
                        }

                    }

                    @Override
                    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                        super.onReceivedHttpError(view, request, errorResponse);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if(request.isForMainFrame()){
                                if(stateManager != null){
                                    stateManager.showError(errorResponse.getStatusCode()+"\n"+errorResponse.getReasonPhrase()+"\n on url:"+request.getUrl());
                                }

                            }
                        }
                    }

                    @Override
                    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                        super.onReceivedSslError(view, handler, error);
                        if(stateManager != null){
                            stateManager.showError("SslError:\n"+error.toString());
                        }

                    }
                })
                .setWebChromeClient(new com.just.agentweb.WebChromeClient(){

                    @Override
                    public void onReceivedTitle(WebView view, String title) {
                        super.onReceivedTitle(view, title);
                        //titleBar.tvTitle.setText(title);
                        titleBar.tvTitle.setText(title);
                        titleBar.tvTitle.requestFocus();
                        currentTitle = title;
                        info.title = title;
                    }

                    @Override
                    public void onReceivedIcon(WebView view, Bitmap icon) {
                        super.onReceivedIcon(view, icon);
                        if(titlebarHolder.isFullWebBrowserMode){
                            titleBar.ivBack.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            titleBar.ivBack.setImageBitmap(icon);
                        }

                    }

                    @Override
                    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                        return jsCreateNewWin.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
                    }

                    @Override
                    public void onCloseWindow(WebView window) {
                        jsCreateNewWin.onCloseWindow(window);
                    }
                })
                .useMiddlewareWebChrome(new JsPermissionImpl())
                .useMiddlewareWebChrome(new FileChooseImpl())
                //.useMiddlewareWebChrome(new JsNewWindowImpl())
                //.useMiddlewareWebChrome(new VideoFullScreenImpl())
              // .setMainFrameErrorView(R.layout.pager_error,R.id.error_btn_retry)
                //.setMainFrameErrorView(errorLayout)
                .createAgentWeb()//
                .ready();

        mAgentWeb = preAgentWeb.get();

        webView = mAgentWeb.getWebCreator().getWebView();
        stateManager = PageStateManager.initWhenUse(mAgentWeb.getWebCreator().getWebParentLayout(), new PageStateConfig() {

            @Override
            public boolean isFirstStateLoading() {
                return false;
            }

            @Override
            public void onRetry(View retryView) {
                stateManager.showContent();
                webView.reload();

            }
        });
        WebConfigger.config(webView);
        debugger =  new WebDebugger();
        debugger.setWebviewDebug(webView);

    }



    String originalUrl = "";
    private void go(String url){
        originalUrl = url;
        info.url = url;
        WebConfigger.syncCookie(webView,url);
        if(mAgentWeb == null){
            LogUtils.w("mAgentWeb == null");
            //mAgentWeb = preAgentWeb.go(url);
        }else {
            mAgentWeb.getUrlLoader().loadUrl(url);
        }


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
