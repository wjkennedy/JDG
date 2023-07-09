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
@Target({ ElementType.METHOD })
@InheritedAnnotation
@Repeatable(List.class)
public @interface EnsuresQualifierIf {
    String[] expression();
    
    Class<? extends Annotation> qualifier();
    
    boolean result();
    
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD })
    @InheritedAnnotation
    public @interface List {
        EnsuresQualifierIf[] value();
    }
}
