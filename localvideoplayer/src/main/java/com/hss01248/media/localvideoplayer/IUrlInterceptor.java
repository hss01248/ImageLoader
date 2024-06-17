package com.hss01248.media.localvideoplayer;

import java.util.Map;

public interface IUrlInterceptor {

    String getUrlWithAuth(String url);

    /**
     * String host = Uri.parse(url).getHost();
     *         if(HttpAuthInterceptor.getAuthMap().containsKey(host)){
     *             headers.put("Authorization",HttpAuthInterceptor.getAuthMap().get(host));
     *         }
     * @param url
     * @return
     */
    Map<String,String>  addHeaders(String url);
}
