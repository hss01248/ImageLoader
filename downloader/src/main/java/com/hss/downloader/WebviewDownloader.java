package com.hss.downloader;

import android.text.TextUtils;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;

import com.blankj.utilcode.util.LogUtils;
import com.hss.downloader.api.DownloadApi;
import com.hss.downloader.callback.DefaultSilentDownloadCallback;

/**
 * @author: Administrator
 * @date: 2023/2/4
 * @desc: //todo
 */
public class WebviewDownloader implements DownloadListener {
    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        LogUtils.i(url,userAgent,contentDisposition,mimetype,contentLength);
        String name = IDownload.getFileName(url,contentDisposition,mimetype);
        //contentDisposition:  attachment; filename="redditsave.com_p0dqho9nqh891.gif"
        if(!TextUtils.isEmpty(contentDisposition)){
            if(contentDisposition.contains(";")){
                String[] split = contentDisposition.split(";");
                for (String s : split) {
                    if(!TextUtils.isEmpty(s)){
                        s = s.trim();
                        if(s.contains("=")){
                            String[] split1 = s.split("=");
                            if(split1.length == 2){
                                if("filename".equals(split1[0])){
                                    String str = split1[1];
                                    str = str.replace("\"","");
                                    str = str.replace("\"","");
                                    str = str.replace("\"","");
                                    str = str.replace("\"","");
                                    name = str;
                                    break ;
                                }
                            }
                        }
                    }
                }
            }
        }

        DownloadApi.create(url)
                .setName(name)
                .callback(new DefaultSilentDownloadCallback());



    }
}
