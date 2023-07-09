// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.common.subtyping.qual;

import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.InvisibleQualifier;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;

@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({})
@InvisibleQualifier
@SubtypeOf({})
public @interface Unqualified {
}
