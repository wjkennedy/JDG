// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

public enum JavaVersion
{
    v1_6, 
    v1_7, 
    v1_8, 
    other;
    
    private static final JavaVersion RUNTIME_VERSION;
    
    public static JavaVersion getRuntimeVersion() {
        return JavaVersion.RUNTIME_VERSION;
    }
    
    public static JavaVersion from(final String version) {
        if (version.startsWith("1.6")) {
            return JavaVersion.v1_6;
        }
        if (version.startsWith("1.7")) {
            return JavaVersion.v1_7;
        }
        if (version.startsWith("1.8")) {
            return JavaVersion.v1_8;
        }
        return JavaVersion.other;
    }
    
    static {
        RUNTIME_VERSION = from(System.getProperty("java.version"));
    }
}
