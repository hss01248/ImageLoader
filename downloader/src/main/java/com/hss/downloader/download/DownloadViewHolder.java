package com.hss.downloader.download;

import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hss.downloader.R;
import com.hss.downloader.databinding.ItemDownloadUiBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
            EventBus.getDefault().unregister(this);
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
           helper.setVisible(R.id.cb_selected,true);
       }else {
           helper.setGone(R.id.cb_selected,true);
       }

        if(info.totalLength>0){
            helper.setText(R.id.tv_size, ConvertUtils.byte2FitMemorySize(info.totalLength,2));
        }
        if(info.createTime > 0){
            helper.setText(R.id.tv_data, TimeUtils.date2String(new Date(info.totalLength),"yy-MM-dd"));
        }


        if(info.status == DownloadInfo.STATUS_DOWNLOADING){
            helper.setText(R.id.tv_status_msg,"下载中");
            helper.setVisible(R.id.progress_bar,true);
            helper.setText(R.id.tv_download_btn_desc,"点击暂停");
        }else if(info.status == DownloadInfo.STATUS_ORIGINAL){
            helper.setText(R.id.tv_status_msg,"等待中");
            helper.setVisible(R.id.progress_bar,false);
            helper.setText(R.id.tv_download_btn_desc,"点击暂停");
        }else if(info.status == DownloadInfo.STATUS_FAIL){
            helper.setText(R.id.tv_status_msg,"失败:"+info.errMsg);
            helper.setVisible(R.id.progress_bar,false);
            helper.setText(R.id.tv_download_btn_desc,"重试");
        }else if(info.status == DownloadInfo.STATUS_SUCCESS){
            helper.setText(R.id.tv_status_msg,"下载成功");
            helper.setVisible(R.id.progress_bar,false);
            helper.setText(R.id.tv_download_btn_desc,"");
        }
        if(info.totalLength>0){
            binding.progressBar.setMax((int) info.totalLength);
            if(info.currentOffset >=0){
                binding.progressBar.setProgress((int) info.currentOffset);
            }
        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DownloadInfo info) {
        ToastUtils.showShort(info.toString());
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
        showView(info);


    }
}
