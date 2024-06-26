package com.hss01248.bigimageviewpager;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.LogUtils;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.davemorrissey.labs.subscaleview.decoder.SkiaImageDecoder;
import com.davemorrissey.labs.subscaleview.decoder.SkiaImageRegionDecoder;
import com.hss01248.bigimageviewpager.databinding.ItemLargeImgSubsamplingBinding;
import com.hss01248.bigimageviewpager.databinding.StateItemLargeImgSubsamplingBinding;
import com.hss01248.bigimageviewpager.pano.MyPanoActivity;
import com.hss01248.bigimageviewpager.photoview.MyGifPhotoView;
import com.hss01248.glide.aop.file.AddByteUtil;
import com.hss01248.media.metadata.ExifUtil;
import com.hss01248.viewstate.StatefulLayout;
import com.hss01248.viewstate.ViewStateConfig;
import com.tencent.qcloud.image.avif.subsampling.AvifSubsamplingImageDecoder;
import com.tencent.qcloud.image.avif.subsampling.AvifSubsamplingImageRegionDecoder;

import java.io.File;
import java.util.Map;

public class MyLargeImageViewBySubSamplingView extends FrameLayout {
    MyGifPhotoView gifView;
    SubsamplingScaleImageView jpgView;

    ImageView ivHelper;
    TextView tvScale;

    StateItemLargeImgSubsamplingBinding stateBinding;
    ItemLargeImgSubsamplingBinding largeImgBinding;

    StatefulLayout stateManager;
    LargeImageInfo info = new LargeImageInfo();

    public float getMaxScaleRatio() {
        return maxScaleRatio;
    }

    public void setMaxScaleRatio(float maxScaleRatio) {
        this.maxScaleRatio = maxScaleRatio;
        jpgView.setMaxScale(maxScaleRatio);
        gifView.setMaximumScale(maxScaleRatio);
    }

    public static float defaultMaxScaleRatio = 8f;
    float maxScaleRatio = defaultMaxScaleRatio;

    public void setShowScale(boolean showScale) {
        this.showScale = showScale;
    }

    boolean showScale = true;

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
        largeImgBinding.getRoot().setBackgroundColor(darkMode? Color.BLACK:Color.WHITE);
    }

    boolean darkMode = true;


    public MyLargeImageViewBySubSamplingView(@NonNull Context context) {
        super(context);
        try {
            init(context);
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }

    }

    private void init(Context context) {
        stateBinding = StateItemLargeImgSubsamplingBinding.inflate(LayoutInflater.from(context),this,true);
        largeImgBinding = stateBinding.itemLargeImg;
        stateManager = stateBinding.stateLayout;
        stateBinding.stateLayout.setConfig(
                ViewStateConfig.newBuilder(ViewStateConfig.getGlobalConfig())
                        .errorClick(new Runnable() {
                    @Override
                    public void run() {
                        reload();
                    }
                }).darkMode(true)
                        .build());
        largeImgBinding.getRoot().setBackgroundColor(darkMode? Color.BLACK:Color.WHITE);
        ivHelper = largeImgBinding.ivGlideHelper;
        tvScale = largeImgBinding.tvScale;
        jpgView = largeImgBinding.ivLarge;
        gifView = largeImgBinding.gifLarge;
        jpgView.setDebug(AppUtils.isAppDebug());
        jpgView.setMaxScale(12);



        //scale显示
// if(showScale)
//                tvScale.setText(percent+"%");
        jpgView.setOnStateChangedListener(new SubsamplingScaleImageView.OnStateChangedListener() {
            @Override
            public void onScaleChanged(float newScale, int origin) {
                if(showScale){
                    String text = String.format("%.1f",newScale*100)+"%";
                    tvScale.setText(text);
                }

            }

            @Override
            public void onCenterChanged(PointF newCenter, int origin) {

            }
        });

        gifView.setOnScaleChangeListener(new MyLargeJpgView.OnScaleChangeListener() {
            @Override
            public void onScaleChanged(int percent, float scale) {
                if(showScale){
                    String text = String.format("%.1f",scale*100)+"%";
                    tvScale.setText(text);
                }

            }
        });
        largeImgBinding.ivGo360.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //viewAsPano = true;
                //reload();
                MyPanoActivity.start(info.localPathOrUri);
            }
        });


    }


    public MyLargeImageViewBySubSamplingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyLargeImageViewBySubSamplingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MyLargeImageViewBySubSamplingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
        /*preOrientation = context.getResources().getConfiguration().orientation;
        if(preOrientation == Configuration.ORIENTATION_LANDSCAPE){
            stateBinding.itemLargeImg.setVisibility(GONE);
        }else {
            tvScale.setVisibility(VISIBLE);
        }*/
    }

    public void setOritation(boolean isLandscape, boolean fromConfigChange){
        if(jpgView != null){
            //jpgView.setOritation(isLandscape,fromConfigChange);
        }
    }

    private void reload() {
        loadUri(info.uri);

    }

    int preOrientation;
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        /*if(newConfig.orientation != preOrientation){
            preOrientation = newConfig.orientation;
            if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
                tvScale.setVisibility(GONE);
            }else {
                tvScale.setVisibility(VISIBLE);
            }
        }*/
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        if(jpgView != null){
            jpgView.setOnClickListener(l);
        }else if(gifView != null){
            gifView.setOnClickListener(l);
        }
    }

    private void loadUrl(String url) {

        stateManager.showLoading();

        //todo 这里有内存泄漏
        UrlLoader.download(getContext(), ivHelper, url, new UrlLoader.LoadListener() {
            @Override
            public void onLoad(String path) {
                loadFile(path, url.contains(".gif"));
                //statefulFrameLayout.showError("fail download");
            }

            @Override
            public void onProgress(int progress) {
                stateManager.showLoading(progress+"%");
            }

            @Override
            public void onFail(Throwable throwable) {
                if (throwable != null) {
                    info.throwable = throwable;
                    LogUtils.w(throwable,url);
                    //toastMsg(throwable.getMessage());
                    stateManager.showError(throwable.getMessage());
                } else {
                    //toastMsg("fail download");
                    info.throwable = new Throwable("fail download");
                    stateManager.showError("fail download");
                }
            }
        });


    }

    private void loadFile(String filePath) {
        loadFile(filePath, false);
    }

    private void loadFile(String filePath, boolean isGif) {
        loadLocal(filePath, isGif);
    }

    private void toastMsg(String message) {
        Toast.makeText(getContext().getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public void loadUri(String uri) {
        info.uri = uri;
        //if(uri.startsWith("file://") || uri.startsWith("/storage/"))
        if (uri.startsWith("http")) {
            loadUrl(uri);
            return;
        }
        if (uri.startsWith("/storage/")) {
            loadFile(uri, false);
            return;
        }
        if (uri.startsWith("file://")) {
            loadFile(uri.substring("file://".length()), false);
            return;
        }
        if (uri.startsWith("content://")) {
            loadLocal(uri, false);
            return;
        }
    }

    public LargeImageInfo getInfo(){
        return info;
    }

    public String getInfoStr(){
        return info.getInfo();
    }

    private void loadLocal(String uri, boolean isGif) {
        info.localPathOrUri = uri;
        largeImgBinding.ivGo360.setVisibility(GONE);
        if (uri.contains(".gif") || isGif) {
            gifView.setVisibility(VISIBLE);
            jpgView.setVisibility(GONE);

            if (uri.startsWith("content://") || uri.startsWith("file://")) {
                gifView.setImageURI(Uri.parse(uri));
            } else {
                File tmpOriginalFile = AddByteUtil.createTmpOriginalFile(uri);
                gifView.setImageURI(Uri.fromFile(tmpOriginalFile));
            }
            stateManager.showContent();
        } else {
            gifView.setVisibility(GONE);
            jpgView.setVisibility(VISIBLE);
            //兼容avif格式
            if(uri.contains(".avif")){
                File tmpOriginalFile = AddByteUtil.createTmpOriginalFile(uri);
                //todo 如何获取avif图片的宽高
                stateManager.showLoading();
                // 设置AVIF图片解码器
                jpgView.setBitmapDecoderClass(AvifSubsamplingImageDecoder.class);
                jpgView.setRegionDecoderClass(AvifSubsamplingImageRegionDecoder.class);

                jpgView.setImage(ImageSource.uri(Uri.fromFile(tmpOriginalFile)));
                //jpgView.setImage(ImageSource.bitmap());
                stateManager.showContent();

                return;
            }

            //判断是否为360全景图

            File tmpOriginalFile = AddByteUtil.createTmpOriginalFile(uri);
            jpgView.setVisibility(VISIBLE);
            // 设置普通图片解码器
            jpgView.setBitmapDecoderClass(SkiaImageDecoder.class);
            jpgView.setRegionDecoderClass(SkiaImageRegionDecoder.class);
            if(isPanoramaImage(tmpOriginalFile.getAbsolutePath())){
                largeImgBinding.ivGo360.setVisibility(VISIBLE);
            }
            jpgView.setImage(ImageSource.uri(Uri.fromFile(tmpOriginalFile)));
        }
    }

    public static boolean isPanoramaImage(String path){
        Map<String, String> map = ExifUtil.readExif(path);
        String xml = map.get("Xmp");
        if(!TextUtils.isEmpty(xml) ){
            if(xml.contains("GPano:UsePanoramaViewer")){
                LogUtils.i("根据exif特征识别出为360全景图,不进行压缩");
                return true;
            }
            if(xml.contains("MotionPhoto")){
                LogUtils.i("根据exif特征识别出为MotionPhoto,不进行压缩");
                return true;
            }
            //MotionPhoto
        }
        return false;
    }
}
