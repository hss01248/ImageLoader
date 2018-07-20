package com.hss01248.image.config;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.hss01248.image.MyUtil;
import com.hss01248.image.R;
import com.hss01248.image.interfaces.ImageListener;

import java.io.File;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

public class SingleConfig {

    public Context getContext() {
        if(context==null){
            context = GlobalConfig.context;
        }
        return context;
    }

    private Context context;

    public int getBorderColor() {
        return borderColor;
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public String getContentProvider() {
        return contentProvider;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getHeight() {
        if(height<=0){
            //先去imageview里取,如果为0,则赋值成matchparent
            if(target!=null){
                height=  target.getMeasuredHeight();
            }
            /*if(height<=0){
                height=GlobalConfig.getWinWidth();
            }*/
        }

        return height;
    }
    private boolean isUseARGB8888;

    public boolean isUseARGB8888(){
        return isUseARGB8888;
    }

    public boolean isNeedBlur() {
        return needBlur;
    }

    public int getPlaceHolderResId() {
        return placeHolderResId;
    }

    public int getRectRoundRadius() {
        return rectRoundRadius;
    }

    public int getResId() {
        return resId;
    }

    public int getScaleMode() {
        return scaleMode;
    }

    public int getShapeMode() {
        return shapeMode;
    }

    public View getTarget() {
        return target;
    }

    public String getUrl() {
        return url;
    }

    public int getWidth() {
        if(width<=0){
            //先去imageview里取,如果为0,则赋值成matchparent
            if(target!=null){
              width=  target.getMeasuredWidth();
            }
           /* if(width<=0){
                width=GlobalConfig.getWinWidth();
            }*/
        }
        return width;
    }

    public int getRoundOverlayColor() {
        return roundOverlayColor;
    }

    public boolean isIgnoreCertificateVerify() {
        return ignoreCertificateVerify;
    }

    public BitmapListener getBitmapListener() {

        return bitmapListener;
    }

    private  boolean ignoreCertificateVerify ;
    private String url;

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    private String thumbnailUrl;//小图的url
    private String filePath;
    private int resId;
    private String contentProvider;
    private boolean isGif;

    private View target;

    private int width;
    private int height;

    private boolean needBlur ;//是否需要模糊
    private int blurRadius;



    //UI:
    private int placeHolderResId;
    private boolean reuseable;

    public int getPlaceHolderScaleType() {
        return placeHolderScaleType;
    }

    public int getErrorScaleType() {
        return errorScaleType;
    }

    private int placeHolderScaleType;
    private int errorScaleType;
    private int loadingScaleType;

    public int getLoadingResId() {
        return loadingResId;
    }

    public int getErrorResId() {
        return errorResId;
    }

    private int loadingResId;
    private int errorResId;

    public boolean isReuseable() {
        return reuseable;
    }
    private int shapeMode;//默认矩形,可选直角矩形,圆形/椭圆
    private int rectRoundRadius;//圆角矩形时圆角的半径

    private int roundOverlayColor;//圆角/圆外覆盖一层背景色
    private int scaleMode;//填充模式,默认centercrop,可选fitXY,centerInside...

    private int borderWidth;//边框的宽度
    private int borderColor;//边框颜色

    public void setAsBitmap(boolean asBitmap) {
        this.asBitmap = asBitmap;
    }

    public boolean isAsBitmap() {
        return asBitmap;
    }

    private boolean asBitmap;//只获取bitmap

    public void setBitmapListener(BitmapListener bitmapListener) {
        this.bitmapListener = MyUtil.getBitmapListenerProxy(bitmapListener);
    }

    private BitmapListener bitmapListener;

    public boolean isCropFace() {
        return cropFace;
    }

    private boolean cropFace;

    public boolean widthWrapContent;
    public boolean heightWrapContent;

    public boolean widthMatchParent;
    public boolean heightMatchParent;

    private ImageListener imageListener;

    public ImageListener getImageListener() {
        return imageListener;
    }

    /*public BigImageView getBigImageView() {
        return bigImageView;
    }*/

   // private BigImageView bigImageView ;//可放大和缩放的大图

    private void show(){
        if(ConfigChecker.check(this)){
            GlobalConfig.getLoader().request(this);
        }

    }


    public boolean isGif() {
        return isGif;
    }

    public int getBlurRadius() {
        return blurRadius;
    }

    public int getLoadingScaleType() {
        return loadingScaleType;
    }

    public SingleConfig(ConfigBuilder builder){
        this.url = builder.url;
        this.thumbnailUrl = builder.thumbnailUrl;
        this.filePath = builder.filePath;
        this.resId = builder.resId;
        this.contentProvider = builder.contentProvider;

        this.ignoreCertificateVerify = builder.ignoreCertificateVerify;

        this.target = builder.target;

        //结合view本身的宽高
        setWH(builder);


        this.shapeMode = builder.shapeMode;
        if(shapeMode== ShapeMode.RECT_ROUND){
            this.rectRoundRadius = builder.rectRoundRadius;
        }
        this.scaleMode = builder.scaleMode;

        this.needBlur = builder.needBlur;
        this.placeHolderResId = builder.placeHolderResId;
        this.borderWidth = builder.borderWidth;
        if(borderWidth>0){
            this.borderColor = builder.borderColor;
        }


        this.asBitmap = builder.asBitmap;
        this.bitmapListener = builder.bitmapListener;

        this.roundOverlayColor = builder.roundOverlayColor;
        this.isGif = builder.isGif;
        this.blurRadius = builder.blurRadius;
        this.reuseable = builder.reuseable;
        this.loadingResId = builder.loadingResId;
        this.errorResId = builder.errorResId;

        this.cropFace = builder.cropFace;
       // this.bigImageView = builder.bigImageView;

        this.errorScaleType = builder.errorScaleType;
        this.placeHolderScaleType = builder.placeHolderScaleType;
        this.loadingScaleType = builder.loadingScaleType;
        this.isUseARGB8888 = builder.isUseARGB8888;



    }

    private void setWH(ConfigBuilder builder) {
        if(!builder.asBitmap){

            this.width = builder.width;
            this.height = builder.height;

            View view = builder.target;
            if(view!=null){
                ViewGroup.LayoutParams params = view.getLayoutParams();
                if(params!=null){
                    //注意:返回的值都是px单位
                    int h = params.height;
                    int w = params.width;

                    //todo
                    // 1. 常用情况: view设置宽度match parent,高度wrap content,
                    // 同时要求图片等比例缩小或放大,使宽度刚好是matchaprent,高度等比例,不能变形
                    //2. 常见情况: 宽度match parent,高度一定



                    if(w >0){
                        if(builder.width<=0 || builder.width > w){
                            this.width = w;
                        }
                    }/*else {
                        if(builder.width == ViewGroup.LayoutParams.MATCH_PARENT){
                            this.width = GlobalConfig.getWinWidth();
                        }
                    }*/
                    if(h >0){
                        if(builder.height<=0 || builder.height>h){
                            this.height = h;
                        }
                    }/*else {
                        if(builder.height<=0){
                            if(builder.height == ViewGroup.LayoutParams.MATCH_PARENT){
                                this.height = GlobalConfig.getWinHeight();
                            }
                        }
                    }*/
                }
            }
        }else {
            this.width = builder.width;
            this.height = builder.height;
        }
    }


    public interface BitmapListener{
        void onSuccess(Bitmap bitmap);
        void onFail(Throwable e);
    }

    public static class ConfigBuilder{
        private Context context;

        private  boolean ignoreCertificateVerify = GlobalConfig.ignoreCertificateVerify;

        //图片源
        /**
         * 类型	SCHEME	示例
         远程图片	http://, https://	HttpURLConnection 或者参考 使用其他网络加载方案
         本地文件	file://	FileInputStream
         Content provider	content://	ContentResolver
         asset目录下的资源	asset://	AssetManager
         res目录下的资源	res://	Resources.openRawResource
         Uri中指定图片数据	data:mime/type;base64,	数据类型必须符合 rfc2397规定 (仅支持 UTF-8)
         * @param config
         * @return
         */
        private String url;
        private String thumbnailUrl;//小图的url
        private String filePath;
        private int resId;
        private String contentProvider;
        private boolean isGif=false;

        private View target;
        private boolean asBitmap;//只获取bitmap
        private BitmapListener bitmapListener;
        private ImageListener imageListener;

        private int width;
        private int height;

        private boolean needBlur = false;//是否需要模糊
        private int blurRadius;

        public ConfigBuilder setLoadingScaleType(int loadingScaleType) {
            this.loadingScaleType = loadingScaleType;
            return this;
        }

        public ConfigBuilder setImageListener(ImageListener imageListener) {
            this.imageListener = imageListener;
            return this;
        }

        private int loadingScaleType = GlobalConfig.loadingScaleType;

        //UI:
        private int placeHolderResId= GlobalConfig.placeHolderResId;
        private boolean reuseable;//当前view是不是可重用的


        public ConfigBuilder setUseARGB8888(boolean useARGB8888) {
            isUseARGB8888 = useARGB8888;
            return this;
        }

        private boolean isUseARGB8888;


        private int placeHolderScaleType= GlobalConfig.placeHolderScaleType;
        private int errorScaleType= GlobalConfig.errorScaleType;

        private int loadingResId = GlobalConfig.loadingResId;
        private int errorResId = GlobalConfig.errorResId;



        private int shapeMode;//默认矩形,可选直角矩形,圆形/椭圆
        private int rectRoundRadius;//圆角矩形时圆角的半径

        public ConfigBuilder setRoundOverlayColor(int roundOverlayColor) {
            this.roundOverlayColor = roundOverlayColor;
            return this;
        }

        private int roundOverlayColor;//圆角/圆外覆盖一层背景色
        private int scaleMode;//填充模式,默认centercrop,可选fitXY,centerInside...

        private int borderWidth;//边框的宽度
        private int borderColor;//边框颜色
        private boolean cropFace;

        /*private BigImageView bigImageView ;//可放大和缩放的大图

        public ConfigBuilder intoBigImageView(BigImageView bigImageView ){
            this.bigImageView = bigImageView;
            return this;
        }*/




        public ConfigBuilder(Context context){
            this.context = context;

        }

        /*public ConfigBuilder(SingleConfig config){

        }*/

        public ConfigBuilder ignoreCertificateVerify(boolean ignoreCertificateVerify){
            this.ignoreCertificateVerify = ignoreCertificateVerify;
            return this;
        }

        /**
         * 设置网络路径
         * @param url
         * @return
         */
        public ConfigBuilder url(String url){
            this.url = url;
            if(url.contains("gif")){
                isGif = true;
            }
            return this;
        }
        public ConfigBuilder thumbnail(String thumbnailUrl){
            this.thumbnailUrl = thumbnailUrl;
            return this;
        }
        public ConfigBuilder loading(int  loadingResId){
            this.loadingResId = loadingResId;
            return this;
        }

        /**
         * 使用默认的loading样式
         * @return
         */
        public ConfigBuilder loadingDefault(){
            this.loadingResId = R.drawable.imageloader_loading_50;
            return this;
        }
        public ConfigBuilder loading(int  loadingResId,int loadingScaleType){
            this.loadingResId = loadingResId;
            this.loadingScaleType = loadingScaleType;
            return this;
        }
        public ConfigBuilder error(int  errorResId){
            this.errorResId = errorResId;
            return this;
        }
        public ConfigBuilder error(int  errorResId,int errorScaleType){
            this.errorResId = errorResId;
            this.errorScaleType = errorScaleType;
            return this;
        }
        public ConfigBuilder cropFace(){
            this.cropFace = true;
            return this;
        }

        public ConfigBuilder file(String filePath){
            if(filePath.startsWith("content:")){
                this.contentProvider = filePath;
                return this;
            }

            if(!new File(filePath).exists()){
                //throw new RuntimeException("文件不存在");
                Log.e("imageloader","文件不存在");
                return this;
            }

            this.filePath = filePath;
            if(filePath.contains("gif")){
                isGif = true;
            }
            return this;
        }

        public ConfigBuilder res(int resId){
            this.resId = resId;
            return this;
        }
        public ConfigBuilder content(String contentProvider){
            this.contentProvider = contentProvider;
            return this;
        }

        public void into(View targetView){
            this.target = targetView;
             new SingleConfig(this).show();
        }

        public ConfigBuilder listener(BitmapListener bitmapListener){
            this.bitmapListener = MyUtil.getBitmapListenerProxy(bitmapListener);
            return this;
        }

        public void asBitmap(BitmapListener bitmapListener){
            this.bitmapListener = MyUtil.getBitmapListenerProxy(bitmapListener);
            this.asBitmap = true;
            new SingleConfig(this).show();
        }

        /*public SingleConfig build(){
            return new SingleConfig(this);
        }*/

        /**
         * dp单位
         * @param widthInDp
         * @param heightInDp
         * @return
         */
        public ConfigBuilder widthHeight(int widthInDp,int heightInDp){
            this.width = MyUtil.dip2px(widthInDp);
            this.height = MyUtil.dip2px(heightInDp);
            return this;
        }
        public ConfigBuilder widthHeightByPx(int widthInPx,int heightInPx){
            this.width = widthInPx;
            this.height = heightInPx;
            return this;
        }

        public ConfigBuilder placeHolder(int placeHolderResId,boolean reuseable,int placeHolderScaleType){
            this.placeHolderResId = placeHolderResId;
            this.reuseable = reuseable;
            this.placeHolderScaleType = placeHolderScaleType;
            return this;
        }
        public ConfigBuilder placeHolder(int placeHolderResId,boolean reuseable){
            this.placeHolderResId = placeHolderResId;
            this.reuseable = reuseable;
            return this;
        }
        public ConfigBuilder placeHolder(int placeHolderResId){
            this.placeHolderResId = placeHolderResId;
            this.reuseable = true;
            return this;
        }


        /**
         * 是否需要高斯模糊
         * @return
         */
        public ConfigBuilder blur(int blurRadius){
            this.needBlur = true;
            this.blurRadius = blurRadius;
            return this;
        }



        public ConfigBuilder asCircle(int overlayColorWhenGif){
            this.shapeMode = ShapeMode.OVAL;
            this.roundOverlayColor  = overlayColorWhenGif;
            return this;
        }



        /**
         * 形状为圆角矩形时的圆角半径
         * @param rectRoundRadius
         * @return
         */
        public ConfigBuilder rectRoundCorner(int rectRoundRadius,int overlayColorWhenGif){
            this.rectRoundRadius = MyUtil.dip2px(rectRoundRadius);
            this.shapeMode = ShapeMode.RECT_ROUND;
            this.roundOverlayColor  = overlayColorWhenGif;

            return this;
        }

        /**
         * 拉伸/裁剪模式
         * @param scaleMode 取值ScaleMode
         * @return
         */
        public ConfigBuilder scale(int scaleMode){
            this.scaleMode = scaleMode;
            return this;
        }

        /** todo 尚未实现此功能
         * 设置边框
         * @param borderWidth
         * @param borderColor
         * @return
         */
        public ConfigBuilder border(int borderWidth,int borderColor){
            this.borderWidth = MyUtil.dip2px(borderWidth);
            this.borderColor = borderColor;
            return this;
        }


        @Override
        public String toString() {
            return "{" +
                "context:" + context +
                ", ignoreCertificateVerify:" + ignoreCertificateVerify +
                ", url:'" + url + '\'' +
                ", thumbnailUrl:'" + thumbnailUrl + '\'' +
                ", filePath:'" + filePath + '\'' +
                ", resId=" + resId +
                ", contentProvider:'" + contentProvider + '\'' +
                ", isGif:" + isGif +
                ", target:" + target +
                ", asBitmap:" + asBitmap +
                ", bitmapListener:" + bitmapListener +
                ", width:" + width +
                ", height:" + height +
                ", needBlur:" + needBlur +
                ", blurRadius:" + blurRadius +
                ", loadingScaleType:" + loadingScaleType +
                ", placeHolderResId:" + placeHolderResId +
                ", reuseable:" + reuseable +
                ", placeHolderScaleType:" + placeHolderScaleType +
                ", errorScaleType:" + errorScaleType +
                ", loadingResId:" + loadingResId +
                ", errorResId:" + errorResId +
                ", shapeMode:" + shapeMode +
                ", rectRoundRadius:" + rectRoundRadius +
                ", roundOverlayColor:" + roundOverlayColor +
                ", scaleMode:" + scaleMode +
                ", borderWidth:" + borderWidth +
                ", borderColor:" + borderColor +
                ", cropFace:" + cropFace +
                '}';
        }
    }


}
