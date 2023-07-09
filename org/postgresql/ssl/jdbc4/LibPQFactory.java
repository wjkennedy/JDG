// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.ssl.jdbc4;

import javax.net.ssl.SSLSession;
import org.postgresql.ssl.PGjdbcHostnameVerifier;
import java.net.IDN;
import org.postgresql.util.PSQLException;
import java.util.Properties;
import org.postgresql.jdbc.SslMode;
import javax.net.ssl.HostnameVerifier;

@Deprecated
public class LibPQFactory extends org.postgresql.ssl.LibPQFactory implements HostnameVerifier
{
    private final SslMode sslMode;
    
    @Deprecated
    public LibPQFactory(final Properties info) throws PSQLException {
        super(info);
        this.sslMode = SslMode.of(info);
    }
    
    @Deprecated
    public static boolean verifyHostName(final String hostname, final String pattern) {
        String canonicalHostname;
        if (hostname.startsWith("[") && hostname.endsWith("]")) {
            canonicalHostname = hostname.substring(1, hostname.length() - 1);
        }
        else {
            try {
                canonicalHostname = IDN.toASCII(hostname);
            }
            catch (final IllegalArgumentException e) {
                return false;
            }
        }
        return PGjdbcHostnameVerifier.INSTANCE.verifyHostName(canonicalHostname, pattern);
    }
    
    @Deprecated
    @Override
    public boolean verify(final String hostname, final SSLSession session) {
        return !this.sslMode.verifyPeerName() || PGjdbcHostnameVerifier.INSTANCE.verify(hostname, session);
    }
}
