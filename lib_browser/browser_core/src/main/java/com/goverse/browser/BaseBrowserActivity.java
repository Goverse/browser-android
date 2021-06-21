package com.goverse.browser;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Preconditions;

import com.goverse.browser.utils.BrowserUtils;

public abstract class BaseBrowserActivity extends AppCompatActivity {

    private Browser mBrowser;

    private BrowserView mBrowserView;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //防止首次截长图异常，需要在webview加载之前调用
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WebView.enableSlowWholeDocumentDraw();
        }
        onRequestWindowFeature();
        setContentView(R.layout.lib_browser_activity_layout);
        onCreateContentView(savedInstanceState);
        mBrowser = Preconditions.checkNotNull(onCreateBrowser(mBrowserView), "Browser == null");
        load(url());
    }


    public void onRequestWindowFeature() {
        BrowserUtils.adapterOppoStyle(this);
    }

    /**
     * Method used for setting fullscreen ui style, which must call
     * before setContentView.
     */
    protected void requestFullScreenLayout() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = getWindow().getDecorView();
            int option = 0;
            boolean isNightMode = Configuration.UI_MODE_NIGHT_YES == (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
            if (isNightMode) {
                option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
            }
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    protected void load(String url) {
        mBrowser.go(url);
    }

    public Browser getBrowser() {
        return mBrowser;
    }

    public BrowserView getBrowserView() { return mBrowserView; }

    public abstract Browser onCreateBrowser(BrowserView browserView);

    public void onCreateContentView(Bundle savedInstanceState) {
        mBrowserView = findViewById(R.id.browser_view);
    }

    public abstract String url();

    @Override
    public void onBackPressed() {
        runOnUiThread(() -> {
            if (mBrowser.canGoBack()) mBrowser.goBack();
            else finish();
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getBrowser().onActivityResult(requestCode, resultCode, data);
    }
}
