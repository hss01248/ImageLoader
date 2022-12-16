package com.konstantinschubert.writeinterceptingwebview;

import android.net.Uri;
import android.webkit.WebResourceRequest;
import java.util.Map;

public class WriteHandlingWebResourceRequest implements WebResourceRequest {
    final private Uri uri;
    final private WebResourceRequest originalWebResourceRequest;
    final private String requestBody;

    WriteHandlingWebResourceRequest(
            WebResourceRequest originalWebResourceRequest,
            String requestBody,
            Uri uri
    ){
        this.originalWebResourceRequest = originalWebResourceRequest;
        this.requestBody = requestBody;
        if (uri!=null) {
            this.uri = uri;
        }else{
            this.uri = originalWebResourceRequest.getUrl();
        }
    }

    @Override
    public Uri getUrl() {
        return this.uri;
    }

    @Override
    public boolean isForMainFrame() {
        return originalWebResourceRequest.isForMainFrame();
    }

    @Override
    public boolean isRedirect() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasGesture() {
        return originalWebResourceRequest.hasGesture();
    }

    @Override
    public String getMethod() {
        return originalWebResourceRequest.getMethod();
    }

    @Override
    public Map<String, String> getRequestHeaders() {
        return originalWebResourceRequest.getRequestHeaders();
    }
    public String getAjaxData(){
        return requestBody;
    }

    public boolean hasAjaxData(){
        return requestBody != null;
    }

}
