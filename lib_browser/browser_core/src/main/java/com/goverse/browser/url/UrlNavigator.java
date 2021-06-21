package com.goverse.browser.url;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import androidx.core.util.Preconditions;
import com.goverse.browser.Browser;
import com.goverse.browser.listener.OnPageLoadListener;
import com.goverse.browser.listener.PageLoadListenerWrapper;
import com.goverse.browser.url.matcher.IMatcher;
import com.goverse.browser.utils.BrowserUtils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import static android.webkit.WebViewClient.ERROR_FILE;
import static android.webkit.WebViewClient.ERROR_UNKNOWN;
import static com.goverse.browser.Browser.ExtWebViewClient.ERROR_INVALID_URL;
import static com.goverse.browser.url.UrlInfo.LOAD_DATA;
import static com.goverse.browser.utils.BrowserUtils.isSameUrl;

/**
 * Used to control webview's navigation.
 */
public class UrlNavigator {

    private final String TAG = UrlNavigator.class.getSimpleName();

    /**
     * urlTree
     */
    private UrlTree urlTree;

    /**
     * Browser
     */
    private Browser browser;

    private WebView webView;

    /**
     * white list Matcher
     */
    private IMatcher whiteListMatcher;

    /**
     * OnPageLoadListener
     */
    private PageLoadListenerWrapper pageLoadListenerWrapper;

    /**
     * load timeout in seconds
     */
    private int timeOut;

    /**
     * min timeout progress
     */
    private final int MIN_LOADING_TIMEOUT_PROGRSS = 80;

    @SuppressLint("RestrictedApi")
    public UrlNavigator(Browser browser, IMatcher matcher, int timeOut) {
        Preconditions.checkNotNull(browser);
        Preconditions.checkNotNull(browser.getWebView());
        this.browser = browser;
        this.webView = browser.getWebView();
        this.urlTree = new UrlTree();
        this.whiteListMatcher = matcher;
        this.timeOut = timeOut;
        this.pageLoadListenerWrapper = new PageLoadListenerWrapper();
    }

    public boolean canGoBack() {
        if (browser.getWebView() != null) {
            return browser.getWebView().canGoBack();
        }
        return false;
    }

    public void back() {
        Log.d(TAG, "back");
        urlTree.pre();
        browser.getWebView().goBack();
    }

    public void forword() {
        Log.d(TAG, "forword");
        if (browser.getWebView().canGoForward()) {
            urlTree.next();
            browser.getWebView().goForward();
        }
    }

    private boolean canLoad(String url) {

        Log.d(TAG, "canLoad, url: " + url);
        if (whiteListMatcher != null && !whiteListMatcher.match(url)) {
            Log.d(TAG, "url: " + url + " can not load, which is not in whiteList!!!");
            return false;
        }

        if (!isUrlValid(url)) {
            Log.d(TAG, "canLoad, " + url + " is not valid!!!");
            return false;
        }

        return true;
    }

    public boolean isUrlValid(String url) {
        Log.d(TAG, "isUrlValid");
        boolean isNetworkUrl = URLUtil.isNetworkUrl(url);
        return isNetworkUrl;
    }

    public void loadUrl(String url) {
        Log.d(TAG, "loadUrl: " + url);

        boolean networkEnable = BrowserUtils.isNetworkEnable(webView.getContext());
        if (!networkEnable) {
            webView.setTag(url);
            if (pageLoadListenerWrapper != null) pageLoadListenerWrapper.onReceivedError(browser, url, ERROR_UNKNOWN, "");
            return;
        }

        if (canLoad(url)) {
            urlTree.push(new UrlInfo(url));
            webView.loadUrl(url);
        }
    }

    public void refresh() {
        Log.d(TAG, "refresh");

        boolean networkEnable = BrowserUtils.isNetworkEnable(webView.getContext());
        if (!networkEnable) {
            String url = (webView.getUrl() == null) ? (String) webView.getTag() : webView.getUrl();
            if (pageLoadListenerWrapper != null) pageLoadListenerWrapper.onReceivedError(browser, url, ERROR_UNKNOWN, "");
            return;
        }

        String url = webView.getUrl();
        Object tag = webView.getTag();
        if (url == null) {
            if (tag != null) loadUrl((String) tag);
        } else {
            webView.reload();
        }
    }

    public void addOnPageLoadListener(OnPageLoadListener onPageLoadListener) {
        pageLoadListenerWrapper.addOnPageLoadListener(onPageLoadListener);
    }

    public void removeOnPageLoadListener(OnPageLoadListener onPageLoadListener) {
        pageLoadListenerWrapper.removeOnPageLoadListener(onPageLoadListener);
    }

    public void onProgressChanged(WebView view, int newProgress) {

        Log.d(TAG, "onProgressChanged: " + view.getUrl() + " newProgress: " + newProgress);
        if (pageLoadListenerWrapper != null) pageLoadListenerWrapper.onLoadProgress(browser, newProgress);
    }

    public void onReceivedTitle(WebView view, String title) {
        if (pageLoadListenerWrapper != null) pageLoadListenerWrapper.onReceivedTitle(browser, title);
    }

    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {

        if (request != null && request.getUrl() != null && isSameUrl(view.getUrl(), request.getUrl().toString())) {
            Log.d(TAG, "onReceivedHttpError---url: " + view.getUrl() + ",requestUrl: " + request.getUrl() + ",getStatusCode: " + errorResponse.getStatusCode());
            removeTimeOut();
            if (pageLoadListenerWrapper != null) pageLoadListenerWrapper.onReceivedError(browser, request.getUrl().toString(), ERROR_FILE, errorResponse.getReasonPhrase());
        }
    }

    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        boolean canHandle = handleSslError(handler, error);
        if (!canHandle) onReceivedError(view, error.getUrl(), ERROR_INVALID_URL, error.toString());
    }

    public boolean shouldOverrideUrlLoading(String url, WebView view, boolean isDirect) {

        WebView.HitTestResult hitTestResult = view.getHitTestResult();
        boolean shouldOverrideLoadUrl = !canLoad(url);
        if (!shouldOverrideLoadUrl) urlTree.push(new UrlInfo(url, hitTestResult.getType(), hitTestResult.getExtra()));
        Log.d(TAG, "shouldOverrideUrlLoading---getExtra: " + hitTestResult.getExtra() + ",getType " + hitTestResult.getType() + ", shouldOverrideLoadUrl: " + shouldOverrideLoadUrl);
        return shouldOverrideLoadUrl;
    }

    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Log.d(TAG, "onPageStarted: " + url);
        refreshTimeOut();
        if (pageLoadListenerWrapper != null) pageLoadListenerWrapper.onPageStart(browser, url, favicon);
    }

    public void onPageFinished(WebView view, String url) {
        Log.d(TAG, "onPageFinished: " + url);
        removeTimeOut();
        if (pageLoadListenerWrapper != null) pageLoadListenerWrapper.onPageFinished(browser, url);
    }

    public void onReceivedError(WebView webView, String failingUrl, int errorCode, String description) {
        Log.d(TAG, "onReceivedError: " + webView.getUrl() + ",failingUrl: " + failingUrl + ",errorCode: " + errorCode + ",description: " + description);
        removeTimeOut();
        if (pageLoadListenerWrapper != null) pageLoadListenerWrapper.onReceivedError(browser, failingUrl, errorCode, description);
    }

    /**
     * refresh timeout
     */
    private void refreshTimeOut() {

        Log.d(TAG, "refreshTimeOut");
        if (timeOut > 0) {
            browser.getHandler().removeCallbacks(timeOutRunnable);
            browser.getHandler().postDelayed(timeOutRunnable, timeOut * 1000);
        }
    }

    /**
     * remove timeout
     */
    private void removeTimeOut() {
        Log.d(TAG, "removeTimeOut");
        if (timeOut > 0) {
            browser.getHandler().removeCallbacks(timeOutRunnable);
        }
    }


    /**
     * timeout runnable
     */
    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            if (getProgress() < MIN_LOADING_TIMEOUT_PROGRSS) {
                onReceivedError(webView, webView.getUrl(),-1, "loading timeout!!!");
            }
        }
    };

    public UrlInfo getCurrentUrlInfo() {
        return urlTree.getCurrentUrlInfo();
    }

    public int getProgress() {
        if (webView != null) {
            return webView.getProgress();
        }
        return 0;
    }

    /**
     * Only in the white list, can the https request proceed when ssl error happened.
     * @param handler SslErrorHandler
     * @param error SslError
     */
    public boolean handleSslError(SslErrorHandler handler, SslError error) {
        Log.d(TAG, "handleSslError: " + error.toString());
        handler.cancel();
        return false;
    }

    /**
     * loadData {@link WebView#loadData(String data, String mimeType, String encoding)}
     * @param data html-formmat
     * @param mimeType mimeType ,"text/html"
     * @param encoding encoding ,"utf-8"
     */
    public void loadData(String data, String mimeType, String encoding) {

        if (webView != null) {
            urlTree.push(new UrlInfo(null, LOAD_DATA, generateSHA1(data)));
            webView.loadData(data, mimeType, encoding);
        }
    }

    /**
     * loadDataWithBaseURL {@link WebView#loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String failUrl)}
     * @param baseUrl baseUrl
     * @param data html-formmat
     * @param mimeType mimeType ,"text/html"
     * @param encoding encoding ,"utf-8"
     * @param failUrl failUrl
     */
    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String failUrl) {
        if (browser.getWebView() != null) {
            urlTree.push(new UrlInfo(baseUrl, LOAD_DATA, generateSHA1(data)));
            browser.getWebView().loadDataWithBaseURL(baseUrl, data, mimeType, encoding, failUrl);
        }
    }

    private String generateSHA1(String data) {

        if (TextUtils.isEmpty(data)) return "";
        byte[] hashText = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA1");
            digest.update(data.getBytes());
            hashText = digest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return bytesToHexString(hashText);
    }

    private String bytesToHexString(byte[] src) {

        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}
