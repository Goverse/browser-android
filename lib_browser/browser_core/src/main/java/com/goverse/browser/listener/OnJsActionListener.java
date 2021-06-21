package com.goverse.browser.listener;

import android.webkit.JsPromptResult;
import android.webkit.JsResult;

import com.goverse.browser.Browser;

public interface OnJsActionListener {
    void onJsAlert(Browser browser, String url, String message, JsResult result);

    void onJsConfirm(Browser browser, String url, String message, JsResult result);

    void onJsPrompt(Browser browser, String url, String message, String defaultValue, JsPromptResult result);
}