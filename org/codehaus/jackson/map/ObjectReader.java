// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.map.type.SimpleType;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.node.TreeTraversingParser;
import org.codehaus.jackson.io.SerializedString;
import org.codehaus.jackson.map.deser.StdDeserializationContext;
import java.io.EOFException;
import org.codehaus.jackson.node.NullNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonToken;
import java.net.URL;
import java.io.File;
import java.io.Reader;
import java.io.InputStream;
import java.util.Iterator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.type.TypeReference;
import java.lang.reflect.Type;
import org.codehaus.jackson.util.VersionUtil;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.FormatSchema;
import org.codehaus.jackson.JsonFactory;
import java.util.concurrent.ConcurrentHashMap;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.Versioned;
import org.codehaus.jackson.ObjectCodec;

public class ObjectReader extends ObjectCodec implements Versioned
{
    private static final JavaType JSON_NODE_TYPE;
    protected final DeserializationConfig _config;
    protected final boolean _unwrapRoot;
    protected final ConcurrentHashMap<JavaType, JsonDeserializer<Object>> _rootDeserializers;
    protected final DeserializerProvider _provider;
    protected final JsonFactory _jsonFactory;
    protected final JavaType _valueType;
    protected final Object _valueToUpdate;
    protected final FormatSchema _schema;
    protected final InjectableValues _injectableValues;
    
    protected ObjectReader(final ObjectMapper mapper, final DeserializationConfig config) {
        this(mapper, config, null, null, null, null);
    }
    
    protected ObjectReader(final ObjectMapper mapper, final DeserializationConfig config, final JavaType valueType, final Object valueToUpdate, final FormatSchema schema, final InjectableValues injectableValues) {
        this._config = config;
        this._rootDeserializers = mapper._rootDeserializers;
        this._provider = mapper._deserializerProvider;
        this._jsonFactory = mapper._jsonFactory;
        this._valueType = valueType;
        this._valueToUpdate = valueToUpdate;
        if (valueToUpdate != null && valueType.isArrayType()) {
            throw new IllegalArgumentException("Can not update an array value");
        }
        this._schema = schema;
        this._injectableValues = injectableValues;
        this._unwrapRoot = config.isEnabled(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE);
    }
    
    protected ObjectReader(final ObjectReader base, final DeserializationConfig config, final JavaType valueType, final Object valueToUpdate, final FormatSchema schema, final InjectableValues injectableValues) {
        this._config = config;
        this._rootDeserializers = base._rootDeserializers;
        this._provider = base._provider;
        this._jsonFactory = base._jsonFactory;
        this._valueType = valueType;
        this._valueToUpdate = valueToUpdate;
        if (valueToUpdate != null && valueType.isArrayType()) {
            throw new IllegalArgumentException("Can not update an array value");
        }
        this._schema = schema;
        this._injectableValues = injectableValues;
        this._unwrapRoot = config.isEnabled(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE);
    }
    
    public Version version() {
        return VersionUtil.versionFor(this.getClass());
    }
    
    public ObjectReader withType(final JavaType valueType) {
        if (valueType == this._valueType) {
            return this;
        }
        return new ObjectReader(this, this._config, valueType, this._valueToUpdate, this._schema, this._injectableValues);
    }
    
    public ObjectReader withType(final Class<?> valueType) {
        return this.withType(this._config.constructType(valueType));
    }
    
    public ObjectReader withType(final Type valueType) {
        return this.withType(this._config.getTypeFactory().constructType(valueType));
    }
    
    public ObjectReader withType(final TypeReference<?> valueTypeRef) {
        return this.withType(this._config.getTypeFactory().constructType(valueTypeRef.getType()));
    }
    
    public ObjectReader withNodeFactory(final JsonNodeFactory f) {
        if (f == this._config.getNodeFactory()) {
            return this;
        }
        return new ObjectReader(this, this._config.withNodeFactory(f), this._valueType, this._valueToUpdate, this._schema, this._injectableValues);
    }
    
    public ObjectReader withValueToUpdate(final Object value) {
        if (value == this._valueToUpdate) {
            return this;
        }
        if (value == null) {
            throw new IllegalArgumentException("cat not update null value");
        }
        final JavaType t = (this._valueType == null) ? this._config.constructType(value.getClass()) : this._valueType;
        return new ObjectReader(this, this._config, t, value, this._schema, this._injectableValues);
    }
    
    public ObjectReader withSchema(final FormatSchema schema) {
        if (this._schema == schema) {
            return this;
        }
        return new ObjectReader(this, this._config, this._valueType, this._valueToUpdate, schema, this._injectableValues);
    }
    
    public ObjectReader withInjectableValues(final InjectableValues injectableValues) {
        if (this._injectableValues == injectableValues) {
            return this;
        }
        return new ObjectReader(this, this._config, this._valueType, this._valueToUpdate, this._schema, injectableValues);
    }
    
    public <T> T readValue(final JsonParser jp) throws IOException, JsonProcessingException {
        return (T)this._bind(jp);
    }
    
    @Override
    public <T> T readValue(final JsonParser jp, final Class<T> valueType) throws IOException, JsonProcessingException {
        return this.withType(valueType).readValue(jp);
    }
    
    @Override
    public <T> T readValue(final JsonParser jp, final TypeReference<?> valueTypeRef) throws IOException, JsonProcessingException {
        return this.withType(valueTypeRef).readValue(jp);
    }
    
    @Override
    public <T> T readValue(final JsonParser jp, final JavaType valueType) throws IOException, JsonProcessingException {
        return this.withType(valueType).readValue(jp);
    }
    
    @Override
    public JsonNode readTree(final JsonParser jp) throws IOException, JsonProcessingException {
        return this._bindAsTree(jp);
    }
    
    @Override
    public <T> Iterator<T> readValues(final JsonParser jp, final Class<T> valueType) throws IOException, JsonProcessingException {
        return (Iterator<T>)this.withType(valueType).readValues(jp);
    }
    
    @Override
    public <T> Iterator<T> readValues(final JsonParser jp, final TypeReference<?> valueTypeRef) throws IOException, JsonProcessingException {
        return (Iterator<T>)this.withType(valueTypeRef).readValues(jp);
    }
    
    @Override
    public <T> Iterator<T> readValues(final JsonParser jp, final JavaType valueType) throws IOException, JsonProcessingException {
        return (Iterator<T>)this.withType(valueType).readValues(jp);
    }
    
    public <T> T readValue(final InputStream src) throws IOException, JsonProcessingException {
        return (T)this._bindAndClose(this._jsonFactory.createJsonParser(src));
    }
    
    public <T> T readValue(final Reader src) throws IOException, JsonProcessingException {
        return (T)this._bindAndClose(this._jsonFactory.createJsonParser(src));
    }
    
    public <T> T readValue(final String src) throws IOException, JsonProcessingException {
        return (T)this._bindAndClose(this._jsonFactory.createJsonParser(src));
    }
    
    public <T> T readValue(final byte[] src) throws IOException, JsonProcessingException {
        return (T)this._bindAndClose(this._jsonFactory.createJsonParser(src));
    }
    
    public <T> T readValue(final byte[] src, final int offset, final int length) throws IOException, JsonProcessingException {
        return (T)this._bindAndClose(this._jsonFactory.createJsonParser(src, offset, length));
    }
    
    public <T> T readValue(final File src) throws IOException, JsonProcessingException {
        return (T)this._bindAndClose(this._jsonFactory.createJsonParser(src));
    }
    
    public <T> T readValue(final URL src) throws IOException, JsonProcessingException {
        return (T)this._bindAndClose(this._jsonFactory.createJsonParser(src));
    }
    
    public <T> T readValue(final JsonNode src) throws IOException, JsonProcessingException {
        return (T)this._bindAndClose(this.treeAsTokens(src));
    }
    
    public JsonNode readTree(final InputStream in) throws IOException, JsonProcessingException {
        return this._bindAndCloseAsTree(this._jsonFactory.createJsonParser(in));
    }
    
    public JsonNode readTree(final Reader r) throws IOException, JsonProcessingException {
        return this._bindAndCloseAsTree(this._jsonFactory.createJsonParser(r));
    }
    
    public JsonNode readTree(final String content) throws IOException, JsonProcessingException {
        return this._bindAndCloseAsTree(this._jsonFactory.createJsonParser(content));
    }
    
    public <T> MappingIterator<T> readValues(final JsonParser jp) throws IOException, JsonProcessingException {
        final DeserializationContext ctxt = this._createDeserializationContext(jp, this._config);
        return new MappingIterator<T>(this._valueType, jp, ctxt, this._findRootDeserializer(this._config, this._valueType), false, this._valueToUpdate);
    }
    
    public <T> MappingIterator<T> readValues(final InputStream src) throws IOException, JsonProcessingException {
        final JsonParser jp = this._jsonFactory.createJsonParser(src);
        if (this._schema != null) {
            jp.setSchema(this._schema);
        }
        final DeserializationContext ctxt = this._createDeserializationContext(jp, this._config);
        return new MappingIterator<T>(this._valueType, jp, ctxt, this._findRootDeserializer(this._config, this._valueType), true, this._valueToUpdate);
    }
    
    public <T> MappingIterator<T> readValues(final Reader src) throws IOException, JsonProcessingException {
        final JsonParser jp = this._jsonFactory.createJsonParser(src);
        if (this._schema != null) {
            jp.setSchema(this._schema);
        }
        final DeserializationContext ctxt = this._createDeserializationContext(jp, this._config);
        return new MappingIterator<T>(this._valueType, jp, ctxt, this._findRootDeserializer(this._config, this._valueType), true, this._valueToUpdate);
    }
    
    public <T> MappingIterator<T> readValues(final String json) throws IOException, JsonProcessingException {
        final JsonParser jp = this._jsonFactory.createJsonParser(json);
        if (this._schema != null) {
            jp.setSchema(this._schema);
        }
        final DeserializationContext ctxt = this._createDeserializationContext(jp, this._config);
        return new MappingIterator<T>(this._valueType, jp, ctxt, this._findRootDeserializer(this._config, this._valueType), true, this._valueToUpdate);
    }
    
    public <T> MappingIterator<T> readValues(final byte[] src, final int offset, final int length) throws IOException, JsonProcessingException {
        final JsonParser jp = this._jsonFactory.createJsonParser(src, offset, length);
        if (this._schema != null) {
            jp.setSchema(this._schema);
        }
        final DeserializationContext ctxt = this._createDeserializationContext(jp, this._config);
        return new MappingIterator<T>(this._valueType, jp, ctxt, this._findRootDeserializer(this._config, this._valueType), true, this._valueToUpdate);
    }
    
    public final <T> MappingIterator<T> readValues(final byte[] src) throws IOException, JsonProcessingException {
        return this.readValues(src, 0, src.length);
    }
    
    public <T> MappingIterator<T> readValues(final File src) throws IOException, JsonProcessingException {
        final JsonParser jp = this._jsonFactory.createJsonParser(src);
        if (this._schema != null) {
            jp.setSchema(this._schema);
        }
        final DeserializationContext ctxt = this._createDeserializationContext(jp, this._config);
        return new MappingIterator<T>(this._valueType, jp, ctxt, this._findRootDeserializer(this._config, this._valueType), true, this._valueToUpdate);
    }
    
    public <T> MappingIterator<T> readValues(final URL src) throws IOException, JsonProcessingException {
        final JsonParser jp = this._jsonFactory.createJsonParser(src);
        if (this._schema != null) {
            jp.setSchema(this._schema);
        }
        final DeserializationContext ctxt = this._createDeserializationContext(jp, this._config);
        return new MappingIterator<T>(this._valueType, jp, ctxt, this._findRootDeserializer(this._config, this._valueType), true, this._valueToUpdate);
    }
    
    protected Object _bind(final JsonParser jp) throws IOException, JsonParseException, JsonMappingException {
        final JsonToken t = _initForReading(jp);
        Object result;
        if (t == JsonToken.VALUE_NULL) {
            if (this._valueToUpdate == null) {
                result = this._findRootDeserializer(this._config, this._valueType).getNullValue();
            }
            else {
                result = this._valueToUpdate;
            }
        }
        else if (t == JsonToken.END_ARRAY || t == JsonToken.END_OBJECT) {
            result = this._valueToUpdate;
        }
        else {
            final DeserializationContext ctxt = this._createDeserializationContext(jp, this._config);
            final JsonDeserializer<Object> deser = this._findRootDeserializer(this._config, this._valueType);
            if (this._unwrapRoot) {
                result = this._unwrapAndDeserialize(jp, ctxt, this._valueType, deser);
            }
            else if (this._valueToUpdate == null) {
                result = deser.deserialize(jp, ctxt);
            }
            else {
                deser.deserialize(jp, ctxt, this._valueToUpdate);
                result = this._valueToUpdate;
            }
        }
        jp.clearCurrentToken();
        return result;
    }
    
    protected Object _bindAndClose(final JsonParser jp) throws IOException, JsonParseException, JsonMappingException {
        if (this._schema != null) {
            jp.setSchema(this._schema);
        }
        try {
            final JsonToken t = _initForReading(jp);
            Object result;
            if (t == JsonToken.VALUE_NULL) {
                if (this._valueToUpdate == null) {
                    result = this._findRootDeserializer(this._config, this._valueType).getNullValue();
                }
                else {
                    result = this._valueToUpdate;
                }
            }
            else if (t == JsonToken.END_ARRAY || t == JsonToken.END_OBJECT) {
                result = this._valueToUpdate;
            }
            else {
                final DeserializationContext ctxt = this._createDeserializationContext(jp, this._config);
                final JsonDeserializer<Object> deser = this._findRootDeserializer(this._config, this._valueType);
                if (this._unwrapRoot) {
                    result = this._unwrapAndDeserialize(jp, ctxt, this._valueType, deser);
                }
                else if (this._valueToUpdate == null) {
                    result = deser.deserialize(jp, ctxt);
                }
                else {
                    deser.deserialize(jp, ctxt, this._valueToUpdate);
                    result = this._valueToUpdate;
                }
            }
            return result;
        }
        finally {
            try {
                jp.close();
            }
            catch (final IOException ex) {}
        }
    }
    
    protected JsonNode _bindAsTree(final JsonParser jp) throws IOException, JsonParseException, JsonMappingException {
        final JsonToken t = _initForReading(jp);
        JsonNode result;
        if (t == JsonToken.VALUE_NULL || t == JsonToken.END_ARRAY || t == JsonToken.END_OBJECT) {
            result = NullNode.instance;
        }
        else {
            final DeserializationContext ctxt = this._createDeserializationContext(jp, this._config);
            final JsonDeserializer<Object> deser = this._findRootDeserializer(this._config, ObjectReader.JSON_NODE_TYPE);
            if (this._unwrapRoot) {
                result = (JsonNode)this._unwrapAndDeserialize(jp, ctxt, ObjectReader.JSON_NODE_TYPE, deser);
            }
            else {
                result = deser.deserialize(jp, ctxt);
            }
        }
        jp.clearCurrentToken();
        return result;
    }
    
    protected JsonNode _bindAndCloseAsTree(final JsonParser jp) throws IOException, JsonParseException, JsonMappingException {
        if (this._schema != null) {
            jp.setSchema(this._schema);
        }
        try {
            return this._bindAsTree(jp);
        }
        finally {
            try {
                jp.close();
            }
            catch (final IOException ex) {}
        }
    }
    
    protected static JsonToken _initForReading(final JsonParser jp) throws IOException, JsonParseException, JsonMappingException {
        JsonToken t = jp.getCurrentToken();
        if (t == null) {
            t = jp.nextToken();
            if (t == null) {
                throw new EOFException("No content to map to Object due to end of input");
            }
        }
        return t;
    }
    
    protected JsonDeserializer<Object> _findRootDeserializer(final DeserializationConfig cfg, final JavaType valueType) throws JsonMappingException {
        if (valueType == null) {
            throw new JsonMappingException("No value type configured for ObjectReader");
        }
        JsonDeserializer<Object> deser = this._rootDeserializers.get(valueType);
        if (deser != null) {
            return deser;
        }
        deser = this._provider.findTypedValueDeserializer(cfg, valueType, null);
        if (deser == null) {
            throw new JsonMappingException("Can not find a deserializer for type " + valueType);
        }
        this._rootDeserializers.put(valueType, deser);
        return deser;
    }
    
    protected DeserializationContext _createDeserializationContext(final JsonParser jp, final DeserializationConfig cfg) {
        return new StdDeserializationContext(cfg, jp, this._provider, this._injectableValues);
    }
    
    protected Object _unwrapAndDeserialize(final JsonParser jp, final DeserializationContext ctxt, final JavaType rootType, final JsonDeserializer<Object> deser) throws IOException, JsonParseException, JsonMappingException {
        final SerializedString rootName = this._provider.findExpectedRootName(ctxt.getConfig(), rootType);
        if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
            throw JsonMappingException.from(jp, "Current token not START_OBJECT (needed to unwrap root name '" + rootName + "'), but " + jp.getCurrentToken());
        }
        if (jp.nextToken() != JsonToken.FIELD_NAME) {
            throw JsonMappingException.from(jp, "Current token not FIELD_NAME (to contain expected root name '" + rootName + "'), but " + jp.getCurrentToken());
        }
        final String actualName = jp.getCurrentName();
        if (!rootName.getValue().equals(actualName)) {
            throw JsonMappingException.from(jp, "Root name '" + actualName + "' does not match expected ('" + rootName + "') for type " + rootType);
        }
        jp.nextToken();
        Object result;
        if (this._valueToUpdate == null) {
            result = deser.deserialize(jp, ctxt);
        }
        else {
            deser.deserialize(jp, ctxt, this._valueToUpdate);
            result = this._valueToUpdate;
        }
        if (jp.nextToken() != JsonToken.END_OBJECT) {
            throw JsonMappingException.from(jp, "Current token not END_OBJECT (to match wrapper object with root name '" + rootName + "'), but " + jp.getCurrentToken());
        }
        return result;
    }
    
    @Override
    public JsonNode createArrayNode() {
        return this._config.getNodeFactory().arrayNode();
    }
    
    @Override
    public JsonNode createObjectNode() {
        return this._config.getNodeFactory().objectNode();
    }
    
    @Override
    public JsonParser treeAsTokens(final JsonNode n) {
        return new TreeTraversingParser(n, this);
    }
    
    @Override
    public <T> T treeToValue(final JsonNode n, final Class<T> valueType) throws IOException, JsonProcessingException {
        return this.readValue(this.treeAsTokens(n), valueType);
    }
    
    @Override
    public void writeTree(final JsonGenerator jgen, final JsonNode rootNode) throws IOException, JsonProcessingException {
        throw new UnsupportedOperationException("Not implemented for ObjectReader");
    }
    
    @Override
    public void writeValue(final JsonGenerator jgen, final Object value) throws IOException, JsonProcessingException {
        throw new UnsupportedOperationException("Not implemented for ObjectReader");
    }
    
    static {
        JSON_NODE_TYPE = SimpleType.constructUnsafe(JsonNode.class);
    }
}
