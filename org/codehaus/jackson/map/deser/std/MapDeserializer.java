// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.std;

import java.lang.reflect.InvocationTargetException;
import org.codehaus.jackson.map.deser.impl.PropertyValueBuffer;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.JsonMappingException;
import java.util.Iterator;
import org.codehaus.jackson.map.deser.SettableBeanProperty;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.util.Annotations;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.util.ArrayBuilders;
import org.codehaus.jackson.map.deser.impl.CreatorProperty;
import org.codehaus.jackson.map.introspect.AnnotatedWithParams;
import org.codehaus.jackson.map.introspect.AnnotationMap;
import org.codehaus.jackson.map.introspect.AnnotatedConstructor;
import org.codehaus.jackson.map.DeserializationConfig;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import org.codehaus.jackson.map.deser.impl.PropertyBasedCreator;
import org.codehaus.jackson.map.deser.ValueInstantiator;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.KeyDeserializer;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.map.ResolvableDeserializer;
import java.util.Map;

@JacksonStdImpl
public class MapDeserializer extends ContainerDeserializerBase<Map<Object, Object>> implements ResolvableDeserializer
{
    protected final JavaType _mapType;
    protected final KeyDeserializer _keyDeserializer;
    protected final JsonDeserializer<Object> _valueDeserializer;
    protected final TypeDeserializer _valueTypeDeserializer;
    protected final ValueInstantiator _valueInstantiator;
    protected final boolean _hasDefaultCreator;
    protected PropertyBasedCreator _propertyBasedCreator;
    protected JsonDeserializer<Object> _delegateDeserializer;
    protected HashSet<String> _ignorableProperties;
    
    @Deprecated
    protected MapDeserializer(final JavaType mapType, final Constructor<Map<Object, Object>> defCtor, final KeyDeserializer keyDeser, final JsonDeserializer<Object> valueDeser, final TypeDeserializer valueTypeDeser) {
        super(Map.class);
        this._mapType = mapType;
        this._keyDeserializer = keyDeser;
        this._valueDeserializer = valueDeser;
        this._valueTypeDeserializer = valueTypeDeser;
        final StdValueInstantiator inst = new StdValueInstantiator(null, mapType);
        if (defCtor != null) {
            final AnnotatedConstructor aCtor = new AnnotatedConstructor(defCtor, null, null);
            inst.configureFromObjectSettings(aCtor, null, null, null, null);
        }
        this._hasDefaultCreator = (defCtor != null);
        this._valueInstantiator = inst;
    }
    
    public MapDeserializer(final JavaType mapType, final ValueInstantiator valueInstantiator, final KeyDeserializer keyDeser, final JsonDeserializer<Object> valueDeser, final TypeDeserializer valueTypeDeser) {
        super(Map.class);
        this._mapType = mapType;
        this._keyDeserializer = keyDeser;
        this._valueDeserializer = valueDeser;
        this._valueTypeDeserializer = valueTypeDeser;
        this._valueInstantiator = valueInstantiator;
        if (valueInstantiator.canCreateFromObjectWith()) {
            this._propertyBasedCreator = new PropertyBasedCreator(valueInstantiator);
        }
        else {
            this._propertyBasedCreator = null;
        }
        this._hasDefaultCreator = valueInstantiator.canCreateUsingDefault();
    }
    
    protected MapDeserializer(final MapDeserializer src) {
        super(src._valueClass);
        this._mapType = src._mapType;
        this._keyDeserializer = src._keyDeserializer;
        this._valueDeserializer = src._valueDeserializer;
        this._valueTypeDeserializer = src._valueTypeDeserializer;
        this._valueInstantiator = src._valueInstantiator;
        this._propertyBasedCreator = src._propertyBasedCreator;
        this._delegateDeserializer = src._delegateDeserializer;
        this._hasDefaultCreator = src._hasDefaultCreator;
        this._ignorableProperties = src._ignorableProperties;
    }
    
    public void setIgnorableProperties(final String[] ignorable) {
        this._ignorableProperties = ((ignorable == null || ignorable.length == 0) ? null : ArrayBuilders.arrayToSet(ignorable));
    }
    
    public void resolve(final DeserializationConfig config, final DeserializerProvider provider) throws JsonMappingException {
        if (this._valueInstantiator.canCreateUsingDelegate()) {
            final JavaType delegateType = this._valueInstantiator.getDelegateType();
            if (delegateType == null) {
                throw new IllegalArgumentException("Invalid delegate-creator definition for " + this._mapType + ": value instantiator (" + this._valueInstantiator.getClass().getName() + ") returned true for 'canCreateUsingDelegate()', but null for 'getDelegateType()'");
            }
            final AnnotatedWithParams delegateCreator = this._valueInstantiator.getDelegateCreator();
            final BeanProperty.Std property = new BeanProperty.Std(null, delegateType, null, delegateCreator);
            this._delegateDeserializer = this.findDeserializer(config, provider, delegateType, property);
        }
        if (this._propertyBasedCreator != null) {
            for (final SettableBeanProperty prop : this._propertyBasedCreator.getCreatorProperties()) {
                if (!prop.hasValueDeserializer()) {
                    this._propertyBasedCreator.assignDeserializer(prop, this.findDeserializer(config, provider, prop.getType(), prop));
                }
            }
        }
    }
    
    @Override
    public JavaType getContentType() {
        return this._mapType.getContentType();
    }
    
    @Override
    public JsonDeserializer<Object> getContentDeserializer() {
        return this._valueDeserializer;
    }
    
    @Override
    public Map<Object, Object> deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (this._propertyBasedCreator != null) {
            return this._deserializeUsingCreator(jp, ctxt);
        }
        if (this._delegateDeserializer != null) {
            return (Map)this._valueInstantiator.createUsingDelegate(this._delegateDeserializer.deserialize(jp, ctxt));
        }
        if (!this._hasDefaultCreator) {
            throw ctxt.instantiationException(this.getMapClass(), "No default constructor found");
        }
        final JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.START_OBJECT || t == JsonToken.FIELD_NAME || t == JsonToken.END_OBJECT) {
            final Map<Object, Object> result = (Map<Object, Object>)this._valueInstantiator.createUsingDefault();
            this._readAndBind(jp, ctxt, result);
            return result;
        }
        if (t == JsonToken.VALUE_STRING) {
            return (Map)this._valueInstantiator.createFromString(jp.getText());
        }
        throw ctxt.mappingException(this.getMapClass());
    }
    
    @Override
    public Map<Object, Object> deserialize(final JsonParser jp, final DeserializationContext ctxt, final Map<Object, Object> result) throws IOException, JsonProcessingException {
        final JsonToken t = jp.getCurrentToken();
        if (t != JsonToken.START_OBJECT && t != JsonToken.FIELD_NAME) {
            throw ctxt.mappingException(this.getMapClass());
        }
        this._readAndBind(jp, ctxt, result);
        return result;
    }
    
    @Override
    public Object deserializeWithType(final JsonParser jp, final DeserializationContext ctxt, final TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
        return typeDeserializer.deserializeTypedFromObject(jp, ctxt);
    }
    
    public final Class<?> getMapClass() {
        return this._mapType.getRawClass();
    }
    
    @Override
    public JavaType getValueType() {
        return this._mapType;
    }
    
    protected final void _readAndBind(final JsonParser jp, final DeserializationContext ctxt, final Map<Object, Object> result) throws IOException, JsonProcessingException {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.START_OBJECT) {
            t = jp.nextToken();
        }
        final KeyDeserializer keyDes = this._keyDeserializer;
        final JsonDeserializer<Object> valueDes = this._valueDeserializer;
        final TypeDeserializer typeDeser = this._valueTypeDeserializer;
        while (t == JsonToken.FIELD_NAME) {
            final String fieldName = jp.getCurrentName();
            final Object key = keyDes.deserializeKey(fieldName, ctxt);
            t = jp.nextToken();
            if (this._ignorableProperties != null && this._ignorableProperties.contains(fieldName)) {
                jp.skipChildren();
            }
            else {
                Object value;
                if (t == JsonToken.VALUE_NULL) {
                    value = null;
                }
                else if (typeDeser == null) {
                    value = valueDes.deserialize(jp, ctxt);
                }
                else {
                    value = valueDes.deserializeWithType(jp, ctxt, typeDeser);
                }
                result.put(key, value);
            }
            t = jp.nextToken();
        }
    }
    
    public Map<Object, Object> _deserializeUsingCreator(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final PropertyBasedCreator creator = this._propertyBasedCreator;
        final PropertyValueBuffer buffer = creator.startBuilding(jp, ctxt);
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.START_OBJECT) {
            t = jp.nextToken();
        }
        final JsonDeserializer<Object> valueDes = this._valueDeserializer;
        final TypeDeserializer typeDeser = this._valueTypeDeserializer;
        while (t == JsonToken.FIELD_NAME) {
            final String propName = jp.getCurrentName();
            t = jp.nextToken();
            if (this._ignorableProperties != null && this._ignorableProperties.contains(propName)) {
                jp.skipChildren();
            }
            else {
                final SettableBeanProperty prop = creator.findCreatorProperty(propName);
                if (prop != null) {
                    final Object value = prop.deserialize(jp, ctxt);
                    if (buffer.assignParameter(prop.getPropertyIndex(), value)) {
                        jp.nextToken();
                        Map<Object, Object> result;
                        try {
                            result = (Map)creator.build(buffer);
                        }
                        catch (final Exception e) {
                            this.wrapAndThrow(e, this._mapType.getRawClass());
                            return null;
                        }
                        this._readAndBind(jp, ctxt, result);
                        return result;
                    }
                }
                else {
                    final String fieldName = jp.getCurrentName();
                    final Object key = this._keyDeserializer.deserializeKey(fieldName, ctxt);
                    Object value2;
                    if (t == JsonToken.VALUE_NULL) {
                        value2 = null;
                    }
                    else if (typeDeser == null) {
                        value2 = valueDes.deserialize(jp, ctxt);
                    }
                    else {
                        value2 = valueDes.deserializeWithType(jp, ctxt, typeDeser);
                    }
                    buffer.bufferMapProperty(key, value2);
                }
            }
            t = jp.nextToken();
        }
        try {
            return (Map)creator.build(buffer);
        }
        catch (final Exception e2) {
            this.wrapAndThrow(e2, this._mapType.getRawClass());
            return null;
        }
    }
    
    protected void wrapAndThrow(Throwable t, final Object ref) throws IOException {
        while (t instanceof InvocationTargetException && t.getCause() != null) {
            t = t.getCause();
        }
        if (t instanceof Error) {
            throw (Error)t;
        }
        if (t instanceof IOException && !(t instanceof JsonMappingException)) {
            throw (IOException)t;
        }
        throw JsonMappingException.wrapWithPath(t, ref, null);
    }
}
