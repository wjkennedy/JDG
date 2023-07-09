// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.std;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.schema.JsonSchema;
import org.codehaus.jackson.schema.SchemaAware;
import java.lang.reflect.ParameterizedType;
import org.codehaus.jackson.JsonNode;
import java.lang.reflect.Type;
import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ser.impl.PropertySerializerMap;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.ResolvableSerializer;

public abstract class AsArraySerializerBase<T> extends ContainerSerializerBase<T> implements ResolvableSerializer
{
    protected final boolean _staticTyping;
    protected final JavaType _elementType;
    protected final TypeSerializer _valueTypeSerializer;
    protected JsonSerializer<Object> _elementSerializer;
    protected final BeanProperty _property;
    protected PropertySerializerMap _dynamicSerializers;
    
    @Deprecated
    protected AsArraySerializerBase(final Class<?> cls, final JavaType et, final boolean staticTyping, final TypeSerializer vts, final BeanProperty property) {
        this(cls, et, staticTyping, vts, property, null);
    }
    
    protected AsArraySerializerBase(final Class<?> cls, final JavaType et, final boolean staticTyping, final TypeSerializer vts, final BeanProperty property, final JsonSerializer<Object> elementSerializer) {
        super(cls, false);
        this._elementType = et;
        this._staticTyping = (staticTyping || (et != null && et.isFinal()));
        this._valueTypeSerializer = vts;
        this._property = property;
        this._elementSerializer = elementSerializer;
        this._dynamicSerializers = PropertySerializerMap.emptyMap();
    }
    
    @Override
    public final void serialize(final T value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        jgen.writeStartArray();
        this.serializeContents(value, jgen, provider);
        jgen.writeEndArray();
    }
    
    @Override
    public final void serializeWithType(final T value, final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException, JsonGenerationException {
        typeSer.writeTypePrefixForArray(value, jgen);
        this.serializeContents(value, jgen, provider);
        typeSer.writeTypeSuffixForArray(value, jgen);
    }
    
    protected abstract void serializeContents(final T p0, final JsonGenerator p1, final SerializerProvider p2) throws IOException, JsonGenerationException;
    
    @Override
    public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) throws JsonMappingException {
        final ObjectNode o = this.createSchemaNode("array", true);
        JavaType contentType = null;
        if (typeHint != null) {
            final JavaType javaType = provider.constructType(typeHint);
            contentType = javaType.getContentType();
            if (contentType == null && typeHint instanceof ParameterizedType) {
                final Type[] typeArgs = ((ParameterizedType)typeHint).getActualTypeArguments();
                if (typeArgs.length == 1) {
                    contentType = provider.constructType(typeArgs[0]);
                }
            }
        }
        if (contentType == null && this._elementType != null) {
            contentType = this._elementType;
        }
        if (contentType != null) {
            JsonNode schemaNode = null;
            if (contentType.getRawClass() != Object.class) {
                final JsonSerializer<Object> ser = provider.findValueSerializer(contentType, this._property);
                if (ser instanceof SchemaAware) {
                    schemaNode = ((SchemaAware)ser).getSchema(provider, null);
                }
            }
            if (schemaNode == null) {
                schemaNode = JsonSchema.getDefaultSchemaNode();
            }
            o.put("items", schemaNode);
        }
        return o;
    }
    
    public void resolve(final SerializerProvider provider) throws JsonMappingException {
        if (this._staticTyping && this._elementType != null && this._elementSerializer == null) {
            this._elementSerializer = provider.findValueSerializer(this._elementType, this._property);
        }
    }
    
    protected final JsonSerializer<Object> _findAndAddDynamic(final PropertySerializerMap map, final Class<?> type, final SerializerProvider provider) throws JsonMappingException {
        final PropertySerializerMap.SerializerAndMapResult result = map.findAndAddSerializer(type, provider, this._property);
        if (map != result.map) {
            this._dynamicSerializers = result.map;
        }
        return result.serializer;
    }
    
    protected final JsonSerializer<Object> _findAndAddDynamic(final PropertySerializerMap map, final JavaType type, final SerializerProvider provider) throws JsonMappingException {
        final PropertySerializerMap.SerializerAndMapResult result = map.findAndAddSerializer(type, provider, this._property);
        if (map != result.map) {
            this._dynamicSerializers = result.map;
        }
        return result.serializer;
    }
}
