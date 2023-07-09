// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.schema;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.JsonNode;
import java.lang.reflect.Type;
import org.codehaus.jackson.map.SerializerProvider;

public interface SchemaAware
{
    JsonNode getSchema(final SerializerProvider p0, final Type p1) throws JsonMappingException;
}
