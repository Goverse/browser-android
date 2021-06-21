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

import com.goverse.browser.BrowserProgressBar;
import com.goverse.browser.R;
import com.goverse.browser.Browser;
import com.goverse.browser.utils.BrowserUtils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.webkit.WebViewClient.ERROR_UNKNOWN;

public class NormalPresentation extends Presentation {

    private final String TAG = "NormalPresentation";
    private WebView webView;
    private Toolbar toolbar;
    private FrameLayout errorView;
    private BrowserProgressBar progressBar;
    private FrameLayout videoView;
    private WebChromeClient.CustomViewCallback callback;
    private View customView;
    private View toolbarLayout;

    public NormalPresentation(Context context, String title) {
        super(context, R.layout.lib_browser_theme_normal, title);
        toolbarLayout = findViewById(R.id.toolbar);
        webView = (WebView) findViewById(R.id.ext_webview);
        toolbar = (Toolbar) findViewById(R.id.lib_base_toolbar);
        errorView = (FrameLayout) findViewById(R.id.error_view);
        progressBar = (BrowserProgressBar) findViewById(R.id.progressbar);
        videoView = (FrameLayout) findViewById(R.id.video_view);
        initToolbar(toolbar, title, true);
    }

    private void initToolbar(Toolbar toolbar, String title, boolean isHomeAsUpEnabled) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) toolbarLayout.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        layoutParams.topMargin += BrowserUtils.getStatusBarHeight(webView.getContext());
        toolbarLayout.setLayoutParams(layoutParams);

        toolbar.setTitle(TextUtils.isEmpty(title) ? " " : title);
        toolbar.setPadding(toolbar.getPaddingLeft(), 10, toolbar.getPaddingRight(), toolbar.getPaddingBottom());
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
    public void setTitle(String title) {
        toolbar.setTitle(title);
    }

    @Override
    public WebView getWebView() {
        return webView;
    }

    @Override
    public void onPageStart(Browser browser, String url, Bitmap favicon) {

        Log.d(TAG, "onPageStart---url: " + url);
        if (BrowserUtils.isNetworkEnable(getContext())) {
            progressBar.startLoading();
        }
        setErrorViewInVisible();
    }

    @Override
    public void onPageFinished(Browser browser, String url) {

        Log.d(TAG, "onPageFinished---url: " + url);
        if (this.errorView.getVisibility() != View.VISIBLE) webView.setVisibility(View.VISIBLE);
        progressBar.stopLoading();
        progressBar.setProgress(100);
        progressBar.postDelayed(() -> {
            if (this.errorView.getVisibility() == View.VISIBLE) return;
            progressBar.setVisibility(GONE);
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
    public void onReceivedError(Browser browser, String failUrl, int errorCode, String description) {
        Log.d(TAG, "onReceivedError---url: " + getWebView().getUrl() +  ", failUrl: " + failUrl + ",errorCode: " + errorCode + ",description: " + description);
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
            webView.setVisibility(View.INVISIBLE);
            progressBar.stopLoading();
            progressBar.postDelayed(() -> progressBar.setVisibility(GONE), 300);
        }
    }

    @Override
    public void onRefresh(Browser browser) {
        super.onRefresh(browser);
        setErrorViewInVisible();
    }

    private void setErrorViewInVisible() {
        errorView.removeAllViews();
        errorView.setVisibility(GONE);
    }

    @Override
    public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {

        ((AppCompatActivity)getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        ((AppCompatActivity)getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.callback = callback;
        customView = view;

        if (customView != null) {

            webView.setVisibility(GONE);
            toolbar.setVisibility(View.INVISIBLE);
            videoView.addView(customView);
            videoView.setVisibility(VISIBLE);
        }
    }

    @Override
    public void onHideCustomView() {
        ((AppCompatActivity)getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ((AppCompatActivity)getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (callback != null) {
            this.callback.onCustomViewHidden();
            webView.setVisibility(VISIBLE);
            toolbar.setVisibility(VISIBLE);
            videoView.removeAllViews();
            videoView.setVisibility(GONE);
        }
    }

}
