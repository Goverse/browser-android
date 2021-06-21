package com.goverse.browser.js.function;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.LocaleList;
import android.webkit.JavascriptInterface;

import com.goverse.browser.js.JsNameSpace;

import java.util.Locale;

@JsNameSpace(namespace = "AppLocale")
public class JsLocale {

    private String TAG = JsLocale.class.getSimpleName();
    private final Locale LOCALE_EN = Locale.ENGLISH;
    private Context mContext;

    public JsLocale(Context context) {
        mContext = context;
    }

    /**
     * get language identifier based on IETF BCP 47
     * @return language identifier
     */
    @JavascriptInterface
    public String onFetchLocale() {

        Locale locale = LOCALE_EN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList locales = mContext.getApplicationContext().getResources().getConfiguration().getLocales();
            if (locales != null && locales.size() > 0) {
                locale = locales.get(0);
            }
        } else {
            Configuration configuration = mContext.getApplicationContext().getResources().getConfiguration();
            if (configuration != null) {
                if (configuration.locale != null) {
                    locale = configuration.locale;
                }
            }
        }
        return locale.getLanguage() + "-" + locale.getCountry();
    }
}
