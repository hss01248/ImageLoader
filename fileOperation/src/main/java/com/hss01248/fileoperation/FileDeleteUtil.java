package com.hss01248.fileoperation;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.ContentUris;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.Utils;
import com.hss01248.activityresult.ActivityResultListener;
import com.hss01248.activityresult.StartActivityUtil;
import com.hss01248.openuri.OpenUri;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;

/**
 * @Despciption
 * 在一次操作中修改或删除多个媒体文件
 * 根据应用在哪个 Android 版本上运行来纳入逻辑。
 *
 * 在 Android 11 上运行
 * 请使用以下方法：
 *
 * 使用 MediaStore.createWriteRequest() 或 MediaStore.createTrashRequest() 为应用的写入或删除请求创建待定 intent，然后通过调用该 intent 提示用户授予修改一组文件的权限。
 * 评估用户的响应：
 *
 * 如果授予了权限，请继续修改或删除操作。
 * 如果未授予权限，请向用户说明您的应用中的功能为何需要该权限。
 * 详细了解如何使用 Android 11 及更高版本提供的这些方法管理媒体文件组。
 *
 * 在 Android 10 上运行
 * 如果您的应用以 Android 10（API 级别 29）为目标平台，请停用分区存储，继续使用适用于 Android 9 及更低版本的方法来执行此操作。
 *
 * 在 Android 9 或更低版本上运行
 * 请使用以下方法：
 *
 * 按照请求应用权限中所述的最佳做法，请求 WRITE_EXTERNAL_STORAGE 权限。
 * 使用 MediaStore API 修改或删除媒体文件。
 * @Author hss
 * @Date 23/02/2022 11:23
 * @Version 1.0
 */
public class FileDeleteUtil {

    public static void deleteImage(String path,boolean canHaveUI, Observer<Boolean> callBack) {
        if(TextUtils.isEmpty(path)){
            callBack.onNext(true);
            return;
        }
        File file = new File(path);
        if(!file.exists()){
            LogUtils.d("file not exist",path);
            callBack.onNext(true);
            return;
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            if(!file.canWrite()){
                LogUtils.w("Android6以下,系统自定义存储权限没有允许");
            }
            boolean isSuccess = file.delete();
            if (isSuccess) {
                callBack.onNext(true);
            } else {
                callBack.onNext(false);
            }
            return;
        }

        if(!file.canWrite()){
            if(!canHaveUI){
                callBack.onError(new Exception("no permission"));
                return;
            }
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                    || (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q && Environment.isExternalStorageLegacy())){
                //android10以下
                //或者Android10,兼容模式下  请求WRITE_EXTERNAL_STORAGE
                PermissionUtils.permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .callback(new PermissionUtils.SimpleCallback() {
                            @Override
                            public void onGranted() {
                                deleteImage(path, canHaveUI, callBack);
                            }

                            @Override
                            public void onDenied() {
                                callBack.onError(new Exception("no permission"));
                            }
                        }).request();

            }else {
                //todo Android10,且为分区存储时(非兼容模式),权限怎么申请?
                //请求manage权限
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                intent.setPackage(AppUtils.getAppPackageName());
                StartActivityUtil.goOutAppForResult(ActivityUtils.getTopActivity(), intent, new ActivityResultListener() {
                    @Override
                    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            if(Environment.isExternalStorageManager()){
                                deleteImage(path, canHaveUI, callBack);
                                return;
                            }
                        }
                        callBack.onError(new Exception("no permission"));
                    }

                    @Override
                    public void onActivityNotFound(Throwable e) {
                        LogUtils.w(e);
                        callBack.onError(new Exception("no permission"));
                    }
                });

            }
            return;
        }
        //有权限时,Android10以下,还是直接使用File api:
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            boolean isSuccess = new File(path).delete();
            if (isSuccess) {
                callBack.onNext(true);
            } else {
                callBack.onNext(false);
            }
            return;
        }
        //先请求权限,再删除
        try {
            List<Uri> uris = new ArrayList<>();
            uris.add(OpenUri.fromFile(Utils.getApp(),new File(path)));
            PendingIntent deleteRequest = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                deleteRequest = MediaStore.createDeleteRequest(Utils.getApp().getContentResolver(), uris);
                // java.lang.IllegalArgumentException: All requested items must be referenced by specific ID
                PendingIntent finalDeleteRequest = deleteRequest;
                StartActivityUtil.goOutAppForResult(ActivityUtils.getTopActivity(), null, new ActivityResultListener() {
                    @Override
                    public boolean onInterceptStartIntent(@NonNull Fragment fragment, @Nullable Intent intent, int requestCode) {
                        try {
                       /* ActivityUtils.getTopActivity().startIntentSenderForResult(
                                exception.getUserAction().getActionIntent().getIntentSender(),
                                requestCode,
                                null,
                                0, 0, 0, null);*/
                            fragment.startIntentSenderForResult(finalDeleteRequest.getIntentSender(), requestCode, null, 0, 0, 0, null);
                        } catch (IntentSender.SendIntentException e) {
                            LogUtils.w(e);
                            callBack.onError(e);
                        }
                        return true;
                    }

                    @Override
                    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
                        if(resultCode == Activity.RESULT_OK){
                            callBack.onNext(true);
                        }else {
                            callBack.onNext(false);
                        }
                    }

                    @Override
                    public void onActivityNotFound(Throwable e) {
                        LogUtils.w(e);
                        callBack.onError(e);
                    }
                });
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        //原文链接：https://blog.csdn.net/zjuter/article/details/121670823


        //Android10及以上,使用MediaStore操作.
        Cursor cursor = MediaStore.Images.Media.query(
                Utils.getApp().getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA.toString() + "=?",
                new String[]{path},
                null);

        try {
            if (cursor.moveToFirst()) {
                Long id = cursor.getLong(0);
                Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                Uri uri = ContentUris.withAppendedId(contentUri, id);

                int count = Utils.getApp().getContentResolver().delete(uri, null, null);

                if (count > 0) {
                    callBack.onNext(true);
                } else {
                    callBack.onNext(false);
                }
            } else {
                boolean isSuccess = new File(path).delete();
                if (isSuccess) {
                    callBack.onNext(true);
                } else {
                    callBack.onNext(false);
                }
            }
        }catch (Throwable throwable){
            LogUtils.w(throwable);
            if (canHaveUI && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && throwable instanceof RecoverableSecurityException) {
                RecoverableSecurityException exception = (RecoverableSecurityException) throwable;

                StartActivityUtil.goOutAppForResult(ActivityUtils.getTopActivity(), null, new ActivityResultListener() {
                    @Override
                    public boolean onInterceptStartIntent(@NonNull Fragment fragment, @Nullable Intent intent, int requestCode) {
                        try {
                            ActivityUtils.getTopActivity().startIntentSenderForResult(
                                    exception.getUserAction().getActionIntent().getIntentSender(),
                                    requestCode,
                                    null,
                                    0, 0, 0, null);
                        } catch (IntentSender.SendIntentException e) {
                            LogUtils.w(e);
                            callBack.onError(e);
                        }
                        return true;
                    }

                    @Override
                    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
                        if(resultCode == Activity.RESULT_OK){
                            callBack.onNext(true);
                        }else {
                            callBack.onNext(false);
                        }
                    }

                    @Override
                    public void onActivityNotFound(Throwable e) {
                        LogUtils.w(e);
                        callBack.onError(e);
                    }
                });
            }else {
                callBack.onError(throwable);
            }
        }
    }
}
