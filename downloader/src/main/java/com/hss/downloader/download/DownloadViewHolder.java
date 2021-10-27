package com.hss.downloader.download;

import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hss.downloader.MyDownloader;
import com.hss.downloader.R;
import com.hss.downloader.databinding.ItemDownloadUiBinding;
import com.hss.downloader.event.DialogCloseEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.Date;


public class DownloadViewHolder extends BaseViewHolder {

    public DownloadInfo info;
    DownloadViewHolder helper;
    ItemDownloadUiBinding binding;
    public DownloadViewHolder(View view) {
        super(view);
        helper = this;
        try {
            binding = ItemDownloadUiBinding.bind(itemView);
            EventBus.getDefault().register(this);
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }
    }

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
            helper.setText(R.id.tv_data, TimeUtils.date2String(new Date(info.createTime),"yyyy-MM-dd"));
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
            helper.setText(R.id.tv_status_msg,"等待中");
            helper.setVisible(R.id.progress_bar,false);
            helper.setText(R.id.tv_download_btn_desc,"暂停");
            binding.llRight.setVisibility(View.GONE);
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
                binding.tvStatusMsg.setText("下载压缩完成");
            }
        }


        binding.llRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(info.status  == DownloadInfo.STATUS_FAIL){
                    MyDownloader.startDownload(info);
                }else if(info.status == DownloadInfo.STATUS_DOWNLOADING || info.status == DownloadInfo.STATUS_ORIGINAL){

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
            if(info.currentOffset>0 && info.currentOffset != info.totalLength){
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
