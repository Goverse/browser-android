package com.goverse.browser;

import android.content.Context;
import android.os.Build;

import com.goverse.browser.function.CommonInterceptListener;
import com.goverse.browser.function.CommonJsFunction;
import com.goverse.browser.js.IJsExecutor;
import com.goverse.browser.main.executors.SystemExecutor;
import com.goverse.browser.process.BpBrowser;
import com.goverse.browser.utils.BrowserUtils;

import java.util.ArrayList;
import java.util.List;

public class BrowserInitializer {

    public static void initMain(Context context) {
        String userAgent = "Manufacturer/" + Build.MANUFACTURER.toLowerCase() + " " + "packageName/" + context.getPackageName();
        final List<Class<? extends IJsExecutor>> DefaultJsExecutorList = new ArrayList<>();
        DefaultJsExecutorList.add(SystemExecutor.class);
        Browser.init(context, userAgent, "https://", DefaultJsExecutorList);
    }

    public static void initSubProcess(Context context) {
        String userAgent = BrowserUtils.getUserAgent(context.getApplicationContext());
        Browser.init(context.getApplicationContext(), userAgent, "https://", null);
        BpBrowser.getInstance().init(context.getApplicationContext(), userAgent);
        BpBrowser.getInstance().registerCommon(new CommonJsFunction(context.getApplicationContext()), new CommonInterceptListener(context.getApplicationContext()), null,  null);
    }
}
