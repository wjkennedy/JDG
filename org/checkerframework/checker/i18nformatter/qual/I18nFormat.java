// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.checker.i18nformatter.qual;

import org.checkerframework.framework.qual.SubtypeOf;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@SubtypeOf({ I18nUnknownFormat.class })
public @interface I18nFormat {
    I18nConversionCategory[] value();
}
