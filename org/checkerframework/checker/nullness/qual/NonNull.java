// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.checker.nullness.qual;

import org.checkerframework.framework.qual.TypeKind;
import org.checkerframework.framework.qual.UpperBoundFor;
import org.checkerframework.framework.qual.TypeUseLocation;
import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.LiteralKind;
import org.checkerframework.framework.qual.QualifierForLiterals;
import org.checkerframework.framework.qual.SubtypeOf;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@SubtypeOf({ MonotonicNonNull.class })
@QualifierForLiterals({ LiteralKind.STRING })
@DefaultQualifierInHierarchy
@DefaultFor({ TypeUseLocation.EXCEPTION_PARAMETER })
@UpperBoundFor(typeKinds = { TypeKind.PACKAGE, TypeKind.INT, TypeKind.BOOLEAN, TypeKind.CHAR, TypeKind.DOUBLE, TypeKind.FLOAT, TypeKind.LONG, TypeKind.SHORT, TypeKind.BYTE })
public @interface NonNull {
}
