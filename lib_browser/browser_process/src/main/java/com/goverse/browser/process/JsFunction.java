package com.goverse.browser.process;

import android.content.Context;

import com.goverse.browser.js.JsResult;

public abstract class JsFunction {

    public Context context;

    public JsFunction(Context context) {
        this.context = context;
    }

    public abstract JsResult onMethodInvoked(String callingUrl, String fromObj, String method, String param);
}
