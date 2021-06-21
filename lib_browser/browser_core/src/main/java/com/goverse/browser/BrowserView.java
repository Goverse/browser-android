package com.goverse.browser;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BrowserView extends FrameLayout {

    public BrowserView(@NonNull Context context) {
        super(context);
    }

    public BrowserView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BrowserView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
