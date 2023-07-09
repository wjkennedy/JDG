// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.jsontype.impl;

import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;

public abstract class TypeIdResolverBase implements TypeIdResolver
{
    protected final TypeFactory _typeFactory;
    protected final JavaType _baseType;
    
    protected TypeIdResolverBase(final JavaType baseType, final TypeFactory typeFactory) {
        this._baseType = baseType;
        this._typeFactory = typeFactory;
    }
    
    public void init(final JavaType bt) {
    }
    
    public String idFromBaseType() {
        return this.idFromValueAndType(null, this._baseType.getRawClass());
    }
}
