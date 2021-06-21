package com.goverse.browser.process;

import android.content.Context;

public abstract class JsActionListener {

    public Context context;
    public JsActionListener(Context context) {
        this.context = context;
    }

    public abstract void onJsAlert(String url, String message);

    public abstract void onJsConfirm(String url, String message);

    public abstract void onJsPrompt(String url, String message, String defaultValue);
}
