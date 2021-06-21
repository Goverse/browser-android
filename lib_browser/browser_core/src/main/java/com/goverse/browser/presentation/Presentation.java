package com.goverse.browser.presentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import androidx.annotation.LayoutRes;

import com.goverse.browser.Browser;
import com.goverse.browser.listener.OnCustomViewListener;
import com.goverse.browser.listener.OnPageLoadListener;

public abstract class Presentation implements OnPageLoadListener, OnCustomViewListener {

    private View view;

    private Context context;

    protected boolean isFixedTitle;

    public Presentation(Context context, @LayoutRes int layoutId, String title) {
        this.context = context;
        this.view = View.inflate(context, layoutId, null);
        isFixedTitle = TextUtils.isEmpty(title) ? false : true;
    }

    public Presentation(Context context, View view, String title) {
        this.context = context;
        this.view = view;
    }

    public View getView() {
        return this.view;
    }

    public Context getContext() {
        return context;
    }

    public View findViewById(int id) {
        return this.view.findViewById(id);
    }

    public abstract void setTitle(String title);

    public abstract WebView getWebView();

    @Override
    public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
    }

    @Override
    public void onHideCustomView() {
    }

    @Override
    public void onPageStart(Browser browser, String url, Bitmap favicon) {
    }

    @Override
    public void onPageFinished(Browser browser, String url) {
    }

    @Override
    public void onLoadProgress(Browser browser, int newProgress) {
    }

    @Override
    public void onReceivedError(Browser browser, String failUrl, int errorCode, String description) {
    }

    @Override
    public void onReceivedTitle(Browser browser, String title) {
    }

    @Override
    public void onRefresh(Browser browser) {
    }
}
