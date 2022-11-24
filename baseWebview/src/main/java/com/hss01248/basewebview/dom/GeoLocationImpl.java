package com.hss01248.basewebview.dom;

import android.Manifest;
import android.webkit.GeolocationPermissions;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.hss01248.permission.MyPermissions;
import com.just.agentweb.MiddlewareWebChromeBase;

import java.util.List;

/**
 * @Despciption https://www.runoob.com/html/html5-geolocation.html
 * @Author hss
 * @Date 24/11/2022 09:59
 * @Version 1.0
 */
public class GeoLocationImpl extends MiddlewareWebChromeBase {

    @Override
    public void onGeolocationPermissionsHidePrompt() {
        super.onGeolocationPermissionsHidePrompt();
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        //super.onGeolocationPermissionsShowPrompt(origin, callback);
        MyPermissions.setCanAcceptOnlyCoarseLocationPermission(true);
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
        }, Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION);


    }
}
