// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.checker.signedness.qual;

import org.checkerframework.framework.qual.TypeKind;
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
@SubtypeOf({ UnknownSignedness.class })
@DefaultFor(typeKinds = { TypeKind.BYTE, TypeKind.INT, TypeKind.LONG, TypeKind.SHORT, TypeKind.FLOAT, TypeKind.DOUBLE, TypeKind.CHAR })
public @interface Signed {
}
