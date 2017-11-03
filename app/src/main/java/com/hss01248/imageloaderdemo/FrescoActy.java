package com.hss01248.imageloaderdemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.facebook.drawee.view.SimpleDraweeView;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.interfaces.MyItemDialogListener;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.config.ScaleMode;
import com.hss01248.image.config.SingleConfig;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.MyToast;

/**
 * @author huangshuisheng
 * @date 2017/11/1
 */

public class FrescoActy extends Activity {


    @Bind(R.id.iv_targetView)
    SimpleDraweeView ivTargetView;
    @Bind(R.id.iv_targetView2)
    ImageView imageView;
    @Bind(R.id.rb_fresco)
    RadioButton rbFresco;
    @Bind(R.id.rb_glide)
    RadioButton rbGlide;
    @Bind(R.id.rb_picasso)
    RadioButton rbPicasso;
    @Bind(R.id.rg_loader)
    RadioGroup rgLoader;
    @Bind(R.id.rb_fromfile)
    RadioButton rbFromfile;
    @Bind(R.id.rb_from_url)
    RadioButton rbFromUrl;
    @Bind(R.id.rb_from_res)
    RadioButton rbFromRes;
    @Bind(R.id.rg_from)
    RadioGroup rgFrom;
    @Bind(R.id.sb_width)
    SeekBar sbWidth;
    @Bind(R.id.sb_height)
    SeekBar sbHeight;
    @Bind(R.id.et_placeholder_res)
    Button etPlaceholderRes;
    @Bind(R.id.et_placeholder_scale)
    Button etPlaceholderScale;
    @Bind(R.id.et_loading_res)
    Button etLoadingRes;
    @Bind(R.id.et_loading_scale)
    Button etLoadingScale;
    @Bind(R.id.et_error_res)
    Button etErrorRes;
    @Bind(R.id.et_error_scale)
    Button etErrorScale;
    @Bind(R.id.et_as_cirle)
    RadioButton etAsCirle;
    @Bind(R.id.et_roundcorner)
    RadioButton etRoundcorner;
    @Bind(R.id.rg_shape)
    RadioGroup rgShape;
    @Bind(R.id.sb_roundcorner_radis)
    SeekBar sbRoundcornerRadis;
    @Bind(R.id.sb_border_width)
    SeekBar sbBorderWidth;
    @Bind(R.id.et_border_color)
    Button etBorderColor;
    @Bind(R.id.sb_blur_rate)
    SeekBar sbBlurRate;
    @Bind(R.id.et_as_imageview)
    RadioButton etAsImageview;
    @Bind(R.id.et_as_bitmap)
    RadioButton etAsBitmap;
    @Bind(R.id.rg_target)
    RadioGroup rgTarget;
    @Bind(R.id.btn_show)
    Button btnShow;

    ArrayList<String> urls ;
    List<String> ress ;
    ArrayList<String> files;
    String selectedPath;
    int selectedRes;

    int[] placeHolderRes;
    int[] loadingRes;
    int[] errorRes;

    SingleConfig.ConfigBuilder builder;
    SingleConfig config;

    List<String> placeHolderDes;
    List<String> scaleModeStrs;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.acty_fresco);
        ButterKnife.bind(this);

        initData();
        initListener();

        ImageLoader.with(this)
            .url("http://pic137.nipic.com/file/20170801/21016265_111024595000_2.jpg")
            .scale(ScaleMode.CENTER_INSIDE)
            .placeHolder(R.drawable.default_placeholder_300x300, true, ScaleMode.FIT_XY)
            .loading(R.drawable.loading)
            .error(R.drawable.error_small, ScaleMode.CENTER_INSIDE)
            .widthHeight(180, 150);
        //.into(ivFile);

    }

    private void initListener() {
        sbWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                builder.widthHeight(progress*1080/100,sbHeight.getProgress()*1920/100);
                loadImage();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sbHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                builder.widthHeight(sbWidth.getProgress()*1080/100,progress*1920/100);
                loadImage();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sbRoundcornerRadis.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                int shapeId =   rgShape.getCheckedRadioButtonId();
               if(shapeId == R.id.et_roundcorner){
                   loadImage();
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sbBlurRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                loadImage();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initData() {
        urls = new ArrayList<>();
        urls.add("http://pic137.nipic.com/file/20170801/21016265_111024595000_2.jpg");
        urls.add("http://img02.tooopen.com/images/20141231/sy_78327074576.jpg");
        urls.add("http://img06.tooopen.cn/images/20170106/tooopen_sy_195886579867.jpg");
        urls.add("http://img06.tooopen.cn/images/20170814/tooopen_sy_220490839847.jpg");
        urls.add("http://img06.tooopen.cn/images/20170818/tooopen_sy_221040993375.jpg");
        urls.add("http://img07.tooopen.cn/images/20170320/tooopen_sy_202527818519.jpg");

        sbWidth.setMax(100);
        sbHeight.setMax(100);
        sbBlurRate.setMax(100);
        sbBorderWidth.setMax(100);
        sbRoundcornerRadis.setMax(100);

         placeHolderRes = new int[]{R.drawable.placeholder1_36dp, R.drawable.placeholder2_48dp,
             R.drawable.placeholder3_24dp, R.drawable.placeholder4_18dp};
        errorRes = new int[]{R.drawable.error1_48dp, R.drawable.error2_24dp,
            R.drawable.error3_18dp, R.drawable.error4_36dp};
        loadingRes = new int[]{R.drawable.loading1_48dp, R.drawable.loading2_18dp,
            R.drawable.loading3_18dp, R.drawable.loading4_48dp};
        ress = Arrays.asList(R.drawable.image+"",R.drawable.thegif+"",R.drawable.img2+"");
        builder = ImageLoader.with(this);
        placeHolderDes = Arrays.asList("1","2","3","4");
        scaleModeStrs = Arrays.asList("CENTER_CROP","FIT_XY","CENTER","FOCUS_CROP","FIT_CENTER","FIT_START","FIT_END","CENTER_INSIDE","FACE_CROP");



    }

    @OnClick({R.id.iv_targetView, R.id.rb_fresco, R.id.rb_glide, R.id.rb_picasso, R.id.rg_loader, R.id.rb_fromfile, R.id.rb_from_url, R.id.rb_from_res, R.id.rg_from, R.id.sb_width, R.id.sb_height, R.id.et_placeholder_res, R.id.et_placeholder_scale, R.id.et_loading_res, R.id.et_loading_scale, R.id.et_error_res, R.id.et_error_scale, R.id.et_as_cirle, R.id.et_roundcorner, R.id.rg_shape, R.id.sb_roundcorner_radis, R.id.sb_border_width, R.id.et_border_color, R.id.sb_blur_rate, R.id.et_as_imageview, R.id.et_as_bitmap, R.id.rg_target, R.id.btn_show})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_targetView:
                break;
            case R.id.rb_fresco:
                break;
            case R.id.rb_glide:
                break;
            case R.id.rb_picasso:
                break;
            case R.id.rg_loader:
                break;
            case R.id.rb_fromfile:
                break;
            case R.id.rb_from_url:
                StyledDialog.buildIosSingleChoose(urls, new MyItemDialogListener() {
                    @Override
                    public void onItemClick(CharSequence charSequence, int i) {
                        builder.url(charSequence.toString());
                        loadImage();
                    }
                }).show();
                break;
            case R.id.rb_from_res:
                StyledDialog.buildIosSingleChoose(ress, new MyItemDialogListener() {
                    @Override
                    public void onItemClick(CharSequence charSequence, int i) {
                        builder.res( Integer.parseInt(charSequence.toString()));
                        loadImage();
                    }
                }).show();
                break;
            case R.id.et_placeholder_res:
                StyledDialog.buildIosSingleChoose(placeHolderDes, new MyItemDialogListener() {
                    @Override
                    public void onItemClick(CharSequence charSequence, int i) {
                        builder.placeHolder(placeHolderRes[i],true,config.getPlaceHolderScaleType());
                        etPlaceholderRes.setText(charSequence);
                        loadImage();
                    }
                }).show();
                break;
            case R.id.et_placeholder_scale:
                StyledDialog.buildIosSingleChoose(scaleModeStrs, new MyItemDialogListener() {
                    @Override
                    public void onItemClick(CharSequence charSequence, int i) {
                        etPlaceholderScale.setText(charSequence);
                        builder.placeHolder(config.getPlaceHolderResId(),true,i+1);
                    }
                }).show();
                break;
            case R.id.et_loading_res:
                StyledDialog.buildIosSingleChoose(placeHolderDes, new MyItemDialogListener() {
                    @Override
                    public void onItemClick(CharSequence charSequence, int i) {
                        builder.placeHolder(loadingRes[i],true,config.getLoadingScaleType());
                        etLoadingRes.setText(charSequence);
                        loadImage();
                    }
                }).show();
                break;
            case R.id.et_loading_scale:
                StyledDialog.buildIosSingleChoose(scaleModeStrs, new MyItemDialogListener() {
                    @Override
                    public void onItemClick(CharSequence charSequence, int i) {
                        etLoadingScale.setText(charSequence);
                        builder.placeHolder(config.getLoadingResId(),true,i+1);
                    }
                }).show();
                break;
            case R.id.et_error_res:
                StyledDialog.buildIosSingleChoose(placeHolderDes, new MyItemDialogListener() {
                    @Override
                    public void onItemClick(CharSequence charSequence, int i) {
                        builder.placeHolder(errorRes[i],true,config.getErrorScaleType());
                        etErrorRes.setText(charSequence);
                        loadImage();
                    }
                }).show();
                break;
            case R.id.et_error_scale:
                StyledDialog.buildIosSingleChoose(scaleModeStrs, new MyItemDialogListener() {
                    @Override
                    public void onItemClick(CharSequence charSequence, int i) {
                        etErrorScale.setText(charSequence);
                        builder.placeHolder(config.getErrorResId(),true,i+1);
                    }
                }).show();
                break;
            case R.id.et_as_cirle:
                loadImage();
                break;
            case R.id.et_roundcorner:
                loadImage();
                break;
            case R.id.sb_roundcorner_radis:
                break;
            case R.id.sb_border_width:
                break;
            case R.id.et_border_color:
                break;
            case R.id.sb_blur_rate:
                break;
            case R.id.et_as_imageview:
                break;
            case R.id.et_as_bitmap:
                break;
            case R.id.rg_target:
                break;
            case R.id.btn_show:
                loadImage();
                break;
            default:break;
        }
    }

    private void loadImage() {

        //loader:
        int loaderId =   rgLoader.getCheckedRadioButtonId();
        boolean isFresco = true;
        /*if(loaderId == R.id.rb_fresco){
            ImageLoader.init(getApplicationContext(),60,new FrescoLoader());
            isFresco = true;
        }else if(loaderId == R.id.rb_glide){
            ImageLoader.init(getApplicationContext(),60,new GlideLoader());
        }else if(loaderId == R.id.rb_picasso){
            ImageLoader.init(getApplicationContext(),60,new PicassoLoader());
        }else {
            MyToast.error("loader not choosed");
            return;
        }*/
        //图片源
        //SingleConfig.ConfigBuilder builder = ImageLoader.with(this);
        /*if(!TextUtils.isEmpty(selectedPath)){
            if(selectedPath.startsWith("http")){
                builder.url(selectedPath);
            }else if(selectedPath.startsWith("content")){
                builder.content(selectedPath);
            }else {
                builder.file(selectedPath);
            }

        }else if(selectedRes >0){
            builder.res(selectedRes);
        }else {
            MyToast.error("图片源为空");
            return;
        }*/

        //宽高
        /*int width = sbWidth.getProgress()*1080/100/2;
        int height = sbHeight.getProgress()*1920/100/2;
        Logger.e("width:"+width+ "  height:"+height);
        if(width>0 && height>0){
            builder.widthHeight(width,height);
        }*/

        //todo 获取res的值




        int shapeId =   rgShape.getCheckedRadioButtonId();
        if(shapeId == R.id.et_as_cirle){
            builder.asCircle(R.color.colorAccent);
        }else if(shapeId == R.id.et_roundcorner){
            builder.rectRoundCorner(sbRoundcornerRadis.getProgress(),R.color.colorAccent);
        }
        //高斯模糊
        int blurRate = sbBlurRate.getProgress();
        if(blurRate>0){
            builder.blur(blurRate);
        }
        //加载到view或者bitmap
        int targetId =   rgTarget.getCheckedRadioButtonId();
        if(targetId == R.id.et_as_imageview){
            if(isFresco){
                builder.into(ivTargetView);
            }else {
                builder.into(imageView);
            }

        }else if(targetId == R.id.et_as_bitmap){
            builder.asBitmap(new SingleConfig.BitmapListener() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    Logger.e("bitmap :%s,width:%d,height:%d",bitmap.toString(),bitmap.getWidth(),bitmap.getHeight());
                }

                @Override
                public void onFail() {
                    Logger.e("on fail");

                }
            });
        }else {
            MyToast.error("no target selected!");
        }


        config = new SingleConfig(builder);
    }
}
