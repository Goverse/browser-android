package com.goverse.browser.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.LocaleList;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.URLUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Locale;

public class BrowserUtils {

    /**
     * 判断是否有网络连接
     *
     * @param context
     * @return
     */
    public static boolean isNetworkEnable(Context context) {
        if (null == context){
            return false;
        }
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == connectivityManager) {
            return false;
        }
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (null == info) {
            return false;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            //判断连接网络是否可以真正上网
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            if (null == networkCapabilities) {
                return info.isAvailable() && info.isConnected();
            }
            boolean hasCapability = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            return info.isAvailable() && info.isConnected() && hasCapability;
        }
        return info.isAvailable() && info.isConnected();
    }



    public static String locale(Context context) {

        Locale locale = Locale.ENGLISH;
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

    /**
     * isNightMode
     * @param context application context
     * @return
     */
    public static boolean isNightMode(Context context) {
        boolean isNightMode = Configuration.UI_MODE_NIGHT_YES == (context.getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
        return isNightMode;
    }

    /**
     * 获取状态栏高度
     */
    private static int statusBarHeight;
    public static int getStatusBarHeight(Context context) {
        if (statusBarHeight > 0) {
            return statusBarHeight;
        }
        int result = dp2px(context, 20);
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        statusBarHeight = result;
        return statusBarHeight;
    }

    public static int dp2px(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static void changeStatusBarStyle(Activity activity, int color) {
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        View statusBarView = new View(activity);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight(activity));
        statusBarView.setBackgroundColor(color);
        decorView.addView(statusBarView, lp);
    }

    public static void adapterOppoStyle(Activity activity) {

        //OPPO手机5.x自定义的用来控制状态栏反色的
        final int SYSTEM_UI_FLAG_OP_STATUS_BAR_TINT = 0x00000010;
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        boolean nightMode = BrowserUtils.isNightMode(activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!nightMode) {
                option |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                option &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
        } else {
            if (!nightMode) {
                option |= SYSTEM_UI_FLAG_OP_STATUS_BAR_TINT;
            } else {
                option &= ~SYSTEM_UI_FLAG_OP_STATUS_BAR_TINT;
            }
        }
        decorView.setSystemUiVisibility(option);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
    }


    public static boolean isSameUrl(String url1, String url2) {
        try {
            if (!URLUtil.isNetworkUrl(url1) || !URLUtil.isNetworkUrl(url2)) return false;
            URI u1 = new URI(url1);
            URI u2 = new URI(url2);
            String s1 = u1.getScheme() + u1.getHost() + u1.getPath() + u1.getQuery();
            String s2 = u2.getScheme() + u2.getHost() + u2.getPath() + u2.getQuery();
            return s1.equalsIgnoreCase(s2);
        } catch (URISyntaxException e) {
        }
        return false;
    }

    public static String getDomainUrl(String url) {
        try {
            if (!URLUtil.isNetworkUrl(url)) return null;
            URI uri = new URI(url);
            return uri.getScheme() + "://" + uri.getHost();
        } catch (URISyntaxException e) {
        }
        return null;
    }

    public static boolean isUrlSameDomain(String domain, String url) {
        boolean isUrlSafe = URLUtil.isValidUrl(url) && url.startsWith(domain);
        if (isUrlSafe) return true;
        try {
            URI domainUri = new URI(domain);
            URI uri = new URI(url);
            return (domainUri.getScheme().equalsIgnoreCase(uri.getScheme()) &&
                    domainUri.getHost().equalsIgnoreCase(uri.getHost()));
        } catch (URISyntaxException e) {
        }
        return false;
    }

    public static String checkIfNeedUrlDecode(String content) {
        try {
            content = URLDecoder.decode(content);
        } catch (Exception e) {
        }
        return content;
    }

    public static String getUserAgent(Context context) {
        String userAgent = "Manufacturer/" + Build.MANUFACTURER.toLowerCase() + " " + "packageName/" + context.getApplicationContext().getPackageName();
        return userAgent;
    }
}
