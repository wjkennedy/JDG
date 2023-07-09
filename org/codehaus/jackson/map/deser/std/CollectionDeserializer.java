// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.std;

import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.util.Annotations;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.deser.impl.CreatorProperty;
import org.codehaus.jackson.map.introspect.AnnotatedWithParams;
import org.codehaus.jackson.map.introspect.AnnotationMap;
import org.codehaus.jackson.map.introspect.AnnotatedConstructor;
import org.codehaus.jackson.map.DeserializationConfig;
import java.lang.reflect.Constructor;
import org.codehaus.jackson.map.deser.ValueInstantiator;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.map.ResolvableDeserializer;
import java.util.Collection;

@JacksonStdImpl
public class CollectionDeserializer extends ContainerDeserializerBase<Collection<Object>> implements ResolvableDeserializer
{
    protected final JavaType _collectionType;
    protected final JsonDeserializer<Object> _valueDeserializer;
    protected final TypeDeserializer _valueTypeDeserializer;
    protected final ValueInstantiator _valueInstantiator;
    protected JsonDeserializer<Object> _delegateDeserializer;
    
    @Deprecated
    protected CollectionDeserializer(final JavaType collectionType, final JsonDeserializer<Object> valueDeser, final TypeDeserializer valueTypeDeser, final Constructor<Collection<Object>> defCtor) {
        super(collectionType.getRawClass());
        this._collectionType = collectionType;
        this._valueDeserializer = valueDeser;
        this._valueTypeDeserializer = valueTypeDeser;
        final StdValueInstantiator inst = new StdValueInstantiator(null, collectionType);
        if (defCtor != null) {
            final AnnotatedConstructor aCtor = new AnnotatedConstructor(defCtor, null, null);
            inst.configureFromObjectSettings(aCtor, null, null, null, null);
        }
        this._valueInstantiator = inst;
    }
    
    public CollectionDeserializer(final JavaType collectionType, final JsonDeserializer<Object> valueDeser, final TypeDeserializer valueTypeDeser, final ValueInstantiator valueInstantiator) {
        super(collectionType.getRawClass());
        this._collectionType = collectionType;
        this._valueDeserializer = valueDeser;
        this._valueTypeDeserializer = valueTypeDeser;
        this._valueInstantiator = valueInstantiator;
    }
    
    protected CollectionDeserializer(final CollectionDeserializer src) {
        super(src._valueClass);
        this._collectionType = src._collectionType;
        this._valueDeserializer = src._valueDeserializer;
        this._valueTypeDeserializer = src._valueTypeDeserializer;
        this._valueInstantiator = src._valueInstantiator;
        this._delegateDeserializer = src._delegateDeserializer;
    }
    
    public void resolve(final DeserializationConfig config, final DeserializerProvider provider) throws JsonMappingException {
        if (this._valueInstantiator.canCreateUsingDelegate()) {
            final JavaType delegateType = this._valueInstantiator.getDelegateType();
            if (delegateType == null) {
                throw new IllegalArgumentException("Invalid delegate-creator definition for " + this._collectionType + ": value instantiator (" + this._valueInstantiator.getClass().getName() + ") returned true for 'canCreateUsingDelegate()', but null for 'getDelegateType()'");
            }
            final AnnotatedWithParams delegateCreator = this._valueInstantiator.getDelegateCreator();
            final BeanProperty.Std property = new BeanProperty.Std(null, delegateType, null, delegateCreator);
            this._delegateDeserializer = this.findDeserializer(config, provider, delegateType, property);
        }
    }
    
    @Override
    public JavaType getContentType() {
        return this._collectionType.getContentType();
    }
    
    @Override
    public JsonDeserializer<Object> getContentDeserializer() {
        return this._valueDeserializer;
    }
    
    @Override
    public Collection<Object> deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (this._delegateDeserializer != null) {
            return (Collection)this._valueInstantiator.createUsingDelegate(this._delegateDeserializer.deserialize(jp, ctxt));
        }
        if (jp.getCurrentToken() == JsonToken.VALUE_STRING) {
            final String str = jp.getText();
            if (str.length() == 0) {
                return (Collection)this._valueInstantiator.createFromString(str);
            }
        }
        return this.deserialize(jp, ctxt, (Collection<Object>)this._valueInstantiator.createUsingDefault());
    }
    
    @Override
    public Collection<Object> deserialize(final JsonParser jp, final DeserializationContext ctxt, final Collection<Object> result) throws IOException, JsonProcessingException {
        if (!jp.isExpectedStartArrayToken()) {
            return this.handleNonArray(jp, ctxt, result);
        }
        final JsonDeserializer<Object> valueDes = this._valueDeserializer;
        final TypeDeserializer typeDeser = this._valueTypeDeserializer;
        JsonToken t;
        while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
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
            result.add(value);
        }
        return result;
    }
    
    @Override
    public Object deserializeWithType(final JsonParser jp, final DeserializationContext ctxt, final TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
        return typeDeserializer.deserializeTypedFromArray(jp, ctxt);
    }
    
    private final Collection<Object> handleNonArray(final JsonParser jp, final DeserializationContext ctxt, final Collection<Object> result) throws IOException, JsonProcessingException {
        if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
            throw ctxt.mappingException(this._collectionType.getRawClass());
        }
        final JsonDeserializer<Object> valueDes = this._valueDeserializer;
        final TypeDeserializer typeDeser = this._valueTypeDeserializer;
        final JsonToken t = jp.getCurrentToken();
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
        result.add(value);
        return result;
    }
}
