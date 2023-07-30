package com.hss01248.glide.aop.net;

import com.blankj.utilcode.util.LogUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;

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

   public static byte dataToAdd = 0x66;
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request()) ;
       // Log.i("glide","request url--> "+chain.request().url());
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

        //inputStream.mark(2);
        //mark(10)，那么在read()10个以内的字符时，reset（）操作后可以重新读取已经读出的数据，如果已经读取的数据超过10个，那reset()操作后，就不能正确读取以前的数据了

        //LogUtils.w("文件长度-->"+body.contentLength()+", inputStream.available():"
        //        +inputStream.available()+", "+chain.request().url()+"\nmarkSupported-"+inputStream.markSupported());
        //markSupported-false
        int read = inputStream.read();
        if(read == dataToAdd){
            //隐藏文件,那么要去掉第一个字符
            LogUtils.w("隐藏文件,那么要去掉第一个字符-->inputStream.available():"+inputStream.available()+", "+chain.request().url());
            return response.newBuilder()
                    .body(ResponseBody.create(body.contentType(),
                            inputStream.available(),
                            Okio.buffer(Okio.source(inputStream)))
                    )
                    .build();
        }else {

            //LogUtils.i("非隐藏文件,正常使用-->,inputStream.reset():"+inputStream.available()+", "+chain.request().url());
            //inputStream.reset();//这里会导致死循环. 如何处理?

            byte[] bytes = new byte[]{(byte) read};
            ByteArrayInputStream inputStream1 = new ByteArrayInputStream(bytes);
            SequenceInputStream newInputStream = new SequenceInputStream(inputStream1,inputStream);
            //newInputStream.available():1
            //LogUtils.i("非隐藏文件,正常使用2-->,newInputStream.available():"+newInputStream.available()+", "+chain.request().url());
            return response.newBuilder()
                    .body(ResponseBody.create(body.contentType(),
                            inputStream.available()+1,
                            Okio.buffer(Okio.source(newInputStream)))
                    )
                    .build();
        }


    }
}
