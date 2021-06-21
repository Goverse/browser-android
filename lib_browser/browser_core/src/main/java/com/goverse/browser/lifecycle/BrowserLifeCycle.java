package com.goverse.browser.lifecycle;

import android.os.Handler;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebView;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

public final class BrowserLifeCycle implements LifecycleObserver {

    private WebView webView;

    /**
     * remove all messages when you have async action.
     */
    private Handler handler;

    private boolean enableCache;

    public BrowserLifeCycle(WebView webView, Handler handler, boolean enableCache) {
        this.handler = handler;
        this.webView = webView;
        this.enableCache = enableCache;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        notifyJsEvent(Lifecycle.Event.ON_START);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
         if (webView != null) {
             notifyJsEvent(Lifecycle.Event.ON_RESUME);
             webView.onResume();
         }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {

        if (webView != null) {
            notifyJsEvent(Lifecycle.Event.ON_PAUSE);
            webView.onPause();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        notifyJsEvent(Lifecycle.Event.ON_STOP);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestory() {

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }

        if (webView != null) {
            ViewParent parent = webView.getParent();
            if (parent != null) {
                ((ViewGroup)parent).removeView(webView);
            }
            webView.getSettings().setJavaScriptEnabled(false);
            if (!enableCache) {
                 webView.clearCache(true);
            }
            webView.clearHistory();
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
    }

    private void notifyJsEvent(Lifecycle.Event event) {

        if (this.webView != null) {
            if (event == Lifecycle.Event.ON_START) {
                webView.evaluateJavascript("javascript:if(window.App.lifeCycle){window.App.lifeCycle.onPageStart();}", null);
            } else if (event == Lifecycle.Event.ON_STOP) {
                webView.evaluateJavascript("javascript:if(window.App.lifeCycle){window.App.lifeCycle.onPageStop();}", null);
            } else if (event == Lifecycle.Event.ON_RESUME) {
                webView.evaluateJavascript("javascript:if(window.App.lifeCycle){window.App.lifeCycle.onPageResume();}", null);
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                webView.evaluateJavascript("javascript:if(window.App.lifeCycle){window.App.lifeCycle.onPagePause();}", null);
            }
        }
    }
}
