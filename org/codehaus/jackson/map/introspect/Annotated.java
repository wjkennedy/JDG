// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.introspect;

import java.lang.reflect.Type;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.type.TypeBindings;
import java.lang.reflect.Modifier;
import java.lang.reflect.AnnotatedElement;
import java.lang.annotation.Annotation;

public abstract class Annotated
{
    protected Annotated() {
    }
    
    public abstract <A extends Annotation> A getAnnotation(final Class<A> p0);
    
    public final <A extends Annotation> boolean hasAnnotation(final Class<A> acls) {
        return this.getAnnotation(acls) != null;
    }
    
    public abstract Annotated withAnnotations(final AnnotationMap p0);
    
    public final Annotated withFallBackAnnotationsFrom(final Annotated annotated) {
        return this.withAnnotations(AnnotationMap.merge(this.getAllAnnotations(), annotated.getAllAnnotations()));
    }
    
    public abstract AnnotatedElement getAnnotated();
    
    protected abstract int getModifiers();
    
    public final boolean isPublic() {
        return Modifier.isPublic(this.getModifiers());
    }
    
    public abstract String getName();
    
    public JavaType getType(final TypeBindings context) {
        return context.resolveType(this.getGenericType());
    }
    
    public abstract Type getGenericType();
    
    public abstract Class<?> getRawType();
    
    protected abstract AnnotationMap getAllAnnotations();
}
