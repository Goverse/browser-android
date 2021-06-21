package com.goverse.browser.utils;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * 该类用于解决WebView在全屏模式下软键盘弹出相关属性无效以及会导致H5 onReSize不回调的问题，具体原因是google的遗留Bug。
 * 解决方案参考: https://stackoverflow.com/questions/7417123/android-how-to-adjust-layout-in-full-screen-mode-when-softkeyboard-is-visible/19494006#19494006。
 */
public class WebViewSoftKeyboardHeightFixer {

    // For more information, see https://code.google.com/p/android/issues/detail?id=5497
    // To use this class, simply invoke assistActivity() on an Activity that already has its content view set.

    public static void assist(WebView webView, int windowPaddingTop) {
        new WebViewSoftKeyboardHeightFixer(webView, windowPaddingTop);
    }

    public static void assist(WebView webView) {
        new WebViewSoftKeyboardHeightFixer(webView,  0);
    }

    private View webView;
    private int webViewHeightPrevious;
    private int windowPaddingTop;
    private ViewGroup.LayoutParams layoutParams;

    private WebViewSoftKeyboardHeightFixer(WebView webView, int windowPaddingTop) {
        this.webView = webView;
        this.windowPaddingTop = windowPaddingTop;
        webView.getViewTreeObserver().addOnGlobalLayoutListener(() -> possiblyResizeChildOfContent());
        layoutParams = (ViewGroup.LayoutParams) webView.getLayoutParams();
    }

    private void possiblyResizeChildOfContent() {
        Rect r = new Rect();
        webView.getWindowVisibleDisplayFrame(r);
        int webViewHeightNow = r.bottom - webView.getTop() - windowPaddingTop;
        if (webViewHeightNow != webViewHeightPrevious) {
            layoutParams.height = webViewHeightNow;
            webView.requestLayout();
            webViewHeightPrevious = webViewHeightNow;
        }
    }

}
