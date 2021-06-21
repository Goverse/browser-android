package com.goverse.browser;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef(value = { Browser.Theme.NORMAL, Browser.Theme.FULL_SCREEN, Browser.Theme.SCREEN})
@Retention(RetentionPolicy.SOURCE)
public @interface BrowserTheme {
}