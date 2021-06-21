package com.goverse.browser.js;

public final class JsResult {

    public static final class Code {
        public static final int SUCCESS = 200;
        public static final int FAILED = 201;
        // 请求调用的接口 不支持
        public static final int NOT_SUPPORT = 202;
    }

    public boolean hasInvoked;
    public String result;
    public static JsResult NOT_INVOKED = new JsResult(false, null);
    public static JsResult NO_RESULT = new JsResult(true, null);
    public static JsResult NOT_SUPPORT = new JsResult(false, "{\"code\":"+ Code.NOT_SUPPORT +"}");

    private JsResult(boolean hasInvoked, String result) {
        this.hasInvoked = hasInvoked;
        this.result = result;
    }

    public boolean isHasInvoked() {
        return hasInvoked;
    }

    public String getResult() {
        return result;
    }

    public static final JsResult result(boolean hasInvoked, String result) {
        return new JsResult(hasInvoked, result);
    }

    public static final JsResult result(String result) {
        return new JsResult(true, result);
    }
}
