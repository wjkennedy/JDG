// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.framework.qual;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;

@Documented
@Retention(RetentionPolicy.SOURCE)
public @interface CFComment {
    String[] value();
}
