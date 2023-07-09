// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.Collection;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.codehaus.jackson.map.util.Comparators;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.introspect.AnnotatedField;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.util.Annotations;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.SerializationConfig;

public class PropertyBuilder
{
    protected final SerializationConfig _config;
    protected final BasicBeanDescription _beanDesc;
    protected final JsonSerialize.Inclusion _outputProps;
    protected final AnnotationIntrospector _annotationIntrospector;
    protected Object _defaultBean;
    
    public PropertyBuilder(final SerializationConfig config, final BasicBeanDescription beanDesc) {
        this._config = config;
        this._beanDesc = beanDesc;
        this._outputProps = beanDesc.findSerializationInclusion(config.getSerializationInclusion());
        this._annotationIntrospector = this._config.getAnnotationIntrospector();
    }
    
    public Annotations getClassAnnotations() {
        return this._beanDesc.getClassAnnotations();
    }
    
    protected BeanPropertyWriter buildWriter(final String name, final JavaType declaredType, final JsonSerializer<Object> ser, final TypeSerializer typeSer, final TypeSerializer contentTypeSer, final AnnotatedMember am, final boolean defaultUseStaticTyping) {
        Method m;
        Field f;
        if (am instanceof AnnotatedField) {
            m = null;
            f = ((AnnotatedField)am).getAnnotated();
        }
        else {
            m = ((AnnotatedMethod)am).getAnnotated();
            f = null;
        }
        JavaType serializationType = this.findSerializationType(am, defaultUseStaticTyping, declaredType);
        if (contentTypeSer != null) {
            if (serializationType == null) {
                serializationType = declaredType;
            }
            JavaType ct = serializationType.getContentType();
            if (ct == null) {
                throw new IllegalStateException("Problem trying to create BeanPropertyWriter for property '" + name + "' (of type " + this._beanDesc.getType() + "); serialization type " + serializationType + " has no content");
            }
            serializationType = serializationType.withContentTypeHandler(contentTypeSer);
            ct = serializationType.getContentType();
        }
        Object valueToSuppress = null;
        boolean suppressNulls = false;
        final JsonSerialize.Inclusion methodProps = this._annotationIntrospector.findSerializationInclusion(am, this._outputProps);
        if (methodProps != null) {
            switch (methodProps) {
                case NON_DEFAULT: {
                    valueToSuppress = this.getDefaultValue(name, m, f);
                    if (valueToSuppress == null) {
                        suppressNulls = true;
                        break;
                    }
                    if (valueToSuppress.getClass().isArray()) {
                        valueToSuppress = Comparators.getArrayComparator(valueToSuppress);
                        break;
                    }
                    break;
                }
                case NON_EMPTY: {
                    suppressNulls = true;
                    valueToSuppress = this.getEmptyValueChecker(name, declaredType);
                    break;
                }
                case NON_NULL: {
                    suppressNulls = true;
                }
                case ALWAYS: {
                    if (declaredType.isContainerType()) {
                        valueToSuppress = this.getContainerValueChecker(name, declaredType);
                        break;
                    }
                    break;
                }
            }
        }
        BeanPropertyWriter bpw = new BeanPropertyWriter(am, this._beanDesc.getClassAnnotations(), name, declaredType, ser, typeSer, serializationType, m, f, suppressNulls, valueToSuppress);
        final Boolean unwrapped = this._annotationIntrospector.shouldUnwrapProperty(am);
        if (unwrapped != null && unwrapped) {
            bpw = bpw.unwrappingWriter();
        }
        return bpw;
    }
    
    protected JavaType findSerializationType(final Annotated a, boolean useStaticTyping, JavaType declaredType) {
        final Class<?> serClass = this._annotationIntrospector.findSerializationType(a);
        if (serClass != null) {
            final Class<?> rawDeclared = declaredType.getRawClass();
            if (serClass.isAssignableFrom(rawDeclared)) {
                declaredType = declaredType.widenBy(serClass);
            }
            else {
                if (!rawDeclared.isAssignableFrom(serClass)) {
                    throw new IllegalArgumentException("Illegal concrete-type annotation for method '" + a.getName() + "': class " + serClass.getName() + " not a super-type of (declared) class " + rawDeclared.getName());
                }
                declaredType = this._config.constructSpecializedType(declaredType, serClass);
            }
            useStaticTyping = true;
        }
        final JavaType secondary = BasicSerializerFactory.modifySecondaryTypesByAnnotation(this._config, a, declaredType);
        if (secondary != declaredType) {
            useStaticTyping = true;
            declaredType = secondary;
        }
        if (!useStaticTyping) {
            final JsonSerialize.Typing typing = this._annotationIntrospector.findSerializationTyping(a);
            if (typing != null) {
                useStaticTyping = (typing == JsonSerialize.Typing.STATIC);
            }
        }
        return useStaticTyping ? declaredType : null;
    }
    
    protected Object getDefaultBean() {
        if (this._defaultBean == null) {
            this._defaultBean = this._beanDesc.instantiateBean(this._config.isEnabled(SerializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS));
            if (this._defaultBean == null) {
                final Class<?> cls = this._beanDesc.getClassInfo().getAnnotated();
                throw new IllegalArgumentException("Class " + cls.getName() + " has no default constructor; can not instantiate default bean value to support 'properties=JsonSerialize.Inclusion.NON_DEFAULT' annotation");
            }
        }
        return this._defaultBean;
    }
    
    protected Object getDefaultValue(final String name, final Method m, final Field f) {
        final Object defaultBean = this.getDefaultBean();
        try {
            if (m != null) {
                return m.invoke(defaultBean, new Object[0]);
            }
            return f.get(defaultBean);
        }
        catch (final Exception e) {
            return this._throwWrapped(e, name, defaultBean);
        }
    }
    
    protected Object getContainerValueChecker(final String propertyName, final JavaType propertyType) {
        if (!this._config.isEnabled(SerializationConfig.Feature.WRITE_EMPTY_JSON_ARRAYS)) {
            if (propertyType.isArrayType()) {
                return new EmptyArrayChecker();
            }
            if (Collection.class.isAssignableFrom(propertyType.getRawClass())) {
                return new EmptyCollectionChecker();
            }
        }
        return null;
    }
    
    protected Object getEmptyValueChecker(final String propertyName, final JavaType propertyType) {
        final Class<?> rawType = propertyType.getRawClass();
        if (rawType == String.class) {
            return new EmptyStringChecker();
        }
        if (propertyType.isArrayType()) {
            return new EmptyArrayChecker();
        }
        if (Collection.class.isAssignableFrom(rawType)) {
            return new EmptyCollectionChecker();
        }
        if (Map.class.isAssignableFrom(rawType)) {
            return new EmptyMapChecker();
        }
        return null;
    }
    
    protected Object _throwWrapped(final Exception e, final String propName, final Object defaultBean) {
        Throwable t;
        for (t = e; t.getCause() != null; t = t.getCause()) {}
        if (t instanceof Error) {
            throw (Error)t;
        }
        if (t instanceof RuntimeException) {
            throw (RuntimeException)t;
        }
        throw new IllegalArgumentException("Failed to get property '" + propName + "' of default " + defaultBean.getClass().getName() + " instance");
    }
    
    public static class EmptyCollectionChecker
    {
        @Override
        public boolean equals(final Object other) {
            return other == null || ((Collection)other).size() == 0;
        }
    }
    
    public static class EmptyMapChecker
    {
        @Override
        public boolean equals(final Object other) {
            return other == null || ((Map)other).size() == 0;
        }
    }
    
    public static class EmptyArrayChecker
    {
        @Override
        public boolean equals(final Object other) {
            return other == null || Array.getLength(other) == 0;
        }
    }
    
    public static class EmptyStringChecker
    {
        @Override
        public boolean equals(final Object other) {
            return other == null || ((String)other).length() == 0;
        }
    }
}
