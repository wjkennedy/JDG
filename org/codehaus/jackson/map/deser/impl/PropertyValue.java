// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.impl;

import java.util.Map;
import org.codehaus.jackson.map.deser.SettableAnyProperty;
import org.codehaus.jackson.map.deser.SettableBeanProperty;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;

public abstract class PropertyValue
{
    public final PropertyValue next;
    public final Object value;
    
    protected PropertyValue(final PropertyValue next, final Object value) {
        this.next = next;
        this.value = value;
    }
    
    public abstract void assign(final Object p0) throws IOException, JsonProcessingException;
    
    static final class Regular extends PropertyValue
    {
        final SettableBeanProperty _property;
        
        public Regular(final PropertyValue next, final Object value, final SettableBeanProperty prop) {
            super(next, value);
            this._property = prop;
        }
        
        @Override
        public void assign(final Object bean) throws IOException, JsonProcessingException {
            this._property.set(bean, this.value);
        }
    }
    
    static final class Any extends PropertyValue
    {
        final SettableAnyProperty _property;
        final String _propertyName;
        
        public Any(final PropertyValue next, final Object value, final SettableAnyProperty prop, final String propName) {
            super(next, value);
            this._property = prop;
            this._propertyName = propName;
        }
        
        @Override
        public void assign(final Object bean) throws IOException, JsonProcessingException {
            this._property.set(bean, this._propertyName, this.value);
        }
    }
    
    static final class Map extends PropertyValue
    {
        final Object _key;
        
        public Map(final PropertyValue next, final Object value, final Object key) {
            super(next, value);
            this._key = key;
        }
        
        @Override
        public void assign(final Object bean) throws IOException, JsonProcessingException {
            ((java.util.Map)bean).put(this._key, this.value);
        }
    }
}
