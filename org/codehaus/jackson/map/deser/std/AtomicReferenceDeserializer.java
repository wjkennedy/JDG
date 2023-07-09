// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.std;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.ResolvableDeserializer;
import java.util.concurrent.atomic.AtomicReference;

public class AtomicReferenceDeserializer extends StdScalarDeserializer<AtomicReference<?>> implements ResolvableDeserializer
{
    protected final JavaType _referencedType;
    protected final BeanProperty _property;
    protected JsonDeserializer<?> _valueDeserializer;
    
    public AtomicReferenceDeserializer(final JavaType referencedType, final BeanProperty property) {
        super(AtomicReference.class);
        this._referencedType = referencedType;
        this._property = property;
    }
    
    @Override
    public AtomicReference<?> deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return new AtomicReference<Object>(this._valueDeserializer.deserialize(jp, ctxt));
    }
    
    public void resolve(final DeserializationConfig config, final DeserializerProvider provider) throws JsonMappingException {
        this._valueDeserializer = provider.findValueDeserializer(config, this._referencedType, this._property);
    }
}
