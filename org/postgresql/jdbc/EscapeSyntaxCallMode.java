// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

public enum EscapeSyntaxCallMode
{
    SELECT("select"), 
    CALL_IF_NO_RETURN("callIfNoReturn"), 
    CALL("call");
    
    private final String value;
    
    private EscapeSyntaxCallMode(final String value) {
        this.value = value;
    }
    
    public static EscapeSyntaxCallMode of(final String mode) {
        for (final EscapeSyntaxCallMode escapeSyntaxCallMode : values()) {
            if (escapeSyntaxCallMode.value.equals(mode)) {
                return escapeSyntaxCallMode;
            }
        }
        return EscapeSyntaxCallMode.SELECT;
    }
    
    public String value() {
        return this.value;
    }
}
