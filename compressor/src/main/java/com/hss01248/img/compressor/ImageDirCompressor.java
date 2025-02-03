package com.hss01248.img.compressor;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;

import java.io.File;
import java.io.FilenameFilter;

public class ImageDirCompressor {

/*    public static void compressDirWithUI(String dirPath, DirCallback callback){
        File dir = new File(dirPath);
        if(dir.exists()){
            callback.onFailed(new Throwable("dir not exist \n"+dirPath));
            return;
        }
        long startTime = System.currentTimeMillis();
        final  long[] totalOriginal = {0};
        final long[] totalCompressed = {0};
        ThreadUtils.executeBySingle(new ThreadUtils.Task<Object>() {
            @Override
            public Object doInBackground() throws Throwable {
                File[] files = dir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return ImageCompressor.isImagesToCompress(name);
                    }
                });
                if(files == null || files.length ==0){
                    throw new Throwable("no image need to compress");
                }
                for (File file : files) {
                    long eachStart = System.currentTimeMillis();
                    long originalSize = file.length();
                    File compressed = ImageCompressor.compressToAvif(file.getAbsolutePath(), false, true);
                    if(compressed.getAbsolutePath().equals(file.getAbsolutePath()) && originalSize == compressed.length()){
                        //压缩失败或跳过
                        LogUtils.w("压缩跳过或失败 ",file.getAbsolutePath());
                    }else {
                        totalOriginal[0] += originalSize;
                        totalCompressed[0] += compressed.length();
                        callback.onEach(file,compressed,System.currentTimeMillis() - eachStart,originalSize,compressed.length());

                    }
                }
                return files;
            }

            @Override
            public void onSuccess(Object result) {
                callback.onComplete(System.currentTimeMillis() - startTime,totalOriginal[0],totalCompressed[0]);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {
                callback.onFailed(t);

            }
        });
    }*/



    public static void compressDir(String dirPath, DirCallback callback){
        File dir = new File(dirPath);
        if(!dir.exists()){
            callback.onFailed(new Throwable("dir not exist "+dirPath));
            return;
        }
        long startTime = System.currentTimeMillis();
        final  long[] totalOriginal = {0};
        final long[] totalCompressed = {0};
        ThreadUtils.executeBySingle(new ThreadUtils.Task<Object>() {
            @Override
            public Object doInBackground() throws Throwable {
                final int[] count = {0};
                File[] files = dir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        count[0]++;
                        return ImageCompressor.isImagesToCompress(name,ImageCompressor.targetJpgQuality);
                    }
                });
                if(files == null || files.length ==0){
                    throw new Throwable("no image need to compress");
                }
                if(callback.showConfirmDialog(count[0],files.length,new Runnable() {
                    @Override
                    public void run() {
                        ThreadUtils.executeBySingle(new ThreadUtils.Task<Object>() {
                            @Override
                            public Object doInBackground() throws Throwable {
                                doCompress(files, totalOriginal, totalCompressed, callback);
                                runOnMain(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onComplete(System.currentTimeMillis() - startTime,totalOriginal[0],totalCompressed[0]);
                                    }
                                });
                                return null;
                            }

                            @Override
                            public void onSuccess(Object result) {

                            }

                            @Override
                            public void onCancel() {

                            }

                            @Override
                            public void onFail(Throwable t) {

                            }
                        });

                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailed(new Throwable("canceled"));
                    }
                })){
                    //无需操作
                }else {
                    doCompress(files, totalOriginal, totalCompressed, callback);
                    runOnMain(new Runnable() {
                        @Override
                        public void run() {
                            callback.onComplete(System.currentTimeMillis() - startTime,totalOriginal[0],totalCompressed[0]);
                        }
                    });
                }

                return files;
            }

            @Override
            public void onSuccess(Object result) {
                //callback.onComplete(System.currentTimeMillis() - startTime,totalOriginal[0],totalCompressed[0]);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {
                callback.onFailed(t);

            }
        });
    }

    static void runOnMain(Runnable runnable){
        ThreadUtils.getMainHandler().post(runnable);
    }

    private static void doCompress(File[] files, long[] totalOriginal, long[] totalCompressed, DirCallback callback) {
        for (File file : files) {
            long eachStart = System.currentTimeMillis();
            long originalSize = file.length();
            File compressed = ImageCompressor.compress(file.getAbsolutePath(), false, true);
            if(compressed.getAbsolutePath().equals(file.getAbsolutePath()) && originalSize == compressed.length()){
                //压缩失败或跳过
                LogUtils.i("压缩跳过或失败 ",file.getAbsolutePath());
            }else {
                totalOriginal[0] += originalSize;
                totalCompressed[0] += compressed.length();
                runOnMain(new Runnable() {
                    @Override
                    public void run() {
                        callback.onEach(file,compressed,System.currentTimeMillis() - eachStart,originalSize,compressed.length());
                    }
                });


            }
        }
    }

    public interface DirCallback{



       default boolean showConfirmDialog(int totalCount,int toCompressCount,Runnable ok,Runnable cancel){
            return false;
        }

        void onEach(File original,File compressed,long cost,long origianlSize,long sizeAfterCompressed);

        void onComplete(long totalCost,long totalOrigianlSize,long totalSizeAfterCompressed);

        default void onFailed(Throwable throwable){
            throwable.printStackTrace();
        }
    }


}
