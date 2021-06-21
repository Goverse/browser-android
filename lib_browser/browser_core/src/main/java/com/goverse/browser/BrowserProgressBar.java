package com.goverse.browser;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BrowserProgressBar extends ProgressBar {

    public static final String TAG = "BrowserProgressBar";

    private Animation animation;

    public BrowserProgressBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAnimation();
    }

    public BrowserProgressBar(@NonNull Context context) {
        super(context);
        initAnimation();
    }

    private void initAnimation() {
        animation = new Animation() {};
        animation.setInterpolator(new ProgressBarInterpolator());
        animation.setDuration(5000);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.d(TAG, "onAnimationEnd");

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private class ProgressBarInterpolator implements Interpolator {
        @Override
        public float getInterpolation(float input) {

            float interpolation = 0;
            if (input <= 0.8f) {
                float x = 1.25f * input - 1.0f;
                interpolation = 0.8f * (x * x * x + 1.0f);
            } else {
                interpolation = (float)(Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
            }
            int progress = (int) (interpolation * 100);
            if (input != 0F) setProgress(progress);
            return interpolation;
        }
    }

    public void startLoading() {

        Log.d(TAG, "startLoading");
        setProgress(0);
        startAnimation(animation);
    }

    public void stopLoading() {

        Log.d(TAG, "stopLoading");
        Animation animation = getAnimation();
        if (animation != null) {
            animation.cancel();
        }
    }
}