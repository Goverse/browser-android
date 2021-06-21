package com.goverse.browser.url;

import android.webkit.WebView;

import com.goverse.browser.Browser;

/**
 * Interface defination used to handle js method invocation by
 * using url schema when method shouldOverrideUrlLoading called on {@link android.webkit.WebViewClient}.
 * Another way to handle is {@link Browser.Builder#addJavaScriptInterfaces(Object... objects)}.
 */
public interface IUrlInterceptor {
    boolean onHandle(WebView webView, String url);
}
