package com.goverse.browser.presentation;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.goverse.browser.Browser;
import com.goverse.browser.BrowserProgressBar;
import com.goverse.browser.R;
import com.goverse.browser.utils.BrowserUtils;
import com.goverse.browser.utils.WebViewSoftKeyboardHeightFixer;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.webkit.WebViewClient.ERROR_UNKNOWN;

public class ScreenPresentation extends Presentation {
    private final String TAG = "UltimatePresentation";
    private WebView webView;
    private Toolbar toolbar;
    private FrameLayout errorView;
    private BrowserProgressBar progressBar;
    private RelativeLayout layoutRoot;
    private WebChromeClient.CustomViewCallback callback;
    private View customView;

    public ScreenPresentation(Context context, String title) {
        super(context, R.layout.lib_browser_theme_screen, title);
        webView = (WebView) findViewById(R.id.ext_webview);
        webView.setAlpha(0f);
        toolbar = (Toolbar) findViewById(R.id.lib_base_toolbar);
        errorView = (FrameLayout) findViewById(R.id.error_view);
        progressBar = (BrowserProgressBar) findViewById(R.id.progressbar);
        progressBar.setVisibility(INVISIBLE);
        layoutRoot = (RelativeLayout) findViewById(R.id.layout_root);
        initToolbar(toolbar, title, true);
        ViewGroup rootView = ((AppCompatActivity) getContext()).getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.setPadding(0, BrowserUtils.getStatusBarHeight(getContext()), 0, 0);
    }

    private void initToolbar(Toolbar toolbar, String title, boolean isHomeAsUpEnabled) {
        toolbar.setTitle(TextUtils.isEmpty(title) ? " " : title);
        ((AppCompatActivity)getContext()).setSupportActionBar(toolbar);
        ActionBar supportActionBar = ((AppCompatActivity) getContext()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(isHomeAsUpEnabled);
        }

        toolbar.setNavigationOnClickListener(v -> {
            if (getWebView().canGoBack()) getWebView().goBack();
            else ((AppCompatActivity)getContext()).finish();
        });
    }

    @Override
    public WebView getWebView() {
        WebViewSoftKeyboardHeightFixer.assist(webView, BrowserUtils.getStatusBarHeight(getContext()));
        return webView;
    }

    @Override
    public void onPageStart(Browser browser, String url, Bitmap favicon) {

        Log.d(TAG, "onPageStart---url: " + url);
        if (BrowserUtils.isNetworkEnable(getContext()) && progressBar.getVisibility() != GONE) {
            progressBar.startLoading();
        }
        if (toolbar.getVisibility() != GONE) toolbar.setVisibility(VISIBLE);
        setErrorViewInVisible();
    }

    @Override
    public void onPageFinished(Browser browser, String url) {

        Log.d(TAG, "onPageFinished---url: " + url);

        if (this.errorView.getVisibility() == View.VISIBLE) return;
        progressBar.stopLoading();
        progressBar.setProgress(100);
        // 防止暗色模式下闪白屏,原因是set VISIBLE
        webView.setVisibility(View.VISIBLE);
        progressBar.postDelayed(() -> {
            if (this.errorView.getVisibility() == View.VISIBLE) return;
            progressBar.setVisibility(View.GONE);
            toolbar.setVisibility(View.GONE);
            webView.setAlpha(1f);
        }, 300);
    }

    @Override
    public void onLoadProgress(Browser browser, int newProgress) {
        Log.d(TAG, "onLoadProgress---newProgress: " + newProgress);

    }

    @Override
    public void onReceivedTitle(Browser browser, String title) {
        Log.d(TAG, "onReceivedTitle---title: " + title);
        if (!isFixedTitle && !URLUtil.isNetworkUrl(title)) toolbar.setTitle(title);
    }

    @Override
    public void setTitle(String title) {
        toolbar.setTitle(title);
    }


    @Override
    public void onReceivedError(Browser browser, String failUrl, int errorCode, String description) {

        Log.d(TAG, "onReceivedError---getUrl: " + getWebView().getUrl() + ", failUrl: " + failUrl + ",errorCode: " + errorCode + ",description: " + description);

        showErrorView(browser, failUrl, errorCode);
    }

    private boolean canShowErrorView(String failUrl) {

        if (this.errorView.getVisibility() == VISIBLE) return false;
        if (!BrowserUtils.isNetworkEnable(getContext())) return true;
        if (URLUtil.isNetworkUrl(failUrl) && (getWebView().getUrl() == null || BrowserUtils.isSameUrl(failUrl, getWebView().getUrl()))) return true;
        return false;
    }

    private void showErrorView(Browser browser, String failUrl, int errorCode) {
        Log.d(TAG, "showErrorView---failingUrl: " + failUrl + ",errorCode: " + errorCode);
        if (!canShowErrorView(failUrl)) return;
        if (!BrowserUtils.isNetworkEnable(getContext())) errorCode = ERROR_UNKNOWN;
        View errorView = ErrorPresentationProvider.createErrorView(browser, errorCode, failUrl);
        if (errorView != null) {
            Log.d(TAG, "setErrorViewVisible---failingUrl: " + failUrl + ",errorCode: " + errorCode);
            this.errorView.addView(errorView);
            this.errorView.setVisibility(View.VISIBLE);
            webView.setAlpha(0f);
            webView.setVisibility(INVISIBLE);
            toolbar.setVisibility(VISIBLE);
            progressBar.setVisibility(INVISIBLE);
        }
    }

    @Override
    public void onRefresh(Browser browser) {
        super.onRefresh(browser);
        setErrorViewInVisible();
    }

    private void setErrorViewInVisible() {
        errorView.removeAllViews();
        errorView.setVisibility(View.GONE);
    }

    @Override
    public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        ((AppCompatActivity)getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        ((AppCompatActivity)getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.callback = callback;
        customView = view;
        if (customView != null) {
            webView.setVisibility(View.GONE);
            layoutRoot.addView(customView);
        }


    }

    @Override
    public void onHideCustomView() {
        ((AppCompatActivity)getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ((AppCompatActivity)getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (callback != null) {
            this.callback.onCustomViewHidden();
            webView.setVisibility(VISIBLE);
            layoutRoot.removeView(customView);
        }
    }

}
