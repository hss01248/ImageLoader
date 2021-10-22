package com.hss.downloader;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.hss.downloader.databinding.DownloadListViewBinding;
import com.hss.downloader.download.DownloadInfo;
import com.hss.downloader.download.DownloadInfoUtil;

import java.util.List;

public class DownloadList {

    public static void showList(Context context, List<DownloadInfo> result){

        DownloadListViewBinding binding = DownloadListViewBinding.inflate(LayoutInflater.from(context),new FrameLayout(context),false);
        DownloadItemAdapter adapter = new DownloadItemAdapter(R.layout.item_download_ui);
        binding.recycler.setAdapter(adapter);
        binding.recycler.setLayoutManager(new LinearLayoutManager(context));
        if(result != null && !result.isEmpty()){
            //从数据库加载
            adapter.addData(result);
            binding.tvTotalProgress.setText("0/"+result.size());
            showViewAsDialog(context,binding.getRoot());
        }else {
            ProgressDialog dialog = new ProgressDialog(ActivityUtils.getTopActivity());
            dialog.setMessage("查询数据库...");
            dialog.show();
            ThreadUtils.executeByIo(new ThreadUtils.Task<List<DownloadInfo>>() {
                @Override
                public List<DownloadInfo> doInBackground() throws Throwable {
                    //todo 分批加载
                    return DownloadInfoUtil.getDao().loadAll();
                }

                @Override
                public void onSuccess(List<DownloadInfo> result) {
                    dialog.dismiss();
                    DownloadList.showList(ActivityUtils.getTopActivity(),result);
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


    public static void showViewAsDialog(Context context, View view) {
        //View view = init.init(MyUtil.getActivityFromContext(context));
        Dialog dialog = new Dialog(view.getContext());
        dialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));//背景颜色一定要有，看自己需求
        dialog.getWindow().setLayout(view.getResources().getDisplayMetrics().widthPixels, ScreenUtils.getAppScreenHeight());//宽高最大- BarUtils.getStatusBarHeight()
        dialog.show();
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
