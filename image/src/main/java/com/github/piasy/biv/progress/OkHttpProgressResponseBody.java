package com.github.piasy.biv.progress;

import com.github.piasy.biv.event.ProgressEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by Administrator on 2017/5/3.
 */

public class OkHttpProgressResponseBody extends ResponseBody{
    private final String mUrl;
    private final ResponseBody mResponseBody;

    private BufferedSource mBufferedSource;

    OkHttpProgressResponseBody(String url, ResponseBody responseBody) {
        this.mUrl = url;
        this.mResponseBody = responseBody;
    }

    @Override
    public MediaType contentType() {
        return mResponseBody.contentType();
    }

    @Override
    public long contentLength() {
        return mResponseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (mBufferedSource == null) {
            mBufferedSource = Okio.buffer(source(mResponseBody.source()));
        }
        return mBufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            private long mTotalBytesRead = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                long fullLength = mResponseBody.contentLength();
                if(mTotalBytesRead ==0){
                    // EventBus.getDefault().post(new StartEvent(mUrl));
                    //observable.notifyObservers(new DownloadStateEvent(mUrl,DownloadStateEvent.STATE_START, (int) (mTotalBytesRead*100/fullLength)));
                }
                if (bytesRead == -1) { // this source is exhausted
                    mTotalBytesRead = fullLength;
                } else {
                    mTotalBytesRead += bytesRead;
                }
                //observable.notifyObservers(new DownloadStateEvent(mUrl,DownloadStateEvent.STATE_PROGRESS, (int) (mTotalBytesRead*100/fullLength)));

                //observable.notifyObservers(new DownloadStateEvent(mUrl,DownloadStateEvent.STATE_FINISH, (int) (mTotalBytesRead*100/fullLength)));
                EventBus.getDefault().post(new ProgressEvent((int) (mTotalBytesRead*100/fullLength),mTotalBytesRead== fullLength,mUrl));

                return bytesRead;
            }
        };
    }
}
