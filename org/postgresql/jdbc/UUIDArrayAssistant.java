// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

import org.postgresql.util.ByteConverter;
import java.util.UUID;
import org.postgresql.jdbc2.ArrayAssistant;

public class UUIDArrayAssistant implements ArrayAssistant
{
    @Override
    public Class<?> baseType() {
        return UUID.class;
    }
    
    @Override
    public Object buildElement(final byte[] bytes, final int pos, final int len) {
        return new UUID(ByteConverter.int8(bytes, pos + 0), ByteConverter.int8(bytes, pos + 8));
    }
    
    @Override
    public Object buildElement(final String literal) {
        return UUID.fromString(literal);
    }
}
