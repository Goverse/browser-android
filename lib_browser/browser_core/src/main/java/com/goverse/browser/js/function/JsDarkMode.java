package com.goverse.browser.js.function;

import android.content.Context;
import android.content.res.Configuration;
import android.webkit.JavascriptInterface;

import com.goverse.browser.js.JsNameSpace;

@JsNameSpace(namespace = "AppDarkMode")
public class JsDarkMode {

    public final String TAG = this.getClass().getSimpleName();

    @JavascriptInterface
    public boolean isDarkMode(Context context) {
        boolean nightMode = isNightMode(context);
        return nightMode;
    }

    public static boolean isNightMode(Context context) {
        if (context == null) {
            return false;
        }
        Configuration configuration = context.getResources().getConfiguration();
        int currentNightMode = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return Configuration.UI_MODE_NIGHT_YES == currentNightMode;
    }

}
