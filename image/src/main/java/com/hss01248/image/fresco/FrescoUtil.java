package com.hss01248.image.fresco;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.URLUtil;

import com.bumptech.glide.Glide;
import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.internal.Supplier;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.cache.CountingMemoryCache;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.cache.ImageCacheStatsTracker;
import com.facebook.imagepipeline.common.Priority;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.hss01248.image.config.GlobalConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

import static com.hss01248.image.ImageLoader.context;


/**
 * Created by Administrator on 2016/6/20 0020.
 * 注意:各方法需要添加的依赖以及写在方法上方文档注释中
 *
 *
 */
public class FrescoUtil {

    public static int screenWidth;

    private static final String PHOTO_FRESCO = GlobalConfig.cacheFolderName;



    /**
     * 初始化操作，在Application的onCreate方法中初始化,建议在子线程中进行
     *
     * 添加的依赖：
                 compile 'com.facebook.fresco:fresco:0.12.0'
                 // 在 API < 14 上的机器支持 WebP 时，需要添加
                 compile 'com.facebook.fresco:animated-base-support:0.12.0'
                 // 支持 GIF 动图，需要添加
                 compile 'com.facebook.fresco:animated-gif:0.12.0'
                 // 支持 WebP （静态图+动图），需要添加
                 compile 'com.facebook.fresco:animated-webp:0.12.0'
                 compile 'com.facebook.fresco:webpsupport:0.12.0'
                 compile "com.facebook.fresco:imagepipeline-okhttp3:0.12.0+"
     * @param context
     * @param cacheSizeInM  磁盘缓存的大小，以M为单位

     */
    public static void init(final Context context, int cacheSizeInM){

        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(context)
                .setMaxCacheSize(cacheSizeInM*1024*1024)
                .setBaseDirectoryName(PHOTO_FRESCO)
                .setBaseDirectoryPathSupplier(new Supplier<File>() {
                    @Override
                    public File get() {
                        return context.getCacheDir();
                    }
                })
                .build();
        MyImageCacheStatsTracker imageCacheStatsTracker = new MyImageCacheStatsTracker();

        OkHttpClient okHttpClient= getAllPassClient();
        ImagePipelineConfig config = OkHttpImagePipelineConfigFactory.newBuilder(context,okHttpClient)
        //ImagePipelineConfig config = ImagePipelineConfig.newBuilder(context)
                .setMainDiskCacheConfig(diskCacheConfig)
                .setImageCacheStatsTracker(imageCacheStatsTracker)
                .setDownsampleEnabled(true)//Downsampling，它处理图片的速度比常规的裁剪更快，
                // 并且同时支持PNG，JPG以及WEP格式的图片，非常强大,与ResizeOptions配合使用
                .setBitmapsConfig(Bitmap.Config.RGB_565)
                .build();
        Fresco.initialize(context, config);

        WindowManager wm = (WindowManager) context.getSystemService(
                Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        screenWidth = display.getWidth() - dip2px(context,15);
    }


    /**
     * 不校验证书
     * @return
     */
    private static OkHttpClient getAllPassClient() {

        X509TrustManager xtm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                X509Certificate[] x509Certificates = new X509Certificate[]{};
                return x509Certificates;
                // return null;
            }
        };

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");

            sslContext.init(null, new TrustManager[]{xtm}, new SecureRandom());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }


        HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

       // sslSocketFactory和hostnameVerifier代码与httpsUtil中一模一样,只有这里不一样,但下面能行,这里就不行,见鬼了
        OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory())
                .hostnameVerifier(DO_NOT_VERIFY)
                .readTimeout(0, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS).writeTimeout(0, TimeUnit.SECONDS) //设置超时
                .build();

       /* OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //HttpsUtil.setAllCerPass(builder);
        builder.sslSocketFactory(sslContext.getSocketFactory());
        builder.hostnameVerifier(DO_NOT_VERIFY);
        OkHttpClient client = builder
                .readTimeout(0, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS).writeTimeout(0, TimeUnit.SECONDS) //设置超时
                .build();*/
        return client;
    }



    public static int dip2px(Context context, float dipValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dipValue * scale + 0.5f);
    }


    /**
     * 需要添加依赖:
     * compile 'com.commit451:NativeStackBlur:1.0.2'//高斯模糊
     * @param bkg
     * @param radius
     * @param downSampling
     * @return
     */
   /* private Bitmap fastBlur(Bitmap bkg, int radius,int downSampling) {
        if (downSampling < 2){
            downSampling = 2;
        }

        Bitmap smallBitmap =   Bitmap.createScaledBitmap(bkg,bkg.getWidth()/downSampling,bkg.getHeight()/downSampling,true);

        return   NativeStackBlur.process(smallBitmap, radius);
    }*/


    /**
     *
     * 需要添加依赖:
     *  compile 'jp.wasabeef:fresco-processors:2.0.0'
     *              或者自己拷贝那个类出来
     *
     *
     * 高斯模糊后显示
     * @param url
     * @param draweeView
     * @param width draweeView的宽
     * @param height draweeView的高
     * @param context
     * @param radius  高斯模糊的半径, 每一个像素都取周边(多少个)像素的平均值
     * @param sampling 采样率 原本是设置到BlurPostprocessor上的,因为高斯模糊本身对图片清晰度要求就不高,
     *                 所以此处直接设置到ResizeOptions上,直接让解码生成的bitmap就缩小,而BlurPostprocessor
     *                 内部sampling设置为1,无需再缩
     */
    /*public static void loadUrlInBlur(String url,SimpleDraweeView draweeView,
                                     int width,int height,Context context,int radius,int sampling){

        if (sampling<2){
            sampling = 2;
        }
        loadUrl(url,draweeView,new BlurPostprocessor(context,radius,1),width/sampling,height/sampling,null);

    }*/


    /**
     *  If the image has some ResizeOptions we put also the resized image into the cache with different key.
     *  currently don't support downsampling / resizing for GIFs.
     * @param url
     * @param draweeView
     * @param processor
     * @param width
     * @param height
     * @param listener
     */
    public static void loadUrl(String url, SimpleDraweeView draweeView, BasePostprocessor processor, int width, int height,
                               BaseControllerListener listener){

        url = append(url);
       load(Uri.parse(url),draweeView,processor,width,height,listener);

    }

    private static String append(String url) {
        String newUrl = url;
        boolean hasHost = url.contains("http:" ) || url.contains("https:" )  ;
        if (!hasHost){
           /* if (isWWW){
                newUrl = wwwBaseUrl + url;
            }else {
                newUrl = test1BaseUrl + url;
            }*/
        }

        return newUrl;
    }

    public static void loadFile(String file, SimpleDraweeView draweeView, BasePostprocessor processor, int width, int height,
                                BaseControllerListener listener){

        load(getFileUri(file),draweeView,processor,width,height,listener);

    }

    public static void loadFile(File file, SimpleDraweeView draweeView, BasePostprocessor processor, int width, int height,
                                BaseControllerListener listener){

        load(getFileUri(file),draweeView,processor,width,height,listener);

    }

    public static void loadRes(int resId, SimpleDraweeView draweeView, BasePostprocessor processor, int width, int height,
                               BaseControllerListener listener){

        load(getResUri(resId),draweeView,processor,width,height,listener);

    }


    public static void load(Uri uri, @NonNull SimpleDraweeView draweeView, @Nullable BasePostprocessor processor, int width, int height,
                            @Nullable BaseControllerListener listener){

        if (width ==0 && height == 0){//如果外部没有传入,那么通过draweeView拿到控件的宽高

            measureView(draweeView);

             height = draweeView.getMeasuredHeight();
             width = draweeView.getMeasuredWidth();

            //处理matchparent的情况:宽度设置为屏幕宽度减去两边的边距共30dp
            if (width < 5){//matchparent时,width为1. todo 2k和4k屏的算法优化?  ResizeOptions指定短边?
                width = screenWidth;
            }
        }



        ResizeOptions resizeOptions = null;
        if (width >0 && height > 0){
            resizeOptions = new ResizeOptions(width,height);
        }
        ImageRequest request =
                ImageRequestBuilder.newBuilderWithSource(uri)
                        .setPostprocessor(processor)
                        .setResizeOptions(resizeOptions)
                        //缩放,在解码前修改内存中的图片大小, 配合Downsampling可以处理所有图片,否则只能处理jpg,
                        // 开启Downsampling:在初始化时设置.setDownsampleEnabled(true)
                        .setProgressiveRenderingEnabled(true)//支持图片渐进式加载
                        .setAutoRotateEnabled(true) //如果图片是侧着,可以自动旋转
                        .build();

        PipelineDraweeController controller =
                (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                        .setImageRequest(request)
                        .setControllerListener(listener)
                        .setOldController(draweeView.getController())
                        .setAutoPlayAnimations(true) //自动播放gif动画
                        .build();



        draweeView.setController(controller);
    }

    /**
     * 目前该方法只支持预计算宽高设置为准确值或wrap_content的情况，
     * 不支持match_parent的情况，因为view的父view还未预计算出宽高
     * @param v 要预计算的view
     *
     *          将要预算的view传入measureView方法，再调用getMeasuredWidth()、getMeasuredHeight()就可以获得将来实际显示的宽高了。
     */
    public static void measureView(View v) {
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        if (lp == null) {
            return;
        }
        int width;
        int height;
        if (lp.width > 0) {
            // xml文件中设置了该view的准确宽度值，例如android:layout_width="150dp"
            width = View.MeasureSpec.makeMeasureSpec(lp.width, View.MeasureSpec.EXACTLY);
        } else {
            // xml文件中使用wrap_content设定该view宽度，例如android:layout_width="wrap_content"
            width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        }

        if (lp.height > 0) {
            // xml文件中设置了该view的准确高度值，例如android:layout_height="50dp"
            height = View.MeasureSpec.makeMeasureSpec(lp.height, View.MeasureSpec.EXACTLY);
        } else {
            // xml文件中使用wrap_content设定该view高度，例如android:layout_height="wrap_content"
            height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        }

        v.measure(width, height);
       // Logger.e("picaso: width:"+v.getMeasuredWidth()+"--height:"+v.getMeasuredHeight());
    }



    public static Uri getFileUri(File file){
        return Uri.fromFile(file);
    }

    public static Uri getFileUri(String filePath){
        return Uri.fromFile(new File(filePath));
    }

    public static Uri getResUri(int resId){
       return Uri.parse("res://xxyy/" + resId);
    }




    /**
     * 当设置roundAsCircle为true无效时,采用这个方法,常用在gif的圆形效果上
     *
     * 或者在xml中设置:fresco:roundWithOverlayColor="@color/you_color_id"
     "you_color_id"是指你的背景色，这样也可以实现圆角、圆圈效果
     *
     *roundAsCircle的局限性:
     * 当使用BITMAP_ONLY（默认）模式时的限制：

     并非所有的图片分支部分都可以实现圆角，目前只有占位图片和实际图片可以实现圆角，我们正在努力为背景图片实现圆角功能。
     只有BitmapDrawable 和 ColorDrawable类的图片可以实现圆角。我们目前不支持包括NinePatchDrawable和 ShapeDrawable在内的其他类型图片。（无论他们是在XML或是程序中声明的）
     动画不能被圆角。
     由于Android的BitmapShader的限制，当一个图片不能覆盖全部的View的时候，边缘部分会被重复显示，而非留白。对这种情况可以使用不同的缩放类型
     （比如centerCrop）来保证图片覆盖了全部的View。 OVERLAY_COLOR模式没有上述限制，但由于这个模式使用在图片上覆盖一个纯色图层的方式来模拟圆角效果，
     因此只有在图标背景是静止的并且与图层同色的情况下才能获得较好的效果。
     * @param draweeView
     * @param bgColor 圆形遮罩的颜色,应该与背景色一致
     */
    public static void setCircle(SimpleDraweeView draweeView, @ColorInt int bgColor){
        RoundingParams roundingParams = RoundingParams.asCircle();//这个方法在某些情况下无法成圆,比如gif
        roundingParams.setOverlayColor(bgColor);//加一层遮罩
        draweeView.getHierarchy().setRoundingParams(roundingParams);



        /*Glide.with(context)

                .load("http://inthecheesefactory.com/uploads/source/glidepicasso/cover.jpg")
                .into();*/
    }


    /**
     * 暂停网络请求
     * 在listview快速滑动时使用
     */
    public static void pause(){
        Fresco.getImagePipeline().pause();
    }


    /**
     * 恢复网络请求
     * 当滑动停止时使用
     */
    public static void resume(){
        Fresco.getImagePipeline().resume();
    }









    /**
     * 清除磁盘缓存
     */
    public static void clearDiskCache(){
        Fresco.getImagePipeline().clearDiskCaches();
    }


    /**
     * 清除单张图片的磁盘缓存
     * @param url
     */
    public static void clearCacheByUrl(String url){
        url = append(url);
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        Uri uri = Uri.parse(url);
       // imagePipeline.evictFromMemoryCache(uri);
        imagePipeline.evictFromDiskCache(uri);
        //imagePipeline.evictFromCache(uri);//这个包含了从内存移除和从硬盘移除
    }

    /**
     * 从fresco的本地缓存拿到图片,注意文件的结束符并不是常见的.jpg,.png等，如果需要另存，可自行另存
     *
     * @param url
     */
    public static File getFileFromDiskCache(String url){
        url = append(url);
        File localFile = null;
        if (!TextUtils.isEmpty(url)) {
            CacheKey cacheKey = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(ImageRequest.fromUri(url),null);
            if (ImagePipelineFactory.getInstance().getMainFileCache().hasKey(cacheKey)) {
                BinaryResource resource = ImagePipelineFactory.getInstance().getMainFileCache().getResource(cacheKey);
                localFile = ((FileBinaryResource) resource).getFile();
            } else if (ImagePipelineFactory.getInstance().getSmallImageFileCache().hasKey(cacheKey)) {
                BinaryResource resource = ImagePipelineFactory.getInstance().getSmallImageFileCache().getResource(cacheKey);
                localFile = ((FileBinaryResource) resource).getFile();
            }
        }
        return localFile;
    }

    /**
     * 拷贝缓存文件,指定目标路径和文件名
     * @param url
     * @param dir
     * @param fileName
     * @return
     */
    public static boolean copyCacheFile(String url, File dir, String fileName){
        url = append(url);
        File path = new File(dir,fileName);
      return   copyCacheFile(url,path);
    }

    /**
     *拷贝到某一个文件,已指定文件名
     * @param url 图片的完整url
     * @param path 目标文件路径
     * @return
     */
    public static boolean copyCacheFile(String url, File path){
        if (path == null ){
            return false;
        }
        File file = getFileFromDiskCache(url);
        if (file == null){
            return false;
        }

        if (path.isDirectory()){
            throw  new RuntimeException(path + "is a directory,you should call copyCacheFileToDir(String url,File dir)");
        }
        boolean isSuccess =   file.renameTo(path);

        return isSuccess;
    }

    /**
     * 拷贝到某一个目录中,自动命名
     * @param url
     * @param dir
     * @return
     */
    public static File copyCacheFileToDir(String url, File dir){
        url = append(url);
        if (dir == null ){
            return null;
        }
        if (!dir.isDirectory()){
            throw  new RuntimeException(dir + "is not a directory,you should call copyCacheFile(String url,File path)");
        }
        if (!dir.exists()){
            dir.mkdirs();
        }
        String fileName = URLUtil.guessFileName(url,"","");//android SDK 提供的方法.
        // 注意不能直接采用file的getName拿到文件名,因为缓存文件是用cacheKey命名的
        if (TextUtils.isEmpty(fileName)){
            fileName = UUID.randomUUID().toString();
        }
        File newFile = new File(dir,fileName);

       boolean isSuccess =  copyCacheFile(url,newFile);
        if (isSuccess){
            return newFile;
        }else {
            return null;
        }

    }

    /**
     *this method is return very fast, you can use it in UI thread.
     * @param url
     * @return 该url对应的图片是否已经缓存到本地
     */
    public static boolean isCached(String url) {
        url = append(url);
      // return Fresco.getImagePipeline().isInDiskCache(Uri.parse(url));

        ImageRequest imageRequest = ImageRequest.fromUri(url);
        CacheKey cacheKey = DefaultCacheKeyFactory.getInstance()
                .getEncodedCacheKey(imageRequest,null);
        return ImagePipelineFactory.getInstance()
                .getMainFileCache().hasKey(cacheKey);
    }





    /**
     * 文件下载到文件夹中：将图片缓存到本地后，将缓存的图片文件copy到另一个文件夹中
     *
     * 容易发生如下异常，progress在100处停留时间长
     * dalvikvm: Could not find method android.graphics.Bitmap.getAllocationByteCount,
     * referenced from method com.facebook.imageutils.BitmapUtil.getSizeInBytes
     06-21 16:15:39.547 3043-3244/com.hss01248.tools W/dalvikvm: VFY:
     unable to resolve virtual method 569: Landroid/graphics/Bitmap;.getAllocationByteCount ()I

     * @param url
     * @param context
     * @param dir 保存图片的文件夹
     * @param listener 自己定义的回调
     */
    public static void download(final String url, Context context, final File dir, final DownloadListener listener){

        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                .build();

        final ImagePipeline imagePipeline = Fresco.getImagePipeline();


        DataSource<Void> dataSource = imagePipeline.prefetchToDiskCache(imageRequest, context, Priority.HIGH);
        dataSource.subscribe(new BaseDataSubscriber<Void>() {
            @Override
            protected void onNewResultImpl(DataSource<Void> dataSource) {


              File file  =   copyCacheFileToDir(url,dir);
                clearCacheByUrl(url);//清除缓存
                if (file == null || !file.exists()){
                    listener.onFail();
                }else {
                    listener.onSuccess(file);
                }

            }

            @Override
            public void onProgressUpdate(DataSource<Void> dataSource) {
                super.onProgressUpdate(dataSource);
                listener.onProgress(dataSource.getProgress());
            }

            @Override
            protected void onFailureImpl(DataSource<Void> dataSource) {
                listener.onFail();
            }
        }, CallerThreadExecutor.getInstance());



    }



    /**
     * 拿到指定宽高的bitmap
     * @param url
     * @param context
     * @param width
     * @param height
     * @param listener
     */
    public static void getBitmap(String url, Context context, int width, int height, final BitmapListener listener){
        url = append(url);
        getBitmapWithProcessor(url,context,width,height,null,listener);
    }

    public static void setSupportGif(SimpleDraweeView draweeView, String url){
        PipelineDraweeController controller =
                (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                        .setUri(url)
                        .setOldController(draweeView.getController())
                        .setAutoPlayAnimations(true) //自动播放gif动画
                        .build();
        draweeView.setController(controller);
    }


    /**
     * 拿到指定宽高，并经过Processor处理的bitmap
     * @param url
     * @param context
     * @param width
     * @param height
     * @param processor 后处理器,可为null
     * @param listener
     *
     */
    public static void getBitmapWithProcessor(@NonNull final String url, @NonNull Context context, @NonNull final int width, @NonNull final int height,
                                              @Nullable BasePostprocessor processor, @NonNull final BitmapListener listener){
        final String finalUrl = append(url);
        ResizeOptions resizeOptions = null;
        if (width !=0 && height != 0 ){
            resizeOptions = new ResizeOptions(width, height);
        }

        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                .setProgressiveRenderingEnabled(false) //我们是拿bitmap对象,不是显示,所以这里不需要渐进渲染
                .setPostprocessor(processor)
                .setResizeOptions(resizeOptions)//无法支持gif
                .build();

        ImagePipeline imagePipeline = Fresco.getImagePipeline();

        DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchDecodedImage(imageRequest, context);
        dataSource.subscribe(new BaseBitmapDataSubscriber() {
            @Override
            protected void onNewResultImpl(Bitmap bitmap) {
                //注意，gif图片解码方法与普通图片不一样，是无法拿到bitmap的。如果要把gif的第一帧的bitmap返回，怎么做？
                //GifImage.create(bytes).decode(1l,9).getFrameInfo(1).
                if (bitmap == null ){
                    File cacheFile  = getFileFromDiskCache(finalUrl);
                    //还要判断文件是不是gif格式的
                    if ("gif".equalsIgnoreCase(getRealType(cacheFile))){
                            Bitmap bitmapGif = GifUtils.getBitmapFromGifFile(cacheFile);//拿到gif第一帧的bitmap
                            Bitmap target = MyBitmapUtils.compressBitmap(bitmapGif, true, width, height);//将bitmap压缩到指定宽高。
                            if (target != null) {
                                listener.onSuccess(target);
                            } else {
                                listener.onFail();
                            }


                    }else {
                        listener.onFail();
                    }

                }else {
                    listener.onSuccess(bitmap);
                }


            }

            @Override
            protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                listener.onFail();
            }
        }, CallerThreadExecutor.getInstance());

    }


    public static void loadUrlWithFace(final String url, final SimpleDraweeView draweeView){
        PointF pointF = new PointF(0.5f,0.38f);
        draweeView
                .getHierarchy()
                .setActualImageFocusPoint(pointF);
        loadUrl(url, draweeView, null, 0, 0,null);

              /*  loadUrl(url, draweeView, null, 0, 0, new BaseControllerListener(){
            @Override
            public void onFinalImageSet(String id, Object imageInfo, Animatable animatable) {
                super.onFinalImageSet(id, imageInfo, animatable);
                getBitmap(url, UIUtils.getContext(), 0, 0, new BitmapListener() {
                    @Override
                    public void onAllSuccess(final Bitmap bitmap) {
                        ThreadPoolFactory.getNormalPool().execute(new Runnable() {
                            @Override
                            public void run() {
                                final PointF f = getFaceFocusRatio(bitmap);
                                if (f != null){

                                    UIUtils.postTaskSafely(new Runnable() {
                                        @Override
                                        public void run() {
                                            draweeView
                                                    .getHierarchy()
                                                    .setActualImageFocusPoint(f);
                                        }
                                    });


                                }
                            }
                        });
                    }

                    @Override
                    public void onAllFail() {

                    }
                });
            }
        });*/



    }



    public static PointF getFaceFocusRatio(Bitmap bitmap){
        PointF pointF = new PointF(0.5f,0.5f);



//假设最多有5张脸
        int faceCount = 5;
        FaceDetector mFaceDetector = new FaceDetector(bitmap.getWidth(),bitmap.getHeight(),faceCount);
        FaceDetector.Face[] faces = new FaceDetector.Face[faceCount];
        //获取实际上有多少张脸
        faceCount = mFaceDetector.findFaces(bitmap, faces);
       // Logger.e(bitmap.getWidth()+"--width---heitht:"+ bitmap.getHeight()+"---count:"+faceCount);
        if (faceCount == 1){
            FaceDetector.Face face = faces[0];
            PointF point = new PointF();
            face.getMidPoint(point);

            pointF.x = point.x/bitmap.getWidth();
            pointF.y = point.y/bitmap.getHeight();


        }else if (faceCount >1){
            int x = 0;
            int y = 0;
            for (int i = 0; i < faceCount; i++) {//计算多边形的中心
                FaceDetector.Face face = faces[i];
                PointF point = new PointF();
                face.getMidPoint(point);
                x += point.x;
                y += point.y;
            }
            pointF.x = x/faceCount/bitmap.getWidth();
            pointF.y = y /faceCount/bitmap.getHeight();
        }else {
            pointF = null;
        }

        if (pointF != null){
           // Logger.e("x:"+ pointF.x+"---y:"+pointF.y);
        }

        return pointF;

    }

    private static String getRealType(File file){
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            byte[] b = new byte[4];
            try {
                is.read(b, 0, b.length);
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
         String type =   FrescoUtil.bytesToHexString(b).toUpperCase();
            if(type.contains("FFD8FF")){
                return "jpg";
            }else if(type.contains("89504E47")){
                return "png";
            }else if(type.contains("47494638")){
                return "gif";
            }else if(type.contains("49492A00")){
                return "tif";
            }else if(type.contains("424D")){
                return "bmp";
            }
            return type;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }




    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }




    /**
     * Created by hss01248 on 11/26/2015.
     */
    public static class MyImageCacheStatsTracker implements ImageCacheStatsTracker {
        @Override
        public void onBitmapCachePut() {

        }

        @Override
        public void onBitmapCacheHit() {

        }

        @Override
        public void onBitmapCacheMiss() {

        }

        @Override
        public void onMemoryCachePut() {

        }

        @Override
        public void onMemoryCacheHit() {

        }

        @Override
        public void onMemoryCacheMiss() {

        }

        @Override
        public void onStagingAreaHit() {

        }

        @Override
        public void onStagingAreaMiss() {

        }

        @Override
        public void onDiskCacheHit() {
            //Logger.e("ImageCacheStatsTracker---onDiskCacheHit");
        }

        @Override
        public void onDiskCacheMiss() {
            //Logger.e("ImageCacheStatsTracker---onDiskCacheMiss");
        }

        @Override
        public void onDiskCacheGetFail() {
            //Logger.e("ImageCacheStatsTracker---onDiskCacheGetFail");
        }

        @Override
        public void registerBitmapMemoryCache(CountingMemoryCache<?, ?> countingMemoryCache) {

        }

        @Override
        public void registerEncodedMemoryCache(CountingMemoryCache<?, ?> countingMemoryCache) {

        }
    }



    public interface BitmapListener{
        void onSuccess(Bitmap bitmap);
        void onFail();
    }

    public interface  DownloadListener{
        void onSuccess(File file);
        void onFail();

        void onProgress(float progress);
    }
}
