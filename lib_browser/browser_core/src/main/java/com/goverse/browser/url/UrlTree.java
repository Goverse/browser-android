package com.goverse.browser.url;

import android.util.Log;

import java.util.List;

/**
 * A data structure for saving the history of exploring website.
 */
public class UrlTree {

    /**
     * root urlInfo
     */
    private UrlInfo mRootUrlInfo;

    /**
     * current urlInfo
     */
    private UrlInfo mCurrentUrlInfo;

    private final String TAG = UrlTree.class.getSimpleName();

    /**
     * 打开新的Url
     * @param urlInfo
     */
    public void push(UrlInfo urlInfo) {
        Log.d(TAG, "push: " + urlInfo);
        if (urlInfo == null) return;
        if (mRootUrlInfo == null) {
            mRootUrlInfo = urlInfo;
            mCurrentUrlInfo = mRootUrlInfo;
        } else {
            List<UrlInfo> subUrlInfoList = mCurrentUrlInfo.getSubUrlInfoList();
            urlInfo.setParentUrlInfo(mCurrentUrlInfo);
            if (!subUrlInfoList.contains(urlInfo)) {
                mCurrentUrlInfo.addSubInfo(urlInfo);
            }
            mCurrentUrlInfo = urlInfo;
        }
    }

    /**
     * next
     */
    public void next() {
        Log.d(TAG, "next");
        if (mCurrentUrlInfo != null) {
            List<UrlInfo> subUrlInfoList = mCurrentUrlInfo.getSubUrlInfoList();
            if (subUrlInfoList != null) {
                mCurrentUrlInfo = subUrlInfoList.get(0);
            }
        }
        Log.d(TAG, "next---:getUrl " + (mCurrentUrlInfo == null ? " " : mCurrentUrlInfo.getUrl()));

    }

    /**
     * pre
     */
    public void pre() {
        Log.d(TAG, "pre");
        if (mCurrentUrlInfo != null) {
            UrlInfo parentUrlInfo = mCurrentUrlInfo.getParentUrlInfo();
            if (parentUrlInfo != null) {
                parentUrlInfo.removeSubInfo(mCurrentUrlInfo);
                parentUrlInfo.addSubInfo(mCurrentUrlInfo);
                mCurrentUrlInfo = parentUrlInfo;
            }
        }
        Log.d(TAG, "pre---:getUrl " + (mCurrentUrlInfo == null ? " " : mCurrentUrlInfo.getUrl()));
    }

    /**
     * get currentUrlInfo
     * @return currentUrlInfo
     */
    public UrlInfo getCurrentUrlInfo() {
        return mCurrentUrlInfo;
    }

}
