// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.introspect.Annotated;
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
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize;

public class SerializationConfig extends Impl<Feature, SerializationConfig>
{
    protected JsonSerialize.Inclusion _serializationInclusion;
    protected Class<?> _serializationView;
    protected FilterProvider _filterProvider;
    
    public SerializationConfig(final ClassIntrospector<? extends BeanDescription> intr, final AnnotationIntrospector annIntr, final VisibilityChecker<?> vc, final SubtypeResolver subtypeResolver, final PropertyNamingStrategy propertyNamingStrategy, final TypeFactory typeFactory, final HandlerInstantiator handlerInstantiator) {
        super(intr, annIntr, vc, subtypeResolver, propertyNamingStrategy, typeFactory, handlerInstantiator, Impl.collectFeatureDefaults(Feature.class));
        this._serializationInclusion = null;
        this._filterProvider = null;
    }
    
    protected SerializationConfig(final SerializationConfig src) {
        this(src, src._base);
    }
    
    protected SerializationConfig(final SerializationConfig src, final HashMap<ClassKey, Class<?>> mixins, final SubtypeResolver str) {
        this(src, src._base);
        this._mixInAnnotations = mixins;
        this._subtypeResolver = str;
    }
    
    protected SerializationConfig(final SerializationConfig src, final Base base) {
        super(src, base, src._subtypeResolver);
        this._serializationInclusion = null;
        this._serializationInclusion = src._serializationInclusion;
        this._serializationView = src._serializationView;
        this._filterProvider = src._filterProvider;
    }
    
    protected SerializationConfig(final SerializationConfig src, final FilterProvider filters) {
        super(src);
        this._serializationInclusion = null;
        this._serializationInclusion = src._serializationInclusion;
        this._serializationView = src._serializationView;
        this._filterProvider = filters;
    }
    
    protected SerializationConfig(final SerializationConfig src, final Class<?> view) {
        super(src);
        this._serializationInclusion = null;
        this._serializationInclusion = src._serializationInclusion;
        this._serializationView = view;
        this._filterProvider = src._filterProvider;
    }
    
    protected SerializationConfig(final SerializationConfig src, final JsonSerialize.Inclusion incl) {
        super(src);
        this._serializationInclusion = null;
        this._serializationInclusion = incl;
        if (incl == JsonSerialize.Inclusion.NON_NULL) {
            this._featureFlags &= ~Feature.WRITE_NULL_PROPERTIES.getMask();
        }
        else {
            this._featureFlags |= Feature.WRITE_NULL_PROPERTIES.getMask();
        }
        this._serializationView = src._serializationView;
        this._filterProvider = src._filterProvider;
    }
    
    protected SerializationConfig(final SerializationConfig src, final int features) {
        super(src, features);
        this._serializationInclusion = null;
        this._serializationInclusion = src._serializationInclusion;
        this._serializationView = src._serializationView;
        this._filterProvider = src._filterProvider;
    }
    
    @Override
    public SerializationConfig withClassIntrospector(final ClassIntrospector<? extends BeanDescription> ci) {
        return new SerializationConfig(this, this._base.withClassIntrospector(ci));
    }
    
    @Override
    public SerializationConfig withAnnotationIntrospector(final AnnotationIntrospector ai) {
        return new SerializationConfig(this, this._base.withAnnotationIntrospector(ai));
    }
    
    @Override
    public SerializationConfig withInsertedAnnotationIntrospector(final AnnotationIntrospector ai) {
        return new SerializationConfig(this, this._base.withInsertedAnnotationIntrospector(ai));
    }
    
    @Override
    public SerializationConfig withAppendedAnnotationIntrospector(final AnnotationIntrospector ai) {
        return new SerializationConfig(this, this._base.withAppendedAnnotationIntrospector(ai));
    }
    
    @Override
    public SerializationConfig withVisibilityChecker(final VisibilityChecker<?> vc) {
        return new SerializationConfig(this, this._base.withVisibilityChecker(vc));
    }
    
    @Override
    public SerializationConfig withVisibility(final JsonMethod forMethod, final JsonAutoDetect.Visibility visibility) {
        return new SerializationConfig(this, this._base.withVisibility(forMethod, visibility));
    }
    
    @Override
    public SerializationConfig withTypeResolverBuilder(final TypeResolverBuilder<?> trb) {
        return new SerializationConfig(this, this._base.withTypeResolverBuilder(trb));
    }
    
    @Override
    public SerializationConfig withSubtypeResolver(final SubtypeResolver str) {
        final SerializationConfig cfg = new SerializationConfig(this);
        cfg._subtypeResolver = str;
        return cfg;
    }
    
    @Override
    public SerializationConfig withPropertyNamingStrategy(final PropertyNamingStrategy pns) {
        return new SerializationConfig(this, this._base.withPropertyNamingStrategy(pns));
    }
    
    @Override
    public SerializationConfig withTypeFactory(final TypeFactory tf) {
        return new SerializationConfig(this, this._base.withTypeFactory(tf));
    }
    
    @Override
    public SerializationConfig withDateFormat(final DateFormat df) {
        SerializationConfig cfg = new SerializationConfig(this, this._base.withDateFormat(df));
        if (df == null) {
            cfg = cfg.with(Feature.WRITE_DATES_AS_TIMESTAMPS);
        }
        else {
            cfg = cfg.without(Feature.WRITE_DATES_AS_TIMESTAMPS);
        }
        return cfg;
    }
    
    @Override
    public SerializationConfig withHandlerInstantiator(final HandlerInstantiator hi) {
        return new SerializationConfig(this, this._base.withHandlerInstantiator(hi));
    }
    
    public SerializationConfig withFilters(final FilterProvider filterProvider) {
        return new SerializationConfig(this, filterProvider);
    }
    
    public SerializationConfig withView(final Class<?> view) {
        return new SerializationConfig(this, view);
    }
    
    public SerializationConfig withSerializationInclusion(final JsonSerialize.Inclusion incl) {
        return new SerializationConfig(this, incl);
    }
    
    @Override
    public SerializationConfig with(final Feature... features) {
        int flags = this._featureFlags;
        for (final Feature f : features) {
            flags |= f.getMask();
        }
        return new SerializationConfig(this, flags);
    }
    
    @Override
    public SerializationConfig without(final Feature... features) {
        int flags = this._featureFlags;
        for (final Feature f : features) {
            flags &= ~f.getMask();
        }
        return new SerializationConfig(this, flags);
    }
    
    @Deprecated
    @Override
    public void fromAnnotations(final Class<?> cls) {
        final AnnotationIntrospector ai = this.getAnnotationIntrospector();
        final AnnotatedClass ac = AnnotatedClass.construct(cls, ai, null);
        this._base = this._base.withVisibilityChecker(ai.findAutoDetectVisibility(ac, this.getDefaultVisibilityChecker()));
        final JsonSerialize.Inclusion incl = ai.findSerializationInclusion(ac, null);
        if (incl != this._serializationInclusion) {
            this.setSerializationInclusion(incl);
        }
        final JsonSerialize.Typing typing = ai.findSerializationTyping(ac);
        if (typing != null) {
            this.set(Feature.USE_STATIC_TYPING, typing == JsonSerialize.Typing.STATIC);
        }
    }
    
    @Override
    public SerializationConfig createUnshared(final SubtypeResolver subtypeResolver) {
        final HashMap<ClassKey, Class<?>> mixins = this._mixInAnnotations;
        this._mixInAnnotationsShared = true;
        return new SerializationConfig(this, mixins, subtypeResolver);
    }
    
    @Override
    public AnnotationIntrospector getAnnotationIntrospector() {
        if (this.isEnabled(Feature.USE_ANNOTATIONS)) {
            return super.getAnnotationIntrospector();
        }
        return AnnotationIntrospector.nopInstance();
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
        return this.isEnabled(Feature.SORT_PROPERTIES_ALPHABETICALLY);
    }
    
    @Override
    public VisibilityChecker<?> getDefaultVisibilityChecker() {
        VisibilityChecker<?> vchecker = super.getDefaultVisibilityChecker();
        if (!this.isEnabled(Feature.AUTO_DETECT_GETTERS)) {
            vchecker = (VisibilityChecker<?>)vchecker.withGetterVisibility(JsonAutoDetect.Visibility.NONE);
        }
        if (!this.isEnabled(Feature.AUTO_DETECT_IS_GETTERS)) {
            vchecker = (VisibilityChecker<?>)vchecker.withIsGetterVisibility(JsonAutoDetect.Visibility.NONE);
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
    
    public Class<?> getSerializationView() {
        return this._serializationView;
    }
    
    public JsonSerialize.Inclusion getSerializationInclusion() {
        if (this._serializationInclusion != null) {
            return this._serializationInclusion;
        }
        return this.isEnabled(Feature.WRITE_NULL_PROPERTIES) ? JsonSerialize.Inclusion.ALWAYS : JsonSerialize.Inclusion.NON_NULL;
    }
    
    @Deprecated
    public void setSerializationInclusion(final JsonSerialize.Inclusion props) {
        this._serializationInclusion = props;
        if (props == JsonSerialize.Inclusion.NON_NULL) {
            this.disable(Feature.WRITE_NULL_PROPERTIES);
        }
        else {
            this.enable(Feature.WRITE_NULL_PROPERTIES);
        }
    }
    
    public FilterProvider getFilterProvider() {
        return this._filterProvider;
    }
    
    public <T extends BeanDescription> T introspect(final JavaType type) {
        return (T)this.getClassIntrospector().forSerialization(this, type, this);
    }
    
    public JsonSerializer<Object> serializerInstance(final Annotated annotated, final Class<? extends JsonSerializer<?>> serClass) {
        final HandlerInstantiator hi = this.getHandlerInstantiator();
        if (hi != null) {
            final JsonSerializer<?> ser = hi.serializerInstance(this, annotated, serClass);
            if (ser != null) {
                return (JsonSerializer<Object>)ser;
            }
        }
        return ClassUtil.createInstance(serClass, this.canOverrideAccessModifiers());
    }
    
    @Deprecated
    @Override
    public final void setDateFormat(final DateFormat df) {
        super.setDateFormat(df);
        this.set(Feature.WRITE_DATES_AS_TIMESTAMPS, df == null);
    }
    
    @Deprecated
    public void setSerializationView(final Class<?> view) {
        this._serializationView = view;
    }
    
    @Override
    public String toString() {
        return "[SerializationConfig: flags=0x" + Integer.toHexString(this._featureFlags) + "]";
    }
    
    public enum Feature implements ConfigFeature
    {
        USE_ANNOTATIONS(true), 
        AUTO_DETECT_GETTERS(true), 
        AUTO_DETECT_IS_GETTERS(true), 
        AUTO_DETECT_FIELDS(true), 
        CAN_OVERRIDE_ACCESS_MODIFIERS(true), 
        REQUIRE_SETTERS_FOR_GETTERS(false), 
        @Deprecated
        WRITE_NULL_PROPERTIES(true), 
        USE_STATIC_TYPING(false), 
        DEFAULT_VIEW_INCLUSION(true), 
        WRAP_ROOT_VALUE(false), 
        INDENT_OUTPUT(false), 
        SORT_PROPERTIES_ALPHABETICALLY(false), 
        FAIL_ON_EMPTY_BEANS(true), 
        WRAP_EXCEPTIONS(true), 
        CLOSE_CLOSEABLE(false), 
        FLUSH_AFTER_WRITE_VALUE(true), 
        WRITE_DATES_AS_TIMESTAMPS(true), 
        WRITE_DATE_KEYS_AS_TIMESTAMPS(false), 
        WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS(false), 
        WRITE_ENUMS_USING_TO_STRING(false), 
        WRITE_ENUMS_USING_INDEX(false), 
        WRITE_NULL_MAP_VALUES(true), 
        WRITE_EMPTY_JSON_ARRAYS(true);
        
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
