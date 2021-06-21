package com.goverse.browser.process;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.webkit.URLUtil;

import com.goverse.browser.Browser;
import com.goverse.browser.js.JsResult;
import java.util.HashMap;
import java.util.Map;
import static com.goverse.browser.utils.BrowserUtils.getDomainUrl;

public class BpBrowser extends BrowserEventReceiver.Stub implements ServiceConnection {

    public static final class InterfaceHolder {
        public JsFunction jsFunction;
        public PageInterceptListener pageInterceptListener;
        public PageLoadListener pageLoadListener;
        public JsActionListener jsActionListener;
        public boolean hold = false;
    }

    public static final String BROWSER_SERVICE_ACTION = "com.goverse.browser.service";

    public static final String TAG = "BpBrowser";

    private Context context;

    private String userAgent;

    private IBrowserBridge browserBridge;

    public static final String MATCH_ALL_URL = "*";

    private Map<String, InterfaceHolder> urlInstanceMap = new HashMap<>();

    private BpBrowser() {}

    private static class Singleton {
        private static BpBrowser instance = new BpBrowser();
    }

    public static BpBrowser getInstance() {
        return Singleton.instance;
    }

    public void init(Context context, String userAgent) {
        Log.d(TAG, "init---userAgent: " + userAgent);
        this.context = context.getApplicationContext();
        this.userAgent = userAgent;
        Intent browserServiceIntent = getBrowserServiceIntent();
        context.startService(browserServiceIntent);
    }

    public void register(String url, JsFunction jsFunction) throws IllegalAccessException {
        Log.d(TAG, "register---url: " + url);
        register(url, jsFunction, null, null, null, false);
    }

    public void register(String url, JsFunction jsFunction, PageInterceptListener pageInterceptListener) throws IllegalAccessException {
        Log.d(TAG, "register---url: " + url);
        register(url, jsFunction, pageInterceptListener, null, null, false);
    }

    public void registerCommon(JsFunction jsFunction, PageInterceptListener pageInterceptListener, PageLoadListener pageLoadListener, JsActionListener jsActionListener) {
        try {
            register(MATCH_ALL_URL, jsFunction, pageInterceptListener, null, null, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void register(String url, JsFunction jsFunction, PageInterceptListener pageInterceptListener, PageLoadListener pageLoadListener, JsActionListener jsActionListener, boolean hold) throws IllegalAccessException {
        Log.d(TAG, "register---url: " + url);
        if ((!MATCH_ALL_URL.equals(url) && !URLUtil.isNetworkUrl(url))) throw new IllegalAccessException("Please register a valid network url !!!");
        String domainUrl = getDomainUrl(url);
        InterfaceHolder interfaceHolder = urlInstanceMap.get(domainUrl);
        if (interfaceHolder == null) interfaceHolder = new InterfaceHolder();
        interfaceHolder.jsFunction = (jsFunction == null ? interfaceHolder.jsFunction : jsFunction);
        interfaceHolder.pageInterceptListener = (pageInterceptListener == null ? interfaceHolder.pageInterceptListener : pageInterceptListener);
        interfaceHolder.pageLoadListener = (pageLoadListener == null ? interfaceHolder.pageLoadListener : pageLoadListener);
        interfaceHolder.jsActionListener = (jsActionListener == null ? interfaceHolder.jsActionListener : jsActionListener);
        interfaceHolder.hold = hold;
        urlInstanceMap.put(domainUrl, interfaceHolder);
    }

    private Intent getBrowserServiceIntent() {
        Intent intent = new Intent(BROWSER_SERVICE_ACTION);
        intent.setPackage("com.goverse.browser");
        return intent;
    }

    public void startBrowser(String url, BrowserSetting browserSetting) throws IllegalAccessException {
        Log.d(TAG, "startBrowser---url: " + url);
        startBrowser(url, browserSetting, null, null, null, null);
    }

    public void startBrowser(String url, BrowserSetting browserSetting, JsFunction jsFunction) throws IllegalAccessException {
        Log.d(TAG, "startBrowser---url: " + url);
        startBrowser(url, browserSetting, jsFunction, null, null, null);
    }

    public void startBrowser(String url, BrowserSetting browserSetting, JsFunction jsFunction, PageInterceptListener pageInterceptListener) throws IllegalAccessException {
        Log.d(TAG, "startBrowser---url: " + url);
        startBrowser(url, browserSetting, jsFunction, pageInterceptListener, null, null);
    }

    public void startBrowser(String url, BrowserSetting browserSetting, JsFunction jsFunction, PageInterceptListener pageInterceptListener, PageLoadListener pageLoadListener, JsActionListener jsActionListener) throws IllegalAccessException {
        Log.d(TAG, "startBrowser---url: " + url);
        register(url, jsFunction, pageInterceptListener, pageLoadListener, jsActionListener, false);
        Intent browserServiceIntent = getBrowserServiceIntent();
        browserServiceIntent.putExtra("url", url);
        browserSetting.setUserAgent(this.userAgent);
        browserServiceIntent.putExtra("browserSetting", browserSetting);
        context.bindService(browserServiceIntent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "onServiceConnected---name: " + name);
        this.browserBridge = IBrowserBridge.Stub.asInterface(service);
        try {
            this.browserBridge.setEventReceiver(this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "onServiceDisconnected---name: " + name);
        this.browserBridge = null;
    }

    public void evaluateJs(String javaScript) {
        sendCommand(BnBrowser.Command.EVALUATE_JS, new String[] { javaScript });
    }

    public void sendCommand(int command, String[] args) {

        Log.d(TAG, "sendCommand---command: " + command);
        if (this.browserBridge != null) {
            try {
                this.browserBridge.dispatchCommand(command, args);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public String sendCommand(int command, String[] args, String fromObj, String replyTo) {

        Log.d(TAG, "sendCommand---command: " + command + ", fromObj: " + fromObj + ", replyTo: " + replyTo);
        if (this.browserBridge != null) {
            try {
                String[] newArgs = new String[args.length + 2];
                System.arraycopy(args, 0, newArgs, 0, args.length);
                newArgs[args.length] = fromObj;
                newArgs[args.length + 1] = replyTo;
                return this.browserBridge.dispatchCommand(command, newArgs);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public String onReceive(BrowserEvent browserEvent) throws RemoteException {

        try {
            int event = browserEvent.event;
            String url = browserEvent.url;
            String[] params = browserEvent.params;

            Log.d(TAG, "onReceive---event: " + event + (params == null ? "" : ", param: " + params[0]) + ", url: " + url);

            String domainUrl = getDomainUrl(url);
            if (event == BnBrowser.Event.CLOSED) {
                // remove interface after page finished
                if (urlInstanceMap.containsKey(domainUrl)) urlInstanceMap.remove(domainUrl);
                if (this.browserBridge != null) {
                    this.browserBridge.setEventReceiver(null);
                }
                context.unbindService(this);
                return null;
            }

            InterfaceHolder interfaceHolder = urlInstanceMap.get(domainUrl);
            PageLoadListener pageLoadListener = (interfaceHolder == null ? null : interfaceHolder.pageLoadListener);
            PageInterceptListener pageInterceptListener = (interfaceHolder == null ? null : interfaceHolder.pageInterceptListener);
            JsActionListener jsActionListener = (interfaceHolder == null ? null : interfaceHolder.jsActionListener);
            JsFunction jsFunction = (interfaceHolder == null ? null : interfaceHolder.jsFunction);

            InterfaceHolder commonInterfaceHolder = urlInstanceMap.get(MATCH_ALL_URL);
            PageInterceptListener commonPageInterceptListener = (commonInterfaceHolder == null ? null : commonInterfaceHolder.pageInterceptListener);
            JsFunction commonJsFunction = (commonInterfaceHolder == null ? null : commonInterfaceHolder.jsFunction);

            if (pageLoadListener != null) {
                if (event == BnBrowser.Event.PAGE_PRELOAD) pageLoadListener.onPagePreLoad(url);
                else if (event == BnBrowser.Event.PAGE_START) pageLoadListener.onPageStart(url);
                else if (event == BnBrowser.Event.PAGE_FINISHED) pageLoadListener.onPageFinished(url);
                else if (event == BnBrowser.Event.LOAD_PROGRESS) pageLoadListener.onLoadProgress(Integer.parseInt(params[0]));
                else if (event == BnBrowser.Event.RECEIVED_ERROR) pageLoadListener.onReceivedError(params[0], Integer.parseInt(params[1]), params[2]);
            }

            if (jsActionListener != null) {
                if (event == BnBrowser.Event.JS_ALERT) jsActionListener.onJsAlert(params[0], params[1]);
                else if (event == BnBrowser.Event.JS_CONFIRM) jsActionListener.onJsConfirm(params[0], params[1]);
                else if (event == BnBrowser.Event.JS_PROMPT) jsActionListener.onJsPrompt(params[0], params[1], params[2]);
            }

            if (event == BnBrowser.Event.PAGE_INTERCEPT) {
                boolean res = false;
                String loadingUrl = params[0] == null ? null : params[0];
                if(pageInterceptListener != null) res = pageInterceptListener.onPageIntercept(url, loadingUrl);
                if (!res && commonPageInterceptListener!= null) res = commonPageInterceptListener.onPageIntercept(url, loadingUrl);
                return String.valueOf(res);

            } else if (event == BnBrowser.Event.METHOD_INVOKED) {
                String method = params[0] == null ? null : params[0];
                String param = (params.length == 1) ? null : params[1];
                String fromObj = browserEvent.fromObj;

                JsResult jsResult = null;
                if (jsFunction != null) jsResult = jsFunction.onMethodInvoked(url, fromObj, method, param);
                if (jsResult != null && !jsResult.hasInvoked && commonJsFunction != null) {
                    jsResult = commonJsFunction.onMethodInvoked(url, fromObj, method, param);
                    if (jsResult.hasInvoked) {
                        return jsResult.getResult();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
