package com.hss.downloader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hss01248.imagelist.download.DownloadInfo;

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
    protected void convert(@NonNull BaseViewHolder helper, DownloadInfo item) {



    }
}
