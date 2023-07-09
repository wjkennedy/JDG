// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.common.aliasing.qual;

import org.checkerframework.framework.qual.TypeUseLocation;
import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.SubtypeOf;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_PARAMETER, ElementType.TYPE_USE })
@SubtypeOf({})
@DefaultQualifierInHierarchy
@DefaultFor(value = { TypeUseLocation.UPPER_BOUND, TypeUseLocation.LOWER_BOUND }, types = { Void.class })
public @interface MaybeAliased {
}