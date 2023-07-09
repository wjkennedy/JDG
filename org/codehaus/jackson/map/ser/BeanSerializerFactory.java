// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser;

import java.util.HashMap;
import java.util.HashSet;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.util.ArrayBuilders;
import org.codehaus.jackson.map.type.TypeBindings;
import org.codehaus.jackson.map.BeanPropertyDefinition;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import java.util.List;
import org.codehaus.jackson.map.ser.std.MapSerializer;
import java.util.ArrayList;
import org.codehaus.jackson.map.jsontype.NamedType;
import java.util.Collection;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.JsonMappingException;
import java.util.Iterator;
import org.codehaus.jackson.map.BeanDescription;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.Serializers;
import org.codehaus.jackson.map.SerializerFactory;

public class BeanSerializerFactory extends BasicSerializerFactory
{
    public static final BeanSerializerFactory instance;
    protected final Config _factoryConfig;
    
    protected BeanSerializerFactory(Config config) {
        if (config == null) {
            config = new ConfigImpl();
        }
        this._factoryConfig = config;
    }
    
    @Override
    public Config getConfig() {
        return this._factoryConfig;
    }
    
    @Override
    public SerializerFactory withConfig(final Config config) {
        if (this._factoryConfig == config) {
            return this;
        }
        if (this.getClass() != BeanSerializerFactory.class) {
            throw new IllegalStateException("Subtype of BeanSerializerFactory (" + this.getClass().getName() + ") has not properly overridden method 'withAdditionalSerializers': can not instantiate subtype with additional serializer definitions");
        }
        return new BeanSerializerFactory(config);
    }
    
    @Override
    protected Iterable<Serializers> customSerializers() {
        return this._factoryConfig.serializers();
    }
    
    @Override
    public JsonSerializer<Object> createSerializer(final SerializationConfig config, final JavaType origType, final BeanProperty property) throws JsonMappingException {
        BasicBeanDescription beanDesc = config.introspect(origType);
        JsonSerializer<?> ser = this.findSerializerFromAnnotation(config, beanDesc.getClassInfo(), property);
        if (ser != null) {
            return (JsonSerializer<Object>)ser;
        }
        final JavaType type = this.modifyTypeByAnnotation(config, beanDesc.getClassInfo(), origType);
        final boolean staticTyping = type != origType;
        if (type != origType && type.getRawClass() != origType.getRawClass()) {
            beanDesc = config.introspect(type);
        }
        if (origType.isContainerType()) {
            return (JsonSerializer<Object>)this.buildContainerSerializer(config, type, beanDesc, property, staticTyping);
        }
        for (final Serializers serializers : this._factoryConfig.serializers()) {
            ser = serializers.findSerializer(config, type, beanDesc, property);
            if (ser != null) {
                return (JsonSerializer<Object>)ser;
            }
        }
        ser = this.findSerializerByLookup(type, config, beanDesc, property, staticTyping);
        if (ser == null) {
            ser = this.findSerializerByPrimaryType(type, config, beanDesc, property, staticTyping);
            if (ser == null) {
                ser = this.findBeanSerializer(config, type, beanDesc, property);
                if (ser == null) {
                    ser = this.findSerializerByAddonType(config, type, beanDesc, property, staticTyping);
                }
            }
        }
        return (JsonSerializer<Object>)ser;
    }
    
    @Override
    public JsonSerializer<Object> createKeySerializer(final SerializationConfig config, final JavaType type, final BeanProperty property) {
        if (!this._factoryConfig.hasKeySerializers()) {
            return null;
        }
        final BasicBeanDescription beanDesc = config.introspectClassAnnotations(type.getRawClass());
        JsonSerializer<?> ser = null;
        for (final Serializers serializers : this._factoryConfig.keySerializers()) {
            ser = serializers.findSerializer(config, type, beanDesc, property);
            if (ser != null) {
                break;
            }
        }
        return (JsonSerializer<Object>)ser;
    }
    
    public JsonSerializer<Object> findBeanSerializer(final SerializationConfig config, final JavaType type, final BasicBeanDescription beanDesc, final BeanProperty property) throws JsonMappingException {
        if (!this.isPotentialBeanType(type.getRawClass())) {
            return null;
        }
        JsonSerializer<Object> serializer = this.constructBeanSerializer(config, beanDesc, property);
        if (this._factoryConfig.hasSerializerModifiers()) {
            for (final BeanSerializerModifier mod : this._factoryConfig.serializerModifiers()) {
                serializer = (JsonSerializer<Object>)mod.modifySerializer(config, beanDesc, serializer);
            }
        }
        return serializer;
    }
    
    public TypeSerializer findPropertyTypeSerializer(final JavaType baseType, final SerializationConfig config, final AnnotatedMember accessor, final BeanProperty property) throws JsonMappingException {
        final AnnotationIntrospector ai = config.getAnnotationIntrospector();
        final TypeResolverBuilder<?> b = ai.findPropertyTypeResolver(config, accessor, baseType);
        if (b == null) {
            return this.createTypeSerializer(config, baseType, property);
        }
        final Collection<NamedType> subtypes = config.getSubtypeResolver().collectAndResolveSubtypes(accessor, config, ai);
        return b.buildTypeSerializer(config, baseType, subtypes, property);
    }
    
    public TypeSerializer findPropertyContentTypeSerializer(final JavaType containerType, final SerializationConfig config, final AnnotatedMember accessor, final BeanProperty property) throws JsonMappingException {
        final JavaType contentType = containerType.getContentType();
        final AnnotationIntrospector ai = config.getAnnotationIntrospector();
        final TypeResolverBuilder<?> b = ai.findPropertyContentTypeResolver(config, accessor, containerType);
        if (b == null) {
            return this.createTypeSerializer(config, contentType, property);
        }
        final Collection<NamedType> subtypes = config.getSubtypeResolver().collectAndResolveSubtypes(accessor, config, ai);
        return b.buildTypeSerializer(config, contentType, subtypes, property);
    }
    
    protected JsonSerializer<Object> constructBeanSerializer(final SerializationConfig config, final BasicBeanDescription beanDesc, final BeanProperty property) throws JsonMappingException {
        if (beanDesc.getBeanClass() == Object.class) {
            throw new IllegalArgumentException("Can not create bean serializer for Object.class");
        }
        BeanSerializerBuilder builder = this.constructBeanSerializerBuilder(beanDesc);
        List<BeanPropertyWriter> props = this.findBeanProperties(config, beanDesc);
        if (props == null) {
            props = new ArrayList<BeanPropertyWriter>();
        }
        if (this._factoryConfig.hasSerializerModifiers()) {
            for (final BeanSerializerModifier mod : this._factoryConfig.serializerModifiers()) {
                props = mod.changeProperties(config, beanDesc, props);
            }
        }
        props = this.filterBeanProperties(config, beanDesc, props);
        props = this.sortBeanProperties(config, beanDesc, props);
        if (this._factoryConfig.hasSerializerModifiers()) {
            for (final BeanSerializerModifier mod : this._factoryConfig.serializerModifiers()) {
                props = mod.orderProperties(config, beanDesc, props);
            }
        }
        builder.setProperties(props);
        builder.setFilterId(this.findFilterId(config, beanDesc));
        final AnnotatedMethod anyGetter = beanDesc.findAnyGetter();
        if (anyGetter != null) {
            if (config.isEnabled(SerializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
                anyGetter.fixAccess();
            }
            final JavaType type = anyGetter.getType(beanDesc.bindingsForBeanType());
            final boolean staticTyping = config.isEnabled(SerializationConfig.Feature.USE_STATIC_TYPING);
            final JavaType valueType = type.getContentType();
            final TypeSerializer typeSer = this.createTypeSerializer(config, valueType, property);
            final MapSerializer mapSer = MapSerializer.construct(null, type, staticTyping, typeSer, property, null, null);
            builder.setAnyGetter(new AnyGetterWriter(anyGetter, mapSer));
        }
        this.processViews(config, builder);
        if (this._factoryConfig.hasSerializerModifiers()) {
            for (final BeanSerializerModifier mod2 : this._factoryConfig.serializerModifiers()) {
                builder = mod2.updateBuilder(config, beanDesc, builder);
            }
        }
        final JsonSerializer<Object> ser = (JsonSerializer<Object>)builder.build();
        if (ser == null && beanDesc.hasKnownClassAnnotations()) {
            return builder.createDummy();
        }
        return ser;
    }
    
    protected BeanPropertyWriter constructFilteredBeanWriter(final BeanPropertyWriter writer, final Class<?>[] inViews) {
        return FilteredBeanPropertyWriter.constructViewBased(writer, inViews);
    }
    
    protected PropertyBuilder constructPropertyBuilder(final SerializationConfig config, final BasicBeanDescription beanDesc) {
        return new PropertyBuilder(config, beanDesc);
    }
    
    protected BeanSerializerBuilder constructBeanSerializerBuilder(final BasicBeanDescription beanDesc) {
        return new BeanSerializerBuilder(beanDesc);
    }
    
    protected Object findFilterId(final SerializationConfig config, final BasicBeanDescription beanDesc) {
        return config.getAnnotationIntrospector().findFilterId(beanDesc.getClassInfo());
    }
    
    protected boolean isPotentialBeanType(final Class<?> type) {
        return ClassUtil.canBeABeanType(type) == null && !ClassUtil.isProxyType(type);
    }
    
    protected List<BeanPropertyWriter> findBeanProperties(final SerializationConfig config, final BasicBeanDescription beanDesc) throws JsonMappingException {
        final List<BeanPropertyDefinition> properties = beanDesc.findProperties();
        final AnnotationIntrospector intr = config.getAnnotationIntrospector();
        this.removeIgnorableTypes(config, beanDesc, properties);
        if (config.isEnabled(SerializationConfig.Feature.REQUIRE_SETTERS_FOR_GETTERS)) {
            this.removeSetterlessGetters(config, beanDesc, properties);
        }
        if (properties.isEmpty()) {
            return null;
        }
        final boolean staticTyping = this.usesStaticTyping(config, beanDesc, null, null);
        final PropertyBuilder pb = this.constructPropertyBuilder(config, beanDesc);
        final ArrayList<BeanPropertyWriter> result = new ArrayList<BeanPropertyWriter>(properties.size());
        final TypeBindings typeBind = beanDesc.bindingsForBeanType();
        for (final BeanPropertyDefinition property : properties) {
            final AnnotatedMember accessor = property.getAccessor();
            final AnnotationIntrospector.ReferenceProperty prop = intr.findReferenceType(accessor);
            if (prop != null && prop.isBackReference()) {
                continue;
            }
            final String name = property.getName();
            if (accessor instanceof AnnotatedMethod) {
                result.add(this._constructWriter(config, typeBind, pb, staticTyping, name, accessor));
            }
            else {
                result.add(this._constructWriter(config, typeBind, pb, staticTyping, name, accessor));
            }
        }
        return result;
    }
    
    protected List<BeanPropertyWriter> filterBeanProperties(final SerializationConfig config, final BasicBeanDescription beanDesc, final List<BeanPropertyWriter> props) {
        final AnnotationIntrospector intr = config.getAnnotationIntrospector();
        final AnnotatedClass ac = beanDesc.getClassInfo();
        final String[] ignored = intr.findPropertiesToIgnore(ac);
        if (ignored != null && ignored.length > 0) {
            final HashSet<String> ignoredSet = ArrayBuilders.arrayToSet(ignored);
            final Iterator<BeanPropertyWriter> it = props.iterator();
            while (it.hasNext()) {
                if (ignoredSet.contains(it.next().getName())) {
                    it.remove();
                }
            }
        }
        return props;
    }
    
    @Deprecated
    protected List<BeanPropertyWriter> sortBeanProperties(final SerializationConfig config, final BasicBeanDescription beanDesc, final List<BeanPropertyWriter> props) {
        return props;
    }
    
    protected void processViews(final SerializationConfig config, final BeanSerializerBuilder builder) {
        final List<BeanPropertyWriter> props = builder.getProperties();
        final boolean includeByDefault = config.isEnabled(SerializationConfig.Feature.DEFAULT_VIEW_INCLUSION);
        final int propCount = props.size();
        int viewsFound = 0;
        final BeanPropertyWriter[] filtered = new BeanPropertyWriter[propCount];
        for (int i = 0; i < propCount; ++i) {
            final BeanPropertyWriter bpw = props.get(i);
            final Class<?>[] views = bpw.getViews();
            if (views == null) {
                if (includeByDefault) {
                    filtered[i] = bpw;
                }
            }
            else {
                ++viewsFound;
                filtered[i] = this.constructFilteredBeanWriter(bpw, views);
            }
        }
        if (includeByDefault && viewsFound == 0) {
            return;
        }
        builder.setFilteredProperties(filtered);
    }
    
    protected void removeIgnorableTypes(final SerializationConfig config, final BasicBeanDescription beanDesc, final List<BeanPropertyDefinition> properties) {
        final AnnotationIntrospector intr = config.getAnnotationIntrospector();
        final HashMap<Class<?>, Boolean> ignores = new HashMap<Class<?>, Boolean>();
        final Iterator<BeanPropertyDefinition> it = properties.iterator();
        while (it.hasNext()) {
            final BeanPropertyDefinition property = it.next();
            final AnnotatedMember accessor = property.getAccessor();
            if (accessor == null) {
                it.remove();
            }
            else {
                final Class<?> type = accessor.getRawType();
                Boolean result = ignores.get(type);
                if (result == null) {
                    final BasicBeanDescription desc = config.introspectClassAnnotations(type);
                    final AnnotatedClass ac = desc.getClassInfo();
                    result = intr.isIgnorableType(ac);
                    if (result == null) {
                        result = Boolean.FALSE;
                    }
                    ignores.put(type, result);
                }
                if (!result) {
                    continue;
                }
                it.remove();
            }
        }
    }
    
    protected void removeSetterlessGetters(final SerializationConfig config, final BasicBeanDescription beanDesc, final List<BeanPropertyDefinition> properties) {
        final Iterator<BeanPropertyDefinition> it = properties.iterator();
        while (it.hasNext()) {
            final BeanPropertyDefinition property = it.next();
            if (!property.couldDeserialize() && !property.isExplicitlyIncluded()) {
                it.remove();
            }
        }
    }
    
    protected BeanPropertyWriter _constructWriter(final SerializationConfig config, final TypeBindings typeContext, final PropertyBuilder pb, final boolean staticTyping, final String name, final AnnotatedMember accessor) throws JsonMappingException {
        if (config.isEnabled(SerializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
            accessor.fixAccess();
        }
        final JavaType type = accessor.getType(typeContext);
        final BeanProperty.Std property = new BeanProperty.Std(name, type, pb.getClassAnnotations(), accessor);
        final JsonSerializer<Object> annotatedSerializer = this.findSerializerFromAnnotation(config, accessor, property);
        TypeSerializer contentTypeSer = null;
        if (ClassUtil.isCollectionMapOrArray(type.getRawClass())) {
            contentTypeSer = this.findPropertyContentTypeSerializer(type, config, accessor, property);
        }
        final TypeSerializer typeSer = this.findPropertyTypeSerializer(type, config, accessor, property);
        final BeanPropertyWriter pbw = pb.buildWriter(name, type, annotatedSerializer, typeSer, contentTypeSer, accessor, staticTyping);
        final AnnotationIntrospector intr = config.getAnnotationIntrospector();
        pbw.setViews(intr.findSerializationViews(accessor));
        return pbw;
    }
    
    static {
        instance = new BeanSerializerFactory(null);
    }
    
    public static class ConfigImpl extends Config
    {
        protected static final Serializers[] NO_SERIALIZERS;
        protected static final BeanSerializerModifier[] NO_MODIFIERS;
        protected final Serializers[] _additionalSerializers;
        protected final Serializers[] _additionalKeySerializers;
        protected final BeanSerializerModifier[] _modifiers;
        
        public ConfigImpl() {
            this(null, null, null);
        }
        
        protected ConfigImpl(final Serializers[] allAdditionalSerializers, final Serializers[] allAdditionalKeySerializers, final BeanSerializerModifier[] modifiers) {
            this._additionalSerializers = ((allAdditionalSerializers == null) ? ConfigImpl.NO_SERIALIZERS : allAdditionalSerializers);
            this._additionalKeySerializers = ((allAdditionalKeySerializers == null) ? ConfigImpl.NO_SERIALIZERS : allAdditionalKeySerializers);
            this._modifiers = ((modifiers == null) ? ConfigImpl.NO_MODIFIERS : modifiers);
        }
        
        @Override
        public Config withAdditionalSerializers(final Serializers additional) {
            if (additional == null) {
                throw new IllegalArgumentException("Can not pass null Serializers");
            }
            final Serializers[] all = ArrayBuilders.insertInListNoDup(this._additionalSerializers, additional);
            return new ConfigImpl(all, this._additionalKeySerializers, this._modifiers);
        }
        
        @Override
        public Config withAdditionalKeySerializers(final Serializers additional) {
            if (additional == null) {
                throw new IllegalArgumentException("Can not pass null Serializers");
            }
            final Serializers[] all = ArrayBuilders.insertInListNoDup(this._additionalKeySerializers, additional);
            return new ConfigImpl(this._additionalSerializers, all, this._modifiers);
        }
        
        @Override
        public Config withSerializerModifier(final BeanSerializerModifier modifier) {
            if (modifier == null) {
                throw new IllegalArgumentException("Can not pass null modifier");
            }
            final BeanSerializerModifier[] modifiers = ArrayBuilders.insertInListNoDup(this._modifiers, modifier);
            return new ConfigImpl(this._additionalSerializers, this._additionalKeySerializers, modifiers);
        }
        
        @Override
        public boolean hasSerializers() {
            return this._additionalSerializers.length > 0;
        }
        
        @Override
        public boolean hasKeySerializers() {
            return this._additionalKeySerializers.length > 0;
        }
        
        @Override
        public boolean hasSerializerModifiers() {
            return this._modifiers.length > 0;
        }
        
        @Override
        public Iterable<Serializers> serializers() {
            return ArrayBuilders.arrayAsIterable(this._additionalSerializers);
        }
        
        @Override
        public Iterable<Serializers> keySerializers() {
            return ArrayBuilders.arrayAsIterable(this._additionalKeySerializers);
        }
        
        @Override
        public Iterable<BeanSerializerModifier> serializerModifiers() {
            return ArrayBuilders.arrayAsIterable(this._modifiers);
        }
        
        static {
            NO_SERIALIZERS = new Serializers[0];
            NO_MODIFIERS = new BeanSerializerModifier[0];
        }
    }
}
