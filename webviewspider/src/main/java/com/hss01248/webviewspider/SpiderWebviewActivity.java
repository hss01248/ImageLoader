package com.hss01248.webviewspider;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.just.agentweb.AgentWeb;

public class SpiderWebviewActivity extends AppCompatActivity {

    public static void start(Activity activity,String url){
        Intent intent = new Intent(activity,SpiderWebviewActivity.class);
        intent.putExtra("url",url);
        activity.startActivity(intent);
    }

    String url  = "";
    BaseQuickWebview quickWebview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        url = getIntent().getStringExtra("url");
        setContentView(R.layout.activity_web_spider);
        initWebView();
    }

    private void initWebView() {
        quickWebview = findViewById(R.id.root_ll);
        quickWebview.addLifecycle(this);
        quickWebview.loadUrl(url);

    }

    @Override
    public void onBackPressed() {
        if(quickWebview == null || !quickWebview.onBackPressed()){
            super.onBackPressed();
        }

    }
}
