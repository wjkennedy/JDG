// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.jsontype;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.type.JavaType;

public interface TypeIdResolver
{
    void init(final JavaType p0);
    
    String idFromValue(final Object p0);
    
    String idFromValueAndType(final Object p0, final Class<?> p1);
    
    JavaType typeFromId(final String p0);
    
    JsonTypeInfo.Id getMechanism();
}
