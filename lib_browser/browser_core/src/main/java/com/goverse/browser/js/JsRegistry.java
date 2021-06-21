package com.goverse.browser.js;

import android.annotation.SuppressLint;
import android.util.Log;
import android.webkit.WebView;
import com.goverse.browser.Browser;
import com.goverse.browser.js.function.JsLocale;
import com.goverse.browser.js.httpproxy.AjaxRouter;
import com.goverse.browser.js.httpproxy.DsBridge;
import com.goverse.browser.listener.BrowserAttachCallBack;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class JsRegistry {

    private final String TAG = getClass().getName();
    private List<Object> injectJsInterfaceList = new CopyOnWriteArrayList<>();
    private List<Object> rejectJsInterfaceList = new CopyOnWriteArrayList<>();

    private WeakReference<WebView> webViewWeakRef;
    private Browser browser;

    public JsRegistry(Browser browser) {

        this.browser = browser;
        this.webViewWeakRef = new WeakReference<>(this.browser.getWebView());
        // add Js Locale
        enableJsInterface(new JsLocale(this.browser.getWebView().getContext()), true);
    }

    public void registerJsInterfaces(List<Object> jsInterfaceList) {

        for (Object obj : jsInterfaceList) {
            Log.d(TAG, "obj is " + obj.getClass());
            enableJsInterface(obj, true);
        }
    }

    public void registerJsInterface(Object obj) {

        enableJsInterface(obj, true);
    }

    public void unRegisterJsInterface(Object obj) {

        enableJsInterface(obj, false);
    }

    public void unRegisterAllJsInterfaces() {
        for (Object obj : injectJsInterfaceList) {
            enableJsInterface(obj, false);
        }
    }

    public void recoveryAllJsInterfaces() {
        for (Object obj : rejectJsInterfaceList) {
            enableJsInterface(obj, true);
        }
    }

    public void enableHttpProxy(boolean enableHttpProxy) {
        if (enableHttpProxy) buildDsBridge();
    }

    private DsBridge buildDsBridge() {

        JsNameSpace ajaxRouter = AjaxRouter.class.getAnnotation(JsNameSpace.class);
        DsBridge dsBridge = new DsBridge(webViewWeakRef.get());
        dsBridge.getDSBridgeObjectMap().put(ajaxRouter.namespace(), new AjaxRouter());
        enableJsInterface(dsBridge, true);
        return dsBridge;
    }

    @SuppressLint("JavascriptInterface")
    private void enableJsInterface(Object obj, boolean enable) {
        JsNameSpace jsNameSpace = obj.getClass().getAnnotation(JsNameSpace.class);
        Log.d(TAG, "JsNameSpace is " + jsNameSpace);
        if (jsNameSpace != null) {
            String interfaceName = jsNameSpace.namespace();
            enableJsInterface(obj, interfaceName, enable);
        }
    }

    @SuppressLint("JavascriptInterface")
    private void enableJsInterface(Object obj, String interfaceName, boolean enable) {
        WebView webView = webViewWeakRef.get();
        if (webView == null) return;
        if (enable) {
            if (obj instanceof BrowserAttachCallBack) ((BrowserAttachCallBack)(obj)).onAttach(this.browser);
            webView.addJavascriptInterface(obj, interfaceName);
            if (!injectJsInterfaceList.contains(obj)) injectJsInterfaceList.add(obj);
            if (rejectJsInterfaceList.contains(obj)) rejectJsInterfaceList.remove(obj);
        } else {
            webView.removeJavascriptInterface(interfaceName);
            if (injectJsInterfaceList.contains(obj)) injectJsInterfaceList.remove(obj);
            if (!rejectJsInterfaceList.contains(obj)) rejectJsInterfaceList.add(obj);
        }
    }

}
