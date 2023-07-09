// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.map.deser.ValueInstantiator;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.Base64Variants;
import org.codehaus.jackson.Base64Variant;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.introspect.NopAnnotationIntrospector;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import java.text.DateFormat;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.type.ClassKey;
import java.util.HashMap;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.map.jsontype.SubtypeResolver;
import org.codehaus.jackson.map.introspect.VisibilityChecker;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.map.util.LinkedNode;

public class DeserializationConfig extends Impl<Feature, DeserializationConfig>
{
    protected LinkedNode<DeserializationProblemHandler> _problemHandlers;
    protected final JsonNodeFactory _nodeFactory;
    protected boolean _sortPropertiesAlphabetically;
    
    public DeserializationConfig(final ClassIntrospector<? extends BeanDescription> intr, final AnnotationIntrospector annIntr, final VisibilityChecker<?> vc, final SubtypeResolver subtypeResolver, final PropertyNamingStrategy propertyNamingStrategy, final TypeFactory typeFactory, final HandlerInstantiator handlerInstantiator) {
        super(intr, annIntr, vc, subtypeResolver, propertyNamingStrategy, typeFactory, handlerInstantiator, Impl.collectFeatureDefaults(Feature.class));
        this._nodeFactory = JsonNodeFactory.instance;
    }
    
    protected DeserializationConfig(final DeserializationConfig src) {
        this(src, src._base);
    }
    
    private DeserializationConfig(final DeserializationConfig src, final HashMap<ClassKey, Class<?>> mixins, final SubtypeResolver str) {
        this(src, src._base);
        this._mixInAnnotations = mixins;
        this._subtypeResolver = str;
    }
    
    protected DeserializationConfig(final DeserializationConfig src, final Base base) {
        super(src, base, src._subtypeResolver);
        this._problemHandlers = src._problemHandlers;
        this._nodeFactory = src._nodeFactory;
        this._sortPropertiesAlphabetically = src._sortPropertiesAlphabetically;
    }
    
    protected DeserializationConfig(final DeserializationConfig src, final JsonNodeFactory f) {
        super(src);
        this._problemHandlers = src._problemHandlers;
        this._nodeFactory = f;
        this._sortPropertiesAlphabetically = src._sortPropertiesAlphabetically;
    }
    
    protected DeserializationConfig(final DeserializationConfig src, final int featureFlags) {
        super(src, featureFlags);
        this._problemHandlers = src._problemHandlers;
        this._nodeFactory = src._nodeFactory;
        this._sortPropertiesAlphabetically = src._sortPropertiesAlphabetically;
    }
    
    protected DeserializationConfig passSerializationFeatures(final int serializationFeatureFlags) {
        this._sortPropertiesAlphabetically = ((serializationFeatureFlags & SerializationConfig.Feature.SORT_PROPERTIES_ALPHABETICALLY.getMask()) != 0x0);
        return this;
    }
    
    @Override
    public DeserializationConfig withClassIntrospector(final ClassIntrospector<? extends BeanDescription> ci) {
        return new DeserializationConfig(this, this._base.withClassIntrospector(ci));
    }
    
    @Override
    public DeserializationConfig withAnnotationIntrospector(final AnnotationIntrospector ai) {
        return new DeserializationConfig(this, this._base.withAnnotationIntrospector(ai));
    }
    
    @Override
    public DeserializationConfig withVisibilityChecker(final VisibilityChecker<?> vc) {
        return new DeserializationConfig(this, this._base.withVisibilityChecker(vc));
    }
    
    @Override
    public DeserializationConfig withVisibility(final JsonMethod forMethod, final JsonAutoDetect.Visibility visibility) {
        return new DeserializationConfig(this, this._base.withVisibility(forMethod, visibility));
    }
    
    @Override
    public DeserializationConfig withTypeResolverBuilder(final TypeResolverBuilder<?> trb) {
        return new DeserializationConfig(this, this._base.withTypeResolverBuilder(trb));
    }
    
    @Override
    public DeserializationConfig withSubtypeResolver(final SubtypeResolver str) {
        final DeserializationConfig cfg = new DeserializationConfig(this);
        cfg._subtypeResolver = str;
        return cfg;
    }
    
    @Override
    public DeserializationConfig withPropertyNamingStrategy(final PropertyNamingStrategy pns) {
        return new DeserializationConfig(this, this._base.withPropertyNamingStrategy(pns));
    }
    
    @Override
    public DeserializationConfig withTypeFactory(final TypeFactory tf) {
        return (tf == this._base.getTypeFactory()) ? this : new DeserializationConfig(this, this._base.withTypeFactory(tf));
    }
    
    @Override
    public DeserializationConfig withDateFormat(final DateFormat df) {
        return (df == this._base.getDateFormat()) ? this : new DeserializationConfig(this, this._base.withDateFormat(df));
    }
    
    @Override
    public DeserializationConfig withHandlerInstantiator(final HandlerInstantiator hi) {
        return (hi == this._base.getHandlerInstantiator()) ? this : new DeserializationConfig(this, this._base.withHandlerInstantiator(hi));
    }
    
    @Override
    public DeserializationConfig withInsertedAnnotationIntrospector(final AnnotationIntrospector ai) {
        return new DeserializationConfig(this, this._base.withInsertedAnnotationIntrospector(ai));
    }
    
    @Override
    public DeserializationConfig withAppendedAnnotationIntrospector(final AnnotationIntrospector ai) {
        return new DeserializationConfig(this, this._base.withAppendedAnnotationIntrospector(ai));
    }
    
    public DeserializationConfig withNodeFactory(final JsonNodeFactory f) {
        return new DeserializationConfig(this, f);
    }
    
    @Override
    public DeserializationConfig with(final Feature... features) {
        int flags = this._featureFlags;
        for (final Feature f : features) {
            flags |= f.getMask();
        }
        return new DeserializationConfig(this, flags);
    }
    
    @Override
    public DeserializationConfig without(final Feature... features) {
        int flags = this._featureFlags;
        for (final Feature f : features) {
            flags &= ~f.getMask();
        }
        return new DeserializationConfig(this, flags);
    }
    
    @Deprecated
    @Override
    public void fromAnnotations(final Class<?> cls) {
        final AnnotationIntrospector ai = this.getAnnotationIntrospector();
        final AnnotatedClass ac = AnnotatedClass.construct(cls, ai, null);
        final VisibilityChecker<?> prevVc = this.getDefaultVisibilityChecker();
        this._base = this._base.withVisibilityChecker(ai.findAutoDetectVisibility(ac, prevVc));
    }
    
    @Override
    public DeserializationConfig createUnshared(final SubtypeResolver subtypeResolver) {
        final HashMap<ClassKey, Class<?>> mixins = this._mixInAnnotations;
        this._mixInAnnotationsShared = true;
        return new DeserializationConfig(this, mixins, subtypeResolver);
    }
    
    @Override
    public AnnotationIntrospector getAnnotationIntrospector() {
        if (this.isEnabled(Feature.USE_ANNOTATIONS)) {
            return super.getAnnotationIntrospector();
        }
        return NopAnnotationIntrospector.instance;
    }
    
    @Override
    public <T extends BeanDescription> T introspectClassAnnotations(final JavaType type) {
        return (T)this.getClassIntrospector().forClassAnnotations(this, type, this);
    }
    
    @Override
    public <T extends BeanDescription> T introspectDirectClassAnnotations(final JavaType type) {
        return (T)this.getClassIntrospector().forDirectClassAnnotations(this, type, this);
    }
    
    @Override
    public boolean isAnnotationProcessingEnabled() {
        return this.isEnabled(Feature.USE_ANNOTATIONS);
    }
    
    @Override
    public boolean canOverrideAccessModifiers() {
        return this.isEnabled(Feature.CAN_OVERRIDE_ACCESS_MODIFIERS);
    }
    
    @Override
    public boolean shouldSortPropertiesAlphabetically() {
        return this._sortPropertiesAlphabetically;
    }
    
    @Override
    public VisibilityChecker<?> getDefaultVisibilityChecker() {
        VisibilityChecker<?> vchecker = super.getDefaultVisibilityChecker();
        if (!this.isEnabled(Feature.AUTO_DETECT_SETTERS)) {
            vchecker = (VisibilityChecker<?>)vchecker.withSetterVisibility(JsonAutoDetect.Visibility.NONE);
        }
        if (!this.isEnabled(Feature.AUTO_DETECT_CREATORS)) {
            vchecker = (VisibilityChecker<?>)vchecker.withCreatorVisibility(JsonAutoDetect.Visibility.NONE);
        }
        if (!this.isEnabled(Feature.AUTO_DETECT_FIELDS)) {
            vchecker = (VisibilityChecker<?>)vchecker.withFieldVisibility(JsonAutoDetect.Visibility.NONE);
        }
        return vchecker;
    }
    
    public boolean isEnabled(final Feature f) {
        return (this._featureFlags & f.getMask()) != 0x0;
    }
    
    @Deprecated
    @Override
    public void enable(final Feature f) {
        super.enable(f);
    }
    
    @Deprecated
    @Override
    public void disable(final Feature f) {
        super.disable(f);
    }
    
    @Deprecated
    @Override
    public void set(final Feature f, final boolean state) {
        super.set(f, state);
    }
    
    public LinkedNode<DeserializationProblemHandler> getProblemHandlers() {
        return this._problemHandlers;
    }
    
    public void addHandler(final DeserializationProblemHandler h) {
        if (!LinkedNode.contains(this._problemHandlers, h)) {
            this._problemHandlers = new LinkedNode<DeserializationProblemHandler>(h, this._problemHandlers);
        }
    }
    
    public void clearHandlers() {
        this._problemHandlers = null;
    }
    
    public Base64Variant getBase64Variant() {
        return Base64Variants.getDefaultVariant();
    }
    
    public final JsonNodeFactory getNodeFactory() {
        return this._nodeFactory;
    }
    
    public <T extends BeanDescription> T introspect(final JavaType type) {
        return (T)this.getClassIntrospector().forDeserialization(this, type, this);
    }
    
    public <T extends BeanDescription> T introspectForCreation(final JavaType type) {
        return (T)this.getClassIntrospector().forCreation(this, type, this);
    }
    
    public JsonDeserializer<Object> deserializerInstance(final Annotated annotated, final Class<? extends JsonDeserializer<?>> deserClass) {
        final HandlerInstantiator hi = this.getHandlerInstantiator();
        if (hi != null) {
            final JsonDeserializer<?> deser = hi.deserializerInstance(this, annotated, deserClass);
            if (deser != null) {
                return (JsonDeserializer<Object>)deser;
            }
        }
        return ClassUtil.createInstance(deserClass, this.canOverrideAccessModifiers());
    }
    
    public KeyDeserializer keyDeserializerInstance(final Annotated annotated, final Class<? extends KeyDeserializer> keyDeserClass) {
        final HandlerInstantiator hi = this.getHandlerInstantiator();
        if (hi != null) {
            final KeyDeserializer keyDeser = hi.keyDeserializerInstance(this, annotated, keyDeserClass);
            if (keyDeser != null) {
                return keyDeser;
            }
        }
        return ClassUtil.createInstance(keyDeserClass, this.canOverrideAccessModifiers());
    }
    
    public ValueInstantiator valueInstantiatorInstance(final Annotated annotated, final Class<? extends ValueInstantiator> instClass) {
        final HandlerInstantiator hi = this.getHandlerInstantiator();
        if (hi != null) {
            final ValueInstantiator inst = hi.valueInstantiatorInstance(this, annotated, instClass);
            if (inst != null) {
                return inst;
            }
        }
        return ClassUtil.createInstance(instClass, this.canOverrideAccessModifiers());
    }
    
    public enum Feature implements ConfigFeature
    {
        USE_ANNOTATIONS(true), 
        AUTO_DETECT_SETTERS(true), 
        AUTO_DETECT_CREATORS(true), 
        AUTO_DETECT_FIELDS(true), 
        USE_GETTERS_AS_SETTERS(true), 
        CAN_OVERRIDE_ACCESS_MODIFIERS(true), 
        USE_BIG_DECIMAL_FOR_FLOATS(false), 
        USE_BIG_INTEGER_FOR_INTS(false), 
        USE_JAVA_ARRAY_FOR_JSON_ARRAY(false), 
        READ_ENUMS_USING_TO_STRING(false), 
        FAIL_ON_UNKNOWN_PROPERTIES(true), 
        FAIL_ON_NULL_FOR_PRIMITIVES(false), 
        FAIL_ON_NUMBERS_FOR_ENUMS(false), 
        WRAP_EXCEPTIONS(true), 
        ACCEPT_SINGLE_VALUE_AS_ARRAY(false), 
        UNWRAP_ROOT_VALUE(false), 
        ACCEPT_EMPTY_STRING_AS_NULL_OBJECT(false);
        
        final boolean _defaultState;
        
        private Feature(final boolean defaultState) {
            this._defaultState = defaultState;
        }
        
        public boolean enabledByDefault() {
            return this._defaultState;
        }
        
        public int getMask() {
            return 1 << this.ordinal();
        }
    }
}
