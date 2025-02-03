package com.hss.downloader;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.hss.downloader.databinding.DownloadListViewBinding;
import com.hss.downloader.download.CompressEvent;
import com.hss.downloader.download.DownloadInfo;
import com.hss.downloader.download.DownloadInfoUtil;
import com.hss.downloader.event.DialogCloseEvent;
import com.hss.downloader.event.DownloadResultEvent;
import com.hss.downloader.list.DownloadItemAdapter;
import com.hss.downloader.list.DownloadRecordListHolder;
import com.hss.utils.enhance.foregroundservice.CommonProgressService;
import com.hss01248.fullscreendialog.FullScreenDialogUtil;
import com.noober.menu.FloatMenu;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class DownloadList {

    public DownloadList() {
        EventBus.getDefault().register(this);
    }

    long compressdTotal;
    long origianlTotal;
    long fileTotal;
    int compressSuccessCount;
    int compressTotalCount;
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CompressEvent event) {

        fileTotal += event.origianl;
       // compressdTotal += event.after;
        compressTotalCount++;
        if(event.success){
            compressSuccessCount++;
            origianlTotal += event.origianl;
            compressdTotal += event.after;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("压缩结果:")
                .append("成功/总数:")
                .append(compressSuccessCount)
                .append("/")
                .append(compressTotalCount)
                .append(",压缩效果:")
                .append(ConvertUtils.byte2FitMemorySize(compressdTotal,1))
                .append("/")
                .append(ConvertUtils.byte2FitMemorySize(origianlTotal,1))
                .append("/")
                .append(ConvertUtils.byte2FitMemorySize(fileTotal,1))
                .append(",压缩率:")
                .append(origianlTotal == 0 ? 0 : compressdTotal*100/origianlTotal)
                .append("%");
        binding.tvCompress.setText(sb.toString());


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DownloadResultEvent info) {
        progress++;

        binding.pbTotal.setProgress(progress);
        if(!info.success){
            if(successCount + failCount < total){
                failCount ++;
            }
        }else {
            successCount++;
            if(successCount + failCount >= total){
                failCount --;
            }
        }

        String text = "("+(successCount) +",f-"+(failCount)+")/"+total+",cost:"+ DateUtils.formatElapsedTime((System.currentTimeMillis() - timeStart)/1000);
        binding.tvTotalProgress.setText(text);
        CommonProgressService.updateProgress(progress, (int) total,"图片下载中","图片下载: "+progress+"/"+total,0);
    }
    DownloadListViewBinding binding;
    long total;
    int progress;
    int successCount;
    int failCount;
    long timeStart;
    public  void showList(Context context, List<DownloadInfo> result){


        if(result != null && !result.isEmpty()){
             binding = DownloadListViewBinding.inflate(LayoutInflater.from(context),new FrameLayout(context),false);
            DownloadItemAdapter adapter = new DownloadItemAdapter(R.layout.item_download_ui);
            binding.recycler.setAdapter(adapter);
            binding.recycler.setLayoutManager(new LinearLayoutManager(context));
            //从数据库加载
            datas = result;
            adapter.addData(result);
            initClick(adapter,result);
            timeStart = System.currentTimeMillis();
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

    public static void setLargeImagesViewer(ILargeImagesViewer largeImagesViewer) {
        DownloadList.largeImagesViewer = largeImagesViewer;
    }

    static ILargeImagesViewer largeImagesViewer;
    List<DownloadInfo> datas;
    private void initClick(DownloadItemAdapter adapter, List<DownloadInfo> result) {
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                DownloadRecordListHolder.onItemClick2(adapter,position);
            }
        });
        adapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                DownloadRecordListHolder.onItemLongClick2(adapter,view,position);
                return true;
            }
        });
        binding.tvRetryall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFailPop(view);

            }
        });
    }


    private void showFailPop(View view) {
        final FloatMenu floatMenu = new FloatMenu(view.getContext(), view);
        //String hide = DbUtil.showHidden ? "隐藏文件夹":"显示隐藏的文件夹";
        String[] desc = new String[2];
        desc[0] = "重试"  ;
        desc[1] ="在数据库里删除所有失败条目";

        floatMenu.items(desc);
        floatMenu.setOnItemClickListener(new FloatMenu.OnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                if(position == 0){
                   dealFailData(0);

                }else if(position == 1){
                    dealFailData(1);
                }
            }
        });

        floatMenu.showAsDropDown(view);
    }

    private void dealFailData(int idx) {
        ThreadUtils.executeByCpu(new ThreadUtils.Task<List<DownloadInfo>>() {
            @Override
            public List<DownloadInfo> doInBackground() throws Throwable {
                List<DownloadInfo> infos = new ArrayList<>();
                for (DownloadInfo downloadInfo : datas) {
                    if(downloadInfo.status == DownloadInfo.STATUS_FAIL){
                        infos.add(downloadInfo);
                    }
                }
                if(idx ==0){
                    for (DownloadInfo downloadInfo : infos) {
                        MyDownloader.startDownload(downloadInfo);
                    }
                }else {
                    DownloadInfoUtil.getDao().deleteInTx(infos);
                }
                return infos;
            }

            @Override
            public void onSuccess(List<DownloadInfo> result) {
                String msg = "";
                if(idx ==0){
                    msg = "已批量重试";
                }else {
                    msg = "已在数据库里删除当前所有失败条目";
                }
                ToastUtils.showShort(msg);

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {

            }
        });
    }

    private static int calHeight() {
        if(!BarUtils.isNavBarVisible(ActivityUtils.getTopActivity())){
            return  ScreenUtils.getScreenHeight()- BarUtils.getStatusBarHeight();
        }
        return ScreenUtils.getScreenHeight()- BarUtils.getNavBarHeight();
    }
    public  void showViewAsDialog(Context context, View view) {
        //View view = init.init(MyUtil.getActivityFromContext(context));

        Dialog dialog =  FullScreenDialogUtil.showFullScreen(view);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
       /* Dialog dialog = new Dialog(view.getContext());
        dialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));//背景颜色一定要有，看自己需求
        int height = calHeight();
        dialog.getWindow().setLayout(view.getResources().getDisplayMetrics().widthPixels, height);//宽高最大- BarUtils.getStatusBarHeight()
        dialog.show();*/
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                try {
                    EventBus.getDefault().post(new DialogCloseEvent());
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
                    EventBus.getDefault().post(new DialogCloseEvent());
                    EventBus.getDefault().unregister(DownloadList.this);
                }catch (Throwable throwable){
                    throwable.printStackTrace();
                }
            }
        });
    }
}
