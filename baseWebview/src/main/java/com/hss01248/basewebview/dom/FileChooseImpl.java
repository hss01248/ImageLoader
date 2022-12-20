package com.hss01248.basewebview.dom;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.hss.utils.enhance.api.MyCommonCallback;
import com.hss01248.basewebview.R;
import com.hss01248.iwidget.singlechoose.SingleChooseDialogImpl;
import com.hss01248.iwidget.singlechoose.SingleChooseDialogListener;
import com.hss01248.media.pick.CaptureAudioUtil;
import com.hss01248.media.pick.CaptureImageUtil;
import com.hss01248.media.pick.CaptureVideoUtil;
import com.hss01248.media.pick.MediaPickOrCaptureUtil;
import com.hss01248.media.pick.MediaPickUtil;
import com.hss01248.media.pick.MimeTypeUtil;
import com.just.agentweb.MiddlewareWebChromeBase;

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
     * @param filePathCallback  error时必须有回调,否则无法再次点击
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
                @Override
                public void onError(String code, String msg, @Nullable Throwable throwable) {
                    MyCommonCallback.super.onError(code, msg, throwable);
                    filePathCallback.onReceiveValue(null);
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
                    @Override
                    public void onError(String code, String msg, @Nullable Throwable throwable) {
                        MyCommonCallback.super.onError(code, msg, throwable);
                        filePathCallback.onReceiveValue(null);
                    }
                });
            }else {
                MediaPickOrCaptureUtil.pickImageOrTakePhoto(false,new MyCommonCallback<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri[] uris1 = {uri};
                        filePathCallback.onReceiveValue(uris1);
                    }
                    @Override
                    public void onError(String code, String msg, @Nullable Throwable throwable) {
                        MyCommonCallback.super.onError(code, msg, throwable);
                        filePathCallback.onReceiveValue(null);
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
                    @Override
                    public void onError(String code, String msg, @Nullable Throwable throwable) {
                        MyCommonCallback.super.onError(code, msg, throwable);
                        filePathCallback.onReceiveValue(null);
                    }
                });
            }else {
                MediaPickOrCaptureUtil.pickOrRecordVideo(false,30,new MyCommonCallback<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri[] uris1 = {uri};
                        filePathCallback.onReceiveValue(uris1);
                    }
                    @Override
                    public void onError(String code, String msg, @Nullable Throwable throwable) {
                        MyCommonCallback.super.onError(code, msg, throwable);
                        filePathCallback.onReceiveValue(null);
                    }
                });
            }
            return true;
        }
        if(isOnlyVideoOrImage(washMimeTypes)){
            if(fileChooserParams.isCaptureEnabled()){
                new SingleChooseDialogImpl().showAtBottom(
                        StringUtils.getString(R.string.meida_pick_please_choose),
                        new String[]{StringUtils.getString(R.string.meida_pick_take_photo),
                                StringUtils.getString(R.string.meida_pick_record_video)},
                        new SingleChooseDialogListener() {
                            @Override
                            public void onItemClicked(int position, CharSequence text) {
                                if(position ==1){
                                    CaptureVideoUtil.startVideoCapture(false,30,1024*1024*1024,new MyCommonCallback<String>() {
                                        @Override
                                        public void onSuccess(String s) {
                                            Uri[] uris1 = {Uri.fromFile(new File(s))};
                                            filePathCallback.onReceiveValue(uris1);
                                        }

                                        @Override
                                        public void onError(String code, String msg, @Nullable Throwable throwable) {
                                            MyCommonCallback.super.onError(code, msg, throwable);
                                            filePathCallback.onReceiveValue(null);
                                        }
                                    });
                                }else if(position ==0){
                                    CaptureImageUtil.takePicture(false,new MyCommonCallback<String>() {
                                        @Override
                                        public void onSuccess(String s) {
                                            Uri[] uris1 = {Uri.fromFile(new File(s))};
                                            filePathCallback.onReceiveValue(uris1);
                                        }
                                        @Override
                                        public void onError(String code, String msg, @Nullable Throwable throwable) {
                                            MyCommonCallback.super.onError(code, msg, throwable);
                                            filePathCallback.onReceiveValue(null);
                                        }
                                    });
                                }
                            }
                            @Override
                            public void onCancel(boolean fromBackPressed, boolean fromOutsideClick, boolean fromCancelButton) {
                                SingleChooseDialogListener.super.onCancel(fromBackPressed, fromOutsideClick, fromCancelButton);
                               // callback.onError(StringUtils.getString(R.string.meida_pick_canceled));
                                filePathCallback.onReceiveValue(null);
                            }
                        }
                );
            }else {
                MediaPickOrCaptureUtil.pickOrCaptureImageOrVideo(true,30, new MyCommonCallback<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri[] uris1 = {uri};
                        filePathCallback.onReceiveValue(uris1);
                    }
                    @Override
                    public void onError(String code, String msg, @Nullable Throwable throwable) {
                        MyCommonCallback.super.onError(code, msg, throwable);
                        filePathCallback.onReceiveValue(null);
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

                    @Override
                    public void onError(String code, String msg, @Nullable Throwable throwable) {
                        MyCommonCallback.super.onError(code, msg, throwable);
                        filePathCallback.onReceiveValue(null);
                    }
                });
            }else {
                MediaPickOrCaptureUtil.pickOrRecordAudio(new MyCommonCallback<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri[] uris1 = {uri};
                        filePathCallback.onReceiveValue(uris1);
                    }
                    @Override
                    public void onError(String code, String msg, @Nullable Throwable throwable) {
                        MyCommonCallback.super.onError(code, msg, throwable);
                        filePathCallback.onReceiveValue(null);
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

            @Override
            public void onError(String code, String msg, @Nullable Throwable throwable) {
                MyCommonCallback.super.onError(code, msg, throwable);
                filePathCallback.onReceiveValue(null);
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
