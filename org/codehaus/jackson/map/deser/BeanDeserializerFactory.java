// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser;

import java.util.Collections;
import java.util.HashSet;
import org.codehaus.jackson.map.introspect.AnnotatedField;
import java.util.List;
import java.util.Collection;
import java.util.Map;
import org.codehaus.jackson.map.BeanPropertyDefinition;
import java.util.HashMap;
import org.codehaus.jackson.map.util.ArrayBuilders;
import org.codehaus.jackson.map.introspect.AnnotatedParameter;
import org.codehaus.jackson.map.introspect.AnnotatedWithParams;
import org.codehaus.jackson.map.deser.impl.CreatorProperty;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.introspect.VisibilityChecker;
import org.codehaus.jackson.map.introspect.AnnotatedConstructor;
import org.codehaus.jackson.map.AnnotationIntrospector;
import java.lang.reflect.Member;
import org.codehaus.jackson.map.deser.impl.CreatorCollector;
import org.codehaus.jackson.map.deser.std.ThrowableDeserializer;
import org.codehaus.jackson.map.AbstractTypeResolver;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.type.MapLikeType;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.map.type.CollectionLikeType;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.Deserializers;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.type.ArrayType;
import org.codehaus.jackson.map.util.EnumResolver;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.JsonMappingException;
import java.util.Iterator;
import org.codehaus.jackson.map.deser.std.StdKeyDeserializers;
import org.codehaus.jackson.map.BeanDescription;
import org.codehaus.jackson.map.KeyDeserializers;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.KeyDeserializer;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.jsontype.impl.SubTypeValidator;
import org.codehaus.jackson.map.DeserializerFactory;
import java.util.Set;

public class BeanDeserializerFactory extends BasicDeserializerFactory
{
    private static final Class<?>[] INIT_CAUSE_PARAMS;
    protected static final Set<String> DEFAULT_NO_DESER_CLASS_NAMES;
    protected Set<String> _cfgIllegalClassNames;
    public static final BeanDeserializerFactory instance;
    protected final Config _factoryConfig;
    protected SubTypeValidator _subtypeValidator;
    
    @Deprecated
    public BeanDeserializerFactory() {
        this(null);
    }
    
    public BeanDeserializerFactory(Config config) {
        this._cfgIllegalClassNames = BeanDeserializerFactory.DEFAULT_NO_DESER_CLASS_NAMES;
        this._subtypeValidator = SubTypeValidator.instance();
        if (config == null) {
            config = new ConfigImpl();
        }
        this._factoryConfig = config;
    }
    
    @Override
    public final Config getConfig() {
        return this._factoryConfig;
    }
    
    @Override
    public DeserializerFactory withConfig(final Config config) {
        if (this._factoryConfig == config) {
            return this;
        }
        if (this.getClass() != BeanDeserializerFactory.class) {
            throw new IllegalStateException("Subtype of BeanDeserializerFactory (" + this.getClass().getName() + ") has not properly overridden method 'withAdditionalDeserializers': can not instantiate subtype with additional deserializer definitions");
        }
        return new BeanDeserializerFactory(config);
    }
    
    @Override
    public KeyDeserializer createKeyDeserializer(final DeserializationConfig config, final JavaType type, final BeanProperty property) throws JsonMappingException {
        if (this._factoryConfig.hasKeyDeserializers()) {
            final BasicBeanDescription beanDesc = config.introspectClassAnnotations(type.getRawClass());
            for (final KeyDeserializers d : this._factoryConfig.keyDeserializers()) {
                final KeyDeserializer deser = d.findKeyDeserializer(type, config, beanDesc, property);
                if (deser != null) {
                    return deser;
                }
            }
        }
        final Class<?> raw = type.getRawClass();
        if (raw == String.class || raw == Object.class) {
            return StdKeyDeserializers.constructStringKeyDeserializer(config, type);
        }
        KeyDeserializer kdes = BeanDeserializerFactory._keyDeserializers.get(type);
        if (kdes != null) {
            return kdes;
        }
        if (type.isEnumType()) {
            return this._createEnumKeyDeserializer(config, type, property);
        }
        kdes = StdKeyDeserializers.findStringBasedKeyDeserializer(config, type);
        return kdes;
    }
    
    private KeyDeserializer _createEnumKeyDeserializer(final DeserializationConfig config, final JavaType type, final BeanProperty property) throws JsonMappingException {
        final BasicBeanDescription beanDesc = config.introspect(type);
        final Class<?> enumClass = type.getRawClass();
        final EnumResolver<?> enumRes = this.constructEnumResolver(enumClass, config);
        for (final AnnotatedMethod factory : beanDesc.getFactoryMethods()) {
            if (config.getAnnotationIntrospector().hasCreatorAnnotation(factory)) {
                final int argCount = factory.getParameterCount();
                if (argCount == 1) {
                    final Class<?> returnType = factory.getRawType();
                    if (returnType.isAssignableFrom(enumClass)) {
                        if (factory.getParameterType(0) != String.class) {
                            throw new IllegalArgumentException("Parameter #0 type for factory method (" + factory + ") not suitable, must be java.lang.String");
                        }
                        if (config.canOverrideAccessModifiers()) {
                            ClassUtil.checkAndFixAccess(factory.getMember());
                        }
                        return StdKeyDeserializers.constructEnumKeyDeserializer(enumRes, factory);
                    }
                }
                throw new IllegalArgumentException("Unsuitable method (" + factory + ") decorated with @JsonCreator (for Enum type " + enumClass.getName() + ")");
            }
        }
        return StdKeyDeserializers.constructEnumKeyDeserializer(enumRes);
    }
    
    @Override
    protected JsonDeserializer<?> _findCustomArrayDeserializer(final ArrayType type, final DeserializationConfig config, final DeserializerProvider provider, final BeanProperty property, final TypeDeserializer elementTypeDeserializer, final JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        for (final Deserializers d : this._factoryConfig.deserializers()) {
            final JsonDeserializer<?> deser = d.findArrayDeserializer(type, config, provider, property, elementTypeDeserializer, elementDeserializer);
            if (deser != null) {
                return deser;
            }
        }
        return null;
    }
    
    @Override
    protected JsonDeserializer<?> _findCustomCollectionDeserializer(final CollectionType type, final DeserializationConfig config, final DeserializerProvider provider, final BasicBeanDescription beanDesc, final BeanProperty property, final TypeDeserializer elementTypeDeserializer, final JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        for (final Deserializers d : this._factoryConfig.deserializers()) {
            final JsonDeserializer<?> deser = d.findCollectionDeserializer(type, config, provider, beanDesc, property, elementTypeDeserializer, elementDeserializer);
            if (deser != null) {
                return deser;
            }
        }
        return null;
    }
    
    @Override
    protected JsonDeserializer<?> _findCustomCollectionLikeDeserializer(final CollectionLikeType type, final DeserializationConfig config, final DeserializerProvider provider, final BasicBeanDescription beanDesc, final BeanProperty property, final TypeDeserializer elementTypeDeserializer, final JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        for (final Deserializers d : this._factoryConfig.deserializers()) {
            final JsonDeserializer<?> deser = d.findCollectionLikeDeserializer(type, config, provider, beanDesc, property, elementTypeDeserializer, elementDeserializer);
            if (deser != null) {
                return deser;
            }
        }
        return null;
    }
    
    @Override
    protected JsonDeserializer<?> _findCustomEnumDeserializer(final Class<?> type, final DeserializationConfig config, final BasicBeanDescription beanDesc, final BeanProperty property) throws JsonMappingException {
        for (final Deserializers d : this._factoryConfig.deserializers()) {
            final JsonDeserializer<?> deser = d.findEnumDeserializer(type, config, beanDesc, property);
            if (deser != null) {
                return deser;
            }
        }
        return null;
    }
    
    @Override
    protected JsonDeserializer<?> _findCustomMapDeserializer(final MapType type, final DeserializationConfig config, final DeserializerProvider provider, final BasicBeanDescription beanDesc, final BeanProperty property, final KeyDeserializer keyDeserializer, final TypeDeserializer elementTypeDeserializer, final JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        for (final Deserializers d : this._factoryConfig.deserializers()) {
            final JsonDeserializer<?> deser = d.findMapDeserializer(type, config, provider, beanDesc, property, keyDeserializer, elementTypeDeserializer, elementDeserializer);
            if (deser != null) {
                return deser;
            }
        }
        return null;
    }
    
    @Override
    protected JsonDeserializer<?> _findCustomMapLikeDeserializer(final MapLikeType type, final DeserializationConfig config, final DeserializerProvider provider, final BasicBeanDescription beanDesc, final BeanProperty property, final KeyDeserializer keyDeserializer, final TypeDeserializer elementTypeDeserializer, final JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        for (final Deserializers d : this._factoryConfig.deserializers()) {
            final JsonDeserializer<?> deser = d.findMapLikeDeserializer(type, config, provider, beanDesc, property, keyDeserializer, elementTypeDeserializer, elementDeserializer);
            if (deser != null) {
                return deser;
            }
        }
        return null;
    }
    
    @Override
    protected JsonDeserializer<?> _findCustomTreeNodeDeserializer(final Class<? extends JsonNode> type, final DeserializationConfig config, final BeanProperty property) throws JsonMappingException {
        for (final Deserializers d : this._factoryConfig.deserializers()) {
            final JsonDeserializer<?> deser = d.findTreeNodeDeserializer(type, config, property);
            if (deser != null) {
                return deser;
            }
        }
        return null;
    }
    
    protected JsonDeserializer<Object> _findCustomBeanDeserializer(final JavaType type, final DeserializationConfig config, final DeserializerProvider provider, final BasicBeanDescription beanDesc, final BeanProperty property) throws JsonMappingException {
        for (final Deserializers d : this._factoryConfig.deserializers()) {
            final JsonDeserializer<?> deser = d.findBeanDeserializer(type, config, provider, beanDesc, property);
            if (deser != null) {
                return (JsonDeserializer<Object>)deser;
            }
        }
        return null;
    }
    
    @Override
    public JavaType mapAbstractType(final DeserializationConfig config, JavaType type) throws JsonMappingException {
        while (true) {
            final JavaType next = this._mapAbstractType2(config, type);
            if (next == null) {
                return type;
            }
            final Class<?> prevCls = type.getRawClass();
            final Class<?> nextCls = next.getRawClass();
            if (prevCls == nextCls || !prevCls.isAssignableFrom(nextCls)) {
                throw new IllegalArgumentException("Invalid abstract type resolution from " + type + " to " + next + ": latter is not a subtype of former");
            }
            type = next;
        }
    }
    
    @Override
    public ValueInstantiator findValueInstantiator(final DeserializationConfig config, final BasicBeanDescription beanDesc) throws JsonMappingException {
        final AnnotatedClass ac = beanDesc.getClassInfo();
        final Object instDef = config.getAnnotationIntrospector().findValueInstantiator(ac);
        ValueInstantiator instantiator;
        if (instDef != null) {
            if (instDef instanceof ValueInstantiator) {
                instantiator = (ValueInstantiator)instDef;
            }
            else {
                if (!(instDef instanceof Class)) {
                    throw new IllegalStateException("Invalid value instantiator returned for type " + beanDesc + ": neither a Class nor ValueInstantiator");
                }
                final Class<?> cls = (Class<?>)instDef;
                if (!ValueInstantiator.class.isAssignableFrom(cls)) {
                    throw new IllegalStateException("Invalid instantiator Class<?> returned for type " + beanDesc + ": " + cls.getName() + " not a ValueInstantiator");
                }
                final Class<? extends ValueInstantiator> instClass = (Class<? extends ValueInstantiator>)cls;
                instantiator = config.valueInstantiatorInstance(ac, instClass);
            }
        }
        else {
            instantiator = this.constructDefaultValueInstantiator(config, beanDesc);
        }
        if (this._factoryConfig.hasValueInstantiators()) {
            for (final ValueInstantiators insts : this._factoryConfig.valueInstantiators()) {
                instantiator = insts.findValueInstantiator(config, beanDesc, instantiator);
                if (instantiator == null) {
                    throw new JsonMappingException("Broken registered ValueInstantiators (of type " + insts.getClass().getName() + "): returned null ValueInstantiator");
                }
            }
        }
        return instantiator;
    }
    
    @Override
    public JsonDeserializer<Object> createBeanDeserializer(final DeserializationConfig config, final DeserializerProvider p, JavaType type, final BeanProperty property) throws JsonMappingException {
        if (type.isAbstract()) {
            type = this.mapAbstractType(config, type);
        }
        BasicBeanDescription beanDesc = config.introspect(type);
        final JsonDeserializer<Object> ad = this.findDeserializerFromAnnotation(config, beanDesc.getClassInfo(), property);
        if (ad != null) {
            return ad;
        }
        final JavaType newType = this.modifyTypeByAnnotation(config, beanDesc.getClassInfo(), type, null);
        if (newType.getRawClass() != type.getRawClass()) {
            type = newType;
            beanDesc = config.introspect(type);
        }
        final JsonDeserializer<Object> custom = this._findCustomBeanDeserializer(type, config, p, beanDesc, property);
        if (custom != null) {
            return custom;
        }
        if (type.isThrowable()) {
            return this.buildThrowableDeserializer(config, type, beanDesc, property);
        }
        if (type.isAbstract()) {
            final JavaType concreteType = this.materializeAbstractType(config, beanDesc);
            if (concreteType != null) {
                beanDesc = config.introspect(concreteType);
                return this.buildBeanDeserializer(config, concreteType, beanDesc, property);
            }
        }
        final JsonDeserializer<Object> deser = this.findStdBeanDeserializer(config, p, type, property);
        if (deser != null) {
            return deser;
        }
        if (!this.isPotentialBeanType(type.getRawClass())) {
            return null;
        }
        this.checkIllegalTypes(type);
        return this.buildBeanDeserializer(config, type, beanDesc, property);
    }
    
    protected JavaType _mapAbstractType2(final DeserializationConfig config, final JavaType type) throws JsonMappingException {
        final Class<?> currClass = type.getRawClass();
        if (this._factoryConfig.hasAbstractTypeResolvers()) {
            for (final AbstractTypeResolver resolver : this._factoryConfig.abstractTypeResolvers()) {
                final JavaType concrete = resolver.findTypeMapping(config, type);
                if (concrete != null && concrete.getRawClass() != currClass) {
                    return concrete;
                }
            }
        }
        return null;
    }
    
    protected JavaType materializeAbstractType(final DeserializationConfig config, final BasicBeanDescription beanDesc) throws JsonMappingException {
        final JavaType abstractType = beanDesc.getType();
        for (final AbstractTypeResolver r : this._factoryConfig.abstractTypeResolvers()) {
            final JavaType concrete = r.resolveAbstractType(config, abstractType);
            if (concrete != null) {
                return concrete;
            }
        }
        return null;
    }
    
    public JsonDeserializer<Object> buildBeanDeserializer(final DeserializationConfig config, final JavaType type, final BasicBeanDescription beanDesc, final BeanProperty property) throws JsonMappingException {
        final ValueInstantiator valueInstantiator = this.findValueInstantiator(config, beanDesc);
        if (type.isAbstract() && !valueInstantiator.canInstantiate()) {
            return new AbstractDeserializer(type);
        }
        BeanDeserializerBuilder builder = this.constructBeanDeserializerBuilder(beanDesc);
        builder.setValueInstantiator(valueInstantiator);
        this.addBeanProps(config, beanDesc, builder);
        this.addReferenceProperties(config, beanDesc, builder);
        this.addInjectables(config, beanDesc, builder);
        if (this._factoryConfig.hasDeserializerModifiers()) {
            for (final BeanDeserializerModifier mod : this._factoryConfig.deserializerModifiers()) {
                builder = mod.updateBuilder(config, beanDesc, builder);
            }
        }
        JsonDeserializer<?> deserializer = builder.build(property);
        if (this._factoryConfig.hasDeserializerModifiers()) {
            for (final BeanDeserializerModifier mod2 : this._factoryConfig.deserializerModifiers()) {
                deserializer = mod2.modifyDeserializer(config, beanDesc, deserializer);
            }
        }
        return (JsonDeserializer<Object>)deserializer;
    }
    
    public JsonDeserializer<Object> buildThrowableDeserializer(final DeserializationConfig config, final JavaType type, final BasicBeanDescription beanDesc, final BeanProperty property) throws JsonMappingException {
        BeanDeserializerBuilder builder = this.constructBeanDeserializerBuilder(beanDesc);
        builder.setValueInstantiator(this.findValueInstantiator(config, beanDesc));
        this.addBeanProps(config, beanDesc, builder);
        final AnnotatedMethod am = beanDesc.findMethod("initCause", BeanDeserializerFactory.INIT_CAUSE_PARAMS);
        if (am != null) {
            final SettableBeanProperty prop = this.constructSettableProperty(config, beanDesc, "cause", am);
            if (prop != null) {
                builder.addOrReplaceProperty(prop, true);
            }
        }
        builder.addIgnorable("localizedMessage");
        builder.addIgnorable("message");
        builder.addIgnorable("suppressed");
        if (this._factoryConfig.hasDeserializerModifiers()) {
            for (final BeanDeserializerModifier mod : this._factoryConfig.deserializerModifiers()) {
                builder = mod.updateBuilder(config, beanDesc, builder);
            }
        }
        JsonDeserializer<?> deserializer = builder.build(property);
        if (deserializer instanceof BeanDeserializer) {
            deserializer = new ThrowableDeserializer((BeanDeserializer)deserializer);
        }
        if (this._factoryConfig.hasDeserializerModifiers()) {
            for (final BeanDeserializerModifier mod2 : this._factoryConfig.deserializerModifiers()) {
                deserializer = mod2.modifyDeserializer(config, beanDesc, deserializer);
            }
        }
        return (JsonDeserializer<Object>)deserializer;
    }
    
    protected BeanDeserializerBuilder constructBeanDeserializerBuilder(final BasicBeanDescription beanDesc) {
        return new BeanDeserializerBuilder(beanDesc);
    }
    
    protected ValueInstantiator constructDefaultValueInstantiator(final DeserializationConfig config, final BasicBeanDescription beanDesc) throws JsonMappingException {
        final boolean fixAccess = config.isEnabled(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS);
        final CreatorCollector creators = new CreatorCollector(beanDesc, fixAccess);
        final AnnotationIntrospector intr = config.getAnnotationIntrospector();
        if (beanDesc.getType().isConcrete()) {
            final AnnotatedConstructor defaultCtor = beanDesc.findDefaultConstructor();
            if (defaultCtor != null) {
                if (fixAccess) {
                    ClassUtil.checkAndFixAccess(defaultCtor.getAnnotated());
                }
                creators.setDefaultConstructor(defaultCtor);
            }
        }
        VisibilityChecker<?> vchecker = config.getDefaultVisibilityChecker();
        vchecker = config.getAnnotationIntrospector().findAutoDetectVisibility(beanDesc.getClassInfo(), vchecker);
        this._addDeserializerFactoryMethods(config, beanDesc, vchecker, intr, creators);
        this._addDeserializerConstructors(config, beanDesc, vchecker, intr, creators);
        return creators.constructValueInstantiator(config);
    }
    
    protected void _addDeserializerConstructors(final DeserializationConfig config, final BasicBeanDescription beanDesc, final VisibilityChecker<?> vchecker, final AnnotationIntrospector intr, final CreatorCollector creators) throws JsonMappingException {
        for (final AnnotatedConstructor ctor : beanDesc.getConstructors()) {
            final int argCount = ctor.getParameterCount();
            if (argCount < 1) {
                continue;
            }
            final boolean isCreator = intr.hasCreatorAnnotation(ctor);
            final boolean isVisible = vchecker.isCreatorVisible(ctor);
            if (argCount == 1) {
                this._handleSingleArgumentConstructor(config, beanDesc, vchecker, intr, creators, ctor, isCreator, isVisible);
            }
            else {
                if (!isCreator && !isVisible) {
                    continue;
                }
                final boolean annotationFound = false;
                AnnotatedParameter nonAnnotatedParam = null;
                int namedCount = 0;
                int injectCount = 0;
                final CreatorProperty[] properties = new CreatorProperty[argCount];
                for (int i = 0; i < argCount; ++i) {
                    final AnnotatedParameter param = ctor.getParameter(i);
                    final String name = (param == null) ? null : intr.findPropertyNameForParam(param);
                    final Object injectId = intr.findInjectableValueId(param);
                    if (name != null && name.length() > 0) {
                        ++namedCount;
                        properties[i] = this.constructCreatorProperty(config, beanDesc, name, i, param, injectId);
                    }
                    else if (injectId != null) {
                        ++injectCount;
                        properties[i] = this.constructCreatorProperty(config, beanDesc, name, i, param, injectId);
                    }
                    else if (nonAnnotatedParam == null) {
                        nonAnnotatedParam = param;
                    }
                }
                if (isCreator || namedCount > 0 || injectCount > 0) {
                    if (namedCount + injectCount == argCount) {
                        creators.addPropertyCreator(ctor, properties);
                    }
                    else {
                        if (namedCount == 0 && injectCount + 1 == argCount) {
                            throw new IllegalArgumentException("Delegated constructor with Injectables not yet supported (see [JACKSON-712]) for " + ctor);
                        }
                        throw new IllegalArgumentException("Argument #" + nonAnnotatedParam.getIndex() + " of constructor " + ctor + " has no property name annotation; must have name when multiple-paramater constructor annotated as Creator");
                    }
                }
                if (!annotationFound) {
                    continue;
                }
                creators.addPropertyCreator(ctor, properties);
            }
        }
    }
    
    protected boolean _handleSingleArgumentConstructor(final DeserializationConfig config, final BasicBeanDescription beanDesc, final VisibilityChecker<?> vchecker, final AnnotationIntrospector intr, final CreatorCollector creators, final AnnotatedConstructor ctor, final boolean isCreator, final boolean isVisible) throws JsonMappingException {
        final AnnotatedParameter param = ctor.getParameter(0);
        final String name = intr.findPropertyNameForParam(param);
        final Object injectId = intr.findInjectableValueId(param);
        if (injectId != null || (name != null && name.length() > 0)) {
            final CreatorProperty[] properties = { this.constructCreatorProperty(config, beanDesc, name, 0, param, injectId) };
            creators.addPropertyCreator(ctor, properties);
            return true;
        }
        final Class<?> type = ctor.getParameterClass(0);
        if (type == String.class) {
            if (isCreator || isVisible) {
                creators.addStringCreator(ctor);
            }
            return true;
        }
        if (type == Integer.TYPE || type == Integer.class) {
            if (isCreator || isVisible) {
                creators.addIntCreator(ctor);
            }
            return true;
        }
        if (type == Long.TYPE || type == Long.class) {
            if (isCreator || isVisible) {
                creators.addLongCreator(ctor);
            }
            return true;
        }
        if (type == Double.TYPE || type == Double.class) {
            if (isCreator || isVisible) {
                creators.addDoubleCreator(ctor);
            }
            return true;
        }
        if (isCreator) {
            creators.addDelegatingCreator(ctor);
            return true;
        }
        return false;
    }
    
    protected void _addDeserializerFactoryMethods(final DeserializationConfig config, final BasicBeanDescription beanDesc, final VisibilityChecker<?> vchecker, final AnnotationIntrospector intr, final CreatorCollector creators) throws JsonMappingException {
        for (final AnnotatedMethod factory : beanDesc.getFactoryMethods()) {
            final int argCount = factory.getParameterCount();
            if (argCount < 1) {
                continue;
            }
            final boolean isCreator = intr.hasCreatorAnnotation(factory);
            if (argCount == 1) {
                final AnnotatedParameter param = factory.getParameter(0);
                final String name = intr.findPropertyNameForParam(param);
                final Object injectId = intr.findInjectableValueId(param);
                if (injectId == null && (name == null || name.length() == 0)) {
                    this._handleSingleArgumentFactory(config, beanDesc, vchecker, intr, creators, factory, isCreator);
                    continue;
                }
            }
            else if (!intr.hasCreatorAnnotation(factory)) {
                continue;
            }
            final CreatorProperty[] properties = new CreatorProperty[argCount];
            for (int i = 0; i < argCount; ++i) {
                final AnnotatedParameter param2 = factory.getParameter(i);
                final String name2 = intr.findPropertyNameForParam(param2);
                final Object injectableId = intr.findInjectableValueId(param2);
                if ((name2 == null || name2.length() == 0) && injectableId == null) {
                    throw new IllegalArgumentException("Argument #" + i + " of factory method " + factory + " has no property name annotation; must have when multiple-paramater static method annotated as Creator");
                }
                properties[i] = this.constructCreatorProperty(config, beanDesc, name2, i, param2, injectableId);
            }
            creators.addPropertyCreator(factory, properties);
        }
    }
    
    protected boolean _handleSingleArgumentFactory(final DeserializationConfig config, final BasicBeanDescription beanDesc, final VisibilityChecker<?> vchecker, final AnnotationIntrospector intr, final CreatorCollector creators, final AnnotatedMethod factory, final boolean isCreator) throws JsonMappingException {
        final Class<?> type = factory.getParameterClass(0);
        if (type == String.class) {
            if (isCreator || vchecker.isCreatorVisible(factory)) {
                creators.addStringCreator(factory);
            }
            return true;
        }
        if (type == Integer.TYPE || type == Integer.class) {
            if (isCreator || vchecker.isCreatorVisible(factory)) {
                creators.addIntCreator(factory);
            }
            return true;
        }
        if (type == Long.TYPE || type == Long.class) {
            if (isCreator || vchecker.isCreatorVisible(factory)) {
                creators.addLongCreator(factory);
            }
            return true;
        }
        if (type == Double.TYPE || type == Double.class) {
            if (isCreator || vchecker.isCreatorVisible(factory)) {
                creators.addDoubleCreator(factory);
            }
            return true;
        }
        if (type == Boolean.TYPE || type == Boolean.class) {
            if (isCreator || vchecker.isCreatorVisible(factory)) {
                creators.addBooleanCreator(factory);
            }
            return true;
        }
        if (intr.hasCreatorAnnotation(factory)) {
            creators.addDelegatingCreator(factory);
            return true;
        }
        return false;
    }
    
    protected CreatorProperty constructCreatorProperty(final DeserializationConfig config, final BasicBeanDescription beanDesc, final String name, final int index, final AnnotatedParameter param, final Object injectableValueId) throws JsonMappingException {
        final JavaType t0 = config.getTypeFactory().constructType(param.getParameterType(), beanDesc.bindingsForBeanType());
        BeanProperty.Std property = new BeanProperty.Std(name, t0, beanDesc.getClassAnnotations(), param);
        JavaType type = this.resolveType(config, beanDesc, t0, param, property);
        if (type != t0) {
            property = property.withType(type);
        }
        final JsonDeserializer<Object> deser = this.findDeserializerFromAnnotation(config, param, property);
        type = this.modifyTypeByAnnotation(config, param, type, name);
        TypeDeserializer typeDeser = type.getTypeHandler();
        if (typeDeser == null) {
            typeDeser = this.findTypeDeserializer(config, type, property);
        }
        CreatorProperty prop = new CreatorProperty(name, type, typeDeser, beanDesc.getClassAnnotations(), param, index, injectableValueId);
        if (deser != null) {
            prop = prop.withValueDeserializer(deser);
        }
        return prop;
    }
    
    protected void addBeanProps(final DeserializationConfig config, final BasicBeanDescription beanDesc, final BeanDeserializerBuilder builder) throws JsonMappingException {
        final List<BeanPropertyDefinition> props = beanDesc.findProperties();
        final AnnotationIntrospector intr = config.getAnnotationIntrospector();
        boolean ignoreAny = false;
        final Boolean B = intr.findIgnoreUnknownProperties(beanDesc.getClassInfo());
        if (B != null) {
            ignoreAny = B;
            builder.setIgnoreUnknownProperties(ignoreAny);
        }
        final Set<String> ignored = ArrayBuilders.arrayToSet(intr.findPropertiesToIgnore(beanDesc.getClassInfo()));
        for (final String propName : ignored) {
            builder.addIgnorable(propName);
        }
        final AnnotatedMethod anySetter = beanDesc.findAnySetter();
        final Collection<String> ignored2 = (anySetter == null) ? beanDesc.getIgnoredPropertyNames() : beanDesc.getIgnoredPropertyNamesForDeser();
        if (ignored2 != null) {
            for (final String propName2 : ignored2) {
                builder.addIgnorable(propName2);
            }
        }
        final HashMap<Class<?>, Boolean> ignoredTypes = new HashMap<Class<?>, Boolean>();
        for (final BeanPropertyDefinition property : props) {
            final String name = property.getName();
            if (ignored.contains(name)) {
                continue;
            }
            if (property.hasConstructorParameter()) {
                builder.addCreatorProperty(property);
            }
            else if (property.hasSetter()) {
                final AnnotatedMethod setter = property.getSetter();
                final Class<?> type = setter.getParameterClass(0);
                if (this.isIgnorableType(config, beanDesc, type, ignoredTypes)) {
                    builder.addIgnorable(name);
                }
                else {
                    final SettableBeanProperty prop = this.constructSettableProperty(config, beanDesc, name, setter);
                    if (prop == null) {
                        continue;
                    }
                    builder.addProperty(prop);
                }
            }
            else {
                if (!property.hasField()) {
                    continue;
                }
                final AnnotatedField field = property.getField();
                final Class<?> type = field.getRawType();
                if (this.isIgnorableType(config, beanDesc, type, ignoredTypes)) {
                    builder.addIgnorable(name);
                }
                else {
                    final SettableBeanProperty prop = this.constructSettableProperty(config, beanDesc, name, field);
                    if (prop == null) {
                        continue;
                    }
                    builder.addProperty(prop);
                }
            }
        }
        if (anySetter != null) {
            builder.setAnySetter(this.constructAnySetter(config, beanDesc, anySetter));
        }
        if (config.isEnabled(DeserializationConfig.Feature.USE_GETTERS_AS_SETTERS)) {
            for (final BeanPropertyDefinition property : props) {
                if (property.hasGetter()) {
                    final String name = property.getName();
                    if (builder.hasProperty(name)) {
                        continue;
                    }
                    if (ignored.contains(name)) {
                        continue;
                    }
                    final AnnotatedMethod getter = property.getGetter();
                    final Class<?> rt = getter.getRawType();
                    if ((!Collection.class.isAssignableFrom(rt) && !Map.class.isAssignableFrom(rt)) || ignored.contains(name) || builder.hasProperty(name)) {
                        continue;
                    }
                    builder.addProperty(this.constructSetterlessProperty(config, beanDesc, name, getter));
                }
            }
        }
    }
    
    protected void addReferenceProperties(final DeserializationConfig config, final BasicBeanDescription beanDesc, final BeanDeserializerBuilder builder) throws JsonMappingException {
        final Map<String, AnnotatedMember> refs = beanDesc.findBackReferenceProperties();
        if (refs != null) {
            for (final Map.Entry<String, AnnotatedMember> en : refs.entrySet()) {
                final String name = en.getKey();
                final AnnotatedMember m = en.getValue();
                if (m instanceof AnnotatedMethod) {
                    builder.addBackReferenceProperty(name, this.constructSettableProperty(config, beanDesc, m.getName(), (AnnotatedMethod)m));
                }
                else {
                    builder.addBackReferenceProperty(name, this.constructSettableProperty(config, beanDesc, m.getName(), (AnnotatedField)m));
                }
            }
        }
    }
    
    protected void addInjectables(final DeserializationConfig config, final BasicBeanDescription beanDesc, final BeanDeserializerBuilder builder) throws JsonMappingException {
        final Map<Object, AnnotatedMember> raw = beanDesc.findInjectables();
        if (raw != null) {
            final boolean fixAccess = config.isEnabled(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS);
            for (final Map.Entry<Object, AnnotatedMember> entry : raw.entrySet()) {
                final AnnotatedMember m = entry.getValue();
                if (fixAccess) {
                    m.fixAccess();
                }
                builder.addInjectable(m.getName(), beanDesc.resolveType(m.getGenericType()), beanDesc.getClassAnnotations(), m, entry.getKey());
            }
        }
    }
    
    protected SettableAnyProperty constructAnySetter(final DeserializationConfig config, final BasicBeanDescription beanDesc, final AnnotatedMethod setter) throws JsonMappingException {
        if (config.isEnabled(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
            setter.fixAccess();
        }
        JavaType type = beanDesc.bindingsForBeanType().resolveType(setter.getParameterType(1));
        final BeanProperty.Std property = new BeanProperty.Std(setter.getName(), type, beanDesc.getClassAnnotations(), setter);
        type = this.resolveType(config, beanDesc, type, setter, property);
        final JsonDeserializer<Object> deser = this.findDeserializerFromAnnotation(config, setter, property);
        if (deser != null) {
            return new SettableAnyProperty(property, setter, type, deser);
        }
        type = this.modifyTypeByAnnotation(config, setter, type, property.getName());
        return new SettableAnyProperty(property, setter, type, null);
    }
    
    protected SettableBeanProperty constructSettableProperty(final DeserializationConfig config, final BasicBeanDescription beanDesc, final String name, final AnnotatedMethod setter) throws JsonMappingException {
        if (config.isEnabled(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
            setter.fixAccess();
        }
        final JavaType t0 = beanDesc.bindingsForBeanType().resolveType(setter.getParameterType(0));
        BeanProperty.Std property = new BeanProperty.Std(name, t0, beanDesc.getClassAnnotations(), setter);
        JavaType type = this.resolveType(config, beanDesc, t0, setter, property);
        if (type != t0) {
            property = property.withType(type);
        }
        final JsonDeserializer<Object> propDeser = this.findDeserializerFromAnnotation(config, setter, property);
        type = this.modifyTypeByAnnotation(config, setter, type, name);
        final TypeDeserializer typeDeser = type.getTypeHandler();
        SettableBeanProperty prop = new SettableBeanProperty.MethodProperty(name, type, typeDeser, beanDesc.getClassAnnotations(), setter);
        if (propDeser != null) {
            prop = prop.withValueDeserializer(propDeser);
        }
        final AnnotationIntrospector.ReferenceProperty ref = config.getAnnotationIntrospector().findReferenceType(setter);
        if (ref != null && ref.isManagedReference()) {
            prop.setManagedReferenceName(ref.getName());
        }
        return prop;
    }
    
    protected SettableBeanProperty constructSettableProperty(final DeserializationConfig config, final BasicBeanDescription beanDesc, final String name, final AnnotatedField field) throws JsonMappingException {
        if (config.isEnabled(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
            field.fixAccess();
        }
        final JavaType t0 = beanDesc.bindingsForBeanType().resolveType(field.getGenericType());
        BeanProperty.Std property = new BeanProperty.Std(name, t0, beanDesc.getClassAnnotations(), field);
        JavaType type = this.resolveType(config, beanDesc, t0, field, property);
        if (type != t0) {
            property = property.withType(type);
        }
        final JsonDeserializer<Object> propDeser = this.findDeserializerFromAnnotation(config, field, property);
        type = this.modifyTypeByAnnotation(config, field, type, name);
        final TypeDeserializer typeDeser = type.getTypeHandler();
        SettableBeanProperty prop = new SettableBeanProperty.FieldProperty(name, type, typeDeser, beanDesc.getClassAnnotations(), field);
        if (propDeser != null) {
            prop = prop.withValueDeserializer(propDeser);
        }
        final AnnotationIntrospector.ReferenceProperty ref = config.getAnnotationIntrospector().findReferenceType(field);
        if (ref != null && ref.isManagedReference()) {
            prop.setManagedReferenceName(ref.getName());
        }
        return prop;
    }
    
    protected SettableBeanProperty constructSetterlessProperty(final DeserializationConfig config, final BasicBeanDescription beanDesc, final String name, final AnnotatedMethod getter) throws JsonMappingException {
        if (config.isEnabled(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
            getter.fixAccess();
        }
        JavaType type = getter.getType(beanDesc.bindingsForBeanType());
        final BeanProperty.Std property = new BeanProperty.Std(name, type, beanDesc.getClassAnnotations(), getter);
        final JsonDeserializer<Object> propDeser = this.findDeserializerFromAnnotation(config, getter, property);
        type = this.modifyTypeByAnnotation(config, getter, type, name);
        final TypeDeserializer typeDeser = type.getTypeHandler();
        SettableBeanProperty prop = new SettableBeanProperty.SetterlessProperty(name, type, typeDeser, beanDesc.getClassAnnotations(), getter);
        if (propDeser != null) {
            prop = prop.withValueDeserializer(propDeser);
        }
        return prop;
    }
    
    protected boolean isPotentialBeanType(final Class<?> type) {
        String typeStr = ClassUtil.canBeABeanType(type);
        if (typeStr != null) {
            throw new IllegalArgumentException("Can not deserialize Class " + type.getName() + " (of type " + typeStr + ") as a Bean");
        }
        if (ClassUtil.isProxyType(type)) {
            throw new IllegalArgumentException("Can not deserialize Proxy class " + type.getName() + " as a Bean");
        }
        typeStr = ClassUtil.isLocalType(type, true);
        if (typeStr != null) {
            throw new IllegalArgumentException("Can not deserialize Class " + type.getName() + " (of type " + typeStr + ") as a Bean");
        }
        return true;
    }
    
    protected boolean isIgnorableType(final DeserializationConfig config, final BasicBeanDescription beanDesc, final Class<?> type, final Map<Class<?>, Boolean> ignoredTypes) {
        Boolean status = ignoredTypes.get(type);
        if (status == null) {
            final BasicBeanDescription desc = config.introspectClassAnnotations(type);
            status = config.getAnnotationIntrospector().isIgnorableType(desc.getClassInfo());
            if (status == null) {
                status = Boolean.FALSE;
            }
        }
        return status;
    }
    
    protected void checkIllegalTypes(final JavaType type) throws JsonMappingException {
        this._subtypeValidator.validateSubType(type);
    }
    
    static {
        INIT_CAUSE_PARAMS = new Class[] { Throwable.class };
        final Set<String> s = new HashSet<String>();
        s.add("org.apache.commons.collections.functors.InvokerTransformer");
        s.add("org.apache.commons.collections.functors.InstantiateTransformer");
        s.add("org.apache.commons.collections4.functors.InvokerTransformer");
        s.add("org.apache.commons.collections4.functors.InstantiateTransformer");
        s.add("org.codehaus.groovy.runtime.ConvertedClosure");
        s.add("org.codehaus.groovy.runtime.MethodClosure");
        s.add("org.springframework.beans.factory.ObjectFactory");
        s.add("com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl");
        s.add("org.apache.xalan.xsltc.trax.TemplatesImpl");
        DEFAULT_NO_DESER_CLASS_NAMES = Collections.unmodifiableSet((Set<? extends String>)s);
        instance = new BeanDeserializerFactory(null);
    }
    
    public static class ConfigImpl extends Config
    {
        protected static final KeyDeserializers[] NO_KEY_DESERIALIZERS;
        protected static final BeanDeserializerModifier[] NO_MODIFIERS;
        protected static final AbstractTypeResolver[] NO_ABSTRACT_TYPE_RESOLVERS;
        protected static final ValueInstantiators[] NO_VALUE_INSTANTIATORS;
        protected final Deserializers[] _additionalDeserializers;
        protected final KeyDeserializers[] _additionalKeyDeserializers;
        protected final BeanDeserializerModifier[] _modifiers;
        protected final AbstractTypeResolver[] _abstractTypeResolvers;
        protected final ValueInstantiators[] _valueInstantiators;
        
        public ConfigImpl() {
            this(null, null, null, null, null);
        }
        
        protected ConfigImpl(final Deserializers[] allAdditionalDeserializers, final KeyDeserializers[] allAdditionalKeyDeserializers, final BeanDeserializerModifier[] modifiers, final AbstractTypeResolver[] atr, final ValueInstantiators[] vi) {
            this._additionalDeserializers = ((allAdditionalDeserializers == null) ? BeanDeserializerFactory.NO_DESERIALIZERS : allAdditionalDeserializers);
            this._additionalKeyDeserializers = ((allAdditionalKeyDeserializers == null) ? ConfigImpl.NO_KEY_DESERIALIZERS : allAdditionalKeyDeserializers);
            this._modifiers = ((modifiers == null) ? ConfigImpl.NO_MODIFIERS : modifiers);
            this._abstractTypeResolvers = ((atr == null) ? ConfigImpl.NO_ABSTRACT_TYPE_RESOLVERS : atr);
            this._valueInstantiators = ((vi == null) ? ConfigImpl.NO_VALUE_INSTANTIATORS : vi);
        }
        
        @Override
        public Config withAdditionalDeserializers(final Deserializers additional) {
            if (additional == null) {
                throw new IllegalArgumentException("Can not pass null Deserializers");
            }
            final Deserializers[] all = ArrayBuilders.insertInListNoDup(this._additionalDeserializers, additional);
            return new ConfigImpl(all, this._additionalKeyDeserializers, this._modifiers, this._abstractTypeResolvers, this._valueInstantiators);
        }
        
        @Override
        public Config withAdditionalKeyDeserializers(final KeyDeserializers additional) {
            if (additional == null) {
                throw new IllegalArgumentException("Can not pass null KeyDeserializers");
            }
            final KeyDeserializers[] all = ArrayBuilders.insertInListNoDup(this._additionalKeyDeserializers, additional);
            return new ConfigImpl(this._additionalDeserializers, all, this._modifiers, this._abstractTypeResolvers, this._valueInstantiators);
        }
        
        @Override
        public Config withDeserializerModifier(final BeanDeserializerModifier modifier) {
            if (modifier == null) {
                throw new IllegalArgumentException("Can not pass null modifier");
            }
            final BeanDeserializerModifier[] all = ArrayBuilders.insertInListNoDup(this._modifiers, modifier);
            return new ConfigImpl(this._additionalDeserializers, this._additionalKeyDeserializers, all, this._abstractTypeResolvers, this._valueInstantiators);
        }
        
        @Override
        public Config withAbstractTypeResolver(final AbstractTypeResolver resolver) {
            if (resolver == null) {
                throw new IllegalArgumentException("Can not pass null resolver");
            }
            final AbstractTypeResolver[] all = ArrayBuilders.insertInListNoDup(this._abstractTypeResolvers, resolver);
            return new ConfigImpl(this._additionalDeserializers, this._additionalKeyDeserializers, this._modifiers, all, this._valueInstantiators);
        }
        
        @Override
        public Config withValueInstantiators(final ValueInstantiators instantiators) {
            if (instantiators == null) {
                throw new IllegalArgumentException("Can not pass null resolver");
            }
            final ValueInstantiators[] all = ArrayBuilders.insertInListNoDup(this._valueInstantiators, instantiators);
            return new ConfigImpl(this._additionalDeserializers, this._additionalKeyDeserializers, this._modifiers, this._abstractTypeResolvers, all);
        }
        
        @Override
        public boolean hasDeserializers() {
            return this._additionalDeserializers.length > 0;
        }
        
        @Override
        public boolean hasKeyDeserializers() {
            return this._additionalKeyDeserializers.length > 0;
        }
        
        @Override
        public boolean hasDeserializerModifiers() {
            return this._modifiers.length > 0;
        }
        
        @Override
        public boolean hasAbstractTypeResolvers() {
            return this._abstractTypeResolvers.length > 0;
        }
        
        @Override
        public boolean hasValueInstantiators() {
            return this._valueInstantiators.length > 0;
        }
        
        @Override
        public Iterable<Deserializers> deserializers() {
            return ArrayBuilders.arrayAsIterable(this._additionalDeserializers);
        }
        
        @Override
        public Iterable<KeyDeserializers> keyDeserializers() {
            return ArrayBuilders.arrayAsIterable(this._additionalKeyDeserializers);
        }
        
        @Override
        public Iterable<BeanDeserializerModifier> deserializerModifiers() {
            return ArrayBuilders.arrayAsIterable(this._modifiers);
        }
        
        @Override
        public Iterable<AbstractTypeResolver> abstractTypeResolvers() {
            return ArrayBuilders.arrayAsIterable(this._abstractTypeResolvers);
        }
        
        @Override
        public Iterable<ValueInstantiators> valueInstantiators() {
            return ArrayBuilders.arrayAsIterable(this._valueInstantiators);
        }
        
        static {
            NO_KEY_DESERIALIZERS = new KeyDeserializers[0];
            NO_MODIFIERS = new BeanDeserializerModifier[0];
            NO_ABSTRACT_TYPE_RESOLVERS = new AbstractTypeResolver[0];
            NO_VALUE_INSTANTIATORS = new ValueInstantiators[0];
        }
    }
}
