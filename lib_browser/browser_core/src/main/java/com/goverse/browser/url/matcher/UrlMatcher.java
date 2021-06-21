package com.goverse.browser.url.matcher;

import android.text.TextUtils;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class UrlMatcher implements IMatcher{

    private final class UrlNode {

        public UrlNode(String nodeValue) {
            this.nodeValue = nodeValue;
        }

        public String nodeValue;
        public List<UrlNode> subNodeList = new ArrayList<>();
    }

    private final String TAG = UrlMatcher.class.getSimpleName();

    private List<UrlNode> mUrlNodeList = new ArrayList<>();

    private void addUrl(String ...urlSegs) {

        Log.d(TAG, "addUrl");
        List<UrlNode> urlNodeList = mUrlNodeList;
        for (int i = 0; i < urlSegs.length; i ++) {
            Log.d(TAG, "urlSeg: " + urlSegs[i]);
            boolean hasNode = false;
            for (int j = 0; j < urlNodeList.size(); j ++) {
                UrlNode urlNode = urlNodeList.get(j);
                if (urlNode.nodeValue.equalsIgnoreCase(urlSegs[i])){
                    urlNodeList = urlNode.subNodeList;
                    hasNode = true;
                    break;
                }
            }
            if (!hasNode) {
                UrlNode urlNode = new UrlNode(urlSegs[i]);
                urlNodeList.add(urlNode);
                urlNodeList = urlNode.subNodeList;
            }
        }
    }

    private boolean match(String ...urlSegs) {

        Log.d(TAG, "match");
        List<UrlNode> urlNodeList = mUrlNodeList;
        for (int i = 0; i < urlSegs.length; i ++) {

            Log.d(TAG, "match---urlSeg: " + urlSegs[i]);

            boolean isMatch = false;
            for (int j = 0; j < urlNodeList.size(); j ++) {
                UrlNode urlNode = urlNodeList.get(j);
                if (urlNode.nodeValue.equalsIgnoreCase(urlSegs[i])){
                    urlNodeList = urlNode.subNodeList;
                    isMatch = true;
                    break;
                }
            }
            if (!isMatch) return false;
        }
        return true;
    }

    public UrlMatcher(List<String> urlList) {
        Log.d(TAG, "UrlMatcher");
        if (urlList != null && urlList.size() > 0) {
            for (String urlStr : urlList) {
                try {
                    URL url = new URL(urlStr);
                    addUrl(url.getProtocol(), url.getHost());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }

    }

    @Override
    public boolean match(String urlStr) {
        Log.d(TAG, "match---url: " + urlStr);
        if (TextUtils.isEmpty(urlStr)) return false;
        URL url;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            Log.d(TAG, "url: " + urlStr + " is invalid!!!");
            e.printStackTrace();
            return false;
        }
        return match(url.getProtocol(), url.getHost());
    }
}
