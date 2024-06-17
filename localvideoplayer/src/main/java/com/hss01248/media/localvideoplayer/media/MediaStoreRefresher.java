package com.hss01248.media.localvideoplayer.media;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.webkit.MimeTypeMap;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.io.File;
import java.io.IOException;


/**
 * Created by Administrator on 2017/1/17 0017.
 */

public class MediaStoreRefresher {


    public static void hideFile(File file){
        if(!file.exists()){
            return;
        }

        if(file.isDirectory()){
            try {
                new File(file,".nomedia").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        //文件,则隐藏文件所在目录
      File dir =   file.getParentFile();
        try {
            new File(dir,".nomedia").createNewFile();
        } catch (IOException e) {
           e.printStackTrace();
        }
    }


    public static  void refreshMediaCenter(Context activity, String filePath){
       /* File file  = new File(filePath);
        try {
            MediaStore.Images.Media.insertImage(activity.getContentResolver(),file.getAbsolutePath(), file.getName(), null);
        } catch (FileNotFoundException e) {
            XLogUtil.exception(e);
        }*/

        try {
            if (Build.VERSION.SDK_INT>19){
                String mineType =getMineType(filePath);

                //saveImageSendScanner(activity,new MyMediaScannerConnectionClient(filePath,mineType));

                MediaScannerConnection.scanFile(activity, new String[]{ filePath }, new String[]{mineType},
                        new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String s, Uri uri) {
                        LogUtils.d(s,uri);

                    }
                });
            }else {

                saveImageSendBroadcast(activity,filePath);
            }
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }


    }

    public static String getMineType(String filePath) {

        String type = "text/plain";
        String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
           type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }

    /**
     * 保存后用广播扫描，Android4.4以下使用这个方法
     * @author YOLANDA
     */
    private static void saveImageSendBroadcast(Context activity, String filePath){
        activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + filePath)));
    }

    /**
     * 保存后用MediaScanner扫描，通用的方法
     *
     */
    private static void saveImageSendScanner (Context context, MyMediaScannerConnectionClient scannerClient) {

        final MediaScannerConnection scanner = new MediaScannerConnection(context, scannerClient);
        scannerClient.setScanner(scanner);
        scanner.connect();
    }
    private   static class MyMediaScannerConnectionClient implements MediaScannerConnection.MediaScannerConnectionClient {

        private MediaScannerConnection mScanner;

        private String mScanPath;
        private String mimeType;

        public MyMediaScannerConnectionClient(String scanPath, String mimeType) {
            mScanPath = scanPath;
            this.mimeType = mimeType;
        }

        public void setScanner(MediaScannerConnection con) {
            mScanner = con;
        }

        @Override
        public void onMediaScannerConnected() {
            mScanner.scanFile(mScanPath, mimeType);
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            mScanner.disconnect();
        }
    }







    private static void showMessage(String s, Context context) {
        ToastUtils.showShort(s);
    }
}
