// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.type.JavaType;

public abstract class AbstractTypeResolver
{
    public JavaType findTypeMapping(final DeserializationConfig config, final JavaType type) {
        return null;
    }
    
    public JavaType resolveAbstractType(final DeserializationConfig config, final JavaType type) {
        return null;
    }
}
