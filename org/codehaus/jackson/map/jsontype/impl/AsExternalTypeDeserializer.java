// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.jsontype.impl;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.type.JavaType;

public class AsExternalTypeDeserializer extends AsArrayTypeDeserializer
{
    protected final String _typePropertyName;
    
    public AsExternalTypeDeserializer(final JavaType bt, final TypeIdResolver idRes, final BeanProperty property, final Class<?> defaultImpl, final String typePropName) {
        super(bt, idRes, property, defaultImpl);
        this._typePropertyName = typePropName;
    }
    
    @Override
    public JsonTypeInfo.As getTypeInclusion() {
        return JsonTypeInfo.As.EXTERNAL_PROPERTY;
    }
    
    @Override
    public String getPropertyName() {
        return this._typePropertyName;
    }
}
