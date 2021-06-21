package com.goverse.browser.main.executors;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.LocaleList;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.goverse.browser.js.IJsExecutor;
import com.goverse.browser.js.JsResult;

import static android.app.Notification.EXTRA_CHANNEL_ID;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.provider.Settings.EXTRA_APP_PACKAGE;
import static com.goverse.browser.js.JsResult.result;

public final class SystemExecutor extends IJsExecutor {

    public static final String TAG = "SystemExecutor";
    private JsonParser jsonParser = new JsonParser();

    @Override
    public JsResult onMethodCall(Context context, String method, String param) {
        Log.i(TAG, "method: " + method + ",param: " + param);
        try {
            JsonObject paramJsonObject = null;
            if (!TextUtils.isEmpty(param)) {
                paramJsonObject = jsonParser.parse(param).getAsJsonObject();
            }
            // 获取 app 包名
            if ("getPackageName".equals(method)) {
                return result(true, getPackageName(context));
            }
            // 获取手机locale
            else if ("getLocale".equals(method)) {
                return result(getLocale(context));
            }
            // 是否为暗色模式
            else if ("isDarkMode".equals(method)) {
                return result(String.valueOf(isDarkMode(context)));
            }
            // 打开系统时间设置
            else if ("onOpenSystemTimeSetting".equals(method)) {
                openSystemTimeSetting(context);
                return JsResult.NO_RESULT;
            }
            // 在浏览器中打开
            else if ("openInBrowser".equals(method)) {
                if (paramJsonObject != null) {
                    String url = paramJsonObject.get("url").getAsString();
                    openInBrowser(context, url);
                }
                return JsResult.NO_RESULT;
            }
            else if ("onOpenSystemNotificationSetting".equals(method)) {
                openSystemNotificationSetting(context);
                return JsResult.NO_RESULT;
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        }
        return JsResult.NOT_INVOKED;
    }

    // get app packageName
    private String getPackageName(Context context) {
        return context.getPackageName();
    }

    // darkMode
    private boolean isDarkMode(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        int currentNightMode = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return Configuration.UI_MODE_NIGHT_YES == currentNightMode;
    }

    // Country + Language
    private String getLocale(Context context) {
        java.util.Locale locale =  java.util.Locale.ENGLISH;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList locales = context.getApplicationContext().getResources().getConfiguration().getLocales();
            if (locales != null && locales.size() > 0) {
                locale = locales.get(0);
            }
        } else {
            Configuration configuration = context.getApplicationContext().getResources().getConfiguration();
            if (configuration != null) {
                if (configuration.locale != null) {
                    locale = configuration.locale;
                }
            }
        }
        return locale.getLanguage() + "-" + locale.getCountry();
    }

    // open in browser
    private void openInBrowser(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // open settings of system time
    private void openSystemTimeSetting(Context context) {
        Intent intent = new Intent(Settings.ACTION_DATE_SETTINGS);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // system notification setting
    private void openSystemNotificationSetting(Context context) {

        try {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.putExtra(EXTRA_APP_PACKAGE, context.getPackageName());
                intent.putExtra(EXTRA_CHANNEL_ID, context.getApplicationInfo().uid);
            } else {
                intent.putExtra("app_package", context.getPackageName());
                intent.putExtra("app_uid", context.getApplicationInfo().uid);
            }
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri);
            context.startActivity(intent);
        }
    }
}
