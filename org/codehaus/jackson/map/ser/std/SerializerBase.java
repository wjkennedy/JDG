// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.std;

import org.codehaus.jackson.map.SerializationConfig;
import java.lang.reflect.InvocationTargetException;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.JsonNode;
import java.lang.reflect.Type;
import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.schema.SchemaAware;
import org.codehaus.jackson.map.JsonSerializer;

public abstract class SerializerBase<T> extends JsonSerializer<T> implements SchemaAware
{
    protected final Class<T> _handledType;
    
    protected SerializerBase(final Class<T> t) {
        this._handledType = t;
    }
    
    protected SerializerBase(final JavaType type) {
        this._handledType = (Class<T>)type.getRawClass();
    }
    
    protected SerializerBase(final Class<?> t, final boolean dummy) {
        this._handledType = (Class<T>)t;
    }
    
    @Override
    public final Class<T> handledType() {
        return this._handledType;
    }
    
    @Override
    public abstract void serialize(final T p0, final JsonGenerator p1, final SerializerProvider p2) throws IOException, JsonGenerationException;
    
    public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) throws JsonMappingException {
        return this.createSchemaNode("string");
    }
    
    protected ObjectNode createObjectNode() {
        return JsonNodeFactory.instance.objectNode();
    }
    
    protected ObjectNode createSchemaNode(final String type) {
        final ObjectNode schema = this.createObjectNode();
        schema.put("type", type);
        return schema;
    }
    
    protected ObjectNode createSchemaNode(final String type, final boolean isOptional) {
        final ObjectNode schema = this.createSchemaNode(type);
        if (!isOptional) {
            schema.put("required", !isOptional);
        }
        return schema;
    }
    
    protected boolean isDefaultSerializer(final JsonSerializer<?> serializer) {
        return serializer != null && serializer.getClass().getAnnotation(JacksonStdImpl.class) != null;
    }
    
    public void wrapAndThrow(final SerializerProvider provider, Throwable t, final Object bean, final String fieldName) throws IOException {
        while (t instanceof InvocationTargetException && t.getCause() != null) {
            t = t.getCause();
        }
        if (t instanceof Error) {
            throw (Error)t;
        }
        final boolean wrap = provider == null || provider.isEnabled(SerializationConfig.Feature.WRAP_EXCEPTIONS);
        if (t instanceof IOException) {
            if (!wrap || !(t instanceof JsonMappingException)) {
                throw (IOException)t;
            }
        }
        else if (!wrap && t instanceof RuntimeException) {
            throw (RuntimeException)t;
        }
        throw JsonMappingException.wrapWithPath(t, bean, fieldName);
    }
    
    public void wrapAndThrow(final SerializerProvider provider, Throwable t, final Object bean, final int index) throws IOException {
        while (t instanceof InvocationTargetException && t.getCause() != null) {
            t = t.getCause();
        }
        if (t instanceof Error) {
            throw (Error)t;
        }
        final boolean wrap = provider == null || provider.isEnabled(SerializationConfig.Feature.WRAP_EXCEPTIONS);
        if (t instanceof IOException) {
            if (!wrap || !(t instanceof JsonMappingException)) {
                throw (IOException)t;
            }
        }
        else if (!wrap && t instanceof RuntimeException) {
            throw (RuntimeException)t;
        }
        throw JsonMappingException.wrapWithPath(t, bean, index);
    }
    
    @Deprecated
    public void wrapAndThrow(final Throwable t, final Object bean, final String fieldName) throws IOException {
        this.wrapAndThrow(null, t, bean, fieldName);
    }
    
    @Deprecated
    public void wrapAndThrow(final Throwable t, final Object bean, final int index) throws IOException {
        this.wrapAndThrow(null, t, bean, index);
    }
}
