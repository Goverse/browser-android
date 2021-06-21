package com.goverse.browser.url.matcher;

/**
 * Interface defination uesd for matching urls. You can add rules
 * to avoid loading web pages which you don't allow.
 */
public interface IMatcher {
    /**
     * Check if url is allowed to load, which means url will be load
     * only if it's matched.
     * @param urlStr urlStr
     * @return true: matched; false: not matched.
     */
    boolean match(String urlStr);
}
