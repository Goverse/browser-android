package com.goverse.browser.presentation.error;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.goverse.browser.R;
import com.goverse.browser.Browser;
import com.goverse.browser.presentation.ErrorPresenter;

import static android.webkit.WebViewClient.ERROR_AUTHENTICATION;
import static android.webkit.WebViewClient.ERROR_BAD_URL;
import static android.webkit.WebViewClient.ERROR_CONNECT;
import static android.webkit.WebViewClient.ERROR_FAILED_SSL_HANDSHAKE;
import static android.webkit.WebViewClient.ERROR_FILE;
import static android.webkit.WebViewClient.ERROR_FILE_NOT_FOUND;
import static android.webkit.WebViewClient.ERROR_HOST_LOOKUP;
import static android.webkit.WebViewClient.ERROR_IO;
import static android.webkit.WebViewClient.ERROR_PROXY_AUTHENTICATION;
import static android.webkit.WebViewClient.ERROR_REDIRECT_LOOP;
import static android.webkit.WebViewClient.ERROR_TIMEOUT;
import static android.webkit.WebViewClient.ERROR_TOO_MANY_REQUESTS;
import static android.webkit.WebViewClient.ERROR_UNSAFE_RESOURCE;
import static android.webkit.WebViewClient.ERROR_UNSUPPORTED_AUTH_SCHEME;
import static android.webkit.WebViewClient.ERROR_UNSUPPORTED_SCHEME;
import static com.goverse.browser.Browser.ExtWebViewClient.ERROR_INVALID_URL;

public class GenericErrorPresenter implements ErrorPresenter {

    @Override
    public View present(Browser browser, int errorCode, String failingUrl) {

        View view = View.inflate(browser.getWebView().getContext(), R.layout.lib_browser_presenter_generic_error, null);

        ImageView animImageView = view.findViewById(R.id.iv_error);
        Drawable drawable = animImageView.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }

        Button btnRefresh = view.findViewById(com.goverse.browser.R.id.btn_refresh);
        btnRefresh.setVisibility(View.VISIBLE);
        btnRefresh.setOnClickListener(v -> {
            if (browser != null) {
                browser.refresh();
            }
        });

        TextView tvError = view.findViewById(R.id.tv_error);

        if (errorCode == ERROR_HOST_LOOKUP ||
                errorCode == ERROR_CONNECT){
            tvError.setText(R.string.lib_browser_webview_network_connect_failed);
        } else if (errorCode == ERROR_UNSUPPORTED_AUTH_SCHEME ||
                errorCode == ERROR_AUTHENTICATION ||
                errorCode == ERROR_PROXY_AUTHENTICATION ||
                errorCode == ERROR_IO ||
                errorCode == ERROR_REDIRECT_LOOP ||
                errorCode == ERROR_UNSUPPORTED_SCHEME ||
                errorCode == ERROR_FAILED_SSL_HANDSHAKE ||
                errorCode == ERROR_FILE ||
                errorCode == ERROR_FILE_NOT_FOUND ||
                errorCode == ERROR_TOO_MANY_REQUESTS ||
                errorCode == ERROR_UNSAFE_RESOURCE) {
            tvError.setText(R.string.lib_browser_webview_generic_error);
        } else if (errorCode == ERROR_TIMEOUT) {
            tvError.setText(R.string.lib_browser_webview_time_out);
        } else if (errorCode == ERROR_BAD_URL) {
            tvError.setText(R.string.lib_browser_webview_bad_url);
        } else if (errorCode == ERROR_INVALID_URL) {
            tvError.setText(R.string.lib_browser_webview_generic_error);
        }
        return view;
    }
}
