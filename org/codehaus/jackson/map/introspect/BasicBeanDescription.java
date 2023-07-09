// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.introspect;

import java.util.LinkedHashMap;
import java.util.Collection;
import java.util.HashMap;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.ArrayList;
import java.lang.reflect.Type;
import org.codehaus.jackson.map.util.Annotations;
import java.util.Collections;
import org.codehaus.jackson.type.JavaType;
import java.util.Set;
import java.util.Map;
import org.codehaus.jackson.map.BeanPropertyDefinition;
import java.util.List;
import org.codehaus.jackson.map.type.TypeBindings;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.BeanDescription;

public class BasicBeanDescription extends BeanDescription
{
    protected final MapperConfig<?> _config;
    protected final AnnotationIntrospector _annotationIntrospector;
    protected final AnnotatedClass _classInfo;
    protected TypeBindings _bindings;
    protected final List<BeanPropertyDefinition> _properties;
    protected AnnotatedMethod _anySetterMethod;
    protected Map<Object, AnnotatedMember> _injectables;
    protected Set<String> _ignoredPropertyNames;
    protected Set<String> _ignoredPropertyNamesForDeser;
    protected AnnotatedMethod _jsonValueMethod;
    protected AnnotatedMethod _anyGetterMethod;
    
    @Deprecated
    public BasicBeanDescription(final MapperConfig<?> config, final JavaType type, final AnnotatedClass ac) {
        this(config, type, ac, Collections.emptyList());
    }
    
    protected BasicBeanDescription(final MapperConfig<?> config, final JavaType type, final AnnotatedClass ac, final List<BeanPropertyDefinition> properties) {
        super(type);
        this._config = config;
        this._annotationIntrospector = ((config == null) ? null : config.getAnnotationIntrospector());
        this._classInfo = ac;
        this._properties = properties;
    }
    
    public static BasicBeanDescription forDeserialization(final POJOPropertiesCollector coll) {
        final BasicBeanDescription desc = new BasicBeanDescription(coll.getConfig(), coll.getType(), coll.getClassDef(), coll.getProperties());
        desc._anySetterMethod = coll.getAnySetterMethod();
        desc._ignoredPropertyNames = coll.getIgnoredPropertyNames();
        desc._ignoredPropertyNamesForDeser = coll.getIgnoredPropertyNamesForDeser();
        desc._injectables = coll.getInjectables();
        return desc;
    }
    
    public static BasicBeanDescription forSerialization(final POJOPropertiesCollector coll) {
        final BasicBeanDescription desc = new BasicBeanDescription(coll.getConfig(), coll.getType(), coll.getClassDef(), coll.getProperties());
        desc._jsonValueMethod = coll.getJsonValueMethod();
        desc._anyGetterMethod = coll.getAnyGetterMethod();
        return desc;
    }
    
    public static BasicBeanDescription forOtherUse(final MapperConfig<?> config, final JavaType type, final AnnotatedClass ac) {
        return new BasicBeanDescription(config, type, ac, Collections.emptyList());
    }
    
    @Override
    public AnnotatedClass getClassInfo() {
        return this._classInfo;
    }
    
    @Override
    public List<BeanPropertyDefinition> findProperties() {
        return this._properties;
    }
    
    @Override
    public AnnotatedMethod findJsonValueMethod() {
        return this._jsonValueMethod;
    }
    
    @Override
    public Set<String> getIgnoredPropertyNames() {
        if (this._ignoredPropertyNames == null) {
            return Collections.emptySet();
        }
        return this._ignoredPropertyNames;
    }
    
    public Set<String> getIgnoredPropertyNamesForDeser() {
        return this._ignoredPropertyNamesForDeser;
    }
    
    @Override
    public boolean hasKnownClassAnnotations() {
        return this._classInfo.hasAnnotations();
    }
    
    @Override
    public Annotations getClassAnnotations() {
        return this._classInfo.getAnnotations();
    }
    
    @Override
    public TypeBindings bindingsForBeanType() {
        if (this._bindings == null) {
            this._bindings = new TypeBindings(this._config.getTypeFactory(), this._type);
        }
        return this._bindings;
    }
    
    @Override
    public JavaType resolveType(final Type jdkType) {
        if (jdkType == null) {
            return null;
        }
        return this.bindingsForBeanType().resolveType(jdkType);
    }
    
    @Override
    public AnnotatedConstructor findDefaultConstructor() {
        return this._classInfo.getDefaultConstructor();
    }
    
    @Override
    public AnnotatedMethod findAnySetter() throws IllegalArgumentException {
        if (this._anySetterMethod != null) {
            final Class<?> type = this._anySetterMethod.getParameterClass(0);
            if (type != String.class && type != Object.class) {
                throw new IllegalArgumentException("Invalid 'any-setter' annotation on method " + this._anySetterMethod.getName() + "(): first argument not of type String or Object, but " + type.getName());
            }
        }
        return this._anySetterMethod;
    }
    
    @Override
    public Map<Object, AnnotatedMember> findInjectables() {
        return this._injectables;
    }
    
    public List<AnnotatedConstructor> getConstructors() {
        return this._classInfo.getConstructors();
    }
    
    public AnnotatedMethod findMethod(final String name, final Class<?>[] paramTypes) {
        return this._classInfo.findMethod(name, paramTypes);
    }
    
    public Object instantiateBean(final boolean fixAccess) {
        final AnnotatedConstructor ac = this._classInfo.getDefaultConstructor();
        if (ac == null) {
            return null;
        }
        if (fixAccess) {
            ac.fixAccess();
        }
        try {
            return ac.getAnnotated().newInstance(new Object[0]);
        }
        catch (final Exception e) {
            Throwable t;
            for (t = e; t.getCause() != null; t = t.getCause()) {}
            if (t instanceof Error) {
                throw (Error)t;
            }
            if (t instanceof RuntimeException) {
                throw (RuntimeException)t;
            }
            throw new IllegalArgumentException("Failed to instantiate bean of type " + this._classInfo.getAnnotated().getName() + ": (" + t.getClass().getName() + ") " + t.getMessage(), t);
        }
    }
    
    public List<AnnotatedMethod> getFactoryMethods() {
        final List<AnnotatedMethod> candidates = this._classInfo.getStaticMethods();
        if (candidates.isEmpty()) {
            return candidates;
        }
        final ArrayList<AnnotatedMethod> result = new ArrayList<AnnotatedMethod>();
        for (final AnnotatedMethod am : candidates) {
            if (this.isFactoryMethod(am)) {
                result.add(am);
            }
        }
        return result;
    }
    
    public Constructor<?> findSingleArgConstructor(final Class<?>... argTypes) {
        for (final AnnotatedConstructor ac : this._classInfo.getConstructors()) {
            if (ac.getParameterCount() == 1) {
                final Class<?> actArg = ac.getParameterClass(0);
                for (final Class<?> expArg : argTypes) {
                    if (expArg == actArg) {
                        return ac.getAnnotated();
                    }
                }
            }
        }
        return null;
    }
    
    public Method findFactoryMethod(final Class<?>... expArgTypes) {
        for (final AnnotatedMethod am : this._classInfo.getStaticMethods()) {
            if (this.isFactoryMethod(am)) {
                final Class<?> actualArgType = am.getParameterClass(0);
                for (final Class<?> expArgType : expArgTypes) {
                    if (actualArgType.isAssignableFrom(expArgType)) {
                        return am.getAnnotated();
                    }
                }
            }
        }
        return null;
    }
    
    protected boolean isFactoryMethod(final AnnotatedMethod am) {
        final Class<?> rt = am.getRawType();
        return this.getBeanClass().isAssignableFrom(rt) && (this._annotationIntrospector.hasCreatorAnnotation(am) || "valueOf".equals(am.getName()));
    }
    
    public List<String> findCreatorPropertyNames() {
        List<String> names = null;
        for (int i = 0; i < 2; ++i) {
            final List<? extends AnnotatedWithParams> l = (List<? extends AnnotatedWithParams>)((i == 0) ? this.getConstructors() : this.getFactoryMethods());
            for (final AnnotatedWithParams creator : l) {
                final int argCount = creator.getParameterCount();
                if (argCount < 1) {
                    continue;
                }
                final String name = this._annotationIntrospector.findPropertyNameForParam(creator.getParameter(0));
                if (name == null) {
                    continue;
                }
                if (names == null) {
                    names = new ArrayList<String>();
                }
                names.add(name);
                for (int p = 1; p < argCount; ++p) {
                    names.add(this._annotationIntrospector.findPropertyNameForParam(creator.getParameter(p)));
                }
            }
        }
        if (names == null) {
            return Collections.emptyList();
        }
        return names;
    }
    
    public JsonSerialize.Inclusion findSerializationInclusion(final JsonSerialize.Inclusion defValue) {
        if (this._annotationIntrospector == null) {
            return defValue;
        }
        return this._annotationIntrospector.findSerializationInclusion(this._classInfo, defValue);
    }
    
    @Override
    public AnnotatedMethod findAnyGetter() throws IllegalArgumentException {
        if (this._anyGetterMethod != null) {
            final Class<?> type = this._anyGetterMethod.getRawType();
            if (!Map.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException("Invalid 'any-getter' annotation on method " + this._anyGetterMethod.getName() + "(): return type is not instance of java.util.Map");
            }
        }
        return this._anyGetterMethod;
    }
    
    public Map<String, AnnotatedMember> findBackReferenceProperties() {
        HashMap<String, AnnotatedMember> result = null;
        for (final BeanPropertyDefinition property : this._properties) {
            final AnnotatedMember am = property.getMutator();
            if (am == null) {
                continue;
            }
            final AnnotationIntrospector.ReferenceProperty refDef = this._annotationIntrospector.findReferenceType(am);
            if (refDef == null || !refDef.isBackReference()) {
                continue;
            }
            if (result == null) {
                result = new HashMap<String, AnnotatedMember>();
            }
            final String refName = refDef.getName();
            if (result.put(refName, am) != null) {
                throw new IllegalArgumentException("Multiple back-reference properties with name '" + refName + "'");
            }
        }
        return result;
    }
    
    public LinkedHashMap<String, AnnotatedField> _findPropertyFields(final Collection<String> ignoredProperties, final boolean forSerialization) {
        final LinkedHashMap<String, AnnotatedField> results = new LinkedHashMap<String, AnnotatedField>();
        for (final BeanPropertyDefinition property : this._properties) {
            final AnnotatedField f = property.getField();
            if (f != null) {
                final String name = property.getName();
                if (ignoredProperties != null && ignoredProperties.contains(name)) {
                    continue;
                }
                results.put(name, f);
            }
        }
        return results;
    }
    
    @Override
    public LinkedHashMap<String, AnnotatedMethod> findGetters(final VisibilityChecker<?> visibilityChecker, final Collection<String> ignoredProperties) {
        final LinkedHashMap<String, AnnotatedMethod> results = new LinkedHashMap<String, AnnotatedMethod>();
        for (final BeanPropertyDefinition property : this._properties) {
            final AnnotatedMethod m = property.getGetter();
            if (m != null) {
                final String name = property.getName();
                if (ignoredProperties != null && ignoredProperties.contains(name)) {
                    continue;
                }
                results.put(name, m);
            }
        }
        return results;
    }
    
    @Override
    public LinkedHashMap<String, AnnotatedMethod> findSetters(final VisibilityChecker<?> visibilityChecker) {
        final LinkedHashMap<String, AnnotatedMethod> results = new LinkedHashMap<String, AnnotatedMethod>();
        for (final BeanPropertyDefinition property : this._properties) {
            final AnnotatedMethod m = property.getSetter();
            if (m != null) {
                results.put(property.getName(), m);
            }
        }
        return results;
    }
    
    @Override
    public LinkedHashMap<String, AnnotatedField> findSerializableFields(final VisibilityChecker<?> visibilityChecker, final Collection<String> ignoredProperties) {
        return this._findPropertyFields(ignoredProperties, true);
    }
    
    @Override
    public LinkedHashMap<String, AnnotatedField> findDeserializableFields(final VisibilityChecker<?> visibilityChecker, final Collection<String> ignoredProperties) {
        return this._findPropertyFields(ignoredProperties, false);
    }
}
