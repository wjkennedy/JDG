// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser;

import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.CollectionLikeType;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.map.type.MapLikeType;
import org.codehaus.jackson.map.type.ArrayType;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ClassIntrospector;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.ResolvableDeserializer;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.map.ContextualKeyDeserializer;
import org.codehaus.jackson.map.KeyDeserializer;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.ContextualDeserializer;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.io.SerializedString;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.AbstractTypeResolver;
import org.codehaus.jackson.map.KeyDeserializers;
import org.codehaus.jackson.map.Deserializers;
import org.codehaus.jackson.map.DeserializerFactory;
import org.codehaus.jackson.map.util.RootNameLookup;
import java.util.HashMap;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.type.JavaType;
import java.util.concurrent.ConcurrentHashMap;
import org.codehaus.jackson.map.DeserializerProvider;

public class StdDeserializerProvider extends DeserializerProvider
{
    protected final ConcurrentHashMap<JavaType, JsonDeserializer<Object>> _cachedDeserializers;
    protected final HashMap<JavaType, JsonDeserializer<Object>> _incompleteDeserializers;
    protected final RootNameLookup _rootNames;
    protected DeserializerFactory _factory;
    
    public StdDeserializerProvider() {
        this(BeanDeserializerFactory.instance);
    }
    
    public StdDeserializerProvider(final DeserializerFactory f) {
        this._cachedDeserializers = new ConcurrentHashMap<JavaType, JsonDeserializer<Object>>(64, 0.75f, 2);
        this._incompleteDeserializers = new HashMap<JavaType, JsonDeserializer<Object>>(8);
        this._factory = f;
        this._rootNames = new RootNameLookup();
    }
    
    @Override
    public DeserializerProvider withAdditionalDeserializers(final Deserializers d) {
        return this.withFactory(this._factory.withAdditionalDeserializers(d));
    }
    
    @Override
    public DeserializerProvider withAdditionalKeyDeserializers(final KeyDeserializers d) {
        return this.withFactory(this._factory.withAdditionalKeyDeserializers(d));
    }
    
    @Override
    public DeserializerProvider withDeserializerModifier(final BeanDeserializerModifier modifier) {
        return this.withFactory(this._factory.withDeserializerModifier(modifier));
    }
    
    @Override
    public DeserializerProvider withAbstractTypeResolver(final AbstractTypeResolver resolver) {
        return this.withFactory(this._factory.withAbstractTypeResolver(resolver));
    }
    
    @Override
    public DeserializerProvider withValueInstantiators(final ValueInstantiators instantiators) {
        return this.withFactory(this._factory.withValueInstantiators(instantiators));
    }
    
    @Override
    public StdDeserializerProvider withFactory(final DeserializerFactory factory) {
        if (this.getClass() != StdDeserializerProvider.class) {
            throw new IllegalStateException("DeserializerProvider of type " + this.getClass().getName() + " does not override 'withFactory()' method");
        }
        return new StdDeserializerProvider(factory);
    }
    
    @Override
    public JavaType mapAbstractType(final DeserializationConfig config, final JavaType type) throws JsonMappingException {
        return this._factory.mapAbstractType(config, type);
    }
    
    @Override
    public SerializedString findExpectedRootName(final DeserializationConfig config, final JavaType type) throws JsonMappingException {
        return this._rootNames.findRootName(type, config);
    }
    
    @Override
    public JsonDeserializer<Object> findValueDeserializer(final DeserializationConfig config, final JavaType propertyType, final BeanProperty property) throws JsonMappingException {
        JsonDeserializer<Object> deser = this._findCachedDeserializer(propertyType);
        if (deser != null) {
            if (deser instanceof ContextualDeserializer) {
                final JsonDeserializer<?> d = deser = ((ContextualDeserializer)deser).createContextual(config, property);
            }
            return deser;
        }
        deser = this._createAndCacheValueDeserializer(config, propertyType, property);
        if (deser == null) {
            deser = this._handleUnknownValueDeserializer(propertyType);
        }
        if (deser instanceof ContextualDeserializer) {
            final JsonDeserializer<?> d = deser = ((ContextualDeserializer)deser).createContextual(config, property);
        }
        return deser;
    }
    
    @Override
    public JsonDeserializer<Object> findTypedValueDeserializer(final DeserializationConfig config, final JavaType type, final BeanProperty property) throws JsonMappingException {
        final JsonDeserializer<Object> deser = this.findValueDeserializer(config, type, property);
        final TypeDeserializer typeDeser = this._factory.findTypeDeserializer(config, type, property);
        if (typeDeser != null) {
            return new WrappedDeserializer(typeDeser, deser);
        }
        return deser;
    }
    
    @Override
    public KeyDeserializer findKeyDeserializer(final DeserializationConfig config, final JavaType type, final BeanProperty property) throws JsonMappingException {
        KeyDeserializer kd = this._factory.createKeyDeserializer(config, type, property);
        if (kd instanceof ContextualKeyDeserializer) {
            kd = ((ContextualKeyDeserializer)kd).createContextual(config, property);
        }
        if (kd == null) {
            return this._handleUnknownKeyDeserializer(type);
        }
        return kd;
    }
    
    @Override
    public boolean hasValueDeserializerFor(final DeserializationConfig config, final JavaType type) {
        JsonDeserializer<Object> deser = this._findCachedDeserializer(type);
        if (deser == null) {
            try {
                deser = this._createAndCacheValueDeserializer(config, type, null);
            }
            catch (final Exception e) {
                return false;
            }
        }
        return deser != null;
    }
    
    @Override
    public int cachedDeserializersCount() {
        return this._cachedDeserializers.size();
    }
    
    @Override
    public void flushCachedDeserializers() {
        this._cachedDeserializers.clear();
    }
    
    protected JsonDeserializer<Object> _findCachedDeserializer(final JavaType type) {
        if (type == null) {
            throw new IllegalArgumentException();
        }
        return this._cachedDeserializers.get(type);
    }
    
    protected JsonDeserializer<Object> _createAndCacheValueDeserializer(final DeserializationConfig config, final JavaType type, final BeanProperty property) throws JsonMappingException {
        synchronized (this._incompleteDeserializers) {
            JsonDeserializer<Object> deser = this._findCachedDeserializer(type);
            if (deser != null) {
                return deser;
            }
            final int count = this._incompleteDeserializers.size();
            if (count > 0) {
                deser = this._incompleteDeserializers.get(type);
                if (deser != null) {
                    return deser;
                }
            }
            try {
                return this._createAndCache2(config, type, property);
            }
            finally {
                if (count == 0 && this._incompleteDeserializers.size() > 0) {
                    this._incompleteDeserializers.clear();
                }
            }
        }
    }
    
    protected JsonDeserializer<Object> _createAndCache2(final DeserializationConfig config, final JavaType type, final BeanProperty property) throws JsonMappingException {
        JsonDeserializer<Object> deser;
        try {
            deser = this._createDeserializer(config, type, property);
        }
        catch (final IllegalArgumentException iae) {
            throw new JsonMappingException(iae.getMessage(), null, iae);
        }
        if (deser == null) {
            return null;
        }
        final boolean isResolvable = deser instanceof ResolvableDeserializer;
        boolean addToCache = deser.getClass() == BeanDeserializer.class;
        if (!addToCache && config.isEnabled(DeserializationConfig.Feature.USE_ANNOTATIONS)) {
            final AnnotationIntrospector aintr = config.getAnnotationIntrospector();
            final AnnotatedClass ac = AnnotatedClass.construct(deser.getClass(), aintr, null);
            final Boolean cacheAnn = aintr.findCachability(ac);
            if (cacheAnn != null) {
                addToCache = cacheAnn;
            }
        }
        if (isResolvable) {
            this._incompleteDeserializers.put(type, deser);
            this._resolveDeserializer(config, (ResolvableDeserializer)deser);
            this._incompleteDeserializers.remove(type);
        }
        if (addToCache) {
            this._cachedDeserializers.put(type, deser);
        }
        return deser;
    }
    
    protected JsonDeserializer<Object> _createDeserializer(final DeserializationConfig config, final JavaType type, final BeanProperty property) throws JsonMappingException {
        if (type.isEnumType()) {
            return (JsonDeserializer<Object>)this._factory.createEnumDeserializer(config, this, type, property);
        }
        if (type.isContainerType()) {
            if (type.isArrayType()) {
                return (JsonDeserializer<Object>)this._factory.createArrayDeserializer(config, this, (ArrayType)type, property);
            }
            if (type.isMapLikeType()) {
                final MapLikeType mlt = (MapLikeType)type;
                if (mlt.isTrueMapType()) {
                    return (JsonDeserializer<Object>)this._factory.createMapDeserializer(config, this, (MapType)mlt, property);
                }
                return (JsonDeserializer<Object>)this._factory.createMapLikeDeserializer(config, this, mlt, property);
            }
            else if (type.isCollectionLikeType()) {
                final CollectionLikeType clt = (CollectionLikeType)type;
                if (clt.isTrueCollectionType()) {
                    return (JsonDeserializer<Object>)this._factory.createCollectionDeserializer(config, this, (CollectionType)clt, property);
                }
                return (JsonDeserializer<Object>)this._factory.createCollectionLikeDeserializer(config, this, clt, property);
            }
        }
        if (JsonNode.class.isAssignableFrom(type.getRawClass())) {
            return (JsonDeserializer<Object>)this._factory.createTreeDeserializer(config, this, type, property);
        }
        return this._factory.createBeanDeserializer(config, this, type, property);
    }
    
    protected void _resolveDeserializer(final DeserializationConfig config, final ResolvableDeserializer ser) throws JsonMappingException {
        ser.resolve(config, this);
    }
    
    protected JsonDeserializer<Object> _handleUnknownValueDeserializer(final JavaType type) throws JsonMappingException {
        final Class<?> rawClass = type.getRawClass();
        if (!ClassUtil.isConcrete(rawClass)) {
            throw new JsonMappingException("Can not find a Value deserializer for abstract type " + type);
        }
        throw new JsonMappingException("Can not find a Value deserializer for type " + type);
    }
    
    protected KeyDeserializer _handleUnknownKeyDeserializer(final JavaType type) throws JsonMappingException {
        throw new JsonMappingException("Can not find a (Map) Key deserializer for type " + type);
    }
    
    protected static final class WrappedDeserializer extends JsonDeserializer<Object>
    {
        final TypeDeserializer _typeDeserializer;
        final JsonDeserializer<Object> _deserializer;
        
        public WrappedDeserializer(final TypeDeserializer typeDeser, final JsonDeserializer<Object> deser) {
            this._typeDeserializer = typeDeser;
            this._deserializer = deser;
        }
        
        @Override
        public Object deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return this._deserializer.deserializeWithType(jp, ctxt, this._typeDeserializer);
        }
        
        @Override
        public Object deserializeWithType(final JsonParser jp, final DeserializationContext ctxt, final TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
            throw new IllegalStateException("Type-wrapped deserializer's deserializeWithType should never get called");
        }
        
        @Override
        public Object deserialize(final JsonParser jp, final DeserializationContext ctxt, final Object intoValue) throws IOException, JsonProcessingException {
            return this._deserializer.deserialize(jp, ctxt, intoValue);
        }
    }
}
