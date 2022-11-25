package com.hss01248.basewebview.dom;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.blankj.utilcode.util.ActivityUtils;
import com.just.agentweb.MiddlewareWebChromeBase;

/**
 * @author: Administrator
 * @date: 2022/11/24
 * @desc:  agentweb已经实现,但似乎有bug
 * https://www.jianshu.com/p/a7ac66691359
 */
public class VideoFullScreenImpl extends MiddlewareWebChromeBase {

    private View mCustomView;
    private ViewGroup mRootViewGroup;

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        //super.onShowCustomView(view, callback);

        //华为手机WebView全屏重写onShowCustomView()白屏问题
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                callback.onCustomViewHidden();
            }
        });

        // 进入全屏
        if (mCustomView != null) {
            return;
        }
        mCustomView = view;
        mCustomView.setLayoutParams(new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT));
        // 或者 getActivity().getWindow().getDecorView(); 获取根视图并addView
        mRootViewGroup = ActivityUtils.getTopActivity().findViewById(android.R.id.content); //获取根视图并addView
        mRootViewGroup .addView(mCustomView); // 添加到根视图

        if (ActivityUtils.getTopActivity() != null) {
            //设置横屏
            ActivityUtils.getTopActivity().setRequestedOrientation(SCREEN_ORIENTATION_LANDSCAPE);
            //设置全屏
            ActivityUtils.getTopActivity().getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }


    }

    @Override
    public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
        //super.onShowCustomView(view, requestedOrientation, callback);
        onShowCustomView(view, callback);
    }

    @Override
    public void onHideCustomView() {
       // super.onHideCustomView();
        //退出全屏
        if (mCustomView == null) {
            return;
        }
        //移除全屏视图并隐藏
        mRootViewGroup.removeView(mCustomView);
        mCustomView = null;
        if (ActivityUtils.getTopActivity() != null) {
            //设置竖屏
            ActivityUtils.getTopActivity().setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
            //清除全屏
            ActivityUtils.getTopActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }


    }


    // <video /> 控件在未播放时，会展示为一张海报图，HTML中可通过它的'poster'属性来指定。
// 如果未指定'poster'属性，则通过此方法提供一个默认的海报图。
/*    public Bitmap getDefaultVideoPoster() {
    return null;
    }

// 当全屏的视频正在缓冲时，此方法返回一个占位视图(比如旋转的菊花)。
    public View getVideoLoadingProgressView() {
    return null;
    }*/

}
