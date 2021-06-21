package com.goverse.browser.js;
import android.content.Context;
import android.webkit.WebView;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.goverse.browser.Browser;
import java.util.HashMap;
import java.util.Map;
import static com.goverse.browser.js.JsResult.Code.FAILED;
import static com.goverse.browser.js.JsResult.Code.SUCCESS;

public abstract class IJsExecutor {

    public static final String TAG = "IJsExecutor";

    /**
     * safe domain to control access to native interface, use defaultSafeDomain in {@link JsScheduler} for default.
     */
    private String safeDomain;

    private Browser browser;

    private WebView webView;

    private boolean needCheckDomain = true;

    public IJsExecutor() {}

    public IJsExecutor(boolean needCheckDomain) {
        this.needCheckDomain = needCheckDomain;
    }

    public IJsExecutor(String safeDomain) {
        this.safeDomain = safeDomain;
    }

    public String getSafeDomain() {
        return safeDomain;
    }

    public boolean needCheckDomain() {
        return needCheckDomain;
    }

    public void setBrowser(Browser browser) {
        this.browser = browser;
        this.webView = browser.getWebView();
    }

    public WebView getWebView() {
        return webView;
    }

    public Browser getBrowser() {
        return this.browser;
    }

    public abstract JsResult onMethodCall(Context context, String method, String param);

    /**
     * H5 Js接口调用 回调默认接口，用于Js调用App，异步返回
     * @param isSuccess res
     * @param data data
     * @param type callback method
     * @param message message
     */
    public void onAppCallBack(boolean isSuccess, JsonElement data, String type, String message) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("code", isSuccess ? SUCCESS : FAILED);
        paramMap.put("data", data);
        paramMap.put("type", type);
        paramMap.put("message", message);
        onAppCallBack(paramMap);
    }

    public void onAppCallBack(Map<String, Object> params) {
        JsonObject jsonObject = new Gson().toJsonTree(params).getAsJsonObject();
        String onAppCallBack = "javascript:onAppCallBack(\'%s\')";
        String jsonStr = (jsonObject == null) ? "" : jsonObject.toString();
        String formatJs = String.format(onAppCallBack, jsonStr);
        WebView webView = getBrowser().getWebView();
        if (webView != null) {
            webView.evaluateJavascript(formatJs, null);
        }
    }

    /**
     * Js 接口调用
     * @param method method
     * @param params params
     */
    public void callJsMethod(String method, String ...params) {
        String param = "'%s'";
        StringBuilder formatJs = new StringBuilder("javascript:if(window." + method + "){window." + method + "(");
        if (params == null || params.length == 0) {
            formatJs.append(");}");
        } else {
            for (int i = 0; i < params.length; i ++) {
                if (i == params.length - 1) {
                    formatJs.append(String.format(param, params[i]) + ");}");
                } else {
                    formatJs.append(String.format(param, params[i]) + ", ");
                }
            }
        }
        WebView webView = getBrowser().getWebView();
        if (webView != null) {
            webView.evaluateJavascript(formatJs.toString(), null);
        }
    }
}
