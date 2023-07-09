// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

public enum AutoSave
{
    NEVER, 
    ALWAYS, 
    CONSERVATIVE;
    
    private final String value;
    
    private AutoSave() {
        this.value = this.name().toLowerCase();
    }
    
    public String value() {
        return this.value;
    }
    
    public static AutoSave of(final String value) {
        return valueOf(value.toUpperCase());
    }
}
