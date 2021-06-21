package com.goverse.browser.js.httpproxy;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.goverse.browser.js.JsNameSpace;

import org.json.JSONException;
import org.json.JSONObject;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@JsNameSpace(namespace = "_dsbridge")
public class DsBridge {

    private final String TAG = DsBridge.class.getSimpleName();

    private Map<String, Object> mDSBridgeObjectMap = new HashMap<>();

    private WebView webView;

    public DsBridge(WebView webView) {
        this.webView = webView;
    }

    @JavascriptInterface
    public String call(String methodName, String argStr) {
        Log.d(TAG, "call---methodName: " + methodName + ",argStr: " + argStr);
        String[] nameStr = parseNamespace(methodName.trim());
        methodName = nameStr[1];
        Object jsb = mDSBridgeObjectMap.get(nameStr[0]);
        JSONObject ret = new JSONObject();
        try {
            ret.put("code", -1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (jsb == null) {
            Log.d(TAG, "Js bridge  called, but can't find a corresponded JavascriptInterface object , please check your code!");
            return ret.toString();
        }
        Object arg=null;
        Method method = null;
        String callback = null;

        try {
            JSONObject args = new JSONObject(argStr);
            if (args.has("_dscbstub")) {
                callback = args.getString("_dscbstub");
            }
            if(args.has("data")) {
                arg = args.get("data");
            }
        } catch (JSONException e) {
            Log.d(TAG, String.format("The argument of \"%s\" must be a JSON object string!", methodName));
            e.printStackTrace();
            return ret.toString();
        }

        Class<?> cls = jsb.getClass();
        boolean asyn = false;
        try {
            method = cls.getMethod(methodName,
                    new Class[]{JSONObject.class, CompletionHandler.class});
            asyn = true;
        } catch (Exception e) {
            try {
                method = cls.getMethod(methodName, new Class[]{Object.class});
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if (method == null) {
            Log.d(TAG, "Not find method \"" + methodName + "\" implementation! please check if the  signature or namespace of the method is right ");
            return ret.toString();
        }

        Object retData;
        method.setAccessible(true);
        try {
            if (asyn) {
                final String cb = callback;
                method.invoke(jsb, arg, new CompletionHandler<JSONObject>() {

                    @Override
                    public void onCompleted(JSONObject jsonObject) {
                        complete(jsonObject, true);
                    }

                    @Override
                    public void onProgress(int progress) {

                    }

                    @Override
                    public void onFailed(JSONObject jsonObject) {
                        complete(jsonObject, false);
                    }

                    private void complete(JSONObject retValue, boolean complete) {
                        try {
                            if (cb != null) {
                                String value = (retValue == null) ? "" : retValue.toString();
                                String script = String.format("javascript:%s(\'%s\')", cb, value);
                                webView.evaluateJavascript(script, null);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                retData = method.invoke(jsb, arg);
                ret.put("code", 0);
                ret.put("data", retData);
                return ret.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, String.format("Call failed：The parameter of \"%s\" in Java is invalid.", methodName));
            return ret.toString();
        }
        return ret.toString();
    }

    private String[] parseNamespace(String method) {
        int pos = method.lastIndexOf('.');
        String namespace = "";
        if (pos != -1) {
            namespace = method.substring(0, pos);
            method = method.substring(pos + 1);
        }
        return new String[]{namespace, method};
    }

    public Map<String, Object> getDSBridgeObjectMap() {
        return mDSBridgeObjectMap;
    }
}
