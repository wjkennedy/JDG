// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.impl;

import java.util.ArrayList;
import org.codehaus.jackson.map.deser.SettableBeanProperty;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.util.TokenBuffer;
import java.util.HashMap;

public class ExternalTypeHandler
{
    private final ExtTypedProperty[] _properties;
    private final HashMap<String, Integer> _nameToPropertyIndex;
    private final String[] _typeIds;
    private final TokenBuffer[] _tokens;
    
    protected ExternalTypeHandler(final ExtTypedProperty[] properties, final HashMap<String, Integer> nameToPropertyIndex, final String[] typeIds, final TokenBuffer[] tokens) {
        this._properties = properties;
        this._nameToPropertyIndex = nameToPropertyIndex;
        this._typeIds = typeIds;
        this._tokens = tokens;
    }
    
    protected ExternalTypeHandler(final ExternalTypeHandler h) {
        this._properties = h._properties;
        this._nameToPropertyIndex = h._nameToPropertyIndex;
        final int len = this._properties.length;
        this._typeIds = new String[len];
        this._tokens = new TokenBuffer[len];
    }
    
    public ExternalTypeHandler start() {
        return new ExternalTypeHandler(this);
    }
    
    public boolean handleTypePropertyValue(final JsonParser jp, final DeserializationContext ctxt, final String propName, final Object bean) throws IOException, JsonProcessingException {
        final Integer I = this._nameToPropertyIndex.get(propName);
        if (I == null) {
            return false;
        }
        final int index = I;
        final ExtTypedProperty prop = this._properties[index];
        if (!prop.hasTypePropertyName(propName)) {
            return false;
        }
        this._typeIds[index] = jp.getText();
        final boolean canDeserialize = bean != null && this._tokens[index] != null;
        if (canDeserialize) {
            this._deserialize(jp, ctxt, bean, index);
            this._typeIds[index] = null;
            this._tokens[index] = null;
        }
        return true;
    }
    
    public boolean handleToken(final JsonParser jp, final DeserializationContext ctxt, final String propName, final Object bean) throws IOException, JsonProcessingException {
        final Integer I = this._nameToPropertyIndex.get(propName);
        if (I == null) {
            return false;
        }
        final int index = I;
        final ExtTypedProperty prop = this._properties[index];
        boolean canDeserialize;
        if (prop.hasTypePropertyName(propName)) {
            this._typeIds[index] = jp.getText();
            jp.skipChildren();
            canDeserialize = (bean != null && this._tokens[index] != null);
        }
        else {
            final TokenBuffer tokens = new TokenBuffer(jp.getCodec());
            tokens.copyCurrentStructure(jp);
            this._tokens[index] = tokens;
            canDeserialize = (bean != null && this._typeIds[index] != null);
        }
        if (canDeserialize) {
            this._deserialize(jp, ctxt, bean, index);
            this._typeIds[index] = null;
            this._tokens[index] = null;
        }
        return true;
    }
    
    public Object complete(final JsonParser jp, final DeserializationContext ctxt, final Object bean) throws IOException, JsonProcessingException {
        for (int i = 0, len = this._properties.length; i < len; ++i) {
            if (this._typeIds[i] == null) {
                if (this._tokens[i] != null) {
                    throw ctxt.mappingException("Missing external type id property '" + this._properties[i].getTypePropertyName() + "'");
                }
            }
            else {
                if (this._tokens[i] == null) {
                    final SettableBeanProperty prop = this._properties[i].getProperty();
                    throw ctxt.mappingException("Missing property '" + prop.getName() + "' for external type id '" + this._properties[i].getTypePropertyName());
                }
                this._deserialize(jp, ctxt, bean, i);
            }
        }
        return bean;
    }
    
    protected final void _deserialize(final JsonParser jp, final DeserializationContext ctxt, final Object bean, final int index) throws IOException, JsonProcessingException {
        final TokenBuffer merged = new TokenBuffer(jp.getCodec());
        merged.writeStartArray();
        merged.writeString(this._typeIds[index]);
        JsonParser p2 = this._tokens[index].asParser(jp);
        p2.nextToken();
        merged.copyCurrentStructure(p2);
        merged.writeEndArray();
        p2 = merged.asParser(jp);
        p2.nextToken();
        this._properties[index].getProperty().deserializeAndSet(p2, ctxt, bean);
    }
    
    public static class Builder
    {
        private final ArrayList<ExtTypedProperty> _properties;
        private final HashMap<String, Integer> _nameToPropertyIndex;
        
        public Builder() {
            this._properties = new ArrayList<ExtTypedProperty>();
            this._nameToPropertyIndex = new HashMap<String, Integer>();
        }
        
        public void addExternal(final SettableBeanProperty property, final String extPropName) {
            final Integer index = this._properties.size();
            this._properties.add(new ExtTypedProperty(property, extPropName));
            this._nameToPropertyIndex.put(property.getName(), index);
            this._nameToPropertyIndex.put(extPropName, index);
        }
        
        public ExternalTypeHandler build() {
            return new ExternalTypeHandler(this._properties.toArray(new ExtTypedProperty[this._properties.size()]), this._nameToPropertyIndex, null, null);
        }
    }
    
    private static final class ExtTypedProperty
    {
        private final SettableBeanProperty _property;
        private final String _typePropertyName;
        
        public ExtTypedProperty(final SettableBeanProperty property, final String typePropertyName) {
            this._property = property;
            this._typePropertyName = typePropertyName;
        }
        
        public boolean hasTypePropertyName(final String n) {
            return n.equals(this._typePropertyName);
        }
        
        public String getTypePropertyName() {
            return this._typePropertyName;
        }
        
        public SettableBeanProperty getProperty() {
            return this._property;
        }
    }
}
