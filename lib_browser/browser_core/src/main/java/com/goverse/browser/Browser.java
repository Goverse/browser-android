package com.goverse.browser;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.goverse.browser.js.IJsExecutor;
import com.goverse.browser.js.JsRegistry;
import com.goverse.browser.js.JsScheduler;
import com.goverse.browser.lifecycle.BrowserLifeCycle;
import com.goverse.browser.listener.OnJsActionListener;
import com.goverse.browser.listener.OnPageLoadListener;
import com.goverse.browser.presentation.FullScreenPresentation;
import com.goverse.browser.presentation.NormalPresentation;
import com.goverse.browser.presentation.Presentation;
import com.goverse.browser.presentation.ScreenPresentation;
import com.goverse.browser.url.IUrlInterceptor;
import com.goverse.browser.url.UrlDispatcher;
import com.goverse.browser.url.UrlInfo;
import com.goverse.browser.url.UrlNavigator;
import com.goverse.browser.url.matcher.IMatcher;
import com.goverse.browser.url.matcher.UrlMatcher;
import com.goverse.browser.BuildConfig;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static com.goverse.browser.utils.BrowserUtils.checkIfNeedUrlDecode;

public class Browser {

    /**
     * TAG
     */
    public static final String TAG = Browser.class.getSimpleName();

    /**
     * CustowebView
     */
    private WebView webView;

    /**
     * JsDispatcher，uesd to dispatch url schema when
     * method shouldOverrideUrlLoading invoked, for call
     * native methods by javascript.
     */
    private UrlDispatcher urlDispatcher;

    /**
     * Url Navigator
     */
    private UrlNavigator urlNavigator;

    private JsRegistry jsRegistry;

    private Presentation presentation;

    public static final class Theme {
        /**
         * normal style, with app title bar
         */
        public static final int NORMAL = 0;
        /**
         * fullScreen, with not title bar, it has to set with on {@link BaseBrowserActivity#requestFullScreenLayout()}
         */
        public static final int FULL_SCREEN = 1;
        /**
         * screen, the difference to fullscreen is that this style will not hide status bar.
         */
        public static final int SCREEN = 2;
    }

    /**
     * UI Handler
     */
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private final int DEFAULT_TIMEOUT = 10;

    private static String DefaultSafeDomain;
    private static String AppendUserAgent;

    /**
     * OnJsActionListener
     */
    private OnJsActionListener onJsActionListener;

    private JsScheduler jsScheduler;

    private static final List<Class<? extends IJsExecutor>> DefaultJsExecutorList = new ArrayList<>();

    private Browser(Builder builder) {
        presentation = builder.presentation == null ? createPresentation(builder.theme, builder.context, builder.title) : builder.presentation;
        builder.browserView.addView(presentation.getView());
        this.webView = presentation.getWebView();
        this.jsRegistry = new JsRegistry(this);
        this.jsScheduler = new JsScheduler(this, DefaultSafeDomain, DefaultJsExecutorList, builder.jsExecutorList);
        this.urlNavigator = new UrlNavigator(this, buildWhiteListMatcher(builder), (builder.timeOut == 0) ? DEFAULT_TIMEOUT : builder.timeOut);
        this.urlDispatcher = new UrlDispatcher(builder.urlInterceptors);
        this.urlNavigator.addOnPageLoadListener(presentation);
        ((ComponentActivity)(builder.context)).getLifecycle().addObserver(new BrowserLifeCycle(webView, mainHandler, builder.enableCache));
        if (builder.onPageLoadListener != null) urlNavigator.addOnPageLoadListener(builder.onPageLoadListener);
        onJsActionListener = builder.onJsActionListener;
        jsRegistry.registerJsInterface(jsScheduler);
        setupWebView(this.webView, builder);
        if (BuildConfig.DEBUG) setAllowUniversalAccessFromFileURLs(webView);
    }

    private void setAllowUniversalAccessFromFileURLs(WebView webView) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Class<?> clazz = webView.getSettings().getClass();
                Method method = clazz.getMethod(
                        "setAllowUniversalAccessFromFileURLs", boolean.class);
                if (method != null) {
                    method.invoke(webView.getSettings(), true);
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private IMatcher buildWhiteListMatcher(Builder builder) {

        Log.d(TAG, "setupWhiteList---resId: " + builder.whiteResId);
        IMatcher whiteListMatcher = null;
        if (builder.matcher != null) {
            whiteListMatcher = builder.matcher;
        } else {
            List<String> list = null;
            if (builder.whiteList != null) {
                list = builder.whiteList;
            } else if (builder.whiteResId != 0) {
                try {
                    list = Arrays.asList(builder.context.getResources().getStringArray(builder.whiteResId));
                } catch (Exception e) {
                    Log.w(TAG, "setupWhiteList---Unable to find resource: " + builder.whiteResId);
                    e.printStackTrace();
                }
            }
            if (list != null && list.size() > 0) {
                whiteListMatcher = new UrlMatcher(list);
            }
        }
        return whiteListMatcher;
    }

    private void setupWebView(WebView webView, Builder builder) {
        Log.d(TAG, "setupWebView");
        if (webView != null) {
            setJavaScriptEnabled(builder.enableJavaScript);
            setAdoptScreen(builder.adoptScreen);
            setSupportZoom(builder.supportZoom);
            webView.setDrawingCacheEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setTextZoom(100);
            webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
            webView.getSettings().setSavePassword(false);
            webView.getSettings().setAllowFileAccess(builder.allowFileAccess);
            webView.getSettings().setAllowFileAccessFromFileURLs(builder.allowFileAccess);
            webView.setWebChromeClient(mWebChromeClient);
            webView.setWebViewClient(mExtWebViewClient);
            jsRegistry.registerJsInterfaces(builder.javascriptInterfaceList);
            jsRegistry.enableHttpProxy(builder.enableHttpProxy);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }
            String userAgentString = webView.getSettings().getUserAgentString();
            webView.getSettings().setUserAgentString(userAgentString + " " + AppendUserAgent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                webView.setForceDarkAllowed(builder.supportDarkMode);
            }
        }

    }

    private Presentation createPresentation(int theme, Context context, String title) {

        Presentation presentation = null;
        title = checkIfNeedUrlDecode(title);
        if (theme == Theme.NORMAL) {
            presentation = new NormalPresentation(context,title);
        } else if (theme == Theme.FULL_SCREEN) {
            presentation = new FullScreenPresentation(context, title);
        } else if (theme == Theme.SCREEN) {
            presentation = new ScreenPresentation(context, title);
        } else {
            presentation = new NormalPresentation(context,title);
        }
        return presentation;
    }

    /**
     * WebChromeClient
     */
    public final WebChromeClient mWebChromeClient = new WebChromeClient() {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            Log.d(TAG, "onProgressChanged---newProgress: " + newProgress + ",url: " + view.getUrl());
            urlNavigator.onProgressChanged(view, newProgress);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Log.d(TAG, "onJsAlert");
            if (onJsActionListener != null)
                onJsActionListener.onJsAlert(Browser.this, url, message, result);
            return super.onJsAlert(view, url, message, result);
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            Log.d(TAG, "onJsConfirm---url: " + url + ", message: " + message);
            if (onJsActionListener != null)
                onJsActionListener.onJsConfirm(Browser.this, url, message, result);
            return super.onJsConfirm(view, url, message, result);
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            Log.d(TAG, "onJsPrompt---url: " + url + ", message: " + message + ", defaultValue: " + defaultValue);
            if (onJsActionListener != null)
                onJsActionListener.onJsPrompt(Browser.this, url, message, defaultValue, result);
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            Log.d(TAG, "onReceivedTitle---title: " + title);
            urlNavigator.onReceivedTitle(view, title);
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
            if (presentation != null) presentation.onShowCustomView(view, callback);
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            if (presentation != null) presentation.onHideCustomView();
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {

            Log.d(TAG, "onShowFileChooser");
            Browser.this.filePathCallback = filePathCallback;
            return openFileChooser(fileChooserParams);
        }
    };

    private boolean acceptImageType(String[] acceptTypes) {
        if (acceptTypes.length == 1 && acceptTypes[0].contains("image")) return true;
        return false;
    }

    public static final int BROWSER_FILE_CHOOSER_REQUSET_CODE = 10000;

    private boolean openFileChooser(WebChromeClient.FileChooserParams fileChooserParams) {

        if (fileChooserParams == null) return false;
        String[] acceptTypes = fileChooserParams.getAcceptTypes();
        if (acceptTypes == null || acceptTypes.length == 0) return false;

        int mode = fileChooserParams.getMode();
        boolean acceptImageType = acceptImageType(acceptTypes);
        String intentType = (acceptImageType) ? "image/*" : "*/*";

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, (mode == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE) ? true : false);
        intent.setType(intentType);
        ((ComponentActivity)getWebView().getContext()).startActivityForResult(intent, BROWSER_FILE_CHOOSER_REQUSET_CODE);
        return true;
    }

    private ValueCallback<Uri[]> filePathCallback;

    public final class ExtWebViewClient extends WebViewClient {

        /**
         * Failed to load the url not in whitelist.
         */
        public static final int ERROR_INVALID_URL = -100;

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
            Log.d(TAG, "onReceivedHttpError---url: " + view.getUrl() + ",requestUrl: " + request.getUrl() + ",originalUrl: " + view.getOriginalUrl());
            urlNavigator.onReceivedHttpError(view, request, errorResponse);

        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Log.d(TAG, "onReceivedSslError---error: " + error.toString());
            urlNavigator.onReceivedSslError(view, handler, error);

        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Log.d(TAG, "shouldOverrideUrlLoading---url: " + request.getUrl().toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Log.d(TAG, "shouldOverrideUrlLoading---isRedirect: " + request.isRedirect());
                return shouldOverrideUrlLoading(request.getUrl().toString(), view, request.isRedirect());
            }
            return shouldOverrideUrlLoading(request.getUrl().toString(), view, false);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "shouldOverrideUrlLoading---url: " + url);
            return shouldOverrideUrlLoading(url, view, false);
        }

        private boolean shouldOverrideUrlLoading(String url, WebView view, boolean isDirect) {

            boolean canHandle = urlDispatcher.dispatch(webView, url);
            Log.d(TAG, "canHandle: " + canHandle);
            if (canHandle)  return true;
            return urlNavigator.shouldOverrideUrlLoading(url, view, isDirect);
        }


        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.d(TAG, "onPageStarted---url: " + url);
            urlNavigator.onPageStarted(view, url, favicon);

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.d(TAG, "onPageFinished---url: " + url);
            urlNavigator.onPageFinished(view, url);

        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            onReceivedError(view, failingUrl, errorCode, description);
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            onReceivedError(view, request.getUrl().toString(), error.getErrorCode(), error.getDescription().toString());
        }

        private void onReceivedError(WebView view, String failingUrl, int errorCode, String description) {
            Log.d(TAG, "onReceivedError---failingUrl: " + failingUrl + ",url: " + view.getUrl() + ",originalUrl: " + view.getOriginalUrl());
            urlNavigator.onReceivedError(view, failingUrl, errorCode, description);
        }

    }

    /**
     * ExtWebViewClient
     */
    private ExtWebViewClient mExtWebViewClient = new ExtWebViewClient();

    /**
     * open url
     *
     * @param url url
     */
    public void go(String url) {

        Log.d(TAG, "go---url: " + url);
        urlNavigator.loadUrl(url);
    }

    /**
     * goBack
     */
    public void goBack() {
        Log.d(TAG, "goBack");
        urlNavigator.back();
    }

    /**
     * goForword
     */
    public void goForword() {
        Log.d(TAG, "goForword");
        urlNavigator.forword();

    }

    /**
     * canGoBack
     */
    public boolean canGoBack() {
        Log.d(TAG, "canGoBack");
        return urlNavigator.canGoBack();
    }

    /**
     * evaluteJs
     *
     * @param js js
     * @param valueCallback valueCallback
     */
    public void evaluteJs(final String js, final ValueCallback valueCallback) {
        Log.d(TAG, "evaluteJs: " + js);
        webView.evaluateJavascript(js, valueCallback);
    }

    /**
     * evaluteJs
     *
     * @param js js
     */
    public void evaluteJs(final String js) {
        Log.d(TAG, "evaluteJs---js: " + js);
        webView.loadUrl(js);
    }

    /**
     * add JavascriptInterface
     * @param obj jsInterface object
     */
    public void addJavascriptInterface(Object obj) {
        jsRegistry.registerJsInterface(obj);
    }

    /**
     * add addJavascriptExecutor
     * @param jsExecutor jsExecutor
     */
    public void addJavascriptExecutor(IJsExecutor jsExecutor) {
        this.jsScheduler.addJsExecutor(jsExecutor);
    }

    /**
     * remove JavascriptInterface
     * @param obj jsInterface object
     */
    public void removeJavascriptInterface(Object obj) {
        jsRegistry.unRegisterJsInterface(obj);
    }

    /**
     * remove all JavascriptInterface
     */
    public void removeAllJavascriptInterfaces() {
        jsRegistry.unRegisterAllJsInterfaces();
    }

    /**
     * add JsHandler to handle url
     * @param jsHandler defined in {@link IUrlInterceptor}
     */
    public void addJsHandler(IUrlInterceptor jsHandler) {
        if (urlDispatcher != null) {
            urlDispatcher.addJsHandler(jsHandler);
        }
    }

    /**
     * remove JsHandler
     * @param jsHandler
     */
    public void removeJsHandler(IUrlInterceptor jsHandler) {
        if (urlDispatcher != null) {
            urlDispatcher.removeJsHandler(jsHandler);
        }
    }

    public void setDefaultSafeDomain(String defaultSafeDomain) {
        if (jsScheduler != null) {
            jsScheduler.setDefaultSafeDomain(defaultSafeDomain);
        }
    }

    public WebView getWebView() {
        return webView;
    }

    /**
     * getPresentation
     *
     * @return  Presentation
     */
    public Presentation getPresentation() {
        Log.d(TAG, "getPresentation");
        return presentation;
    }

    /**
     * get current url
     *
     * @return current url
     */
    public String getCurrentUrl() {
        String url = "";
        UrlInfo currentUrlInfo = urlNavigator.getCurrentUrlInfo();
        if (currentUrlInfo != null) {
            url = currentUrlInfo.getUrl();
        }
        return url;
    }

    /**
     * get current urlInfo, also can get url stack
     * @return urlInfo
     */
    public UrlInfo getCurrentUrlInfo() {
        return urlNavigator.getCurrentUrlInfo();
    }

    /**
     * reload current url
     *
     * @return current url
     */
    public void refresh() {
        Log.d(TAG, "refresh");
        urlNavigator.refresh();
    }

    /**
     * set Title
     * @param title title
     */
    public void setTitle(String title) {
        Log.d(TAG, "setTitle: " + title);
        presentation.setTitle(title);
    }

    /**
     * loadData
     * @param data html-formmat
     * @param mimeType mimeType ,"text/html"
     * @param encoding encoding ,"utf-8"
     */
    public void loadData(String data, String mimeType, String encoding) {
        urlNavigator.loadData(data, mimeType, encoding);
    }

    /**
     * loadDataWithBaseURL
     * @param baseUrl baseUrl, used for locating resource by relativePath
     * @param data html-formmat
     * @param mimeType mimeType ,"text/html"
     * @param encoding encoding ,"utf-8"
     * @param failUrl failUrl
     */
    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String failUrl) {
        urlNavigator.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, failUrl);
    }

    /**
     * handle file chooser in callback {@link BaseBrowserActivity onActivityResult}
     * @param requestCode requestCode
     * @param resultCode resultCode
     * @param data data
     */
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == BROWSER_FILE_CHOOSER_REQUSET_CODE) {
            Uri[] uris = null;
            if (resultCode == Activity.RESULT_OK) {

                String dataString = data.getDataString();
                ClipData clipData = data.getClipData();

                if (clipData != null) {
                    uris = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i ++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        uris[i] = item.getUri();
                    }
                } else {
                    if (!TextUtils.isEmpty(dataString)) {
                        uris = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }
            filePathCallback.onReceiveValue(uris);
            filePathCallback = null;
        }
    }

    private void setJavaScriptEnabled(boolean enable) {
        webView.getSettings().setJavaScriptEnabled(enable);
    }

    private void setSupportZoom(boolean supportZoom) {
        webView.getSettings().setSupportZoom(supportZoom);
    }

    private void setAdoptScreen(boolean adoptScreen) {
        webView.getSettings().setUseWideViewPort(adoptScreen);
        webView.getSettings().setLoadWithOverviewMode(adoptScreen);
    }

    public Handler getHandler() {
        return mainHandler;
    }

    /**
     * create a builder of Browser, supply interfaces to
     * config params.
     *
     * @param context context
     * @return Browser.Builder
     */
    public static Browser.Builder with(Context context) {
        Log.d(TAG, "withContext");
        return new Builder(context);
    }

    /**
     * Browser.Builder
     */
    public static class Builder {
        private BrowserView browserView;
        private Context context;
        private String title = null;
        private int whiteResId = 0;
        private List<String> whiteList;
        private IMatcher matcher;
        private List<Object> javascriptInterfaceList = new ArrayList<>();
        private boolean enableHttpProxy = false;
        private boolean adoptScreen = true;
        private boolean supportZoom = true;
        private boolean supportDarkMode = true;
        private boolean enableJavaScript = true;
        private boolean openJavaScriptLog;
        private boolean enableCache = true;
        private boolean allowFileAccess;
        private IUrlInterceptor[] urlInterceptors;
        private List<IJsExecutor> jsExecutorList;
        private OnPageLoadListener onPageLoadListener;
        private OnJsActionListener onJsActionListener;
        private int timeOut = 0;
        private int theme = 0;
        private Presentation presentation;

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * Set webView
         *
         * @param browserView browserView
         * @return Browser.Builder
         */
        public Builder setView(BrowserView browserView) {
            this.browserView = browserView;
            return this;
        }

        public Builder theme(@BrowserTheme int theme) {
            this.theme = theme;
            return this;
        }

        public Builder presentation(Presentation presentation) {
            this.presentation = presentation;
            return this;
        }

        /**
         * Set url whiteList
         *
         * @param whiteList whiteList
         * @return Browser.Builder
         */
        public Builder whiteList(List<String> whiteList) {
            this.whiteList = whiteList;
            return this;
        }

        public Builder whiteList(String[] whiteList) {
            if (whiteList != null) {
                this.whiteList = Arrays.asList(whiteList);
            }
            return this;
        }

        /**
         * Set url whiteList
         *
         * @param resId resId
         * @return Browser.Builder
         */
        public Builder whiteList(int resId) {
            this.whiteResId = resId;
            return this;
        }

        /**
         * Set url whiteList
         *
         * @param matcher matcher
         * @return Browser.Builder
         */
        public Builder whiteList(IMatcher matcher) {
            this.matcher = matcher;
            return this;
        }

        /**
         * Set adoptScreen
         *
         * @param adoptScreen adoptScreen
         * @return Browser.Builder
         */
        public Builder adoptScreen(boolean adoptScreen) {
            this.adoptScreen = adoptScreen;
            return this;
        }

        /**
         * Set allowFileAccess
         *
         * @param allowFileAccess allowFileAccess
         * @return Browser.Builder
         */
        public Builder allowFileAccess(boolean allowFileAccess) {
            this.allowFileAccess = allowFileAccess;
            return this;
        }

        /**
         * enable JavaScript Log, default method for js is:
         * Android:printJsLog(int level, String tag, String log)
         *
         * @return Browser.Builder
         */
        public Builder openJavaScriptLog() {
            this.openJavaScriptLog = true;
            return this;
        }

        /**
         * Enable JavaScript function
         *
         * @param enableJavaScript enableJavaScript
         * @return Browser.Builder
         */
        public Builder enableJavaScript(boolean enableJavaScript) {
            this.enableJavaScript = enableJavaScript;
            return this;
        }

        /**
         * set timeout in seconds
         *
         * @param timeOut timeOut
         * @return Browser.Builder
         */
        public Builder timeOut(int timeOut) {
            this.timeOut = timeOut;
            return this;
        }

        /**
         * enable cache
         *
         * @param enableCache enableCache
         * @return Browser.Builder
         */
        public Builder cache(boolean enableCache) {
            this.enableCache = enableCache;
            return this;
        }

        /**
         * enable Http Proxy, proxying H5 request by native.
         *
         * @param enableHttpProxy enableHttpProxy
         * @return Browser.Builder
         */
        public Builder enableHttpProxy(boolean enableHttpProxy) {
            this.enableHttpProxy = enableHttpProxy;
            return this;
        }

        /**
         * set title
         *
         * @param title title
         * @return Browser.Builder
         */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /**
         * set title resId
         *
         * @param resId resId
         * @return Browser.Builder
         */
        public Builder title(int resId) {
            this.title = context.getString(resId);
            return this;
        }


        /**
         * support the operation of zoom
         *
         * @param supportZoom supportZoom
         * @return Browser.Builder
         */
        public Builder supportZoom(boolean supportZoom) {
            this.supportZoom = supportZoom;
            return this;
        }

        /**
         * support darkMode above Android Q, support for default.
         * It's necessary to compatible with html file, if you want to
         * show custom style in dark mode instead using system default.
         * also need {@link com.goverse.browser.js.function.JsDarkMode}
         *
         * @param supportDarkMode supportDarkMode
         * @return Browser.Builder
         */
        public Builder supportDarkMode(boolean supportDarkMode) {
            this.supportDarkMode = supportDarkMode;
            return this;
        }

        /**
         * set OnPageLoadListener
         *
         * @param onPageLoadListener onPageLoadListener
         * @return Browser.Builder
         */
        public Builder setOnPageLoadListener(OnPageLoadListener onPageLoadListener) {
            this.onPageLoadListener = onPageLoadListener;
            return this;
        }

        /**
         * set OnJsActionListener
         *
         * @param onJsActionListener onJsActionListener
         * @return Browser.Builder
         */
        public Builder setOnJsActionListener(OnJsActionListener onJsActionListener) {
            this.onJsActionListener = onJsActionListener;
            return this;
        }

        /**
         * Register the object implement interface IJsHandler, which can
         * receive url schema to finish invocation by javascript method.
         * And all jsHandler should have annotation{@link .JsNameSpace}
         * marked above their class namespace. Besides, There is another way to
         * communicate with javascript method,See {@link Builder#addJavaScriptInterfaces}.
         *
         * @param urlInterceptors urlInterceptors
         * @return Browser.Builder
         */
        public Builder registerUrlInterceptor(IUrlInterceptor... urlInterceptors) {
            this.urlInterceptors = urlInterceptors;
            return this;
        }

        /**
         * Add nativeInterface objects marked on class namespace with
         * annotation {@link .JsNameSpace} that can be invoked
         * by javascript method. And each native method must mark
         *
         * @param objects objects
         * @return Browser.Builder
         * @link{@JavaScriptInterface} annotation, otherwise, security
         * problem may happen in low api version.
         */
        public Builder addJavaScriptInterfaces(Object... objects) {

            if (objects != null) {
                for (Object obj : objects) {
                    if (!javascriptInterfaceList.contains(obj)) {
                        javascriptInterfaceList.add(obj);
                    }
                }
            }
            return this;
        }

        /**
         * set javascript default executor to handle method by "window.App.call()" which
         * is the contract made by App with H5.
         * @param jsExecutor jsExecutor
         * @return Browser.Builder
         */
        public Builder addJavaScriptExecutor(IJsExecutor jsExecutor) {
            if (this.jsExecutorList == null) this.jsExecutorList = new ArrayList<>();
            if (!this.jsExecutorList.contains(jsExecutor)) this.jsExecutorList.add(jsExecutor);
            return this;
        }

        /**
         * To build a new Browser which is responsible for
         * managing the operation of webview.
         *
         * @return Browser
         */
        public Browser build() {
            return new Browser(this);
        }
    }

    /**
     * clear webview cache including localStorage, cookies.
     * @param context
     */
    public static void clearCache(Context context) {
        WebStorage.getInstance().deleteAllData();
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
    }

    /**
     * init browser
     * @param context context
     * @param defaultSafeDomain defaultSafeDomain
     * @param defaultJsExecutorList defaultJsExecutorList
     */
    public static void init(Context context, String appendUserAgent, String defaultSafeDomain, List<Class<? extends IJsExecutor>> defaultJsExecutorList) {
        DefaultSafeDomain = defaultSafeDomain;
        AppendUserAgent = appendUserAgent;
        if (defaultJsExecutorList != null) DefaultJsExecutorList.addAll(defaultJsExecutorList);
    }

    public static void initApplication(Context context) {
        setUpDataDirSuffix(context);
    }

    /**
     * It's necessary to setUp webView's data dir above android P if
     * using webView from more than one process possibly.
     * @param context context
     */
    private static void setUpDataDirSuffix(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = null;
            String mainProcessName = context.getPackageName();
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses();
            if(runningAppProcesses != null) {
                for (ActivityManager.RunningAppProcessInfo processInfo : runningAppProcesses) {
                    if(processInfo != null) {
                        if (processInfo.pid == android.os.Process.myPid()) {
                            processName = processInfo.processName;
                        }
                    }
                }
            }
            if (!TextUtils.isEmpty(processName) && !mainProcessName.equals(processName)) {
                try {
                    WebView.setDataDirectorySuffix(processName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
