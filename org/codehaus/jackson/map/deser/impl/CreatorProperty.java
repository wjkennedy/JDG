// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.impl;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import java.lang.annotation.Annotation;
import java.io.IOException;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.util.Annotations;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.introspect.AnnotatedParameter;
import org.codehaus.jackson.map.deser.SettableBeanProperty;

public class CreatorProperty extends SettableBeanProperty
{
    protected final AnnotatedParameter _annotated;
    protected final Object _injectableValueId;
    
    public CreatorProperty(final String name, final JavaType type, final TypeDeserializer typeDeser, final Annotations contextAnnotations, final AnnotatedParameter param, final int index, final Object injectableValueId) {
        super(name, type, typeDeser, contextAnnotations);
        this._annotated = param;
        this._propertyIndex = index;
        this._injectableValueId = injectableValueId;
    }
    
    protected CreatorProperty(final CreatorProperty src, final JsonDeserializer<Object> deser) {
        super(src, deser);
        this._annotated = src._annotated;
        this._injectableValueId = src._injectableValueId;
    }
    
    @Override
    public CreatorProperty withValueDeserializer(final JsonDeserializer<Object> deser) {
        return new CreatorProperty(this, deser);
    }
    
    public Object findInjectableValue(final DeserializationContext context, final Object beanInstance) {
        if (this._injectableValueId == null) {
            throw new IllegalStateException("Property '" + this.getName() + "' (type " + this.getClass().getName() + ") has no injectable value id configured");
        }
        return context.findInjectableValue(this._injectableValueId, this, beanInstance);
    }
    
    public void inject(final DeserializationContext context, final Object beanInstance) throws IOException {
        this.set(beanInstance, this.findInjectableValue(context, beanInstance));
    }
    
    @Override
    public <A extends Annotation> A getAnnotation(final Class<A> acls) {
        if (this._annotated == null) {
            return null;
        }
        return this._annotated.getAnnotation(acls);
    }
    
    @Override
    public AnnotatedMember getMember() {
        return this._annotated;
    }
    
    @Override
    public void deserializeAndSet(final JsonParser jp, final DeserializationContext ctxt, final Object instance) throws IOException, JsonProcessingException {
        this.set(instance, this.deserialize(jp, ctxt));
    }
    
    @Override
    public void set(final Object instance, final Object value) throws IOException {
    }
    
    @Override
    public Object getInjectableValueId() {
        return this._injectableValueId;
    }
}
