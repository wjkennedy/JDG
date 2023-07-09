// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.checker.units.qual;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface UnitsRelations {
    Class<?> value();
}
