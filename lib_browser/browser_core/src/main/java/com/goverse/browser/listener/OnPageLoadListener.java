package com.goverse.browser.listener;

import android.graphics.Bitmap;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;

import com.goverse.browser.Browser;

/**
 * Interface definition for a callback to be invoked when load a
 * webview page.The format of the data depends on {@link WebViewClient}
 * and {@link WebChromeClient} callbacks.
 */
public interface OnPageLoadListener {

    void onPageStart(Browser browser, String url, Bitmap favicon);

    void onPageFinished(Browser browser, String url);

    void onLoadProgress(Browser browser, int newProgress);

    void onReceivedTitle(Browser browser, String title);

    void onReceivedError(Browser browser, String failUrl, int errorCode, String description);

    void onRefresh(Browser browser);
}