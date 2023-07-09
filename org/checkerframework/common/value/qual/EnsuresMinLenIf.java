// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.common.value.qual;

import org.checkerframework.framework.qual.QualifierArgument;
import java.lang.annotation.Repeatable;
import org.checkerframework.framework.qual.InheritedAnnotation;
import org.checkerframework.framework.qual.ConditionalPostconditionAnnotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
@ConditionalPostconditionAnnotation(qualifier = MinLen.class)
@InheritedAnnotation
@Repeatable(List.class)
public @interface EnsuresMinLenIf {
    String[] expression();
    
    boolean result();
    
    @QualifierArgument("value")
    int targetValue() default 0;
    
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
    @ConditionalPostconditionAnnotation(qualifier = MinLen.class)
    @InheritedAnnotation
    public @interface List {
        EnsuresMinLenIf[] value();
    }
}
