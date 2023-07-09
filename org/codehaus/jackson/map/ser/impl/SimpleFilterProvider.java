// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.impl;

import java.util.HashMap;
import org.codehaus.jackson.map.ser.BeanPropertyFilter;
import java.util.Map;
import org.codehaus.jackson.map.ser.FilterProvider;

public class SimpleFilterProvider extends FilterProvider
{
    protected final Map<String, BeanPropertyFilter> _filtersById;
    protected BeanPropertyFilter _defaultFilter;
    protected boolean _cfgFailOnUnknownId;
    
    public SimpleFilterProvider() {
        this(new HashMap<String, BeanPropertyFilter>());
    }
    
    public SimpleFilterProvider(final Map<String, BeanPropertyFilter> mapping) {
        this._cfgFailOnUnknownId = true;
        this._filtersById = mapping;
    }
    
    public SimpleFilterProvider setDefaultFilter(final BeanPropertyFilter f) {
        this._defaultFilter = f;
        return this;
    }
    
    public BeanPropertyFilter getDefaultFilter() {
        return this._defaultFilter;
    }
    
    public SimpleFilterProvider setFailOnUnknownId(final boolean state) {
        this._cfgFailOnUnknownId = state;
        return this;
    }
    
    public boolean willFailOnUnknownId() {
        return this._cfgFailOnUnknownId;
    }
    
    public SimpleFilterProvider addFilter(final String id, final BeanPropertyFilter filter) {
        this._filtersById.put(id, filter);
        return this;
    }
    
    public BeanPropertyFilter removeFilter(final String id) {
        return this._filtersById.remove(id);
    }
    
    @Override
    public BeanPropertyFilter findFilter(final Object filterId) {
        BeanPropertyFilter f = this._filtersById.get(filterId);
        if (f == null) {
            f = this._defaultFilter;
            if (f == null && this._cfgFailOnUnknownId) {
                throw new IllegalArgumentException("No filter configured with id '" + filterId + "' (type " + filterId.getClass().getName() + ")");
            }
        }
        return f;
    }
}
