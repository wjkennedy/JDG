// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.introspect;

import java.util.Map;
import java.util.Collection;
import org.codehaus.jackson.map.util.ClassUtil;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import org.codehaus.jackson.map.type.SimpleType;
import org.codehaus.jackson.map.BeanDescription;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.ClassIntrospector;

public class BasicClassIntrospector extends ClassIntrospector<BasicBeanDescription>
{
    protected static final BasicBeanDescription STRING_DESC;
    protected static final BasicBeanDescription BOOLEAN_DESC;
    protected static final BasicBeanDescription INT_DESC;
    protected static final BasicBeanDescription LONG_DESC;
    @Deprecated
    public static final GetterMethodFilter DEFAULT_GETTER_FILTER;
    @Deprecated
    public static final SetterMethodFilter DEFAULT_SETTER_FILTER;
    @Deprecated
    public static final SetterAndGetterMethodFilter DEFAULT_SETTER_AND_GETTER_FILTER;
    protected static final MethodFilter MINIMAL_FILTER;
    public static final BasicClassIntrospector instance;
    
    @Override
    public BasicBeanDescription forSerialization(final SerializationConfig cfg, final JavaType type, final MixInResolver r) {
        BasicBeanDescription desc = this._findCachedDesc(type);
        if (desc == null) {
            desc = BasicBeanDescription.forSerialization(this.collectProperties(cfg, type, r, true));
        }
        return desc;
    }
    
    @Override
    public BasicBeanDescription forDeserialization(final DeserializationConfig cfg, final JavaType type, final MixInResolver r) {
        BasicBeanDescription desc = this._findCachedDesc(type);
        if (desc == null) {
            desc = BasicBeanDescription.forDeserialization(this.collectProperties(cfg, type, r, false));
        }
        return desc;
    }
    
    @Override
    public BasicBeanDescription forCreation(final DeserializationConfig cfg, final JavaType type, final MixInResolver r) {
        BasicBeanDescription desc = this._findCachedDesc(type);
        if (desc == null) {
            desc = BasicBeanDescription.forDeserialization(this.collectProperties(cfg, type, r, false));
        }
        return desc;
    }
    
    @Override
    public BasicBeanDescription forClassAnnotations(final MapperConfig<?> cfg, final JavaType type, final MixInResolver r) {
        final boolean useAnnotations = cfg.isAnnotationProcessingEnabled();
        final AnnotationIntrospector ai = cfg.getAnnotationIntrospector();
        final AnnotatedClass ac = AnnotatedClass.construct(type.getRawClass(), useAnnotations ? ai : null, r);
        return BasicBeanDescription.forOtherUse(cfg, type, ac);
    }
    
    @Override
    public BasicBeanDescription forDirectClassAnnotations(final MapperConfig<?> cfg, final JavaType type, final MixInResolver r) {
        final boolean useAnnotations = cfg.isAnnotationProcessingEnabled();
        final AnnotationIntrospector ai = cfg.getAnnotationIntrospector();
        final AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(type.getRawClass(), useAnnotations ? ai : null, r);
        return BasicBeanDescription.forOtherUse(cfg, type, ac);
    }
    
    public POJOPropertiesCollector collectProperties(final MapperConfig<?> config, final JavaType type, final MixInResolver r, final boolean forSerialization) {
        final AnnotatedClass ac = this.classWithCreators(config, type, r);
        ac.resolveMemberMethods(BasicClassIntrospector.MINIMAL_FILTER);
        ac.resolveFields();
        return this.constructPropertyCollector(config, ac, type, forSerialization).collect();
    }
    
    protected POJOPropertiesCollector constructPropertyCollector(final MapperConfig<?> config, final AnnotatedClass ac, final JavaType type, final boolean forSerialization) {
        return new POJOPropertiesCollector(config, forSerialization, type, ac);
    }
    
    public AnnotatedClass classWithCreators(final MapperConfig<?> config, final JavaType type, final MixInResolver r) {
        final boolean useAnnotations = config.isAnnotationProcessingEnabled();
        final AnnotationIntrospector ai = config.getAnnotationIntrospector();
        final AnnotatedClass ac = AnnotatedClass.construct(type.getRawClass(), useAnnotations ? ai : null, r);
        ac.resolveMemberMethods(BasicClassIntrospector.MINIMAL_FILTER);
        ac.resolveCreators(true);
        return ac;
    }
    
    protected BasicBeanDescription _findCachedDesc(final JavaType type) {
        final Class<?> cls = type.getRawClass();
        if (cls == String.class) {
            return BasicClassIntrospector.STRING_DESC;
        }
        if (cls == Boolean.TYPE) {
            return BasicClassIntrospector.BOOLEAN_DESC;
        }
        if (cls == Integer.TYPE) {
            return BasicClassIntrospector.INT_DESC;
        }
        if (cls == Long.TYPE) {
            return BasicClassIntrospector.LONG_DESC;
        }
        return null;
    }
    
    @Deprecated
    protected MethodFilter getSerializationMethodFilter(final SerializationConfig cfg) {
        return BasicClassIntrospector.DEFAULT_GETTER_FILTER;
    }
    
    @Deprecated
    protected MethodFilter getDeserializationMethodFilter(final DeserializationConfig cfg) {
        if (cfg.isEnabled(DeserializationConfig.Feature.USE_GETTERS_AS_SETTERS)) {
            return BasicClassIntrospector.DEFAULT_SETTER_AND_GETTER_FILTER;
        }
        return BasicClassIntrospector.DEFAULT_SETTER_FILTER;
    }
    
    static {
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(String.class, null, null);
        STRING_DESC = BasicBeanDescription.forOtherUse(null, SimpleType.constructUnsafe(String.class), ac);
        ac = AnnotatedClass.constructWithoutSuperTypes(Boolean.TYPE, null, null);
        BOOLEAN_DESC = BasicBeanDescription.forOtherUse(null, SimpleType.constructUnsafe(Boolean.TYPE), ac);
        ac = AnnotatedClass.constructWithoutSuperTypes(Integer.TYPE, null, null);
        INT_DESC = BasicBeanDescription.forOtherUse(null, SimpleType.constructUnsafe(Integer.TYPE), ac);
        ac = AnnotatedClass.constructWithoutSuperTypes(Long.TYPE, null, null);
        LONG_DESC = BasicBeanDescription.forOtherUse(null, SimpleType.constructUnsafe(Long.TYPE), ac);
        DEFAULT_GETTER_FILTER = new GetterMethodFilter();
        DEFAULT_SETTER_FILTER = new SetterMethodFilter();
        DEFAULT_SETTER_AND_GETTER_FILTER = new SetterAndGetterMethodFilter();
        MINIMAL_FILTER = new MinimalMethodFilter();
        instance = new BasicClassIntrospector();
    }
    
    private static class MinimalMethodFilter implements MethodFilter
    {
        public boolean includeMethod(final Method m) {
            if (Modifier.isStatic(m.getModifiers())) {
                return false;
            }
            final int pcount = m.getParameterTypes().length;
            return pcount <= 2;
        }
    }
    
    @Deprecated
    public static class GetterMethodFilter implements MethodFilter
    {
        private GetterMethodFilter() {
        }
        
        public boolean includeMethod(final Method m) {
            return ClassUtil.hasGetterSignature(m);
        }
    }
    
    @Deprecated
    public static class SetterMethodFilter implements MethodFilter
    {
        public boolean includeMethod(final Method m) {
            if (Modifier.isStatic(m.getModifiers())) {
                return false;
            }
            final int pcount = m.getParameterTypes().length;
            switch (pcount) {
                case 1: {
                    return true;
                }
                case 2: {
                    return true;
                }
                default: {
                    return false;
                }
            }
        }
    }
    
    @Deprecated
    public static final class SetterAndGetterMethodFilter extends SetterMethodFilter
    {
        @Override
        public boolean includeMethod(final Method m) {
            if (super.includeMethod(m)) {
                return true;
            }
            if (!ClassUtil.hasGetterSignature(m)) {
                return false;
            }
            final Class<?> rt = m.getReturnType();
            return Collection.class.isAssignableFrom(rt) || Map.class.isAssignableFrom(rt);
        }
    }
}
