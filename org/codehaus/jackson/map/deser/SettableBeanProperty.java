// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.util.ClassUtil;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Map;
import java.util.Collection;
import java.lang.reflect.Field;
import org.codehaus.jackson.map.introspect.AnnotatedField;
import java.lang.reflect.Method;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import java.lang.annotation.Annotation;
import org.codehaus.jackson.util.InternCache;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.util.Annotations;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.BeanProperty;

public abstract class SettableBeanProperty implements BeanProperty
{
    protected final String _propName;
    protected final JavaType _type;
    protected final Annotations _contextAnnotations;
    protected JsonDeserializer<Object> _valueDeserializer;
    protected TypeDeserializer _valueTypeDeserializer;
    protected NullProvider _nullProvider;
    protected String _managedReferenceName;
    protected int _propertyIndex;
    
    protected SettableBeanProperty(final String propName, final JavaType type, final TypeDeserializer typeDeser, final Annotations contextAnnotations) {
        this._propertyIndex = -1;
        if (propName == null || propName.length() == 0) {
            this._propName = "";
        }
        else {
            this._propName = InternCache.instance.intern(propName);
        }
        this._type = type;
        this._contextAnnotations = contextAnnotations;
        this._valueTypeDeserializer = typeDeser;
    }
    
    protected SettableBeanProperty(final SettableBeanProperty src) {
        this._propertyIndex = -1;
        this._propName = src._propName;
        this._type = src._type;
        this._contextAnnotations = src._contextAnnotations;
        this._valueDeserializer = src._valueDeserializer;
        this._valueTypeDeserializer = src._valueTypeDeserializer;
        this._nullProvider = src._nullProvider;
        this._managedReferenceName = src._managedReferenceName;
        this._propertyIndex = src._propertyIndex;
    }
    
    protected SettableBeanProperty(final SettableBeanProperty src, final JsonDeserializer<Object> deser) {
        this._propertyIndex = -1;
        this._propName = src._propName;
        this._type = src._type;
        this._contextAnnotations = src._contextAnnotations;
        this._valueTypeDeserializer = src._valueTypeDeserializer;
        this._managedReferenceName = src._managedReferenceName;
        this._propertyIndex = src._propertyIndex;
        this._valueDeserializer = deser;
        if (deser == null) {
            this._nullProvider = null;
        }
        else {
            final Object nvl = deser.getNullValue();
            this._nullProvider = ((nvl == null) ? null : new NullProvider(this._type, nvl));
        }
    }
    
    @Deprecated
    public void setValueDeserializer(final JsonDeserializer<Object> deser) {
        if (this._valueDeserializer != null) {
            throw new IllegalStateException("Already had assigned deserializer for property '" + this.getName() + "' (class " + this.getDeclaringClass().getName() + ")");
        }
        this._valueDeserializer = deser;
        final Object nvl = this._valueDeserializer.getNullValue();
        this._nullProvider = ((nvl == null) ? null : new NullProvider(this._type, nvl));
    }
    
    public abstract SettableBeanProperty withValueDeserializer(final JsonDeserializer<Object> p0);
    
    public void setManagedReferenceName(final String n) {
        this._managedReferenceName = n;
    }
    
    public void assignIndex(final int index) {
        if (this._propertyIndex != -1) {
            throw new IllegalStateException("Property '" + this.getName() + "' already had index (" + this._propertyIndex + "), trying to assign " + index);
        }
        this._propertyIndex = index;
    }
    
    public final String getName() {
        return this._propName;
    }
    
    public JavaType getType() {
        return this._type;
    }
    
    public abstract <A extends Annotation> A getAnnotation(final Class<A> p0);
    
    public abstract AnnotatedMember getMember();
    
    public <A extends Annotation> A getContextAnnotation(final Class<A> acls) {
        return this._contextAnnotations.get(acls);
    }
    
    protected final Class<?> getDeclaringClass() {
        return this.getMember().getDeclaringClass();
    }
    
    @Deprecated
    public String getPropertyName() {
        return this._propName;
    }
    
    public String getManagedReferenceName() {
        return this._managedReferenceName;
    }
    
    public boolean hasValueDeserializer() {
        return this._valueDeserializer != null;
    }
    
    public boolean hasValueTypeDeserializer() {
        return this._valueTypeDeserializer != null;
    }
    
    public JsonDeserializer<Object> getValueDeserializer() {
        return this._valueDeserializer;
    }
    
    public TypeDeserializer getValueTypeDeserializer() {
        return this._valueTypeDeserializer;
    }
    
    public int getPropertyIndex() {
        return this._propertyIndex;
    }
    
    @Deprecated
    public int getProperytIndex() {
        return this.getPropertyIndex();
    }
    
    public Object getInjectableValueId() {
        return null;
    }
    
    public abstract void deserializeAndSet(final JsonParser p0, final DeserializationContext p1, final Object p2) throws IOException, JsonProcessingException;
    
    public abstract void set(final Object p0, final Object p1) throws IOException;
    
    public final Object deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NULL) {
            return (this._nullProvider == null) ? null : this._nullProvider.nullValue(ctxt);
        }
        if (this._valueTypeDeserializer != null) {
            return this._valueDeserializer.deserializeWithType(jp, ctxt, this._valueTypeDeserializer);
        }
        return this._valueDeserializer.deserialize(jp, ctxt);
    }
    
    protected void _throwAsIOE(final Exception e, final Object value) throws IOException {
        if (e instanceof IllegalArgumentException) {
            final String actType = (value == null) ? "[NULL]" : value.getClass().getName();
            final StringBuilder msg = new StringBuilder("Problem deserializing property '").append(this.getPropertyName());
            msg.append("' (expected type: ").append(this.getType());
            msg.append("; actual type: ").append(actType).append(")");
            final String origMsg = e.getMessage();
            if (origMsg != null) {
                msg.append(", problem: ").append(origMsg);
            }
            else {
                msg.append(" (no error message provided)");
            }
            throw new JsonMappingException(msg.toString(), null, e);
        }
        this._throwAsIOE(e);
    }
    
    protected IOException _throwAsIOE(final Exception e) throws IOException {
        if (e instanceof IOException) {
            throw (IOException)e;
        }
        if (e instanceof RuntimeException) {
            throw (RuntimeException)e;
        }
        Throwable th;
        for (th = e; th.getCause() != null; th = th.getCause()) {}
        throw new JsonMappingException(th.getMessage(), null, th);
    }
    
    @Override
    public String toString() {
        return "[property '" + this.getName() + "']";
    }
    
    public static final class MethodProperty extends SettableBeanProperty
    {
        protected final AnnotatedMethod _annotated;
        protected final Method _setter;
        
        public MethodProperty(final String name, final JavaType type, final TypeDeserializer typeDeser, final Annotations contextAnnotations, final AnnotatedMethod method) {
            super(name, type, typeDeser, contextAnnotations);
            this._annotated = method;
            this._setter = method.getAnnotated();
        }
        
        protected MethodProperty(final MethodProperty src, final JsonDeserializer<Object> deser) {
            super(src, deser);
            this._annotated = src._annotated;
            this._setter = src._setter;
        }
        
        @Override
        public MethodProperty withValueDeserializer(final JsonDeserializer<Object> deser) {
            return new MethodProperty(this, deser);
        }
        
        @Override
        public <A extends Annotation> A getAnnotation(final Class<A> acls) {
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
        public final void set(final Object instance, final Object value) throws IOException {
            try {
                this._setter.invoke(instance, value);
            }
            catch (final Exception e) {
                this._throwAsIOE(e, value);
            }
        }
    }
    
    public static final class SetterlessProperty extends SettableBeanProperty
    {
        protected final AnnotatedMethod _annotated;
        protected final Method _getter;
        
        public SetterlessProperty(final String name, final JavaType type, final TypeDeserializer typeDeser, final Annotations contextAnnotations, final AnnotatedMethod method) {
            super(name, type, typeDeser, contextAnnotations);
            this._annotated = method;
            this._getter = method.getAnnotated();
        }
        
        protected SetterlessProperty(final SetterlessProperty src, final JsonDeserializer<Object> deser) {
            super(src, deser);
            this._annotated = src._annotated;
            this._getter = src._getter;
        }
        
        @Override
        public SetterlessProperty withValueDeserializer(final JsonDeserializer<Object> deser) {
            return new SetterlessProperty(this, deser);
        }
        
        @Override
        public <A extends Annotation> A getAnnotation(final Class<A> acls) {
            return this._annotated.getAnnotation(acls);
        }
        
        @Override
        public AnnotatedMember getMember() {
            return this._annotated;
        }
        
        @Override
        public final void deserializeAndSet(final JsonParser jp, final DeserializationContext ctxt, final Object instance) throws IOException, JsonProcessingException {
            final JsonToken t = jp.getCurrentToken();
            if (t == JsonToken.VALUE_NULL) {
                return;
            }
            Object toModify;
            try {
                toModify = this._getter.invoke(instance, new Object[0]);
            }
            catch (final Exception e) {
                this._throwAsIOE(e);
                return;
            }
            if (toModify == null) {
                throw new JsonMappingException("Problem deserializing 'setterless' property '" + this.getName() + "': get method returned null");
            }
            this._valueDeserializer.deserialize(jp, ctxt, toModify);
        }
        
        @Override
        public final void set(final Object instance, final Object value) throws IOException {
            throw new UnsupportedOperationException("Should never call 'set' on setterless property");
        }
    }
    
    public static final class FieldProperty extends SettableBeanProperty
    {
        protected final AnnotatedField _annotated;
        protected final Field _field;
        
        public FieldProperty(final String name, final JavaType type, final TypeDeserializer typeDeser, final Annotations contextAnnotations, final AnnotatedField field) {
            super(name, type, typeDeser, contextAnnotations);
            this._annotated = field;
            this._field = field.getAnnotated();
        }
        
        protected FieldProperty(final FieldProperty src, final JsonDeserializer<Object> deser) {
            super(src, deser);
            this._annotated = src._annotated;
            this._field = src._field;
        }
        
        @Override
        public FieldProperty withValueDeserializer(final JsonDeserializer<Object> deser) {
            return new FieldProperty(this, deser);
        }
        
        @Override
        public <A extends Annotation> A getAnnotation(final Class<A> acls) {
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
        public final void set(final Object instance, final Object value) throws IOException {
            try {
                this._field.set(instance, value);
            }
            catch (final Exception e) {
                this._throwAsIOE(e, value);
            }
        }
    }
    
    public static final class ManagedReferenceProperty extends SettableBeanProperty
    {
        protected final String _referenceName;
        protected final boolean _isContainer;
        protected final SettableBeanProperty _managedProperty;
        protected final SettableBeanProperty _backProperty;
        
        public ManagedReferenceProperty(final String refName, final SettableBeanProperty forward, final SettableBeanProperty backward, final Annotations contextAnnotations, final boolean isContainer) {
            super(forward.getName(), forward.getType(), forward._valueTypeDeserializer, contextAnnotations);
            this._referenceName = refName;
            this._managedProperty = forward;
            this._backProperty = backward;
            this._isContainer = isContainer;
        }
        
        protected ManagedReferenceProperty(final ManagedReferenceProperty src, final JsonDeserializer<Object> deser) {
            super(src, deser);
            this._referenceName = src._referenceName;
            this._isContainer = src._isContainer;
            this._managedProperty = src._managedProperty;
            this._backProperty = src._backProperty;
        }
        
        @Override
        public ManagedReferenceProperty withValueDeserializer(final JsonDeserializer<Object> deser) {
            return new ManagedReferenceProperty(this, deser);
        }
        
        @Override
        public <A extends Annotation> A getAnnotation(final Class<A> acls) {
            return this._managedProperty.getAnnotation(acls);
        }
        
        @Override
        public AnnotatedMember getMember() {
            return this._managedProperty.getMember();
        }
        
        @Override
        public void deserializeAndSet(final JsonParser jp, final DeserializationContext ctxt, final Object instance) throws IOException, JsonProcessingException {
            this.set(instance, this._managedProperty.deserialize(jp, ctxt));
        }
        
        @Override
        public final void set(final Object instance, final Object value) throws IOException {
            this._managedProperty.set(instance, value);
            if (value != null) {
                if (this._isContainer) {
                    if (value instanceof Object[]) {
                        for (final Object ob : (Object[])value) {
                            if (ob != null) {
                                this._backProperty.set(ob, instance);
                            }
                        }
                    }
                    else if (value instanceof Collection) {
                        for (final Object ob2 : (Collection)value) {
                            if (ob2 != null) {
                                this._backProperty.set(ob2, instance);
                            }
                        }
                    }
                    else {
                        if (!(value instanceof Map)) {
                            throw new IllegalStateException("Unsupported container type (" + value.getClass().getName() + ") when resolving reference '" + this._referenceName + "'");
                        }
                        for (final Object ob2 : ((Map)value).values()) {
                            if (ob2 != null) {
                                this._backProperty.set(ob2, instance);
                            }
                        }
                    }
                }
                else {
                    this._backProperty.set(value, instance);
                }
            }
        }
    }
    
    public static final class InnerClassProperty extends SettableBeanProperty
    {
        protected final SettableBeanProperty _delegate;
        protected final Constructor<?> _creator;
        
        public InnerClassProperty(final SettableBeanProperty delegate, final Constructor<?> ctor) {
            super(delegate);
            this._delegate = delegate;
            this._creator = ctor;
        }
        
        protected InnerClassProperty(final InnerClassProperty src, final JsonDeserializer<Object> deser) {
            super(src, deser);
            this._delegate = src._delegate.withValueDeserializer(deser);
            this._creator = src._creator;
        }
        
        @Override
        public InnerClassProperty withValueDeserializer(final JsonDeserializer<Object> deser) {
            return new InnerClassProperty(this, deser);
        }
        
        @Override
        public <A extends Annotation> A getAnnotation(final Class<A> acls) {
            return this._delegate.getAnnotation(acls);
        }
        
        @Override
        public AnnotatedMember getMember() {
            return this._delegate.getMember();
        }
        
        @Override
        public void deserializeAndSet(final JsonParser jp, final DeserializationContext ctxt, final Object bean) throws IOException, JsonProcessingException {
            final JsonToken t = jp.getCurrentToken();
            Object value;
            if (t == JsonToken.VALUE_NULL) {
                value = ((this._nullProvider == null) ? null : this._nullProvider.nullValue(ctxt));
            }
            else if (this._valueTypeDeserializer != null) {
                value = this._valueDeserializer.deserializeWithType(jp, ctxt, this._valueTypeDeserializer);
            }
            else {
                try {
                    value = this._creator.newInstance(bean);
                }
                catch (final Exception e) {
                    ClassUtil.unwrapAndThrowAsIAE(e, "Failed to instantiate class " + this._creator.getDeclaringClass().getName() + ", problem: " + e.getMessage());
                    value = null;
                }
                this._valueDeserializer.deserialize(jp, ctxt, value);
            }
            this.set(bean, value);
        }
        
        @Override
        public final void set(final Object instance, final Object value) throws IOException {
            this._delegate.set(instance, value);
        }
    }
    
    protected static final class NullProvider
    {
        private final Object _nullValue;
        private final boolean _isPrimitive;
        private final Class<?> _rawType;
        
        protected NullProvider(final JavaType type, final Object nullValue) {
            this._nullValue = nullValue;
            this._isPrimitive = type.isPrimitive();
            this._rawType = type.getRawClass();
        }
        
        public Object nullValue(final DeserializationContext ctxt) throws JsonProcessingException {
            if (this._isPrimitive && ctxt.isEnabled(DeserializationConfig.Feature.FAIL_ON_NULL_FOR_PRIMITIVES)) {
                throw ctxt.mappingException("Can not map JSON null into type " + this._rawType.getName() + " (set DeserializationConfig.Feature.FAIL_ON_NULL_FOR_PRIMITIVES to 'false' to allow)");
            }
            return this._nullValue;
        }
    }
}
