// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.introspect;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.TypeVariable;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.type.TypeBindings;
import java.lang.reflect.Type;
import java.lang.reflect.Constructor;

public final class AnnotatedConstructor extends AnnotatedWithParams
{
    protected final Constructor<?> _constructor;
    
    public AnnotatedConstructor(final Constructor<?> constructor, final AnnotationMap classAnn, final AnnotationMap[] paramAnn) {
        super(classAnn, paramAnn);
        if (constructor == null) {
            throw new IllegalArgumentException("Null constructor not allowed");
        }
        this._constructor = constructor;
    }
    
    @Override
    public AnnotatedConstructor withAnnotations(final AnnotationMap ann) {
        return new AnnotatedConstructor(this._constructor, ann, this._paramAnnotations);
    }
    
    @Override
    public Constructor<?> getAnnotated() {
        return this._constructor;
    }
    
    public int getModifiers() {
        return this._constructor.getModifiers();
    }
    
    @Override
    public String getName() {
        return this._constructor.getName();
    }
    
    @Override
    public Type getGenericType() {
        return this.getRawType();
    }
    
    @Override
    public Class<?> getRawType() {
        return this._constructor.getDeclaringClass();
    }
    
    @Override
    public JavaType getType(final TypeBindings bindings) {
        return this.getType(bindings, this._constructor.getTypeParameters());
    }
    
    @Override
    public int getParameterCount() {
        return this._constructor.getParameterTypes().length;
    }
    
    @Override
    public Class<?> getParameterClass(final int index) {
        final Class<?>[] types = this._constructor.getParameterTypes();
        return (index >= types.length) ? null : types[index];
    }
    
    @Override
    public Type getParameterType(final int index) {
        final Type[] types = this._constructor.getGenericParameterTypes();
        return (index >= types.length) ? null : types[index];
    }
    
    @Override
    public final Object call() throws Exception {
        return this._constructor.newInstance(new Object[0]);
    }
    
    @Override
    public final Object call(final Object[] args) throws Exception {
        return this._constructor.newInstance(args);
    }
    
    @Override
    public final Object call1(final Object arg) throws Exception {
        return this._constructor.newInstance(arg);
    }
    
    @Override
    public Class<?> getDeclaringClass() {
        return this._constructor.getDeclaringClass();
    }
    
    @Override
    public Member getMember() {
        return this._constructor;
    }
    
    @Override
    public void setValue(final Object pojo, final Object value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot call setValue() on constructor of " + this.getDeclaringClass().getName());
    }
    
    @Override
    public String toString() {
        return "[constructor for " + this.getName() + ", annotations: " + this._annotations + "]";
    }
}
