// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

public class RuntimeJsonMappingException extends RuntimeException
{
    public RuntimeJsonMappingException(final JsonMappingException cause) {
        super(cause);
    }
    
    public RuntimeJsonMappingException(final String message) {
        super(message);
    }
    
    public RuntimeJsonMappingException(final String message, final JsonMappingException cause) {
        super(message, cause);
    }
}
