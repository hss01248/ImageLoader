package com.hss01248.bigimageviewpager;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hss01248.bigimageviewpager.databinding.ItemLargeImgBinding;
import com.hss01248.bigimageviewpager.databinding.StateItemLargeImgBinding;
import com.hss01248.bigimageviewpager.pano.MyPanoActivity;
import com.hss01248.bigimageviewpager.photoview.MyGifPhotoView;
import com.hss01248.glide.aop.file.AddByteUtil;
import com.hss01248.glide.aop.file.ReadFileUtil;
import com.hss01248.media.metadata.ExifUtil;
import com.hss01248.motion_photos.MotionPhotoUtil;
import com.hss01248.viewstate.StatefulLayout;
import com.hss01248.viewstate.ViewStateConfig;
import com.shizhefei.view.largeimage.factory.InputStreamBitmapDecoderFactory;

import java.io.File;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

@Deprecated
public class MyLargeImageView extends FrameLayout {
    MyGifPhotoView gifView;
    MyLargeJpgView jpgView;

    ImageView ivHelper;
    TextView tvScale;

    StateItemLargeImgBinding stateBinding;
    ItemLargeImgBinding largeImgBinding;

    StatefulLayout stateManager;
    LargeImageInfo info = new LargeImageInfo();

    public float getMaxScaleRatio() {
        return maxScaleRatio;
    }

    public void setMaxScaleRatio(float maxScaleRatio) {
        this.maxScaleRatio = maxScaleRatio;
        jpgView.setMaxScaleRatio(maxScaleRatio);
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
    PlayerView playerView;

    public MyLargeImageView(@NonNull Context context) {
        super(context);
        try {
            init(context);
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }

    }

    ImageView ivPlayVideo;
    private void init(Context context) {
        stateBinding = StateItemLargeImgBinding.inflate(LayoutInflater.from(context),this,true);
        largeImgBinding = stateBinding.itemLargeImg;
        stateManager = stateBinding.stateLayout;
        playerView = stateBinding.itemLargeImg.playView;
        ivPlayVideo = stateBinding.itemLargeImg.ivPlayVideo;
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



        //scale显示

        jpgView.setOnScaleChangeListener(new MyLargeJpgView.OnScaleChangeListener() {
            @Override
            public void onScaleChanged(int percent, float scale) {
                if(showScale)
                tvScale.setText(percent+"%");
            }
        });

        gifView.setOnScaleChangeListener(new MyLargeJpgView.OnScaleChangeListener() {
            @Override
            public void onScaleChanged(int percent, float scale) {
                if(showScale)
                tvScale.setText(percent+"%");
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


    public MyLargeImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyLargeImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MyLargeImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
            jpgView.setOritation(isLandscape,fromConfigChange);
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

    private void loadUrl(String url, boolean loadMotionVideo) {

        stateManager.showLoading();
        UrlLoader.download(getContext(), ivHelper, url, new UrlLoader.LoadListener() {
            @Override
            public void onLoad(String path) {
                loadFile(path, url.contains(".gif"),loadMotionVideo);
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
                    LogUtils.w(throwable);
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

    @Deprecated
    private void loadFile(String filePath) {
        loadFile(filePath, false,false);
    }
    @Deprecated
    private void loadFile(String filePath, boolean isGif, boolean loadMotionVideo) {
        loadLocal(filePath, isGif,loadMotionVideo);
    }

    private void toastMsg(String message) {
        Toast.makeText(getContext().getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public void loadUri(String uri,boolean loadMotionVideo) {
        info.uri = uri;
        //if(uri.startsWith("file://") || uri.startsWith("/storage/"))
        if (uri.startsWith("http")) {
            loadUrl(uri,loadMotionVideo);
            return;
        }
        if (uri.startsWith("/storage/")) {
            loadFile(uri, false,loadMotionVideo);
            return;
        }
        if (uri.startsWith("file://")) {
            loadFile(uri.substring("file://".length()), false,loadMotionVideo);
            return;
        }
        if (uri.startsWith("content://")) {
            loadLocal(uri, false,loadMotionVideo);
            return;
        }
    }
    @Deprecated
    public void loadUri(String uri) {
        loadUri(uri,true);
    }

    public LargeImageInfo getInfo(){
        return info;
    }

    public String getInfoStr(){
        return info.getInfo();
    }

    private void loadLocal(String uri, boolean isGif,boolean loadMotionVideo) {
        info.localPathOrUri = uri;
        stateManager.showContent();
        if( MotionPhotoUtil.isMotionImage(uri,false)){
            if(loadMotionVideo){
                loadMotionVideo(uri);
                return;
            }
            ivPlayVideo.setVisibility(VISIBLE);
            ivPlayVideo.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    loadLocal(info.localPathOrUri,isGif,true);
                }
            });

        }else {
            ivPlayVideo.setVisibility(GONE);
        }

        if (uri.contains(".gif") || isGif) {
            gifView.setVisibility(VISIBLE);
            largeImgBinding.ivGo360.setVisibility(GONE);
            jpgView.setVisibility(GONE);

            if (uri.startsWith("content://") || uri.startsWith("file://")) {
                gifView.setImageURI(Uri.parse(uri));
            } else {
                File tmpOriginalFile = AddByteUtil.createTmpOriginalFile(uri);
                gifView.setImageURI(Uri.fromFile(tmpOriginalFile));
            }
            stateManager.showContent();
            playerView.setVisibility(GONE);
        } else {

            gifView.setVisibility(GONE);
            jpgView.setVisibility(VISIBLE);
            if(MotionPhotoUtil.isMotionImage(uri,false) ){
                if(playerView.getVisibility() != View.GONE){
                    LargeImageViewer.fadeToGone(playerView,500);
                }
                ivHelper.setVisibility(VISIBLE);
                ivHelper.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        loadLocal(info.localPathOrUri, isGif, true);
                    }
                });

            }else {
                playerView.setVisibility(View.GONE);
            }
            //兼容avif格式
            if(uri.contains(".avif")){
                File tmpOriginalFile = AddByteUtil.createTmpOriginalFile(uri);
                //todo 如何获取avif图片的宽高
                stateManager.showLoading();
                Glide.with(getContext())
                        .asBitmap()
                        .load(tmpOriginalFile)
                        //.override(w,h)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                Log.d("large","large image load onResourceReady:"+resource.getWidth()+"x"+resource.getHeight());
                                if(resource != null){
                                    jpgView.setImage(resource);
                                    stateManager.showContent();
                                }else {
                                    onLoadFailed(null);
                                    //jpgView.setImageDrawable(new BitmapDrawable(BitmapFactory.decodeFile(uri.replace(".avif",",jpg"))));
                                   // stateManager.showContent();
                                }
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                Log.w("avif","large image load failed:"+tmpOriginalFile);
                                info.throwable = new Throwable("load avif faild");
                                stateManager.showError(errorDrawable+" - load avif failed:\n"+tmpOriginalFile+"\n"+tmpOriginalFile.exists());
                            }
                        });
                return;
            }

            //判断是否为360全景图

            jpgView.setVisibility(VISIBLE);

            Disposable subscribe = Observable.just(uri)
                    .subscribeOn(Schedulers.io())
                    .map(new Function<String, InputStreamBitmapDecoderFactory>() {
                        @Override
                        public InputStreamBitmapDecoderFactory apply(String uri) throws Exception {
                            if(isPanoramaImage(info.localPathOrUri)){
                                ThreadUtils.getMainHandler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        largeImgBinding.ivGo360.setVisibility(VISIBLE);
                                    }
                                });
                            }else {
                                ThreadUtils.getMainHandler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        largeImgBinding.ivGo360.setVisibility(GONE);
                                    }
                                });
                            }
                            if (uri.startsWith("content://") || uri.startsWith("file://")) {
                                try {
                                    return new InputStreamBitmapDecoderFactory(
                                            ReadFileUtil.read(getContext().getContentResolver().openInputStream(Uri.parse(uri))));
                                } catch (Throwable e) {
                                    throw new RuntimeException(e);
                                }
                            } else if (uri.startsWith("/storage/")) {
                                try {
                                    return new InputStreamBitmapDecoderFactory(ReadFileUtil.read(new File(uri)));
                                } catch (Throwable e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            try {
                                return new InputStreamBitmapDecoderFactory(ReadFileUtil.read(new File(uri)));
                            } catch (Throwable e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<InputStreamBitmapDecoderFactory>() {
                        @Override
                        public void accept(InputStreamBitmapDecoderFactory inputStreamBitmapDecoderFactory) throws Exception {

                            jpgView.setImage(inputStreamBitmapDecoderFactory);
                            stateManager.showContent();
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            throwable.printStackTrace();
                            //toastMsg(throwable.getMessage());
                            info.throwable = throwable;
                            stateManager.showError(throwable.getMessage());
                        }
                    });
        }
    }
    ExoPlayer player;
    Player.Listener listener;
    private void loadMotionVideo(String uri) {
        String motionVideoPath = MotionPhotoUtil.getMotionVideoPath(uri);
        if(motionVideoPath ==null || motionVideoPath.equals("")){
            return;
        }
        ivHelper.setVisibility(GONE);
        playerView.setVisibility(VISIBLE);
        gifView.setVisibility(GONE);
        LargeImageViewer.fadeToGone(jpgView,300);
        //jpgView.setVisibility(GONE);
        File file = new File(motionVideoPath);

        if(player !=null){
            if(player.isPlaying()){
                player.stop();
            }else if(player.isLoading()){
                player.stop();
            }
            player.release();
            player = null;
        }

             player = new ExoPlayer.Builder(getContext())
                    .build();
            playerView.setPlayer(player);
             listener = new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    Player.Listener.super.onPlaybackStateChanged(playbackState);
                    if(playbackState == Player.STATE_ENDED ){
                        loadLocal(uri,false,false);
                    }
                }

                @Override
                public void onPlayerError(PlaybackException error) {
                    Player.Listener.super.onPlayerError(error);
                    LogUtils.w(error);
                    loadLocal(uri,false,false);
                }
            };
             player.addListener(listener);
            player.setPlayWhenReady(true);
            player.setRepeatMode(Player.REPEAT_MODE_OFF);


        //sending message to a Handler on a dead thread
        try{
            //IllegalStateException: Handler (android.os.Handler) {674e76a} sending message to a Handler on a dead thread
            player.setMediaItem(MediaItem.fromUri(Uri.fromFile(file)));
            player.prepare();
        }catch (Throwable throwable){
            LogUtils.w(throwable);
            player.release();
            player = null;
            loadLocal(uri,false,false);
        }

    }

    public void pausePlayer(){
        if(player !=null){
            if(player.isPlaying() || player.isLoading()){
                player.stop();
            }
            player.release();
            player = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        LogUtils.d("onDetachedFromWindow-"+this);
        try {
            if(player !=null){
                if(player.isPlaying()){
                    player.stop();
                }
                player.release();
            }
        }catch (Throwable throwable){
            LogUtils.w(throwable);
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
            //MotionPhoto
        }
        return false;
    }
}
