// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.checker.signature.qual;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;
import org.checkerframework.framework.qual.SubtypeOf;

@SubtypeOf({ DotSeparatedIdentifiers.class, BinaryNameWithoutPackage.class, IdentifierOrPrimitiveType.class })
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
public @interface Identifier {
}
