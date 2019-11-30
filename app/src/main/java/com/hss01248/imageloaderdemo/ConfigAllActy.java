package com.hss01248.imageloaderdemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.elvishew.xlog.XLog;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.interfaces.MyItemDialogListener;
import com.hss01248.frescoloader.FrescoLoader;
import com.hss01248.glideloader.GlideLoader;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.config.GlobalConfig;
import com.hss01248.image.config.SingleConfig;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.MyToast;

/**
 * @author huangshuisheng
 * @date 2017/11/1
 */

public class ConfigAllActy extends Activity {


    @BindView(R.id.iv_targetView)
    ImageView ivTargetView;
    @BindView(R.id.iv_targetView2)
    ImageView imageView;
    @BindView(R.id.rb_fresco)
    RadioButton rbFresco;
    @BindView(R.id.rb_glide)
    RadioButton rbGlide;
    @BindView(R.id.rb_picasso)
    RadioButton rbPicasso;
    @BindView(R.id.rg_loader)
    RadioGroup rgLoader;
    @BindView(R.id.rb_fromfile)
    RadioButton rbFromfile;
    @BindView(R.id.rb_from_url)
    RadioButton rbFromUrl;
    @BindView(R.id.rb_from_res)
    RadioButton rbFromRes;
    @BindView(R.id.rg_from)
    RadioGroup rgFrom;
    @BindView(R.id.sb_width)
    SeekBar sbWidth;
    @BindView(R.id.sb_height)
    SeekBar sbHeight;
    @BindView(R.id.et_placeholder_res)
    Button etPlaceholderRes;
    @BindView(R.id.et_placeholder_scale)
    Button etPlaceholderScale;
    @BindView(R.id.et_loading_res)
    Button etLoadingRes;
    @BindView(R.id.et_loading_scale)
    Button etLoadingScale;
    @BindView(R.id.et_error_res)
    Button etErrorRes;
    @BindView(R.id.et_error_scale)
    Button etErrorScale;
    @BindView(R.id.et_as_cirle)
    RadioButton etAsCirle;
    @BindView(R.id.et_roundcorner)
    RadioButton etRoundcorner;
    @BindView(R.id.rg_shape)
    RadioGroup rgShape;
    @BindView(R.id.sb_roundcorner_radis)
    SeekBar sbRoundcornerRadis;
    @BindView(R.id.sb_border_width)
    SeekBar sbBorderWidth;
    @BindView(R.id.et_border_color)
    Button etBorderColor;
    @BindView(R.id.sb_blur_rate)
    SeekBar sbBlurRate;
    @BindView(R.id.et_as_imageview)
    RadioButton etAsImageview;
    @BindView(R.id.et_as_bitmap)
    RadioButton etAsBitmap;
    @BindView(R.id.rg_target)
    RadioGroup rgTarget;
    @BindView(R.id.btn_show)
    FloatingActionButton btnShow;
    @BindView(R.id.btn_scale)
    Button btn_scale;

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

        /*ImageLoader.with(this)
            .url("http://pic137.nipic.com/file/20170801/21016265_111024595000_2.jpg")
            .scale(ScaleMode.CENTER_INSIDE)
            .placeHolder(R.drawable.default_placeholder_300x300, true, ScaleMode.FIT_XY)
            .loading(R.drawable.loading2)
            .error(R.drawable.error_small, ScaleMode.CENTER_INSIDE)
            .widthHeight(180, 150);*/
        //.into(ivFile);

    }

    private void initListener() {
        sbWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                builder.widthHeightByPx(progress*1080/100,sbHeight.getProgress()*1920/100);
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
                builder.widthHeightByPx(sbWidth.getProgress()*1080/100,progress*1920/100);
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

        urls.add("http://img02.tooopen.com/images/20141231/sy_78327074576.jpg");
        urls.add("https://c-ssl.duitang.com/uploads/blog/201407/04/20140704234425_j5zHS.thumb.700_0.gif");
        urls.add("http://qntemp.bejson.com/upload/15942127644639826Xnip2019-07-31_18-21-49.jpg?imageView2/0/format/webp");
        urls.add("http://img.mp.itc.cn/upload/20170101/43c903083d904a58a8d9e06511c5641a_th.png");
        urls.add("https://s3.51cto.com/wyfs02/M02/06/ED/wKiom1nAst7gJXLWAApAOtlw0r4105.jpg");
        urls.add("http://image.juntu.com/uploadfile/2017/0818/20170818095821327.jpg");

        sbWidth.setMax(100);
        sbHeight.setMax(100);
        sbBlurRate.setMax(30);
        sbBorderWidth.setMax(30);
        sbRoundcornerRadis.setMax(50);

         placeHolderRes = new int[]{R.drawable.placeholder1_36dp, R.drawable.placeholder2_48dp,
             R.drawable.placeholder3_24dp, R.drawable.placeholder4_18dp};
        errorRes = new int[]{R.drawable.error1_48dp, R.drawable.error2_24dp,
            R.drawable.error3_18dp, R.drawable.error4_36dp};
        loadingRes = new int[]{R.drawable.loading1_48dp, R.drawable.loading2_18dp,
            R.drawable.imageloader_loading_81, R.drawable.imageloader_loading_50};
        ress = Arrays.asList(R.drawable.image+"",R.drawable.thegif+"",R.drawable.img2+"");
        builder = ImageLoader.with(this);
        config = new SingleConfig(builder);
        placeHolderDes = Arrays.asList("0","1","2","3");
        scaleModeStrs = Arrays.asList("CENTER_CROP","FIT_XY","CENTER","FOCUS_CROP","FIT_CENTER","FIT_START","FIT_END","CENTER_INSIDE","FACE_CROP");


        sbWidth.setProgress(30);
        sbHeight.setProgress(30);
        rbFresco.setChecked(true);
        etAsImageview.setChecked(true);


    }

    int selectedPlaceHolderScale;
    int selectedLoadingScale;
    int selectedErrorScale;


    @OnClick({R.id.iv_targetView, R.id.rb_fresco, R.id.rb_glide, R.id.rb_picasso, R.id.rg_loader,
        R.id.rb_fromfile, R.id.rb_from_url, R.id.rb_from_res, R.id.rg_from, R.id.sb_width, R.id.sb_height,
        R.id.et_placeholder_res, R.id.et_placeholder_scale, R.id.et_loading_res, R.id.et_loading_scale,
        R.id.et_error_res, R.id.et_error_scale, R.id.et_as_cirle, R.id.et_roundcorner, R.id.rg_shape,
        R.id.sb_roundcorner_radis, R.id.sb_border_width, R.id.et_border_color, R.id.sb_blur_rate,
        R.id.et_as_imageview, R.id.et_as_bitmap, R.id.rg_target, R.id.btn_show,R.id.btn_scale})
    public void onViewClicked(View view) {
        try {
            switch (view.getId()) {
                case R.id.btn_scale:
                    StyledDialog.buildIosSingleChoose(scaleModeStrs, new MyItemDialogListener() {
                        @Override
                        public void onItemClick(CharSequence charSequence, int i) {
                            builder.scale(i+1);
                            btn_scale.setText(charSequence);
                            loadImage();


                        }
                    }).show();

                    break;
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
                            builder.res(0);
                            builder.file("");
                            builder.url(charSequence.toString());

                        }
                    }).show();
                    break;
                case R.id.rb_from_res:
                    StyledDialog.buildIosSingleChoose(ress, new MyItemDialogListener() {
                        @Override
                        public void onItemClick(CharSequence charSequence, int i) {
                            builder.url("");
                            builder.file("");
                            builder.res( Integer.parseInt(charSequence.toString()));

                        }
                    }).show();
                    break;
                case R.id.et_placeholder_res:
                    StyledDialog.buildIosSingleChoose(placeHolderDes, new MyItemDialogListener() {
                        @Override
                        public void onItemClick(CharSequence charSequence, int i) {
                            XLog.e("place holder:%s,positon:%d,resid:%d",charSequence,i,placeHolderRes[i]);
                            builder.placeHolder(placeHolderRes[i],true,selectedPlaceHolderScale);
                            etPlaceholderRes.setText(charSequence);


                        }
                    }).show();
                    break;
                case R.id.et_placeholder_scale:
                    StyledDialog.buildIosSingleChoose(scaleModeStrs, new MyItemDialogListener() {
                        @Override
                        public void onItemClick(CharSequence charSequence, int i) {
                            etPlaceholderScale.setText(charSequence);
                            selectedPlaceHolderScale = i+1;
                            String text = (String) etPlaceholderRes.getText();
                            if(!TextUtils.isEmpty(text) && TextUtils.isDigitsOnly(text)){
                                int idx = Integer.parseInt(text);
                                builder.placeHolder(placeHolderRes[idx],true,i+1);
                            }


                        }
                    }).show();
                    break;
                case R.id.et_loading_res:
                    StyledDialog.buildIosSingleChoose(placeHolderDes, new MyItemDialogListener() {
                        @Override
                        public void onItemClick(CharSequence charSequence, int i) {
                            builder.loading(loadingRes[i],selectedLoadingScale);
                            etLoadingRes.setText(charSequence);

                        }
                    }).show();
                    break;
                case R.id.et_loading_scale:
                    StyledDialog.buildIosSingleChoose(scaleModeStrs, new MyItemDialogListener() {
                        @Override
                        public void onItemClick(CharSequence charSequence, int i) {
                            etLoadingScale.setText(charSequence);
                            selectedLoadingScale = i+1;
                            String text = (String) etLoadingRes.getText();
                            if(!TextUtils.isEmpty(text) && TextUtils.isDigitsOnly(text)){
                                int idx = Integer.parseInt(text);
                                builder.loading(placeHolderRes[idx],i+1);
                            }

                        }
                    }).show();
                    break;
                case R.id.et_error_res:
                    StyledDialog.buildIosSingleChoose(placeHolderDes, new MyItemDialogListener() {
                        @Override
                        public void onItemClick(CharSequence charSequence, int i) {
                            builder.error(errorRes[i],selectedErrorScale);
                            etErrorRes.setText(charSequence);

                        }
                    }).show();
                    break;
                case R.id.et_error_scale:
                    StyledDialog.buildIosSingleChoose(scaleModeStrs, new MyItemDialogListener() {
                        @Override
                        public void onItemClick(CharSequence charSequence, int i) {
                            etErrorScale.setText(charSequence);
                            selectedErrorScale = i+1;
                            String text = (String) etErrorRes.getText();
                            if(!TextUtils.isEmpty(text) && TextUtils.isDigitsOnly(text)){
                                int idx = Integer.parseInt(text);
                                builder.error(errorRes[idx],i+1);
                            }
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
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    boolean hasFrescoInited = true;
    boolean hasGlideInited = false;
    boolean hasPicassoInited = false;
    private void loadImage() {

        try {
            //loader:
            int loaderId =   rgLoader.getCheckedRadioButtonId();
            boolean isFresco = false;
            if(loaderId == R.id.rb_fresco){
                isFresco = true;
                if(hasFrescoInited){
                    GlobalConfig.setLoader(new FrescoLoader());
                }else {
                    ImageLoader.init(getApplicationContext(),60,new FrescoLoader());
                    hasFrescoInited = true;
                }

            }else if(loaderId == R.id.rb_glide){
                if(hasGlideInited){
                    GlobalConfig.setLoader(new GlideLoader());
                }else {
                    ImageLoader.init(getApplicationContext(),60,new GlideLoader());
                    hasGlideInited = true;
                }
            }else if(loaderId == R.id.rb_picasso){
                if(hasPicassoInited){
                   // GlobalConfig.setLoader(new PicassoLoader());
                }else {
                    //ImageLoader.init(getApplicationContext(),60,new PicassoLoader());
                    hasPicassoInited = true;
                }
            }else {
                MyToast.error("loader not choosed");
                return;
            }

            ImageLoader.getActualLoader().clearDiskCache();
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
        int width = sbWidth.getProgress()*1080/100;
        int height = sbHeight.getProgress()*1920/100;
        XLog.e("width:"+width+ "  height:"+height);
        builder.widthHeightByPx(width,height);

            //todo 获取res的值




            int shapeId =   rgShape.getCheckedRadioButtonId();
            int border = sbBorderWidth.getProgress();
            if(shapeId == R.id.et_as_cirle){
                builder.asCircle(R.color.colorAccent);

            }else if(shapeId == R.id.et_roundcorner){

                builder.rectRoundCorner(sbRoundcornerRadis.getProgress(),R.color.colorAccent);
            }
            builder.border(border,R.color.colorAccent);
            //高斯模糊
            int blurRate = sbBlurRate.getProgress();
            if(blurRate>0){
                builder.blur(blurRate);
            }
            //加载到view或者bitmap
            int targetId =   rgTarget.getCheckedRadioButtonId();
            XLog.e(builder);
            if(targetId == R.id.et_as_imageview){
                if(isFresco){
                    imageView.setVisibility(View.GONE);
                    ivTargetView.setVisibility(View.VISIBLE);
                    builder.into(ivTargetView);
                }else {
                    imageView.setVisibility(View.VISIBLE);
                    ivTargetView.setVisibility(View.GONE);
                    builder.into(imageView);

                }

            }else if(targetId == R.id.et_as_bitmap){
                builder.asBitmap(new SingleConfig.BitmapListener() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        XLog.e("bitmap :%s,width:%d,height:%d",bitmap.toString(),bitmap.getWidth(),bitmap.getHeight());
                        imageView.setVisibility(View.VISIBLE);
                        ivTargetView.setVisibility(View.GONE);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        imageView.setImageBitmap(bitmap);

                    }

                    @Override
                    public void onFail(Throwable e) {
                        e.printStackTrace();
                        XLog.e("on fail");
                        //MyToast.error("on bitmap fail");
                        imageView.setVisibility(View.VISIBLE);
                        ivTargetView.setVisibility(View.GONE);
                        imageView.setImageResource(R.drawable.error1_48dp);

                    }
                });
            }else {
                MyToast.error("no target selected!");
            }


            config = new SingleConfig(builder);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
