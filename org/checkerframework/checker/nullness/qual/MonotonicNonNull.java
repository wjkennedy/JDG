// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.checker.nullness.qual;

import org.checkerframework.framework.qual.MonotonicQualifier;
import org.checkerframework.framework.qual.SubtypeOf;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE })
@SubtypeOf({ Nullable.class })
@MonotonicQualifier(NonNull.class)
public @interface MonotonicNonNull {
}
