package com.hss01248.webviewspider;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hss01248.mybrowser.view.TitleBar;
import com.hss01248.mybrowser.web.GoogleImage;
import com.hss01248.mybrowser.web.IWebsite;
import com.hss01248.mybrowser.web.WebsiteAdapter;
import com.hss01248.mybrowser.web.XiaoCaoWebSite;
import com.hss01248.mybrowser.web.XxPicsWebSite;
import com.hss01248.net.wrapper.MyLog;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.ChromeClientCallbackManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.co.alt236.webviewdebug.DebugWebChromeClient;
import uk.co.alt236.webviewdebug.DebugWebViewClient;


public class MainActivity extends AppCompatActivity {

    AgentWeb mAgentWeb;
    TitleBar titleBar;
    private WebViewClient mWebViewClient;
    private WebChromeClient mWebChromeClient;
    FloatingActionButton fab;
    WebView webView;

    private static String currentUrl;
    private String title;

    IWebsite website;
    RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.activity_main);
        fab = findViewById(R.id.fbtn);

        initRecyclerview();

        initTitleBar();
        initWebView();
        initEvent();

        XXPermissions.with(this)
                .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        Toast.makeText(MainActivity.this, all+"-These permissions are : "+ Arrays.toString(permissions.toArray()), Toast.LENGTH_LONG).show();
                    }
                });

       /* MyPermission.askExternalStorage(new MyPermission.PermissionListener() {
            @Override
            public void onGranted(List<String> permissions) {

            }

            @Override
            public void onDenied(List<String> permissions) {

            }
        });*/
        XiaoCaoWebSite.splitDir(this);



    }

    private void initRecyclerview() {
        recyclerView = findViewById(R.id.rcv_websites);
        final List<IWebsite> websites = new ArrayList<>();
        websites.add(new XiaoCaoWebSite());
        websites.add(new XxPicsWebSite());
        websites.add(new GoogleImage());
        WebsiteAdapter adapter = new WebsiteAdapter(R.layout.item_website,websites);
        recyclerView.setLayoutManager(new GridLayoutManager(this,3));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                website = websites.get(position);
                recyclerView.setVisibility(View.GONE);
                go(website.entranceUrl());

            }
        });
    }

    public void setHtml( String html) {
        html = "<html xmlns=\"http://www.w3.org/1999/xhtml\">"+html+"</html>";
        this.html = html;
        //showHtml(html);
        MyLog.d(html);

    }

    private void showHtml(final String html) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View view = View.inflate(MainActivity.this,R.layout.html,null);
                TextView textView = view.findViewById(R.id.tv_html);
                textView.setText(html);
                new AlertDialog.Builder(MainActivity.this)
                        .setView(view)
                        .create().show();

            }
        });
    }

    String html;
    private void initEvent() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(website == null){
                    return;
                }
                ArrayList<String> urls =  website.parseTargetImagesInHtml(html);
                if(urls.isEmpty()){
                    loadSource();
                }else {
                    ImgListActivity.start(MainActivity.this,urls,title,website.folderName());
                }

            }
        });
        titleBar.setTitle("点击查看本地图片");
        titleBar.setOnTitleClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(website == null){
                    return;
                }
                //showHtml(html);
                ImgListActivity.start(MainActivity.this,
                        new File (Environment.getExternalStorageDirectory(),website.folderName()).getAbsolutePath(),"file");
            }
        });


    }






    public void loadSource(ValueCallback<String> valueCallback){
        if(webView == null){
            Log.w("loadSource","webview is null");
            return;
        }
        String script = "javascript:document.getElementsByTagName('html')[0].innerHTML";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(script, valueCallback);
        }
    }



    private void initWebView() {

        //WebViewClient
        mWebViewClient=new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                //do you  work
                currentUrl = url;
                if(website != null){
                    view.getSettings().setBlockNetworkImage(website.interceptImage(url));
                    debugWebViewClient.setJsDebugPannelEnable(!(website instanceof XiaoCaoWebSite));

                }
            }




            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if(website != null){
                    debugWebViewClient.setJsDebugPannelEnable(!(website instanceof XiaoCaoWebSite));
                }
                return super.shouldInterceptRequest(view, request);
            }


            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if(website != null){
                    debugWebViewClient.setJsDebugPannelEnable(!(website instanceof XiaoCaoWebSite));
                }
                return super.shouldInterceptRequest(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //currentUrl = url;
                if(website != null){
                    view.getSettings().setBlockNetworkImage(website.interceptImage(url));
                }

                // 获取页面内容
                loadSource();

            }
        };
//WebChromeClient
        mWebChromeClient=new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                //do you work
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                Log.d("onReceivedTitle",title);
                if(website != null){
                    MainActivity.this.title = website.resetTitle(title);
                }

            }
        };

        debugWebViewClient = new DebugWebViewClient(mWebViewClient);
        debugWebViewClient.setLoggingEnabled(true);
        debugWebViewClient.setJsDebugPannelEnable(true);

        DebugWebChromeClient chromeClient = new DebugWebChromeClient(mWebChromeClient);
        chromeClient.setLoggingEnabled(true);



        preAgentWeb = AgentWeb.with(this)//传入Activity or Fragment
                .setAgentWebParent((LinearLayout) findViewById(R.id.l_root),
                        new LinearLayout.LayoutParams(-1, -1))//传入AgentWeb 的父控件 ，如果父控件为 RelativeLayout ， 那么第二参数需要传入 RelativeLayout.LayoutParams ,第一个参数和第二个参数应该对应。
                .useDefaultIndicator()// 使用默认进度条
                .defaultProgressBarColor() // 使用默认进度条颜色
                .setReceivedTitleCallback(new ChromeClientCallbackManager.ReceivedTitleCallback() {
                    @Override
                    public void onReceivedTitle(WebView view, String title) {
                        titleBar.setTitle(title);

                    }
                }) //设置 Web 页面的 title 回调
                .setWebChromeClient(chromeClient)
                .setWebViewClient(debugWebViewClient)
                .createAgentWeb()//
                .ready();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

    }
    AgentWeb.PreAgentWeb preAgentWeb;

    DebugWebViewClient debugWebViewClient;

    private void go(String url){
        if(mAgentWeb == null){
            mAgentWeb = preAgentWeb.go(url);
            webView = mAgentWeb.getWebCreator().get();
            syncCookie(getApplicationContext(),url);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
            mAgentWeb.getJsInterfaceHolder().addJavaObject("android",new AndroidInterface(mAgentWeb,this));
        }else {
            mAgentWeb.getLoader().loadUrl(url);
        }

    }

    private void syncCookie(Context context, String url) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        //cookieManager.removeSessionCookie();// 移除旧的[可以省略]
        /*List<HttpCookie> cookies = new PersistentCookieStore(context).getCookies();// 获取Cookie[可以是其他的方式获取]
        for (int i = 0; i < cookies.size(); i++) {
            HttpCookie cookie = cookies.get(i);
            String value = cookie.getName() + "=" + cookie.getValue();
            cookieManager.setCookie(url, value);
        }*/
        CookieSyncManager.getInstance().sync();// To get instant sync instead of waiting for the timer to trigger, the host can call this.
    }


    private void initTitleBar() {
        titleBar = (TitleBar) findViewById(R.id.titlebar);
        titleBar.setLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onPause() {
        if(mAgentWeb != null){
            mAgentWeb.getWebLifeCycle().onPause();
        }

        super.onPause();

    }

    @Override
    protected void onResume() {
        if(mAgentWeb != null){
            mAgentWeb.getWebLifeCycle().onResume();
        }

        super.onResume();
    }
    @Override
    public void onDestroy() {
        if(mAgentWeb != null){
            mAgentWeb.getWebLifeCycle().onDestroy();
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(mAgentWeb == null){
            super.onBackPressed();
            return;
        }
        if(!mAgentWeb.back()){
            if(recyclerView.getVisibility() == View.GONE){
                recyclerView.setVisibility(View.VISIBLE);
                return;
            }
            super.onBackPressed();
        }

    }
}

