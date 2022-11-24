package com.hss01248.basewebview.dom;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.hss01248.activityresult.ActivityResultListener;
import com.hss01248.activityresult.StartActivityUtil;
import com.just.agentweb.MiddlewareWebChromeBase;

/**
 * @Despciption
 * 不使用file类型input也能触发文件上传
 *  https://www.zhangxinxu.com/wordpress/2021/08/file-system-access-api/
 *  js 的input type = file
 *  https://developer.mozilla.org/zh-CN/docs/Web/HTML/Element/Input/file
 *
 * @Author hss
 * @Date 24/11/2022 09:45
 * @Version 1.0
 */
public class FileChooseImpl extends MiddlewareWebChromeBase {

    /**
     * <input type="file" id="file1" accept="image/*,.pdf" capture="camera" @change="changePic" multiple="true"/>
     * @param webView
     * @param filePathCallback
     * @param fileChooserParams
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        Intent intent0 = fileChooserParams.createIntent();
        //Intent { act=android.intent.action.GET_CONTENT cat=[android.intent.category.OPENABLE] typ=image/png }

        LogUtils.i("getTitle-"+fileChooserParams.getTitle(),
                //mode: 0 -单选  1-多选
                "getMode-"+fileChooserParams.getMode(),
                "getFilenameHint-"+fileChooserParams.getFilenameHint(),
                fileChooserParams.getAcceptTypes(),
                "isCaptureEnabled-"+fileChooserParams.isCaptureEnabled(),
                intent0);
        String mimeType = buildMimeType(fileChooserParams.getAcceptTypes());
        LogUtils.d(mimeType);
       /* if(fileChooserParams.isCaptureEnabled()){

        }*/


        Intent intent = intent0;
        if(mimeType.contains("video/") || mimeType.contains("image/") || mimeType.contains("audio/")){
            intent = new Intent();
            //intent.setType("video/*;image/*");//同时选择视频和图片
            intent.setType(mimeType);//
            //intent.setAction(Intent.ACTION_GET_CONTENT);
            //打开方式有两种action，1.ACTION_PICK；2.ACTION_GET_CONTENT 区分大意为：
            // ACTION_PICK 为打开特定数据一个列表来供用户挑选，其中数据为现有的数据。而 ACTION_GET_CONTENT 区别在于它允许用户创建一个之前并不存在的数据。
            intent.setAction(Intent.ACTION_PICK);
        }

        //startActivityForResult(Intent.createChooser(intent,"选择图像..."), PICK_IMAGE_REQUEST);
        //FragmentManager: Activity result delivered for unknown Fragment
        PackageManager manager = Utils.getApp().getPackageManager();
        if (manager.queryIntentActivities(intent, 0).size() <= 0) {
            //这个action比ACTION_GET_CONTENT多了过滤器的功能,所以优先用这个
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType(mimeType);
            //Intent { act=android.intent.action.GET_CONTENT cat=[android.intent.category.OPENABLE] typ=image/png }
        }



        //ThreadUtils.getMainHandler()
        StartActivityUtil.goOutAppForResult(ActivityUtils.getTopActivity(), intent, new ActivityResultListener() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
                LogUtils.i(data);
                if(resultCode == Activity.RESULT_OK){
                    if(data != null && data.getData() != null){
                        //如果是图片,就先压缩一遍
                        Uri[] uris = new Uri[1];
                        uris[0] = data.getData();
                        filePathCallback.onReceiveValue(uris);
                        return;
                    }
                }
                filePathCallback.onReceiveValue(null);
            }

            @Override
            public void onActivityNotFound(Throwable e) {
                LogUtils.w(e);
                filePathCallback.onReceiveValue(null);
            }
        });
        return true;
    }

    private String buildMimeType(String[] acceptTypes) {
        if(acceptTypes == null || acceptTypes .length ==0){
            return "*/*";
        }
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < acceptTypes.length; i++) {
            String type = acceptTypes[i];
            if(type.startsWith(".")){
                //兼容带.号 的type
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(type.substring(1));
            }
            str.append(type);
            if(i != acceptTypes.length-1){
                str.append(",");
            }
        }
        String st =  str.toString();
        if(TextUtils.isEmpty(st)){
            return "*/*";
        }
        return st;
    }

}
