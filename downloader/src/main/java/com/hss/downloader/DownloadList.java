package com.hss.downloader;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.hss.downloader.databinding.DownloadListViewBinding;
import com.hss.downloader.download.DownloadInfo;
import com.hss.downloader.download.DownloadInfoUtil;
import com.hss.downloader.download.DownloadResultEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class DownloadList {

    public DownloadList() {
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DownloadResultEvent info) {
        progress++;

        binding.pbTotal.setProgress(progress);
        if(!info.success){
            failCount ++;
        }
        String text = "("+(progress - failCount) +",f-"+(failCount)+")/"+total;
        binding.tvTotalProgress.setText(text);
    }
    DownloadListViewBinding binding;
    long total;
    int progress;
    int failCount;
    public  void showList(Context context, List<DownloadInfo> result){


        if(result != null && !result.isEmpty()){
             binding = DownloadListViewBinding.inflate(LayoutInflater.from(context),new FrameLayout(context),false);
            DownloadItemAdapter adapter = new DownloadItemAdapter(R.layout.item_download_ui);
            binding.recycler.setAdapter(adapter);
            binding.recycler.setLayoutManager(new LinearLayoutManager(context));
            //从数据库加载
            adapter.addData(result);
            initClick(adapter,result);

            total = result.size();
            binding.tvTotalProgress.setText("0/"+total);
            binding.pbTotal.setMax((int) total);
            showViewAsDialog(context,binding.getRoot());
            ThreadUtils.executeByCpu(new ThreadUtils.Task<Integer>() {
                @Override
                public Integer doInBackground() throws Throwable {
                    int count = 0;
                    for (DownloadInfo downloadInfo : result) {
                        if(downloadInfo.status == DownloadInfo.STATUS_SUCCESS){
                            count++;
                        }
                    }
                    return count;
                }

                @Override
                public void onSuccess(Integer result) {
                    binding.tvTotalProgress.setText(result+ "/"+total);
                    binding.pbTotal.setProgress(result);
                    progress = result;
                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onFail(Throwable t) {

                }
            });
        }else {
           /* ProgressDialog dialog = new ProgressDialog(ActivityUtils.getTopActivity());
            dialog.setMessage("查询数据库...");
            dialog.show();*/
            ThreadUtils.executeByIo(new ThreadUtils.Task<List<DownloadInfo>>() {
                @Override
                public List<DownloadInfo> doInBackground() throws Throwable {
                    //todo 分批加载
                    return DownloadInfoUtil.getDao().loadAll();
                }

                @Override
                public void onSuccess(List<DownloadInfo> result) {
                   // dialog.dismiss();
                    if(result.isEmpty()){
                        return;
                    }
                    showList(ActivityUtils.getTopActivity(),result);
                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onFail(Throwable t) {

                }
            });
        }



    }

    private void initClick(DownloadItemAdapter adapter, List<DownloadInfo> result) {
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

            }
        });
        adapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                return true;
            }
        });
    }


    public  void showViewAsDialog(Context context, View view) {
        //View view = init.init(MyUtil.getActivityFromContext(context));
        Dialog dialog = new Dialog(view.getContext());
        dialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));//背景颜色一定要有，看自己需求
        dialog.getWindow().setLayout(view.getResources().getDisplayMetrics().widthPixels, ScreenUtils.getAppScreenHeight());//宽高最大- BarUtils.getStatusBarHeight()
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                try {
                    EventBus.getDefault().unregister(DownloadList.this);
                }catch (Throwable throwable){
                    throwable.printStackTrace();
                }
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                try {
                    EventBus.getDefault().unregister(DownloadList.this);
                }catch (Throwable throwable){
                    throwable.printStackTrace();
                }
            }
        });
        /*ImageView ivClose = view.findViewById(R.id.iv_back);
        if (ivClose != null) {
            ivClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }*/
    }
}
