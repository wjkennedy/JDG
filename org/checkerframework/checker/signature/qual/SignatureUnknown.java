// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.checker.signature.qual;

import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
@DefaultQualifierInHierarchy
@SubtypeOf({})
public @interface SignatureUnknown {
}
