// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

public enum PreferQueryMode
{
    SIMPLE("simple"), 
    EXTENDED_FOR_PREPARED("extendedForPrepared"), 
    EXTENDED("extended"), 
    EXTENDED_CACHE_EVERYTHING("extendedCacheEverything");
    
    private final String value;
    
    private PreferQueryMode(final String value) {
        this.value = value;
    }
    
    public static PreferQueryMode of(final String mode) {
        for (final PreferQueryMode preferQueryMode : values()) {
            if (preferQueryMode.value.equals(mode)) {
                return preferQueryMode;
            }
        }
        return PreferQueryMode.EXTENDED;
    }
    
    public String value() {
        return this.value;
    }
}
