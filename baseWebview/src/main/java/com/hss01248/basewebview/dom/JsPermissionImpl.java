package com.hss01248.basewebview.dom;

import android.Manifest;
import android.location.Location;
import android.os.Build;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.Utils;
import com.hss01248.location.LocationUtil;
import com.hss01248.location.MyLocationCallback;
import com.hss01248.permission.MyPermissions;
import com.just.agentweb.MiddlewareWebChromeBase;

import java.util.Arrays;
import java.util.List;

/**
 * @Despciption https://www.runoob.com/html/html5-geolocation.html
 *
 * 从API24开始，此方法只为安全的源(https)调用，非安全的源会被自动拒绝
 * @Author hss
 * @Date 25/11/2022 09:49
 * @Version 1.0
 */
public class JsPermissionImpl extends MiddlewareWebChromeBase {

    /**
     * MIDI 设备-数字音乐设备,暂不处理
     * @param request
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onPermissionRequest(PermissionRequest request) {
        //super.onPermissionRequest(request);
        LogUtils.d(request.getOrigin(),request.getResources());
        String[] resources = request.getResources();
        if(isOnlyAudio(resources)){
            MyPermissions.requestByMostEffort(false, true, new PermissionUtils.FullCallback() {
                @Override
                public void onGranted(@NonNull List<String> granted) {
                    request.grant(resources);
                }

                @Override
                public void onDenied(@NonNull List<String> deniedForever, @NonNull List<String> denied) {
                    request.deny();
                }
            }, Manifest.permission.RECORD_AUDIO);
        }else if(isVideo(resources)){
            MyPermissions.requestByMostEffort(false, true, new PermissionUtils.FullCallback() {
                @Override
                public void onGranted(@NonNull List<String> granted) {
                    request.grant(resources);
                }

                @Override
                public void onDenied(@NonNull List<String> deniedForever, @NonNull List<String> denied) {
                    request.deny();
                }
            }, Manifest.permission.RECORD_AUDIO,Manifest.permission.CAMERA);
        }

    }

    private boolean isVideo(String[] resources) {
        List<String> strings = Arrays.asList(resources);
        return strings.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE);
    }

    private boolean isOnlyAudio(String[] resources) {
        List<String> strings = Arrays.asList(resources);
        return !strings.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE) && strings.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onPermissionRequestCanceled(PermissionRequest request) {
        //super.onPermissionRequestCanceled(request);
    }

    @Override
    public void onGeolocationPermissionsHidePrompt() {
        super.onGeolocationPermissionsHidePrompt();
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        //super.onGeolocationPermissionsShowPrompt(origin, callback);

        LocationUtil.getLocation(Utils.getApp(), false, 10000,
                false, true, new MyLocationCallback() {
                    @Override
                    public boolean configJustAskPermissionAndSwitch() {
                        return true;
                    }

                    @Override
                    public boolean configAcceptOnlyCoarseLocationPermission() {
                        return true;
                    }

                    @Override
                    public void onSuccess(Location location, String msg) {
                        //注意个函数，第二个参数就是是否同意定位权限，第三个是是否希望内核记住
                        callback.invoke(origin,true,false);
                    }

                    @Override
                    public void onFailed(int type, String msg, boolean isFailBeforeReallyRequest) {
                        callback.invoke(origin,false,false);
                    }
                });
       /* MyPermissions.setCanAcceptOnlyCoarseLocationPermission(true);
        MyPermissions.requestByMostEffort(false, true, new PermissionUtils.FullCallback() {
            @Override
            public void onGranted(@NonNull List<String> granted) {
                //注意个函数，第二个参数就是是否同意定位权限，第三个是是否希望内核记住
                callback.invoke(origin,true,false);
            }

            @Override
            public void onDenied(@NonNull List<String> deniedForever, @NonNull List<String> denied) {
                LogUtils.w(deniedForever,denied);
                if(!denied.contains(Manifest.permission.ACCESS_COARSE_LOCATION)
                        && !denied.contains(Manifest.permission.ACCESS_COARSE_LOCATION)){
                    callback.invoke(origin,true,false);
                }else {
                    callback.invoke(origin,false,false);
                }
            }
        }, Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION);*/


    }
}
