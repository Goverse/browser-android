package com.goverse.browser.presentation;

import android.view.View;

import com.goverse.browser.Browser;
import com.goverse.browser.presentation.error.GenericErrorPresenter;
import com.goverse.browser.presentation.error.NetworkErrorPresenter;

import static android.webkit.WebViewClient.ERROR_UNKNOWN;
import static android.webkit.WebViewClient.ERROR_UNSAFE_RESOURCE;
import static com.goverse.browser.Browser.ExtWebViewClient.ERROR_INVALID_URL;

public class ErrorPresentationProvider {

    /**
     * Interface used for creating presentation when an error occurred, all
     * based on errorCode defined in{@link Browser.ExtWebViewClient}
     * as follows:
     * Generic error {@link Browser.ExtWebViewClient#ERROR_UNKNOWN}
     * Server or proxy hostname lookup failed {@link Browser.ExtWebViewClient#ERROR_HOST_LOOKUP}
     * Unsupported authentication scheme (not basic or digest) {@link Browser.ExtWebViewClient#ERROR_UNSUPPORTED_AUTH_SCHEME}
     * User authentication failed on server {@link Browser.ExtWebViewClient#ERROR_AUTHENTICATION}
     * User authentication failed on proxy {@link Browser.ExtWebViewClient#ERROR_PROXY_AUTHENTICATION}
     * Failed to connect to the server {@link Browser.ExtWebViewClient#ERROR_CONNECT}
     * Failed to read or write to the server {@link Browser.ExtWebViewClient#ERROR_IO}
     * Connection timed out {@link Browser.ExtWebViewClient#ERROR_TIMEOUT}
     * Too many redirects {@link Browser.ExtWebViewClient#ERROR_REDIRECT_LOOP}
     * Unsupported URI scheme {@link Browser.ExtWebViewClient#ERROR_UNSUPPORTED_SCHEME}
     * Failed to perform SSL handshake {@link Browser.ExtWebViewClient#ERROR_FAILED_SSL_HANDSHAKE}
     * Malformed URL {@link Browser.ExtWebViewClient#ERROR_BAD_URL}
     * Generic file error {@link Browser.ExtWebViewClient#ERROR_FILE}
     * File not found {@link Browser.ExtWebViewClient#ERROR_FILE_NOT_FOUND}
     * Too many requests during this load {@link Browser.ExtWebViewClient#ERROR_TOO_MANY_REQUESTS}
     * Resource load was canceled by Safe Browsing error {@link Browser.ExtWebViewClient#ERROR_UNSAFE_RESOURCE}
     * Failed to load the url not in whitelist. {@link Browser.ExtWebViewClient#ERROR_INVALID_URL}
     * @param browser browser
     * @param errorCode errorCode
     * @param failingUrl failingUrl
     * @return View
     */
    public static View createErrorView(Browser browser, int errorCode, String failingUrl) {

        ErrorPresenter errorPresenter = null;
        if (isNetWorkError(errorCode)) errorPresenter = new NetworkErrorPresenter();
        else if (isGenericError(errorCode)) errorPresenter = new GenericErrorPresenter();
        if (errorPresenter != null) {
            return errorPresenter.present(browser, errorCode, failingUrl);
        }
        return null;
    }

    private static boolean isGenericError(int errorCode) {

        return ((errorCode >= ERROR_UNSAFE_RESOURCE && errorCode < ERROR_UNKNOWN) || errorCode == ERROR_INVALID_URL);
    }

    private static boolean isNetWorkError(int errorCode) {

        return (errorCode == ERROR_UNKNOWN);
    }
}
