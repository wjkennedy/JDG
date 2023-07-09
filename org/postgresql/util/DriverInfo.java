// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

public final class DriverInfo
{
    public static final String DRIVER_NAME = "PostgreSQL JDBC Driver";
    public static final String DRIVER_SHORT_NAME = "PgJDBC";
    public static final String DRIVER_VERSION = "42.2.25";
    public static final String DRIVER_FULL_NAME = "PostgreSQL JDBC Driver 42.2.25";
    public static final int MAJOR_VERSION = 42;
    public static final int MINOR_VERSION = 2;
    public static final int PATCH_VERSION = 25;
    public static final String JDBC_VERSION = "4.2";
    public static final int JDBC_MAJOR_VERSION;
    public static final int JDBC_MINOR_VERSION;
    
    private DriverInfo() {
    }
    
    static {
        JDBC_MAJOR_VERSION = "4.2".charAt(0) - '0';
        JDBC_MINOR_VERSION = "4.2".charAt(2) - '0';
    }
}
