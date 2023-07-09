// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.checker.tainting.qual;

import org.checkerframework.framework.qual.PolymorphicQualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@PolymorphicQualifier(Tainted.class)
public @interface PolyTainted {
}
