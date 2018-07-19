package com.hss01248.frescoloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.QualityInfo;

/**
 * Created by hss on 2018/7/19.
 */

public class FileImageInfo implements ImageInfo {

    int width;
    int height ;

    public FileImageInfo(String path){
        BitmapFactory.Options options = new BitmapFactory.Options();

        /**
         * 最关键在此，把options.inJustDecodeBounds = true;
         * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
         */
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options); // 此时返回的bitmap为null
        width = options.outWidth;
        height = options.outHeight;

    }
    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public QualityInfo getQualityInfo() {
        return null;
    }
}
