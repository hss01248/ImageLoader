package com.hss01248.frescoglidebridge;

import com.blankj.utilcode.util.Utils;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Okio;

/**
 * @Despciption todo
 * @Author hss
 * @Date 05/05/2022 10:19
 * @Version 1.0
 */
public class FrescoGlideInterceptor implements Interceptor {
    boolean ignoreParamsInUrl = false;
    public FrescoGlideInterceptor(boolean ignoreParamsInUrl) {
        this.ignoreParamsInUrl = ignoreParamsInUrl;
    }


    @Override
    public Response intercept(Chain chain) throws IOException {
        HttpUrl url = chain.request().url();
        String path = url.toString();
        final File resource;
        try {
            if(ignoreParamsInUrl){
                if(path.contains("?")){
                    path = path.substring(0,path.indexOf("?"));
                }
            }
            resource = Glide.with(Utils.getApp())
                    .asFile()
                    //url是否需要过滤?号后面的内容?
                    .load(path)
                    .submit().get();
            if (resource != null && resource.exists() && resource.isFile() && resource.length() > 0) {
                return new Response.Builder()
                        .code(200)
                        .message("ok")
                        .body(ResponseBody.create(MediaType.parse("image/*"),resource.length(), Okio.buffer(Okio.source(resource)))).build();
            }else {
                throw new ImageBridgeLoadException("file not exist or length is 0");
            }
        } catch (Throwable e) {
           throw new ImageBridgeLoadException(e);
        }

    }
}
