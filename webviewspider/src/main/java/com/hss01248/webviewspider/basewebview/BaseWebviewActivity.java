package com.hss01248.webviewspider.basewebview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hss01248.webviewspider.R;


public class BaseWebviewActivity extends AppCompatActivity implements ISetWebviewHolder{


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
            setContentView(R.layout.default_webview_container);
           quickWebview = findViewById(R.id.root_ll);

            initWebview2(quickWebview);
           quickWebview.loadUrl(url);
        }
    }

    protected  void initWebview2(BaseQuickWebview quickWebview) {

    }

    @Override
    public void setWebviewHolder(BaseQuickWebview webview) {
        this.quickWebview = webview;
    }

    @Override
    public void onBackPressed() {
        if(quickWebview == null || !quickWebview.onBackPressed()){
            super.onBackPressed();
        }

    }
}
