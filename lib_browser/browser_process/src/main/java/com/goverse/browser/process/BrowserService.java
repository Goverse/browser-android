package com.goverse.browser.process;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class BrowserService extends Service {

    public static final String TAG = "BrowserService";
    public static final String BROWSER_ACTIVITY_ACTION = "com.goverse.browser.activity";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        startBrowser(intent);
        return BnBrowser.getInstance();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind");
        super.onRebind(intent);
        startBrowser(intent);
    }

    private void startBrowser(Intent intent) {
        Log.d(TAG, "startBrowser");
        Intent browserActivityIntent = new Intent(intent);
        browserActivityIntent.setAction(BROWSER_ACTIVITY_ACTION);
        browserActivityIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        startActivity(browserActivityIntent);
    }

}
