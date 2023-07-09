// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser;

import org.codehaus.jackson.map.deser.std.PrimitiveArrayDeserializers;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.LinkedHashMap;
import org.codehaus.jackson.map.deser.std.StdKeyDeserializers;
import org.codehaus.jackson.map.util.EnumResolver;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.map.ContextualDeserializer;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.jsontype.NamedType;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.deser.std.AtomicReferenceDeserializer;
import org.codehaus.jackson.map.type.TypeFactory;
import java.util.concurrent.atomic.AtomicReference;
import org.codehaus.jackson.map.deser.std.JsonNodeDeserializer;
import java.util.Iterator;
import org.codehaus.jackson.map.deser.std.EnumDeserializer;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.deser.std.EnumMapDeserializer;
import org.codehaus.jackson.map.deser.std.MapDeserializer;
import java.util.EnumMap;
import org.codehaus.jackson.map.deser.std.CollectionDeserializer;
import org.codehaus.jackson.map.deser.std.StringCollectionDeserializer;
import org.codehaus.jackson.map.deser.std.EnumSetDeserializer;
import java.util.EnumSet;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.deser.std.ObjectArrayDeserializer;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.type.MapLikeType;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.map.type.CollectionLikeType;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.type.ArrayType;
import org.codehaus.jackson.map.ext.OptionalHandlerFactory;
import java.util.Collection;
import java.util.Map;
import org.codehaus.jackson.map.KeyDeserializer;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.type.ClassKey;
import java.util.HashMap;
import org.codehaus.jackson.map.DeserializerFactory;

public abstract class BasicDeserializerFactory extends DeserializerFactory
{
    static final HashMap<ClassKey, JsonDeserializer<Object>> _simpleDeserializers;
    static final HashMap<JavaType, KeyDeserializer> _keyDeserializers;
    static final HashMap<String, Class<? extends Map>> _mapFallbacks;
    static final HashMap<String, Class<? extends Collection>> _collectionFallbacks;
    protected static final HashMap<JavaType, JsonDeserializer<Object>> _arrayDeserializers;
    protected OptionalHandlerFactory optionalHandlers;
    
    protected BasicDeserializerFactory() {
        this.optionalHandlers = OptionalHandlerFactory.instance;
    }
    
    @Override
    public abstract DeserializerFactory withConfig(final Config p0);
    
    protected abstract JsonDeserializer<?> _findCustomArrayDeserializer(final ArrayType p0, final DeserializationConfig p1, final DeserializerProvider p2, final BeanProperty p3, final TypeDeserializer p4, final JsonDeserializer<?> p5) throws JsonMappingException;
    
    protected abstract JsonDeserializer<?> _findCustomCollectionDeserializer(final CollectionType p0, final DeserializationConfig p1, final DeserializerProvider p2, final BasicBeanDescription p3, final BeanProperty p4, final TypeDeserializer p5, final JsonDeserializer<?> p6) throws JsonMappingException;
    
    protected abstract JsonDeserializer<?> _findCustomCollectionLikeDeserializer(final CollectionLikeType p0, final DeserializationConfig p1, final DeserializerProvider p2, final BasicBeanDescription p3, final BeanProperty p4, final TypeDeserializer p5, final JsonDeserializer<?> p6) throws JsonMappingException;
    
    protected abstract JsonDeserializer<?> _findCustomEnumDeserializer(final Class<?> p0, final DeserializationConfig p1, final BasicBeanDescription p2, final BeanProperty p3) throws JsonMappingException;
    
    protected abstract JsonDeserializer<?> _findCustomMapDeserializer(final MapType p0, final DeserializationConfig p1, final DeserializerProvider p2, final BasicBeanDescription p3, final BeanProperty p4, final KeyDeserializer p5, final TypeDeserializer p6, final JsonDeserializer<?> p7) throws JsonMappingException;
    
    protected abstract JsonDeserializer<?> _findCustomMapLikeDeserializer(final MapLikeType p0, final DeserializationConfig p1, final DeserializerProvider p2, final BasicBeanDescription p3, final BeanProperty p4, final KeyDeserializer p5, final TypeDeserializer p6, final JsonDeserializer<?> p7) throws JsonMappingException;
    
    protected abstract JsonDeserializer<?> _findCustomTreeNodeDeserializer(final Class<? extends JsonNode> p0, final DeserializationConfig p1, final BeanProperty p2) throws JsonMappingException;
    
    @Override
    public abstract ValueInstantiator findValueInstantiator(final DeserializationConfig p0, final BasicBeanDescription p1) throws JsonMappingException;
    
    @Override
    public abstract JavaType mapAbstractType(final DeserializationConfig p0, final JavaType p1) throws JsonMappingException;
    
    @Override
    public JsonDeserializer<?> createArrayDeserializer(final DeserializationConfig config, final DeserializerProvider p, final ArrayType type, final BeanProperty property) throws JsonMappingException {
        final JavaType elemType = type.getContentType();
        JsonDeserializer<Object> contentDeser = elemType.getValueHandler();
        if (contentDeser == null) {
            final JsonDeserializer<?> deser = BasicDeserializerFactory._arrayDeserializers.get(elemType);
            if (deser != null) {
                final JsonDeserializer<?> custom = this._findCustomArrayDeserializer(type, config, p, property, null, null);
                if (custom != null) {
                    return custom;
                }
                return deser;
            }
            else if (elemType.isPrimitive()) {
                throw new IllegalArgumentException("Internal error: primitive type (" + type + ") passed, no array deserializer found");
            }
        }
        TypeDeserializer elemTypeDeser = elemType.getTypeHandler();
        if (elemTypeDeser == null) {
            elemTypeDeser = this.findTypeDeserializer(config, elemType, property);
        }
        final JsonDeserializer<?> custom = this._findCustomArrayDeserializer(type, config, p, property, elemTypeDeser, contentDeser);
        if (custom != null) {
            return custom;
        }
        if (contentDeser == null) {
            contentDeser = p.findValueDeserializer(config, elemType, property);
        }
        return new ObjectArrayDeserializer(type, contentDeser, elemTypeDeser);
    }
    
    @Override
    public JsonDeserializer<?> createCollectionDeserializer(final DeserializationConfig config, final DeserializerProvider p, CollectionType type, final BeanProperty property) throws JsonMappingException {
        type = (CollectionType)this.mapAbstractType(config, type);
        Class<?> collectionClass = type.getRawClass();
        BasicBeanDescription beanDesc = config.introspectForCreation(type);
        final JsonDeserializer<Object> deser = this.findDeserializerFromAnnotation(config, beanDesc.getClassInfo(), property);
        if (deser != null) {
            return deser;
        }
        type = this.modifyTypeByAnnotation(config, beanDesc.getClassInfo(), type, null);
        final JavaType contentType = type.getContentType();
        JsonDeserializer<Object> contentDeser = contentType.getValueHandler();
        TypeDeserializer contentTypeDeser = contentType.getTypeHandler();
        if (contentTypeDeser == null) {
            contentTypeDeser = this.findTypeDeserializer(config, contentType, property);
        }
        final JsonDeserializer<?> custom = this._findCustomCollectionDeserializer(type, config, p, beanDesc, property, contentTypeDeser, contentDeser);
        if (custom != null) {
            return custom;
        }
        if (contentDeser == null) {
            if (EnumSet.class.isAssignableFrom(collectionClass)) {
                return new EnumSetDeserializer(contentType.getRawClass(), this.createEnumDeserializer(config, p, contentType, property));
            }
            contentDeser = p.findValueDeserializer(config, contentType, property);
        }
        if (type.isInterface() || type.isAbstract()) {
            final Class<? extends Collection> fallback = BasicDeserializerFactory._collectionFallbacks.get(collectionClass.getName());
            if (fallback == null) {
                throw new IllegalArgumentException("Can not find a deserializer for non-concrete Collection type " + type);
            }
            collectionClass = fallback;
            type = (CollectionType)config.constructSpecializedType(type, collectionClass);
            beanDesc = config.introspectForCreation(type);
        }
        final ValueInstantiator inst = this.findValueInstantiator(config, beanDesc);
        if (contentType.getRawClass() == String.class) {
            return new StringCollectionDeserializer(type, contentDeser, inst);
        }
        return new CollectionDeserializer(type, contentDeser, contentTypeDeser, inst);
    }
    
    @Override
    public JsonDeserializer<?> createCollectionLikeDeserializer(final DeserializationConfig config, final DeserializerProvider p, CollectionLikeType type, final BeanProperty property) throws JsonMappingException {
        type = (CollectionLikeType)this.mapAbstractType(config, type);
        final Class<?> collectionClass = type.getRawClass();
        final BasicBeanDescription beanDesc = config.introspectClassAnnotations(collectionClass);
        final JsonDeserializer<Object> deser = this.findDeserializerFromAnnotation(config, beanDesc.getClassInfo(), property);
        if (deser != null) {
            return deser;
        }
        type = this.modifyTypeByAnnotation(config, beanDesc.getClassInfo(), type, null);
        final JavaType contentType = type.getContentType();
        final JsonDeserializer<Object> contentDeser = contentType.getValueHandler();
        TypeDeserializer contentTypeDeser = contentType.getTypeHandler();
        if (contentTypeDeser == null) {
            contentTypeDeser = this.findTypeDeserializer(config, contentType, property);
        }
        return this._findCustomCollectionLikeDeserializer(type, config, p, beanDesc, property, contentTypeDeser, contentDeser);
    }
    
    @Override
    public JsonDeserializer<?> createMapDeserializer(final DeserializationConfig config, final DeserializerProvider p, MapType type, final BeanProperty property) throws JsonMappingException {
        type = (MapType)this.mapAbstractType(config, type);
        BasicBeanDescription beanDesc = config.introspectForCreation(type);
        final JsonDeserializer<Object> deser = this.findDeserializerFromAnnotation(config, beanDesc.getClassInfo(), property);
        if (deser != null) {
            return deser;
        }
        type = this.modifyTypeByAnnotation(config, beanDesc.getClassInfo(), type, null);
        final JavaType keyType = type.getKeyType();
        final JavaType contentType = type.getContentType();
        JsonDeserializer<Object> contentDeser = contentType.getValueHandler();
        KeyDeserializer keyDes = keyType.getValueHandler();
        if (keyDes == null) {
            keyDes = p.findKeyDeserializer(config, keyType, property);
        }
        TypeDeserializer contentTypeDeser = contentType.getTypeHandler();
        if (contentTypeDeser == null) {
            contentTypeDeser = this.findTypeDeserializer(config, contentType, property);
        }
        final JsonDeserializer<?> custom = this._findCustomMapDeserializer(type, config, p, beanDesc, property, keyDes, contentTypeDeser, contentDeser);
        if (custom != null) {
            return custom;
        }
        if (contentDeser == null) {
            contentDeser = p.findValueDeserializer(config, contentType, property);
        }
        Class<?> mapClass = type.getRawClass();
        if (!EnumMap.class.isAssignableFrom(mapClass)) {
            if (type.isInterface() || type.isAbstract()) {
                final Class<? extends Map> fallback = BasicDeserializerFactory._mapFallbacks.get(mapClass.getName());
                if (fallback == null) {
                    throw new IllegalArgumentException("Can not find a deserializer for non-concrete Map type " + type);
                }
                mapClass = fallback;
                type = (MapType)config.constructSpecializedType(type, mapClass);
                beanDesc = config.introspectForCreation(type);
            }
            final ValueInstantiator inst = this.findValueInstantiator(config, beanDesc);
            final MapDeserializer md = new MapDeserializer(type, inst, keyDes, contentDeser, contentTypeDeser);
            md.setIgnorableProperties(config.getAnnotationIntrospector().findPropertiesToIgnore(beanDesc.getClassInfo()));
            return md;
        }
        final Class<?> kt = keyType.getRawClass();
        if (kt == null || !kt.isEnum()) {
            throw new IllegalArgumentException("Can not construct EnumMap; generic (key) type not available");
        }
        return new EnumMapDeserializer(keyType.getRawClass(), this.createEnumDeserializer(config, p, keyType, property), contentDeser);
    }
    
    @Override
    public JsonDeserializer<?> createMapLikeDeserializer(final DeserializationConfig config, final DeserializerProvider p, MapLikeType type, final BeanProperty property) throws JsonMappingException {
        type = (MapLikeType)this.mapAbstractType(config, type);
        final BasicBeanDescription beanDesc = config.introspectForCreation(type);
        final JsonDeserializer<Object> deser = this.findDeserializerFromAnnotation(config, beanDesc.getClassInfo(), property);
        if (deser != null) {
            return deser;
        }
        type = this.modifyTypeByAnnotation(config, beanDesc.getClassInfo(), type, null);
        final JavaType keyType = type.getKeyType();
        final JavaType contentType = type.getContentType();
        final JsonDeserializer<Object> contentDeser = contentType.getValueHandler();
        KeyDeserializer keyDes = keyType.getValueHandler();
        if (keyDes == null) {
            keyDes = p.findKeyDeserializer(config, keyType, property);
        }
        TypeDeserializer contentTypeDeser = contentType.getTypeHandler();
        if (contentTypeDeser == null) {
            contentTypeDeser = this.findTypeDeserializer(config, contentType, property);
        }
        return this._findCustomMapLikeDeserializer(type, config, p, beanDesc, property, keyDes, contentTypeDeser, contentDeser);
    }
    
    @Override
    public JsonDeserializer<?> createEnumDeserializer(final DeserializationConfig config, final DeserializerProvider p, final JavaType type, final BeanProperty property) throws JsonMappingException {
        final BasicBeanDescription beanDesc = config.introspectForCreation(type);
        final JsonDeserializer<?> des = this.findDeserializerFromAnnotation(config, beanDesc.getClassInfo(), property);
        if (des != null) {
            return des;
        }
        final Class<?> enumClass = type.getRawClass();
        final JsonDeserializer<?> custom = this._findCustomEnumDeserializer(enumClass, config, beanDesc, property);
        if (custom != null) {
            return custom;
        }
        for (final AnnotatedMethod factory : beanDesc.getFactoryMethods()) {
            if (config.getAnnotationIntrospector().hasCreatorAnnotation(factory)) {
                final int argCount = factory.getParameterCount();
                if (argCount == 1) {
                    final Class<?> returnType = factory.getRawType();
                    if (returnType.isAssignableFrom(enumClass)) {
                        return EnumDeserializer.deserializerForCreator(config, enumClass, factory);
                    }
                }
                throw new IllegalArgumentException("Unsuitable method (" + factory + ") decorated with @JsonCreator (for Enum type " + enumClass.getName() + ")");
            }
        }
        return new EnumDeserializer(this.constructEnumResolver(enumClass, config));
    }
    
    @Override
    public JsonDeserializer<?> createTreeDeserializer(final DeserializationConfig config, final DeserializerProvider p, final JavaType nodeType, final BeanProperty property) throws JsonMappingException {
        final Class<? extends JsonNode> nodeClass = (Class<? extends JsonNode>)nodeType.getRawClass();
        final JsonDeserializer<?> custom = this._findCustomTreeNodeDeserializer(nodeClass, config, property);
        if (custom != null) {
            return custom;
        }
        return JsonNodeDeserializer.getDeserializer(nodeClass);
    }
    
    protected JsonDeserializer<Object> findStdBeanDeserializer(final DeserializationConfig config, final DeserializerProvider p, final JavaType type, final BeanProperty property) throws JsonMappingException {
        final Class<?> cls = type.getRawClass();
        final JsonDeserializer<Object> deser = BasicDeserializerFactory._simpleDeserializers.get(new ClassKey(cls));
        if (deser != null) {
            return deser;
        }
        if (AtomicReference.class.isAssignableFrom(cls)) {
            final TypeFactory tf = config.getTypeFactory();
            final JavaType[] params = tf.findTypeParameters(type, AtomicReference.class);
            JavaType referencedType;
            if (params == null || params.length < 1) {
                referencedType = TypeFactory.unknownType();
            }
            else {
                referencedType = params[0];
            }
            final JsonDeserializer<?> d2 = new AtomicReferenceDeserializer(referencedType, property);
            return (JsonDeserializer<Object>)d2;
        }
        final JsonDeserializer<?> d3 = this.optionalHandlers.findDeserializer(type, config, p);
        if (d3 != null) {
            return (JsonDeserializer<Object>)d3;
        }
        return null;
    }
    
    @Override
    public TypeDeserializer findTypeDeserializer(final DeserializationConfig config, final JavaType baseType, final BeanProperty property) throws JsonMappingException {
        final Class<?> cls = baseType.getRawClass();
        final BasicBeanDescription bean = config.introspectClassAnnotations(cls);
        final AnnotatedClass ac = bean.getClassInfo();
        final AnnotationIntrospector ai = config.getAnnotationIntrospector();
        TypeResolverBuilder<?> b = ai.findTypeResolver(config, ac, baseType);
        Collection<NamedType> subtypes = null;
        if (b == null) {
            b = config.getDefaultTyper(baseType);
            if (b == null) {
                return null;
            }
        }
        else {
            subtypes = config.getSubtypeResolver().collectAndResolveSubtypes(ac, config, ai);
        }
        if (b.getDefaultImpl() == null && baseType.isAbstract()) {
            final JavaType defaultType = this.mapAbstractType(config, baseType);
            if (defaultType != null && defaultType.getRawClass() != baseType.getRawClass()) {
                b = (TypeResolverBuilder<?>)b.defaultImpl(defaultType.getRawClass());
            }
        }
        return b.buildTypeDeserializer(config, baseType, subtypes, property);
    }
    
    public TypeDeserializer findPropertyTypeDeserializer(final DeserializationConfig config, final JavaType baseType, final AnnotatedMember annotated, final BeanProperty property) throws JsonMappingException {
        final AnnotationIntrospector ai = config.getAnnotationIntrospector();
        final TypeResolverBuilder<?> b = ai.findPropertyTypeResolver(config, annotated, baseType);
        if (b == null) {
            return this.findTypeDeserializer(config, baseType, property);
        }
        final Collection<NamedType> subtypes = config.getSubtypeResolver().collectAndResolveSubtypes(annotated, config, ai);
        return b.buildTypeDeserializer(config, baseType, subtypes, property);
    }
    
    public TypeDeserializer findPropertyContentTypeDeserializer(final DeserializationConfig config, final JavaType containerType, final AnnotatedMember propertyEntity, final BeanProperty property) throws JsonMappingException {
        final AnnotationIntrospector ai = config.getAnnotationIntrospector();
        final TypeResolverBuilder<?> b = ai.findPropertyContentTypeResolver(config, propertyEntity, containerType);
        final JavaType contentType = containerType.getContentType();
        if (b == null) {
            return this.findTypeDeserializer(config, contentType, property);
        }
        final Collection<NamedType> subtypes = config.getSubtypeResolver().collectAndResolveSubtypes(propertyEntity, config, ai);
        return b.buildTypeDeserializer(config, contentType, subtypes, property);
    }
    
    protected JsonDeserializer<Object> findDeserializerFromAnnotation(final DeserializationConfig config, final Annotated ann, final BeanProperty property) throws JsonMappingException {
        final Object deserDef = config.getAnnotationIntrospector().findDeserializer(ann);
        if (deserDef != null) {
            return this._constructDeserializer(config, ann, property, deserDef);
        }
        return null;
    }
    
    JsonDeserializer<Object> _constructDeserializer(final DeserializationConfig config, final Annotated ann, final BeanProperty property, final Object deserDef) throws JsonMappingException {
        if (deserDef instanceof JsonDeserializer) {
            JsonDeserializer<Object> deser = (JsonDeserializer<Object>)deserDef;
            if (deser instanceof ContextualDeserializer) {
                deser = ((ContextualDeserializer)deser).createContextual(config, property);
            }
            return deser;
        }
        if (!(deserDef instanceof Class)) {
            throw new IllegalStateException("AnnotationIntrospector returned deserializer definition of type " + deserDef.getClass().getName() + "; expected type JsonDeserializer or Class<JsonDeserializer> instead");
        }
        final Class<? extends JsonDeserializer<?>> deserClass = (Class<? extends JsonDeserializer<?>>)deserDef;
        if (!JsonDeserializer.class.isAssignableFrom(deserClass)) {
            throw new IllegalStateException("AnnotationIntrospector returned Class " + deserClass.getName() + "; expected Class<JsonDeserializer>");
        }
        JsonDeserializer<Object> deser2 = config.deserializerInstance(ann, deserClass);
        if (deser2 instanceof ContextualDeserializer) {
            deser2 = ((ContextualDeserializer)deser2).createContextual(config, property);
        }
        return deser2;
    }
    
    protected <T extends JavaType> T modifyTypeByAnnotation(final DeserializationConfig config, final Annotated a, T type, final String propName) throws JsonMappingException {
        final AnnotationIntrospector intr = config.getAnnotationIntrospector();
        final Class<?> subclass = intr.findDeserializationType(a, type, propName);
        if (subclass != null) {
            try {
                type = (T)type.narrowBy(subclass);
            }
            catch (final IllegalArgumentException iae) {
                throw new JsonMappingException("Failed to narrow type " + type + " with concrete-type annotation (value " + subclass.getName() + "), method '" + a.getName() + "': " + iae.getMessage(), null, iae);
            }
        }
        if (type.isContainerType()) {
            final Class<?> keyClass = intr.findDeserializationKeyType(a, type.getKeyType(), propName);
            if (keyClass != null) {
                if (!(type instanceof MapLikeType)) {
                    throw new JsonMappingException("Illegal key-type annotation: type " + type + " is not a Map(-like) type");
                }
                try {
                    type = (T)((MapLikeType)type).narrowKey(keyClass);
                }
                catch (final IllegalArgumentException iae2) {
                    throw new JsonMappingException("Failed to narrow key type " + type + " with key-type annotation (" + keyClass.getName() + "): " + iae2.getMessage(), null, iae2);
                }
            }
            final JavaType keyType = type.getKeyType();
            if (keyType != null && keyType.getValueHandler() == null) {
                final Class<? extends KeyDeserializer> kdClass = intr.findKeyDeserializer(a);
                if (kdClass != null && kdClass != KeyDeserializer.None.class) {
                    final KeyDeserializer kd = config.keyDeserializerInstance(a, kdClass);
                    keyType.setValueHandler(kd);
                }
            }
            final Class<?> cc = intr.findDeserializationContentType(a, type.getContentType(), propName);
            if (cc != null) {
                try {
                    type = (T)type.narrowContentsBy(cc);
                }
                catch (final IllegalArgumentException iae3) {
                    throw new JsonMappingException("Failed to narrow content type " + type + " with content-type annotation (" + cc.getName() + "): " + iae3.getMessage(), null, iae3);
                }
            }
            final JavaType contentType = type.getContentType();
            if (contentType.getValueHandler() == null) {
                final Class<? extends JsonDeserializer<?>> cdClass = intr.findContentDeserializer(a);
                if (cdClass != null && cdClass != JsonDeserializer.None.class) {
                    final JsonDeserializer<Object> cd = config.deserializerInstance(a, cdClass);
                    type.getContentType().setValueHandler(cd);
                }
            }
        }
        return type;
    }
    
    protected JavaType resolveType(final DeserializationConfig config, final BasicBeanDescription beanDesc, JavaType type, final AnnotatedMember member, final BeanProperty property) throws JsonMappingException {
        if (type.isContainerType()) {
            final AnnotationIntrospector intr = config.getAnnotationIntrospector();
            final JavaType keyType = type.getKeyType();
            if (keyType != null) {
                final Class<? extends KeyDeserializer> kdClass = intr.findKeyDeserializer(member);
                if (kdClass != null && kdClass != KeyDeserializer.None.class) {
                    final KeyDeserializer kd = config.keyDeserializerInstance(member, kdClass);
                    keyType.setValueHandler(kd);
                }
            }
            final Class<? extends JsonDeserializer<?>> cdClass = intr.findContentDeserializer(member);
            if (cdClass != null && cdClass != JsonDeserializer.None.class) {
                final JsonDeserializer<Object> cd = config.deserializerInstance(member, cdClass);
                type.getContentType().setValueHandler(cd);
            }
            if (member instanceof AnnotatedMember) {
                final TypeDeserializer contentTypeDeser = this.findPropertyContentTypeDeserializer(config, type, member, property);
                if (contentTypeDeser != null) {
                    type = type.withContentTypeHandler(contentTypeDeser);
                }
            }
        }
        TypeDeserializer valueTypeDeser;
        if (member instanceof AnnotatedMember) {
            valueTypeDeser = this.findPropertyTypeDeserializer(config, type, member, property);
        }
        else {
            valueTypeDeser = this.findTypeDeserializer(config, type, null);
        }
        if (valueTypeDeser != null) {
            type = type.withTypeHandler(valueTypeDeser);
        }
        return type;
    }
    
    protected EnumResolver<?> constructEnumResolver(final Class<?> enumClass, final DeserializationConfig config) {
        if (config.isEnabled(DeserializationConfig.Feature.READ_ENUMS_USING_TO_STRING)) {
            return EnumResolver.constructUnsafeUsingToString(enumClass);
        }
        return EnumResolver.constructUnsafe(enumClass, config.getAnnotationIntrospector());
    }
    
    static {
        _simpleDeserializers = StdDeserializers.constructAll();
        _keyDeserializers = StdKeyDeserializers.constructAll();
        (_mapFallbacks = new HashMap<String, Class<? extends Map>>()).put(Map.class.getName(), LinkedHashMap.class);
        BasicDeserializerFactory._mapFallbacks.put(ConcurrentMap.class.getName(), ConcurrentHashMap.class);
        BasicDeserializerFactory._mapFallbacks.put(SortedMap.class.getName(), TreeMap.class);
        BasicDeserializerFactory._mapFallbacks.put("java.util.NavigableMap", TreeMap.class);
        try {
            final Class<?> key = Class.forName("java.util.concurrent.ConcurrentNavigableMap");
            final Class<? extends Map<?, ?>> mapValue;
            final Class<?> value = mapValue = (Class<? extends Map<?, ?>>)Class.forName("java.util.concurrent.ConcurrentSkipListMap");
            BasicDeserializerFactory._mapFallbacks.put(key.getName(), mapValue);
        }
        catch (final ClassNotFoundException ex) {}
        catch (final SecurityException ex2) {}
        (_collectionFallbacks = new HashMap<String, Class<? extends Collection>>()).put(Collection.class.getName(), ArrayList.class);
        BasicDeserializerFactory._collectionFallbacks.put(List.class.getName(), ArrayList.class);
        BasicDeserializerFactory._collectionFallbacks.put(Set.class.getName(), HashSet.class);
        BasicDeserializerFactory._collectionFallbacks.put(SortedSet.class.getName(), TreeSet.class);
        BasicDeserializerFactory._collectionFallbacks.put(Queue.class.getName(), LinkedList.class);
        BasicDeserializerFactory._collectionFallbacks.put("java.util.Deque", LinkedList.class);
        BasicDeserializerFactory._collectionFallbacks.put("java.util.NavigableSet", TreeSet.class);
        _arrayDeserializers = PrimitiveArrayDeserializers.getAll();
    }
}
