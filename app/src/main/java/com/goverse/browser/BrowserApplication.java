package com.goverse.browser;
import android.app.Application;
import android.os.Build;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BrowserApplication extends Application {

    public static final String TAG = "BrowserApplication";
    @Override
    public void onCreate() {
        super.onCreate();
        String currentProcessName = getCurrentProcessName();
        Log.d(TAG, "currentProcessName: " + currentProcessName);
        Browser.initApplication(getApplicationContext());
    }

    private String getCurrentProcessName() {
        if (Build.VERSION.SDK_INT >= 28)
            return Application.getProcessName();
        else {
            try {
                Class<?> activityThread = Class.forName("android.app.ActivityThread");
                Method getProcessName = activityThread.getDeclaredMethod("currentProcessName");
                return (String) getProcessName.invoke(null);
            } catch (ClassNotFoundException |
                    NoSuchMethodException |
                    IllegalAccessException |
                    InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
