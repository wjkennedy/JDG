// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.annotate;

import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.annotate.JacksonAnnotation;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonTypeIdResolver {
    Class<? extends TypeIdResolver> value();
}
