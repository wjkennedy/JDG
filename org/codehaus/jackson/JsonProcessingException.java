// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson;

import java.io.IOException;

public class JsonProcessingException extends IOException
{
    static final long serialVersionUID = 123L;
    protected JsonLocation mLocation;
    
    protected JsonProcessingException(final String msg, final JsonLocation loc, final Throwable rootCause) {
        super(msg);
        if (rootCause != null) {
            this.initCause(rootCause);
        }
        this.mLocation = loc;
    }
    
    protected JsonProcessingException(final String msg) {
        super(msg);
    }
    
    protected JsonProcessingException(final String msg, final JsonLocation loc) {
        this(msg, loc, null);
    }
    
    protected JsonProcessingException(final String msg, final Throwable rootCause) {
        this(msg, null, rootCause);
    }
    
    protected JsonProcessingException(final Throwable rootCause) {
        this(null, null, rootCause);
    }
    
    public JsonLocation getLocation() {
        return this.mLocation;
    }
    
    @Override
    public String getMessage() {
        String msg = super.getMessage();
        if (msg == null) {
            msg = "N/A";
        }
        final JsonLocation loc = this.getLocation();
        if (loc != null) {
            final StringBuilder sb = new StringBuilder();
            sb.append(msg);
            sb.append('\n');
            sb.append(" at ");
            sb.append(loc.toString());
            return sb.toString();
        }
        return msg;
    }
    
    @Override
    public String toString() {
        return this.getClass().getName() + ": " + this.getMessage();
    }
}
