package com.hss01248.fileoperation;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
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
import java.util.Iterator;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

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
public class FileBatchDeleteUtil {






    public static void deleteImages(List<String> paths, Observer<Boolean> callBack) {
        Iterator<String> iterator = paths.iterator();
        while (iterator.hasNext()){
            String path = iterator.next();
            if(TextUtils.isEmpty(path)){
                iterator.remove();
                continue;
            }
            File file = new File(path);
            if(!file.exists()){
                LogUtils.d("file not exist",path);
                iterator.remove();
                continue;
            }
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                if(!file.canWrite()){
                    LogUtils.w("Android6以下,系统自定义存储权限没有允许");
                }
                boolean isSuccess = file.delete();
                LogUtils.d("Android6以下,文件删除状态");
            }
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            callBack.onNext(true);
            return;
        }
        //申请权限: 读写存储权限 + 高版本的管理媒体权限:
        askWritePermission(new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Boolean aBoolean) {
                if(!aBoolean){
                    callBack.onNext(false);
                }else {
                    //进行删除
                    doDelete(paths,callBack);
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

    }

    private static void doDelete(List<String> paths, Observer<Boolean> callBack) {
        //有权限时,Android10以下,还是直接使用File api:
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
        || (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q && Environment.isExternalStorageLegacy())){
            //
            for (String path : paths) {
                boolean isSuccess = new File(path).delete();
            }
            callBack.onNext(true);
            return;
        }


        try {
            List<Uri> uris = new ArrayList<>();

            for (String path : paths) {
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
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                deleteByMediaDeleteReqeust(callBack, uris);
                return;
            }
        } catch (Exception e) {
            LogUtils.w(paths,e);
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
            if(FileDeleteUtil.askMediaManagerPermission){
                FileDeleteUtil.checkMediaManagerPermission(new Runnable() {
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

    private static void askWritePermission(Observer<Boolean> callBack) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                || (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q
                && Environment.isExternalStorageLegacy())){
            //android10以下
            //或者Android10,兼容模式下  请求WRITE_EXTERNAL_STORAGE
            PermissionUtils.permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .callback(new PermissionUtils.SimpleCallback() {
                        @Override
                        public void onGranted() {
                            callBack.onNext(true);
                        }

                        @Override
                        public void onDenied() {
                            callBack.onNext(false);
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
                                                    callBack.onNext(true);
                                                }

                                                @Override
                                                public void onDenied(String name) {
                                                    callBack.onNext(false);
                                                }
                                            });


                                    return;
                                }
                            }
                            callBack.onNext(false);
                        }

                        @Override
                        public void onDenied(List<String> permissions, boolean never) {
                            OnPermissionCallback.super.onDenied(permissions, never);
                            callBack.onNext(false);
                        }
                    });



            //No Activity found to handle Intent
           // Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
           // intent.setPackage(AppUtils.getAppPackageName());
           // StartActivityUtil.goOutAppForResult(ActivityUtils.getTopActivity(), intent, new ActivityResultListener() {


        }
    }
}
