// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.io;

import java.io.Reader;
import java.io.IOException;
import java.io.InputStream;

public abstract class InputDecorator
{
    public abstract InputStream decorate(final IOContext p0, final InputStream p1) throws IOException;
    
    public abstract InputStream decorate(final IOContext p0, final byte[] p1, final int p2, final int p3) throws IOException;
    
    public abstract Reader decorate(final IOContext p0, final Reader p1) throws IOException;
}
