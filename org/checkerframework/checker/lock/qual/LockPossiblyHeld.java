// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.checker.lock.qual;

import org.checkerframework.framework.qual.LiteralKind;
import org.checkerframework.framework.qual.QualifierForLiterals;
import org.checkerframework.framework.qual.TypeUseLocation;
import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.InvisibleQualifier;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
@InvisibleQualifier
@SubtypeOf({})
@DefaultQualifierInHierarchy
@DefaultFor(value = { TypeUseLocation.LOWER_BOUND }, types = { Void.class })
@QualifierForLiterals({ LiteralKind.NULL })
public @interface LockPossiblyHeld {
}
