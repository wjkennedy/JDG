// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.introspect;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.TypeVariable;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.type.TypeBindings;
import java.lang.reflect.Type;
import java.lang.reflect.Method;

public final class AnnotatedMethod extends AnnotatedWithParams
{
    protected final Method _method;
    protected Class<?>[] _paramTypes;
    
    public AnnotatedMethod(final Method method, final AnnotationMap classAnn, final AnnotationMap[] paramAnnotations) {
        super(classAnn, paramAnnotations);
        this._method = method;
    }
    
    public AnnotatedMethod withMethod(final Method m) {
        return new AnnotatedMethod(m, this._annotations, this._paramAnnotations);
    }
    
    @Override
    public AnnotatedMethod withAnnotations(final AnnotationMap ann) {
        return new AnnotatedMethod(this._method, ann, this._paramAnnotations);
    }
    
    @Override
    public Method getAnnotated() {
        return this._method;
    }
    
    public int getModifiers() {
        return this._method.getModifiers();
    }
    
    @Override
    public String getName() {
        return this._method.getName();
    }
    
    @Override
    public Type getGenericType() {
        return this._method.getGenericReturnType();
    }
    
    @Override
    public Class<?> getRawType() {
        return this._method.getReturnType();
    }
    
    @Override
    public JavaType getType(final TypeBindings bindings) {
        return this.getType(bindings, this._method.getTypeParameters());
    }
    
    @Override
    public final Object call() throws Exception {
        return this._method.invoke(null, new Object[0]);
    }
    
    @Override
    public final Object call(final Object[] args) throws Exception {
        return this._method.invoke(null, args);
    }
    
    @Override
    public final Object call1(final Object arg) throws Exception {
        return this._method.invoke(null, arg);
    }
    
    @Override
    public Class<?> getDeclaringClass() {
        return this._method.getDeclaringClass();
    }
    
    @Override
    public Member getMember() {
        return this._method;
    }
    
    @Override
    public void setValue(final Object pojo, final Object value) throws IllegalArgumentException {
        try {
            this._method.invoke(pojo, value);
        }
        catch (final IllegalAccessException e) {
            throw new IllegalArgumentException("Failed to setValue() with method " + this.getFullName() + ": " + e.getMessage(), e);
        }
        catch (final InvocationTargetException e2) {
            throw new IllegalArgumentException("Failed to setValue() with method " + this.getFullName() + ": " + e2.getMessage(), e2);
        }
    }
    
    @Override
    public int getParameterCount() {
        return this.getParameterTypes().length;
    }
    
    public Type[] getParameterTypes() {
        return this._method.getGenericParameterTypes();
    }
    
    @Override
    public Class<?> getParameterClass(final int index) {
        final Class<?>[] types = this._method.getParameterTypes();
        return (index >= types.length) ? null : types[index];
    }
    
    @Override
    public Type getParameterType(final int index) {
        final Type[] types = this._method.getGenericParameterTypes();
        return (index >= types.length) ? null : types[index];
    }
    
    public Class<?>[] getParameterClasses() {
        if (this._paramTypes == null) {
            this._paramTypes = this._method.getParameterTypes();
        }
        return this._paramTypes;
    }
    
    public String getFullName() {
        return this.getDeclaringClass().getName() + "#" + this.getName() + "(" + this.getParameterCount() + " params)";
    }
    
    @Override
    public String toString() {
        return "[method " + this.getName() + ", annotations: " + this._annotations + "]";
    }
}
