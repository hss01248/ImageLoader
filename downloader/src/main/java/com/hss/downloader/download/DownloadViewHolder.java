package com.hss.downloader.download;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hss.downloader.FileOpenUtil;
import com.hss.downloader.MyDownloader;
import com.hss.downloader.R;
import com.hss.downloader.databinding.ItemDownloadUiBinding;
import com.hss.downloader.event.DialogCloseEvent;
import com.hss01248.toast.MyToast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class DownloadViewHolder extends BaseViewHolder {

    public DownloadInfo info;
    DownloadViewHolder helper;
    ItemDownloadUiBinding binding;
    public List<DownloadInfo> datas;
    public DownloadViewHolder(View view) {
        super(view);
        helper = this;
        try {
            binding = ItemDownloadUiBinding.bind(itemView);
            EventBus.getDefault().register(this);
            LifecycleOwner lifecycleOwnerFromObj = LifecycleObjectUtil2.getLifecycleOwnerFromObj(view.getContext());
            if(lifecycleOwnerFromObj !=null){
                lifecycleOwnerFromObj.getLifecycle().addObserver(new DefaultLifecycleObserver() {
                    @Override
                    public void onDestroy(@NonNull LifecycleOwner owner) {
                        //DefaultLifecycleObserver.super.onDestroy(owner);
                        EventBus.getDefault().unregister(DownloadViewHolder.this);
                        LogUtils.w("EventBus.getDefault().unregister : "+DownloadViewHolder.this);
                    }
                });
            }else {
                LogUtils.w("getLifecycleOwnerFromObj is null : "+view.getContext());
            }
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }
    }

//内存泄漏
    public void onViewRecycled() {
        try {
           // EventBus.getDefault().unregister(this);
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }
    }


    public void showView(DownloadInfo info){

        try {
            binding.tvName.setText(info.name);
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }

        //helper.setText(R.id.tv_name,info.name);

       if(info.isInSelectMode){
           binding.cbSelected.setVisibility(View.VISIBLE);
       }else {
           binding.cbSelected.setVisibility(View.GONE);
       }

        if(info.totalLength>0){
            helper.setText(R.id.tv_size, ConvertUtils.byte2FitMemorySize(info.totalLength,2));
        }else {
            binding.tvSize.setText("");
        }
        if(info.createTime > 0){
            helper.setText(R.id.tv_data, TimeUtils.date2String(new Date(info.createTime),"yyyy-MM-dd mm:ss"));
        }else {
            binding.tvData.setText("");
        }


        if(info.status == DownloadInfo.STATUS_DOWNLOADING){
            helper.setText(R.id.tv_status_msg,"下载中");
            helper.setVisible(R.id.progress_bar,true);
            helper.setText(R.id.tv_download_btn_desc,"暂停");
            binding.llRight.setVisibility(View.VISIBLE);
            if(info.totalLength>0){
                binding.progressBar.setMax((int) info.totalLength);
                if(info.currentOffset >=0){
                    binding.progressBar.setProgress((int) info.currentOffset);
                }
            }
            binding.ivIcon.setImageResource(R.color.design_dark_default_color_primary);
        }else if(info.status == DownloadInfo.STATUS_ORIGINAL){
            helper.setText(R.id.tv_status_msg,"未开始或等待中");
            helper.setVisible(R.id.progress_bar,false);
            helper.setText(R.id.tv_download_btn_desc,"开始");
            binding.llRight.setVisibility(View.VISIBLE);
            binding.ivIcon.setImageResource(R.color.design_dark_default_color_primary);

            if(info.totalLength>0 && info.currentOffset>0){
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.progressBar.setMax((int) info.totalLength);
                binding.progressBar.setProgress((int) info.currentOffset);
            }else {
                binding.progressBar.setVisibility(View.GONE);
            }

        }else if(info.status == DownloadInfo.STATUS_FAIL){
            helper.setText(R.id.tv_status_msg,"失败:"+info.errMsg);
            helper.setVisible(R.id.progress_bar,false);
            helper.setText(R.id.tv_download_btn_desc,"重试");
            binding.llRight.setVisibility(View.GONE);
            binding.ivIcon.setImageResource(R.color.design_default_color_error);
            if(info.totalLength>0 && info.currentOffset>0){
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.progressBar.setMax((int) info.totalLength);
                binding.progressBar.setProgress((int) info.currentOffset);
            }else {
                binding.progressBar.setVisibility(View.GONE);
            }

        }else if(info.status == DownloadInfo.STATUS_SUCCESS){
            helper.setText(R.id.tv_status_msg,"下载成功");
            helper.setText(R.id.tv_download_btn_desc,"");
            binding.llRight.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.GONE);
            Glide.with(binding.ivIcon)
                    .load(new File(info.dir+"/"+info.name))
                    .into(binding.ivIcon);
            if(info.isCompressing){
                binding.tvStatusMsg.setText("压缩中");
            }else {
                binding.tvStatusMsg.setText("下载完成");
            }
        }


        binding.llRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(info.status  == DownloadInfo.STATUS_FAIL){
                    MyDownloader.startDownload(info);
                }else if(info.status == DownloadInfo.STATUS_ORIGINAL){
                    MyDownloader.startDownload(info);
                }else if(info.status == DownloadInfo.STATUS_DOWNLOADING){
                    MyDownloader.stopDownload(info);
                }

            }
        });
        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(info.status == DownloadInfo.STATUS_SUCCESS){
                    if(datas !=null){

                        String mimeType = FileOpenUtil.getMineType2(info.getFilePath());
                        boolean image = mimeType.contains("image");
                        boolean video = mimeType.contains("video");
                        if(image || video){
                            List<String> paths = new ArrayList<>();
                            for (DownloadInfo data : datas) {
                                if(data.status != DownloadInfo.STATUS_FAIL){
                                    //类型要一致
                                    if(image){
                                        if(FileOpenUtil.getMineType2(data.getFilePath()).contains("image")){
                                            if(data.status == DownloadInfo.STATUS_SUCCESS){
                                                paths.add(data.getFilePath());
                                            }else {
                                                paths.add(data.getUrl());
                                            }
                                        }
                                    }else if(video){
                                        if(FileOpenUtil.getMineType2(data.getFilePath()).contains("video")){
                                            if(data.status == DownloadInfo.STATUS_SUCCESS){
                                                paths.add(data.getFilePath());
                                            }else {
                                                paths.add(data.getUrl());
                                            }
                                        }
                                    }
                                }
                            }
                            FileOpenUtil.paths = paths;
                        }
                    }
                    FileOpenUtil.open(info.getFilePath());
                }else if(info.status  == DownloadInfo.STATUS_FAIL){
                    //MyDownloader.startDownload(info);
                    MyToast.show("文件还下载失败,请重试");
                }else if(info.status == DownloadInfo.STATUS_ORIGINAL){
                    MyToast.show("文件还没开始下载,现在开始下载");
                    MyDownloader.startDownload(info);
                }else if(info.status == DownloadInfo.STATUS_DOWNLOADING){
                    //MyDownloader.stopDownload(info);
                    MyToast.show("文件还在下载中");
                }
            }
        });


    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DownloadInfo info) {
        //ToastUtils.showShort(info.toString());
        if(this.info == null){
            return;
        }
        if(!info.url.equals(this.info.url)){
            return;
        }
        if(this.info != info){
            this.info.status = info.status;
            this.info.totalLength = info.totalLength;
            this.info.errMsg = info.errMsg;
            this.info.currentOffset = info.currentOffset;
        }
        if(info.status == DownloadInfo.STATUS_DOWNLOADING){
            if(info.currentOffset>0 && info.currentOffset != info.totalLength && info.totalLength>0){
                //binding.progressBar.setVisibility(View.VISIBLE);
                binding.progressBar.setMax((int) info.totalLength);
                binding.progressBar.setProgress((int) info.currentOffset);
                if(binding.tvSize.getText().length() ==0){
                    binding.tvSize.setText(ConvertUtils.byte2FitMemorySize(info.totalLength,2));
                }
                return;
            }
        }
        showView(info);


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DialogCloseEvent info) {
        try {
             EventBus.getDefault().unregister(this);
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }
    }
}
