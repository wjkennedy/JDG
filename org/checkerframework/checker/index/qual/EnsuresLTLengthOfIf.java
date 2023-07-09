// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.checker.index.qual;

import org.checkerframework.framework.qual.QualifierArgument;
import org.checkerframework.framework.qual.JavaExpression;
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
@ConditionalPostconditionAnnotation(qualifier = LTLengthOf.class)
@InheritedAnnotation
@Repeatable(List.class)
public @interface EnsuresLTLengthOfIf {
    String[] expression();
    
    boolean result();
    
    @JavaExpression
    @QualifierArgument("value")
    String[] targetValue();
    
    @JavaExpression
    @QualifierArgument("offset")
    String[] offset() default {};
    
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
    @ConditionalPostconditionAnnotation(qualifier = LTLengthOf.class)
    @InheritedAnnotation
    public @interface List {
        EnsuresLTLengthOfIf[] value();
    }
}
