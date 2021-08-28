package com.hss01248.webviewspider;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hss01248.ui.pop.list.PopList;
import com.hss01248.webviewspider.basewebview.BaseQuickWebview;
import com.hss01248.webviewspider.spider.IHtmlParser;
import com.hss01248.webviewspider.spider.ListToDetailImgsInfo;
import com.hss01248.webviewspider.spider.PexelImageParser;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
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


    public static List<String> getSpiders(){
        List<String> strings = new ArrayList<>();
        Iterator<Map.Entry<String, IHtmlParser>> iterator = parsers.entrySet().iterator();
        while (iterator.hasNext()){
            strings.add(iterator.next().getValue().entranceUrl());
        }
        return strings;
    }

    static Map<String,IHtmlParser> parsers = new HashMap<>();
    static {
        parsers.put(new PexelImageParser().entranceUrl(),new PexelImageParser());
    }
    IHtmlParser parser;

    public static void addParser(IHtmlParser parser){
        parsers.put(parser.entranceUrl(),parser);
    }

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
        quickWebview.setNeedBlockImageLoad( parser.interceptImage(url));
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
        menus.add("当前为分页list,爬取list内所有页面的图片并直接下载");
        menus.add("显示图片文件夹");
        PopList.showPop(this, -1, button, menus, new PopList.OnItemClickListener() {
            @Override
            public void onClick(int position, String str) {
                if(position == 0){
                    showSource();
                }else if(position == 1){
                    parseUrlsAndShow();
                }else if(position == 2){
                    parseListUrlsAndShow();
                }else if(position == 3){
                    if(iShowUrls != null){
                        iShowUrls.showFolder(SpiderWebviewActivity.this, new File(Environment.getExternalStorageDirectory(),"0spider").getAbsolutePath());
                    }
                }

            }
        });
    }

    boolean isParsingList;

    private void parseListUrlsAndShow() {
        if(quickWebview == null){
            return;
        }
        String url = quickWebview.getCurrentUrl();
        TextView textView = new TextView(this);
        textView.setTextColor(Color.WHITE);
        textView.setPadding(20,20,20,20);
        textView.setBackground(new ColorDrawable(Color.parseColor("#66333333")));
        textView.setText("爬取urllist start");
        EasyFloat.with(this)
                .setTag(url)
                .setLayout(textView)
                // .setGravity(Gravity.BOTTOM)
                .setDragEnable(true)
                .setShowPattern(ShowPattern.FOREGROUND)
                .show();

        quickWebview.loadSource(new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isParsingList = true;
                        parser.parseListAndDetail(SpiderWebviewActivity.this,quickWebview.getInfo(), new ValueCallback<ListToDetailImgsInfo>() {
                            @Override
                            public void onReceiveValue(ListToDetailImgsInfo info) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        isParsingList = false;
                                        EasyFloat.dismiss(info.listUrl);
                                        if (info.imagUrls.isEmpty()) {
                                            quickWebview.loadSource(new ValueCallback<String>() {
                                                @Override
                                                public void onReceiveValue(String value) {

                                                }
                                            });
                                        } else {
                                            if (iShowUrls != null) {
                                                info.saveDirPath = getSaveDir(parser.folderName(),parser.subfolderName(quickWebview.getCurrentTitle(),quickWebview.getCurrentUrl()));
                                                Log.v("caol","parseListUrlsAndShow path:"+info.saveDirPath);
                                                info.hiddenFolder = parser.hiddenFolder();
                                                iShowUrls.showUrls(SpiderWebviewActivity.this,info.listTitle,info.titlesToImags,info.imagUrls,info.saveDirPath,info.hiddenFolder,true);
                                            }
                                        }
                                    }
                                });
                            }
                        }, new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                textView.setText("爬取urllist: "+value);
                            }
                        });
                    }
                });

            }
        });
    }


    private String getSaveDir(String folderName, String subFolderName) {

         //new File(System.getenv("EXTERNAL_STORAGE"));
        File dir0 = new File(Environment.getExternalStorageDirectory(),"0spider");
        dir0.mkdirs();
        File dir = new File(dir0,folderName);
        dir.mkdirs();
        if(!TextUtils.isEmpty(subFolderName)){
            dir = new File(dir,subFolderName);
            dir.mkdirs();
        }
        dir = findNextSub(dir,0);
        //dir.listFiles();
        return dir.getAbsolutePath();
    }

    private File findNextSub(File dir,int idx) {
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        if(files == null || files.length ==0){
            File dir2 = new File(dir,dir.getName()+idx);
            dir2.mkdirs();
            Log.v("dirs","0 new dir :"+dir2);
            return dir2;
        }else {
            List<File> dirs = new ArrayList<>(Arrays.asList(files));
            Collections.sort(dirs, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return o2.getName().compareTo(o1.getName());
                }
            });
            Log.v("dirs",Arrays.toString(dirs.toArray()));
            String[] list = dirs.get(0).list();
            if(list == null ||list.length < 3000){
                return dirs.get(0);
            }
            File dir2 = new File(dir,dir.getName()+dirs.size());
            dir2.mkdirs();
            Log.v("dirs","1 new dir :"+dir2);
            return dir2;
        }
    }

    private void parseUrlsAndShow() {
        quickWebview.getSource(new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                List<String> list = parser.parseTargetImagesInHtml(value);
                if(list != null && !list.isEmpty()){
                    if(iShowUrls != null){
                        String path =  getSaveDir(parser.folderName(), parser.subfolderName(quickWebview.getCurrentTitle(),quickWebview.getCurrentUrl()));
                        Log.v("caol","parseUrlsAndShow path:"+path);
                        iShowUrls.showUrls(SpiderWebviewActivity.this,
                                parser.resetDetailTitle(quickWebview.getCurrentTitle()),list,path
                               ,parser.hiddenFolder(),false);
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
        if(isParsingList){
            ToastUtils.showShort("正在爬取list,不可退出当前页面");
            return;
        }
        if(quickWebview == null || !quickWebview.onBackPressed()){
            super.onBackPressed();
        }

    }
}
