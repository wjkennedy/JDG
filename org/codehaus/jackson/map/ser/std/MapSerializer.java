// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.std;

import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.JsonNode;
import java.lang.reflect.Type;
import java.util.Iterator;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ser.impl.PropertySerializerMap;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.JsonSerializer;
import java.util.HashSet;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.map.ResolvableSerializer;
import java.util.Map;

@JacksonStdImpl
public class MapSerializer extends ContainerSerializerBase<Map<?, ?>> implements ResolvableSerializer
{
    protected static final JavaType UNSPECIFIED_TYPE;
    protected final BeanProperty _property;
    protected final HashSet<String> _ignoredEntries;
    protected final boolean _valueTypeIsStatic;
    protected final JavaType _keyType;
    protected final JavaType _valueType;
    protected JsonSerializer<Object> _keySerializer;
    protected JsonSerializer<Object> _valueSerializer;
    protected final TypeSerializer _valueTypeSerializer;
    protected PropertySerializerMap _dynamicValueSerializers;
    
    protected MapSerializer() {
        this(null, null, null, false, null, null, null, null);
    }
    
    protected MapSerializer(final HashSet<String> ignoredEntries, final JavaType keyType, final JavaType valueType, final boolean valueTypeIsStatic, final TypeSerializer vts, final JsonSerializer<Object> keySerializer, final JsonSerializer<Object> valueSerializer, final BeanProperty property) {
        super(Map.class, false);
        this._property = property;
        this._ignoredEntries = ignoredEntries;
        this._keyType = keyType;
        this._valueType = valueType;
        this._valueTypeIsStatic = valueTypeIsStatic;
        this._valueTypeSerializer = vts;
        this._keySerializer = keySerializer;
        this._valueSerializer = valueSerializer;
        this._dynamicValueSerializers = PropertySerializerMap.emptyMap();
    }
    
    @Override
    public ContainerSerializerBase<?> _withValueTypeSerializer(final TypeSerializer vts) {
        final MapSerializer ms = new MapSerializer(this._ignoredEntries, this._keyType, this._valueType, this._valueTypeIsStatic, vts, this._keySerializer, this._valueSerializer, this._property);
        if (this._valueSerializer != null) {
            ms._valueSerializer = this._valueSerializer;
        }
        return ms;
    }
    
    @Deprecated
    public static MapSerializer construct(final String[] ignoredList, final JavaType mapType, final boolean staticValueType, final TypeSerializer vts, final BeanProperty property) {
        return construct(ignoredList, mapType, staticValueType, vts, property, null, null);
    }
    
    public static MapSerializer construct(final String[] ignoredList, final JavaType mapType, boolean staticValueType, final TypeSerializer vts, final BeanProperty property, final JsonSerializer<Object> keySerializer, final JsonSerializer<Object> valueSerializer) {
        final HashSet<String> ignoredEntries = toSet(ignoredList);
        JavaType keyType;
        JavaType valueType;
        if (mapType == null) {
            valueType = (keyType = MapSerializer.UNSPECIFIED_TYPE);
        }
        else {
            keyType = mapType.getKeyType();
            valueType = mapType.getContentType();
        }
        if (!staticValueType) {
            staticValueType = (valueType != null && valueType.isFinal());
        }
        return new MapSerializer(ignoredEntries, keyType, valueType, staticValueType, vts, keySerializer, valueSerializer, property);
    }
    
    private static HashSet<String> toSet(final String[] ignoredEntries) {
        if (ignoredEntries == null || ignoredEntries.length == 0) {
            return null;
        }
        final HashSet<String> result = new HashSet<String>(ignoredEntries.length);
        for (final String prop : ignoredEntries) {
            result.add(prop);
        }
        return result;
    }
    
    @Override
    public void serialize(final Map<?, ?> value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        jgen.writeStartObject();
        if (!value.isEmpty()) {
            if (this._valueSerializer != null) {
                this.serializeFieldsUsing(value, jgen, provider, this._valueSerializer);
            }
            else {
                this.serializeFields(value, jgen, provider);
            }
        }
        jgen.writeEndObject();
    }
    
    @Override
    public void serializeWithType(final Map<?, ?> value, final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException, JsonGenerationException {
        typeSer.writeTypePrefixForObject(value, jgen);
        if (!value.isEmpty()) {
            if (this._valueSerializer != null) {
                this.serializeFieldsUsing(value, jgen, provider, this._valueSerializer);
            }
            else {
                this.serializeFields(value, jgen, provider);
            }
        }
        typeSer.writeTypeSuffixForObject(value, jgen);
    }
    
    public void serializeFields(final Map<?, ?> value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        if (this._valueTypeSerializer != null) {
            this.serializeTypedFields(value, jgen, provider);
            return;
        }
        final JsonSerializer<Object> keySerializer = this._keySerializer;
        final HashSet<String> ignored = this._ignoredEntries;
        final boolean skipNulls = !provider.isEnabled(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES);
        PropertySerializerMap serializers = this._dynamicValueSerializers;
        for (final Map.Entry<?, ?> entry : value.entrySet()) {
            final Object valueElem = entry.getValue();
            final Object keyElem = entry.getKey();
            if (keyElem == null) {
                provider.getNullKeySerializer().serialize(null, jgen, provider);
            }
            else {
                if (skipNulls && valueElem == null) {
                    continue;
                }
                if (ignored != null && ignored.contains(keyElem)) {
                    continue;
                }
                keySerializer.serialize(keyElem, jgen, provider);
            }
            if (valueElem == null) {
                provider.defaultSerializeNull(jgen);
            }
            else {
                final Class<?> cc = valueElem.getClass();
                JsonSerializer<Object> serializer = serializers.serializerFor(cc);
                if (serializer == null) {
                    if (this._valueType.hasGenericTypes()) {
                        serializer = this._findAndAddDynamic(serializers, provider.constructSpecializedType(this._valueType, cc), provider);
                    }
                    else {
                        serializer = this._findAndAddDynamic(serializers, cc, provider);
                    }
                    serializers = this._dynamicValueSerializers;
                }
                try {
                    serializer.serialize(valueElem, jgen, provider);
                }
                catch (final Exception e) {
                    final String keyDesc = "" + keyElem;
                    this.wrapAndThrow(provider, e, value, keyDesc);
                }
            }
        }
    }
    
    protected void serializeFieldsUsing(final Map<?, ?> value, final JsonGenerator jgen, final SerializerProvider provider, final JsonSerializer<Object> ser) throws IOException, JsonGenerationException {
        final JsonSerializer<Object> keySerializer = this._keySerializer;
        final HashSet<String> ignored = this._ignoredEntries;
        final TypeSerializer typeSer = this._valueTypeSerializer;
        final boolean skipNulls = !provider.isEnabled(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES);
        for (final Map.Entry<?, ?> entry : value.entrySet()) {
            final Object valueElem = entry.getValue();
            final Object keyElem = entry.getKey();
            if (keyElem == null) {
                provider.getNullKeySerializer().serialize(null, jgen, provider);
            }
            else {
                if (skipNulls && valueElem == null) {
                    continue;
                }
                if (ignored != null && ignored.contains(keyElem)) {
                    continue;
                }
                keySerializer.serialize(keyElem, jgen, provider);
            }
            if (valueElem == null) {
                provider.defaultSerializeNull(jgen);
            }
            else {
                try {
                    if (typeSer == null) {
                        ser.serialize(valueElem, jgen, provider);
                    }
                    else {
                        ser.serializeWithType(valueElem, jgen, provider, typeSer);
                    }
                }
                catch (final Exception e) {
                    final String keyDesc = "" + keyElem;
                    this.wrapAndThrow(provider, e, value, keyDesc);
                }
            }
        }
    }
    
    protected void serializeTypedFields(final Map<?, ?> value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        final JsonSerializer<Object> keySerializer = this._keySerializer;
        JsonSerializer<Object> prevValueSerializer = null;
        Class<?> prevValueClass = null;
        final HashSet<String> ignored = this._ignoredEntries;
        final boolean skipNulls = !provider.isEnabled(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES);
        for (final Map.Entry<?, ?> entry : value.entrySet()) {
            final Object valueElem = entry.getValue();
            final Object keyElem = entry.getKey();
            if (keyElem == null) {
                provider.getNullKeySerializer().serialize(null, jgen, provider);
            }
            else {
                if (skipNulls && valueElem == null) {
                    continue;
                }
                if (ignored != null && ignored.contains(keyElem)) {
                    continue;
                }
                keySerializer.serialize(keyElem, jgen, provider);
            }
            if (valueElem == null) {
                provider.defaultSerializeNull(jgen);
            }
            else {
                final Class<?> cc = valueElem.getClass();
                JsonSerializer<Object> currSerializer;
                if (cc == prevValueClass) {
                    currSerializer = prevValueSerializer;
                }
                else {
                    if (this._valueType.hasGenericTypes()) {
                        currSerializer = provider.findValueSerializer(provider.constructSpecializedType(this._valueType, cc), this._property);
                    }
                    else {
                        currSerializer = provider.findValueSerializer(cc, this._property);
                    }
                    prevValueSerializer = currSerializer;
                    prevValueClass = cc;
                }
                try {
                    currSerializer.serializeWithType(valueElem, jgen, provider, this._valueTypeSerializer);
                }
                catch (final Exception e) {
                    final String keyDesc = "" + keyElem;
                    this.wrapAndThrow(provider, e, value, keyDesc);
                }
            }
        }
    }
    
    @Override
    public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
        final ObjectNode o = this.createSchemaNode("object", true);
        return o;
    }
    
    public void resolve(final SerializerProvider provider) throws JsonMappingException {
        if (this._valueTypeIsStatic && this._valueSerializer == null) {
            this._valueSerializer = provider.findValueSerializer(this._valueType, this._property);
        }
        if (this._keySerializer == null) {
            this._keySerializer = provider.findKeySerializer(this._keyType, this._property);
        }
    }
    
    protected final JsonSerializer<Object> _findAndAddDynamic(final PropertySerializerMap map, final Class<?> type, final SerializerProvider provider) throws JsonMappingException {
        final PropertySerializerMap.SerializerAndMapResult result = map.findAndAddSerializer(type, provider, this._property);
        if (map != result.map) {
            this._dynamicValueSerializers = result.map;
        }
        return result.serializer;
    }
    
    protected final JsonSerializer<Object> _findAndAddDynamic(final PropertySerializerMap map, final JavaType type, final SerializerProvider provider) throws JsonMappingException {
        final PropertySerializerMap.SerializerAndMapResult result = map.findAndAddSerializer(type, provider, this._property);
        if (map != result.map) {
            this._dynamicValueSerializers = result.map;
        }
        return result.serializer;
    }
    
    static {
        UNSPECIFIED_TYPE = TypeFactory.unknownType();
    }
}
