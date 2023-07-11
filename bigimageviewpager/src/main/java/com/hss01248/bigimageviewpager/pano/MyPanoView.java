package com.hss01248.bigimageviewpager.pano;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.vr.sdk.widgets.pano.ExtendVrPanoramaView;
import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;
import com.hss01248.bigimageviewpager.LifecycleObjectUtil2;
import com.hss01248.media.metadata.ExifUtil;

import java.util.Map;

/**
 * @Despciption todo
 * @Author hss
 * @Date 20/02/2023 11:15
 * @Version 1.0
 */
public class MyPanoView extends RelativeLayout implements DefaultLifecycleObserver {
    private VrPanoramaView mVrPanoramaView;
    private VrPanoramaView.Options paNormalOptions;
    public MyPanoView(Context context) {
        this(context,null);
    }

    public MyPanoView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MyPanoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init2();
    }



    private void init2() {
        mVrPanoramaView = new ExtendVrPanoramaView(getContext());
        paNormalOptions = new VrPanoramaView.Options();
        paNormalOptions.inputType = VrPanoramaView.Options.TYPE_MONO;
        mVrPanoramaView.setFullscreenButtonEnabled (false); //隐藏全屏模式按钮
        mVrPanoramaView.setInfoButtonEnabled(false); //设置隐藏最左边信息的按钮
        mVrPanoramaView.setStereoModeButtonEnabled(false); //设置隐藏立体模型的按钮
        mVrPanoramaView.setEventListener(new ActivityEventListener()); //设置监听
        mVrPanoramaView.setFlingingEnabled(true);

        //设置为跟随手移动,而不是传感器
        mVrPanoramaView.setTouchTrackingEnabled(true);
        mVrPanoramaView.setPureTouchTracking(true);

        //mVrPanoramaView.setVerticalFadingEdgeEnabled(true);

        LifecycleOwner lifecycleOwnerFromObj = LifecycleObjectUtil2.getLifecycleOwnerFromObj(getContext());
        if(lifecycleOwnerFromObj !=  null){
            lifecycleOwnerFromObj.getLifecycle().addObserver(this);
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mVrPanoramaView.setLayoutParams(params);
        addView(mVrPanoramaView);


       // GestureViewBinder.bind(getContext(), this, mVrPanoramaView).setFullGroup(true);//不起作用
        // mVrPanoramaView.loadImageFromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.andes), paNormalOptions);
    }



    public void loadBitmap(Bitmap bitmap){
        mVrPanoramaView.loadImageFromBitmap(bitmap, paNormalOptions);
        //mVrPanoramaView.resumeRendering();
    }

    public void loadFile(String path){
        byte[] bytes = FileIOUtils.readFile2BytesByChannel(path);
        mVrPanoramaView.loadImageFromByteArray(bytes, paNormalOptions);
        //mVrPanoramaView.resumeRendering();
    }

    public static boolean isPanoramaImage(String path){
        Map<String, String> map = ExifUtil.readExif(path);
        String xml = map.get("Xmp");
        if(!TextUtils.isEmpty(xml) ){
            if(xml.contains("GPano:UsePanoramaViewer")){
                return true;
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MyPanoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr);
    }


    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onResume(owner);
        mVrPanoramaView.resumeRendering();
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onPause(owner);
        mVrPanoramaView.pauseRendering();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onDestroy(owner);
        mVrPanoramaView.shutdown();
    }

    private class ActivityEventListener extends VrPanoramaEventListener {
        @Override
        public void onLoadSuccess() {//图片加载成功
        }


        @Override
        public void onLoadError(String errorMessage) {//图片加载失败
            ToastUtils.showShort(errorMessage);
        }

        @Override
        public void onClick() {//当我们点击了VrPanoramaView 时候触发            super.onClick();
        }

        @Override
        public void onDisplayModeChanged(int newDisplayMode) {
            //改变显示模式时候出发（全屏模式和纸板模式）
            super.onDisplayModeChanged(newDisplayMode);
        }
    }

}
