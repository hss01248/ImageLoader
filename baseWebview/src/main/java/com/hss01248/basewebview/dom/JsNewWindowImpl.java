package com.hss01248.basewebview.dom;

import android.os.Message;
import android.webkit.WebView;

import com.just.agentweb.MiddlewareWebChromeBase;

/**
 * @Despciption todo
 * @Author hss
 * @Date 25/11/2022 10:10
 * @Version 1.0
 */
public class JsNewWindowImpl extends MiddlewareWebChromeBase {
    JsCreateNewWinImpl jsCreateNewWin = new JsCreateNewWinImpl();
    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        return jsCreateNewWin.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
    }

    @Override
    public void onCloseWindow(WebView window) {
        jsCreateNewWin.onCloseWindow(window);
    }

}
