// IBrowserBridge.aidl
package com.goverse.browser.process;
import com.goverse.browser.process.BrowserEventReceiver;

// Declare any non-default types here with import statements

interface IBrowserBridge {
    String dispatchCommand(in int command, in String[] args);
    void setEventReceiver(in BrowserEventReceiver browserEventReceiver);
}
