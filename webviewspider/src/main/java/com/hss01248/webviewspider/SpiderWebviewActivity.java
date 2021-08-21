package com.hss01248.webviewspider;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.ValueCallback;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.hss01248.ui.pop.list.PopList;
import com.hss01248.webviewspider.spider.IHtmlParser;
import com.hss01248.webviewspider.spider.PexelImageParser;
import com.just.agentweb.AgentWeb;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpiderWebviewActivity extends AppCompatActivity {

    public static void setShowUrls(IShowUrls iShowUrls) {
        SpiderWebviewActivity.iShowUrls = iShowUrls;
    }

    static IShowUrls iShowUrls;

    public static void start(Activity activity,String url){
        Intent intent = new Intent(activity,SpiderWebviewActivity.class);
        intent.putExtra("url",url);
        activity.startActivity(intent);
    }

    String url  = "";
    BaseQuickWebview quickWebview;
    Button button;

    static Map<String,IHtmlParser> parsers = new HashMap<>();
    static {
        parsers.put(new PexelImageParser().entranceUrl(),new PexelImageParser());
    }
    IHtmlParser parser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        url = getIntent().getStringExtra("url");
        parser = parsers.get(url);
        setContentView(R.layout.activity_web_spider);
        button = findViewById(R.id.btn_float);
        initWebView();
    }

    private void initWebView() {
        quickWebview = findViewById(R.id.root_ll);
        //quickWebview.addLifecycle(this);
        quickWebview.loadUrl(url);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu();
            }
        });

    }

    private void showMenu() {
        List<String> menus = new ArrayList<>();
        menus.add("显示html源码");
        menus.add("展示当前页面所有图片");
        PopList.showPop(this, -1, button, menus, new PopList.OnItemClickListener() {
            @Override
            public void onClick(int position, String str) {
                if(position == 0){
                    showSource();
                }else if(position == 1){
                    parseUrlsAndShow();
                }

            }
        });

    }

    private void parseUrlsAndShow() {
        quickWebview.getSource(new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                List<String> list = parser.parseTargetImagesInHtml(value);
                if(list != null && !list.isEmpty()){
                    if(iShowUrls != null){
                        iShowUrls.showUrls(SpiderWebviewActivity.this,parser.getClass().getSimpleName(),list, getExternalFilesDir(parser.getClass().getSimpleName()).getAbsolutePath(),false);
                    }

                }
            }
        });
    }

    private void showSource() {
        quickWebview.getSource(new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                new AlertDialog.Builder(SpiderWebviewActivity.this)
                        .setTitle("source")
                        .setMessage(value)
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).create().show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(quickWebview == null || !quickWebview.onBackPressed()){
            super.onBackPressed();
        }

    }
}
