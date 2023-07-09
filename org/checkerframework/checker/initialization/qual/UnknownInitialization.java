// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.checker.initialization.qual;

import org.checkerframework.framework.qual.TypeUseLocation;
import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.SubtypeOf;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@SubtypeOf({})
@DefaultFor({ TypeUseLocation.LOCAL_VARIABLE, TypeUseLocation.RESOURCE_VARIABLE })
public @interface UnknownInitialization {
    Class<?> value() default Object.class;
}
