// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson;

public class JsonGenerationException extends JsonProcessingException
{
    static final long serialVersionUID = 123L;
    
    public JsonGenerationException(final Throwable rootCause) {
        super(rootCause);
    }
    
    public JsonGenerationException(final String msg) {
        super(msg, (JsonLocation)null);
    }
    
    public JsonGenerationException(final String msg, final Throwable rootCause) {
        super(msg, null, rootCause);
    }
}
