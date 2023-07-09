// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.std;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.schema.JsonSchema;
import org.codehaus.jackson.schema.SchemaAware;
import org.codehaus.jackson.node.JsonNodeFactory;
import java.lang.reflect.ParameterizedType;
import org.codehaus.jackson.JsonNode;
import java.lang.reflect.Type;
import org.codehaus.jackson.map.JsonMappingException;
import java.util.Iterator;
import java.util.Map;
import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.util.EnumValues;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.map.ResolvableSerializer;
import java.util.EnumMap;

@JacksonStdImpl
public class EnumMapSerializer extends ContainerSerializerBase<EnumMap<? extends Enum<?>, ?>> implements ResolvableSerializer
{
    protected final boolean _staticTyping;
    protected final EnumValues _keyEnums;
    protected final JavaType _valueType;
    protected final BeanProperty _property;
    protected JsonSerializer<Object> _valueSerializer;
    protected final TypeSerializer _valueTypeSerializer;
    
    @Deprecated
    public EnumMapSerializer(final JavaType valueType, final boolean staticTyping, final EnumValues keyEnums, final TypeSerializer vts, final BeanProperty property) {
        this(valueType, staticTyping, keyEnums, vts, property, null);
    }
    
    public EnumMapSerializer(final JavaType valueType, final boolean staticTyping, final EnumValues keyEnums, final TypeSerializer vts, final BeanProperty property, final JsonSerializer<Object> valueSerializer) {
        super(EnumMap.class, false);
        this._staticTyping = (staticTyping || (valueType != null && valueType.isFinal()));
        this._valueType = valueType;
        this._keyEnums = keyEnums;
        this._valueTypeSerializer = vts;
        this._property = property;
        this._valueSerializer = valueSerializer;
    }
    
    @Override
    public ContainerSerializerBase<?> _withValueTypeSerializer(final TypeSerializer vts) {
        return new EnumMapSerializer(this._valueType, this._staticTyping, this._keyEnums, vts, this._property, this._valueSerializer);
    }
    
    @Override
    public void serialize(final EnumMap<? extends Enum<?>, ?> value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        jgen.writeStartObject();
        if (!value.isEmpty()) {
            this.serializeContents(value, jgen, provider);
        }
        jgen.writeEndObject();
    }
    
    @Override
    public void serializeWithType(final EnumMap<? extends Enum<?>, ?> value, final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException, JsonGenerationException {
        typeSer.writeTypePrefixForObject(value, jgen);
        if (!value.isEmpty()) {
            this.serializeContents(value, jgen, provider);
        }
        typeSer.writeTypeSuffixForObject(value, jgen);
    }
    
    protected void serializeContents(final EnumMap<? extends Enum<?>, ?> value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        if (this._valueSerializer != null) {
            this.serializeContentsUsing(value, jgen, provider, this._valueSerializer);
            return;
        }
        JsonSerializer<Object> prevSerializer = null;
        Class<?> prevClass = null;
        EnumValues keyEnums = this._keyEnums;
        for (final Map.Entry<? extends Enum<?>, ?> entry : value.entrySet()) {
            final Enum<?> key = (Enum<?>)entry.getKey();
            if (keyEnums == null) {
                final SerializerBase<?> ser = (SerializerBase)provider.findValueSerializer(key.getDeclaringClass(), this._property);
                keyEnums = ((EnumSerializer)ser).getEnumValues();
            }
            jgen.writeFieldName(keyEnums.serializedValueFor(key));
            final Object valueElem = entry.getValue();
            if (valueElem == null) {
                provider.defaultSerializeNull(jgen);
            }
            else {
                final Class<?> cc = valueElem.getClass();
                JsonSerializer<Object> currSerializer;
                if (cc == prevClass) {
                    currSerializer = prevSerializer;
                }
                else {
                    currSerializer = (prevSerializer = provider.findValueSerializer(cc, this._property));
                    prevClass = cc;
                }
                try {
                    currSerializer.serialize(valueElem, jgen, provider);
                }
                catch (final Exception e) {
                    this.wrapAndThrow(provider, e, value, ((Enum)entry.getKey()).name());
                }
            }
        }
    }
    
    protected void serializeContentsUsing(final EnumMap<? extends Enum<?>, ?> value, final JsonGenerator jgen, final SerializerProvider provider, final JsonSerializer<Object> valueSer) throws IOException, JsonGenerationException {
        EnumValues keyEnums = this._keyEnums;
        for (final Map.Entry<? extends Enum<?>, ?> entry : value.entrySet()) {
            final Enum<?> key = (Enum<?>)entry.getKey();
            if (keyEnums == null) {
                final SerializerBase<?> ser = (SerializerBase)provider.findValueSerializer(key.getDeclaringClass(), this._property);
                keyEnums = ((EnumSerializer)ser).getEnumValues();
            }
            jgen.writeFieldName(keyEnums.serializedValueFor(key));
            final Object valueElem = entry.getValue();
            if (valueElem == null) {
                provider.defaultSerializeNull(jgen);
            }
            else {
                try {
                    valueSer.serialize(valueElem, jgen, provider);
                }
                catch (final Exception e) {
                    this.wrapAndThrow(provider, e, value, ((Enum)entry.getKey()).name());
                }
            }
        }
    }
    
    public void resolve(final SerializerProvider provider) throws JsonMappingException {
        if (this._staticTyping && this._valueSerializer == null) {
            this._valueSerializer = provider.findValueSerializer(this._valueType, this._property);
        }
    }
    
    @Override
    public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) throws JsonMappingException {
        final ObjectNode o = this.createSchemaNode("object", true);
        if (typeHint instanceof ParameterizedType) {
            final Type[] typeArgs = ((ParameterizedType)typeHint).getActualTypeArguments();
            if (typeArgs.length == 2) {
                final JavaType enumType = provider.constructType(typeArgs[0]);
                final JavaType valueType = provider.constructType(typeArgs[1]);
                final ObjectNode propsNode = JsonNodeFactory.instance.objectNode();
                final Class<Enum<?>> enumClass = (Class<Enum<?>>)enumType.getRawClass();
                for (final Enum<?> enumValue : enumClass.getEnumConstants()) {
                    final JsonSerializer<Object> ser = provider.findValueSerializer(valueType.getRawClass(), this._property);
                    final JsonNode schemaNode = (ser instanceof SchemaAware) ? ((SchemaAware)ser).getSchema(provider, null) : JsonSchema.getDefaultSchemaNode();
                    propsNode.put(provider.getConfig().getAnnotationIntrospector().findEnumValue(enumValue), schemaNode);
                }
                o.put("properties", propsNode);
            }
        }
        return o;
    }
}
