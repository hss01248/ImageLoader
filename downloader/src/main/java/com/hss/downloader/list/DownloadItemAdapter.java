package com.hss.downloader.list;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.hss.downloader.download.DownloadInfo;

import java.util.List;

public class DownloadItemAdapter extends BaseQuickAdapter<DownloadInfo, DownloadViewHolder> {
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
    protected void convert(@NonNull DownloadViewHolder helper, DownloadInfo info) {
        helper.info = info;
        helper.datas = this.getData();
        helper.showView(info);
    }

    @Override
    public void onViewRecycled(@NonNull DownloadViewHolder holder) {
        super.onViewRecycled(holder);
        holder.onViewRecycled();
    }
}