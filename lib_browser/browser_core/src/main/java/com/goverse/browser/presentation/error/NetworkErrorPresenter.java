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

public class NetworkErrorPresenter implements ErrorPresenter {

    @Override
    public View present(Browser browser, int errorCode, String failingUrl) {

        View view = View.inflate(browser.getWebView().getContext(), R.layout.lib_browser_presenter_generic_error, null);
        TextView tvError = view.findViewById(R.id.tv_error);
        tvError.setText(R.string.lib_browser_webview_network_not_connected);
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
        return view;
    }
}
