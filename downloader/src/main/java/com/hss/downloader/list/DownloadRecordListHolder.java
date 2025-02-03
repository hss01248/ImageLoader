package com.hss.downloader.list;

import android.content.Context;
import android.view.View;

import androidx.core.util.Pair;
import androidx.lifecycle.LifecycleOwner;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hss.downloader.MyDownloader;
import com.hss.downloader.R;
import com.hss.downloader.databinding.ContainerHistoryCollectBinding;
import com.hss.downloader.download.DownloadInfo;
import com.hss.utils.enhance.viewholder.ContainerActivity2;
import com.hss.utils.enhance.viewholder.mvvm.BaseViewHolder;
import com.hss.utils.enhance.viewholder.mvvm.ContainerViewHolderWithTitleBar;
import com.hss01248.fileoperation.FileOpenUtil;
import com.hss01248.fileoperation.FileTypeUtil2;
import com.hss01248.fullscreendialog.FullScreenDialogUtil;
import com.hss01248.iwidget.singlechoose.ISingleChooseItem;
import com.hss01248.media.metadata.MetaDataUtil;
import com.hss01248.refresh_loadmore.search.SearchViewHolder;
import com.hss01248.toast.MyToast;
import com.hss01248.viewholder_media.FileTreeViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
                onItemClick2(adapter, position);
            }
        });
        adapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                onItemLongClick2(adapter,view,position);
                return true;
            }
        });
    }

    public static void onItemLongClick2(BaseQuickAdapter adapter, View view, int position) {
        DownloadInfo info  = (DownloadInfo) adapter.getData().get(position);
        List<ISingleChooseItem<DownloadInfo>> menus = new ArrayList<>();
        ISingleChooseItem<DownloadInfo> item1 = new ISingleChooseItem<DownloadInfo>() {
            @Override
            public String text() {
                return "打开下载文件夹";
            }

            @Override
            public void onItemClicked(int position, DownloadInfo bean) {
                FileTreeViewHolder.viewDirInActivity(bean.dir);
            }
        };
        menus.add(item1);
        ISingleChooseItem<DownloadInfo> item2 = new ISingleChooseItem<DownloadInfo>() {
            @Override
            public String text() {
                return "查看详细信息";
            }

            @Override
            public void onItemClicked(int position, DownloadInfo info) {
                Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
                File file = new File(info.dir,info.name);
                Map map = new TreeMap();
                map.put("0db",info);
                String json = "";
                if(file.exists()){
                    Map<String, String> metaData = MetaDataUtil.getMetaData(file.getAbsolutePath());
                    map.put("meta",metaData);
                    json = gson.toJson(map);
                }else {
                    json = gson.toJson(info);
                }
                FullScreenDialogUtil.showText("详细信息",json);
            }
        };
        menus.add(item2);

        ISingleChooseItem<DownloadInfo> item3 = new ISingleChooseItem<DownloadInfo>() {
            @Override
            public String text() {
                return "重新下载";
            }

            @Override
            public void onItemClicked(int position, DownloadInfo bean) {
                if(bean.downloadSuccess() || bean.downloadFailed()){
                    File file = new File(bean.dir,bean.name);
                    if(file.exists()){
                        file.delete();
                    }
                    MyDownloader.startDownload(info);
                    MyToast.show("开始重新下载");
                }else {
                    MyToast.error("已经在下载队列中");
                }
            }
        };
        menus.add(item3);

        ISingleChooseItem.showAsMenu2(view,menus,info);
    }

    public static void onItemClick2(BaseQuickAdapter adapter, int position) {
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
}
