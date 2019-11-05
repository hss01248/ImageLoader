package com.hss01248.frescoloader;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.renderscript.RSRuntimeException;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.internal.Supplier;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.AutoRotateDrawable;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.DraweeHolder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.common.ImageDecodeOptionsBuilder;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.view.BigImageView;
import com.hss01248.frescoloader.big.BigImageLoader;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.MyUtil;
import com.hss01248.image.config.GlobalConfig;
import com.hss01248.image.config.ShapeMode;
import com.hss01248.image.config.SingleConfig;
import com.hss01248.image.interfaces.FileGetter;
import com.hss01248.image.interfaces.ILoader;

import java.io.File;

import jp.wasabeef.fresco.processors.BlurPostprocessor;
import jp.wasabeef.fresco.processors.internal.FastBlur;
import jp.wasabeef.fresco.processors.internal.RSBlur;
import okhttp3.OkHttpClient;


import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.hss01248.image.config.GlobalConfig.context;

/**
 * Created by Administrator on 2017/3/15 0015.
 *
 * 参考: https://github.com/ladingwu/ImageLoaderFramework/blob/5b943f69f042d153fdde3bb767d68072422f696d/fresco/src/main/java/com/ladingwu/frescolibrary/FrescoImageLoader.java
 */

public class FrescoLoader implements ILoader {
    @Override
    public void init(final Context context, int cacheSizeInM) {
        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(context)
                .setMaxCacheSize(cacheSizeInM*1024*1024)
                .setBaseDirectoryName(GlobalConfig.cacheFolderName)
                .setBaseDirectoryPathSupplier(new Supplier<File>() {
                    @Override
                    public File get() {
                        return context.getCacheDir();
                    }
                })
                .build();
        MyImageCacheStatsTracker imageCacheStatsTracker = new MyImageCacheStatsTracker();

        OkHttpClient okHttpClient= MyUtil.getClient(GlobalConfig.ignoreCertificateVerify);
        ImagePipelineConfig config = OkHttpImagePipelineConfigFactory.newBuilder(context,okHttpClient)
                //ImagePipelineConfig config = ImagePipelineConfig.newBuilder(context)
                .setMainDiskCacheConfig(diskCacheConfig)
                .setImageCacheStatsTracker(imageCacheStatsTracker)
                .setDownsampleEnabled(true)//Downsampling，它处理图片的速度比常规的裁剪更快，
                // 并且同时支持PNG，JPG以及WEP格式的图片，非常强大,与ResizeOptions配合使用
                .setBitmapsConfig(Bitmap.Config.RGB_565)

                //让fresco即时清理内存:http://blog.csdn.net/honjane/article/details/65629799
                .setBitmapMemoryCacheParamsSupplier(new MyBitmapMemoryCacheParamsSupplier((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)))
                .build();
        //Fresco.initialize(context, config);

        BigImageViewer.initialize(BigImageLoader.with(context,config));

    }

    @Override
    public void request(SingleConfig config) {
        if(config.isAsBitmap()){
            requestBitmap(config);
        }else {
            //requestForImageView(config);
            if(config.getTarget() instanceof BigImageView){
                MyUtil.viewBigImage(config);
            }else if(config.getTarget() instanceof SimpleDraweeView){
                requestForSimpleDraweeView((SimpleDraweeView) config.getTarget(),config);
            }else if(config.getTarget() instanceof ImageView){
                requestForImageView((ImageView) config.getTarget(),config);
            }else {
                //todo 抛出异常
            }
        }


    }

    @Override
    public void debug(SingleConfig config) {

    }

    private void requestForSimpleDraweeView(SimpleDraweeView target, SingleConfig config) {

        // 数据-model
        ImageRequest request = buildRequest(config);

        //view
        setDraweeHierarchyForDraweeView(config);

        //controller
        PipelineDraweeControllerBuilder controller = buildPipelineDraweeController(config,request);
        controller.setOldController(target.getController());

        target.setController(controller.build());
    }

    private void requestForImageView(ImageView imageView,final SingleConfig config) {

           /* config.setBitmapListener(new SingleConfig.BitmapListener() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    imageView.setImageBitmap(bitmap);
                }

                @Override
                public void onFail(Throwable e) {
                    if(config.getErrorResId() >0){
                        imageView.setImageResource(config.getErrorResId());
                    }
                }
            });
            requestBitmap(config);
            return;*/
           checkWrapContentOrMatchParent(config);


        //GenericDraweeHierarchy hierarchy=null;
        GenericDraweeHierarchy hierarchy = GenericDraweeHierarchyBuilder.newInstance(imageView.getContext().getResources()).build();
        setupHierarchy(hierarchy,config);

        // 数据-model
        ImageRequest request = buildRequest(config);

        //设置controller

        DraweeHolder draweeHolder= (DraweeHolder) imageView.getTag(R.id.fresco_drawee);


        PipelineDraweeControllerBuilder controllerBuilder = buildPipelineDraweeController(config,request);


        if (draweeHolder == null) {
            draweeHolder=DraweeHolder.create(hierarchy,imageView.getContext());
        }else {
             controllerBuilder.setOldController(draweeHolder.getController());
            draweeHolder.setHierarchy(hierarchy);
        }

        draweeHolder.setController(controllerBuilder.build());


        //imageview的特殊处理
        ViewStatesListener mStatesListener=new ViewStatesListener(draweeHolder);

        imageView.addOnAttachStateChangeListener(mStatesListener);

        // 判断是否ImageView已经 attachToWindow
        if (ViewCompat.isAttachedToWindow(imageView)) {
            draweeHolder.onAttach();
        }

        //设置scaletype
        //setImageViewScaleType(imageView,config);


//        if (ViewC.isAttachedToWindow()) {
//            draweeHolder.onAttach();
//        }
        // 保证每一个ImageView中只存在一个draweeHolder
        imageView.setTag(R.id.fresco_drawee,draweeHolder);
        // 拿到数据
        imageView.setImageDrawable(draweeHolder.getTopLevelDrawable());


    }

    private void checkWrapContentOrMatchParent(SingleConfig config) {
        if(!(config.getTarget() instanceof ImageView)){
            return;
        }
        ViewGroup.LayoutParams params=config.getTarget().getLayoutParams();
        if (params==null) {
            params=new ViewGroup.LayoutParams(200,200);
        }

        if (params.width==WRAP_CONTENT){
            //params.width=MATCH_PARENT;
            config.widthWrapContent = true;
        }
        if (params.height==WRAP_CONTENT){
            //params.height=MATCH_PARENT;
            config.heightWrapContent = true;
        }
        //imageView.setLayoutParams(params);
    }


    private PipelineDraweeControllerBuilder buildPipelineDraweeController(final SingleConfig config, final ImageRequest request) {
        final PipelineDraweeControllerBuilder builder =  Fresco.newDraweeControllerBuilder();
        builder.setImageRequest(request);

        if(!config.isAsBitmap()){
            builder.setAutoPlayAnimations(true);//自动播放gif动画
        }


        builder.setControllerListener(new ControllerListener<ImageInfo>() {
            @Override
            public void onSubmit(String id, Object callerContext) {

            }

            @Override
            public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                /*if(config.getBitmapListener()!=null){
                    config.setAsBitmap(true);
                    getLoader().request(config);
                }*/
                if(config.isAsBitmap()){
                    return;
                }
                if(config.getImageListener() ==null){
                    return;
                }
                if(TextUtils.isEmpty(config.getUrl())){
                    return;
                }
                config.getImageListener().onSuccess(null,null,imageInfo.getWidth(),imageInfo.getHeight());
//                File file = getFileFromDiskCache(config.getUrl());
//                if(file!=null && file.exists()){
//                    if(imageInfo ==null){
//                        imageInfo = new FileImageInfo(file.getAbsolutePath());
//                    }
//                    config.getImageListener().onSuccess(null,null,0,0);
//                }else {
//                    config.getBitmapListener().onFail(new Throwable("file not found:"+config.getUrl()));
//                }


            }

            @Override
            public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {

            }

            @Override
            public void onIntermediateImageFailed(String id, Throwable throwable) {

            }

            @Override
            public void onFailure(String id, Throwable throwable) {
                /*if(config.getBitmapListener()!=null){
                    config.getBitmapListener().onFail(throwable);
                }*/
                if(config.isAsBitmap()){
                    return;
                }
                if(config.getImageListener() ==null){
                    return;
                }
                config.getImageListener().onFail(throwable);
            }

            @Override
            public void onRelease(String id) {

            }
        });
        return builder;
    }

    private void setDraweeHierarchyForDraweeView(SingleConfig config) {
        SimpleDraweeView simpleDraweeView = null;
        if(config.isAsBitmap()){
            return;
        }

        if(config.getTarget() instanceof SimpleDraweeView){
            simpleDraweeView = (SimpleDraweeView) config.getTarget();
        }
        if(simpleDraweeView ==null){
            return;
        }

        GenericDraweeHierarchy hierarchy = simpleDraweeView.getHierarchy();

        if(hierarchy==null){
            hierarchy = new GenericDraweeHierarchyBuilder(context.getResources()).build();
        }

        setupHierarchy(hierarchy,config);

        simpleDraweeView.setHierarchy(hierarchy);


    }

    private void setupHierarchy(GenericDraweeHierarchy hierarchy, SingleConfig config) {
        //边角形状和边框
        int shapeMode = config.getShapeMode();
        RoundingParams roundingParams =null;

        switch (shapeMode){
            case ShapeMode.RECT:
                roundingParams = RoundingParams.fromCornersRadius(0);
                if(config.getBorderWidth()>0){
                    roundingParams.setBorder(config.getBorderColor(),config.getBorderWidth());
                }
                break;
            case ShapeMode.RECT_ROUND:
                roundingParams = RoundingParams.fromCornersRadius(config.getRectRoundRadius());
                if(config.getBorderWidth()>0){
                    roundingParams.setBorder(config.getBorderColor(),config.getBorderWidth());
                }
                if(config.isGif() && config.getRoundOverlayColor()>0){
                    roundingParams.setOverlayColor(config.getRoundOverlayColor());
                }
                break;
            case ShapeMode.OVAL:
                roundingParams = RoundingParams.asCircle();
                if(config.getBorderWidth()>0){
                    roundingParams.setBorder(config.getBorderColor(),config.getBorderWidth());
                }
                if(config.isGif() && config.getRoundOverlayColor()>0){
                    roundingParams.setOverlayColor(config.getRoundOverlayColor());
                }
                break;
            default:
                break;
        }

        hierarchy.setRoundingParams(roundingParams);





        //loading图
        if(config.getLoadingResId()>0){
            ScalingUtils.ScaleType scaleType2 = ScalingUtils.ScaleType.CENTER_INSIDE;
            if(config.getLoadingScaleType() >0){
                scaleType2 = FrescoUtil.getActualScaleType(config.getLoadingScaleType());
            }
//todo 提供几种选择，以及可以自己设置内部圈圈  另外,设置了loading,就不要占位图了
            /*AnimationDrawable animationDrawable = new AnimationDrawable();
            Drawable drawable = ImageLoader.context.getResources().getDrawable(config.getLoadingResId());
            if(drawable != null){
                animationDrawable.addFrame(drawable,50);
                animationDrawable.setOneShot(false);
                hierarchy.setProgressBarImage(animationDrawable,scaleType2);//new ProgressBarDrawable(),R.drawable.progressstyleshape
            }*/

            Object roundingParams1 = ImageLoader.context.getResources().getDrawable(config.getLoadingResId());//R.drawable.imageloader_loading_50
            roundingParams1 = new AutoRotateDrawable((Drawable) roundingParams1, 1200);
            hierarchy.setProgressBarImage((Drawable) roundingParams1,scaleType2);
        }

        //占位图
        if(MyUtil.shouldSetPlaceHolder(config)){
            ScalingUtils.ScaleType scaleType = ScalingUtils.ScaleType.CENTER_CROP;
            if(config.getPlaceHolderScaleType() >0){
                scaleType = FrescoUtil.getActualScaleType(config.getPlaceHolderScaleType());
            }
            if(config.getPlaceHolderResId() >0){
                hierarchy.setPlaceholderImage(config.getPlaceHolderResId(), scaleType);
            }
        }

        //正常图的伸缩模式
        ScalingUtils.ScaleType scaleMode = FrescoUtil.getActualScaleType(config.getScaleMode());
        hierarchy.setActualImageScaleType(scaleMode);

        //失败图
        if(config.getErrorResId()>0){
            ScalingUtils.ScaleType scaleType3 = ScalingUtils.ScaleType.CENTER_INSIDE;
            if(config.getErrorScaleType() >0){
                scaleType3 = FrescoUtil.getActualScaleType(config.getErrorScaleType());
            }
            hierarchy.setFailureImage(config.getErrorResId(),scaleType3);
        }
    }

    private ImageRequest buildRequest(SingleConfig config) {
        Uri uri = MyUtil.buildUriByType(config);

        ImageRequestBuilder builder =   ImageRequestBuilder.newBuilderWithSource(uri);

        if(!config.isAsBitmap()){
            builder.setProgressiveRenderingEnabled(true)//支持图片渐进式加载
                    .setLocalThumbnailPreviewsEnabled(true);

        }

        Postprocessor postprocessor=null;
        if(config.isNeedBlur()){
            postprocessor = new BlurPostprocessor(context,config.getBlurRadius(),2);
        }
        if(config.isCropFace()){
            //postprocessor = new FaceCenterCrop(config.getWidth(), config.getHeight());//脸部识别
        }


        ResizeOptions resizeOptions = getResizeOption(config);

        ImageDecodeOptionsBuilder decodeOptionsBuilder = ImageDecodeOptions.newBuilder();
        if(config.isUseARGB8888()){
            decodeOptionsBuilder.setBitmapConfig(Bitmap.Config.ARGB_8888);
        }else {
            decodeOptionsBuilder.setBitmapConfig(Bitmap.Config.RGB_565);
        }
        /*if(config.isAsBitmap()){
            decodeOptionsBuilder.setDecodePreviewFrame(true);
        }*/

        builder.setPostprocessor(postprocessor)
                .setImageDecodeOptions(decodeOptionsBuilder.build())
                .setResizeOptions(resizeOptions)//缩放,在解码前修改内存中的图片大小, 配合Downsampling可以处理所有图片,否则只能处理jpg,
                // 开启Downsampling:在初始化时设置.setDownsampleEnabled(true)
                .setAutoRotateEnabled(true);



        return builder.build();
    }

    private ResizeOptions getResizeOption(SingleConfig config) {
        ResizeOptions resizeOptions = null;
        if (config.getWidth() >0 && config.getHeight() > 0){
            resizeOptions = new ResizeOptions(config.getWidth(),config.getHeight());
        }else {
            //todo 通过图片宽高和view宽高计算出最合理的resization

        }
        return resizeOptions;
    }





    /**
     * 千万不要把bitmap复制给onNewResultImpl函数范围之外的任何变量。订阅者执行完操作之后，image pipeline 会回收这个bitmap，释放内存。在这个函数范围内再次使用这个Bitmap对象进行绘制将会导致IllegalStateException。
     Bitmap对象对象回调线程有可能是在UI主线程回调，也有可能在子线程中回调，如果需要更新UI，需要做判断进行不同的逻辑处理。
     * 注:能够拿到网络gif的第一帧图,但拿不到res,本地file的第一帧图
     * @param config
     */
    private void requestBitmap(final SingleConfig config) {

        final ImageRequest request = buildRequest(config);
        final String finalUrl = request.getSourceUri().toString();//;MyUtil.appendUrl(config.getUrl());
        //Log.e("uri",finalUrl);

        DataSource<CloseableReference<CloseableImage>> dataSource = Fresco.getImagePipeline().fetchDecodedImage(request, GlobalConfig.context);

        dataSource.subscribe(new MyBaseBitmapDataSubscriber(finalUrl,config.getWidth(),config.getHeight()) {
            @Override
            protected void onNewResultImpl(Bitmap bitmap,DataSource<CloseableReference<CloseableImage>> dataSource) {
                //注意，gif图片解码方法与普通图片不一样，是无法拿到bitmap的。如果要把gif的第一帧的bitmap返回，怎么做？
                //GifImage.create(bytes).decode(1l,9).getFrameInfo(1).
                Bitmap bitmap1 = null;
                if(config.getShapeMode() == ShapeMode.OVAL){
                    bitmap1 = MyUtil.cropCirle(bitmap,false);

                }else if(config.getShapeMode() == ShapeMode.RECT_ROUND && config.getRectRoundRadius()>0){
                    bitmap1 = MyUtil.rectRound(bitmap,config.getRectRoundRadius(),0);
                }else {
                    bitmap1 = Bitmap.createBitmap(bitmap);
                }
                //不要将bitmap放到fresco的内存池中,不然又被回收
               // FrescoUtil.putIntoPool(bitmap1,finalUrl);
                config.getBitmapListener().onSuccess(bitmap1);
            }

            @Override
            protected void onFail(Throwable e) {
                config.getBitmapListener().onFail(e);
            }
        }, CallerThreadExecutor.getInstance());

    }



    @Override
    public void pause() {
        Fresco.getImagePipeline().pause();
    }

    @Override
    public void resume() {
        Fresco.getImagePipeline().resume();
    }

    @Override
    public void clearDiskCache() {

        Fresco.getImagePipeline().clearDiskCaches();
    }

    @Override
    public void clearMomoryCache() {
        Fresco.getImagePipeline().clearMemoryCaches();
    }

    @Override
    public long getCacheSize() {
        return MyUtil.getCacheSize();
    }

    @Override
    public void clearCacheByUrl(String url) {
        url = MyUtil.appendUrl(url);
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        Uri uri = Uri.parse(url);
         imagePipeline.evictFromMemoryCache(uri);
        imagePipeline.evictFromDiskCache(uri);
        //imagePipeline.evictFromCache(uri);//这个包含了从内存移除和从硬盘移除
    }

    @Override
    public void clearMomoryCache(View view) {

    }

    @Override
    public void clearMomoryCache(String url) {
        url = MyUtil.appendUrl(url);
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        Uri uri = Uri.parse(url);
        imagePipeline.evictFromMemoryCache(uri);
    }

    @Override
    public File getFileFromDiskCache(String url) {
        url = MyUtil.appendUrl(url);
        File localFile = null;
        if (!TextUtils.isEmpty(url)) {
            Log.d("getfilefromdisk","url is:"+url);
            CacheKey cacheKey = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(ImageRequest.fromUri(url),false);
            Log.d("getfilefromdisk","cacheKey is:"+cacheKey);
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

    @Override
    public void getFileFromDiskCache(String url, FileGetter getter) {
        download(url,getter);
    }

    @Override
    public boolean isCached(String url) {
       /* if(TextUtils.isEmpty(url)){
            return false;
        }
        url = MyUtil.appendUrl(url);
        DataSource<Boolean> isIn = Fresco.getImagePipeline().isInDiskCache(Uri.parse(url));
        if(isIn!=null){
            try {
                return isIn.getResult();
            }catch (Exception e){
                return false;
            }
        }else {
            return false;
        }*/


        ImageRequest imageRequest = ImageRequest.fromUri(url);
        CacheKey cacheKey = DefaultCacheKeyFactory.getInstance()
                .getEncodedCacheKey(imageRequest,null);
        return ImagePipelineFactory.getInstance()
                .getMainFileCache().hasKey(cacheKey);
    }

    @Override
    public  void trimMemory(int level){
       //todo  BitmapMemoryCacheTrimStrategy.getTrimRatio(trimType)
    }

    @Override
    public  void onLowMemory(){
        Fresco.getImagePipeline().clearMemoryCaches();
    }

    @Override
    public void download(String url, FileGetter getter) {
        if(isCached(url)){
            File file = getFileFromDiskCache(url);
            if(file!=null && file.exists()){

                int[] wh = MyUtil.getImageWidthHeight(file.getAbsolutePath());
                getter.onSuccess(file,wh[0],wh[1]);
            }else {
                getter.onFail(new Throwable("file does not exist"));
            }
        }else {
            downloadReally(url,getter);
        }
    }

    private void downloadReally(final String url, final FileGetter getter) {
        ImageRequest imageRequest = ImageRequest.fromUri(url);
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<Void> dataSource = imagePipeline.prefetchToDiskCache(imageRequest, false);
        DataSubscriber<Void> dataSubscriber = new DataSubscriber<Void>() {
            @Override
            public void onNewResult(DataSource<Void> dataSource) {
                if(dataSource.hasFailed()){
                    onFailure(dataSource);
                }else {
                    File file = getFileFromDiskCache(url);
                    if(file!=null && file.exists()){
                        int[] wh = MyUtil.getImageWidthHeight(file.getAbsolutePath());
                        getter.onSuccess(file,wh[0],wh[1]);
                    }else {
                        getter.onFail(new Throwable("file does not exist after prefetched"));
                    }
                }
            }

            @Override
            public void onFailure(DataSource<Void> dataSource) {
                getter.onFail(dataSource.getFailureCause());
            }

            @Override
            public void onCancellation(DataSource<Void> dataSource) {
                getter.onFail(dataSource.getFailureCause());
            }

            @Override
            public void onProgressUpdate(DataSource<Void> dataSource) {

            }
        };
        dataSource.subscribe(dataSubscriber, UiThreadImmediateExecutorService.getInstance());
    }


    public static Bitmap blur(Bitmap source,int mRadius,boolean recycleOriginal){
        int mSampling = 2;
        int width = source.getWidth();
        int height = source.getHeight();
        int scaledWidth = width / mSampling;
        int scaledHeight = height / mSampling;

        Bitmap bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, source.getConfig());


        Canvas canvas = new Canvas(bitmap);
        canvas.scale(1 / (float) mSampling, 1 / (float) mSampling);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(source, 0, 0, paint);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            try {
                bitmap = RSBlur.blur(ImageLoader.context, bitmap, mRadius);
            } catch (RSRuntimeException e) {
                bitmap = FastBlur.blur(bitmap, mRadius, true);
            }
        } else {
            bitmap = FastBlur.blur(bitmap, mRadius, true);
        }
        if(recycleOriginal){
            source.recycle();
        }

        return bitmap;
    }


}
