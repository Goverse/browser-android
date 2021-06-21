package com.goverse.browser.js;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import com.goverse.browser.Browser;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static com.goverse.browser.utils.BrowserUtils.isUrlSameDomain;

@JsNameSpace(namespace = "App")
public class JsScheduler {

    private final String TAG = getClass().getName();
    private Browser browser;
    private Context context;
    private String defaultSafeDomain;
    private List<IJsExecutor> defaultExecutorList = new ArrayList<>();
    private List<IJsExecutor> jsExecutorList;

    public JsScheduler(Browser browser, String defaultSafeDomain, List<Class<? extends IJsExecutor>> defaultExecutorClsList, List<IJsExecutor> jsExecutorList) {
        this.browser = browser;
        this.context = browser.getWebView().getContext();
        this.defaultSafeDomain = defaultSafeDomain;
        this.jsExecutorList = jsExecutorList;
        initDefaultExecutorList(defaultExecutorClsList);
    }

    private void initDefaultExecutorList(List<Class<? extends IJsExecutor>> defaultExecutorClsList) {
        if (defaultExecutorClsList != null) {
            for (Class cls : defaultExecutorClsList) {
                try {
                    defaultExecutorList.add((IJsExecutor) cls.getConstructor().newInstance());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addJsExecutor(IJsExecutor jsExecutor) {
        if (this.jsExecutorList == null) this.jsExecutorList = new ArrayList<>();
        if (!this.jsExecutorList.contains(jsExecutor)) this.jsExecutorList.add(jsExecutor);
    }

    @JavascriptInterface
    public String call(String method, String param) {
        Log.d(TAG, "JsApp---call: " + method + ", param: " + param);

        try {
            String currentUrl = this.browser.getCurrentUrl();
            if (this.jsExecutorList != null) {
                for (IJsExecutor jsExecutor : this.jsExecutorList) {
                    jsExecutor.setBrowser(this.browser);
                    String safeDomain = TextUtils.isEmpty(jsExecutor.getSafeDomain()) ? defaultSafeDomain : jsExecutor.getSafeDomain();
                    if (!jsExecutor.needCheckDomain() || isUrlSameDomain(safeDomain, currentUrl)) {
                        JsResult jsResult = jsExecutor.onMethodCall(this.context, method, param);
                        if (jsResult != null && jsResult.hasInvoked) return jsResult.result;
                    }
                }
            }

            if (isUrlSameDomain(defaultSafeDomain, currentUrl) && defaultExecutorList != null) {
                for (IJsExecutor jsExecutor : this.defaultExecutorList) {
                    jsExecutor.setBrowser(this.browser);
                    JsResult jsResult = jsExecutor.onMethodCall(this.context, method, param);
                    if (jsResult != null && jsResult.hasInvoked) return jsResult.result;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        }

        return JsResult.NOT_SUPPORT.getResult();
    }

    public void setDefaultSafeDomain(String defaultSafeDomain) {
        this.defaultSafeDomain = defaultSafeDomain;
    }
}
