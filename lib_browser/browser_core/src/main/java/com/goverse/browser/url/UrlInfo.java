package com.goverse.browser.url;

import androidx.annotation.NonNull;

import java.util.LinkedList;
import java.util.List;

/**
 * Url信息节点，保存Url类型，地址，以及子节点。
 */
public class UrlInfo {

    /**
     * Flag indicate that this url is showed by loading data which has to be html well-formed.
     * {@link android.webkit.WebView#loadData(String data, String mimeType, String encoding)}
     * {@link android.webkit.WebView#loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String failUrl)}
     */
    public static final int LOAD_DATA = 100;

    /**
     * parent url
     */
    private UrlInfo parentUrlInfo;

    /**
     * list of sub urls which opened by current url.
     */
    private List<UrlInfo> subUrlInfoList = new LinkedList<>();

    /**
     * url
     */
    private String url;

    /**
     * url extra，related to HitTestResult.getExtra()
     * {@link android.webkit.WebView.HitTestResult#getExtra()}
     */
    private String extra;

    /**
     * url type，related to HitTestResult.getType()
     * {@link android.webkit.WebView.HitTestResult#getType()}
     */
    private int type;

    public UrlInfo(String url) {
        this.url = url;
    }

    public UrlInfo(String url, int type, String extra) {
        this.url = url;
        this.type = type;
        this.extra = extra;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setParentUrlInfo(UrlInfo parentUrlInfo) {
        this.parentUrlInfo = parentUrlInfo;
    }

    public void addSubInfo(UrlInfo urlInfo) {
        this.subUrlInfoList.add(0, urlInfo);
    }

    public void removeSubInfo(UrlInfo urlInfo) {
        if (subUrlInfoList.contains(urlInfo)) {
            subUrlInfoList.remove(urlInfo);
        }
    }

    public List<UrlInfo> getSubUrlInfoList() {
        return subUrlInfoList;
    }

    public UrlInfo getParentUrlInfo() {
        return parentUrlInfo;
    }

    @NonNull
    @Override
    public String toString() {
        return "UrlInfo---url: " + this.getUrl() + ", type: " + this.getType() + ", extra: " + this.getExtra();
    }
}
