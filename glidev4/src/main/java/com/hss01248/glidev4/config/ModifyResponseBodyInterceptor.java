package com.hss01248.glidev4.config;

import android.util.Log;

import com.blankj.utilcode.util.LogUtils;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Okio;

/**
 * @Despciption todo
 * @Author hss
 * @Date 27/07/2023 16:17
 * @Version 1.0
 */
public class ModifyResponseBodyInterceptor implements Interceptor {

    static byte dataToAdd = 0x66;
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request()) ;
        Log.i("glide","request url--> "+chain.request().url());
        if(!response.isSuccessful()){
            return response;
        }
        if(response.body() == null){
            return response;
        }
        if(response.body().contentLength()<= 0){
            return response;
        }
        ResponseBody body = response.body();
        InputStream inputStream = body.byteStream();
        if(inputStream == null || inputStream.available()<=0){
            return response;
        }

        inputStream.mark(1);

        LogUtils.w("文件长度-->"+body.contentLength()+", inputStream.available():"
                +inputStream.available()+", "+chain.request().url());
        int read = inputStream.read();
        if(read == dataToAdd){
            //隐藏文件,那么要去掉第一个字符
            LogUtils.w("隐藏文件,那么要去掉第一个字符-->inputStream.available():"+inputStream.available()+", "+chain.request().url());
        }else {

            LogUtils.i("非隐藏文件,正常使用-->,inputStream.reset():"+inputStream.available()+", "+chain.request().url());
            inputStream.reset();//这里会导致死循环. 如何处理?
        }
        return response.newBuilder()
                .body(ResponseBody.create(body.contentType(),inputStream.available(),
                        Okio.buffer(Okio.source(inputStream)))).build();
    }
}
