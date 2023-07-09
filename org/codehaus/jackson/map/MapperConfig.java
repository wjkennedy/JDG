// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.map.util.StdDateFormat;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.type.TypeReference;
import java.lang.reflect.Type;
import org.codehaus.jackson.map.type.TypeBindings;
import org.codehaus.jackson.map.jsontype.impl.StdSubtypeResolver;
import org.codehaus.jackson.type.JavaType;
import java.util.Iterator;
import java.util.Map;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.map.introspect.VisibilityChecker;
import org.codehaus.jackson.map.jsontype.SubtypeResolver;
import org.codehaus.jackson.map.type.ClassKey;
import java.util.HashMap;
import java.text.DateFormat;

public abstract class MapperConfig<T extends MapperConfig<T>> implements ClassIntrospector.MixInResolver
{
    protected static final DateFormat DEFAULT_DATE_FORMAT;
    protected Base _base;
    protected HashMap<ClassKey, Class<?>> _mixInAnnotations;
    protected boolean _mixInAnnotationsShared;
    protected SubtypeResolver _subtypeResolver;
    
    protected MapperConfig(final ClassIntrospector<? extends BeanDescription> ci, final AnnotationIntrospector ai, final VisibilityChecker<?> vc, final SubtypeResolver str, final PropertyNamingStrategy pns, final TypeFactory tf, final HandlerInstantiator hi) {
        this._base = new Base(ci, ai, vc, pns, tf, null, MapperConfig.DEFAULT_DATE_FORMAT, hi);
        this._subtypeResolver = str;
        this._mixInAnnotationsShared = true;
    }
    
    protected MapperConfig(final MapperConfig<T> src) {
        this(src, src._base, src._subtypeResolver);
    }
    
    protected MapperConfig(final MapperConfig<T> src, final Base base, final SubtypeResolver str) {
        this._base = base;
        this._subtypeResolver = str;
        this._mixInAnnotationsShared = true;
        this._mixInAnnotations = src._mixInAnnotations;
    }
    
    @Deprecated
    public abstract void fromAnnotations(final Class<?> p0);
    
    public abstract T createUnshared(final SubtypeResolver p0);
    
    public abstract T withClassIntrospector(final ClassIntrospector<? extends BeanDescription> p0);
    
    public abstract T withAnnotationIntrospector(final AnnotationIntrospector p0);
    
    public abstract T withVisibilityChecker(final VisibilityChecker<?> p0);
    
    public abstract T withVisibility(final JsonMethod p0, final JsonAutoDetect.Visibility p1);
    
    public abstract T withTypeResolverBuilder(final TypeResolverBuilder<?> p0);
    
    public abstract T withSubtypeResolver(final SubtypeResolver p0);
    
    public abstract T withPropertyNamingStrategy(final PropertyNamingStrategy p0);
    
    public abstract T withTypeFactory(final TypeFactory p0);
    
    public abstract T withDateFormat(final DateFormat p0);
    
    public abstract T withHandlerInstantiator(final HandlerInstantiator p0);
    
    public abstract T withInsertedAnnotationIntrospector(final AnnotationIntrospector p0);
    
    public abstract T withAppendedAnnotationIntrospector(final AnnotationIntrospector p0);
    
    public abstract boolean isEnabled(final ConfigFeature p0);
    
    public abstract boolean isAnnotationProcessingEnabled();
    
    public abstract boolean canOverrideAccessModifiers();
    
    public abstract boolean shouldSortPropertiesAlphabetically();
    
    public ClassIntrospector<? extends BeanDescription> getClassIntrospector() {
        return this._base.getClassIntrospector();
    }
    
    public AnnotationIntrospector getAnnotationIntrospector() {
        return this._base.getAnnotationIntrospector();
    }
    
    @Deprecated
    public final void insertAnnotationIntrospector(final AnnotationIntrospector introspector) {
        this._base = this._base.withAnnotationIntrospector(AnnotationIntrospector.Pair.create(introspector, this.getAnnotationIntrospector()));
    }
    
    @Deprecated
    public final void appendAnnotationIntrospector(final AnnotationIntrospector introspector) {
        this._base = this._base.withAnnotationIntrospector(AnnotationIntrospector.Pair.create(this.getAnnotationIntrospector(), introspector));
    }
    
    public VisibilityChecker<?> getDefaultVisibilityChecker() {
        return this._base.getVisibilityChecker();
    }
    
    public final PropertyNamingStrategy getPropertyNamingStrategy() {
        return this._base.getPropertyNamingStrategy();
    }
    
    public final HandlerInstantiator getHandlerInstantiator() {
        return this._base.getHandlerInstantiator();
    }
    
    public final void setMixInAnnotations(final Map<Class<?>, Class<?>> sourceMixins) {
        HashMap<ClassKey, Class<?>> mixins = null;
        if (sourceMixins != null && sourceMixins.size() > 0) {
            mixins = new HashMap<ClassKey, Class<?>>(sourceMixins.size());
            for (final Map.Entry<Class<?>, Class<?>> en : sourceMixins.entrySet()) {
                mixins.put(new ClassKey(en.getKey()), en.getValue());
            }
        }
        this._mixInAnnotationsShared = false;
        this._mixInAnnotations = mixins;
    }
    
    public final void addMixInAnnotations(final Class<?> target, final Class<?> mixinSource) {
        if (this._mixInAnnotations == null) {
            this._mixInAnnotationsShared = false;
            this._mixInAnnotations = new HashMap<ClassKey, Class<?>>();
        }
        else if (this._mixInAnnotationsShared) {
            this._mixInAnnotationsShared = false;
            this._mixInAnnotations = new HashMap<ClassKey, Class<?>>(this._mixInAnnotations);
        }
        this._mixInAnnotations.put(new ClassKey(target), mixinSource);
    }
    
    public final Class<?> findMixInClassFor(final Class<?> cls) {
        return (this._mixInAnnotations == null) ? null : this._mixInAnnotations.get(new ClassKey(cls));
    }
    
    public final int mixInCount() {
        return (this._mixInAnnotations == null) ? 0 : this._mixInAnnotations.size();
    }
    
    public final TypeResolverBuilder<?> getDefaultTyper(final JavaType baseType) {
        return this._base.getTypeResolverBuilder();
    }
    
    public final SubtypeResolver getSubtypeResolver() {
        if (this._subtypeResolver == null) {
            this._subtypeResolver = new StdSubtypeResolver();
        }
        return this._subtypeResolver;
    }
    
    public final TypeFactory getTypeFactory() {
        return this._base.getTypeFactory();
    }
    
    public final JavaType constructType(final Class<?> cls) {
        return this.getTypeFactory().constructType(cls, (TypeBindings)null);
    }
    
    public final JavaType constructType(final TypeReference<?> valueTypeRef) {
        return this.getTypeFactory().constructType(valueTypeRef.getType(), (TypeBindings)null);
    }
    
    public JavaType constructSpecializedType(final JavaType baseType, final Class<?> subclass) {
        return this.getTypeFactory().constructSpecializedType(baseType, subclass);
    }
    
    public final DateFormat getDateFormat() {
        return this._base.getDateFormat();
    }
    
    public <DESC extends BeanDescription> DESC introspectClassAnnotations(final Class<?> cls) {
        return this.introspectClassAnnotations(this.constructType(cls));
    }
    
    public abstract <DESC extends BeanDescription> DESC introspectClassAnnotations(final JavaType p0);
    
    public <DESC extends BeanDescription> DESC introspectDirectClassAnnotations(final Class<?> cls) {
        return this.introspectDirectClassAnnotations(this.constructType(cls));
    }
    
    public abstract <DESC extends BeanDescription> DESC introspectDirectClassAnnotations(final JavaType p0);
    
    public TypeResolverBuilder<?> typeResolverBuilderInstance(final Annotated annotated, final Class<? extends TypeResolverBuilder<?>> builderClass) {
        final HandlerInstantiator hi = this.getHandlerInstantiator();
        if (hi != null) {
            final TypeResolverBuilder<?> builder = hi.typeResolverBuilderInstance(this, annotated, builderClass);
            if (builder != null) {
                return builder;
            }
        }
        return ClassUtil.createInstance(builderClass, this.canOverrideAccessModifiers());
    }
    
    public TypeIdResolver typeIdResolverInstance(final Annotated annotated, final Class<? extends TypeIdResolver> resolverClass) {
        final HandlerInstantiator hi = this.getHandlerInstantiator();
        if (hi != null) {
            final TypeIdResolver builder = hi.typeIdResolverInstance(this, annotated, resolverClass);
            if (builder != null) {
                return builder;
            }
        }
        return ClassUtil.createInstance(resolverClass, this.canOverrideAccessModifiers());
    }
    
    @Deprecated
    public final void setAnnotationIntrospector(final AnnotationIntrospector ai) {
        this._base = this._base.withAnnotationIntrospector(ai);
    }
    
    @Deprecated
    public void setDateFormat(DateFormat df) {
        if (df == null) {
            df = MapperConfig.DEFAULT_DATE_FORMAT;
        }
        this._base = this._base.withDateFormat(df);
    }
    
    static {
        DEFAULT_DATE_FORMAT = StdDateFormat.instance;
    }
    
    public static class Base
    {
        protected final ClassIntrospector<? extends BeanDescription> _classIntrospector;
        protected final AnnotationIntrospector _annotationIntrospector;
        protected final VisibilityChecker<?> _visibilityChecker;
        protected final PropertyNamingStrategy _propertyNamingStrategy;
        protected final TypeFactory _typeFactory;
        protected final TypeResolverBuilder<?> _typeResolverBuilder;
        protected final DateFormat _dateFormat;
        protected final HandlerInstantiator _handlerInstantiator;
        
        public Base(final ClassIntrospector<? extends BeanDescription> ci, final AnnotationIntrospector ai, final VisibilityChecker<?> vc, final PropertyNamingStrategy pns, final TypeFactory tf, final TypeResolverBuilder<?> typer, final DateFormat dateFormat, final HandlerInstantiator hi) {
            this._classIntrospector = ci;
            this._annotationIntrospector = ai;
            this._visibilityChecker = vc;
            this._propertyNamingStrategy = pns;
            this._typeFactory = tf;
            this._typeResolverBuilder = typer;
            this._dateFormat = dateFormat;
            this._handlerInstantiator = hi;
        }
        
        public Base withClassIntrospector(final ClassIntrospector<? extends BeanDescription> ci) {
            return new Base(ci, this._annotationIntrospector, this._visibilityChecker, this._propertyNamingStrategy, this._typeFactory, this._typeResolverBuilder, this._dateFormat, this._handlerInstantiator);
        }
        
        public Base withAnnotationIntrospector(final AnnotationIntrospector ai) {
            return new Base(this._classIntrospector, ai, this._visibilityChecker, this._propertyNamingStrategy, this._typeFactory, this._typeResolverBuilder, this._dateFormat, this._handlerInstantiator);
        }
        
        public Base withInsertedAnnotationIntrospector(final AnnotationIntrospector ai) {
            return this.withAnnotationIntrospector(AnnotationIntrospector.Pair.create(ai, this._annotationIntrospector));
        }
        
        public Base withAppendedAnnotationIntrospector(final AnnotationIntrospector ai) {
            return this.withAnnotationIntrospector(AnnotationIntrospector.Pair.create(this._annotationIntrospector, ai));
        }
        
        public Base withVisibilityChecker(final VisibilityChecker<?> vc) {
            return new Base(this._classIntrospector, this._annotationIntrospector, vc, this._propertyNamingStrategy, this._typeFactory, this._typeResolverBuilder, this._dateFormat, this._handlerInstantiator);
        }
        
        public Base withVisibility(final JsonMethod forMethod, final JsonAutoDetect.Visibility visibility) {
            return new Base(this._classIntrospector, this._annotationIntrospector, (VisibilityChecker<?>)this._visibilityChecker.withVisibility(forMethod, visibility), this._propertyNamingStrategy, this._typeFactory, this._typeResolverBuilder, this._dateFormat, this._handlerInstantiator);
        }
        
        public Base withPropertyNamingStrategy(final PropertyNamingStrategy pns) {
            return new Base(this._classIntrospector, this._annotationIntrospector, this._visibilityChecker, pns, this._typeFactory, this._typeResolverBuilder, this._dateFormat, this._handlerInstantiator);
        }
        
        public Base withTypeFactory(final TypeFactory tf) {
            return new Base(this._classIntrospector, this._annotationIntrospector, this._visibilityChecker, this._propertyNamingStrategy, tf, this._typeResolverBuilder, this._dateFormat, this._handlerInstantiator);
        }
        
        public Base withTypeResolverBuilder(final TypeResolverBuilder<?> typer) {
            return new Base(this._classIntrospector, this._annotationIntrospector, this._visibilityChecker, this._propertyNamingStrategy, this._typeFactory, typer, this._dateFormat, this._handlerInstantiator);
        }
        
        public Base withDateFormat(final DateFormat df) {
            return new Base(this._classIntrospector, this._annotationIntrospector, this._visibilityChecker, this._propertyNamingStrategy, this._typeFactory, this._typeResolverBuilder, df, this._handlerInstantiator);
        }
        
        public Base withHandlerInstantiator(final HandlerInstantiator hi) {
            return new Base(this._classIntrospector, this._annotationIntrospector, this._visibilityChecker, this._propertyNamingStrategy, this._typeFactory, this._typeResolverBuilder, this._dateFormat, hi);
        }
        
        public ClassIntrospector<? extends BeanDescription> getClassIntrospector() {
            return this._classIntrospector;
        }
        
        public AnnotationIntrospector getAnnotationIntrospector() {
            return this._annotationIntrospector;
        }
        
        public VisibilityChecker<?> getVisibilityChecker() {
            return this._visibilityChecker;
        }
        
        public PropertyNamingStrategy getPropertyNamingStrategy() {
            return this._propertyNamingStrategy;
        }
        
        public TypeFactory getTypeFactory() {
            return this._typeFactory;
        }
        
        public TypeResolverBuilder<?> getTypeResolverBuilder() {
            return this._typeResolverBuilder;
        }
        
        public DateFormat getDateFormat() {
            return this._dateFormat;
        }
        
        public HandlerInstantiator getHandlerInstantiator() {
            return this._handlerInstantiator;
        }
    }
    
    abstract static class Impl<CFG extends ConfigFeature, T extends Impl<CFG, T>> extends MapperConfig<T>
    {
        protected int _featureFlags;
        
        protected Impl(final ClassIntrospector<? extends BeanDescription> ci, final AnnotationIntrospector ai, final VisibilityChecker<?> vc, final SubtypeResolver str, final PropertyNamingStrategy pns, final TypeFactory tf, final HandlerInstantiator hi, final int defaultFeatures) {
            super(ci, ai, vc, str, pns, tf, hi);
            this._featureFlags = defaultFeatures;
        }
        
        protected Impl(final Impl<CFG, T> src) {
            super((MapperConfig<MapperConfig>)src);
            this._featureFlags = src._featureFlags;
        }
        
        protected Impl(final Impl<CFG, T> src, final int features) {
            super((MapperConfig<MapperConfig>)src);
            this._featureFlags = features;
        }
        
        protected Impl(final Impl<CFG, T> src, final Base base, final SubtypeResolver str) {
            super((MapperConfig<MapperConfig>)src, base, str);
            this._featureFlags = src._featureFlags;
        }
        
        static <F extends Enum<F> & ConfigFeature> int collectFeatureDefaults(final Class<F> enumClass) {
            int flags = 0;
            for (final F value : enumClass.getEnumConstants()) {
                if (value.enabledByDefault()) {
                    flags |= value.getMask();
                }
            }
            return flags;
        }
        
        public abstract T with(final CFG... p0);
        
        public abstract T without(final CFG... p0);
        
        @Override
        public boolean isEnabled(final ConfigFeature f) {
            return (this._featureFlags & f.getMask()) != 0x0;
        }
        
        @Deprecated
        public void enable(final CFG f) {
            this._featureFlags |= f.getMask();
        }
        
        @Deprecated
        public void disable(final CFG f) {
            this._featureFlags &= ~f.getMask();
        }
        
        @Deprecated
        public void set(final CFG f, final boolean state) {
            if (state) {
                this.enable(f);
            }
            else {
                this.disable(f);
            }
        }
    }
    
    public interface ConfigFeature
    {
        boolean enabledByDefault();
        
        int getMask();
    }
}
