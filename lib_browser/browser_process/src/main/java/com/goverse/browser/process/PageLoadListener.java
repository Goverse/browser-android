package com.goverse.browser.process;

import android.content.Context;

public abstract class PageLoadListener {

    public Context context;

    public PageLoadListener(Context context) {
        this.context = context;
    }

    public abstract void onPagePreLoad(String url);

    public abstract void onPageStart(String url);

    public abstract void onPageFinished(String url);

    public abstract void onLoadProgress(int newProgress);

    public abstract void onReceivedError(String failUrl, int errorCode, String description);

}
