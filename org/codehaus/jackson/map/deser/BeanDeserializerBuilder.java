// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser;

import org.codehaus.jackson.map.BeanDescription;
import org.codehaus.jackson.map.deser.impl.BeanPropertyMap;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.BeanProperty;
import java.util.Iterator;
import org.codehaus.jackson.map.BeanPropertyDefinition;
import java.util.ArrayList;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.util.Annotations;
import org.codehaus.jackson.type.JavaType;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashSet;
import org.codehaus.jackson.map.deser.impl.ValueInjector;
import java.util.List;
import java.util.HashMap;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;

public class BeanDeserializerBuilder
{
    protected final BasicBeanDescription _beanDesc;
    protected final HashMap<String, SettableBeanProperty> _properties;
    protected List<ValueInjector> _injectables;
    protected HashMap<String, SettableBeanProperty> _backRefProperties;
    protected HashSet<String> _ignorableProps;
    protected ValueInstantiator _valueInstantiator;
    protected SettableAnyProperty _anySetter;
    protected boolean _ignoreAllUnknown;
    
    public BeanDeserializerBuilder(final BasicBeanDescription beanDesc) {
        this._properties = new LinkedHashMap<String, SettableBeanProperty>();
        this._beanDesc = beanDesc;
    }
    
    protected BeanDeserializerBuilder(final BeanDeserializerBuilder src) {
        this._properties = new LinkedHashMap<String, SettableBeanProperty>();
        this._beanDesc = src._beanDesc;
        this._anySetter = src._anySetter;
        this._ignoreAllUnknown = src._ignoreAllUnknown;
        this._properties.putAll(src._properties);
        this._backRefProperties = _copy(src._backRefProperties);
        this._ignorableProps = src._ignorableProps;
        this._valueInstantiator = src._valueInstantiator;
    }
    
    private static HashMap<String, SettableBeanProperty> _copy(final HashMap<String, SettableBeanProperty> src) {
        if (src == null) {
            return null;
        }
        return new HashMap<String, SettableBeanProperty>(src);
    }
    
    public void addOrReplaceProperty(final SettableBeanProperty prop, final boolean allowOverride) {
        this._properties.put(prop.getName(), prop);
    }
    
    public void addProperty(final SettableBeanProperty prop) {
        final SettableBeanProperty old = this._properties.put(prop.getName(), prop);
        if (old != null && old != prop) {
            throw new IllegalArgumentException("Duplicate property '" + prop.getName() + "' for " + this._beanDesc.getType());
        }
    }
    
    public void addBackReferenceProperty(final String referenceName, final SettableBeanProperty prop) {
        if (this._backRefProperties == null) {
            this._backRefProperties = new HashMap<String, SettableBeanProperty>(4);
        }
        this._backRefProperties.put(referenceName, prop);
        if (this._properties != null) {
            this._properties.remove(prop.getName());
        }
    }
    
    public void addInjectable(final String propertyName, final JavaType propertyType, final Annotations contextAnnotations, final AnnotatedMember member, final Object valueId) {
        if (this._injectables == null) {
            this._injectables = new ArrayList<ValueInjector>();
        }
        this._injectables.add(new ValueInjector(propertyName, propertyType, contextAnnotations, member, valueId));
    }
    
    public void addIgnorable(final String propName) {
        if (this._ignorableProps == null) {
            this._ignorableProps = new HashSet<String>();
        }
        this._ignorableProps.add(propName);
    }
    
    public void addCreatorProperty(final BeanPropertyDefinition propDef) {
    }
    
    public void setAnySetter(final SettableAnyProperty s) {
        if (this._anySetter != null && s != null) {
            throw new IllegalStateException("_anySetter already set to non-null");
        }
        this._anySetter = s;
    }
    
    public void setIgnoreUnknownProperties(final boolean ignore) {
        this._ignoreAllUnknown = ignore;
    }
    
    public void setValueInstantiator(final ValueInstantiator inst) {
        this._valueInstantiator = inst;
    }
    
    public Iterator<SettableBeanProperty> getProperties() {
        return this._properties.values().iterator();
    }
    
    public boolean hasProperty(final String propertyName) {
        return this._properties.containsKey(propertyName);
    }
    
    public SettableBeanProperty removeProperty(final String name) {
        return this._properties.remove(name);
    }
    
    public ValueInstantiator getValueInstantiator() {
        return this._valueInstantiator;
    }
    
    public JsonDeserializer<?> build(final BeanProperty forProperty) {
        final BeanPropertyMap propertyMap = new BeanPropertyMap(this._properties.values());
        propertyMap.assignIndexes();
        return new BeanDeserializer(this._beanDesc, forProperty, this._valueInstantiator, propertyMap, this._backRefProperties, this._ignorableProps, this._ignoreAllUnknown, this._anySetter, this._injectables);
    }
}
