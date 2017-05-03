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
import com.github.piasy.biv.progress.ProgressInterceptor;

import java.io.InputStream;

import okhttp3.OkHttpClient;

/**
 * Created by Piasy{github.com/Piasy} on 12/11/2016.
 */

public class GlideProgressSupport {

    public static void init(Glide glide, OkHttpClient okHttpClient) {
        OkHttpClient.Builder builder;
        if (okHttpClient != null) {
            builder = okHttpClient.newBuilder();
        } else {
            builder = new OkHttpClient.Builder();
        }
        builder.addNetworkInterceptor(new ProgressInterceptor());
        glide.register(GlideUrl.class, InputStream.class,
                new OkHttpUrlLoader.Factory(builder.build()));
    }

}
