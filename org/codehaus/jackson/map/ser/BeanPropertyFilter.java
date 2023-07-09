// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser;

import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;

public interface BeanPropertyFilter
{
    void serializeAsField(final Object p0, final JsonGenerator p1, final SerializerProvider p2, final BeanPropertyWriter p3) throws Exception;
}
