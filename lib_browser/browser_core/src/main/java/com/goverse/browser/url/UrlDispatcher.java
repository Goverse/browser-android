package com.goverse.browser.url;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.WebView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *  This class is a component that is responsible for
 *  dispatch url schema when url redirect changed as
 *  {@Link WebViewClient#shouldOverrideUrlLoading} call.
 */
public class UrlDispatcher {

    private List<IUrlInterceptor> mJsHandlerList = new ArrayList<>();

    public UrlDispatcher(IUrlInterceptor... jsHandlerList) {
        if (jsHandlerList != null) {
            mJsHandlerList.addAll(Arrays.asList(jsHandlerList));
        }
        addJsHandler(new FileInterceptor());
        addJsHandler(new AppInterceptor());
    }

    public void addJsHandler(IUrlInterceptor jsHandler) {
        if (!mJsHandlerList.contains(jsHandler)) {
            mJsHandlerList.add(jsHandler);
        }
    }

    public void removeJsHandler(IUrlInterceptor jsHandler) {
        if (mJsHandlerList.contains(jsHandler)) {
            mJsHandlerList.remove(jsHandler);
        }
    }

    /**
     * dispatch url schema
     * @param webView webView
     * @param urlStr urlStr
     */
    public boolean dispatch(WebView webView, final String urlStr) {

        if (TextUtils.isEmpty(urlStr)) return false;
        if (mJsHandlerList != null) {
            for (IUrlInterceptor jsHandler : mJsHandlerList) {
                if (jsHandler.onHandle(webView, urlStr)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Open in browser to handle the url of pdf file.
     */
    private static class FileInterceptor implements IUrlInterceptor {

        @Override
        public boolean onHandle(WebView webView, String url) {
            if (url.endsWith(".pdf") || url.endsWith(".apk")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                webView.getContext().startActivity(intent);
                return true;
            }
            return false;
        }
    }

    // open third-party app, like alipay
    private static class AppInterceptor implements IUrlInterceptor {

        @Override
        public boolean onHandle(WebView webView, String url) {
            if (url.startsWith("alipays://")) {

                Intent intent = new Intent();
                intent.setData(Uri.parse(url));
                webView.getContext().startActivity(intent);
                return true;
            }

            return false;
        }
    }
}
