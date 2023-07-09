// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import org.postgresql.PGProperty;
import java.util.Properties;

public enum SslMode
{
    DISABLE("disable"), 
    ALLOW("allow"), 
    PREFER("prefer"), 
    REQUIRE("require"), 
    VERIFY_CA("verify-ca"), 
    VERIFY_FULL("verify-full");
    
    public static final SslMode[] VALUES;
    public final String value;
    
    private SslMode(final String value) {
        this.value = value;
    }
    
    public boolean requireEncryption() {
        return this.compareTo(SslMode.REQUIRE) >= 0;
    }
    
    public boolean verifyCertificate() {
        return this == SslMode.VERIFY_CA || this == SslMode.VERIFY_FULL;
    }
    
    public boolean verifyPeerName() {
        return this == SslMode.VERIFY_FULL;
    }
    
    public static SslMode of(final Properties info) throws PSQLException {
        final String sslmode = PGProperty.SSL_MODE.get(info);
        if (sslmode != null) {
            for (final SslMode sslMode : SslMode.VALUES) {
                if (sslMode.value.equalsIgnoreCase(sslmode)) {
                    return sslMode;
                }
            }
            throw new PSQLException(GT.tr("Invalid sslmode value: {0}", sslmode), PSQLState.CONNECTION_UNABLE_TO_CONNECT);
        }
        if (PGProperty.SSL.getBoolean(info) || "".equals(PGProperty.SSL.get(info))) {
            return SslMode.VERIFY_FULL;
        }
        return SslMode.PREFER;
    }
    
    static {
        VALUES = values();
    }
}
