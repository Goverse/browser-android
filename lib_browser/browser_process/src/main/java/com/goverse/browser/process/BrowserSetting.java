package com.goverse.browser.process;

import android.os.Parcel;
import android.os.Parcelable;
import com.goverse.browser.Browser;

public class BrowserSetting implements Parcelable {

    public static BrowserSetting NORMAL = new Builder().theme(Browser.Theme.NORMAL).build();

    public static BrowserSetting FULLSCREEN = new Builder().theme(Browser.Theme.FULL_SCREEN).build();

    private BrowserSetting(Builder builder) {
        this.adoptScreen = builder.adoptScreen;
        this.theme = builder.theme;
        this.fixedTitle = builder.fixedTitle;
        this.whiteList = builder.whiteList;
        this.enableHttpProxy = builder.enableHttpProxy;
        this.supportZoom = builder.supportZoom;
        this.supportDarkMode = builder.supportDarkMode;
        this.enableJavaScript = builder.enableJavaScript;
        this.enableCache = builder.enableCache;
        this.allowFileAccess = builder.allowFileAccess;
        this.timeOut = builder.timeOut;
        this.builder = builder;
    }

    public Builder newBuilder() {
        return builder;
    }

    private Builder builder;

    private int theme;

    private int timeOut;

    private String fixedTitle;

    private String[] whiteList;

    private boolean enableHttpProxy;

    private boolean adoptScreen;

    private boolean supportZoom;

    private boolean supportDarkMode;

    private boolean enableJavaScript;

    private boolean enableCache;

    private boolean allowFileAccess;

    private String userAgent;

    private String[] javascriptObjects;

    void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public int getTheme() {
        return theme;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public String[] getWhiteList() {
        return whiteList;
    }

    public String getFixedTitle() {
        return fixedTitle;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public boolean isAdoptScreen() {
        return adoptScreen;
    }

    public boolean isAllowFileAccess() {
        return allowFileAccess;
    }

    public boolean isEnableCache() {
        return enableCache;
    }

    public boolean isEnableHttpProxy() {
        return enableHttpProxy;
    }

    public boolean isEnableJavaScript() {
        return enableJavaScript;
    }

    public boolean isSupportDarkMode() {
        return supportDarkMode;
    }

    public boolean isSupportZoom() {
        return supportZoom;
    }

    public String[] getJavascriptObjects() {
        return javascriptObjects;
    }

    protected BrowserSetting(Parcel in) {

        this.theme = in.readInt();
        this.timeOut = in.readInt();
        this.fixedTitle = in.readString();
        this.userAgent = in.readString();
        if (this.whiteList != null)  in.readStringArray(this.whiteList);
        this.adoptScreen = in.readInt() == 0 ? false : true;
        this.allowFileAccess = in.readInt() == 0 ? false : true;
        this.enableCache = in.readInt() == 0 ? false : true;
        this.enableJavaScript = in.readInt() == 0 ? false : true;
        this.supportDarkMode = in.readInt() == 0 ? false : true;
        this.enableHttpProxy = in.readInt() == 0 ? false : true;
        this.supportZoom = in.readInt() == 0 ? false : true;
        if (this.javascriptObjects != null)  in.readStringArray(this.javascriptObjects);

    }

    public static final Creator<BrowserSetting> CREATOR = new Creator<BrowserSetting>() {
        @Override
        public BrowserSetting createFromParcel(Parcel in) {
            return new BrowserSetting(in);
        }

        @Override
        public BrowserSetting[] newArray(int size) {
            return new BrowserSetting[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(this.theme);
        dest.writeInt(this.timeOut);
        dest.writeString(this.fixedTitle);
        dest.writeString(this.userAgent);
        if (this.whiteList != null) dest.readStringArray(this.whiteList);
        dest.writeInt(this.adoptScreen ? 1 : 0);
        dest.writeInt(this.allowFileAccess ? 1 : 0);
        dest.writeInt(this.enableCache ? 1 : 0);
        dest.writeInt(this.enableJavaScript ? 1 : 0);
        dest.writeInt(this.supportDarkMode ? 1 : 0);
        dest.writeInt(this.enableHttpProxy ? 1 : 0);
        dest.writeInt(this.supportZoom ? 1 : 0);
        if (this.javascriptObjects != null) dest.readStringArray(this.javascriptObjects);
    }

    public static class Builder {

        private int theme;

        private String fixedTitle;

        private String[] whiteList;

        private boolean enableHttpProxy = false;

        private boolean adoptScreen = true;

        private boolean supportZoom = true;

        private boolean supportDarkMode = true;

        private boolean enableJavaScript = true;

        private boolean enableCache = true;

        private boolean allowFileAccess = false;

        private int timeOut = 0;

        private String[] javascriptObjects;

        public Builder theme(int theme) {
            this.theme = theme;
            return this;
        }

        public Builder fixedTitle(String fixedTitle) {
            this.fixedTitle = fixedTitle;
            return this;
        }

        public Builder whiteList(String[] whiteList) {
            this.whiteList = whiteList;
            return this;
        }

        public Builder enableHttpProxy(boolean enableHttpProxy) {
            this.enableHttpProxy = enableHttpProxy;
            return this;
        }

        public Builder adoptScreen(boolean adoptScreen) {
            this.adoptScreen = adoptScreen;
            return this;
        }

        public Builder supportZoom(boolean supportZoom) {
            this.supportZoom = supportZoom;
            return this;
        }

        public Builder supportDarkMode(boolean supportDarkMode) {
            this.supportDarkMode = supportDarkMode;
            return this;
        }

        public Builder enableJavaScript(boolean enableJavaScript) {
            this.enableJavaScript = enableJavaScript;
            return this;
        }

        public Builder enableCache(boolean enableCache) {
            this.enableCache = enableCache;
            return this;
        }

        public Builder allowFileAccess(boolean allowFileAccess) {
            this.allowFileAccess = allowFileAccess;
            return this;
        }

        public Builder timeOut(int timeOut) {
            this.timeOut = timeOut;
            return this;
        }

        public Builder javascriptObjects(String[] javascriptObjects) {
            this.javascriptObjects = javascriptObjects;
            return this;
        }

        public BrowserSetting build() {
            return new BrowserSetting(this);
        }
    }
}
