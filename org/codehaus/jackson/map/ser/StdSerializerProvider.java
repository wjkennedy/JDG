// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser;

import org.codehaus.jackson.map.ser.impl.UnknownSerializer;
import org.codehaus.jackson.map.ser.std.StdKeySerializer;
import org.codehaus.jackson.map.ser.impl.FailingSerializer;
import org.codehaus.jackson.map.ResolvableSerializer;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.JsonProcessingException;
import java.util.Date;
import org.codehaus.jackson.map.ContextualSerializer;
import org.codehaus.jackson.map.ser.std.StdKeySerializers;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import java.lang.reflect.Type;
import org.codehaus.jackson.schema.SchemaAware;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.schema.JsonSchema;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ser.std.NullSerializer;
import org.codehaus.jackson.map.SerializationConfig;
import java.text.DateFormat;
import org.codehaus.jackson.map.ser.impl.ReadOnlyClassToSerializerMap;
import org.codehaus.jackson.map.util.RootNameLookup;
import org.codehaus.jackson.map.ser.impl.SerializerCache;
import org.codehaus.jackson.map.SerializerFactory;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

public class StdSerializerProvider extends SerializerProvider
{
    static final boolean CACHE_UNKNOWN_MAPPINGS = false;
    public static final JsonSerializer<Object> DEFAULT_NULL_KEY_SERIALIZER;
    @Deprecated
    public static final JsonSerializer<Object> DEFAULT_KEY_SERIALIZER;
    public static final JsonSerializer<Object> DEFAULT_UNKNOWN_SERIALIZER;
    protected final SerializerFactory _serializerFactory;
    protected final SerializerCache _serializerCache;
    protected final RootNameLookup _rootNames;
    protected JsonSerializer<Object> _unknownTypeSerializer;
    protected JsonSerializer<Object> _keySerializer;
    protected JsonSerializer<Object> _nullValueSerializer;
    protected JsonSerializer<Object> _nullKeySerializer;
    protected final ReadOnlyClassToSerializerMap _knownSerializers;
    protected DateFormat _dateFormat;
    
    public StdSerializerProvider() {
        super(null);
        this._unknownTypeSerializer = StdSerializerProvider.DEFAULT_UNKNOWN_SERIALIZER;
        this._nullValueSerializer = NullSerializer.instance;
        this._nullKeySerializer = StdSerializerProvider.DEFAULT_NULL_KEY_SERIALIZER;
        this._serializerFactory = null;
        this._serializerCache = new SerializerCache();
        this._knownSerializers = null;
        this._rootNames = new RootNameLookup();
    }
    
    protected StdSerializerProvider(final SerializationConfig config, final StdSerializerProvider src, final SerializerFactory f) {
        super(config);
        this._unknownTypeSerializer = StdSerializerProvider.DEFAULT_UNKNOWN_SERIALIZER;
        this._nullValueSerializer = NullSerializer.instance;
        this._nullKeySerializer = StdSerializerProvider.DEFAULT_NULL_KEY_SERIALIZER;
        if (config == null) {
            throw new NullPointerException();
        }
        this._serializerFactory = f;
        this._serializerCache = src._serializerCache;
        this._unknownTypeSerializer = src._unknownTypeSerializer;
        this._keySerializer = src._keySerializer;
        this._nullValueSerializer = src._nullValueSerializer;
        this._nullKeySerializer = src._nullKeySerializer;
        this._rootNames = src._rootNames;
        this._knownSerializers = this._serializerCache.getReadOnlyLookupMap();
    }
    
    protected StdSerializerProvider createInstance(final SerializationConfig config, final SerializerFactory jsf) {
        return new StdSerializerProvider(config, this, jsf);
    }
    
    @Override
    public void setDefaultKeySerializer(final JsonSerializer<Object> ks) {
        if (ks == null) {
            throw new IllegalArgumentException("Can not pass null JsonSerializer");
        }
        this._keySerializer = ks;
    }
    
    @Override
    public void setNullValueSerializer(final JsonSerializer<Object> nvs) {
        if (nvs == null) {
            throw new IllegalArgumentException("Can not pass null JsonSerializer");
        }
        this._nullValueSerializer = nvs;
    }
    
    @Override
    public void setNullKeySerializer(final JsonSerializer<Object> nks) {
        if (nks == null) {
            throw new IllegalArgumentException("Can not pass null JsonSerializer");
        }
        this._nullKeySerializer = nks;
    }
    
    @Override
    public final void serializeValue(final SerializationConfig config, final JsonGenerator jgen, final Object value, final SerializerFactory jsf) throws IOException, JsonGenerationException {
        if (jsf == null) {
            throw new IllegalArgumentException("Can not pass null serializerFactory");
        }
        final StdSerializerProvider inst = this.createInstance(config, jsf);
        if (inst.getClass() != this.getClass()) {
            throw new IllegalStateException("Broken serializer provider: createInstance returned instance of type " + inst.getClass() + "; blueprint of type " + this.getClass());
        }
        inst._serializeValue(jgen, value);
    }
    
    @Override
    public final void serializeValue(final SerializationConfig config, final JsonGenerator jgen, final Object value, final JavaType rootType, final SerializerFactory jsf) throws IOException, JsonGenerationException {
        if (jsf == null) {
            throw new IllegalArgumentException("Can not pass null serializerFactory");
        }
        final StdSerializerProvider inst = this.createInstance(config, jsf);
        if (inst.getClass() != this.getClass()) {
            throw new IllegalStateException("Broken serializer provider: createInstance returned instance of type " + inst.getClass() + "; blueprint of type " + this.getClass());
        }
        inst._serializeValue(jgen, value, rootType);
    }
    
    @Override
    public JsonSchema generateJsonSchema(final Class<?> type, final SerializationConfig config, final SerializerFactory jsf) throws JsonMappingException {
        if (type == null) {
            throw new IllegalArgumentException("A class must be provided");
        }
        final StdSerializerProvider inst = this.createInstance(config, jsf);
        if (inst.getClass() != this.getClass()) {
            throw new IllegalStateException("Broken serializer provider: createInstance returned instance of type " + inst.getClass() + "; blueprint of type " + this.getClass());
        }
        final JsonSerializer<Object> ser = inst.findValueSerializer(type, null);
        final JsonNode schemaNode = (ser instanceof SchemaAware) ? ((SchemaAware)ser).getSchema(inst, null) : JsonSchema.getDefaultSchemaNode();
        if (!(schemaNode instanceof ObjectNode)) {
            throw new IllegalArgumentException("Class " + type.getName() + " would not be serialized as a JSON object and therefore has no schema");
        }
        return new JsonSchema((ObjectNode)schemaNode);
    }
    
    @Override
    public boolean hasSerializerFor(final SerializationConfig config, final Class<?> cls, final SerializerFactory jsf) {
        return this.createInstance(config, jsf)._findExplicitUntypedSerializer(cls, null) != null;
    }
    
    @Override
    public int cachedSerializersCount() {
        return this._serializerCache.size();
    }
    
    @Override
    public void flushCachedSerializers() {
        this._serializerCache.flush();
    }
    
    @Override
    public JsonSerializer<Object> findValueSerializer(final Class<?> valueType, final BeanProperty property) throws JsonMappingException {
        JsonSerializer<Object> ser = this._knownSerializers.untypedValueSerializer(valueType);
        if (ser == null) {
            ser = this._serializerCache.untypedValueSerializer(valueType);
            if (ser == null) {
                ser = this._serializerCache.untypedValueSerializer(this._config.constructType(valueType));
                if (ser == null) {
                    ser = this._createAndCacheUntypedSerializer(valueType, property);
                    if (ser == null) {
                        ser = this.getUnknownTypeSerializer(valueType);
                        return ser;
                    }
                }
            }
        }
        return this._handleContextualResolvable(ser, property);
    }
    
    @Override
    public JsonSerializer<Object> findValueSerializer(final JavaType valueType, final BeanProperty property) throws JsonMappingException {
        JsonSerializer<Object> ser = this._knownSerializers.untypedValueSerializer(valueType);
        if (ser == null) {
            ser = this._serializerCache.untypedValueSerializer(valueType);
            if (ser == null) {
                ser = this._createAndCacheUntypedSerializer(valueType, property);
                if (ser == null) {
                    ser = this.getUnknownTypeSerializer(valueType.getRawClass());
                    return ser;
                }
            }
        }
        return this._handleContextualResolvable(ser, property);
    }
    
    @Override
    public JsonSerializer<Object> findTypedValueSerializer(final Class<?> valueType, final boolean cache, final BeanProperty property) throws JsonMappingException {
        JsonSerializer<Object> ser = this._knownSerializers.typedValueSerializer(valueType);
        if (ser != null) {
            return ser;
        }
        ser = this._serializerCache.typedValueSerializer(valueType);
        if (ser != null) {
            return ser;
        }
        ser = this.findValueSerializer(valueType, property);
        final TypeSerializer typeSer = this._serializerFactory.createTypeSerializer(this._config, this._config.constructType(valueType), property);
        if (typeSer != null) {
            ser = new WrappedSerializer(typeSer, ser);
        }
        if (cache) {
            this._serializerCache.addTypedSerializer(valueType, ser);
        }
        return ser;
    }
    
    @Override
    public JsonSerializer<Object> findTypedValueSerializer(final JavaType valueType, final boolean cache, final BeanProperty property) throws JsonMappingException {
        JsonSerializer<Object> ser = this._knownSerializers.typedValueSerializer(valueType);
        if (ser != null) {
            return ser;
        }
        ser = this._serializerCache.typedValueSerializer(valueType);
        if (ser != null) {
            return ser;
        }
        ser = this.findValueSerializer(valueType, property);
        final TypeSerializer typeSer = this._serializerFactory.createTypeSerializer(this._config, valueType, property);
        if (typeSer != null) {
            ser = new WrappedSerializer(typeSer, ser);
        }
        if (cache) {
            this._serializerCache.addTypedSerializer(valueType, ser);
        }
        return ser;
    }
    
    @Override
    public JsonSerializer<Object> findKeySerializer(final JavaType keyType, final BeanProperty property) throws JsonMappingException {
        JsonSerializer<Object> ser = this._serializerFactory.createKeySerializer(this._config, keyType, property);
        if (ser == null) {
            if (this._keySerializer == null) {
                ser = StdKeySerializers.getStdKeySerializer(keyType);
            }
            else {
                ser = this._keySerializer;
            }
        }
        if (ser instanceof ContextualSerializer) {
            final ContextualSerializer<?> contextual = (ContextualSerializer)ser;
            ser = (JsonSerializer<Object>)contextual.createContextual(this._config, property);
        }
        return ser;
    }
    
    @Override
    public JsonSerializer<Object> getNullKeySerializer() {
        return this._nullKeySerializer;
    }
    
    @Override
    public JsonSerializer<Object> getNullValueSerializer() {
        return this._nullValueSerializer;
    }
    
    @Override
    public JsonSerializer<Object> getUnknownTypeSerializer(final Class<?> unknownType) {
        return this._unknownTypeSerializer;
    }
    
    @Override
    public final void defaultSerializeDateValue(final long timestamp, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        if (this.isEnabled(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS)) {
            jgen.writeNumber(timestamp);
        }
        else {
            if (this._dateFormat == null) {
                this._dateFormat = (DateFormat)this._config.getDateFormat().clone();
            }
            jgen.writeString(this._dateFormat.format(new Date(timestamp)));
        }
    }
    
    @Override
    public final void defaultSerializeDateValue(final Date date, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        if (this.isEnabled(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS)) {
            jgen.writeNumber(date.getTime());
        }
        else {
            if (this._dateFormat == null) {
                final DateFormat blueprint = this._config.getDateFormat();
                this._dateFormat = (DateFormat)blueprint.clone();
            }
            jgen.writeString(this._dateFormat.format(date));
        }
    }
    
    @Override
    public void defaultSerializeDateKey(final long timestamp, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        if (this.isEnabled(SerializationConfig.Feature.WRITE_DATE_KEYS_AS_TIMESTAMPS)) {
            jgen.writeFieldName(String.valueOf(timestamp));
        }
        else {
            if (this._dateFormat == null) {
                final DateFormat blueprint = this._config.getDateFormat();
                this._dateFormat = (DateFormat)blueprint.clone();
            }
            jgen.writeFieldName(this._dateFormat.format(new Date(timestamp)));
        }
    }
    
    @Override
    public void defaultSerializeDateKey(final Date date, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        if (this.isEnabled(SerializationConfig.Feature.WRITE_DATE_KEYS_AS_TIMESTAMPS)) {
            jgen.writeFieldName(String.valueOf(date.getTime()));
        }
        else {
            if (this._dateFormat == null) {
                final DateFormat blueprint = this._config.getDateFormat();
                this._dateFormat = (DateFormat)blueprint.clone();
            }
            jgen.writeFieldName(this._dateFormat.format(date));
        }
    }
    
    protected void _serializeValue(final JsonGenerator jgen, final Object value) throws IOException, JsonProcessingException {
        JsonSerializer<Object> ser;
        boolean wrap;
        if (value == null) {
            ser = this.getNullValueSerializer();
            wrap = false;
        }
        else {
            final Class<?> cls = value.getClass();
            ser = this.findTypedValueSerializer(cls, true, null);
            wrap = this._config.isEnabled(SerializationConfig.Feature.WRAP_ROOT_VALUE);
            if (wrap) {
                jgen.writeStartObject();
                jgen.writeFieldName(this._rootNames.findRootName(value.getClass(), this._config));
            }
        }
        try {
            ser.serialize(value, jgen, this);
            if (wrap) {
                jgen.writeEndObject();
            }
        }
        catch (final IOException ioe) {
            throw ioe;
        }
        catch (final Exception e) {
            String msg = e.getMessage();
            if (msg == null) {
                msg = "[no message for " + e.getClass().getName() + "]";
            }
            throw new JsonMappingException(msg, e);
        }
    }
    
    protected void _serializeValue(final JsonGenerator jgen, final Object value, final JavaType rootType) throws IOException, JsonProcessingException {
        JsonSerializer<Object> ser;
        boolean wrap;
        if (value == null) {
            ser = this.getNullValueSerializer();
            wrap = false;
        }
        else {
            if (!rootType.getRawClass().isAssignableFrom(value.getClass())) {
                this._reportIncompatibleRootType(value, rootType);
            }
            ser = this.findTypedValueSerializer(rootType, true, null);
            wrap = this._config.isEnabled(SerializationConfig.Feature.WRAP_ROOT_VALUE);
            if (wrap) {
                jgen.writeStartObject();
                jgen.writeFieldName(this._rootNames.findRootName(rootType, this._config));
            }
        }
        try {
            ser.serialize(value, jgen, this);
            if (wrap) {
                jgen.writeEndObject();
            }
        }
        catch (final IOException ioe) {
            throw ioe;
        }
        catch (final Exception e) {
            String msg = e.getMessage();
            if (msg == null) {
                msg = "[no message for " + e.getClass().getName() + "]";
            }
            throw new JsonMappingException(msg, e);
        }
    }
    
    protected void _reportIncompatibleRootType(final Object value, final JavaType rootType) throws IOException, JsonProcessingException {
        if (rootType.isPrimitive()) {
            final Class<?> wrapperType = ClassUtil.wrapperType(rootType.getRawClass());
            if (wrapperType.isAssignableFrom(value.getClass())) {
                return;
            }
        }
        throw new JsonMappingException("Incompatible types: declared root type (" + rootType + ") vs " + value.getClass().getName());
    }
    
    protected JsonSerializer<Object> _findExplicitUntypedSerializer(final Class<?> runtimeType, final BeanProperty property) {
        JsonSerializer<Object> ser = this._knownSerializers.untypedValueSerializer(runtimeType);
        if (ser != null) {
            return ser;
        }
        ser = this._serializerCache.untypedValueSerializer(runtimeType);
        if (ser != null) {
            return ser;
        }
        try {
            return this._createAndCacheUntypedSerializer(runtimeType, property);
        }
        catch (final Exception e) {
            return null;
        }
    }
    
    protected JsonSerializer<Object> _createAndCacheUntypedSerializer(final Class<?> type, final BeanProperty property) throws JsonMappingException {
        JsonSerializer<Object> ser;
        try {
            ser = this._createUntypedSerializer(this._config.constructType(type), property);
        }
        catch (final IllegalArgumentException iae) {
            throw new JsonMappingException(iae.getMessage(), null, iae);
        }
        if (ser != null) {
            this._serializerCache.addAndResolveNonTypedSerializer(type, ser, this);
        }
        return ser;
    }
    
    protected JsonSerializer<Object> _createAndCacheUntypedSerializer(final JavaType type, final BeanProperty property) throws JsonMappingException {
        JsonSerializer<Object> ser;
        try {
            ser = this._createUntypedSerializer(type, property);
        }
        catch (final IllegalArgumentException iae) {
            throw new JsonMappingException(iae.getMessage(), null, iae);
        }
        if (ser != null) {
            this._serializerCache.addAndResolveNonTypedSerializer(type, ser, this);
        }
        return ser;
    }
    
    protected JsonSerializer<Object> _createUntypedSerializer(final JavaType type, final BeanProperty property) throws JsonMappingException {
        return this._serializerFactory.createSerializer(this._config, type, property);
    }
    
    protected JsonSerializer<Object> _handleContextualResolvable(JsonSerializer<Object> ser, final BeanProperty property) throws JsonMappingException {
        if (!(ser instanceof ContextualSerializer)) {
            return ser;
        }
        final JsonSerializer<Object> ctxtSer = ((ContextualSerializer)ser).createContextual(this._config, property);
        if (ctxtSer != ser) {
            if (ctxtSer instanceof ResolvableSerializer) {
                ((ResolvableSerializer)ctxtSer).resolve(this);
            }
            ser = ctxtSer;
        }
        return ser;
    }
    
    static {
        DEFAULT_NULL_KEY_SERIALIZER = new FailingSerializer("Null key for a Map not allowed in JSON (use a converting NullKeySerializer?)");
        DEFAULT_KEY_SERIALIZER = new StdKeySerializer();
        DEFAULT_UNKNOWN_SERIALIZER = new UnknownSerializer();
    }
    
    private static final class WrappedSerializer extends JsonSerializer<Object>
    {
        protected final TypeSerializer _typeSerializer;
        protected final JsonSerializer<Object> _serializer;
        
        public WrappedSerializer(final TypeSerializer typeSer, final JsonSerializer<Object> ser) {
            this._typeSerializer = typeSer;
            this._serializer = ser;
        }
        
        @Override
        public void serialize(final Object value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonProcessingException {
            this._serializer.serializeWithType(value, jgen, provider, this._typeSerializer);
        }
        
        @Override
        public void serializeWithType(final Object value, final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException, JsonProcessingException {
            this._serializer.serializeWithType(value, jgen, provider, typeSer);
        }
        
        @Override
        public Class<Object> handledType() {
            return Object.class;
        }
    }
}
