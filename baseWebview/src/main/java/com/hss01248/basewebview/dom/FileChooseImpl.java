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
import com.hss.utils.enhance.api.MyCommonCallback;

import com.hss01248.media.pick.CaptureAudioUtil;
import com.hss01248.media.pick.CaptureImageUtil;
import com.hss01248.media.pick.CaptureVideoUtil;
import com.hss01248.media.pick.MediaPickOrCaptureUtil;
import com.hss01248.media.pick.MediaPickUtil;
import com.hss01248.media.pick.MimeTypeUtil;

import com.just.agentweb.MiddlewareWebChromeBase;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;

import java.io.File;
import java.util.List;

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
                //Use getAcceptTypes to determine suitable capture devices.  caputure="user" 前置摄像头,但传不过来
                intent0);
        String[] washMimeTypes = MimeTypeUtil.washMimeType(fileChooserParams.getAcceptTypes());
        LogUtils.d(washMimeTypes);
        if(fileChooserParams.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE){
            MediaPickUtil.pickMultiFiles(new MyCommonCallback<List<Uri>>() {
                @Override
                public void onSuccess(List<Uri> uris) {
                    Uri[] uris1 = new Uri[uris.size()];
                    for (int i = 0; i < uris.size(); i++) {
                        uris1[i] = uris.get(i);
                    }
                    filePathCallback.onReceiveValue(uris1);
                }
            });
            return true;
        }

        if(isOnlyImage(washMimeTypes)){
            if(fileChooserParams.isCaptureEnabled()){
                CaptureImageUtil.takePicture(false,new MyCommonCallback<String>() {
                    @Override
                    public void onSuccess(String s) {
                        Uri[] uris1 = {Uri.fromFile(new File(s))};
                        filePathCallback.onReceiveValue(uris1);
                    }
                });
            }else {
                MediaPickOrCaptureUtil.pickImageOrTakePhoto(false,new MyCommonCallback<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri[] uris1 = {uri};
                        filePathCallback.onReceiveValue(uris1);
                    }
                });
            }
            return true;
        }

        if(isOnlyVideo(washMimeTypes)){
            if(fileChooserParams.isCaptureEnabled()){
                CaptureVideoUtil.startVideoCapture(false,30,1024*1024*1024,new MyCommonCallback<String>() {
                    @Override
                    public void onSuccess(String s) {
                        Uri[] uris1 = {Uri.fromFile(new File(s))};
                        filePathCallback.onReceiveValue(uris1);
                    }
                });
            }else {
                MediaPickOrCaptureUtil.pickOrRecordVideo(false,30,new MyCommonCallback<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri[] uris1 = {uri};
                        filePathCallback.onReceiveValue(uris1);
                    }
                });
            }
            return true;
        }
        if(isOnlyVideoOrImage(washMimeTypes)){
            if(fileChooserParams.isCaptureEnabled()){
                new XPopup.Builder(ActivityUtils.getTopActivity())
                        .asBottomList("请选择", new String[]{"拍照", "录制视频"},
                                new OnSelectListener() {
                                    @Override
                                    public void onSelect(int position, String text) {
                                        if(position ==1){
                                            CaptureVideoUtil.startVideoCapture(false,30,1024*1024*1024,new MyCommonCallback<String>() {
                                                @Override
                                                public void onSuccess(String s) {
                                                    Uri[] uris1 = {Uri.fromFile(new File(s))};
                                                    filePathCallback.onReceiveValue(uris1);
                                                }

                                                @Override
                                                public void onError(String code, String msg, @Nullable Throwable throwable) {
                                                   // callback.onError(code, msg, throwable);
                                                }
                                            });
                                        }else if(position ==0){
                                            CaptureImageUtil.takePicture(false,new MyCommonCallback<String>() {
                                                @Override
                                                public void onSuccess(String s) {
                                                    Uri[] uris1 = {Uri.fromFile(new File(s))};
                                                    filePathCallback.onReceiveValue(uris1);
                                                }
                                            });
                                        }
                                    }
                                })
                        .show();
            }else {
                MediaPickOrCaptureUtil.pickOrCaptureImageOrVideo(true,30, new MyCommonCallback<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri[] uris1 = {uri};
                        filePathCallback.onReceiveValue(uris1);
                    }
                });
            }
            return true;
        }

        if(isOnlyAudio(washMimeTypes)){
            if(fileChooserParams.isCaptureEnabled()){
                CaptureAudioUtil.startRecord(new MyCommonCallback<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri[] uris1 = {uri};
                        filePathCallback.onReceiveValue(uris1);
                    }
                });
            }else {
                MediaPickOrCaptureUtil.pickOrRecordAudio(new MyCommonCallback<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri[] uris1 = {uri};
                        filePathCallback.onReceiveValue(uris1);
                    }
                });
            }
            return true;
        }



        MediaPickUtil.pickOne(new MyCommonCallback<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Uri[] uris1 = {uri};
                filePathCallback.onReceiveValue(uris1);
            }
        },washMimeTypes);


        return true;
    }

    private boolean isOnlyImage(String[] washMimeTypes) {
        for (String washMimeType : washMimeTypes) {
            if(!washMimeType.startsWith("image")){
                return false;
            }
        }
        return true;
    }

    private boolean isOnlyVideo(String[] washMimeTypes) {
        for (String washMimeType : washMimeTypes) {
            if(!washMimeType.startsWith("video")){
                return false;
            }
        }
        return true;
    }
    private boolean isOnlyAudio(String[] washMimeTypes) {
        for (String washMimeType : washMimeTypes) {
            if(!washMimeType.startsWith("audio")){
                return false;
            }
        }
        return true;
    }
    private boolean isOnlyVideoOrImage(String[] washMimeTypes) {
        for (String washMimeType : washMimeTypes) {
            if(!washMimeType.startsWith("video") && !washMimeType.startsWith("image")){
                return false;
            }
        }
        return true;
    }



}
