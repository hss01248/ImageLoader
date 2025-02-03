package com.hss01248.image.config;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;

import com.blankj.utilcode.util.LogUtils;
import com.hss01248.image.MyUtil;
import com.hss01248.image.R;
import com.hss01248.image.interfaces.ImageListener;
import com.hss01248.image.interfaces.LoadInterceptor;
import com.hss01248.image.utils.ContextUtil;

import java.util.Arrays;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

public class SingleConfig {

    public Context getContext() {
        if (context == null) {
            context = GlobalConfig.context;
        }
        return context;
    }

    private Context context;

    public String getErrorDes() {
        return errorDes;
    }

    public void setErrorDes(String errorDes) {
        this.errorDes = errorDes;
    }

    private String errorDes;

    public long loadStartTime;

    public long cost;

    public String urlForCacheKey;

    public int getBorderColor() {
        return borderColor;
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int getHeight() {
        return height;
    }

    private boolean isUseARGB8888;

    public boolean isUseARGB8888() {
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

    public void setScaleMode(int scaleMode) {
        this.scaleMode = scaleMode;
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


    public int getWidth() {
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

    private boolean ignoreCertificateVerify;

    public void setUrl(String url) {
        this.sourceString = url;
    }


    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    private String thumbnailUrl;//小图的url

    private int resId;


    public LoadInterceptor getInterceptor() {
        return interceptor;
    }

    private LoadInterceptor interceptor;

    /**
     * byte[]类型的图片源
     */
    private byte[] bytes;
    private boolean isGif;

    private View target;

    private int width;
    private int height;

    private boolean needBlur;//是否需要模糊
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

    public int getLeftTopRadius() {
        return leftTopRadius;
    }

    public int getRightTopRadius() {
        return rightTopRadius;
    }

    public int getLeftBottomRadius() {
        return leftBottomRadius;
    }

    public int getRightBottomRadius() {
        return rightBottomRadius;
    }

    private int leftTopRadius, rightTopRadius, leftBottomRadius, rightBottomRadius;

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
        this.bitmapListener = MyUtil.getProxy(bitmapListener);
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


    @Deprecated
    public String getUrl() {
        return sourceString;
    }


    public String getSourceString() {
        //Log.d("source", sourceString + "  ->>>>");
        return sourceString;
    }

    private String sourceString;

    private ImageListener imageListener;

    public boolean isUseThirdPartyGifLoader() {
        return useThirdPartyGifLoader;
    }

    private boolean useThirdPartyGifLoader;

    public ImageListener getImageListener() {
        return imageListener;
    }

    /*public BigImageView getBigImageView() {
        return bigImageView;
    }*/

    // private BigImageView bigImageView ;//可放大和缩放的大图

    private void show() {
        try {
            if (ConfigChecker.check(this)) {
                if (GlobalConfig.debug) {
                    GlobalConfig.getLoader().debug(this);
                }
                if (interceptor != null && interceptor.intercept(this)) {
                    LogUtils.i("intercept by interceptor: "+interceptor.toString()+", "+ getSourceString());
                    return;
                }

                if (isAsBitmap()) {
                    GlobalConfig.getLoader().requestAsBitmap(this);
                } /*else if (target instanceof BigImageView) {
                    MyUtil.viewBigImage(this);
                }*/
                else {
                    GlobalConfig.getLoader().requestForNormalDiaplay(this);
                }
            }else {
                LogUtils.i("ConfigChecker.check(this) failed: "+ getSourceString());
            }
        } catch (Throwable e) {
            if (GlobalConfig.getExceptionHandler() != null) {
                try {
                    GlobalConfig.getExceptionHandler().onError(e);
                } catch (Throwable e2) {
                    e2.printStackTrace();
                }
            } else {
                e.printStackTrace();
            }
        }

    }

    @Override
    public String toString() {
        return "SingleConfig{" +
                "context=" + context +
                ", isUseARGB8888=" + isUseARGB8888 +
                ", ignoreCertificateVerify=" + ignoreCertificateVerify +
                ", source='" + sourceString + '\'' +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", resId=" + resId +
                ", interceptor=" + interceptor +
                ", bytes=" + Arrays.toString(bytes) +
                ", isGif=" + isGif +
                ", target=" + target +
                ", width=" + width +
                ", height=" + height +
                ", needBlur=" + needBlur +
                ", blurRadius=" + blurRadius +
                ", placeHolderResId=" + placeHolderResId +
                ", reuseable=" + reuseable +
                ", placeHolderScaleType=" + placeHolderScaleType +
                ", errorScaleType=" + errorScaleType +
                ", loadingScaleType=" + loadingScaleType +
                ", loadingResId=" + loadingResId +
                ", errorResId=" + errorResId +
                ", shapeMode=" + shapeMode +
                ", rectRoundRadius=" + rectRoundRadius +
                ", roundOverlayColor=" + roundOverlayColor +
                ", scaleMode=" + scaleMode +
                ", borderWidth=" + borderWidth +
                ", borderColor=" + borderColor +
                ", asBitmap=" + asBitmap +
                ", bitmapListener=" + bitmapListener +
                ", cropFace=" + cropFace +
                ", widthWrapContent=" + widthWrapContent +
                ", heightWrapContent=" + heightWrapContent +
                ", widthMatchParent=" + widthMatchParent +
                ", heightMatchParent=" + heightMatchParent +
                ", imageListener=" + imageListener +
                '}';
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

    public SingleConfig(ConfigBuilder builder) {

        this.thumbnailUrl = builder.thumbnailUrl;
        this.resId = builder.resId;
        this.bytes = builder.bytes;

        this.ignoreCertificateVerify = builder.ignoreCertificateVerify;

        this.target = builder.target;

        //结合view本身的宽高
        this.width = builder.width;
        this.height = builder.height;


        this.shapeMode = builder.shapeMode;
        if (shapeMode == ShapeMode.RECT_ROUND) {
            this.rectRoundRadius = builder.rectRoundRadius;
        }
        this.scaleMode = builder.scaleMode;

        this.needBlur = builder.needBlur;
        this.placeHolderResId = builder.placeHolderResId;
        this.borderWidth = builder.borderWidth;
        if (borderWidth > 0) {
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
        this.imageListener = builder.imageListener;
        this.interceptor = builder.interceptor;
        this.useThirdPartyGifLoader = builder.useThirdPartyGifLoader;

        this.leftTopRadius = builder.leftTopRadius;
        this.rightTopRadius = builder.rightTopRadius;
        this.leftBottomRadius = builder.leftBottomRadius;
        this.rightBottomRadius = builder.rightBottomRadius;
        this.context = builder.context;

        this.sourceString = builder.sourceString;
    }


    public interface BitmapListener {
        void onSuccess(Bitmap bitmap);

        void onFail(Throwable e);
    }

    public static class ConfigBuilder {
        private Context context;


        private boolean ignoreCertificateVerify = GlobalConfig.ignoreCertificateVerify;


        private boolean isGif = false;

        private View target;
        private boolean asBitmap;//只获取bitmap
        private BitmapListener bitmapListener;
        private ImageListener imageListener;

        private int width;
        private int height;


        private int leftTopRadius, rightTopRadius, leftBottomRadius, rightBottomRadius;

        /**
         * 是否使用全局的默认图+centerinside
         *
         * @param useDefaultPlaceHolder
         * @return
         */
        public ConfigBuilder defaultPlaceHolder(boolean useDefaultPlaceHolder) {
            if (useDefaultPlaceHolder) {
                placeHolderResId = GlobalConfig.placeHolderResId;
                placeHolderScaleType = GlobalConfig.placeHolderScaleType;
            }
            return this;
        }

        /**
         * 是否使用全局的失败图+centerinside
         *
         * @param useDefaultErrorRes
         * @return
         */
        public ConfigBuilder defaultErrorRes(boolean useDefaultErrorRes) {
            if (useDefaultErrorRes) {
                errorScaleType = GlobalConfig.errorScaleType;
                errorResId = GlobalConfig.errorResId;
            }
            return this;
        }

        /**
         * glide支持不好,基本上用不上.
         * fresco完美支持
         *
         * @param useDefaultLoadingRes
         * @return
         */
        public ConfigBuilder defaultLoadingRes(boolean useDefaultLoadingRes) {
            if (useDefaultLoadingRes) {
                loadingScaleType = GlobalConfig.loadingScaleType;
                loadingResId = GlobalConfig.loadingResId;
            }
            return this;
        }

        private boolean needBlur = false;//是否需要模糊
        private int blurRadius;

        public ConfigBuilder setLoadingScaleType(int loadingScaleType) {
            this.loadingScaleType = loadingScaleType;
            return this;
        }

        public ConfigBuilder setImageListener(ImageListener imageListener) {
            this.imageListener = MyUtil.getProxy(imageListener);
            return this;
        }


        private boolean reuseable;//当前view是不是可重用的


        public ConfigBuilder setUseARGB8888(boolean useARGB8888) {
            isUseARGB8888 = useARGB8888;
            return this;
        }

        private boolean isUseARGB8888;

        private int loadingScaleType;
        private int placeHolderResId;
        private int placeHolderScaleType;
        private int errorScaleType;

        private int loadingResId;
        private int errorResId;
        /**
         * 类型	SCHEME	示例
         * 远程图片	http://, https://	HttpURLConnection 或者参考 使用其他网络加载方案
         * 本地文件	file://	FileInputStream  或者/storage/
         * Content provider	content://	ContentResolver
         * asset目录下的资源	asset://	AssetManager
         * res目录下的资源	res://	Resources.openRawResource
         * Uri中指定图片数据	data:mime/type;base64,	数据类型必须符合 rfc2397规定 (仅支持 UTF-8)
         *
         * @param config
         * @return
         */
        private String sourceString;
        private String thumbnailUrl;//小图的url
        private int resId;
        /**
         * byte[]类型的图片源
         */
        private byte[] bytes;

        public ConfigBuilder setInterceptor(LoadInterceptor interceptor) {
            this.interceptor = interceptor;
            return this;
        }

        private LoadInterceptor interceptor = GlobalConfig.interceptor;


        private int shapeMode;//默认矩形,可选直角矩形,圆形/椭圆
        private int rectRoundRadius;//圆角矩形时圆角的半径

        public ConfigBuilder setRoundOverlayColor(int roundOverlayColor) {
            this.roundOverlayColor = roundOverlayColor;
            return this;
        }

        private int roundOverlayColor;//圆角/圆外覆盖一层背景色
        private int scaleMode = GlobalConfig.errorScaleType;//填充模式,默认centercrop,可选fitXY,centerInside...

        private int borderWidth;//边框的宽度
        private int borderColor;//边框颜色
        private boolean cropFace;

        private boolean useThirdPartyGifLoader = GlobalConfig.useThirdPartyGifLoader;

        public ConfigBuilder useThirdPartyGifLoader(boolean useThirdPartyGifLoader) {
            this.useThirdPartyGifLoader = useThirdPartyGifLoader;
            return this;
        }

        /*private BigImageView bigImageView ;//可放大和缩放的大图

        public ConfigBuilder intoBigImageView(BigImageView bigImageView ){
            this.bigImageView = bigImageView;
            return this;
        }*/


        public ConfigBuilder(Context context) {
            this.context = context;
            Activity activity = ContextUtil.getActivityFromContext(context);
            if (activity != null) {
                this.context = activity;
            }
        }

        /*public ConfigBuilder(SingleConfig config){

        }*/

        public ConfigBuilder ignoreCertificateVerify(boolean ignoreCertificateVerify) {
            this.ignoreCertificateVerify = ignoreCertificateVerify;
            return this;
        }


        public ConfigBuilder thumbnail(String thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;
            return this;
        }

        public ConfigBuilder loading(int loadingResId) {
            this.loadingResId = loadingResId;
            return this;
        }

        /**
         * 使用默认的loading样式
         *
         * @return
         */
        public ConfigBuilder loadingDefault() {
            this.loadingResId = R.drawable.imageloader_loading_50;
            return this;
        }

        public ConfigBuilder loading(int loadingResId, int loadingScaleType) {
            this.loadingResId = loadingResId;
            this.loadingScaleType = loadingScaleType;
            return this;
        }

        public ConfigBuilder error(int errorResId) {
            this.errorResId = errorResId;
            return this;
        }

        public ConfigBuilder error(int errorResId, int errorScaleType) {
            this.errorResId = errorResId;
            this.errorScaleType = errorScaleType;
            return this;
        }

        public ConfigBuilder cropFace() {
            this.cropFace = true;
            return this;
        }

        public ConfigBuilder load(String sourceString) {
            this.sourceString = sourceString;
            if (!TextUtils.isEmpty(sourceString)) {
                if (sourceString.endsWith(".gif")) {
                    isGif = true;
                }
            }
            return this;
        }

        /**
         * 设置网络路径
         *
         * @param url
         * @return
         */
        @Deprecated
        public ConfigBuilder url(String url) {
            load(url);
            return this;
        }

        @Deprecated
        public ConfigBuilder file(String filePath) {
            load(filePath);
            return this;
        }

        @Deprecated
        public ConfigBuilder content(String contentProvider) {
            load(contentProvider);
            return this;
        }

        public ConfigBuilder res(int resId) {
            this.resId = resId;
            //也可以转成content://xxx
            return this;
        }

        public ConfigBuilder bytes(byte[] bytes) {
            this.bytes = bytes;
            return this;
        }


        public void into(View targetView) {
            this.target = targetView;
            new SingleConfig(this).show();
        }


        public void asBitmap(BitmapListener bitmapListener) {
            this.bitmapListener = MyUtil.getProxy(bitmapListener);
            this.asBitmap = true;
            new SingleConfig(this).show();
        }

        /*public SingleConfig build(){
            return new SingleConfig(this);
        }*/

        /**
         * dp单位
         *
         * @param widthInDp
         * @param heightInDp
         * @return
         */
        public ConfigBuilder widthHeight(int widthInDp, int heightInDp) {
            this.width = MyUtil.dip2px(widthInDp);
            this.height = MyUtil.dip2px(heightInDp);
            return this;
        }

        public ConfigBuilder widthHeightByPx(int widthInPx, int heightInPx) {
            this.width = widthInPx;
            this.height = heightInPx;
            return this;
        }

        public ConfigBuilder placeHolder(int placeHolderResId, boolean reuseable, int placeHolderScaleType) {
            this.placeHolderResId = placeHolderResId;
            this.reuseable = reuseable;
            this.placeHolderScaleType = placeHolderScaleType;
            return this;
        }

        public ConfigBuilder placeHolder(int placeHolderResId, boolean reuseable) {
            this.placeHolderResId = placeHolderResId;
            this.reuseable = reuseable;
            return this;
        }

        public ConfigBuilder placeHolder(int placeHolderResId) {
            this.placeHolderResId = placeHolderResId;
            this.reuseable = true;
            return this;
        }


        /**
         * 是否需要高斯模糊
         *
         * @return
         */
        public ConfigBuilder blur(int blurRadius) {
            this.needBlur = true;
            this.blurRadius = blurRadius;
            return this;
        }


        /**
         * fresco 用
         *
         * @param overlayColorWhenGif
         * @return
         */
        public ConfigBuilder asCircle(int overlayColorWhenGif) {
            this.shapeMode = ShapeMode.OVAL;
            this.roundOverlayColor = overlayColorWhenGif;
            return this;
        }

        public ConfigBuilder asCircle() {
            this.shapeMode = ShapeMode.OVAL;
            return this;
        }


        /**
         * 形状为圆角矩形时的圆角半径
         *
         * @param rectRoundRadius
         * @return
         */
        public ConfigBuilder rectRoundCorner(int rectRoundRadius, int overlayColorWhenGif) {
            this.rectRoundRadius = MyUtil.dip2px(rectRoundRadius);
            this.shapeMode = ShapeMode.RECT_ROUND;
            this.roundOverlayColor = overlayColorWhenGif;

            return this;
        }

        public ConfigBuilder rectRoundCorner(int leftTopRadius, int rightTopRadius, int leftBottomRadius, int rightBottomRadius) {
            this.leftTopRadius = MyUtil.dip2px(leftTopRadius);
            this.rightTopRadius = MyUtil.dip2px(rightTopRadius);
            this.leftBottomRadius = MyUtil.dip2px(leftBottomRadius);
            this.rightBottomRadius = MyUtil.dip2px(rightBottomRadius);
            this.shapeMode = ShapeMode.RECT_ROUND;
            return this;
        }

        /**
         * 拉伸/裁剪模式
         *
         * @param scaleMode 取值ScaleMode
         * @return
         */
        public ConfigBuilder scale(int scaleMode) {
            this.scaleMode = scaleMode;
            return this;
        }

        /**
         * todo 尚未实现此功能
         * 设置边框
         *
         * @param borderWidth
         * @param borderColor
         * @return
         */
        public ConfigBuilder border(int borderWidth, int borderColor) {
            this.borderWidth = MyUtil.dip2px(borderWidth);
            this.borderColor = borderColor;
            return this;
        }


        @Override
        public String toString() {
            return "{" +
                    "context:" + context +
                    ", ignoreCertificateVerify:" + ignoreCertificateVerify +
                    ", source:'" + sourceString + '\'' +
                    ", thumbnailUrl:'" + thumbnailUrl + '\'' +

                    ", resId=" + resId +

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
