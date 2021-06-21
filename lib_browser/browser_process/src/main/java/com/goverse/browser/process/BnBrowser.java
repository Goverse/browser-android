package com.goverse.browser.process;

import android.os.RemoteException;
import android.util.Log;

public class BnBrowser extends IBrowserBridge.Stub {

    public static final String TAG = "BnBrowser";

    public static final class Event {
        public static final int METHOD_INVOKED = 0x001;
        public static final int PAGE_INTERCEPT = 0x002;
        public static final int PAGE_PRELOAD = 0x003;
        public static final int PAGE_START = 0x004;
        public static final int PAGE_FINISHED = 0x005;
        public static final int LOAD_PROGRESS = 0x006;
        public static final int RECEIVED_ERROR = 0x007;
        public static final int JS_ALERT = 0x008;
        public static final int JS_CONFIRM = 0x009;
        public static final int JS_PROMPT = 0x00A;
        public static final int CLOSED = 0x00B;
        public static final int REFRESH = 0x00C;
        public static final int HTTP_PROXY = 0x00D;

    }

    public static final class Command {

        public static final int LOAD_URL = 0x100;
        public static final int LOAD_DATA = 0x101;
        public static final int GO_FORWARD = 0x102;
        public static final int GO_BACK = 0x103;
        public static final int REFRESH = 0x104;
        public static final int EVALUATE_JS = 0x105;
        public static final int CLOSE = 0x106;
        public static final int CAPTURE = 0x107;
        public static final int CURRENT_URL = 0x108;
        public static final int CLEAR_MEMORY_FILE = 0x109;
        public static final int SCREEN_LANDSCAPE = 0x10A;
        public static final int SCREEN_PORTRAIT = 0x10B;
    }

    public interface OnCommandListener {
        String onCommandDispatch(int command, String[] args);
    }

    private OnCommandListener onCommandListener;

    private BrowserEventReceiver browserEventReceiver;

    @Override
    public String dispatchCommand(int command, String[] args) throws RemoteException {
        Log.d(TAG, "dispatchCommand---command: " + command );
        if (onCommandListener != null) return onCommandListener.onCommandDispatch(command, args);
        return null;
    }

    @Override
    public void setEventReceiver(BrowserEventReceiver browserEventReceiver) throws RemoteException {
        this.browserEventReceiver = browserEventReceiver;
    }

    public String sendBrowserEvent(BrowserEvent browserEvent) {

        Log.d(TAG, "sendBrowserEvent---event: " + browserEvent.event + ",url: " + browserEvent.url);
        if (this.browserEventReceiver != null) {
            try {
                return this.browserEventReceiver.onReceive(browserEvent);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private BnBrowser() {}

    private static class Singleton {
        private static BnBrowser instance = new BnBrowser();
    }

    public static BnBrowser getInstance() {
        return Singleton.instance;
    }

    public void setOnCommandListener(OnCommandListener onCommandListener) {
        this.onCommandListener = onCommandListener;
    }
}
