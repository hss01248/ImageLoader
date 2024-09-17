package com.hss01248.img.compressor;


import android.text.TextUtils;

import androidx.exifinterface.media.ExifInterface;

import com.blankj.utilcode.util.LogUtils;

import java.io.File;
import java.io.IOException;

public class MotionImageUtil {

    public static boolean isMotionImage(String filePath){
        File file = new File(filePath);
        if(!file.exists() ){
            return false;
        }
        if(!file.isFile()){
            return false;
        }
        if(file.length() ==0){
            return false;
        }
        try {
            ExifInterface exifInterface = new ExifInterface(file);
            String xmp = exifInterface.getAttribute(ExifInterface.TAG_XMP);
            if(TextUtils.isEmpty(xmp)){
                return false;
            }


        } catch (Exception e) {
            LogUtils.w(filePath,e);
            return false;
        }
    }
}
