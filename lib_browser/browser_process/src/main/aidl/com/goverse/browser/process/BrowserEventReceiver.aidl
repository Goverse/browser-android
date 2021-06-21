// BrowserEventReceiver.aidl
package com.goverse.browser.process;
import com.goverse.browser.process.BrowserEvent;

// Declare any non-default types here with import statements

interface BrowserEventReceiver {
    String onReceive(in BrowserEvent browserEvent);
}
