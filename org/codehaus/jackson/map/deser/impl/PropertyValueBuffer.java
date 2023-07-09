// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.impl;

import org.codehaus.jackson.map.deser.SettableAnyProperty;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.deser.SettableBeanProperty;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;

public final class PropertyValueBuffer
{
    final JsonParser _parser;
    final DeserializationContext _context;
    final Object[] _creatorParameters;
    private int _paramsNeeded;
    private PropertyValue _buffered;
    
    public PropertyValueBuffer(final JsonParser jp, final DeserializationContext ctxt, final int paramCount) {
        this._parser = jp;
        this._context = ctxt;
        this._paramsNeeded = paramCount;
        this._creatorParameters = new Object[paramCount];
    }
    
    public void inject(final SettableBeanProperty[] injectableProperties) {
        for (int i = 0, len = injectableProperties.length; i < len; ++i) {
            final SettableBeanProperty prop = injectableProperties[i];
            if (prop != null) {
                this._creatorParameters[i] = this._context.findInjectableValue(prop.getInjectableValueId(), prop, null);
            }
        }
    }
    
    protected final Object[] getParameters(final Object[] defaults) {
        if (defaults != null) {
            for (int i = 0, len = this._creatorParameters.length; i < len; ++i) {
                if (this._creatorParameters[i] == null) {
                    final Object value = defaults[i];
                    if (value != null) {
                        this._creatorParameters[i] = value;
                    }
                }
            }
        }
        return this._creatorParameters;
    }
    
    protected PropertyValue buffered() {
        return this._buffered;
    }
    
    public boolean assignParameter(final int index, final Object value) {
        this._creatorParameters[index] = value;
        final int paramsNeeded = this._paramsNeeded - 1;
        this._paramsNeeded = paramsNeeded;
        return paramsNeeded <= 0;
    }
    
    public void bufferProperty(final SettableBeanProperty prop, final Object value) {
        this._buffered = new PropertyValue.Regular(this._buffered, value, prop);
    }
    
    public void bufferAnyProperty(final SettableAnyProperty prop, final String propName, final Object value) {
        this._buffered = new PropertyValue.Any(this._buffered, value, prop, propName);
    }
    
    public void bufferMapProperty(final Object key, final Object value) {
        this._buffered = new PropertyValue.Map(this._buffered, value, key);
    }
}
