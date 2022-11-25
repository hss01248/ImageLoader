package com.hss01248.basewebview.dom;

import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebView;

import com.just.agentweb.MiddlewareWebChromeBase;

/**
 * @author: Administrator
 * @date: 2022/11/24
 * @desc: //无须实现,agentweb内部已经实现
 */
public class JsPopImpl extends MiddlewareWebChromeBase {

    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        return super.onJsAlert(view, url, message, result);
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
        return super.onJsConfirm(view, url, message, result);
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        return super.onJsPrompt(view, url, message, defaultValue, result);
    }

   /*
   //// 显示一个对话框让用户选择是否离开当前页面
   @Override
    public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
        return super.onJsBeforeUnload(view, url, message, result);
    }

    @Override
    public boolean onJsTimeout() {
        return super.onJsTimeout();
    }*/
}
