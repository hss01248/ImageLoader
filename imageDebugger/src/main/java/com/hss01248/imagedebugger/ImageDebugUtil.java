package com.hss01248.imagedebugger;

import com.blankj.utilcode.util.Utils;
import com.hss01248.media.metadata.ExifUtil;


import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import java.text.DecimalFormat;



/**
 * Created by Administrator on 2017/3/15 0015.
 */

 class ImageDebugUtil {





    /**
     * 等比压缩（宽高等比缩放）
     *
     * @param bitmap
     * @param needRecycle
     * @param targetWidth
     * @param targeHeight
     * @return
     */
    public static Bitmap compressBitmap(Bitmap bitmap, boolean needRecycle, int targetWidth, int targeHeight) {
        float sourceWidth = bitmap.getWidth();
        float sourceHeight = bitmap.getHeight();

        float scaleWidth = targetWidth / sourceWidth;
        float scaleHeight = targeHeight / sourceHeight;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight); //长和宽放大缩小的比例
        Bitmap bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (needRecycle) {
            bitmap.recycle();
        }
        bitmap = bm;
        return bitmap;
    }


    public static String getRealType(File file) {
        if (!file.exists()) {
            return "";
        }
        if (file.getName().endsWith(".gif")) {
            return "gif";
        }
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
            String type = bytesToHexString(b).toUpperCase();
            if (type.contains("FFD8FF")) {
                return "jpg";
            } else if (type.contains("89504E47")) {
                return "png";
            } else if (type.contains("47494638")) {
                return "gif";
            } else if (type.contains("49492A00")) {
                return "tif";
            } else if (type.contains("424D")) {
                return "bmp";
            }
            return type;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
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
     * 获取指定文件夹内所有文件大小的和
     *
     * @param file file
     * @return size
     */
    public static long getFolderSize(File file) {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (File aFileList : fileList) {
                if (aFileList.isDirectory()) {
                    size = size + getFolderSize(aFileList);
                } else {
                    size = size + aFileList.length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }







    public static int[] getImageWidthHeight(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        /**
         * 最关键在此，把options.inJustDecodeBounds = true;
         * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
         */
        options.inJustDecodeBounds = true;
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(path, options); // 此时返回的bitmap为null
        } catch (Exception e) {
            e.printStackTrace();
        }
        /**
         *options.outHeight为原始图片的高
         */
        return new int[]{options.outWidth, options.outHeight};
    }


    public static Activity getActivityFromContext(Context context) {
        if (context == null) {
            return null;
        }
        if (context instanceof Activity) {
            return (Activity) context;
        }
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    public static String formatFileSize(long size) {
        try {
            DecimalFormat dff = new DecimalFormat(".00");
            if (size >= 1024 * 1024) {
                double doubleValue = ((double) size) / (1024 * 1024);
                String value = dff.format(doubleValue);
                return value + "MB";
            } else if (size > 1024) {
                double doubleValue = ((double) size) / 1024;
                String value = dff.format(doubleValue);
                return value + "KB";
            } else {
                return size + "B";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.valueOf(size);
    }

    /**
     * 复制文本到剪贴板
     *
     * @param text 文本
     */
    public static void copyText(final CharSequence text) {
        //部分机型出现SecurityException
        //http://10.0.20.7/zentao/bug-view-22357.html
        try {
            ClipboardManager clipboard = (ClipboardManager) Utils.getApp().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("text", text));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String printBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return "null";
        }
        StringBuilder stringBuilder = new StringBuilder("bitmap:\n");
        stringBuilder.append(bitmap.getWidth())
                .append("x")
                .append(bitmap.getHeight())
                .append(",")
                .append(bitmap.getConfig().name())
                .append(",isRecycled:")
                .append(bitmap.isRecycled())
                .append(",\nByteCount:")
                .append(formatFileSize(bitmap.getByteCount()))
                .append("\ndensity:")
                .append(bitmap.getDensity())
                .append("\nhasAlpha:")
                .append(bitmap.hasAlpha());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            stringBuilder.append("\n,hasMipMap:").append(bitmap.hasMipMap());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            stringBuilder.append("\nallocationByteCount:").append(formatFileSize(bitmap.getAllocationByteCount()));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stringBuilder.append("\nColorSpace:").append(bitmap.getColorSpace());
        }
        stringBuilder.append("\nGenerationId:").append(bitmap.getGenerationId());
        return stringBuilder.toString();
    }

    public static String printImageView(ImageView imageView) {
        if (imageView == null) {
            return "null";
        }

        StringBuilder stringBuilder = new StringBuilder("imageView:\n");
        stringBuilder.append(imageView.getMeasuredWidth())
                .append("x")
                .append(imageView.getMeasuredHeight())
                .append("\nscaleType:")
                .append(imageView.getScaleType().name())
                .append("\nLayoutParams:")
                .append(logWH(imageView.getLayoutParams().width))
                .append("x")
                .append(logWH(imageView.getLayoutParams().height))
                .append("\nid:")
                .append(getIdName(imageView))
                .append("\ntag:")
                .append(imageView.getTag());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            stringBuilder.append("\n,CropToPadding:").append(imageView.getCropToPadding());
            stringBuilder.append("\nAdjustViewBounds:").append(imageView.getAdjustViewBounds());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (imageView.getClipBounds() != null) {
                stringBuilder.append("\nClipBounds:").append(imageView.getClipBounds().toString());
            }
            stringBuilder.append("\nImageAlpha:").append(imageView.getImageAlpha());
            stringBuilder.append("\nAlpha:").append(imageView.getAlpha());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            stringBuilder.append("\nTintMode:").append(imageView.getImageTintMode());
        }
        stringBuilder.append("\nforground:");
        stringBuilder.append(imageView.getDrawable());
        if (imageView.getDrawable() != null) {
            stringBuilder.append(",").append(imageView.getDrawable().getIntrinsicWidth())
                    .append("x").append(imageView.getDrawable().getIntrinsicHeight());
        }
        stringBuilder.append("\nbackground:");
        stringBuilder.append(imageView.getBackground()).append("\n");
        stringBuilder.append(imageView.toString());
        return stringBuilder.toString();
    }

    private static String getIdName(View view) {
        if (view == null) {
            return "";
        }
        try {
            return view.getResources().getResourceEntryName(view.getId());
        } catch (Throwable e) {
            e.printStackTrace();
            return view.getId() + "";
        }

    }

    public static String logWH(int wh) {
        if (wh > 0) {
            return wh + "";
        }
        if (wh == ViewGroup.LayoutParams.MATCH_PARENT) {
            return " MATCH_PARENT ";
        } else if (wh == ViewGroup.LayoutParams.WRAP_CONTENT) {
            return " WRAP_CONTENT ";
        } else {
            return wh + "";
        }
    }

    public static String printException(Throwable e) {
        if (e == null) {
            return "exception is null";
        }
        return "exception:\n" + e.getClass().getName() + ":" + e.getMessage();
    }

    public static boolean isBitmapTooLarge(float bw, float bh, ImageView imageView) {

        float bitmapArea = bw * bh;
        float ivArea = imageView.getMeasuredWidth() * imageView.getMeasuredHeight();
        if (ivArea == 0) {
            return false;
        }
        return (bitmapArea / ivArea) > 1.25f;
    }

    public static int calculateScaleRatio(int width, int height, int reqWidth, int reqHeight, int multiplRatio) {

        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            //计算图片高度和我们需要高度的最接近比例值
            final int heightRatio = Math.round(((float) height / (float) reqHeight) * multiplRatio);
            //宽度比例值
            final int widthRatio = Math.round(((float) width / (float) reqWidth) * multiplRatio);
            //取比例值中的较大值作为inSampleSize
            inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    /**
     * https://www.exif.org/Exif2-2.PDF
     *
     * @param filePath
     * @return
     */
    public static String printExif(String filePath) {
        return ExifUtil.getExifStr(filePath);
    }






/*---------------------
    作者：yyanjun
    来源：CSDN
    原文：https://blog.csdn.net/yyanjun/article/details/79896677
    版权声明：本文为博主原创文章，转载请附上博文链接！*/


}
