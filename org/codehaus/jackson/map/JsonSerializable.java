// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;

@Deprecated
public interface JsonSerializable
{
    void serialize(final JsonGenerator p0, final SerializerProvider p1) throws IOException, JsonProcessingException;
}
