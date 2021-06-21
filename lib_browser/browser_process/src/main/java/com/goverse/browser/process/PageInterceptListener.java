package com.goverse.browser.process;

import android.content.Context;

public abstract class PageInterceptListener {

    public Context context;

    public PageInterceptListener(Context context) {
        this.context = context;
    }

    /**
     *  Give a chance to take control when a URL is about to be loaded in the
     *  current WebView.
     * @param currentUrl the url which webView is showing.
     * @param loadingUrl the url to be loaded, may be the same as currentUrl when first loading.
     * @return {@code true} to cancel the current load, otherwise return {@code false}.
     */
    public abstract boolean onPageIntercept(String currentUrl, String loadingUrl);
}
