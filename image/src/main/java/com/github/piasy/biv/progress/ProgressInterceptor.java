package com.github.piasy.biv.progress;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/5/3.
 */

public class ProgressInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        return response.newBuilder()
                .body(new OkHttpProgressResponseBody(request.url().toString(), response.body()))
                .build();
    }
}
