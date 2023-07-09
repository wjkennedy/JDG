// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.io;

import java.io.Writer;
import java.io.IOException;
import java.io.OutputStream;

public abstract class OutputDecorator
{
    public abstract OutputStream decorate(final IOContext p0, final OutputStream p1) throws IOException;
    
    public abstract Writer decorate(final IOContext p0, final Writer p1) throws IOException;
}
