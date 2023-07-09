// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.checker.i18nformatter.qual;

import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.SubtypeOf;
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
@SubtypeOf({ I18nFormat.class, I18nInvalidFormat.class, I18nFormatFor.class })
@DefaultFor({ TypeUseLocation.LOWER_BOUND })
public @interface I18nFormatBottom {
}
