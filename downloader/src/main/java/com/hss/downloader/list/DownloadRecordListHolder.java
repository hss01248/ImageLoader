package com.hss.downloader.list;

import android.content.Context;
import android.view.View;

import androidx.core.util.Pair;
import androidx.lifecycle.LifecycleOwner;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.hss.downloader.DownloadItemAdapter;
import com.hss.downloader.R;
import com.hss.downloader.databinding.ContainerHistoryCollectBinding;
import com.hss.downloader.download.DownloadInfo;
import com.hss.utils.enhance.viewholder.ContainerActivity2;
import com.hss.utils.enhance.viewholder.mvvm.BaseViewHolder;
import com.hss.utils.enhance.viewholder.mvvm.ContainerViewHolderWithTitleBar;
import com.hss01248.fileoperation.FileOpenUtil;
import com.hss01248.fileoperation.FileTypeUtil2;
import com.hss01248.refresh_loadmore.search.SearchViewHolder;
import com.hss01248.toast.MyToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * @Despciption todo
 * @Author hss
 * @Date 10/01/2023 17:39
 * @Version 1.0
 */
public class DownloadRecordListHolder extends BaseViewHolder<ContainerHistoryCollectBinding, String> {


    public DownloadRecordListHolder(Context context) {
        super(context);
    }

    public static void show(){
        ContainerActivity2.start( new Consumer<Pair<ContainerActivity2, ContainerViewHolderWithTitleBar>>() {
            @Override
            public void accept(Pair<ContainerActivity2, ContainerViewHolderWithTitleBar> pair) throws Exception {
                DownloadRecordListHolder holder1 = new DownloadRecordListHolder(pair.first);
                pair.second.getBinding().rlContainer.addView(holder1.getRootView());
                holder1.init("");
                pair.second.getBinding().realTitleBar.setVisibility(View.GONE);

            }
        });

    }

    int type = 0;

    SearchViewHolder<DownloadInfo> holder;


    @Override
    protected void initDataAndEventInternal(LifecycleOwner lifecycleOwner, String bean) {
        holder = new SearchViewHolder<DownloadInfo>(binding.getRoot().getContext());
        binding.getRoot().addView(holder.binding.getRoot());
        //binding.getRoot().setPadding(0, BarUtils.getStatusBarHeight(),0,0);

        holder.getLoadMoreRecycleViewHolder().getDto().pageSize = 500;
        holder.getLoadMoreRecycleViewHolder().initRecyclerViewDefault();

        holder.getLoadMoreRecycleViewHolder().setEmptyMsg("下载记录为空");
        holder.getLoadMoreRecycleViewHolder().setLoadDataImpl(new LoadDataByHistoryDb());
        BaseQuickAdapter adapter = new DownloadItemAdapter(R.layout.item_download_ui);
        holder.getLoadMoreRecycleViewHolder().setAdapter(adapter);
        holder.getLoadMoreRecycleViewHolder().assignDataAndEvent(new HashMap<>());

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                List list = adapter.getData();
                DownloadInfo info  = (DownloadInfo) adapter.getData().get(position);
                int type = FileTypeUtil2.getTypeIntByFileName(info.getFilePath());
                List<String> paths = new ArrayList<>(list.size());
                if(FileTypeUtil2.isImageOrVideo(info.getFilePath())){
                    for (Object o : list) {
                        DownloadInfo info2  = (DownloadInfo) o;
                        if(FileTypeUtil2.getTypeIntByFileName(info2.getFilePath())==type){
                            paths.add(info2.getFilePath());
                        }
                    }
                }
                MyToast.debug(info.getFilePath());
                FileOpenUtil.open(info.getFilePath(),paths);
            }
        });
    }
}
