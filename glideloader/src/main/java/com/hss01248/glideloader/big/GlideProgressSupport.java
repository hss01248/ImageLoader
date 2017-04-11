/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Piasy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * credit: https://gist.github.com/TWiStErRob/08d5807e396740e52c90
 */

package com.hss01248.glideloader.big;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.github.piasy.biv.event.ProgressEvent;
import com.github.piasy.biv.event.StartEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by Piasy{github.com/Piasy} on 12/11/2016.
 */

public class GlideProgressSupport {
    private static Interceptor createInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Response response = chain.proceed(request);
                return response.newBuilder()
                        .body(new OkHttpProgressResponseBody(request.url().toString(), response.body()))
                        .build();
            }
        };
    }

    public static void init(Glide glide, OkHttpClient okHttpClient) {
        OkHttpClient.Builder builder;
        if (okHttpClient != null) {
            builder = okHttpClient.newBuilder();
        } else {
            builder = new OkHttpClient.Builder();
        }
        builder.addNetworkInterceptor(createInterceptor());
        glide.register(GlideUrl.class, InputStream.class,
                new OkHttpUrlLoader.Factory(builder.build()));
    }

    /*public static void forget(String url) {
        DispatchingProgressListener.forget(url);
    }

    public static void expect(String url, ProgressListener listener) {
        DispatchingProgressListener.expect(url, listener);
    }*/

    public interface ProgressListener {
        void onDownloadStart(String url);

        void onProgress(String url, long bytesRead, long contentLength);

        void onDownloadFinish(String url);

        void onDownloadFail(String url);
    }

    /*public abstract class ResponseProgressListener {
        public String url;
       public abstract void update(String url, long bytesRead, long contentLength);
    }*/

     /*static class DispatchingProgressListener implements ProgressListener {
        private static final Map<String, ProgressListener> LISTENERS = new HashMap<>();
        private static final Map<String, Integer> PROGRESSES = new HashMap<>();

        static void forget(String url) {
            if(LISTENERS.containsKey(url))
            LISTENERS.remove(url);
            if(PROGRESSES.containsKey(url))
            PROGRESSES.remove(url);
        }

        static void expect(String url, ProgressListener listener) {
            LISTENERS.put(url, listener);
        }

        @Override
        public void update(HttpUrl url, final long bytesRead, final long contentLength) {
            String key = url.toString();
            final ProgressListener listener = LISTENERS.get(key);
            if (listener == null) {
                return;
            }

            Integer lastProgress = PROGRESSES.get(key);
            if (lastProgress == null) {
                // ensure `onStart` is called before `onProgress` and `onFinish`
                listener.onDownloadStart();
            }
            if (contentLength <= bytesRead) {
                listener.onDownloadFinish();
                forget(key);
                return;
            }
            int progress = (int) ((float) bytesRead / contentLength * 100);
            if (lastProgress == null || progress != lastProgress) {
                PROGRESSES.put(key, progress);
                listener.onProgress(progress);
            }
        }
    }*/

    private static class OkHttpProgressResponseBody extends ResponseBody {
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
                        EventBus.getDefault().post(new StartEvent(mUrl));
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
}
