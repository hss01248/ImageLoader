package com.konstantinschubert.writeinterceptingwebview;

import java.io.IOException;
import org.jsoup.Jsoup;

import android.content.Context;
import android.webkit.JavascriptInterface;


class AjaxInterceptJavascriptInterface {

    private static String interceptHeader = null;
    private WriteHandlingWebViewClient mWebViewClient = null;

    public AjaxInterceptJavascriptInterface(WriteHandlingWebViewClient webViewClient) {
        mWebViewClient = webViewClient;
    }

    public static String enableIntercept(Context context, byte[] data) throws IOException {
        if (interceptHeader == null) {
            interceptHeader = new String(
                    Utils.consumeInputStream(context.getAssets().open("interceptheader.html"))
            );
        }

        org.jsoup.nodes.Document doc = Jsoup.parse(new String(data));
        doc.outputSettings().prettyPrint(true);

        // Prefix every script to capture submits
        // Make sure our interception is the first element in the
        // header
        org.jsoup.select.Elements element = doc.getElementsByTag("head");
        if (element.size() > 0) {
            element.get(0).prepend(interceptHeader);
        }

        String pageContents = doc.toString();
        return pageContents;
    }

    @JavascriptInterface
    public void customAjax(final String ID, final String body) {
        mWebViewClient.addAjaxRequest(ID, body);
    }


}
