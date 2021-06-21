package com.goverse.browser.listener;

import android.view.View;
import android.webkit.WebChromeClient;

public interface OnCustomViewListener {

    void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback);
    void onHideCustomView();
}
