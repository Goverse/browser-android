package com.goverse.browser.js.httpproxy;

public interface CompletionHandler<T> {

    void onCompleted(T t);
    void onProgress(int progress);
    void onFailed(T t);
}
