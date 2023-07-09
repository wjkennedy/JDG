// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.map.introspect.AnnotatedField;
import java.util.LinkedHashMap;
import java.util.Collection;
import org.codehaus.jackson.map.introspect.VisibilityChecker;
import java.util.Set;
import org.codehaus.jackson.map.introspect.AnnotatedConstructor;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import java.util.Map;
import java.util.List;
import org.codehaus.jackson.map.util.Annotations;
import java.lang.reflect.Type;
import org.codehaus.jackson.map.type.TypeBindings;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.type.JavaType;

public abstract class BeanDescription
{
    protected final JavaType _type;
    
    protected BeanDescription(final JavaType type) {
        this._type = type;
    }
    
    public JavaType getType() {
        return this._type;
    }
    
    public Class<?> getBeanClass() {
        return this._type.getRawClass();
    }
    
    public abstract AnnotatedClass getClassInfo();
    
    public abstract boolean hasKnownClassAnnotations();
    
    public abstract TypeBindings bindingsForBeanType();
    
    public abstract JavaType resolveType(final Type p0);
    
    public abstract Annotations getClassAnnotations();
    
    public abstract List<BeanPropertyDefinition> findProperties();
    
    public abstract Map<Object, AnnotatedMember> findInjectables();
    
    public abstract AnnotatedMethod findAnyGetter();
    
    public abstract AnnotatedMethod findAnySetter();
    
    public abstract AnnotatedMethod findJsonValueMethod();
    
    public abstract AnnotatedConstructor findDefaultConstructor();
    
    public abstract Set<String> getIgnoredPropertyNames();
    
    @Deprecated
    public abstract LinkedHashMap<String, AnnotatedMethod> findGetters(final VisibilityChecker<?> p0, final Collection<String> p1);
    
    @Deprecated
    public abstract LinkedHashMap<String, AnnotatedMethod> findSetters(final VisibilityChecker<?> p0);
    
    @Deprecated
    public abstract LinkedHashMap<String, AnnotatedField> findDeserializableFields(final VisibilityChecker<?> p0, final Collection<String> p1);
    
    @Deprecated
    public abstract Map<String, AnnotatedField> findSerializableFields(final VisibilityChecker<?> p0, final Collection<String> p1);
}
