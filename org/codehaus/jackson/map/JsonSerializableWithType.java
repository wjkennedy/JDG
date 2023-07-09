// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;

public interface JsonSerializableWithType extends JsonSerializable
{
    void serializeWithType(final JsonGenerator p0, final SerializerProvider p1, final TypeSerializer p2) throws IOException, JsonProcessingException;
}
