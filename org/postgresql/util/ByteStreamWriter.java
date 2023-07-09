// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import java.io.OutputStream;
import java.io.IOException;

public interface ByteStreamWriter
{
    int getLength();
    
    void writeTo(final ByteStreamTarget p0) throws IOException;
    
    public interface ByteStreamTarget
    {
        OutputStream getOutputStream();
    }
}
