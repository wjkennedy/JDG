// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.common.returnsreceiver.qual;

import org.checkerframework.framework.qual.TypeUseLocation;
import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.LiteralKind;
import org.checkerframework.framework.qual.QualifierForLiterals;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@DefaultQualifierInHierarchy
@SubtypeOf({})
@QualifierForLiterals({ LiteralKind.NULL })
@DefaultFor({ TypeUseLocation.LOWER_BOUND })
public @interface UnknownThis {
}
