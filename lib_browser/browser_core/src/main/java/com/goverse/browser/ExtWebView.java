package com.goverse.browser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Looper;
import android.util.AttributeSet;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import androidx.annotation.Nullable;

import com.goverse.browser.utils.BrowserUtils;

public class ExtWebView extends WebView {

    public ExtWebView(Context context) {
        super(context);
        initWebView(context);
    }

    public ExtWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initWebView(context);
    }

    public ExtWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initWebView(context);
    }

    private void initWebView(Context context) {
        hideOverScrollFade();
        darkModeCompat();
        //disalbe webview longClickEvent, avoid to show select text
        setLongClickable(false);
        setOnLongClickListener(v -> true);
    }

    /**
     * 适配暗色模式，闪白屏问题
     */
    private void darkModeCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (BrowserUtils.isNightMode(getContext())) {
                if (isForceDarkAllowed()) {
                    setBackgroundColor(Color.TRANSPARENT);
                } else {
                    setBackgroundColor(Color.BLACK);
                    setAlpha(0F);
                }
            }
        }
    }

    /**
     * 去掉默认滑动到顶部或者底部出现的阴影
     */
    private void hideOverScrollFade() {
        setOverScrollMode(OVER_SCROLL_NEVER);
        setHorizontalFadingEdgeEnabled(false);
        setVerticalFadingEdgeEnabled(false);
    }

    @SuppressLint("JavascriptInterface")
    @Override
    public void addJavascriptInterface(Object object, String name) {

        runOnUiThread(() -> super.addJavascriptInterface(object, name));
    }


    @SuppressLint("JavascriptInterface")
    public void removeJavascriptInterface(String interfaceName) {
        runOnUiThread(() -> super.removeJavascriptInterface(interfaceName));
    }

    @Override
    public void evaluateJavascript(String script, @Nullable ValueCallback<String> resultCallback) {
        runOnUiThread(() -> super.evaluateJavascript(script, resultCallback));
    }

    @Override
    public void loadUrl(String url) {
        runOnUiThread(() -> super.loadUrl(url));
    }

    @Override
    public void loadData(String data, @Nullable String mimeType, @Nullable String encoding) {

        runOnUiThread(() -> super.loadData(data, mimeType, encoding));
    }

    @Override
    public void loadDataWithBaseURL(@Nullable String baseUrl, String data, @Nullable String mimeType, @Nullable String encoding, @Nullable String historyUrl) {

        runOnUiThread(() -> super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl));
    }

    @Override
    public void stopLoading() {
        runOnUiThread(() -> super.stopLoading());
    }

    @Override
    public void reload() {

        runOnUiThread(() -> super.reload());
    }

    @Override
    public void goBack() {
        runOnUiThread(() -> super.goBack());
    }

    private boolean isOnMainThread() {
        return Thread.currentThread() == Looper.getMainLooper().getThread();
    }

    private final void runOnUiThread(Runnable action) {
        if (!isOnMainThread()) {
            post(action);
        } else {
            action.run();
        }
    }
}
