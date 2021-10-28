package com.hss01248.img.compressor;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.io.File;

public abstract class UiForDirCompress implements ImageDirCompressor.DirCallback {

   /* ImageDirCompressor.DirCallback callback;
    public UiForDirCompress(ImageDirCompressor.DirCallback callback) {
        this.callback = callback;
    }*/

    @Override
    public boolean showConfirmDialog(int totalCount, int toCompressCount, Runnable ok, Runnable cancel) {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UiForDirCompress.this.totalCount = totalCount;
                StringBuilder sb = new StringBuilder();
                sb.append("当前文件夹中共有")
                        .append(totalCount)
                        .append("个文件,需要压缩的图片有")
                        .append(toCompressCount)
                        .append("个\n")
                        .append("是否执行压缩?");
                String msg = sb.toString();
                AlertDialog dialog = new AlertDialog.Builder(ActivityUtils.getTopActivity())
                        .setTitle("图片压缩")
                        .setMessage(msg)
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startTime = System.currentTimeMillis();
                                showProgressDialog(toCompressCount);
                                ok.run();
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                cancel.run();
                            }
                        }).create();
                dialog.show();
            }
        });

        return true;
    }
    AlertDialog dialog = null;
    ProgressBar progressBar;
    private void showProgressDialog(int toCompressCount) {
         progressBar = new ProgressBar(ActivityUtils.getTopActivity(),null,android.R.attr.progressBarStyleHorizontal);
        //progressBar.setScrollBarStyle(View.SCROLL_AXIS_HORIZONTAL);

        progressBar.setMax(toCompressCount);
         dialog = new AlertDialog.Builder(ActivityUtils.getTopActivity())
         .setMessage("开始压缩")
                 .setCancelable(false)
                 .setView(progressBar)
                 .setPositiveButton("查看文件夹", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         if(dir != null){
                             showDirImags(dir.getAbsolutePath());
                         }

                     }
                 })
                 .create();
         dialog.setCanceledOnTouchOutside(false);

        dialog.show();

    }

    long totalOriginalSize = 1;
    long totalSizeAfterCompressed = 1;
    int totalCount;
    int currentCount;
    int totalCost;
    long startTime;
    File dir;
    @Override
    public void onEach(File original, File compressed, long cost, long origianlSize, long sizeAfterCompressed) {
        dir = original.getParentFile();
        if(origianlSize ==0){
            origianlSize =1;
        }
        totalOriginalSize += origianlSize;
        totalSizeAfterCompressed += sizeAfterCompressed;
        currentCount++;
        totalCost +=cost;
        StringBuilder sb = new StringBuilder();
        sb.append("压缩进度:\n")
                .append("文件个数:")
                .append(currentCount)
                .append("/")
                .append(totalCount)
                .append("\n当前耗时/总耗时:")
                .append(DateUtils.formatElapsedTime(cost))
                .append("/")
                .append(DateUtils.formatElapsedTime(System.currentTimeMillis() - startTime))
                .append("\n当前压缩率/总压缩率:")
                .append(100*sizeAfterCompressed/origianlSize)
                .append("%/")
                .append(100*totalSizeAfterCompressed/totalOriginalSize) .append("%");
        if(dialog != null){
            dialog.setMessage(sb.toString());
            progressBar.setProgress(currentCount);
        }
    }

    @Override
    public void onComplete(long totalCost, long totalOrigianlSize, long totalSizeAfterCompressed) {
        ToastUtils.showShort("压缩完成!");
    }

    @Override
    public void onFailed(Throwable throwable) {
        LogUtils.w(throwable);
        ToastUtils.showShort(throwable.getMessage());

    }

    public abstract void showDirImags(String dir);


}
