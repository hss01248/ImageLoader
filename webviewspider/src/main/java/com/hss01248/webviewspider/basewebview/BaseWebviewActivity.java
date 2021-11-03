package com.hss01248.webviewspider.basewebview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;



public class BaseWebviewActivity extends AppCompatActivity {


    public static void start(Activity activity, String url){
        Intent intent = new Intent(activity, BaseWebviewActivity.class);
        intent.putExtra("url",url);
        activity.startActivity(intent);
    }

    protected String url  = "";
   protected BaseQuickWebview quickWebview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        url = getIntent().getStringExtra("url");
        if(!getIntent().getBooleanExtra(ISetWebviewHolder.setWebviewHolderByOutSide,false)){
           quickWebview = new BaseQuickWebview(this);
           setContentView(quickWebview);
           initWebview2(quickWebview);
           quickWebview.loadUrl(url);
        }
    }

    protected  void initWebview2(BaseQuickWebview quickWebview) {

    }
}
