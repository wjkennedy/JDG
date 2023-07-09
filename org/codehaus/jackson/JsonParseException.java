// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson;

public class JsonParseException extends JsonProcessingException
{
    static final long serialVersionUID = 123L;
    
    public JsonParseException(final String msg, final JsonLocation loc) {
        super(msg, loc);
    }
    
    public JsonParseException(final String msg, final JsonLocation loc, final Throwable root) {
        super(msg, loc, root);
    }
}
