package com.goverse.browser;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
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

        RadioGroup rgTheme = findViewById(R.id.rg_theme);
        RadioGroup rgProcess = findViewById(R.id.rg_process);
        EditText etUrl = findViewById(R.id.et_url);
        Button btnEnter = findViewById(R.id.btn_enter);

        btnEnter.setOnClickListener(v -> {
            int theme = Browser.Theme.NORMAL;
            int checkedThemeId = rgTheme.getCheckedRadioButtonId();
            int checkedProcessId = rgProcess.getCheckedRadioButtonId();
            if (checkedThemeId == R.id.rb_full) theme = Browser.Theme.FULL_SCREEN;
            if (checkedThemeId == R.id.rb_screen) theme = Browser.Theme.SCREEN;
            openBrowser(etUrl.getText().toString(), theme, checkedProcessId == R.id.rb_main ? true: false);
        });
    }

    private void openBrowser(String url, int theme, boolean isMainProcess) {

        if (isMainProcess) {
            Intent intent = new Intent(this, BrowserMainActivity.class);
            intent.putExtra("url", url);
            intent.putExtra("theme", theme);
            startActivity(intent);
        } else {
            startSubProcessBrowser(url, new BrowserSetting.Builder().theme(theme).build());
        }
    }


    private void startSubProcessBrowser(String url, BrowserSetting browserSetting) {
        BrowserInitializer.initSubProcess(getApplicationContext());
        try {
            BpBrowser.getInstance().startBrowser(url, browserSetting, new BrowserJsFunction(getApplicationContext()), new BrowserPageInterceptListener(getApplicationContext()));
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