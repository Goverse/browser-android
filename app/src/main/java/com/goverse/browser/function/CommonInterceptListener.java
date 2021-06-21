package com.goverse.browser.function;

import android.content.Context;

import com.goverse.browser.process.PageInterceptListener;

public class CommonInterceptListener extends PageInterceptListener {

    public CommonInterceptListener(Context context) {
        super(context);
    }

    @Override
    public boolean onPageIntercept(String currentUrl, String loadingUrl) {
        return false;
    }
}
