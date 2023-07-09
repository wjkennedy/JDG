// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.introspect;

import java.util.HashSet;
import org.codehaus.jackson.map.util.BeanUtil;
import java.util.TreeMap;
import java.util.Iterator;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;
import org.codehaus.jackson.map.BeanPropertyDefinition;
import java.util.List;
import java.util.Set;
import java.util.LinkedList;
import java.util.LinkedHashMap;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.MapperConfig;

public class POJOPropertiesCollector
{
    protected final MapperConfig<?> _config;
    protected final boolean _forSerialization;
    protected final JavaType _type;
    protected final AnnotatedClass _classDef;
    protected final VisibilityChecker<?> _visibilityChecker;
    protected final AnnotationIntrospector _annotationIntrospector;
    protected final LinkedHashMap<String, POJOPropertyBuilder> _properties;
    protected LinkedList<POJOPropertyBuilder> _creatorProperties;
    protected LinkedList<AnnotatedMethod> _anyGetters;
    protected LinkedList<AnnotatedMethod> _anySetters;
    protected LinkedList<AnnotatedMethod> _jsonValueGetters;
    protected Set<String> _ignoredPropertyNames;
    protected Set<String> _ignoredPropertyNamesForDeser;
    protected LinkedHashMap<Object, AnnotatedMember> _injectables;
    
    protected POJOPropertiesCollector(final MapperConfig<?> config, final boolean forSerialization, final JavaType type, final AnnotatedClass classDef) {
        this._properties = new LinkedHashMap<String, POJOPropertyBuilder>();
        this._creatorProperties = null;
        this._anyGetters = null;
        this._anySetters = null;
        this._jsonValueGetters = null;
        this._config = config;
        this._forSerialization = forSerialization;
        this._type = type;
        this._classDef = classDef;
        this._annotationIntrospector = (config.isAnnotationProcessingEnabled() ? this._config.getAnnotationIntrospector() : null);
        if (this._annotationIntrospector == null) {
            this._visibilityChecker = this._config.getDefaultVisibilityChecker();
        }
        else {
            this._visibilityChecker = this._annotationIntrospector.findAutoDetectVisibility(classDef, this._config.getDefaultVisibilityChecker());
        }
    }
    
    public MapperConfig<?> getConfig() {
        return this._config;
    }
    
    public JavaType getType() {
        return this._type;
    }
    
    public AnnotatedClass getClassDef() {
        return this._classDef;
    }
    
    public AnnotationIntrospector getAnnotationIntrospector() {
        return this._annotationIntrospector;
    }
    
    public List<BeanPropertyDefinition> getProperties() {
        return new ArrayList<BeanPropertyDefinition>(this._properties.values());
    }
    
    public Map<Object, AnnotatedMember> getInjectables() {
        return this._injectables;
    }
    
    public AnnotatedMethod getJsonValueMethod() {
        if (this._jsonValueGetters != null) {
            if (this._jsonValueGetters.size() > 1) {
                this.reportProblem("Multiple value properties defined (" + this._jsonValueGetters.get(0) + " vs " + this._jsonValueGetters.get(1) + ")");
            }
            return this._jsonValueGetters.get(0);
        }
        return null;
    }
    
    public AnnotatedMethod getAnyGetterMethod() {
        if (this._anyGetters != null) {
            if (this._anyGetters.size() > 1) {
                this.reportProblem("Multiple 'any-getters' defined (" + this._anyGetters.get(0) + " vs " + this._anyGetters.get(1) + ")");
            }
            return this._anyGetters.getFirst();
        }
        return null;
    }
    
    public AnnotatedMethod getAnySetterMethod() {
        if (this._anySetters != null) {
            if (this._anySetters.size() > 1) {
                this.reportProblem("Multiple 'any-setters' defined (" + this._anySetters.get(0) + " vs " + this._anySetters.get(1) + ")");
            }
            return this._anySetters.getFirst();
        }
        return null;
    }
    
    public Set<String> getIgnoredPropertyNames() {
        return this._ignoredPropertyNames;
    }
    
    public Set<String> getIgnoredPropertyNamesForDeser() {
        return this._ignoredPropertyNamesForDeser;
    }
    
    protected Map<String, POJOPropertyBuilder> getPropertyMap() {
        return this._properties;
    }
    
    public POJOPropertiesCollector collect() {
        this._properties.clear();
        this._addFields();
        this._addMethods();
        this._addCreators();
        this._addInjectables();
        this._removeUnwantedProperties();
        this._renameProperties();
        final PropertyNamingStrategy naming = this._config.getPropertyNamingStrategy();
        if (naming != null) {
            this._renameUsing(naming);
        }
        for (final POJOPropertyBuilder property : this._properties.values()) {
            property.trimByVisibility();
        }
        for (final POJOPropertyBuilder property : this._properties.values()) {
            property.mergeAnnotations(this._forSerialization);
        }
        this._sortProperties();
        return this;
    }
    
    protected void _sortProperties() {
        final AnnotationIntrospector intr = this._config.getAnnotationIntrospector();
        final Boolean alpha = intr.findSerializationSortAlphabetically(this._classDef);
        boolean sort;
        if (alpha == null) {
            sort = this._config.shouldSortPropertiesAlphabetically();
        }
        else {
            sort = alpha;
        }
        final String[] propertyOrder = intr.findSerializationPropertyOrder(this._classDef);
        if (!sort && this._creatorProperties == null && propertyOrder == null) {
            return;
        }
        final int size = this._properties.size();
        Map<String, POJOPropertyBuilder> all;
        if (sort) {
            all = new TreeMap<String, POJOPropertyBuilder>();
        }
        else {
            all = new LinkedHashMap<String, POJOPropertyBuilder>(size + size);
        }
        for (final POJOPropertyBuilder prop : this._properties.values()) {
            all.put(prop.getName(), prop);
        }
        final Map<String, POJOPropertyBuilder> ordered = new LinkedHashMap<String, POJOPropertyBuilder>(size + size);
        if (propertyOrder != null) {
            for (String name : propertyOrder) {
                POJOPropertyBuilder w = all.get(name);
                if (w == null) {
                    for (final POJOPropertyBuilder prop2 : this._properties.values()) {
                        if (name.equals(prop2.getInternalName())) {
                            w = prop2;
                            name = prop2.getName();
                            break;
                        }
                    }
                }
                if (w != null) {
                    ordered.put(name, w);
                }
            }
        }
        if (this._creatorProperties != null) {
            for (final POJOPropertyBuilder prop3 : this._creatorProperties) {
                ordered.put(prop3.getName(), prop3);
            }
        }
        ordered.putAll(all);
        this._properties.clear();
        this._properties.putAll((Map<?, ?>)ordered);
    }
    
    protected void _addFields() {
        final AnnotationIntrospector ai = this._annotationIntrospector;
        for (final AnnotatedField f : this._classDef.fields()) {
            final String implName = f.getName();
            String explName;
            if (ai == null) {
                explName = null;
            }
            else if (this._forSerialization) {
                explName = ai.findSerializablePropertyName(f);
            }
            else {
                explName = ai.findDeserializablePropertyName(f);
            }
            if ("".equals(explName)) {
                explName = implName;
            }
            boolean visible = explName != null;
            if (!visible) {
                visible = this._visibilityChecker.isFieldVisible(f);
            }
            final boolean ignored = ai != null && ai.hasIgnoreMarker(f);
            this._property(implName).addField(f, explName, visible, ignored);
        }
    }
    
    protected void _addCreators() {
        final AnnotationIntrospector ai = this._annotationIntrospector;
        if (ai == null) {
            return;
        }
        for (final AnnotatedConstructor ctor : this._classDef.getConstructors()) {
            if (this._creatorProperties == null) {
                this._creatorProperties = new LinkedList<POJOPropertyBuilder>();
            }
            for (int i = 0, len = ctor.getParameterCount(); i < len; ++i) {
                final AnnotatedParameter param = ctor.getParameter(i);
                final String name = ai.findPropertyNameForParam(param);
                if (name != null) {
                    final POJOPropertyBuilder prop = this._property(name);
                    prop.addCtor(param, name, true, false);
                    this._creatorProperties.add(prop);
                }
            }
        }
        for (final AnnotatedMethod factory : this._classDef.getStaticMethods()) {
            if (this._creatorProperties == null) {
                this._creatorProperties = new LinkedList<POJOPropertyBuilder>();
            }
            for (int i = 0, len = factory.getParameterCount(); i < len; ++i) {
                final AnnotatedParameter param = factory.getParameter(i);
                final String name = ai.findPropertyNameForParam(param);
                if (name != null) {
                    final POJOPropertyBuilder prop = this._property(name);
                    prop.addCtor(param, name, true, false);
                    this._creatorProperties.add(prop);
                }
            }
        }
    }
    
    protected void _addMethods() {
        final AnnotationIntrospector ai = this._annotationIntrospector;
        for (final AnnotatedMethod m : this._classDef.memberMethods()) {
            final int argCount = m.getParameterCount();
            if (argCount == 0) {
                if (ai != null) {
                    if (ai.hasAnyGetterAnnotation(m)) {
                        if (this._anyGetters == null) {
                            this._anyGetters = new LinkedList<AnnotatedMethod>();
                        }
                        this._anyGetters.add(m);
                        continue;
                    }
                    if (ai.hasAsValueAnnotation(m)) {
                        if (this._jsonValueGetters == null) {
                            this._jsonValueGetters = new LinkedList<AnnotatedMethod>();
                        }
                        this._jsonValueGetters.add(m);
                        continue;
                    }
                }
                String explName = (ai == null) ? null : ai.findGettablePropertyName(m);
                String implName;
                boolean visible;
                if (explName == null) {
                    implName = BeanUtil.okNameForRegularGetter(m, m.getName());
                    if (implName == null) {
                        implName = BeanUtil.okNameForIsGetter(m, m.getName());
                        if (implName == null) {
                            continue;
                        }
                        visible = this._visibilityChecker.isIsGetterVisible(m);
                    }
                    else {
                        visible = this._visibilityChecker.isGetterVisible(m);
                    }
                }
                else {
                    implName = BeanUtil.okNameForGetter(m);
                    if (implName == null) {
                        implName = m.getName();
                    }
                    if (explName.length() == 0) {
                        explName = implName;
                    }
                    visible = true;
                }
                final boolean ignore = ai != null && ai.hasIgnoreMarker(m);
                this._property(implName).addGetter(m, explName, visible, ignore);
            }
            else if (argCount == 1) {
                String explName = (ai == null) ? null : ai.findSettablePropertyName(m);
                String implName;
                boolean visible;
                if (explName == null) {
                    implName = BeanUtil.okNameForSetter(m);
                    if (implName == null) {
                        continue;
                    }
                    visible = this._visibilityChecker.isSetterVisible(m);
                }
                else {
                    implName = BeanUtil.okNameForSetter(m);
                    if (implName == null) {
                        implName = m.getName();
                    }
                    if (explName.length() == 0) {
                        explName = implName;
                    }
                    visible = true;
                }
                final boolean ignore = ai != null && ai.hasIgnoreMarker(m);
                this._property(implName).addSetter(m, explName, visible, ignore);
            }
            else {
                if (argCount != 2 || ai == null || !ai.hasAnySetterAnnotation(m)) {
                    continue;
                }
                if (this._anySetters == null) {
                    this._anySetters = new LinkedList<AnnotatedMethod>();
                }
                this._anySetters.add(m);
            }
        }
    }
    
    protected void _addInjectables() {
        final AnnotationIntrospector ai = this._annotationIntrospector;
        if (ai == null) {
            return;
        }
        for (final AnnotatedField f : this._classDef.fields()) {
            this._doAddInjectable(ai.findInjectableValueId(f), f);
        }
        for (final AnnotatedMethod m : this._classDef.memberMethods()) {
            if (m.getParameterCount() != 1) {
                continue;
            }
            this._doAddInjectable(ai.findInjectableValueId(m), m);
        }
    }
    
    protected void _doAddInjectable(final Object id, final AnnotatedMember m) {
        if (id == null) {
            return;
        }
        if (this._injectables == null) {
            this._injectables = new LinkedHashMap<Object, AnnotatedMember>();
        }
        final AnnotatedMember prev = this._injectables.put(id, m);
        if (prev != null) {
            final String type = (id == null) ? "[null]" : id.getClass().getName();
            throw new IllegalArgumentException("Duplicate injectable value with id '" + String.valueOf(id) + "' (of type " + type + ")");
        }
    }
    
    protected void _removeUnwantedProperties() {
        final Iterator<Map.Entry<String, POJOPropertyBuilder>> it = this._properties.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<String, POJOPropertyBuilder> entry = it.next();
            final POJOPropertyBuilder prop = entry.getValue();
            if (!prop.anyVisible()) {
                it.remove();
            }
            else {
                if (prop.anyIgnorals()) {
                    this._addIgnored(prop);
                    if (!prop.anyExplicitNames()) {
                        it.remove();
                        continue;
                    }
                    prop.removeIgnored();
                }
                prop.removeNonVisible();
            }
        }
    }
    
    private void _addIgnored(final POJOPropertyBuilder prop) {
        if (this._forSerialization) {
            return;
        }
        final String name = prop.getName();
        this._ignoredPropertyNames = this.addToSet(this._ignoredPropertyNames, name);
        if (prop.anyDeserializeIgnorals()) {
            this._ignoredPropertyNamesForDeser = this.addToSet(this._ignoredPropertyNamesForDeser, name);
        }
    }
    
    protected void _renameProperties() {
        final Iterator<Map.Entry<String, POJOPropertyBuilder>> it = this._properties.entrySet().iterator();
        LinkedList<POJOPropertyBuilder> renamed = null;
        while (it.hasNext()) {
            final Map.Entry<String, POJOPropertyBuilder> entry = it.next();
            POJOPropertyBuilder prop = entry.getValue();
            final String newName = prop.findNewName();
            if (newName != null) {
                if (renamed == null) {
                    renamed = new LinkedList<POJOPropertyBuilder>();
                }
                prop = prop.withName(newName);
                renamed.add(prop);
                it.remove();
            }
        }
        if (renamed != null) {
            for (final POJOPropertyBuilder prop : renamed) {
                final String name = prop.getName();
                final POJOPropertyBuilder old = this._properties.get(name);
                if (old == null) {
                    this._properties.put(name, prop);
                }
                else {
                    old.addAll(prop);
                }
            }
        }
    }
    
    protected void _renameUsing(final PropertyNamingStrategy naming) {
        final POJOPropertyBuilder[] props = this._properties.values().toArray(new POJOPropertyBuilder[this._properties.size()]);
        this._properties.clear();
        for (POJOPropertyBuilder prop : props) {
            String name = prop.getName();
            if (this._forSerialization) {
                if (prop.hasGetter()) {
                    name = naming.nameForGetterMethod(this._config, prop.getGetter(), name);
                }
                else if (prop.hasField()) {
                    name = naming.nameForField(this._config, prop.getField(), name);
                }
            }
            else if (prop.hasSetter()) {
                name = naming.nameForSetterMethod(this._config, prop.getSetter(), name);
            }
            else if (prop.hasConstructorParameter()) {
                name = naming.nameForConstructorParameter(this._config, prop.getConstructorParameter(), name);
            }
            else if (prop.hasField()) {
                name = naming.nameForField(this._config, prop.getField(), name);
            }
            else if (prop.hasGetter()) {
                name = naming.nameForGetterMethod(this._config, prop.getGetter(), name);
            }
            if (!name.equals(prop.getName())) {
                prop = prop.withName(name);
            }
            final POJOPropertyBuilder old = this._properties.get(name);
            if (old == null) {
                this._properties.put(name, prop);
            }
            else {
                old.addAll(prop);
            }
        }
    }
    
    protected void reportProblem(final String msg) {
        throw new IllegalArgumentException("Problem with definition of " + this._classDef + ": " + msg);
    }
    
    protected POJOPropertyBuilder _property(final String implName) {
        POJOPropertyBuilder prop = this._properties.get(implName);
        if (prop == null) {
            prop = new POJOPropertyBuilder(implName);
            this._properties.put(implName, prop);
        }
        return prop;
    }
    
    private Set<String> addToSet(Set<String> set, final String str) {
        if (set == null) {
            set = new HashSet<String>();
        }
        set.add(str);
        return set;
    }
}
