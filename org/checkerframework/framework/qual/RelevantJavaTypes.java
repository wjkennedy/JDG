// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.framework.qual;

import java.lang.annotation.Inherited;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Inherited
public @interface RelevantJavaTypes {
    Class<?>[] value();
}
