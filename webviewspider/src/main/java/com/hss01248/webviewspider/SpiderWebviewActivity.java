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
    AgentWeb mAgentWeb;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        url = getIntent().getStringExtra("url");
        setContentView(R.layout.activity_web_spider);
        initWebView();
    }

    private void initWebView() {
        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent((LinearLayout) findViewById(R.id.root_ll), new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .setMainFrameErrorView(R.layout.pager_error,R.id.error_btn_retry)
                //.addJavascriptInterface()
                .createAgentWeb()
                .ready()
                .go(url);

    }

    @Override
    public void onBackPressed() {
        if(!mAgentWeb.back()){
            super.onBackPressed();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAgentWeb.destroy();
    }
}
