// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.std;

import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.introspect.AnnotatedWithParams;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.util.Annotations;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.deser.ValueInstantiator;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.map.ResolvableDeserializer;
import java.util.Collection;

@JacksonStdImpl
public final class StringCollectionDeserializer extends ContainerDeserializerBase<Collection<String>> implements ResolvableDeserializer
{
    protected final JavaType _collectionType;
    protected final JsonDeserializer<String> _valueDeserializer;
    protected final boolean _isDefaultDeserializer;
    protected final ValueInstantiator _valueInstantiator;
    protected JsonDeserializer<Object> _delegateDeserializer;
    
    public StringCollectionDeserializer(final JavaType collectionType, final JsonDeserializer<?> valueDeser, final ValueInstantiator valueInstantiator) {
        super(collectionType.getRawClass());
        this._collectionType = collectionType;
        this._valueDeserializer = (JsonDeserializer<String>)valueDeser;
        this._valueInstantiator = valueInstantiator;
        this._isDefaultDeserializer = this.isDefaultSerializer(valueDeser);
    }
    
    protected StringCollectionDeserializer(final StringCollectionDeserializer src) {
        super(src._valueClass);
        this._collectionType = src._collectionType;
        this._valueDeserializer = src._valueDeserializer;
        this._valueInstantiator = src._valueInstantiator;
        this._isDefaultDeserializer = src._isDefaultDeserializer;
    }
    
    public void resolve(final DeserializationConfig config, final DeserializerProvider provider) throws JsonMappingException {
        final AnnotatedWithParams delegateCreator = this._valueInstantiator.getDelegateCreator();
        if (delegateCreator != null) {
            final JavaType delegateType = this._valueInstantiator.getDelegateType();
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
        final JsonDeserializer<?> deser = this._valueDeserializer;
        return (JsonDeserializer<Object>)deser;
    }
    
    @Override
    public Collection<String> deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (this._delegateDeserializer != null) {
            return (Collection)this._valueInstantiator.createUsingDelegate(this._delegateDeserializer.deserialize(jp, ctxt));
        }
        final Collection<String> result = (Collection<String>)this._valueInstantiator.createUsingDefault();
        return this.deserialize(jp, ctxt, result);
    }
    
    @Override
    public Collection<String> deserialize(final JsonParser jp, final DeserializationContext ctxt, final Collection<String> result) throws IOException, JsonProcessingException {
        if (!jp.isExpectedStartArrayToken()) {
            return this.handleNonArray(jp, ctxt, result);
        }
        if (!this._isDefaultDeserializer) {
            return this.deserializeUsingCustom(jp, ctxt, result);
        }
        JsonToken t;
        while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
            result.add((t == JsonToken.VALUE_NULL) ? null : jp.getText());
        }
        return result;
    }
    
    private Collection<String> deserializeUsingCustom(final JsonParser jp, final DeserializationContext ctxt, final Collection<String> result) throws IOException, JsonProcessingException {
        final JsonDeserializer<String> deser = this._valueDeserializer;
        JsonToken t;
        while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
            String value;
            if (t == JsonToken.VALUE_NULL) {
                value = null;
            }
            else {
                value = deser.deserialize(jp, ctxt);
            }
            result.add(value);
        }
        return result;
    }
    
    @Override
    public Object deserializeWithType(final JsonParser jp, final DeserializationContext ctxt, final TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
        return typeDeserializer.deserializeTypedFromArray(jp, ctxt);
    }
    
    private final Collection<String> handleNonArray(final JsonParser jp, final DeserializationContext ctxt, final Collection<String> result) throws IOException, JsonProcessingException {
        if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
            throw ctxt.mappingException(this._collectionType.getRawClass());
        }
        final JsonDeserializer<String> valueDes = this._valueDeserializer;
        final JsonToken t = jp.getCurrentToken();
        String value;
        if (t == JsonToken.VALUE_NULL) {
            value = null;
        }
        else {
            value = ((valueDes == null) ? jp.getText() : valueDes.deserialize(jp, ctxt));
        }
        result.add(value);
        return result;
    }
}
