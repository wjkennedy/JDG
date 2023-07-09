// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.common.value.qual;

import org.checkerframework.framework.qual.PolymorphicQualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@PolymorphicQualifier(UnknownVal.class)
public @interface PolyValue {
}
