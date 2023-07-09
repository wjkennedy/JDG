// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.checker.compilermsgs.qual;

import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.InvisibleQualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@InvisibleQualifier
@SubtypeOf({})
@DefaultQualifierInHierarchy
public @interface UnknownCompilerMessageKey {
}