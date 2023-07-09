// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.checker.lock.qual;

import org.checkerframework.framework.qual.InheritedAnnotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
@InheritedAnnotation
public @interface LockingFree {
}
