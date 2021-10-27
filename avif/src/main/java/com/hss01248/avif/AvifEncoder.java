package com.hss01248.avif;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


public class AvifEncoder {

    public static Context context;

    public static void init(Application application){
        context = application;
    }


    public static File encodeOneFile(String path){
        File file = new File(path);
        String name = file.getName();
        if(!name.contains(".") || name.endsWith(".")){
            Log.w("avif","文件无后缀名,无需压缩: "+path);
            return file;
        }
        String suffix = name.substring(name.lastIndexOf(".")+1);
        if("avif".equalsIgnoreCase(suffix)){
            Log.w("avif","已经是avif,不再压缩");
            return file;
        }
        if("gif".equalsIgnoreCase(suffix)){
            Log.w("avif","暂不压缩gif");
            return file;
        }
        if("jpg".equalsIgnoreCase(suffix)
        || "jpeg".equalsIgnoreCase(suffix)
        || "png".equalsIgnoreCase(suffix)
        || "webp".equalsIgnoreCase(suffix)){
           String newName = name.substring(0,name.lastIndexOf("."));
            newName = newName+".avif";
            File dir = file.getParentFile();
            if(!file.canWrite()){
                dir = context.getExternalFilesDir("avif");
                Log.w("avif","没有写的权限,压缩目的地目录改成getExternalFilesDir(avif):"+dir.getAbsolutePath());
            }
            File out = new File(dir,newName);
            Log.d("avif","out path:"+out.getAbsolutePath());

           boolean success =  encodeFileTo(path,out.getAbsolutePath());
           if(success){
               Log.i("avif","avif压缩成功:"+out);
               return out;
           }
           return file;

        }else {
            Log.w("avif","不是图片文件,不压缩:"+path);
            return file;
        }


    }


    public static boolean  encodeFileTo(String input,String output){
       return encodeFileTo(input, output,25,35);
    }
    /**
     *
     * @param input
     * @param output
     * @param quality
     * @param q2
     * @return
     */
    public static boolean  encodeFileTo(String input,String output,int quality,int q2) {
        try {
            Bitmap bitmap = decodeBitmap(input);
            if (bitmap == null) {
                Log.w("avif", "bitmap is null");
                return false;
            }
            String rawPath = writeRaw(bitmap);
           /* int qua1 = 63-quality;
            int  qua2 = qua1 + 10;

            if(qua2 > 63) qua2 = 63;
            if(qua1 == 0) qua2 = 0;*/
            File outFile = new File(output);
            File tmp = new File(outFile.getParent(),new Random(Integer.MAX_VALUE).nextInt()+".avif");
            if(tmp.exists()){
                tmp.delete();
            }
            if(outFile.exists()){
                outFile.delete();
            }
            boolean success = execAvifEncoder(rawPath, tmp.getAbsolutePath(), bitmap.getWidth(), bitmap.getHeight(), 8, quality, q2, 10);
            if (success) {
                File file = new File(output);
                if (tmp.exists() && tmp.length() > 10) {
                    boolean renameTo = tmp.renameTo(outFile);
                    if(renameTo){
                        return true;
                    }else {
                        //todo copy file
                        Log.w("avif","rename 失败 "+tmp.getAbsolutePath()+" -> "+output);
                        tmp.delete();
                    }
                    return false;
                }else {
                    Log.w("avif", "file not exists or lenght is not right:"+file.length()+"B,"+file.exists()+", raw file:"+new File(rawPath).length());
                }
            }
            /*if(success){
                copyExif(input,output);
            }*/
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return false;
    }






    //使用exiftool
    private static void copyExif(String input, String output) throws IOException {
        /*it.sephiroth.android.library.exif2.ExifInterface exifInterface = new it.sephiroth.android.library.exif2.ExifInterface();
        exifInterface.readExif(input, it.sephiroth.android.library.exif2.ExifInterface.Options.OPTION_ALL);
        exifInterface.writeExif(output);

        it.sephiroth.android.library.exif2.ExifInterface exifInterface2 = new it.sephiroth.android.library.exif2.ExifInterface();
        exifInterface2.readExif(output, it.sephiroth.android.library.exif2.ExifInterface.Options.OPTION_ALL);
        List<ExifTag> allTags = exifInterface2.getAllTags();
        for (ExifTag allTag : allTags) {
            Log.d("exif",allTag.toString());
        }*/


    }

    private static boolean execAvifEncoder(String rawPath, String output,int width,int height,int threads,int qua1,int qua2,int speed) {
        Log.d("Log","execAvifEncoder start ");
        String ev = context.getApplicationInfo().nativeLibraryDir;
        String[] envp = {"LD_LIBRARY_PATH=" + ev};

        String name = context.getApplicationInfo().nativeLibraryDir + "/libavif_example1.so "
                +  rawPath + " " + output
                + " " + width + " " + height +" " + threads + " " + qua1 +  " " + qua2 +  " " + speed;
//EXECUTING: /data/app/com.jackco.avifencoder-pfiGIymaFugjRSeVPy2yOg==/lib/arm64/libavif_example1.so
// /data/user/0/com.jackco.avifencoder/decoded.raw /data/user/0/com.jackco.avifencoder/b.avif 1080 2340 8 50 60 10

        String res = null;
        try {
            res = execCmd(name, envp);
            Log.d("Log","execAvifEncoder cmd end.... ");
            return true;

        } catch (Throwable e) {
            e.printStackTrace();
        }finally {
            new File(rawPath).delete();
        }
        return false;
    }

    private static String execCmd(String cmd, String[] envp) throws IOException {
        Log.i("EXECUTING", cmd.replace(".avif ",".avif\nparams: "));

        Process proc = null;
        proc = Runtime.getRuntime().exec(cmd, envp);
        java.io.InputStream is = proc.getInputStream();
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        String val = "";
        if (s.hasNext()) {
            val = s.next();
            Log.i("Result",val);
        }
        else {
            val = "";
        }
        return val;
    }

    private static File getCacheDir(){
        File cacheDir = new File(context.getExternalCacheDir(),"avifRawTmp");
        if(!cacheDir.exists()){
            cacheDir.mkdirs();
        }
        return cacheDir;
    }

    private static String writeRaw(Bitmap bitmap_f) throws IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("_dd_MM_yyyy_HH_mm_ss");
        Date date = new Date();
        File cacheDir =getCacheDir();
        File file = new File(cacheDir,formatter.format(date)+".raw");
        OutputStream fo = new FileOutputStream(file);

        int size = bitmap_f.getRowBytes() * bitmap_f.getHeight();

        byte[] byteArray;

        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        bitmap_f.copyPixelsToBuffer(byteBuffer);

        byteArray = byteBuffer.array();

        fo.write(byteArray);

        return file.getAbsolutePath();
    }

    private static Bitmap decodeBitmap(String input) {
        if(TextUtils.isEmpty(input)){
            return null;
        }
        File file = new File(input);
        Bitmap bitmap = null;
        int rotation = 0;
        ExifInterface exif = null;
        if(file.exists()){
            try {
                bitmap =  BitmapFactory.decodeFile(input);
            }catch (Throwable throwable){
                throwable.printStackTrace();
            }

            try {
                exif = new ExifInterface(input);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else {
            try {
                bitmap =  BitmapFactory.decodeStream(context.getContentResolver().openInputStream(Uri.parse(input)));
                exif = new ExifInterface(context.getContentResolver().openInputStream(Uri.parse(input)));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        if(bitmap != null && exif != null){
            rotation =  exif.getRotationDegrees();
           if(rotation != 0){
               //旋转图片
               Log.w("avif","旋转图片:"+rotation);
               int width = bitmap.getWidth();
               int height = bitmap.getHeight();
               Matrix matrix = new Matrix();
               matrix.setRotate(rotation);
               // 围绕原地进行旋转
               try {
                   Bitmap newBM = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                   if (newBM != null && !newBM.equals(bitmap)) {
                       bitmap = newBM;
                   }
               }catch (Throwable throwable){
                   throwable.printStackTrace();
               }

           }
        }
        return bitmap;
    }
}
