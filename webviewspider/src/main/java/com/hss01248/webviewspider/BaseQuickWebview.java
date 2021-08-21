package com.hss01248.webviewspider;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;

import android.webkit.WebView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.hss01248.webviewspider.basewebview.WebConfigger;
import com.hss01248.webviewspider.basewebview.WebDebugger;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.WebViewClient;

import org.apache.commons.lang3.StringEscapeUtils;


public class BaseQuickWebview extends LinearLayout implements DefaultLifecycleObserver {



    String currentUrl = "";

    public AgentWeb getAgentWeb() {
        return mAgentWeb;
    }

    AgentWeb mAgentWeb;
    TitleBar titleBar;
    AgentWeb.PreAgentWeb preAgentWeb;
    WebView webView;
    WebDebugger debugger;
    String source;

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
        titleBar = new TitleBar(context);
        LinearLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,TitleBar.dip2px(50));
        titleBar.setLayoutParams(params);
        addView(titleBar);
        titleBar.setLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        Activity activity = WebDebugger.getActivityFromContext(context);
        if(activity instanceof LifecycleOwner){
            LifecycleOwner owner = (LifecycleOwner) activity;
            addLifecycle(owner);
        }
        initWebView();
    }

    public void getSource(ValueCallback<String> valueCallback){
        if(!TextUtils.isEmpty(source)){
            valueCallback.onReceiveValue(source);
            return;
        }
        loadSource(valueCallback);
    }

    public static BaseQuickWebview loadHtml(Context context,String url,ValueCallback<String> sourceLoadListener){
        BaseQuickWebview quickWebview = new BaseQuickWebview(context);
        quickWebview.setSourceLoadListener(sourceLoadListener);
        quickWebview.needBlockImageLoad = true;
        quickWebview.loadUrl(url);
        Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(quickWebview);
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(quickWebview.getAgentWeb() != null){
                    quickWebview.getAgentWeb().destroy();
                }
            }
        });
        return quickWebview;

    }
    ValueCallback<String> sourceLoadListener;
    boolean needBlockImageLoad;
    public void setSourceLoadListener(ValueCallback<String> sourceLoadListener){
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
        String script = "javascript:document.getElementsByTagName('html')[0].innerHTML";
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
                    source = "<html>"+source +"</html>";
                    Log.w("source",Thread.currentThread().getName() +source);
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
        go(url);
    }

    private void initWebView() {
        preAgentWeb = AgentWeb.with((Activity) getContext())//传入Activity or Fragment
                .setAgentWebParent(this,
                        new LayoutParams(-1, -1))
                //传入AgentWeb 的父控件 ，如果父控件为 RelativeLayout ， 那么第二参数需要传入 RelativeLayout.LayoutParams ,第一个参数和第二个参数应该对应。
                .useDefaultIndicator()// 使用默认进度条
                .setWebViewClient(new WebViewClient(){
                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        super.onPageStarted(view, url, favicon);
                        source = "";
                        currentUrl = url;
                        if(needBlockImageLoad){
                            view.getSettings().setBlockNetworkImage(needBlockImageLoad);
                        }
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        loadSource(new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                if(sourceLoadListener != null){
                                    sourceLoadListener.onReceiveValue(source);
                                }
                            }
                        });
                    }
                })
                .setWebChromeClient(new com.just.agentweb.WebChromeClient(){
                    @Override
                    public void onReceivedTitle(WebView view, String title) {
                        super.onReceivedTitle(view, title);
                        titleBar.setTitle(title);
                    }
                })
                .setMainFrameErrorView(R.layout.pager_error,R.id.error_btn_retry)
                .createAgentWeb()//
                .ready();

    }



    private void go(String url){
        if(mAgentWeb == null){
            mAgentWeb = preAgentWeb.go(url);
            webView = mAgentWeb.getWebCreator().getWebView();
            WebConfigger.config(webView,url);
            debugger =  new WebDebugger();
            debugger.setWebviewDebug(webView);
            //debugger.setWebviewDebugDebugLine(webView,titleBar);
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
