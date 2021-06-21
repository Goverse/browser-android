package com.goverse.browser.process;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.MemoryFile;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebView;
import com.goverse.browser.BaseBrowserActivity;
import com.goverse.browser.Browser;
import com.goverse.browser.BrowserView;
import com.goverse.browser.listener.OnJsActionListener;
import com.goverse.browser.listener.OnPageLoadListener;
import java.io.FileDescriptor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BrowserActivity extends BaseBrowserActivity implements BnBrowser.OnCommandListener {

    public static final String TAG = "BrowserActivity";
    private String startUrl;
    private Map<FileDescriptor, MemoryFile> memoryFileMap = new HashMap<>();
    private Map<String, BrowserSetting> urlBrowserSettingMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        BnBrowser.getInstance().setOnCommandListener(this);
        onReceiveIntent(getIntent());
        super.onCreate(savedInstanceState);
        BnBrowser.getInstance().sendBrowserEvent(new BrowserEvent(BnBrowser.Event.METHOD_INVOKED, getBrowser().getCurrentUrl(), new String[]{ "onCreate" }, "Browser"));
    }

    @Override
    public void onRequestWindowFeature() {
        super.onRequestWindowFeature();
        BrowserSetting browserSetting = urlBrowserSettingMap.get(startUrl);
        if (browserSetting != null && browserSetting.getTheme() == Browser.Theme.FULL_SCREEN) {
            requestFullScreenLayout();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BnBrowser.getInstance().sendBrowserEvent(new BrowserEvent(BnBrowser.Event.METHOD_INVOKED, getBrowser().getCurrentUrl(), new String[]{ "onResume" }, "Browser"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        BnBrowser.getInstance().sendBrowserEvent(new BrowserEvent(BnBrowser.Event.METHOD_INVOKED, getBrowser().getCurrentUrl(), new String[]{ "onPause" }, "Browser"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        BnBrowser.getInstance().sendBrowserEvent(new BrowserEvent(BnBrowser.Event.METHOD_INVOKED, getBrowser().getCurrentUrl(), new String[]{ "onStop" }, "Browser"));
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        BnBrowser.getInstance().sendBrowserEvent(new BrowserEvent(BnBrowser.Event.METHOD_INVOKED, getBrowser().getCurrentUrl(), new String[]{ "onDestroy" }, "Browser"));
        BnBrowser.getInstance().sendBrowserEvent(new BrowserEvent(BnBrowser.Event.CLOSED, getBrowser().getCurrentUrl()));
        BnBrowser.getInstance().setOnCommandListener(null);
        closeAllMemoryFile();
    }

    private void closeAllMemoryFile() {
        Set<FileDescriptor> fileDescriptors = memoryFileMap.keySet();
        if (fileDescriptors != null && fileDescriptors.size() > 0) {
            for (FileDescriptor fileDescriptor : fileDescriptors) {
                MemoryFile memoryFile = memoryFileMap.get(fileDescriptor);
                if (memoryFile != null) memoryFile.close();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        onReceiveIntent(intent);
    }

    private void onReceiveIntent(Intent intent) {
        startUrl = intent.getStringExtra("url");
        Log.d(TAG, "onReceiveIntent---url: " + startUrl);
        BrowserSetting browserSetting = intent.getParcelableExtra("browserSetting");
        urlBrowserSettingMap.put(startUrl, browserSetting);
    }

    private Browser buildBrowser(BrowserView browserView, BrowserSetting browserSetting) {
        Browser browser = Browser.with(this)
                .setView(browserView)
                .theme(browserSetting.getTheme())
                .title(browserSetting.getFixedTitle())
                .adoptScreen(browserSetting.isAdoptScreen())
                .allowFileAccess(browserSetting.isAllowFileAccess())
                .cache(browserSetting.isEnableCache())
                .enableJavaScript(browserSetting.isEnableJavaScript())
                .registerUrlInterceptor((webView, url) -> {
                    String res = BnBrowser.getInstance().sendBrowserEvent(new BrowserEvent(BnBrowser.Event.PAGE_INTERCEPT, webView.getUrl(), new String[]{url}));
                    return Boolean.parseBoolean(res);
                })
                .setOnJsActionListener(onJsActionListener)
                .setOnPageLoadListener(onPageLoadListener)
                .build();
        String[] javascriptObjects = browserSetting.getJavascriptObjects();
        if (javascriptObjects != null && javascriptObjects.length != 0) {
            for (String javascriptObjectName : javascriptObjects) {
                browser.getWebView().addJavascriptInterface(new JsAppProxy(javascriptObjectName, browser.getWebView()), javascriptObjectName);
            }
        }
        return browser;
    }

    @Override
    public Browser onCreateBrowser(BrowserView browserView) {
        return buildBrowser(browserView, urlBrowserSettingMap.get(startUrl));
    }

    @Override
    public String url() {
        return startUrl;
    }

    private void clearMemoryFile(int fileDescriptor) {
        Log.d(TAG, "clearMemoryFile---fileDescriptor: " + fileDescriptor);
        MemoryFile memoryFile = memoryFileMap.get(fileDescriptor);
        if (memoryFile != null) memoryFile.close();
    }

    @Override
    public String onCommandDispatch(int command, String[] args) {
        Log.d(TAG, "onCommandDispatch---command: " + command );

        switch (command) {
            case BnBrowser.Command.GO_BACK :
                getBrowser().goBack();
                break;
            case BnBrowser.Command.GO_FORWARD :
                getBrowser().goForword();
                break;
            case BnBrowser.Command.REFRESH :
                getBrowser().refresh();
                break;
            case BnBrowser.Command.EVALUATE_JS :
                getBrowser().evaluteJs(args[0]);
                break;
            case BnBrowser.Command.CLOSE :
                finish();
                break;
            case BnBrowser.Command.CLEAR_MEMORY_FILE :
                int fileDescriptor = Integer.parseInt(args[0]);
                clearMemoryFile(fileDescriptor);
                break;
            case BnBrowser.Command.CAPTURE :
                String fromObj = args[args.length - 2];
                String replyTo = args[args.length - 1];
                BnBrowser.getInstance().sendBrowserEvent(new BrowserEvent(BnBrowser.Event.METHOD_INVOKED, getBrowser().getCurrentUrl(), new String[]{ replyTo }, fromObj));
                break;
            case BnBrowser.Command.SCREEN_LANDSCAPE :
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                break;
            case BnBrowser.Command.SCREEN_PORTRAIT :
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                break;
        }
        return null;
    }

    private OnJsActionListener onJsActionListener = new OnJsActionListener() {

        @Override
        public void onJsAlert(Browser browser, String url, String message, JsResult result) {
            BnBrowser.getInstance().sendBrowserEvent(new BrowserEvent(BnBrowser.Event.JS_ALERT, url, new String[]{message}));
        }

        @Override
        public void onJsConfirm(Browser browser, String url, String message, JsResult result) {
            BnBrowser.getInstance().sendBrowserEvent(new BrowserEvent(BnBrowser.Event.JS_CONFIRM, url, new String[]{message}));
        }

        @Override
        public void onJsPrompt(Browser browser, String url, String message, String defaultValue, JsPromptResult result) {
            BnBrowser.getInstance().sendBrowserEvent(new BrowserEvent(BnBrowser.Event.JS_PROMPT, url, new String[]{message, defaultValue}));
        }
    };

    private OnPageLoadListener onPageLoadListener = new OnPageLoadListener() {
        @Override
        public void onPageStart(Browser browser, String url, Bitmap favicon) {
            BnBrowser.getInstance().sendBrowserEvent(new BrowserEvent(BnBrowser.Event.PAGE_START, url));
        }

        @Override
        public void onPageFinished(Browser browser, String url) {
            BnBrowser.getInstance().sendBrowserEvent(new BrowserEvent(BnBrowser.Event.PAGE_FINISHED, url));
        }

        @Override
        public void onLoadProgress(Browser browser, int newProgress) {
            BnBrowser.getInstance().sendBrowserEvent(new BrowserEvent(BnBrowser.Event.LOAD_PROGRESS, browser.getCurrentUrl()));
        }

        @Override
        public void onReceivedTitle(Browser browser, String title) {

        }

        @Override
        public void onReceivedError(Browser browser, String failUrl, int errorCode, String description) {
            BnBrowser.getInstance().sendBrowserEvent(new BrowserEvent(BnBrowser.Event.RECEIVED_ERROR, browser.getCurrentUrl(), new String[]{failUrl, String.valueOf(errorCode), description}));

        }

        @Override
        public void onRefresh(Browser browser) {
            BnBrowser.getInstance().sendBrowserEvent(new BrowserEvent(BnBrowser.Event.REFRESH, browser.getCurrentUrl()));

        }
    };

    private static class JsAppProxy {

        private String javascriptObjectName;
        private WebView webView;

        public JsAppProxy(String javascriptObjectName, WebView webView) {
            this.javascriptObjectName = javascriptObjectName;
            this.webView = webView;
        }

        @JavascriptInterface
        public String call(String method, String param) {
            Log.d(TAG, "JsApp---call: " + method + ", param: " + param);
            String res = null;
            try {
                res = onMethodCall(method, param, this.webView.getUrl());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }

        public String onMethodCall(String method, String param, String url) {
            return BnBrowser.getInstance().sendBrowserEvent(new BrowserEvent(BnBrowser.Event.METHOD_INVOKED, url, new String[]{ method, param }, javascriptObjectName));
        }
    }
}
