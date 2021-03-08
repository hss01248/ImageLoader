package com.hss01248.imageloaderdemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.Animatable;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.util.Log;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;


import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * https://github.com/CentMeng/Face/blob/master/app/src/main/java/com/test/face/MainActivity.java
 * Created by huangshuisheng on 2018/6/23.
 */

public class FrescoFaceCropActivity extends Activity {


    @BindView(R.id.iv1)
    SimpleDraweeView iv1;
    @BindView(R.id.iv2)
    SimpleDraweeView iv2;
    @BindView(R.id.iv3)
    SimpleDraweeView iv3;
    @BindView(R.id.iv4)
    SimpleDraweeView iv4;
    /**
     * 人脸识别最多数
     */
    public final static int FACE_COUNT = 1;


    public static void launch(Activity activity) {
        activity.startActivity(new Intent(activity, FrescoFaceCropActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_crop);
        ButterKnife.bind(this);

        initData();
    }

    private void initData() {
        String url1 = "http://pic1.win4000.com/wallpaper/2017-11-13/5a0980eaa1d8c.jpg";
        String url2 = "http://pic1.win4000.com/wallpaper/6/58808992d5530.jpg";
        String url3 = "http://d.ifengimg.com/w640_h500/y1.ifengimg.com/a/2015_41/98958af668e0f40.jpg";
        String url4 = "http://video.rednet.cn/uploadfile/2015/1009/20151009114134390.jpg";
        //
       /* ImageLoader.with(this)
            .url(url1)
            .into(iv1);

        ImageLoader.with(this)
           // .cropFace()
            .url(url2)
            .into(iv2);

        ImageLoader.with(this)
            //.cropFace()
            .url(url3)
            .into(iv3);

        ImageLoader.with(this)
            //.cropFace()
            .url(url4)
            .into(iv4);*/
        check(iv1, url1);
        check(iv2, url2);
        check(iv3, url3);
        check(iv4, url4);
    }


    /**
     * Fresco getController
     *
     * @param sdv                SimpleDraweeView
     * @param url                图片地址
     * @param controllerListener 下载图片后执行事件
     * @return
     */
    private static DraweeController getController(SimpleDraweeView sdv, String url, ControllerListener controllerListener) {
        ImageRequest request = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(url))
                .setProgressiveRenderingEnabled(true)
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setControllerListener(controllerListener)
                .setOldController(sdv.getController())
                .build();
        return controller;
    }


    /**
     * 人脸识别
     *
     * @param bitmap
     * @return 人脸中间位置
     */
    public PointF setFace(Bitmap bitmap) {
        FaceDetector fd;
        FaceDetector.Face[] faces = new FaceDetector.Face[FACE_COUNT];
        PointF midpoint = new PointF();
        int count = 0;
        try {
            //     这是关键点，官方要求必须是RGB_565否则识别不出来
            Bitmap faceBitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
            bitmap.recycle();
            // 宽高不等的话会报异常（IllegalArgumentException if the Bitmap dimensions don't match the dimensions defined at initialization），这个可以从源码中看出
            fd = new FaceDetector(faceBitmap.getWidth(), faceBitmap.getHeight(), FACE_COUNT);
            count = fd.findFaces(faceBitmap, faces);
            faceBitmap.recycle();
            Toast.makeText(this, "有" + count + "张脸", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("检测脸部", "setFace(): " + e.toString());
            return midpoint;
        }

        // 检测出来的脸部获取位置，用于设置SimpleDraweeView
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                try {
                    faces[i].getMidPoint(midpoint);
                } catch (Exception e) {
                    Log.e("检测脸部", "setFace(): face " + i + ": " + e.toString());
                }
            }
        }

        return midpoint;
    }

    public void check(final SimpleDraweeView sdv_picture, String url) {
        sdv_picture.setController(getController(sdv_picture, url, new ControllerListener<ImageInfo>() {
            @Override
            public void onSubmit(String id, Object callerContext) {

            }

            @Override
            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                /**在调用getDrawingCache的时候要注意下面2点：
                 在调用getDrawingCache()方法从ImageView对象获取图像之前，一定要调用setDrawingCacheEnabled(true)方法：
                 imageview.setDrawingCacheEnabled(true);
                 否则，无法从ImageView对象iv_photo中获取图像； **/
                sdv_picture.setDrawingCacheEnabled(true);
                Bitmap bitmap = sdv_picture.getDrawingCache();
                PointF pointF = setFace(bitmap);
                //按百分比算，防止偏差
                PointF pointF1 = new PointF(pointF.x / (float) sdv_picture.getWidth(), pointF.y / (float) sdv_picture.getHeight());
                //设置聚焦点
                sdv_picture.getHierarchy()
                        .setActualImageFocusPoint(pointF1);
                /**在调用getDrawingCache()方法从ImageView对象获取图像之后，一定要调用setDrawingCacheEnabled(false)方法：
                 imageview.setDrawingCacheEnabled(false);
                 以清空画图缓冲区，否则，下一次从ImageView对象iv_photo中获取的图像，还是原来的图像。**/
                sdv_picture.setDrawingCacheEnabled(false);
            }

            @Override
            public void onIntermediateImageSet(String id, ImageInfo imageInfo) {

            }

            @Override
            public void onIntermediateImageFailed(String id, Throwable throwable) {

            }

            @Override
            public void onFailure(String id, Throwable throwable) {

            }

            @Override
            public void onRelease(String id) {

            }
        }));
        sdv_picture.setAspectRatio(1.618f);
    }
}
