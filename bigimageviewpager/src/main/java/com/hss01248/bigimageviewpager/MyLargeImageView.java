package com.hss01248.bigimageviewpager;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hss01248.bigimageviewpager.databinding.ItemLargeImgBinding;
import com.hss01248.bigimageviewpager.databinding.StateItemLargeImgBinding;
import com.hss01248.bigimageviewpager.photoview.MyGifPhotoView;
import com.hss01248.pagestate.PageStateConfig;
import com.hss01248.pagestate.StatefulFrameLayout;
import com.shizhefei.view.largeimage.factory.InputStreamBitmapDecoderFactory;

import java.io.File;
import java.io.FileInputStream;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MyLargeImageView extends FrameLayout {
    MyGifPhotoView gifView;
    MyLargeJpgView jpgView;
    MyPanoView panoView;
    boolean viewAsPano;
    ImageView ivHelper;
    TextView tvScale;

    StateItemLargeImgBinding stateBinding;
    ItemLargeImgBinding largeImgBinding;

    StatefulFrameLayout stateManager;
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
    }

    boolean darkMode = true;


    public MyLargeImageView(@NonNull Context context) {
        super(context);
        try {
            init(context);
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }

    }

    private void init(Context context) {
        stateBinding = StateItemLargeImgBinding.inflate(LayoutInflater.from(context),this,true);
        largeImgBinding = stateBinding.itemLargeImg;
        stateManager = stateBinding.stateLayout;
        stateBinding.stateLayout.init(new PageStateConfig() {
            @Override
            public void onRetry(View retryView) {
                reload();
            }

            @Override
            public boolean isFirstStateLoading() {
                return false;
            }

            @Override
            public boolean darkMode() {
                return darkMode;
            }
        });
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
                viewAsPano = true;
                reload();
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

    private void loadUrl(String url) {

        stateManager.showLoading();
        UrlLoader.download(getContext(), ivHelper, url, new UrlLoader.LoadListener() {
            @Override
            public void onLoad(String path) {
                loadFile(path, url.contains(".gif"));
                //statefulFrameLayout.showError("fail download");
            }

            @Override
            public void onProgress(int progress) {
                stateManager.showLoading(progress);
            }

            @Override
            public void onFail(Throwable throwable) {

                if (throwable != null) {
                    info.throwable = throwable;
                    throwable.printStackTrace();
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
        if (uri.contains(".gif") || isGif) {
            gifView.setVisibility(VISIBLE);
            largeImgBinding.ivGo360.setVisibility(GONE);
            jpgView.setVisibility(GONE);
            if(panoView != null){
                panoView.setVisibility(GONE);
            }
            if (uri.startsWith("content://") || uri.startsWith("file://")) {
                gifView.setImageURI(Uri.parse(uri));
            } else {
                gifView.setImageURI(Uri.fromFile(new File(uri)));
            }
            stateManager.showContent();
        } else {
            gifView.setVisibility(GONE);
            if(panoView != null){
                panoView.setVisibility(GONE);
            }
            jpgView.setVisibility(VISIBLE);
            largeImgBinding.ivGo360.setVisibility(VISIBLE);

            //兼容avif格式
            if(uri.endsWith(".avif")){
                //todo 如何获取avif图片的宽高
                stateManager.showLoading();
                Glide.with(getContext())
                        .asBitmap()
                        .load(new File(uri))
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
                                Log.w("avif","large image load failed:"+uri);
                                info.throwable = new Throwable("load avif faild");
                                stateManager.showError(errorDrawable+" - load avif failed:\n"+uri+"\n"+new File(uri).exists());

                               /* jpgView.setImageDrawable(new BitmapDrawable(BitmapFactory.decodeFile(uri.replace(".avif",".jpg"))));
                                stateManager.showContent();*/
                            }
                        });
                return;
            }

            //判断是否为360全景图
            if(MyPanoView.isPanoramaImage(uri) || viewAsPano){
                if(panoView == null){
                    panoView = new MyPanoView(getContext());
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT);
                    panoView.setLayoutParams(params);
                    stateBinding.itemLargeImg.getRoot().addView(panoView,2);
                }
                largeImgBinding.ivGo360.setVisibility(GONE);
                jpgView.setVisibility(GONE);
                panoView.setVisibility(VISIBLE);
                Disposable subscribe = Observable.just(uri)
                        .subscribeOn(Schedulers.io())
                        .map(new Function<String, Bitmap>() {
                            @Override
                            public Bitmap apply(String uri) throws Exception {
                                if (uri.startsWith("content://") || uri.startsWith("file://")) {
                                    return BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(Uri.parse(uri)));
                                }
                                return BitmapFactory.decodeStream(new FileInputStream(new File(uri)));
                            }
                        }).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Bitmap>() {
                            @Override
                            public void accept(Bitmap bitmap) throws Exception {
                                panoView.loadBitmap(bitmap);
                                stateManager.showContent();
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                info.throwable = throwable;
                                stateManager.showError(throwable.getMessage());
                            }
                        });
                return;
            }
            jpgView.setVisibility(VISIBLE);
            if(panoView != null){
                panoView.setVisibility(GONE);
            }

            Disposable subscribe = Observable.just(uri)
                    .subscribeOn(Schedulers.io())
                    .map(new Function<String, InputStreamBitmapDecoderFactory>() {
                        @Override
                        public InputStreamBitmapDecoderFactory apply(String uri) throws Exception {
                            if (uri.startsWith("content://") || uri.startsWith("file://")) {
                                return new InputStreamBitmapDecoderFactory(
                                        getContext().getContentResolver().openInputStream(Uri.parse(uri)));
                            } else if (uri.startsWith("/storage/")) {
                                return new InputStreamBitmapDecoderFactory(new FileInputStream(new File(uri)));
                            }
                            return new InputStreamBitmapDecoderFactory(new FileInputStream(new File(uri)));
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


}
