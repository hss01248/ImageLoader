package com.hss01248.fileoperation;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
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
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.Utils;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hss01248.activityresult.ActivityResultListener;
import com.hss01248.activityresult.StartActivityUtil;
import com.hss01248.permission.ext.IExtPermissionCallback;
import com.hss01248.permission.ext.MyPermissionsExt;
import com.hss01248.permission.ext.permissions.ManageMediaPermission;

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
 *
 *
 * https://blog.csdn.net/CHENEY0314/article/details/124216224
 *
 * @Author hss
 * @Date 23/02/2022 11:23
 * @Version 1.0
 */
public class FileDeleteUtil {



    public static  boolean askMediaManagerPermission = true;

    // 注意compileSdkVersion和targetSdkVersion均需要 >= 31
    public static void checkMediaManagerPermission(Runnable onSuccess,Runnable onDenied) {
        //<uses-permission android:name="android.permission.MANAGE_MEDIA"/>
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            boolean canManageMedia = MediaStore.canManageMedia(Utils.getApp());
            if(canManageMedia){
                LogUtils.i("已经有管理媒体的权限了");

                if (onSuccess !=null) onSuccess.run();
            }else {
                LogUtils.i("还没有管理媒体的权限,去申请");
                Intent intent = new Intent(Settings.ACTION_REQUEST_MANAGE_MEDIA);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                StartActivityUtil.goOutAppForResult(ActivityUtils.getTopActivity(), intent,
                        new ActivityResultListener() {
                            @Override
                            public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
                                boolean canManageMedia = MediaStore.canManageMedia(Utils.getApp());
                                if(canManageMedia){
                                    if (onSuccess !=null) onSuccess.run();
                                }else {
                                    if (onDenied !=null) onDenied.run();
                                }
                            }

                            @Override
                            public void onActivityNotFound(Throwable e) {
                                if (onDenied !=null) onDenied.run();
                            }
                        });
            }
        }else {
            LogUtils.i("Android版本小于12,还不需要这个管理媒体的权限");
            onSuccess.run();
        }
    }


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
            //申请权限的模块
            if(!canHaveUI){
                callBack.onError(new Exception("no permission"));
                return;
            }
            askWritePermission(path, canHaveUI, callBack);
            return;
        }
        //有权限时,Android10以下,还是直接使用File api:
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            //
            boolean isSuccess = new File(path).delete();
            if (isSuccess) {
                callBack.onNext(true);
            } else {
                callBack.onNext(false);
            }
            return;
        }
        //android10以上,通过createDeleteRequest来删除
        try {
            List<Uri> uris = new ArrayList<>();

            //这个uri应该是从mediastore查出来的uri,而不是自己通过file构建的:
            //uris.add(OpenUri.fromFile(Utils.getApp(),new File(path)));
            ContentResolver resolver = Utils.getApp().getContentResolver();	// 通过context上下文可拿到

            //区分: 图片/视频/音频
            Cursor cursor = MediaStore.Images.Media.query(resolver,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media._ID},
                    MediaStore.Images.Media.DATA + "=?",
                    new String[]{path}, null);
            if (null != cursor && cursor.moveToFirst()) {
                long id = cursor.getLong(0);
                Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                Uri uri = ContentUris.withAppendedId(contentUri, id);
                uris.add(uri);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                deleteByMediaDeleteReqeust(callBack, uris);
                return;
            }
        } catch (Exception e) {
            LogUtils.w(path,e);
        }


        //原文链接：https://blog.csdn.net/zjuter/article/details/121670823


        //Android10-12之间,使用MediaStore操作.
        //todo 在华为手机上依然被拦截
        Cursor cursor = MediaStore.Images.Media.query(
                Utils.getApp().getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=?",
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
                    LogUtils.w("无法使用media store删除,还得用file.delete");
                    boolean isSuccess = new File(path).delete();
                    if (isSuccess) {
                        callBack.onNext(true);
                    } else {
                        callBack.onNext(false);
                    }
                }
            } else {
                LogUtils.w("无法使用media store删除,还得用file.delete");
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

    @RequiresApi(api = Build.VERSION_CODES.R)
    private static void deleteByMediaDeleteReqeust(Observer<Boolean> callBack, List<Uri> uris) throws IntentSender.SendIntentException {
        PendingIntent deleteRequest;
        deleteRequest = MediaStore.createDeleteRequest(Utils.getApp().getContentResolver(), uris);
        // java.lang.IllegalArgumentException: All requested items must be referenced by specific ID
        PendingIntent finalDeleteRequest = deleteRequest;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if(MediaStore.canManageMedia(Utils.getApp())){
                //不会弹窗,直接删除
                ActivityUtils.getTopActivity().startIntentSenderForResult(finalDeleteRequest.getIntentSender(),
                        100, null, 0, 0, 0, null);
                callBack.onNext(true);
                return;
            }
            if(askMediaManagerPermission){
                checkMediaManagerPermission(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ActivityUtils.getTopActivity().startIntentSenderForResult(finalDeleteRequest.getIntentSender(),
                                    100, null, 0, 0, 0, null);
                            callBack.onNext(true);
                        }catch (Throwable throwable){
                            LogUtils.w(throwable);
                            //callBack.onError(throwable);
                            deleteByUserDialog(callBack, finalDeleteRequest);
                        }

                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        //callBack.onNext(false);
                        deleteByUserDialog(callBack, finalDeleteRequest);
                    }
                });
                return;
            }
        }
        deleteByUserDialog(callBack, finalDeleteRequest);
    }

    private static void deleteByUserDialog(Observer<Boolean> callBack, PendingIntent finalDeleteRequest) {
        StartActivityUtil.goOutAppForResult(ActivityUtils.getTopActivity(), null, new ActivityResultListener() {
            @Override
            public boolean onInterceptStartIntent(@NonNull Fragment fragment, @Nullable Intent intent, int requestCode) {
                try {
               /* ActivityUtils.getTopActivity().startIntentSenderForResult(
                        exception.getUserAction().getActionIntent().getIntentSender(),
                        requestCode,
                        null,
                        0, 0, 0, null);*/
                    fragment.startIntentSenderForResult(finalDeleteRequest.getIntentSender(),
                            requestCode, null, 0, 0, 0, null);
                } catch (IntentSender.SendIntentException e) {
                    LogUtils.w(e);
                    callBack.onError(e);
                }
                return true;
            }

            @Override
            public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
                //这里没有回调
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
    }

    private static void askWritePermission(String path, boolean canHaveUI, Observer<Boolean> callBack) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                || (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q
                && Environment.isExternalStorageLegacy())){
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
            XXPermissions.with(ActivityUtils.getTopActivity())
                    .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                    .request(new OnPermissionCallback() {
                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                if(Environment.isExternalStorageManager()){
                                    MyPermissionsExt.askPermission(ActivityUtils.getTopActivity(),
                                            new ManageMediaPermission(),
                                            new IExtPermissionCallback() {
                                                @Override
                                                public void onGranted(String name) {
                                                    deleteImage(path, canHaveUI, callBack);
                                                }

                                                @Override
                                                public void onDenied(String name) {
                                                    deleteImage(path, canHaveUI, callBack);
                                                }
                                            });


                                    return;
                                }
                            }
                            callBack.onError(new Exception("no permission"));
                        }

                        @Override
                        public void onDenied(List<String> permissions, boolean never) {
                            OnPermissionCallback.super.onDenied(permissions, never);
                            callBack.onError(new Exception("no permission"));
                        }
                    });



            //No Activity found to handle Intent
           // Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
           // intent.setPackage(AppUtils.getAppPackageName());
           // StartActivityUtil.goOutAppForResult(ActivityUtils.getTopActivity(), intent, new ActivityResultListener() {


        }
    }
}
