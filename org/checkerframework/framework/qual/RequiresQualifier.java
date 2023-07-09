// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.framework.qual;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
@Repeatable(List.class)
public @interface RequiresQualifier {
    String[] expression();
    
    Class<? extends Annotation> qualifier();
    
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
    public @interface List {
        RequiresQualifier[] value();
    }
}
