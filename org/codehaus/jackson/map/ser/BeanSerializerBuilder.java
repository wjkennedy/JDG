// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser;

import org.codehaus.jackson.map.JsonSerializer;
import java.util.List;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;

public class BeanSerializerBuilder
{
    private static final BeanPropertyWriter[] NO_PROPERTIES;
    protected final BasicBeanDescription _beanDesc;
    protected List<BeanPropertyWriter> _properties;
    protected BeanPropertyWriter[] _filteredProperties;
    protected AnyGetterWriter _anyGetter;
    protected Object _filterId;
    
    public BeanSerializerBuilder(final BasicBeanDescription beanDesc) {
        this._beanDesc = beanDesc;
    }
    
    protected BeanSerializerBuilder(final BeanSerializerBuilder src) {
        this._beanDesc = src._beanDesc;
        this._properties = src._properties;
        this._filteredProperties = src._filteredProperties;
        this._anyGetter = src._anyGetter;
        this._filterId = src._filterId;
    }
    
    public BasicBeanDescription getBeanDescription() {
        return this._beanDesc;
    }
    
    public List<BeanPropertyWriter> getProperties() {
        return this._properties;
    }
    
    public BeanPropertyWriter[] getFilteredProperties() {
        return this._filteredProperties;
    }
    
    public boolean hasProperties() {
        return this._properties != null && this._properties.size() > 0;
    }
    
    public void setProperties(final List<BeanPropertyWriter> properties) {
        this._properties = properties;
    }
    
    public void setFilteredProperties(final BeanPropertyWriter[] properties) {
        this._filteredProperties = properties;
    }
    
    public void setAnyGetter(final AnyGetterWriter anyGetter) {
        this._anyGetter = anyGetter;
    }
    
    public void setFilterId(final Object filterId) {
        this._filterId = filterId;
    }
    
    public JsonSerializer<?> build() {
        BeanPropertyWriter[] properties;
        if (this._properties == null || this._properties.isEmpty()) {
            if (this._anyGetter == null) {
                return null;
            }
            properties = BeanSerializerBuilder.NO_PROPERTIES;
        }
        else {
            properties = this._properties.toArray(new BeanPropertyWriter[this._properties.size()]);
        }
        return new BeanSerializer(this._beanDesc.getType(), properties, this._filteredProperties, this._anyGetter, this._filterId);
    }
    
    public BeanSerializer createDummy() {
        return BeanSerializer.createDummy(this._beanDesc.getBeanClass());
    }
    
    static {
        NO_PROPERTIES = new BeanPropertyWriter[0];
    }
}
