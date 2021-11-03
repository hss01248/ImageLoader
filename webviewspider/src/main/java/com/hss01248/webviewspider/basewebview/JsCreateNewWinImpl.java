package com.hss01248.webviewspider.basewebview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.hss01248.activityresult.StartActivityUtil;
import com.hss01248.activityresult.TheActivityListener;

public class JsCreateNewWinImpl {



     Activity activity;

    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                onCreateWindow2( view,  isDialog,  isUserGesture,  resultMsg);
            }
        });
        return true;
    }

    private void onCreateWindow2(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        Intent intent = new Intent(ActivityUtils.getTopActivity(),WebConfigger.getInit().html5ActivityClass());
        intent.putExtra(ISetWebviewHolder.setWebviewHolderByOutSide,true);
        StartActivityUtil.startActivity(ActivityUtils.getTopActivity(),
                WebConfigger.getInit().html5ActivityClass(),intent,
                false, new TheActivityListener<AppCompatActivity>(){

                    @Override
                    protected void onActivityCreated(@NonNull AppCompatActivity activity, @Nullable Bundle savedInstanceState) {
                        super.onActivityCreated(activity, savedInstanceState);
                        if(activity instanceof ISetWebviewHolder){
                            BaseQuickWebview quickWebview = new BaseQuickWebview(activity);
                            ISetWebviewHolder holder = (ISetWebviewHolder) activity;

                            holder.setWebviewHolder(quickWebview);
                            activity.setContentView(quickWebview);

                            //相当于load url
                           WebView  newWebView = quickWebview.getWebView();
                            LogUtils.w("webdebug", "onCreateWindow:isDialog:" + isDialog +
                                    ",isUserGesture:" + isUserGesture + ",msg:" + resultMsg + "\n chromeclient:" + this+","+newWebView);
                            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                            transport.setWebView(newWebView);
                            resultMsg.sendToTarget();


                            //给新打开的webview响应closewindow用
                            quickWebview.jsCreateNewWin.activity = activity;
                        }
                    }
                });
    }


    /**
     * 子窗口收到父窗口的window.close()方法,或者自己的window.close()方法
     * @param window
     */
    public void onCloseWindow(WebView window) {
        if(activity != null){
            activity.finish();
        }

    }
}
