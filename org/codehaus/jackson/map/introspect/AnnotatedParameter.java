// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.introspect;

import java.lang.reflect.Member;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.type.TypeFactory;
import java.lang.reflect.AnnotatedElement;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public final class AnnotatedParameter extends AnnotatedMember
{
    protected final AnnotatedWithParams _owner;
    protected final Type _type;
    protected final int _index;
    
    public AnnotatedParameter(final AnnotatedWithParams owner, final Type type, final AnnotationMap annotations, final int index) {
        super(annotations);
        this._owner = owner;
        this._type = type;
        this._index = index;
    }
    
    @Override
    public AnnotatedParameter withAnnotations(final AnnotationMap ann) {
        if (ann == this._annotations) {
            return this;
        }
        return this._owner.replaceParameterAnnotations(this._index, ann);
    }
    
    public void addOrOverride(final Annotation a) {
        this._annotations.add(a);
    }
    
    @Override
    public AnnotatedElement getAnnotated() {
        return null;
    }
    
    public int getModifiers() {
        return this._owner.getModifiers();
    }
    
    @Override
    public String getName() {
        return "";
    }
    
    @Override
    public <A extends Annotation> A getAnnotation(final Class<A> acls) {
        return this._annotations.get(acls);
    }
    
    @Override
    public Type getGenericType() {
        return this._type;
    }
    
    @Override
    public Class<?> getRawType() {
        if (this._type instanceof Class) {
            return (Class)this._type;
        }
        final JavaType t = TypeFactory.defaultInstance().constructType(this._type);
        return t.getRawClass();
    }
    
    @Override
    public Class<?> getDeclaringClass() {
        return this._owner.getDeclaringClass();
    }
    
    @Override
    public Member getMember() {
        return this._owner.getMember();
    }
    
    @Override
    public void setValue(final Object pojo, final Object value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot call setValue() on constructor parameter of " + this.getDeclaringClass().getName());
    }
    
    public Type getParameterType() {
        return this._type;
    }
    
    public AnnotatedWithParams getOwner() {
        return this._owner;
    }
    
    public int getIndex() {
        return this._index;
    }
    
    @Override
    public String toString() {
        return "[parameter #" + this.getIndex() + ", annotations: " + this._annotations + "]";
    }
}
