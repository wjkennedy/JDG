// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.checker.fenum.qual;

import org.checkerframework.framework.qual.TypeUseLocation;
import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.SubtypeOf;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
@SubtypeOf({ FenumTop.class })
@DefaultQualifierInHierarchy
@DefaultFor({ TypeUseLocation.EXCEPTION_PARAMETER })
public @interface FenumUnqualified {
}
