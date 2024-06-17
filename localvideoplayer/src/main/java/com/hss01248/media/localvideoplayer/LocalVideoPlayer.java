package com.hss01248.media.localvideoplayer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ClickUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPStaticUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hss01248.bitmap_saver.BitmapSaveUtil;
import com.shuyu.gsyvideoplayer.listener.GSYVideoShotListener;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView;
@Keep
public class LocalVideoPlayer extends StandardGSYVideoPlayer {
    public LocalVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public LocalVideoPlayer(Context context) {
        super(context);
    }

    public LocalVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    SeekBar seekBar;
    ImageView tvPre;
    ImageView tvNext;
    LinearLayout llSingle;
    RelativeLayout bottomLl;
    ImageView ivBack;
    TextView tvSpeed;
    ImageView ivSnapshot;
    LinearLayout llTop;
    TextView tvError;
  protected ProgressBar pbLoading;

    public void setCurrentName(String currentName) {
        this.currentName = currentName;
    }

    protected String currentName;
    //VideoLayoutLocalBinding binding;


    public void setActivity(PictureVideoPlayByGSYActivity activity) {
        this.activity = activity;
    }

    PictureVideoPlayByGSYActivity activity;

    @Override
    protected void init(Context context) {
        super.init(context);
        seekBar = findViewById(R.id.progress);
        tvPre = findViewById(R.id.tv_play_pre);
        tvNext = findViewById(R.id.tv_play_next);
        llSingle = findViewById(R.id.bottom_single);
        llTop = findViewById(R.id.layout_top);
        bottomLl = findViewById(R.id.layout_bottom);
        ivBack = findViewById(R.id.back);
        tvSpeed = findViewById(R.id.tv_speedup);
        ivSnapshot = findViewById(R.id.iv_screenshot);
        pbLoading = findViewById(R.id.pb_loading);
        tvError = findViewById(R.id.tv_error);
        preOrNext();
        if(context instanceof PictureVideoPlayByGSYActivity){
            activity = (PictureVideoPlayByGSYActivity) context;
        }
        ivBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
        tvSpeed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showSpeedChooseDialog(tvSpeed.getText().toString());
            }
        });
        if(SPStaticUtils.getBoolean("ShowScreenshotAlways",false)){
            ivSnapshot.setVisibility(VISIBLE);
        }else{
            ivSnapshot.setVisibility(GONE);
        }

        ClickUtils.expandClickArea(ivSnapshot, SizeUtils.dp2px(16));
        ivSnapshot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                taskShotPic(new GSYVideoShotListener() {
                    @Override
                    public void getBitmap(Bitmap bitmap) {
                        //ToastUtils.showShort("截屏....");
                        try {

                            BitmapSaveUtil.saveBitmap(bitmap);
                            ToastUtils.showShort("保存截图成功");
                           // ToastUtils.showLong(getString2(R.string.saved_success, Utils.getApp()));
                        } catch (Exception e) {
                            LogUtils.w(e);
                            ToastUtils.showShort("截图失败:"+e.getMessage());
                           // ToastUtils.showLong(getString2(R.string.deliver_error_save_failed,Utils.getApp()));
                        }
                    }
                },true);
            }
        });
        bottomLl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bottomLl.getVisibility() == View.VISIBLE){
                    bottomLl.setVisibility(View.GONE);
                }
                if(llTop.getVisibility() == View.VISIBLE){
                    llTop.setVisibility(View.GONE);
                }
            }
        });
    }

    private void showSpeedChooseDialog(String text) {
        CharSequence[] items = {"x0.5","x0.8","x1","x1.25","x1.5","x2","x3","x4"};
        int idx = 0;
        for (int i = 0; i < items.length; i++) {
            if(text.equals(items[i])){
                idx = i;
                break;
            }
        }
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setSingleChoiceItems(items, idx, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String str = items[which].toString();
                        tvSpeed.setText(str);
                        setSpeed(Float.parseFloat(str.substring(1)));
                        dialog.dismiss();
                    }
                }).setTitle("选择播放速度,当前为: "+text)
                //.setMessage("当前播放速度为: " + text)
        .setNegativeButton("取消", null).create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog2) {
                BarUtils.setStatusBarColor(dialog.getWindow(), Color.TRANSPARENT);
                BarUtils.setStatusBarVisibility(dialog.getWindow(),true);
                BarUtils.setNavBarColor(dialog.getWindow(),Color.TRANSPARENT);
            }
        });

        dialog.show();

    }


    public void setVideoList(boolean videoList) {
        isVideoList = videoList;
        if(isVideoList){
            llSingle.setVisibility(VISIBLE);
            preOrNext();
        }else {
            llSingle.setVisibility(VISIBLE);
        }
    }

    @Override
    public GSYBaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {
        GSYBaseVideoPlayer player =  super.startWindowFullscreen(context, actionBar, statusBar);
        return player;
    }


    private void preOrNext() {
        tvPre.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               activity.onPlayPre();
            }
        });

        tvNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onPlayNext();
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            if(v.getId()==R.id.tv_play_next || v.getId()==R.id.tv_play_pre){
                return true;
            }
        }
        if(event.getAction() == MotionEvent.ACTION_UP){
            if(v.getId()==R.id.tv_play_next || v.getId()==R.id.tv_play_pre){
                v.performClick();
                return true;
            }
        }


        return super.onTouch(v, event);
    }

    boolean isVideoList;

    @Override
    protected void changeUiToPreparingShow() {
        //super.changeUiToPreparingShow();
        hideAllWidget();
    }

    @Override
    protected void hideAllWidget() {
        super.hideAllWidget();
        mBottomProgressBar.setVisibility(GONE);
        if(SPStaticUtils.getBoolean("ShowScreenshotAlways",false)){
            ivSnapshot.setVisibility(VISIBLE);
        }else{
           // ivSnapshot.setVisibility(GONE);
        }
    }

    @Override
    public void onError(int what, int extra) {
        super.onError(what, extra);
    }



    @Override
    public void startPlayLogic() {
        //super.startPlayLogic();
        if (mVideoAllCallBack != null) {
            Debuger.printfLog("onClickStartThumb");
            mVideoAllCallBack.onClickStartThumb(mOriginUrl, mTitle, LocalVideoPlayer.this);
        }

        prepareVideo();
        hideAllWidget();
        //changeUiToPlayingShow();
        if(mTitleTextView != null){
            mTitleTextView.requestFocus();
        }


        //startWindowFullscreen(activity,false,false);
    }


    @Override
    protected void setStateAndUi(int state) {
        super.setStateAndUi(state);
        LogUtils.i("播放状态变更: state: "+state);
        if(state == GSYVideoView.CURRENT_STATE_PREPAREING
                || state == GSYVideoView.CURRENT_STATE_PLAYING_BUFFERING_START ){
            hideAllWidget();
            pbLoading.setVisibility(View.VISIBLE);
            tvError.setVisibility(View.GONE);
            //mLoadingProgressBar, VISIBLE
            setViewShowState(mLoadingProgressBar,GONE);
        }else if(state == GSYVideoView.CURRENT_STATE_ERROR) {
            pbLoading.setVisibility(View.GONE);
            tvError.setVisibility(View.VISIBLE);
            tvError.setText("播放出错,请点击重试");
        }else {
            hideAllWidget();
            pbLoading.setVisibility(View.GONE);
            tvError.setVisibility(View.GONE);

        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.video_layout_local;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //int progress = SPStaticUtils.getInt(mOriginUrl+"_progress",0);
        //seekTo(progress);
        float bright =   SPStaticUtils.getFloat("bright_percent",-1f);
        LogUtils.d("onAttachedToWindow  bright: "+ bright);
        if(bright >0){
            WindowManager.LayoutParams lpa = ((Activity) (mContext)).getWindow().getAttributes();
            lpa.screenBrightness = bright;
            ((Activity) (mContext)).getWindow().setAttributes(lpa);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onBrightnessSlide(float percent) {

        super.onBrightnessSlide(percent);

    }

    @Override
    protected void changeUiToNormal() {
        super.changeUiToNormal();
        hideAllWidget();
    }

    @Override
    protected void showBrightnessDialog(float percent) {
        super.showBrightnessDialog(percent);
        float b = ((Activity) (mContext)).getWindow().getAttributes().screenBrightness;
        SPStaticUtils.put("bright_percent",b);
        LogUtils.d("onBrightnessSlide  bright: "+ b);
    }
}
