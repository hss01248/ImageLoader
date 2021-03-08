package com.hss01248.image.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;

import androidx.fragment.app.Fragment;

import android.view.View;

import com.hss01248.image.ImageLoader;

/**
 * Created by huangshuisheng on 2019/1/25.
 */
public class ContextUtil {


    public static boolean isNotUsable(Object context) {
        return !isUseable(context);
    }

    /**
     * 注意: 这里不要使用isDetached()来判断,因为Fragment被detach之后,它的isDetached()方法依然可能返回false
     * 2.如果Fragment A是因为被replace而detach的,那么它的isDetached()将返回false
     * 3.如果Fragment A对应的FragmentTransaction被加入到返回栈中,因为出栈而detach,那么它的isDetached()将返回true
     *
     * @param context
     * @return
     */
    public static boolean isUseable(Object context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Fragment) {
            Fragment fragment = (Fragment) context;
            if (fragment.getActivity() == null) {
                return false;
            }
            /*if(fragment.isRemoving()){
                return false;
            }
            if(fragment.isDetached()){
                return false;
            }*/
            return true;
        } else if (context instanceof android.app.Fragment) {
            android.app.Fragment fragment = (android.app.Fragment) context;
            if (fragment.getActivity() == null) {
                return false;
            }
           /* if(fragment.isRemoving()){
                return false;
            }
            if(fragment.isDetached()){
                return false;
            }*/
            return true;
        } else if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.isFinishing()) {
                return false;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return !activity.isDestroyed();
            } else {
                if (ImageLoader.config != null && ImageLoader.config.getActivityStack() != null) {
                    return ImageLoader.config.getActivityStack().contains(activity);
                } else {
                    return true;
                }
            }

        } else if (context instanceof Context) {
           /* Context context1 = (Context) context;
            Activity activity = getActivityFromContext(context1);
            if(activity == null){
                return true;
            }
            if(activity.isFinishing()){
                return false;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if(activity.isDestroyed()){
                    return false;
                }
            }*/
            return true;
        } else {
            return false;
        }
    }

    public static Activity getActivityFromView(View view) {
        if (null != view) {
            Context context = view.getContext();
            return getActivityFromContext(context);
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
