// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import java.net.URL;
import org.postgresql.Driver;

public class PGJDBCMain
{
    public static void main(final String[] args) {
        final URL url = Driver.class.getResource("/org/postgresql/Driver.class");
        System.out.printf("%n%s%n", "PostgreSQL JDBC Driver 42.2.25");
        System.out.printf("Found in: %s%n%n", url);
        System.out.printf("The PgJDBC driver is not an executable Java program.%n%nYou must install it according to the JDBC driver installation instructions for your application / container / appserver, then use it by specifying a JDBC URL of the form %n    jdbc:postgresql://%nor using an application specific method.%n%nSee the PgJDBC documentation: http://jdbc.postgresql.org/documentation/head/index.html%n%nThis command has had no effect.%n", new Object[0]);
        System.exit(1);
    }
}
