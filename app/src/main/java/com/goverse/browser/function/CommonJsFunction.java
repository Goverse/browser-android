package com.goverse.browser.function;

import android.content.Context;
import android.text.TextUtils;
import com.goverse.browser.js.JsResult;
import com.goverse.browser.process.BnBrowser;
import com.goverse.browser.process.BpBrowser;
import com.goverse.browser.process.JsFunction;
import org.json.JSONException;
import org.json.JSONObject;

public class CommonJsFunction extends JsFunction {

    public CommonJsFunction(Context context) {
        super(context);
    }

    @Override
    public JsResult onMethodInvoked(String callingUrl, String fromObj, String method, String param) {

        try {
            JSONObject paramJsonObject = null;
            if (!TextUtils.isEmpty(param)) {
                paramJsonObject = new JSONObject(param);
            }
            if ("getPackageName".equals(method)) {
                return JsResult.result(getPackageName());
            } else if ("onBackPressed".equals(method)) {
                BpBrowser.getInstance().sendCommand(BnBrowser.Command.GO_BACK, null);
                return JsResult.result(null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return JsResult.NOT_INVOKED;
    }

    // get app packageName
    private String getPackageName() {
        return context.getPackageName();
    }

}
