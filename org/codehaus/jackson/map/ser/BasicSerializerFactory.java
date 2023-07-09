// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser;

import org.codehaus.jackson.map.ser.std.TokenBufferSerializer;
import org.codehaus.jackson.util.TokenBuffer;
import java.util.Map;
import org.codehaus.jackson.map.ser.std.StdJdkSerializers;
import java.sql.Time;
import java.sql.Timestamp;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.codehaus.jackson.map.ser.std.StringSerializer;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.map.ser.std.ObjectArraySerializer;
import org.codehaus.jackson.map.ser.std.StdArraySerializers;
import org.codehaus.jackson.map.ser.std.EnumMapSerializer;
import org.codehaus.jackson.map.util.EnumValues;
import org.codehaus.jackson.map.ser.std.MapSerializer;
import java.util.EnumMap;
import java.util.RandomAccess;
import org.codehaus.jackson.map.ser.std.StringCollectionSerializer;
import org.codehaus.jackson.map.ser.std.StdContainerSerializers;
import org.codehaus.jackson.map.ser.std.IndexedStringListSerializer;
import java.util.EnumSet;
import org.codehaus.jackson.map.BeanDescription;
import org.codehaus.jackson.map.type.ArrayType;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.CollectionLikeType;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.map.type.MapLikeType;
import org.codehaus.jackson.map.ContextualSerializer;
import java.util.Iterator;
import java.lang.reflect.Method;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.ser.std.DateSerializer;
import java.util.Date;
import org.codehaus.jackson.map.ser.std.CalendarSerializer;
import java.util.Calendar;
import org.codehaus.jackson.map.ser.std.EnumSerializer;
import org.codehaus.jackson.map.ser.std.ToStringSerializer;
import java.nio.charset.Charset;
import org.codehaus.jackson.map.ser.std.TimeZoneSerializer;
import java.util.TimeZone;
import org.codehaus.jackson.map.ser.std.InetAddressSerializer;
import java.net.InetAddress;
import org.codehaus.jackson.map.ser.std.JsonValueSerializer;
import org.codehaus.jackson.map.introspect.Annotated;
import java.lang.reflect.Member;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.map.ser.std.SerializableSerializer;
import org.codehaus.jackson.map.ser.std.SerializableWithTypeSerializer;
import org.codehaus.jackson.map.JsonSerializableWithType;
import org.codehaus.jackson.map.JsonSerializable;
import org.codehaus.jackson.map.Serializers;
import org.codehaus.jackson.map.ser.std.NullSerializer;
import org.codehaus.jackson.map.jsontype.NamedType;
import java.util.Collection;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.ext.OptionalHandlerFactory;
import org.codehaus.jackson.map.JsonSerializer;
import java.util.HashMap;
import org.codehaus.jackson.map.SerializerFactory;

public abstract class BasicSerializerFactory extends SerializerFactory
{
    protected static final HashMap<String, JsonSerializer<?>> _concrete;
    protected static final HashMap<String, Class<? extends JsonSerializer<?>>> _concreteLazy;
    protected static final HashMap<String, JsonSerializer<?>> _arraySerializers;
    protected OptionalHandlerFactory optionalHandlers;
    
    protected BasicSerializerFactory() {
        this.optionalHandlers = OptionalHandlerFactory.instance;
    }
    
    @Override
    public abstract JsonSerializer<Object> createSerializer(final SerializationConfig p0, final JavaType p1, final BeanProperty p2) throws JsonMappingException;
    
    @Override
    public TypeSerializer createTypeSerializer(final SerializationConfig config, final JavaType baseType, final BeanProperty property) {
        final BasicBeanDescription bean = config.introspectClassAnnotations(baseType.getRawClass());
        final AnnotatedClass ac = bean.getClassInfo();
        final AnnotationIntrospector ai = config.getAnnotationIntrospector();
        TypeResolverBuilder<?> b = ai.findTypeResolver(config, ac, baseType);
        Collection<NamedType> subtypes = null;
        if (b == null) {
            b = config.getDefaultTyper(baseType);
        }
        else {
            subtypes = config.getSubtypeResolver().collectAndResolveSubtypes(ac, config, ai);
        }
        return (b == null) ? null : b.buildTypeSerializer(config, baseType, subtypes, property);
    }
    
    public final JsonSerializer<?> getNullSerializer() {
        return NullSerializer.instance;
    }
    
    protected abstract Iterable<Serializers> customSerializers();
    
    public final JsonSerializer<?> findSerializerByLookup(final JavaType type, final SerializationConfig config, final BasicBeanDescription beanDesc, final BeanProperty property, final boolean staticTyping) {
        final Class<?> raw = type.getRawClass();
        final String clsName = raw.getName();
        final JsonSerializer<?> ser = BasicSerializerFactory._concrete.get(clsName);
        if (ser != null) {
            return ser;
        }
        final Class<? extends JsonSerializer<?>> serClass = BasicSerializerFactory._concreteLazy.get(clsName);
        if (serClass != null) {
            try {
                return (JsonSerializer)serClass.newInstance();
            }
            catch (final Exception e) {
                throw new IllegalStateException("Failed to instantiate standard serializer (of type " + serClass.getName() + "): " + e.getMessage(), e);
            }
        }
        return null;
    }
    
    public final JsonSerializer<?> findSerializerByPrimaryType(final JavaType type, final SerializationConfig config, final BasicBeanDescription beanDesc, final BeanProperty property, final boolean staticTyping) throws JsonMappingException {
        final Class<?> raw = type.getRawClass();
        if (JsonSerializable.class.isAssignableFrom(raw)) {
            if (JsonSerializableWithType.class.isAssignableFrom(raw)) {
                return SerializableWithTypeSerializer.instance;
            }
            return SerializableSerializer.instance;
        }
        else {
            final AnnotatedMethod valueMethod = beanDesc.findJsonValueMethod();
            if (valueMethod != null) {
                final Method m = valueMethod.getAnnotated();
                if (config.isEnabled(SerializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
                    ClassUtil.checkAndFixAccess(m);
                }
                final JsonSerializer<Object> ser = this.findSerializerFromAnnotation(config, valueMethod, property);
                return new JsonValueSerializer(m, ser, property);
            }
            if (InetAddress.class.isAssignableFrom(raw)) {
                return InetAddressSerializer.instance;
            }
            if (TimeZone.class.isAssignableFrom(raw)) {
                return TimeZoneSerializer.instance;
            }
            if (Charset.class.isAssignableFrom(raw)) {
                return ToStringSerializer.instance;
            }
            final JsonSerializer<?> ser2 = this.optionalHandlers.findSerializer(config, type);
            if (ser2 != null) {
                return ser2;
            }
            if (Number.class.isAssignableFrom(raw)) {
                return StdSerializers.NumberSerializer.instance;
            }
            if (Enum.class.isAssignableFrom(raw)) {
                final Class<Enum<?>> enumClass = (Class<Enum<?>>)raw;
                return EnumSerializer.construct(enumClass, config, beanDesc);
            }
            if (Calendar.class.isAssignableFrom(raw)) {
                return CalendarSerializer.instance;
            }
            if (Date.class.isAssignableFrom(raw)) {
                return DateSerializer.instance;
            }
            return null;
        }
    }
    
    public final JsonSerializer<?> findSerializerByAddonType(final SerializationConfig config, final JavaType javaType, final BasicBeanDescription beanDesc, final BeanProperty property, final boolean staticTyping) throws JsonMappingException {
        final Class<?> type = javaType.getRawClass();
        if (Iterator.class.isAssignableFrom(type)) {
            return this.buildIteratorSerializer(config, javaType, beanDesc, property, staticTyping);
        }
        if (Iterable.class.isAssignableFrom(type)) {
            return this.buildIterableSerializer(config, javaType, beanDesc, property, staticTyping);
        }
        if (CharSequence.class.isAssignableFrom(type)) {
            return ToStringSerializer.instance;
        }
        return null;
    }
    
    protected JsonSerializer<Object> findSerializerFromAnnotation(final SerializationConfig config, final Annotated a, final BeanProperty property) throws JsonMappingException {
        final Object serDef = config.getAnnotationIntrospector().findSerializer(a);
        if (serDef == null) {
            return null;
        }
        if (serDef instanceof JsonSerializer) {
            final JsonSerializer<Object> ser = (JsonSerializer<Object>)serDef;
            if (ser instanceof ContextualSerializer) {
                return ((ContextualSerializer)ser).createContextual(config, property);
            }
            return ser;
        }
        else {
            if (!(serDef instanceof Class)) {
                throw new IllegalStateException("AnnotationIntrospector returned value of type " + serDef.getClass().getName() + "; expected type JsonSerializer or Class<JsonSerializer> instead");
            }
            final Class<?> cls = (Class<?>)serDef;
            if (!JsonSerializer.class.isAssignableFrom(cls)) {
                throw new IllegalStateException("AnnotationIntrospector returned Class " + cls.getName() + "; expected Class<JsonSerializer>");
            }
            final JsonSerializer<Object> ser2 = config.serializerInstance(a, (Class<? extends JsonSerializer<?>>)cls);
            if (ser2 instanceof ContextualSerializer) {
                return ((ContextualSerializer)ser2).createContextual(config, property);
            }
            return ser2;
        }
    }
    
    public JsonSerializer<?> buildContainerSerializer(final SerializationConfig config, final JavaType type, final BasicBeanDescription beanDesc, final BeanProperty property, boolean staticTyping) {
        final JavaType elementType = type.getContentType();
        final TypeSerializer elementTypeSerializer = this.createTypeSerializer(config, elementType, property);
        if (elementTypeSerializer != null) {
            staticTyping = false;
        }
        else if (!staticTyping) {
            staticTyping = this.usesStaticTyping(config, beanDesc, elementTypeSerializer, property);
        }
        final JsonSerializer<Object> elementValueSerializer = findContentSerializer(config, beanDesc.getClassInfo(), property);
        if (type.isMapLikeType()) {
            final MapLikeType mlt = (MapLikeType)type;
            final JsonSerializer<Object> keySerializer = findKeySerializer(config, beanDesc.getClassInfo(), property);
            if (mlt.isTrueMapType()) {
                return this.buildMapSerializer(config, (MapType)mlt, beanDesc, property, staticTyping, keySerializer, elementTypeSerializer, elementValueSerializer);
            }
            return this.buildMapLikeSerializer(config, mlt, beanDesc, property, staticTyping, keySerializer, elementTypeSerializer, elementValueSerializer);
        }
        else if (type.isCollectionLikeType()) {
            final CollectionLikeType clt = (CollectionLikeType)type;
            if (clt.isTrueCollectionType()) {
                return this.buildCollectionSerializer(config, (CollectionType)clt, beanDesc, property, staticTyping, elementTypeSerializer, elementValueSerializer);
            }
            return this.buildCollectionLikeSerializer(config, clt, beanDesc, property, staticTyping, elementTypeSerializer, elementValueSerializer);
        }
        else {
            if (type.isArrayType()) {
                return this.buildArraySerializer(config, (ArrayType)type, beanDesc, property, staticTyping, elementTypeSerializer, elementValueSerializer);
            }
            return null;
        }
    }
    
    protected JsonSerializer<?> buildCollectionLikeSerializer(final SerializationConfig config, final CollectionLikeType type, final BasicBeanDescription beanDesc, final BeanProperty property, final boolean staticTyping, final TypeSerializer elementTypeSerializer, final JsonSerializer<Object> elementValueSerializer) {
        for (final Serializers serializers : this.customSerializers()) {
            final JsonSerializer<?> ser = serializers.findCollectionLikeSerializer(config, type, beanDesc, property, elementTypeSerializer, elementValueSerializer);
            if (ser != null) {
                return ser;
            }
        }
        return null;
    }
    
    protected JsonSerializer<?> buildCollectionSerializer(final SerializationConfig config, final CollectionType type, final BasicBeanDescription beanDesc, final BeanProperty property, final boolean staticTyping, final TypeSerializer elementTypeSerializer, final JsonSerializer<Object> elementValueSerializer) {
        for (final Serializers serializers : this.customSerializers()) {
            final JsonSerializer<?> ser = serializers.findCollectionSerializer(config, type, beanDesc, property, elementTypeSerializer, elementValueSerializer);
            if (ser != null) {
                return ser;
            }
        }
        final Class<?> raw = type.getRawClass();
        if (EnumSet.class.isAssignableFrom(raw)) {
            return this.buildEnumSetSerializer(config, type, beanDesc, property, staticTyping, elementTypeSerializer, elementValueSerializer);
        }
        final Class<?> elementRaw = type.getContentType().getRawClass();
        if (this.isIndexedList(raw)) {
            if (elementRaw == String.class) {
                return new IndexedStringListSerializer(property, elementValueSerializer);
            }
            return StdContainerSerializers.indexedListSerializer(type.getContentType(), staticTyping, elementTypeSerializer, property, elementValueSerializer);
        }
        else {
            if (elementRaw == String.class) {
                return new StringCollectionSerializer(property, elementValueSerializer);
            }
            return StdContainerSerializers.collectionSerializer(type.getContentType(), staticTyping, elementTypeSerializer, property, elementValueSerializer);
        }
    }
    
    protected JsonSerializer<?> buildEnumSetSerializer(final SerializationConfig config, final JavaType type, final BasicBeanDescription beanDesc, final BeanProperty property, final boolean staticTyping, final TypeSerializer elementTypeSerializer, final JsonSerializer<Object> elementValueSerializer) {
        JavaType enumType = type.getContentType();
        if (!enumType.isEnumType()) {
            enumType = null;
        }
        return StdContainerSerializers.enumSetSerializer(enumType, property);
    }
    
    protected boolean isIndexedList(final Class<?> cls) {
        return RandomAccess.class.isAssignableFrom(cls);
    }
    
    protected JsonSerializer<?> buildMapLikeSerializer(final SerializationConfig config, final MapLikeType type, final BasicBeanDescription beanDesc, final BeanProperty property, final boolean staticTyping, final JsonSerializer<Object> keySerializer, final TypeSerializer elementTypeSerializer, final JsonSerializer<Object> elementValueSerializer) {
        for (final Serializers serializers : this.customSerializers()) {
            final JsonSerializer<?> ser = serializers.findMapLikeSerializer(config, type, beanDesc, property, keySerializer, elementTypeSerializer, elementValueSerializer);
            if (ser != null) {
                return ser;
            }
        }
        return null;
    }
    
    protected JsonSerializer<?> buildMapSerializer(final SerializationConfig config, final MapType type, final BasicBeanDescription beanDesc, final BeanProperty property, final boolean staticTyping, final JsonSerializer<Object> keySerializer, final TypeSerializer elementTypeSerializer, final JsonSerializer<Object> elementValueSerializer) {
        for (final Serializers serializers : this.customSerializers()) {
            final JsonSerializer<?> ser = serializers.findMapSerializer(config, type, beanDesc, property, keySerializer, elementTypeSerializer, elementValueSerializer);
            if (ser != null) {
                return ser;
            }
        }
        if (EnumMap.class.isAssignableFrom(type.getRawClass())) {
            return this.buildEnumMapSerializer(config, type, beanDesc, property, staticTyping, elementTypeSerializer, elementValueSerializer);
        }
        return MapSerializer.construct(config.getAnnotationIntrospector().findPropertiesToIgnore(beanDesc.getClassInfo()), type, staticTyping, elementTypeSerializer, property, keySerializer, elementValueSerializer);
    }
    
    protected JsonSerializer<?> buildEnumMapSerializer(final SerializationConfig config, final JavaType type, final BasicBeanDescription beanDesc, final BeanProperty property, final boolean staticTyping, final TypeSerializer elementTypeSerializer, final JsonSerializer<Object> elementValueSerializer) {
        final JavaType keyType = type.getKeyType();
        EnumValues enums = null;
        if (keyType.isEnumType()) {
            final Class<Enum<?>> enumClass = (Class<Enum<?>>)keyType.getRawClass();
            enums = EnumValues.construct(enumClass, config.getAnnotationIntrospector());
        }
        return new EnumMapSerializer(type.getContentType(), staticTyping, enums, elementTypeSerializer, property, elementValueSerializer);
    }
    
    protected JsonSerializer<?> buildArraySerializer(final SerializationConfig config, final ArrayType type, final BasicBeanDescription beanDesc, final BeanProperty property, final boolean staticTyping, final TypeSerializer elementTypeSerializer, final JsonSerializer<Object> elementValueSerializer) {
        final Class<?> raw = type.getRawClass();
        if (String[].class == raw) {
            return new StdArraySerializers.StringArraySerializer(property);
        }
        final JsonSerializer<?> ser = BasicSerializerFactory._arraySerializers.get(raw.getName());
        if (ser != null) {
            return ser;
        }
        return new ObjectArraySerializer(type.getContentType(), staticTyping, elementTypeSerializer, property, elementValueSerializer);
    }
    
    protected JsonSerializer<?> buildIteratorSerializer(final SerializationConfig config, final JavaType type, final BasicBeanDescription beanDesc, final BeanProperty property, final boolean staticTyping) {
        JavaType valueType = type.containedType(0);
        if (valueType == null) {
            valueType = TypeFactory.unknownType();
        }
        final TypeSerializer vts = this.createTypeSerializer(config, valueType, property);
        return StdContainerSerializers.iteratorSerializer(valueType, this.usesStaticTyping(config, beanDesc, vts, property), vts, property);
    }
    
    protected JsonSerializer<?> buildIterableSerializer(final SerializationConfig config, final JavaType type, final BasicBeanDescription beanDesc, final BeanProperty property, final boolean staticTyping) {
        JavaType valueType = type.containedType(0);
        if (valueType == null) {
            valueType = TypeFactory.unknownType();
        }
        final TypeSerializer vts = this.createTypeSerializer(config, valueType, property);
        return StdContainerSerializers.iterableSerializer(valueType, this.usesStaticTyping(config, beanDesc, vts, property), vts, property);
    }
    
    protected <T extends JavaType> T modifyTypeByAnnotation(final SerializationConfig config, final Annotated a, T type) {
        final Class<?> superclass = config.getAnnotationIntrospector().findSerializationType(a);
        if (superclass != null) {
            try {
                type = (T)type.widenBy(superclass);
            }
            catch (final IllegalArgumentException iae) {
                throw new IllegalArgumentException("Failed to widen type " + type + " with concrete-type annotation (value " + superclass.getName() + "), method '" + a.getName() + "': " + iae.getMessage());
            }
        }
        return (T)modifySecondaryTypesByAnnotation(config, a, (JavaType)type);
    }
    
    protected static <T extends JavaType> T modifySecondaryTypesByAnnotation(final SerializationConfig config, final Annotated a, T type) {
        final AnnotationIntrospector intr = config.getAnnotationIntrospector();
        if (type.isContainerType()) {
            final Class<?> keyClass = intr.findSerializationKeyType(a, type.getKeyType());
            if (keyClass != null) {
                if (!(type instanceof MapType)) {
                    throw new IllegalArgumentException("Illegal key-type annotation: type " + type + " is not a Map type");
                }
                try {
                    type = (T)((MapType)type).widenKey(keyClass);
                }
                catch (final IllegalArgumentException iae) {
                    throw new IllegalArgumentException("Failed to narrow key type " + type + " with key-type annotation (" + keyClass.getName() + "): " + iae.getMessage());
                }
            }
            final Class<?> cc = intr.findSerializationContentType(a, type.getContentType());
            if (cc != null) {
                try {
                    type = (T)type.widenContentsBy(cc);
                }
                catch (final IllegalArgumentException iae2) {
                    throw new IllegalArgumentException("Failed to narrow content type " + type + " with content-type annotation (" + cc.getName() + "): " + iae2.getMessage());
                }
            }
        }
        return type;
    }
    
    protected static JsonSerializer<Object> findKeySerializer(final SerializationConfig config, final Annotated a, final BeanProperty property) {
        final AnnotationIntrospector intr = config.getAnnotationIntrospector();
        Class<? extends JsonSerializer<?>> serClass = intr.findKeySerializer(a);
        if ((serClass == null || serClass == JsonSerializer.None.class) && property != null) {
            serClass = intr.findKeySerializer(property.getMember());
        }
        if (serClass != null && serClass != JsonSerializer.None.class) {
            return config.serializerInstance(a, serClass);
        }
        return null;
    }
    
    protected static JsonSerializer<Object> findContentSerializer(final SerializationConfig config, final Annotated a, final BeanProperty property) {
        final AnnotationIntrospector intr = config.getAnnotationIntrospector();
        Class<? extends JsonSerializer<?>> serClass = intr.findContentSerializer(a);
        if ((serClass == null || serClass == JsonSerializer.None.class) && property != null) {
            serClass = intr.findContentSerializer(property.getMember());
        }
        if (serClass != null && serClass != JsonSerializer.None.class) {
            return config.serializerInstance(a, serClass);
        }
        return null;
    }
    
    protected boolean usesStaticTyping(final SerializationConfig config, final BasicBeanDescription beanDesc, final TypeSerializer typeSer, final BeanProperty property) {
        if (typeSer != null) {
            return false;
        }
        final AnnotationIntrospector intr = config.getAnnotationIntrospector();
        final JsonSerialize.Typing t = intr.findSerializationTyping(beanDesc.getClassInfo());
        if (t != null) {
            if (t == JsonSerialize.Typing.STATIC) {
                return true;
            }
        }
        else if (config.isEnabled(SerializationConfig.Feature.USE_STATIC_TYPING)) {
            return true;
        }
        if (property != null) {
            final JavaType type = property.getType();
            if (type.isContainerType()) {
                if (intr.findSerializationContentType(property.getMember(), property.getType()) != null) {
                    return true;
                }
                if (type instanceof MapType && intr.findSerializationKeyType(property.getMember(), property.getType()) != null) {
                    return true;
                }
            }
        }
        return false;
    }
    
    static {
        _concrete = new HashMap<String, JsonSerializer<?>>();
        _concreteLazy = new HashMap<String, Class<? extends JsonSerializer<?>>>();
        BasicSerializerFactory._concrete.put(String.class.getName(), new StringSerializer());
        final ToStringSerializer sls = ToStringSerializer.instance;
        BasicSerializerFactory._concrete.put(StringBuffer.class.getName(), sls);
        BasicSerializerFactory._concrete.put(StringBuilder.class.getName(), sls);
        BasicSerializerFactory._concrete.put(Character.class.getName(), sls);
        BasicSerializerFactory._concrete.put(Character.TYPE.getName(), sls);
        BasicSerializerFactory._concrete.put(Boolean.TYPE.getName(), new StdSerializers.BooleanSerializer(true));
        BasicSerializerFactory._concrete.put(Boolean.class.getName(), new StdSerializers.BooleanSerializer(false));
        final JsonSerializer<?> intS = new StdSerializers.IntegerSerializer();
        BasicSerializerFactory._concrete.put(Integer.class.getName(), intS);
        BasicSerializerFactory._concrete.put(Integer.TYPE.getName(), intS);
        BasicSerializerFactory._concrete.put(Long.class.getName(), StdSerializers.LongSerializer.instance);
        BasicSerializerFactory._concrete.put(Long.TYPE.getName(), StdSerializers.LongSerializer.instance);
        BasicSerializerFactory._concrete.put(Byte.class.getName(), StdSerializers.IntLikeSerializer.instance);
        BasicSerializerFactory._concrete.put(Byte.TYPE.getName(), StdSerializers.IntLikeSerializer.instance);
        BasicSerializerFactory._concrete.put(Short.class.getName(), StdSerializers.IntLikeSerializer.instance);
        BasicSerializerFactory._concrete.put(Short.TYPE.getName(), StdSerializers.IntLikeSerializer.instance);
        BasicSerializerFactory._concrete.put(Float.class.getName(), StdSerializers.FloatSerializer.instance);
        BasicSerializerFactory._concrete.put(Float.TYPE.getName(), StdSerializers.FloatSerializer.instance);
        BasicSerializerFactory._concrete.put(Double.class.getName(), StdSerializers.DoubleSerializer.instance);
        BasicSerializerFactory._concrete.put(Double.TYPE.getName(), StdSerializers.DoubleSerializer.instance);
        final JsonSerializer<?> ns = new StdSerializers.NumberSerializer();
        BasicSerializerFactory._concrete.put(BigInteger.class.getName(), ns);
        BasicSerializerFactory._concrete.put(BigDecimal.class.getName(), ns);
        BasicSerializerFactory._concrete.put(Calendar.class.getName(), CalendarSerializer.instance);
        final DateSerializer dateSer = DateSerializer.instance;
        BasicSerializerFactory._concrete.put(Date.class.getName(), dateSer);
        BasicSerializerFactory._concrete.put(Timestamp.class.getName(), dateSer);
        BasicSerializerFactory._concrete.put(java.sql.Date.class.getName(), new StdSerializers.SqlDateSerializer());
        BasicSerializerFactory._concrete.put(Time.class.getName(), new StdSerializers.SqlTimeSerializer());
        for (final Map.Entry<Class<?>, Object> en : new StdJdkSerializers().provide()) {
            final Object value = en.getValue();
            if (value instanceof JsonSerializer) {
                BasicSerializerFactory._concrete.put(en.getKey().getName(), (JsonSerializer)value);
            }
            else {
                if (!(value instanceof Class)) {
                    throw new IllegalStateException("Internal error: unrecognized value of type " + en.getClass().getName());
                }
                final Class<? extends JsonSerializer<?>> cls = (Class<? extends JsonSerializer<?>>)value;
                BasicSerializerFactory._concreteLazy.put(en.getKey().getName(), cls);
            }
        }
        BasicSerializerFactory._concreteLazy.put(TokenBuffer.class.getName(), TokenBufferSerializer.class);
        (_arraySerializers = new HashMap<String, JsonSerializer<?>>()).put(boolean[].class.getName(), new StdArraySerializers.BooleanArraySerializer());
        BasicSerializerFactory._arraySerializers.put(byte[].class.getName(), new StdArraySerializers.ByteArraySerializer());
        BasicSerializerFactory._arraySerializers.put(char[].class.getName(), new StdArraySerializers.CharArraySerializer());
        BasicSerializerFactory._arraySerializers.put(short[].class.getName(), new StdArraySerializers.ShortArraySerializer());
        BasicSerializerFactory._arraySerializers.put(int[].class.getName(), new StdArraySerializers.IntArraySerializer());
        BasicSerializerFactory._arraySerializers.put(long[].class.getName(), new StdArraySerializers.LongArraySerializer());
        BasicSerializerFactory._arraySerializers.put(float[].class.getName(), new StdArraySerializers.FloatArraySerializer());
        BasicSerializerFactory._arraySerializers.put(double[].class.getName(), new StdArraySerializers.DoubleArraySerializer());
    }
}
