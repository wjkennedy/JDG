// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import java.util.Collection;
import org.codehaus.jackson.map.jsontype.impl.StdTypeResolverBuilder;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.map.introspect.BasicClassIntrospector;
import org.codehaus.jackson.map.type.SimpleType;
import java.util.Iterator;
import org.codehaus.jackson.map.deser.StdDeserializationContext;
import org.codehaus.jackson.io.SerializedString;
import java.io.EOFException;
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import org.codehaus.jackson.schema.JsonSchema;
import org.codehaus.jackson.FormatSchema;
import org.codehaus.jackson.PrettyPrinter;
import org.codehaus.jackson.util.ByteArrayBuilder;
import org.codehaus.jackson.io.SegmentedStringWriter;
import java.io.Writer;
import java.io.OutputStream;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.util.TokenBuffer;
import org.codehaus.jackson.node.TreeTraversingParser;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.JsonGenerationException;
import java.io.Closeable;
import java.net.URL;
import java.io.File;
import java.io.Reader;
import java.io.InputStream;
import org.codehaus.jackson.node.NullNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jackson.JsonParseException;
import java.io.IOException;
import java.text.DateFormat;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.node.JsonNodeFactory;
import java.lang.reflect.Type;
import org.codehaus.jackson.map.jsontype.NamedType;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.jsontype.impl.StdSubtypeResolver;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.deser.ValueInstantiators;
import org.codehaus.jackson.map.type.TypeModifier;
import org.codehaus.jackson.map.deser.BeanDeserializerModifier;
import org.codehaus.jackson.map.ser.BeanSerializerModifier;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.util.VersionUtil;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ser.BeanSerializerFactory;
import org.codehaus.jackson.map.deser.StdDeserializerProvider;
import org.codehaus.jackson.map.ser.StdSerializerProvider;
import java.util.concurrent.ConcurrentHashMap;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.map.jsontype.SubtypeResolver;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.introspect.VisibilityChecker;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.Versioned;
import org.codehaus.jackson.ObjectCodec;

public class ObjectMapper extends ObjectCodec implements Versioned
{
    private static final JavaType JSON_NODE_TYPE;
    protected static final ClassIntrospector<? extends BeanDescription> DEFAULT_INTROSPECTOR;
    protected static final AnnotationIntrospector DEFAULT_ANNOTATION_INTROSPECTOR;
    protected static final VisibilityChecker<?> STD_VISIBILITY_CHECKER;
    protected final JsonFactory _jsonFactory;
    protected SubtypeResolver _subtypeResolver;
    protected TypeFactory _typeFactory;
    protected InjectableValues _injectableValues;
    protected SerializationConfig _serializationConfig;
    protected SerializerProvider _serializerProvider;
    protected SerializerFactory _serializerFactory;
    protected DeserializationConfig _deserializationConfig;
    protected DeserializerProvider _deserializerProvider;
    protected final ConcurrentHashMap<JavaType, JsonDeserializer<Object>> _rootDeserializers;
    
    public ObjectMapper() {
        this(null, null, null);
    }
    
    public ObjectMapper(final JsonFactory jf) {
        this(jf, null, null);
    }
    
    @Deprecated
    public ObjectMapper(final SerializerFactory sf) {
        this(null, null, null);
        this.setSerializerFactory(sf);
    }
    
    public ObjectMapper(final JsonFactory jf, final SerializerProvider sp, final DeserializerProvider dp) {
        this(jf, sp, dp, null, null);
    }
    
    public ObjectMapper(final JsonFactory jf, final SerializerProvider sp, final DeserializerProvider dp, final SerializationConfig sconfig, final DeserializationConfig dconfig) {
        this._rootDeserializers = new ConcurrentHashMap<JavaType, JsonDeserializer<Object>>(64, 0.6f, 2);
        if (jf == null) {
            this._jsonFactory = new MappingJsonFactory(this);
        }
        else {
            this._jsonFactory = jf;
            if (jf.getCodec() == null) {
                this._jsonFactory.setCodec(this);
            }
        }
        this._typeFactory = TypeFactory.defaultInstance();
        this._serializationConfig = ((sconfig != null) ? sconfig : new SerializationConfig(ObjectMapper.DEFAULT_INTROSPECTOR, ObjectMapper.DEFAULT_ANNOTATION_INTROSPECTOR, ObjectMapper.STD_VISIBILITY_CHECKER, null, null, this._typeFactory, null));
        this._deserializationConfig = ((dconfig != null) ? dconfig : new DeserializationConfig(ObjectMapper.DEFAULT_INTROSPECTOR, ObjectMapper.DEFAULT_ANNOTATION_INTROSPECTOR, ObjectMapper.STD_VISIBILITY_CHECKER, null, null, this._typeFactory, null));
        this._serializerProvider = ((sp == null) ? new StdSerializerProvider() : sp);
        this._deserializerProvider = ((dp == null) ? new StdDeserializerProvider() : dp);
        this._serializerFactory = BeanSerializerFactory.instance;
    }
    
    public Version version() {
        return VersionUtil.versionFor(this.getClass());
    }
    
    public void registerModule(final Module module) {
        final String name = module.getModuleName();
        if (name == null) {
            throw new IllegalArgumentException("Module without defined name");
        }
        final Version version = module.version();
        if (version == null) {
            throw new IllegalArgumentException("Module without defined version");
        }
        final ObjectMapper mapper = this;
        module.setupModule(new Module.SetupContext() {
            public Version getMapperVersion() {
                return ObjectMapper.this.version();
            }
            
            public DeserializationConfig getDeserializationConfig() {
                return mapper.getDeserializationConfig();
            }
            
            public SerializationConfig getSerializationConfig() {
                return mapper.getSerializationConfig();
            }
            
            public boolean isEnabled(final DeserializationConfig.Feature f) {
                return mapper.isEnabled(f);
            }
            
            public boolean isEnabled(final SerializationConfig.Feature f) {
                return mapper.isEnabled(f);
            }
            
            public boolean isEnabled(final JsonParser.Feature f) {
                return mapper.isEnabled(f);
            }
            
            public boolean isEnabled(final JsonGenerator.Feature f) {
                return mapper.isEnabled(f);
            }
            
            public void addDeserializers(final Deserializers d) {
                mapper._deserializerProvider = mapper._deserializerProvider.withAdditionalDeserializers(d);
            }
            
            public void addKeyDeserializers(final KeyDeserializers d) {
                mapper._deserializerProvider = mapper._deserializerProvider.withAdditionalKeyDeserializers(d);
            }
            
            public void addSerializers(final Serializers s) {
                mapper._serializerFactory = mapper._serializerFactory.withAdditionalSerializers(s);
            }
            
            public void addKeySerializers(final Serializers s) {
                mapper._serializerFactory = mapper._serializerFactory.withAdditionalKeySerializers(s);
            }
            
            public void addBeanSerializerModifier(final BeanSerializerModifier modifier) {
                mapper._serializerFactory = mapper._serializerFactory.withSerializerModifier(modifier);
            }
            
            public void addBeanDeserializerModifier(final BeanDeserializerModifier modifier) {
                mapper._deserializerProvider = mapper._deserializerProvider.withDeserializerModifier(modifier);
            }
            
            public void addAbstractTypeResolver(final AbstractTypeResolver resolver) {
                mapper._deserializerProvider = mapper._deserializerProvider.withAbstractTypeResolver(resolver);
            }
            
            public void addTypeModifier(final TypeModifier modifier) {
                TypeFactory f = mapper._typeFactory;
                f = f.withModifier(modifier);
                mapper.setTypeFactory(f);
            }
            
            public void addValueInstantiators(final ValueInstantiators instantiators) {
                mapper._deserializerProvider = mapper._deserializerProvider.withValueInstantiators(instantiators);
            }
            
            public void insertAnnotationIntrospector(final AnnotationIntrospector ai) {
                mapper._deserializationConfig = mapper._deserializationConfig.withInsertedAnnotationIntrospector(ai);
                mapper._serializationConfig = mapper._serializationConfig.withInsertedAnnotationIntrospector(ai);
            }
            
            public void appendAnnotationIntrospector(final AnnotationIntrospector ai) {
                mapper._deserializationConfig = mapper._deserializationConfig.withAppendedAnnotationIntrospector(ai);
                mapper._serializationConfig = mapper._serializationConfig.withAppendedAnnotationIntrospector(ai);
            }
            
            public void setMixInAnnotations(final Class<?> target, final Class<?> mixinSource) {
                mapper._deserializationConfig.addMixInAnnotations(target, mixinSource);
                mapper._serializationConfig.addMixInAnnotations(target, mixinSource);
            }
        });
    }
    
    public ObjectMapper withModule(final Module module) {
        this.registerModule(module);
        return this;
    }
    
    public SerializationConfig getSerializationConfig() {
        return this._serializationConfig;
    }
    
    public SerializationConfig copySerializationConfig() {
        return this._serializationConfig.createUnshared(this._subtypeResolver);
    }
    
    public ObjectMapper setSerializationConfig(final SerializationConfig cfg) {
        this._serializationConfig = cfg;
        return this;
    }
    
    public DeserializationConfig getDeserializationConfig() {
        return this._deserializationConfig;
    }
    
    public DeserializationConfig copyDeserializationConfig() {
        return this._deserializationConfig.createUnshared(this._subtypeResolver).passSerializationFeatures(this._serializationConfig._featureFlags);
    }
    
    public ObjectMapper setDeserializationConfig(final DeserializationConfig cfg) {
        this._deserializationConfig = cfg;
        return this;
    }
    
    public ObjectMapper setSerializerFactory(final SerializerFactory f) {
        this._serializerFactory = f;
        return this;
    }
    
    public ObjectMapper setSerializerProvider(final SerializerProvider p) {
        this._serializerProvider = p;
        return this;
    }
    
    public SerializerProvider getSerializerProvider() {
        return this._serializerProvider;
    }
    
    public ObjectMapper setDeserializerProvider(final DeserializerProvider p) {
        this._deserializerProvider = p;
        return this;
    }
    
    public DeserializerProvider getDeserializerProvider() {
        return this._deserializerProvider;
    }
    
    public VisibilityChecker<?> getVisibilityChecker() {
        return this._serializationConfig.getDefaultVisibilityChecker();
    }
    
    public void setVisibilityChecker(final VisibilityChecker<?> vc) {
        this._deserializationConfig = this._deserializationConfig.withVisibilityChecker(vc);
        this._serializationConfig = this._serializationConfig.withVisibilityChecker(vc);
    }
    
    public ObjectMapper setVisibility(final JsonMethod forMethod, final JsonAutoDetect.Visibility visibility) {
        this._deserializationConfig = this._deserializationConfig.withVisibility(forMethod, visibility);
        this._serializationConfig = this._serializationConfig.withVisibility(forMethod, visibility);
        return this;
    }
    
    public SubtypeResolver getSubtypeResolver() {
        if (this._subtypeResolver == null) {
            this._subtypeResolver = new StdSubtypeResolver();
        }
        return this._subtypeResolver;
    }
    
    public void setSubtypeResolver(final SubtypeResolver r) {
        this._subtypeResolver = r;
    }
    
    public ObjectMapper setAnnotationIntrospector(final AnnotationIntrospector ai) {
        this._serializationConfig = this._serializationConfig.withAnnotationIntrospector(ai);
        this._deserializationConfig = this._deserializationConfig.withAnnotationIntrospector(ai);
        return this;
    }
    
    public ObjectMapper setPropertyNamingStrategy(final PropertyNamingStrategy s) {
        this._serializationConfig = this._serializationConfig.withPropertyNamingStrategy(s);
        this._deserializationConfig = this._deserializationConfig.withPropertyNamingStrategy(s);
        return this;
    }
    
    public ObjectMapper setSerializationInclusion(final JsonSerialize.Inclusion incl) {
        this._serializationConfig = this._serializationConfig.withSerializationInclusion(incl);
        return this;
    }
    
    public ObjectMapper enableDefaultTyping() {
        return this.enableDefaultTyping(DefaultTyping.OBJECT_AND_NON_CONCRETE);
    }
    
    public ObjectMapper enableDefaultTyping(final DefaultTyping dti) {
        return this.enableDefaultTyping(dti, JsonTypeInfo.As.WRAPPER_ARRAY);
    }
    
    public ObjectMapper enableDefaultTyping(final DefaultTyping applicability, final JsonTypeInfo.As includeAs) {
        TypeResolverBuilder<?> typer = new DefaultTypeResolverBuilder(applicability);
        typer = (TypeResolverBuilder<?>)typer.init(JsonTypeInfo.Id.CLASS, null);
        typer = (TypeResolverBuilder<?>)typer.inclusion(includeAs);
        return this.setDefaultTyping(typer);
    }
    
    public ObjectMapper enableDefaultTypingAsProperty(final DefaultTyping applicability, final String propertyName) {
        TypeResolverBuilder<?> typer = new DefaultTypeResolverBuilder(applicability);
        typer = (TypeResolverBuilder<?>)typer.init(JsonTypeInfo.Id.CLASS, null);
        typer = (TypeResolverBuilder<?>)typer.inclusion(JsonTypeInfo.As.PROPERTY);
        typer = (TypeResolverBuilder<?>)typer.typeProperty(propertyName);
        return this.setDefaultTyping(typer);
    }
    
    public ObjectMapper disableDefaultTyping() {
        return this.setDefaultTyping(null);
    }
    
    public ObjectMapper setDefaultTyping(final TypeResolverBuilder<?> typer) {
        this._deserializationConfig = this._deserializationConfig.withTypeResolverBuilder(typer);
        this._serializationConfig = this._serializationConfig.withTypeResolverBuilder(typer);
        return this;
    }
    
    public void registerSubtypes(final Class<?>... classes) {
        this.getSubtypeResolver().registerSubtypes(classes);
    }
    
    public void registerSubtypes(final NamedType... types) {
        this.getSubtypeResolver().registerSubtypes(types);
    }
    
    public TypeFactory getTypeFactory() {
        return this._typeFactory;
    }
    
    public ObjectMapper setTypeFactory(final TypeFactory f) {
        this._typeFactory = f;
        this._deserializationConfig = this._deserializationConfig.withTypeFactory(f);
        this._serializationConfig = this._serializationConfig.withTypeFactory(f);
        return this;
    }
    
    public JavaType constructType(final Type t) {
        return this._typeFactory.constructType(t);
    }
    
    public ObjectMapper setNodeFactory(final JsonNodeFactory f) {
        this._deserializationConfig = this._deserializationConfig.withNodeFactory(f);
        return this;
    }
    
    public void setFilters(final FilterProvider filterProvider) {
        this._serializationConfig = this._serializationConfig.withFilters(filterProvider);
    }
    
    public JsonFactory getJsonFactory() {
        return this._jsonFactory;
    }
    
    public void setDateFormat(final DateFormat dateFormat) {
        this._deserializationConfig = this._deserializationConfig.withDateFormat(dateFormat);
        this._serializationConfig = this._serializationConfig.withDateFormat(dateFormat);
    }
    
    public void setHandlerInstantiator(final HandlerInstantiator hi) {
        this._deserializationConfig = this._deserializationConfig.withHandlerInstantiator(hi);
        this._serializationConfig = this._serializationConfig.withHandlerInstantiator(hi);
    }
    
    public ObjectMapper setInjectableValues(final InjectableValues injectableValues) {
        this._injectableValues = injectableValues;
        return this;
    }
    
    public ObjectMapper configure(final SerializationConfig.Feature f, final boolean state) {
        this._serializationConfig.set(f, state);
        return this;
    }
    
    public ObjectMapper configure(final DeserializationConfig.Feature f, final boolean state) {
        this._deserializationConfig.set(f, state);
        return this;
    }
    
    public ObjectMapper configure(final JsonParser.Feature f, final boolean state) {
        this._jsonFactory.configure(f, state);
        return this;
    }
    
    public ObjectMapper configure(final JsonGenerator.Feature f, final boolean state) {
        this._jsonFactory.configure(f, state);
        return this;
    }
    
    public ObjectMapper enable(final DeserializationConfig.Feature... f) {
        this._deserializationConfig = this._deserializationConfig.with(f);
        return this;
    }
    
    public ObjectMapper disable(final DeserializationConfig.Feature... f) {
        this._deserializationConfig = this._deserializationConfig.without(f);
        return this;
    }
    
    public ObjectMapper enable(final SerializationConfig.Feature... f) {
        this._serializationConfig = this._serializationConfig.with(f);
        return this;
    }
    
    public ObjectMapper disable(final SerializationConfig.Feature... f) {
        this._serializationConfig = this._serializationConfig.without(f);
        return this;
    }
    
    public boolean isEnabled(final SerializationConfig.Feature f) {
        return this._serializationConfig.isEnabled(f);
    }
    
    public boolean isEnabled(final DeserializationConfig.Feature f) {
        return this._deserializationConfig.isEnabled(f);
    }
    
    public boolean isEnabled(final JsonParser.Feature f) {
        return this._jsonFactory.isEnabled(f);
    }
    
    public boolean isEnabled(final JsonGenerator.Feature f) {
        return this._jsonFactory.isEnabled(f);
    }
    
    public JsonNodeFactory getNodeFactory() {
        return this._deserializationConfig.getNodeFactory();
    }
    
    @Override
    public <T> T readValue(final JsonParser jp, final Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readValue(this.copyDeserializationConfig(), jp, this._typeFactory.constructType(valueType));
    }
    
    @Override
    public <T> T readValue(final JsonParser jp, final TypeReference<?> valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readValue(this.copyDeserializationConfig(), jp, this._typeFactory.constructType(valueTypeRef));
    }
    
    @Override
    public <T> T readValue(final JsonParser jp, final JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readValue(this.copyDeserializationConfig(), jp, valueType);
    }
    
    @Override
    public JsonNode readTree(final JsonParser jp) throws IOException, JsonProcessingException {
        final DeserializationConfig cfg = this.copyDeserializationConfig();
        JsonToken t = jp.getCurrentToken();
        if (t == null) {
            t = jp.nextToken();
            if (t == null) {
                return null;
            }
        }
        final JsonNode n = (JsonNode)this._readValue(cfg, jp, ObjectMapper.JSON_NODE_TYPE);
        return (n == null) ? this.getNodeFactory().nullNode() : n;
    }
    
    @Override
    public <T> MappingIterator<T> readValues(final JsonParser jp, final JavaType valueType) throws IOException, JsonProcessingException {
        final DeserializationConfig config = this.copyDeserializationConfig();
        final DeserializationContext ctxt = this._createDeserializationContext(jp, config);
        final JsonDeserializer<?> deser = this._findRootDeserializer(config, valueType);
        return new MappingIterator<T>(valueType, jp, ctxt, deser, false, null);
    }
    
    @Override
    public <T> MappingIterator<T> readValues(final JsonParser jp, final Class<T> valueType) throws IOException, JsonProcessingException {
        return this.readValues(jp, this._typeFactory.constructType(valueType));
    }
    
    @Override
    public <T> MappingIterator<T> readValues(final JsonParser jp, final TypeReference<?> valueTypeRef) throws IOException, JsonProcessingException {
        return this.readValues(jp, this._typeFactory.constructType(valueTypeRef));
    }
    
    public <T> T readValue(final JsonParser jp, final Class<T> valueType, final DeserializationConfig cfg) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readValue(cfg, jp, this._typeFactory.constructType(valueType));
    }
    
    public <T> T readValue(final JsonParser jp, final TypeReference<?> valueTypeRef, final DeserializationConfig cfg) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readValue(cfg, jp, this._typeFactory.constructType(valueTypeRef));
    }
    
    public <T> T readValue(final JsonParser jp, final JavaType valueType, final DeserializationConfig cfg) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readValue(cfg, jp, valueType);
    }
    
    public JsonNode readTree(final JsonParser jp, final DeserializationConfig cfg) throws IOException, JsonProcessingException {
        final JsonNode n = (JsonNode)this._readValue(cfg, jp, ObjectMapper.JSON_NODE_TYPE);
        return (n == null) ? NullNode.instance : n;
    }
    
    public JsonNode readTree(final InputStream in) throws IOException, JsonProcessingException {
        final JsonNode n = (JsonNode)this._readMapAndClose(this._jsonFactory.createJsonParser(in), ObjectMapper.JSON_NODE_TYPE);
        return (n == null) ? NullNode.instance : n;
    }
    
    public JsonNode readTree(final Reader r) throws IOException, JsonProcessingException {
        final JsonNode n = (JsonNode)this._readMapAndClose(this._jsonFactory.createJsonParser(r), ObjectMapper.JSON_NODE_TYPE);
        return (n == null) ? NullNode.instance : n;
    }
    
    public JsonNode readTree(final String content) throws IOException, JsonProcessingException {
        final JsonNode n = (JsonNode)this._readMapAndClose(this._jsonFactory.createJsonParser(content), ObjectMapper.JSON_NODE_TYPE);
        return (n == null) ? NullNode.instance : n;
    }
    
    public JsonNode readTree(final byte[] content) throws IOException, JsonProcessingException {
        final JsonNode n = (JsonNode)this._readMapAndClose(this._jsonFactory.createJsonParser(content), ObjectMapper.JSON_NODE_TYPE);
        return (n == null) ? NullNode.instance : n;
    }
    
    public JsonNode readTree(final File file) throws IOException, JsonProcessingException {
        final JsonNode n = (JsonNode)this._readMapAndClose(this._jsonFactory.createJsonParser(file), ObjectMapper.JSON_NODE_TYPE);
        return (n == null) ? NullNode.instance : n;
    }
    
    public JsonNode readTree(final URL source) throws IOException, JsonProcessingException {
        final JsonNode n = (JsonNode)this._readMapAndClose(this._jsonFactory.createJsonParser(source), ObjectMapper.JSON_NODE_TYPE);
        return (n == null) ? NullNode.instance : n;
    }
    
    @Override
    public void writeValue(final JsonGenerator jgen, final Object value) throws IOException, JsonGenerationException, JsonMappingException {
        final SerializationConfig config = this.copySerializationConfig();
        if (config.isEnabled(SerializationConfig.Feature.CLOSE_CLOSEABLE) && value instanceof Closeable) {
            this._writeCloseableValue(jgen, value, config);
        }
        else {
            this._serializerProvider.serializeValue(config, jgen, value, this._serializerFactory);
            if (config.isEnabled(SerializationConfig.Feature.FLUSH_AFTER_WRITE_VALUE)) {
                jgen.flush();
            }
        }
    }
    
    public void writeValue(final JsonGenerator jgen, final Object value, final SerializationConfig config) throws IOException, JsonGenerationException, JsonMappingException {
        if (config.isEnabled(SerializationConfig.Feature.CLOSE_CLOSEABLE) && value instanceof Closeable) {
            this._writeCloseableValue(jgen, value, config);
        }
        else {
            this._serializerProvider.serializeValue(config, jgen, value, this._serializerFactory);
            if (config.isEnabled(SerializationConfig.Feature.FLUSH_AFTER_WRITE_VALUE)) {
                jgen.flush();
            }
        }
    }
    
    @Override
    public void writeTree(final JsonGenerator jgen, final JsonNode rootNode) throws IOException, JsonProcessingException {
        final SerializationConfig config = this.copySerializationConfig();
        this._serializerProvider.serializeValue(config, jgen, rootNode, this._serializerFactory);
        if (config.isEnabled(SerializationConfig.Feature.FLUSH_AFTER_WRITE_VALUE)) {
            jgen.flush();
        }
    }
    
    public void writeTree(final JsonGenerator jgen, final JsonNode rootNode, final SerializationConfig cfg) throws IOException, JsonProcessingException {
        this._serializerProvider.serializeValue(cfg, jgen, rootNode, this._serializerFactory);
        if (cfg.isEnabled(SerializationConfig.Feature.FLUSH_AFTER_WRITE_VALUE)) {
            jgen.flush();
        }
    }
    
    @Override
    public ObjectNode createObjectNode() {
        return this._deserializationConfig.getNodeFactory().objectNode();
    }
    
    @Override
    public ArrayNode createArrayNode() {
        return this._deserializationConfig.getNodeFactory().arrayNode();
    }
    
    @Override
    public JsonParser treeAsTokens(final JsonNode n) {
        return new TreeTraversingParser(n, this);
    }
    
    @Override
    public <T> T treeToValue(final JsonNode n, final Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        return this.readValue(this.treeAsTokens(n), valueType);
    }
    
    public <T extends JsonNode> T valueToTree(final Object fromValue) throws IllegalArgumentException {
        if (fromValue == null) {
            return null;
        }
        final TokenBuffer buf = new TokenBuffer(this);
        JsonNode result;
        try {
            this.writeValue(buf, fromValue);
            final JsonParser jp = buf.asParser();
            result = this.readTree(jp);
            jp.close();
        }
        catch (final IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        return (T)result;
    }
    
    public boolean canSerialize(final Class<?> type) {
        return this._serializerProvider.hasSerializerFor(this.copySerializationConfig(), type, this._serializerFactory);
    }
    
    public boolean canDeserialize(final JavaType type) {
        return this._deserializerProvider.hasValueDeserializerFor(this.copyDeserializationConfig(), type);
    }
    
    public <T> T readValue(final File src, final Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readMapAndClose(this._jsonFactory.createJsonParser(src), this._typeFactory.constructType(valueType));
    }
    
    public <T> T readValue(final File src, final TypeReference valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readMapAndClose(this._jsonFactory.createJsonParser(src), this._typeFactory.constructType(valueTypeRef));
    }
    
    public <T> T readValue(final File src, final JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readMapAndClose(this._jsonFactory.createJsonParser(src), valueType);
    }
    
    public <T> T readValue(final URL src, final Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readMapAndClose(this._jsonFactory.createJsonParser(src), this._typeFactory.constructType(valueType));
    }
    
    public <T> T readValue(final URL src, final TypeReference valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readMapAndClose(this._jsonFactory.createJsonParser(src), this._typeFactory.constructType(valueTypeRef));
    }
    
    public <T> T readValue(final URL src, final JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readMapAndClose(this._jsonFactory.createJsonParser(src), valueType);
    }
    
    public <T> T readValue(final String content, final Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readMapAndClose(this._jsonFactory.createJsonParser(content), this._typeFactory.constructType(valueType));
    }
    
    public <T> T readValue(final String content, final TypeReference valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readMapAndClose(this._jsonFactory.createJsonParser(content), this._typeFactory.constructType(valueTypeRef));
    }
    
    public <T> T readValue(final String content, final JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readMapAndClose(this._jsonFactory.createJsonParser(content), valueType);
    }
    
    public <T> T readValue(final Reader src, final Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readMapAndClose(this._jsonFactory.createJsonParser(src), this._typeFactory.constructType(valueType));
    }
    
    public <T> T readValue(final Reader src, final TypeReference valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readMapAndClose(this._jsonFactory.createJsonParser(src), this._typeFactory.constructType(valueTypeRef));
    }
    
    public <T> T readValue(final Reader src, final JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readMapAndClose(this._jsonFactory.createJsonParser(src), valueType);
    }
    
    public <T> T readValue(final InputStream src, final Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readMapAndClose(this._jsonFactory.createJsonParser(src), this._typeFactory.constructType(valueType));
    }
    
    public <T> T readValue(final InputStream src, final TypeReference valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readMapAndClose(this._jsonFactory.createJsonParser(src), this._typeFactory.constructType(valueTypeRef));
    }
    
    public <T> T readValue(final InputStream src, final JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readMapAndClose(this._jsonFactory.createJsonParser(src), valueType);
    }
    
    public <T> T readValue(final byte[] src, final Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readMapAndClose(this._jsonFactory.createJsonParser(src), this._typeFactory.constructType(valueType));
    }
    
    public <T> T readValue(final byte[] src, final int offset, final int len, final Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readMapAndClose(this._jsonFactory.createJsonParser(src, offset, len), this._typeFactory.constructType(valueType));
    }
    
    public <T> T readValue(final byte[] src, final TypeReference valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readMapAndClose(this._jsonFactory.createJsonParser(src), this._typeFactory.constructType(valueTypeRef));
    }
    
    public <T> T readValue(final byte[] src, final int offset, final int len, final TypeReference valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readMapAndClose(this._jsonFactory.createJsonParser(src, offset, len), this._typeFactory.constructType(valueTypeRef));
    }
    
    public <T> T readValue(final byte[] src, final JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readMapAndClose(this._jsonFactory.createJsonParser(src), valueType);
    }
    
    public <T> T readValue(final byte[] src, final int offset, final int len, final JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readMapAndClose(this._jsonFactory.createJsonParser(src, offset, len), valueType);
    }
    
    public <T> T readValue(final JsonNode root, final Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readValue(this.copyDeserializationConfig(), this.treeAsTokens(root), this._typeFactory.constructType(valueType));
    }
    
    public <T> T readValue(final JsonNode root, final TypeReference valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readValue(this.copyDeserializationConfig(), this.treeAsTokens(root), this._typeFactory.constructType(valueTypeRef));
    }
    
    public <T> T readValue(final JsonNode root, final JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
        return (T)this._readValue(this.copyDeserializationConfig(), this.treeAsTokens(root), valueType);
    }
    
    public void writeValue(final File resultFile, final Object value) throws IOException, JsonGenerationException, JsonMappingException {
        this._configAndWriteValue(this._jsonFactory.createJsonGenerator(resultFile, JsonEncoding.UTF8), value);
    }
    
    public void writeValue(final OutputStream out, final Object value) throws IOException, JsonGenerationException, JsonMappingException {
        this._configAndWriteValue(this._jsonFactory.createJsonGenerator(out, JsonEncoding.UTF8), value);
    }
    
    public void writeValue(final Writer w, final Object value) throws IOException, JsonGenerationException, JsonMappingException {
        this._configAndWriteValue(this._jsonFactory.createJsonGenerator(w), value);
    }
    
    public String writeValueAsString(final Object value) throws IOException, JsonGenerationException, JsonMappingException {
        final SegmentedStringWriter sw = new SegmentedStringWriter(this._jsonFactory._getBufferRecycler());
        this._configAndWriteValue(this._jsonFactory.createJsonGenerator(sw), value);
        return sw.getAndClear();
    }
    
    public byte[] writeValueAsBytes(final Object value) throws IOException, JsonGenerationException, JsonMappingException {
        final ByteArrayBuilder bb = new ByteArrayBuilder(this._jsonFactory._getBufferRecycler());
        this._configAndWriteValue(this._jsonFactory.createJsonGenerator(bb, JsonEncoding.UTF8), value);
        final byte[] result = bb.toByteArray();
        bb.release();
        return result;
    }
    
    public ObjectWriter writer() {
        return new ObjectWriter(this, this.copySerializationConfig());
    }
    
    public ObjectWriter writer(final DateFormat df) {
        return new ObjectWriter(this, this.copySerializationConfig().withDateFormat(df));
    }
    
    public ObjectWriter writerWithView(final Class<?> serializationView) {
        return new ObjectWriter(this, this.copySerializationConfig().withView(serializationView));
    }
    
    public ObjectWriter writerWithType(final Class<?> rootType) {
        final JavaType t = (rootType == null) ? null : this._typeFactory.constructType(rootType);
        return new ObjectWriter(this, this.copySerializationConfig(), t, null);
    }
    
    public ObjectWriter writerWithType(final JavaType rootType) {
        return new ObjectWriter(this, this.copySerializationConfig(), rootType, null);
    }
    
    public ObjectWriter writerWithType(final TypeReference<?> rootType) {
        final JavaType t = (rootType == null) ? null : this._typeFactory.constructType(rootType);
        return new ObjectWriter(this, this.copySerializationConfig(), t, null);
    }
    
    public ObjectWriter writer(PrettyPrinter pp) {
        if (pp == null) {
            pp = ObjectWriter.NULL_PRETTY_PRINTER;
        }
        return new ObjectWriter(this, this.copySerializationConfig(), null, pp);
    }
    
    public ObjectWriter writerWithDefaultPrettyPrinter() {
        return new ObjectWriter(this, this.copySerializationConfig(), null, this._defaultPrettyPrinter());
    }
    
    public ObjectWriter writer(final FilterProvider filterProvider) {
        return new ObjectWriter(this, this.copySerializationConfig().withFilters(filterProvider));
    }
    
    public ObjectWriter writer(final FormatSchema schema) {
        return new ObjectWriter(this, this.copySerializationConfig(), schema);
    }
    
    @Deprecated
    public ObjectWriter typedWriter(final Class<?> rootType) {
        return this.writerWithType(rootType);
    }
    
    @Deprecated
    public ObjectWriter typedWriter(final JavaType rootType) {
        return this.writerWithType(rootType);
    }
    
    @Deprecated
    public ObjectWriter typedWriter(final TypeReference<?> rootType) {
        return this.writerWithType(rootType);
    }
    
    @Deprecated
    public ObjectWriter viewWriter(final Class<?> serializationView) {
        return this.writerWithView(serializationView);
    }
    
    @Deprecated
    public ObjectWriter prettyPrintingWriter(final PrettyPrinter pp) {
        return this.writer(pp);
    }
    
    @Deprecated
    public ObjectWriter defaultPrettyPrintingWriter() {
        return this.writerWithDefaultPrettyPrinter();
    }
    
    @Deprecated
    public ObjectWriter filteredWriter(final FilterProvider filterProvider) {
        return this.writer(filterProvider);
    }
    
    @Deprecated
    public ObjectWriter schemaBasedWriter(final FormatSchema schema) {
        return this.writer(schema);
    }
    
    public ObjectReader reader() {
        return new ObjectReader(this, this.copyDeserializationConfig()).withInjectableValues(this._injectableValues);
    }
    
    public ObjectReader readerForUpdating(final Object valueToUpdate) {
        final JavaType t = this._typeFactory.constructType(valueToUpdate.getClass());
        return new ObjectReader(this, this.copyDeserializationConfig(), t, valueToUpdate, null, this._injectableValues);
    }
    
    public ObjectReader reader(final JavaType type) {
        return new ObjectReader(this, this.copyDeserializationConfig(), type, null, null, this._injectableValues);
    }
    
    public ObjectReader reader(final Class<?> type) {
        return this.reader(this._typeFactory.constructType(type));
    }
    
    public ObjectReader reader(final TypeReference<?> type) {
        return this.reader(this._typeFactory.constructType(type));
    }
    
    public ObjectReader reader(final JsonNodeFactory f) {
        return new ObjectReader(this, this.copyDeserializationConfig()).withNodeFactory(f);
    }
    
    public ObjectReader reader(final FormatSchema schema) {
        return new ObjectReader(this, this.copyDeserializationConfig(), null, null, schema, this._injectableValues);
    }
    
    public ObjectReader reader(final InjectableValues injectableValues) {
        return new ObjectReader(this, this.copyDeserializationConfig(), null, null, null, injectableValues);
    }
    
    @Deprecated
    public ObjectReader updatingReader(final Object valueToUpdate) {
        return this.readerForUpdating(valueToUpdate);
    }
    
    @Deprecated
    public ObjectReader schemaBasedReader(final FormatSchema schema) {
        return this.reader(schema);
    }
    
    public <T> T convertValue(final Object fromValue, final Class<T> toValueType) throws IllegalArgumentException {
        return (T)this._convert(fromValue, this._typeFactory.constructType(toValueType));
    }
    
    public <T> T convertValue(final Object fromValue, final TypeReference toValueTypeRef) throws IllegalArgumentException {
        return (T)this._convert(fromValue, this._typeFactory.constructType(toValueTypeRef));
    }
    
    public <T> T convertValue(final Object fromValue, final JavaType toValueType) throws IllegalArgumentException {
        return (T)this._convert(fromValue, toValueType);
    }
    
    protected Object _convert(final Object fromValue, final JavaType toValueType) throws IllegalArgumentException {
        if (fromValue == null) {
            return null;
        }
        final TokenBuffer buf = new TokenBuffer(this);
        try {
            this.writeValue(buf, fromValue);
            final JsonParser jp = buf.asParser();
            final Object result = this.readValue(jp, toValueType);
            jp.close();
            return result;
        }
        catch (final IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
    
    public JsonSchema generateJsonSchema(final Class<?> t) throws JsonMappingException {
        return this.generateJsonSchema(t, this.copySerializationConfig());
    }
    
    public JsonSchema generateJsonSchema(final Class<?> t, final SerializationConfig cfg) throws JsonMappingException {
        return this._serializerProvider.generateJsonSchema(t, cfg, this._serializerFactory);
    }
    
    protected PrettyPrinter _defaultPrettyPrinter() {
        return new DefaultPrettyPrinter();
    }
    
    protected final void _configAndWriteValue(final JsonGenerator jgen, final Object value) throws IOException, JsonGenerationException, JsonMappingException {
        final SerializationConfig cfg = this.copySerializationConfig();
        if (cfg.isEnabled(SerializationConfig.Feature.INDENT_OUTPUT)) {
            jgen.useDefaultPrettyPrinter();
        }
        if (cfg.isEnabled(SerializationConfig.Feature.CLOSE_CLOSEABLE) && value instanceof Closeable) {
            this._configAndWriteCloseable(jgen, value, cfg);
            return;
        }
        boolean closed = false;
        try {
            this._serializerProvider.serializeValue(cfg, jgen, value, this._serializerFactory);
            closed = true;
            jgen.close();
        }
        finally {
            if (!closed) {
                try {
                    jgen.close();
                }
                catch (final IOException ex) {}
            }
        }
    }
    
    protected final void _configAndWriteValue(final JsonGenerator jgen, final Object value, final Class<?> viewClass) throws IOException, JsonGenerationException, JsonMappingException {
        final SerializationConfig cfg = this.copySerializationConfig().withView(viewClass);
        if (cfg.isEnabled(SerializationConfig.Feature.INDENT_OUTPUT)) {
            jgen.useDefaultPrettyPrinter();
        }
        if (cfg.isEnabled(SerializationConfig.Feature.CLOSE_CLOSEABLE) && value instanceof Closeable) {
            this._configAndWriteCloseable(jgen, value, cfg);
            return;
        }
        boolean closed = false;
        try {
            this._serializerProvider.serializeValue(cfg, jgen, value, this._serializerFactory);
            closed = true;
            jgen.close();
        }
        finally {
            if (!closed) {
                try {
                    jgen.close();
                }
                catch (final IOException ex) {}
            }
        }
    }
    
    private final void _configAndWriteCloseable(JsonGenerator jgen, final Object value, final SerializationConfig cfg) throws IOException, JsonGenerationException, JsonMappingException {
        Closeable toClose = (Closeable)value;
        try {
            this._serializerProvider.serializeValue(cfg, jgen, value, this._serializerFactory);
            final JsonGenerator tmpJgen = jgen;
            jgen = null;
            tmpJgen.close();
            final Closeable tmpToClose = toClose;
            toClose = null;
            tmpToClose.close();
        }
        finally {
            if (jgen != null) {
                try {
                    jgen.close();
                }
                catch (final IOException ex) {}
            }
            if (toClose != null) {
                try {
                    toClose.close();
                }
                catch (final IOException ex2) {}
            }
        }
    }
    
    private final void _writeCloseableValue(final JsonGenerator jgen, final Object value, final SerializationConfig cfg) throws IOException, JsonGenerationException, JsonMappingException {
        Closeable toClose = (Closeable)value;
        try {
            this._serializerProvider.serializeValue(cfg, jgen, value, this._serializerFactory);
            if (cfg.isEnabled(SerializationConfig.Feature.FLUSH_AFTER_WRITE_VALUE)) {
                jgen.flush();
            }
            final Closeable tmpToClose = toClose;
            toClose = null;
            tmpToClose.close();
        }
        finally {
            if (toClose != null) {
                try {
                    toClose.close();
                }
                catch (final IOException ex) {}
            }
        }
    }
    
    protected Object _readValue(final DeserializationConfig cfg, final JsonParser jp, final JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
        final JsonToken t = this._initForReading(jp);
        Object result;
        if (t == JsonToken.VALUE_NULL) {
            result = this._findRootDeserializer(cfg, valueType).getNullValue();
        }
        else if (t == JsonToken.END_ARRAY || t == JsonToken.END_OBJECT) {
            result = null;
        }
        else {
            final DeserializationContext ctxt = this._createDeserializationContext(jp, cfg);
            final JsonDeserializer<Object> deser = this._findRootDeserializer(cfg, valueType);
            if (cfg.isEnabled(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE)) {
                result = this._unwrapAndDeserialize(jp, valueType, ctxt, deser);
            }
            else {
                result = deser.deserialize(jp, ctxt);
            }
        }
        jp.clearCurrentToken();
        return result;
    }
    
    protected Object _readMapAndClose(final JsonParser jp, final JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
        try {
            final JsonToken t = this._initForReading(jp);
            Object result;
            if (t == JsonToken.VALUE_NULL) {
                result = this._findRootDeserializer(this._deserializationConfig, valueType).getNullValue();
            }
            else if (t == JsonToken.END_ARRAY || t == JsonToken.END_OBJECT) {
                result = null;
            }
            else {
                final DeserializationConfig cfg = this.copyDeserializationConfig();
                final DeserializationContext ctxt = this._createDeserializationContext(jp, cfg);
                final JsonDeserializer<Object> deser = this._findRootDeserializer(cfg, valueType);
                if (cfg.isEnabled(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE)) {
                    result = this._unwrapAndDeserialize(jp, valueType, ctxt, deser);
                }
                else {
                    result = deser.deserialize(jp, ctxt);
                }
            }
            jp.clearCurrentToken();
            return result;
        }
        finally {
            try {
                jp.close();
            }
            catch (final IOException ex) {}
        }
    }
    
    protected JsonToken _initForReading(final JsonParser jp) throws IOException, JsonParseException, JsonMappingException {
        JsonToken t = jp.getCurrentToken();
        if (t == null) {
            t = jp.nextToken();
            if (t == null) {
                throw new EOFException("No content to map to Object due to end of input");
            }
        }
        return t;
    }
    
    protected Object _unwrapAndDeserialize(final JsonParser jp, final JavaType rootType, final DeserializationContext ctxt, final JsonDeserializer<Object> deser) throws IOException, JsonParseException, JsonMappingException {
        final SerializedString rootName = this._deserializerProvider.findExpectedRootName(ctxt.getConfig(), rootType);
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
        final Object result = deser.deserialize(jp, ctxt);
        if (jp.nextToken() != JsonToken.END_OBJECT) {
            throw JsonMappingException.from(jp, "Current token not END_OBJECT (to match wrapper object with root name '" + rootName + "'), but " + jp.getCurrentToken());
        }
        return result;
    }
    
    protected JsonDeserializer<Object> _findRootDeserializer(final DeserializationConfig cfg, final JavaType valueType) throws JsonMappingException {
        JsonDeserializer<Object> deser = this._rootDeserializers.get(valueType);
        if (deser != null) {
            return deser;
        }
        deser = this._deserializerProvider.findTypedValueDeserializer(cfg, valueType, null);
        if (deser == null) {
            throw new JsonMappingException("Can not find a deserializer for type " + valueType);
        }
        this._rootDeserializers.put(valueType, deser);
        return deser;
    }
    
    protected DeserializationContext _createDeserializationContext(final JsonParser jp, final DeserializationConfig cfg) {
        return new StdDeserializationContext(cfg, jp, this._deserializerProvider, this._injectableValues);
    }
    
    static {
        JSON_NODE_TYPE = SimpleType.constructUnsafe(JsonNode.class);
        DEFAULT_INTROSPECTOR = BasicClassIntrospector.instance;
        DEFAULT_ANNOTATION_INTROSPECTOR = new JacksonAnnotationIntrospector();
        STD_VISIBILITY_CHECKER = VisibilityChecker.Std.defaultInstance();
    }
    
    public enum DefaultTyping
    {
        JAVA_LANG_OBJECT, 
        OBJECT_AND_NON_CONCRETE, 
        NON_CONCRETE_AND_ARRAYS, 
        NON_FINAL;
    }
    
    public static class DefaultTypeResolverBuilder extends StdTypeResolverBuilder
    {
        protected final DefaultTyping _appliesFor;
        
        public DefaultTypeResolverBuilder(final DefaultTyping t) {
            this._appliesFor = t;
        }
        
        @Override
        public TypeDeserializer buildTypeDeserializer(final DeserializationConfig config, final JavaType baseType, final Collection<NamedType> subtypes, final BeanProperty property) {
            return this.useForType(baseType) ? super.buildTypeDeserializer(config, baseType, subtypes, property) : null;
        }
        
        @Override
        public TypeSerializer buildTypeSerializer(final SerializationConfig config, final JavaType baseType, final Collection<NamedType> subtypes, final BeanProperty property) {
            return this.useForType(baseType) ? super.buildTypeSerializer(config, baseType, subtypes, property) : null;
        }
        
        public boolean useForType(JavaType t) {
            switch (this._appliesFor) {
                case NON_CONCRETE_AND_ARRAYS: {
                    while (t.isArrayType()) {
                        t = t.getContentType();
                    }
                }
                case OBJECT_AND_NON_CONCRETE: {
                    return t.getRawClass() == Object.class || !t.isConcrete();
                }
                case NON_FINAL: {
                    while (t.isArrayType()) {
                        t = t.getContentType();
                    }
                    return !t.isFinal();
                }
                default: {
                    return t.getRawClass() == Object.class;
                }
            }
        }
    }
}
