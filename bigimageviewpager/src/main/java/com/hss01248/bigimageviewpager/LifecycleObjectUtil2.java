package com.hss01248.bigimageviewpager;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

/**
 * @Despciption todo
 * @Author hss
 * @Date 11/08/2022 10:04
 * @Version 1.0
 */
public class LifecycleObjectUtil2 {

    public static LifecycleOwner getLifecycleOwnerFromObj(Object contextOrFragment){
        if(contextOrFragment instanceof LifecycleOwner){
            LifecycleOwner owner = (LifecycleOwner) contextOrFragment;
            return owner;
        }
        if(contextOrFragment instanceof Context){
            Activity activity = getActivityFromContext((Context) contextOrFragment);
            if(activity instanceof LifecycleOwner){
                return (LifecycleOwner)activity;
            }
            return null;
        }
        return null;
    }
    public static Object getLifecycledObjectFromObj(Object contextOrFragment){
        if(contextOrFragment instanceof LifecycleOwner){
            LifecycleOwner owner = (LifecycleOwner) contextOrFragment;
            return owner;
        }
        if(contextOrFragment instanceof Context){
            Activity activity = getActivityFromContext((Context) contextOrFragment);
            return activity;
        }
        if(contextOrFragment instanceof Fragment){
            return contextOrFragment;
        }
        if(contextOrFragment instanceof android.app.Fragment){
            return contextOrFragment;
        }
        return null;
    }



    public static Activity getActivityFromContext(Context context) {
        if (context == null) {
            return null;
        }
        if (context instanceof Activity) {
            return (Activity) context;
        }
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}
