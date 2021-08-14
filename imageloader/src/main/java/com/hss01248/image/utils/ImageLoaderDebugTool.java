package com.hss01248.image.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.hss01248.image.MyUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * time:2019/11/9
 * author:hss
 * desription:
 */
public class ImageLoaderDebugTool {


    public static void warnBigBitmapInCurrentViewTree(final View rootView) {


        // 获取屏幕像素
        WindowManager wm = (WindowManager) rootView.getContext().getSystemService(Context.WINDOW_SERVICE);
        final Display display = wm.getDefaultDisplay();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                List<ImageViewInfo> list = new ArrayList<>();
                findImageViewInViewTree(rootView, list);

                for (ImageViewInfo imageViewInfo : list) {
                    Log.e("haha", "ImageView bitmap size：" + imageViewInfo.imageSize / 1024f + "KB," + "height:" + imageViewInfo.imageHeight + ",width:" +
                            imageViewInfo.imageWidth + "\nimageView:" + imageViewInfo.imgViewInfo);
                }
            }
        });


    }


    private static class ImageViewInfo {
        int imageNum;
        int imageSize;
        int imageHeight;
        int imageWidth;
        String imgViewInfo;
    }

    private static void findImageViewInViewTree(View curNode, List<ImageViewInfo> imageList) {

        if (curNode.getVisibility() != View.VISIBLE) {
            return;
        }
        if (curNode instanceof ViewGroup) {
            ViewGroup curNodeGroup = (ViewGroup) curNode;
            for (int i = 0; i < curNodeGroup.getChildCount(); i++) {
                findImageViewInViewTree(curNodeGroup.getChildAt(i), imageList);
            }
        } else {
            if (curNode instanceof ImageView) {
                ImageViewInfo imageViewInfo = new ImageViewInfo();
                ImageView curImage = (ImageView) curNode;
                Drawable drawable = curImage.getDrawable();
                if (drawable instanceof BitmapDrawable) {
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    imageViewInfo.imageHeight = bitmap.getHeight();
                    imageViewInfo.imageWidth = bitmap.getWidth();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {     //API 19
                        imageViewInfo.imageSize = bitmap.getAllocationByteCount();
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {//API 12
                        imageViewInfo.imageSize = bitmap.getByteCount();
                    } else {
                        imageViewInfo.imageSize = bitmap.getRowBytes() * bitmap.getHeight();
                    }

                    if (bitmap.getHeight() * bitmap.getWidth() > curImage.getMeasuredHeight() * curImage.getMeasuredWidth()) {
                        imageViewInfo.imgViewInfo = MyUtil.printImageView(curImage);
                        imageList.add(imageViewInfo);
                    }

                } else {
                    if(drawable != null){
                        imageViewInfo.imageHeight = drawable.getIntrinsicHeight();
                        imageViewInfo.imageWidth = drawable.getIntrinsicWidth();
                        if (imageViewInfo.imageHeight * imageViewInfo.imageWidth > curImage.getMeasuredHeight() * curImage.getMeasuredWidth()) {
                            imageViewInfo.imgViewInfo = MyUtil.printImageView(curImage);
                            imageList.add(imageViewInfo);
                        }
                    }

                }

            }
        }
        return;
    }
}
