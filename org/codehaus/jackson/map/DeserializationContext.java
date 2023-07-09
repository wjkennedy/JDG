// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import org.codehaus.jackson.map.util.ArrayBuilders;
import org.codehaus.jackson.map.util.ObjectBuffer;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.Base64Variant;

public abstract class DeserializationContext
{
    protected final DeserializationConfig _config;
    protected final int _featureFlags;
    
    protected DeserializationContext(final DeserializationConfig config) {
        this._config = config;
        this._featureFlags = config._featureFlags;
    }
    
    public DeserializationConfig getConfig() {
        return this._config;
    }
    
    public DeserializerProvider getDeserializerProvider() {
        return null;
    }
    
    public boolean isEnabled(final DeserializationConfig.Feature feat) {
        return (this._featureFlags & feat.getMask()) != 0x0;
    }
    
    public Base64Variant getBase64Variant() {
        return this._config.getBase64Variant();
    }
    
    public abstract JsonParser getParser();
    
    public final JsonNodeFactory getNodeFactory() {
        return this._config.getNodeFactory();
    }
    
    public JavaType constructType(final Class<?> cls) {
        return this._config.constructType(cls);
    }
    
    public TypeFactory getTypeFactory() {
        return this._config.getTypeFactory();
    }
    
    public abstract Object findInjectableValue(final Object p0, final BeanProperty p1, final Object p2);
    
    public abstract ObjectBuffer leaseObjectBuffer();
    
    public abstract void returnObjectBuffer(final ObjectBuffer p0);
    
    public abstract ArrayBuilders getArrayBuilders();
    
    public abstract Date parseDate(final String p0) throws IllegalArgumentException;
    
    public abstract Calendar constructCalendar(final Date p0);
    
    public abstract boolean handleUnknownProperty(final JsonParser p0, final JsonDeserializer<?> p1, final Object p2, final String p3) throws IOException, JsonProcessingException;
    
    public abstract JsonMappingException mappingException(final Class<?> p0);
    
    public abstract JsonMappingException mappingException(final Class<?> p0, final JsonToken p1);
    
    public JsonMappingException mappingException(final String message) {
        return JsonMappingException.from(this.getParser(), message);
    }
    
    public abstract JsonMappingException instantiationException(final Class<?> p0, final Throwable p1);
    
    public abstract JsonMappingException instantiationException(final Class<?> p0, final String p1);
    
    public abstract JsonMappingException weirdStringException(final Class<?> p0, final String p1);
    
    public abstract JsonMappingException weirdNumberException(final Class<?> p0, final String p1);
    
    public abstract JsonMappingException weirdKeyException(final Class<?> p0, final String p1, final String p2);
    
    public abstract JsonMappingException wrongTokenException(final JsonParser p0, final JsonToken p1, final String p2);
    
    public abstract JsonMappingException unknownFieldException(final Object p0, final String p1);
    
    public abstract JsonMappingException unknownTypeException(final JavaType p0, final String p1);
}
