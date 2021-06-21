package com.goverse.browser.presentation;

import android.view.View;

import com.goverse.browser.Browser;

public interface ErrorPresenter {

    View present(Browser browser, int errorCode, String failingUrl);
}
