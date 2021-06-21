package com.goverse.browser;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.goverse.browser.R;
import com.goverse.browser.js.JsResult;
import com.goverse.browser.main.BrowserMainActivity;
import com.goverse.browser.process.BpBrowser;
import com.goverse.browser.process.BrowserSetting;
import com.goverse.browser.process.JsFunction;
import com.goverse.browser.process.PageInterceptListener;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_main).setOnClickListener(v -> { startActivity(new Intent(this, BrowserMainActivity.class)); });
        findViewById(R.id.btn_process).setOnClickListener(v -> { startSubProcessBrowser(); });
    }

    private void startSubProcessBrowser() {
        BrowserInitializer.initSubProcess(getApplicationContext());
        try {
            BpBrowser.getInstance().startBrowser("https://www.baidu.com", BrowserSetting.FULLSCREEN, new BrowserJsFunction(getApplicationContext()), new BrowserPageInterceptListener(getApplicationContext()));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static class BrowserJsFunction extends JsFunction {

        public BrowserJsFunction(Context context) {
            super(context);
        }

        @Override
        public JsResult onMethodInvoked(String callingUrl, String fromObj, String method, String param) {
            Log.d(TAG, "onMethodInvoked: " + callingUrl);
            return null;
        }
    }

    public static class BrowserPageInterceptListener extends PageInterceptListener {
        public BrowserPageInterceptListener(Context context) {
            super(context);
        }
        @Override
        public boolean onPageIntercept(String currentUrl, String loadingUrl) {
            Log.d(TAG, "currentUrl: " + currentUrl);
            return false;
        }
    }

}