// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.introspect;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public final class AnnotatedField extends AnnotatedMember
{
    protected final Field _field;
    
    public AnnotatedField(final Field field, final AnnotationMap annMap) {
        super(annMap);
        this._field = field;
    }
    
    @Override
    public AnnotatedField withAnnotations(final AnnotationMap ann) {
        return new AnnotatedField(this._field, ann);
    }
    
    public void addOrOverride(final Annotation a) {
        this._annotations.add(a);
    }
    
    @Override
    public Field getAnnotated() {
        return this._field;
    }
    
    public int getModifiers() {
        return this._field.getModifiers();
    }
    
    @Override
    public String getName() {
        return this._field.getName();
    }
    
    @Override
    public <A extends Annotation> A getAnnotation(final Class<A> acls) {
        return this._annotations.get(acls);
    }
    
    @Override
    public Type getGenericType() {
        return this._field.getGenericType();
    }
    
    @Override
    public Class<?> getRawType() {
        return this._field.getType();
    }
    
    @Override
    public Class<?> getDeclaringClass() {
        return this._field.getDeclaringClass();
    }
    
    @Override
    public Member getMember() {
        return this._field;
    }
    
    @Override
    public void setValue(final Object pojo, final Object value) throws IllegalArgumentException {
        try {
            this._field.set(pojo, value);
        }
        catch (final IllegalAccessException e) {
            throw new IllegalArgumentException("Failed to setValue() for field " + this.getFullName() + ": " + e.getMessage(), e);
        }
    }
    
    public String getFullName() {
        return this.getDeclaringClass().getName() + "#" + this.getName();
    }
    
    public int getAnnotationCount() {
        return this._annotations.size();
    }
    
    @Override
    public String toString() {
        return "[field " + this.getName() + ", annotations: " + this._annotations + "]";
    }
}
