// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.impl;

import java.io.IOException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.JsonDeserializer;
import java.util.Collection;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.map.deser.SettableBeanProperty;
import java.util.HashMap;
import org.codehaus.jackson.map.deser.ValueInstantiator;

public final class PropertyBasedCreator
{
    protected final ValueInstantiator _valueInstantiator;
    protected final HashMap<String, SettableBeanProperty> _properties;
    protected final int _propertyCount;
    protected Object[] _defaultValues;
    protected final SettableBeanProperty[] _propertiesWithInjectables;
    
    public PropertyBasedCreator(final ValueInstantiator valueInstantiator) {
        this._valueInstantiator = valueInstantiator;
        this._properties = new HashMap<String, SettableBeanProperty>();
        Object[] defValues = null;
        final SettableBeanProperty[] creatorProps = valueInstantiator.getFromObjectArguments();
        SettableBeanProperty[] propertiesWithInjectables = null;
        final int len = creatorProps.length;
        this._propertyCount = len;
        for (int i = 0; i < len; ++i) {
            final SettableBeanProperty prop = creatorProps[i];
            this._properties.put(prop.getName(), prop);
            if (prop.getType().isPrimitive()) {
                if (defValues == null) {
                    defValues = new Object[len];
                }
                defValues[i] = ClassUtil.defaultValue(prop.getType().getRawClass());
            }
            final Object injectableValueId = prop.getInjectableValueId();
            if (injectableValueId != null) {
                if (propertiesWithInjectables == null) {
                    propertiesWithInjectables = new SettableBeanProperty[len];
                }
                propertiesWithInjectables[i] = prop;
            }
        }
        this._defaultValues = defValues;
        this._propertiesWithInjectables = propertiesWithInjectables;
    }
    
    public Collection<SettableBeanProperty> getCreatorProperties() {
        return this._properties.values();
    }
    
    public SettableBeanProperty findCreatorProperty(final String name) {
        return this._properties.get(name);
    }
    
    public void assignDeserializer(SettableBeanProperty prop, final JsonDeserializer<Object> deser) {
        prop = prop.withValueDeserializer(deser);
        this._properties.put(prop.getName(), prop);
        final Object nullValue = deser.getNullValue();
        if (nullValue != null) {
            if (this._defaultValues == null) {
                this._defaultValues = new Object[this._properties.size()];
            }
            this._defaultValues[prop.getPropertyIndex()] = nullValue;
        }
    }
    
    public PropertyValueBuffer startBuilding(final JsonParser jp, final DeserializationContext ctxt) {
        final PropertyValueBuffer buffer = new PropertyValueBuffer(jp, ctxt, this._propertyCount);
        if (this._propertiesWithInjectables != null) {
            buffer.inject(this._propertiesWithInjectables);
        }
        return buffer;
    }
    
    public Object build(final PropertyValueBuffer buffer) throws IOException {
        final Object bean = this._valueInstantiator.createFromObjectWith(buffer.getParameters(this._defaultValues));
        for (PropertyValue pv = buffer.buffered(); pv != null; pv = pv.next) {
            pv.assign(bean);
        }
        return bean;
    }
}
