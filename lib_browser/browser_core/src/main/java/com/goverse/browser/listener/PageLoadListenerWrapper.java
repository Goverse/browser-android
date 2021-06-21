package com.goverse.browser.listener;

import android.graphics.Bitmap;

import com.goverse.browser.Browser;

import java.util.ArrayList;
import java.util.List;

public class PageLoadListenerWrapper implements OnPageLoadListener {

    private List<OnPageLoadListener> onPageLoadListenerList = new ArrayList<>();

    public void addOnPageLoadListener(OnPageLoadListener onPageLoadListener) {
        if (!onPageLoadListenerList.contains(onPageLoadListener)) {
            onPageLoadListenerList.add(onPageLoadListener);
        }
    }

    public void removeOnPageLoadListener(OnPageLoadListener onPageLoadListener) {
        if (onPageLoadListenerList.contains(onPageLoadListener)) {
            onPageLoadListenerList.remove(onPageLoadListener);
        }
    }

    @Override
    public void onPageStart(Browser browser, String url, Bitmap favicon) {
        for (OnPageLoadListener onPageLoadListenerList : onPageLoadListenerList) {
            onPageLoadListenerList.onPageStart(browser, url, favicon);
        }
    }

    @Override
    public void onPageFinished(Browser browser, String url) {
        for (OnPageLoadListener onPageLoadListenerList : onPageLoadListenerList) {
            onPageLoadListenerList.onPageFinished(browser, url);
        }
    }

    @Override
    public void onLoadProgress(Browser browser, int newProgress) {
        for (OnPageLoadListener onPageLoadListenerList : onPageLoadListenerList) {
            onPageLoadListenerList.onLoadProgress(browser, newProgress);
        }
    }

    @Override
    public void onReceivedTitle(Browser browser, String title) {
        for (OnPageLoadListener onPageLoadListenerList : onPageLoadListenerList) {
            onPageLoadListenerList.onReceivedTitle(browser, title);
        }
    }

    @Override
    public void onReceivedError(Browser browser, String failUrl, int errorCode, String description) {
        for (OnPageLoadListener onPageLoadListenerList : onPageLoadListenerList) {
            onPageLoadListenerList.onReceivedError(browser, failUrl, errorCode, description);
        }
    }

    @Override
    public void onRefresh(Browser browser) {
        for (OnPageLoadListener onPageLoadListenerList : onPageLoadListenerList) {
            onPageLoadListenerList.onRefresh(browser);
        }
    }
}
