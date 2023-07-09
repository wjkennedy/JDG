// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;

public abstract class KeyDeserializer
{
    public abstract Object deserializeKey(final String p0, final DeserializationContext p1) throws IOException, JsonProcessingException;
    
    public abstract static class None extends KeyDeserializer
    {
    }
}
