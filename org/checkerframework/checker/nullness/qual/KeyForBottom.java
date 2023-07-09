// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.checker.nullness.qual;

import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.InvisibleQualifier;
import org.checkerframework.framework.qual.TypeUseLocation;
import org.checkerframework.framework.qual.TargetLocations;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@TargetLocations({ TypeUseLocation.EXPLICIT_LOWER_BOUND, TypeUseLocation.EXPLICIT_UPPER_BOUND })
@InvisibleQualifier
@SubtypeOf({ KeyFor.class })
public @interface KeyForBottom {
}
