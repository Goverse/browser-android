package com.goverse.browser.main;

import android.content.Context;
import android.os.Bundle;
import com.goverse.browser.BaseBrowserActivity;
import com.goverse.browser.Browser;
import com.goverse.browser.BrowserInitializer;
import com.goverse.browser.BrowserView;
import com.goverse.browser.js.IJsExecutor;
import com.goverse.browser.js.JsResult;

public class BrowserMainActivity extends BaseBrowserActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BrowserInitializer.initMain(getApplicationContext());
    }

    @Override
    public Browser onCreateBrowser(BrowserView browserView) {
        return Browser.with(this)
                .setView(browserView)
                .theme(Browser.Theme.FULL_SCREEN)
                .supportDarkMode(true)
                .enableJavaScript(true)
                .adoptScreen(true)
                .addJavaScriptExecutor(new IJsExecutor() {
                    @Override
                    public JsResult onMethodCall(Context context, String method, String param) {
                        return JsResult.NOT_INVOKED;
                    }
                })
                .build();
    }

    @Override
    public String url() {
        return "https://www.baidu.com";
    }

}