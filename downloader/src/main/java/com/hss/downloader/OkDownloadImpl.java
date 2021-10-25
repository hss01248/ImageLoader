package com.hss.downloader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.Utils;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.OkDownload;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.liulishuo.okdownload.core.connection.DownloadUrlConnection;
import com.liulishuo.okdownload.core.dispatcher.DownloadDispatcher;
import com.liulishuo.okdownload.core.listener.DownloadListener1;
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist;

import java.io.File;

public class OkDownloadImpl implements IDownload{

    public static void setMaxParallelRunningCount(int maxParallelRunningCount) {
        OkDownloadImpl.maxParallelRunningCount = maxParallelRunningCount;
        DownloadDispatcher.setMaxParallelRunningCount(maxParallelRunningCount);
    }

    public static int maxParallelRunningCount = 20;
    static volatile boolean init;

    public OkDownloadImpl() {
        init();
    }

    static void init(){
        if(init){
            return;
        }
        init = true;

        /*OkDownload.Builder builder = new OkDownload.Builder(Utils.getApp())
                .connectionFactory(OkDownloadProvider.)
                .downloadStore(downloadStore)
                .callbackDispatcher(callbackDispatcher)
                .downloadDispatcher(downloadDispatcher)
                .connectionFactory(connectionFactory)
                .outputStreamFactory(outputStreamFactory)
                .downloadStrategy(downloadStrategy)
                .processFileStrategy(processFileStrategy)
                .monitor(monitor);

        OkDownload.setSingletonInstance(builder.build());*/
        DownloadUrlConnection.Factory factory = new DownloadUrlConnection.Factory(
                new DownloadUrlConnection.Configuration()
                        .connectTimeout(20000)
                        .readTimeout(20000));
        OkDownload.Builder builder = new OkDownload.Builder(Utils.getApp())
                .connectionFactory(factory);
        OkDownload.setSingletonInstance(builder.build());

        DownloadDispatcher.setMaxParallelRunningCount(maxParallelRunningCount);
    }

    @Override
    public void download(String url, String filePath, IDownloadCallback callback) {
        File file = new File(filePath);
        DownloadTask task = new DownloadTask.Builder(url, file.getParentFile().getAbsolutePath(),file.getName())
                // the minimal interval millisecond for callback progress
                .setMinIntervalMillisCallbackProcess(100)
                .setConnectionCount(1)
                // do re-download even if the task has already been completed in the past.
                .setPassIfAlreadyCompleted(true)
                .build();

        task.enqueue(new DownloadListener1() {
            @Override
            public void taskStart(@NonNull DownloadTask task, @NonNull Listener1Assist.Listener1Model model) {
                callback.onStart(url);
            }

            @Override
            public void retry(@NonNull DownloadTask task, @NonNull ResumeFailedCause cause) {

            }

            @Override
            public void connected(@NonNull DownloadTask task, int blockCount, long currentOffset, long totalLength) {

            }

            @Override
            public void progress(@NonNull DownloadTask task, long currentOffset, long totalLength) {
                callback.progress(url,currentOffset,totalLength);
            }

            @Override
            public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause, @Nullable Exception realCause, @NonNull Listener1Assist.Listener1Model model) {
                if(cause.equals(EndCause.COMPLETED)){
                    callback.onSuccess(url);
                }else {
                    String des = cause.name();
                    if(realCause != null){
                        realCause.printStackTrace();
                        des = des+","+cause.getClass().getSimpleName()+" "+realCause.getMessage();
                    }
                  callback.onFail(url,des,realCause);
                }
            }
        });
    }
}
