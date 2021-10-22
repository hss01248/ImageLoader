package com.hss.downloader;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hss.downloader.download.DownloadInfo;
import java.util.Date;
import java.util.List;

public class DownloadItemAdapter extends BaseQuickAdapter<DownloadInfo,BaseViewHolder> {
    public DownloadItemAdapter(int layoutResId, @Nullable List<DownloadInfo> data) {
        super(layoutResId, data);
    }

    public DownloadItemAdapter(@Nullable List<DownloadInfo> data) {
        super(data);
    }

    public DownloadItemAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, DownloadInfo info) {
       helper.setText(R.id.tv_name,info.name);

       helper.setGone(R.id.cb_selected,!info.isInSelectMode);

       helper.setChecked(R.id.cb_selected,info.selected);

       if(info.totalLength>0){
           helper.setText(R.id.tv_size, ConvertUtils.byte2FitMemorySize(info.totalLength));
       }
       if(info.createTime > 0){
           helper.setText(R.id.tv_size, TimeUtils.date2String(new Date(info.totalLength)));
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

    }
}
