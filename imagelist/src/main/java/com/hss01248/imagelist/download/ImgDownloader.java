package com.hss01248.imagelist.download;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.webkit.URLUtil;
import android.widget.TextView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hss01248.bigimageviewpager.LargeImageViewer;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.MyUtil;
import com.hss01248.image.interfaces.FileGetter;
import com.hss01248.image.utils.ThreadPoolFactory;
import com.hss01248.imagelist.R;
import com.hss01248.imagelist.download.db.DownloadInfoDao;
import com.hss01248.notifyutil.NotifyUtil;
import com.hss01248.notifyutil.builder.ProgressBuilder;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.enums.SidePattern;
import com.lzf.easyfloat.utils.DisplayUtils;


import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;



public class ImgDownloader {

   AtomicInteger count = new AtomicInteger(0);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger preDownloadedCount = new AtomicInteger(0);
    volatile int failCount  =0;
    volatile long fileSize = 0;
    volatile long originalFileSize = 0;
    Handler handler;
    String title;

    public interface IFileNamePrefix{
        String getFileNamePreffix(String url);
    }
    IFileNamePrefix namePrefix;

    public static void downladUrlsInDB(Context context,File dir){
        List<DownloadInfo> list = DownloadInfoUtil.getDao().queryBuilder()
                .whereOr(DownloadInfoDao.Properties.Status.eq(1),DownloadInfoDao.Properties.Status.eq(-1)).list();
        if(list ==null || list.isEmpty()){
            ToastUtils.showShort("no results");
            return;
        }
        List<String> urls = new ArrayList<>(list.size());
        for (DownloadInfo info : list) {
            urls.add(info.url);
        }
        new ImgDownloader().download(context, urls, dir, true, "downloading",null);

    }
    public void download(Context context, final List<String> urls,  File dir, boolean hideFolder, final String title,IFileNamePrefix fileNamePrefix){

        DownloadInfoUtil.context = context.getApplicationContext();
        this.title = title;
        namePrefix = fileNamePrefix;
        ToastUtils.showShort("开始下载"+urls.size()+"张图片");
        handler = new Handler(Looper.getMainLooper());

       //dir =  dealFolderCount(dir,hideFolder);
        if(!dir.exists()){
            dir.mkdirs();
        }
        if (hideFolder) {
            File hidden = new File(dir, ".nomedia");
            if (!hidden.exists()) {
                try {
                    hidden.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            File hidden = new File(dir, ".nomedia");
            if (hidden.exists()) {
                hidden.delete();
            }
        }


        final Map<String,Long> map =  new HashMap();
        showProgress(context,0,urls.size());
        File finalDir = dir;
        for ( String url0 : urls) {
            final String url = LargeImageViewer.getBigImageUrl(url0);
            if(TextUtils.isEmpty(url)){
                continue;
            }
            map.put(url,System.currentTimeMillis());

            //根据数据库记录,判断是否需要下载:
            DownloadInfo load = DownloadInfoUtil.getDao().load(url);
            Log.v("download","down info from db: "+load);
            if(load != null && load.getStatus() >1){
                preDownloadedCount.getAndIncrement();
                onOneFinished(context, urls, fileNamePrefix);
                continue;
            }
            try {
                if(load == null){
                    load = new DownloadInfo();
                    load.setStatus(1);
                    load.setUrl(url);
                    DownloadInfoUtil.getDao().insert(load);
                }else {
                    load.setStatus(1);
                    DownloadInfoUtil.getDao().update(load);
                }
            }catch (Throwable throwable){
                throwable.printStackTrace();
            }




            ImageLoader.getActualLoader().download(url, new FileGetter() {
                @Override
                public void onSuccess(final File file, int width, int height) {
                    ThreadPoolFactory.getNormalPool().execute(new Runnable() {
                        @Override
                        public void run() {
                            String name = getFileNamePrefix(url0,title,fileNamePrefix) + "-"+ URLUtil.guessFileName(url,"","image/*");
                            if(name.contains(File.pathSeparator)){
                                LogUtils.e("name contains /:"+name);
                                name = name.replaceAll(File.pathSeparator,"");
                            }

                            File file2 = new File(finalDir, "tmp-"+name);
                            File file3 = new File(finalDir, name);
                            FileUtils.copy(file, file3, new FileUtils.OnReplaceListener() {
                                @Override
                                public boolean onReplace(File srcFile, File destFile) {
                                    return true;
                                }
                            });
                            //MyLog.d("下载成功,url:"+url);
                            //然后压缩:
                            boolean compress = TurboCompressor.compressOringinal(file3.getAbsolutePath(), 80,file2.getAbsolutePath());
                            if(compress){
                                FileUtils.copy(file2, file3, new FileUtils.OnReplaceListener() {
                                    @Override
                                    public boolean onReplace(File srcFile, File destFile) {
                                        return true;
                                    }
                                });
                                originalFileSize += file.length();
                                fileSize += file3.length();
                            }else {
                                originalFileSize += file3.length();
                                fileSize += file3.length();
                            }
                            DownloadInfo info = new DownloadInfo();
                            info.url = url;
                            info.setStatus(2);
                            info.filePath = file3.getAbsolutePath();
                            try {
                                DownloadInfoUtil.getDao().update(info);
                            }catch (Throwable throwable){
                                throwable.printStackTrace();
                            }


                            file2.delete();
                            if(map.containsKey(url)){
                                long cost = System.currentTimeMillis() - map.get(url);
                                LogUtils.d("下载和压缩成功,耗时ms:"+cost+",url:"+url+"\n");//MyUtil.printExif(file3.getAbsolutePath())
                            }

                            successCount.getAndIncrement();
                            onOneFinished(context, urls, fileNamePrefix);

                        }
                    });

                }

                @Override
                public void onFail(Throwable e) {
                    if(e != null){
                        e.printStackTrace();
                    }
                    DownloadInfo info = new DownloadInfo();
                    info.url = url;
                    info.setStatus(-1);
                    try {
                        DownloadInfoUtil.getDao().update(info);
                    }catch (Throwable throwable){
                        throwable.printStackTrace();
                    }
                    failCount++;
                    onOneFinished(context, urls, fileNamePrefix);

                }
            });
        }
    }

    public static File dealFolderCount(File dir, boolean hideFolder) {
        if(!dir.exists()){
            dir.mkdirs();
        }
        SubFolderCount load = DownloadInfoUtil.getFolderCountDao().load(dir.getAbsolutePath());
        if(load == null){
            load = new SubFolderCount();
            load.dirPath = dir.getAbsolutePath();
            load.count = 1;
           File subDir =  createSubDir(dir,1,hideFolder);
           DownloadInfoUtil.getFolderCountDao().insert(load);
           return subDir;
        }

      File  subDir = createSubDir(dir,load.count,hideFolder);
        File[] list = subDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return !file.isDirectory();
            }
        });
        if(list != null && list.length > 3000){
            load.count = load.count +1;
            File  subDir2 = createSubDir(dir,load.count,hideFolder);
            DownloadInfoUtil.getFolderCountDao().update(load);
            return subDir2;
        }else {
            return subDir;
        }
    }

    private static File createSubDir(File dir, int count, boolean hideFolder) {
        dir = new File(dir,dir.getName()+count);
        if(!dir.exists()){
            dir.mkdirs();
        }
        if(hideFolder){
            File hidden = new File(dir,".nomedia");
            if(!hidden.exists()){
                try {
                    hidden.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return dir;
    }

    private void onOneFinished(Context context, List<String> urls, IFileNamePrefix fileNamePrefix) {
        count.getAndIncrement();
        handler.post(new Runnable() {
            @Override
            public void run() {
                showProgress(context, count.get(), urls.size());
            }
        });
        LogUtils.d("downloadcount:" + count);
        if (count.get() >= urls.size()) {
            String size = MyUtil.formatFileSize(fileSize);
            String sizeSaved = MyUtil.formatFileSize(originalFileSize - fileSize);
            String txt = "总大小:" + size + ",节省大小:" + sizeSaved + ",(成功数:" + successCount + ",失败数:" + failCount +", 之前已下载数:"+preDownloadedCount+ ")\n";
            ToastUtils.showLong("下载完成:" + txt + fileNamePrefix);
        }
    }

    private String getFileNamePrefix(String url, String title, IFileNamePrefix fileNamePrefix) {
        if(fileNamePrefix == null){
            return title;
        }
        return fileNamePrefix.getFileNamePreffix(url);
    }

    TextView textView;
    private void showProgress(Context context,int i, int size) {
       // ProgressBuilder progressBuilder = NotifyUtil.buildProgress(urls.hashCode(), android.R.mipmap.sym_def_app_icon, fileNamePrefix, 0, urls.size(), "下载进度:%d/%d");
       // progressBuilder.show();
        // progressBuilder.setProgressAndFormat(count.get(),urls.size(),false,"").show();

        String text = title+"  下载进度: "+i+"/"+size;

        if(textView == null){
             textView = new TextView(context);
            textView.setTextColor(Color.WHITE);
            textView.setPadding(20,20,20,20);
            textView.setBackground(new ColorDrawable(Color.parseColor("#66333333")));
            textView.setText(text);
            // https://github.com/princekin-f/EasyFloat
            EasyFloat.with(context)
                    .setTag(title)
                    .setLayout(textView)
                   // .setGravity(Gravity.BOTTOM)
                    .setDragEnable(true)
                    .setShowPattern(ShowPattern.FOREGROUND)
                    .show();
        }else {
            textView.setText(text);
        }
        if(i == size){
            EasyFloat.dismiss(title);
        }


       // https://github.com/princekin-f/EasyFloat
       /* EasyFloat.with(context)
                .setLayout(textView)
                // 设置浮窗xml布局文件/自定义View，并可设置详细信息
                .setLayout(R.layout.float_app) { }
        // 设置浮窗显示类型，默认只在当前Activity显示，可选一直显示、仅前台显示
    .setShowPattern(ShowPattern.ALL_TIME)
                // 设置吸附方式，共15种模式，详情参考SidePattern
                .setSidePattern(SidePattern.RESULT_HORIZONTAL)
                // 设置浮窗的标签，用于区分多个浮窗
                .setTag("testFloat")
                // 设置浮窗是否可拖拽
                .setDragEnable(true)
                // 浮窗是否包含EditText，默认不包含
                .hasEditText(false)
                // 设置浮窗固定坐标，ps：设置固定坐标，Gravity属性和offset属性将无效
               // .setLocation(100, 200)
                // 设置浮窗的对齐方式和坐标偏移量
                .setGravity(Gravity.END or Gravity.CENTER_VERTICAL, 0, 200)
                // 设置当布局大小变化后，整体view的位置对齐方式
                .setLayoutChangedGravity(Gravity.END)
                // 设置拖拽边界值
                .setBorder(100, 100，800，800)
                // 设置宽高是否充满父布局，直接在xml设置match_parent属性无效
                .setMatchParent(widthMatch = false, heightMatch = false)
                // 设置浮窗的出入动画，可自定义，实现相应接口即可（策略模式），无需动画直接设置为null
                .setAnimator(DefaultAnimator())
                // 设置系统浮窗的不需要显示的页面
                .setFilter(MainActivity::class.java, SecondActivity::class.java)
        // 设置系统浮窗的有效显示高度（不包含虚拟导航栏的高度），基本用不到，除非有虚拟导航栏适配问题
    .setDisplayHeight { context -> DisplayUtils.rejectedNavHeight(context) }
        // 浮窗的一些状态回调，如：创建结果、显示、隐藏、销毁、touchEvent、拖拽过程、拖拽结束。
        // ps：通过Kotlin DSL实现的回调，可以按需复写方法，用到哪个写哪个
    .registerCallback {
            createResult { isCreated, msg, view ->  }
            show {  }
            hide {  }
            dismiss {  }
            touchEvent { view, motionEvent ->  }
            drag { view, motionEvent ->  }
            dragEnd {  }
        }
        // 创建浮窗（这是关键哦😂）
    .show()*/
    }
}
