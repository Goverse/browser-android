package com.goverse.browser.js;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  Marks the class as a Native Interface Object,
 *  which can be invoked by javascript.
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface JsNameSpace {
    String namespace();
}
