// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.impl;

import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;

public interface Indenter
{
    void writeIndentation(final JsonGenerator p0, final int p1) throws IOException, JsonGenerationException;
    
    boolean isInline();
}
