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
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.PACKAGE, ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.PARAMETER })
@Repeatable(List.class)
public @interface DefaultQualifier {
    Class<? extends Annotation> value();
    
    TypeUseLocation[] locations() default { TypeUseLocation.ALL };
    
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.PACKAGE, ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.PARAMETER })
    public @interface List {
        DefaultQualifier[] value();
    }
}
