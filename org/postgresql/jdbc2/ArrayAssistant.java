// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc2;

public interface ArrayAssistant
{
    Class<?> baseType();
    
    Object buildElement(final byte[] p0, final int p1, final int p2);
    
    Object buildElement(final String p0);
}
