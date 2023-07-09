// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.annotate;

import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.annotate.JacksonAnnotation;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonSerialize {
    Class<? extends JsonSerializer<?>> using() default JsonSerializer.None.class;
    
    Class<? extends JsonSerializer<?>> contentUsing() default JsonSerializer.None.class;
    
    Class<? extends JsonSerializer<?>> keyUsing() default JsonSerializer.None.class;
    
    Class<?> as() default NoClass.class;
    
    Class<?> keyAs() default NoClass.class;
    
    Class<?> contentAs() default NoClass.class;
    
    Typing typing() default Typing.DYNAMIC;
    
    Inclusion include() default Inclusion.ALWAYS;
    
    public enum Inclusion
    {
        ALWAYS, 
        NON_NULL, 
        NON_DEFAULT, 
        NON_EMPTY;
    }
    
    public enum Typing
    {
        DYNAMIC, 
        STATIC;
    }
}
