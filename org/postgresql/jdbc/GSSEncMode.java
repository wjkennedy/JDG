// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import org.postgresql.PGProperty;
import java.util.Properties;

public enum GSSEncMode
{
    DISABLE("disable"), 
    ALLOW("allow"), 
    PREFER("prefer"), 
    REQUIRE("require");
    
    private static final GSSEncMode[] VALUES;
    public final String value;
    
    private GSSEncMode(final String value) {
        this.value = value;
    }
    
    public boolean requireEncryption() {
        return this.compareTo(GSSEncMode.REQUIRE) >= 0;
    }
    
    public static GSSEncMode of(final Properties info) throws PSQLException {
        final String gssEncMode = PGProperty.GSS_ENC_MODE.get(info);
        if (gssEncMode == null) {
            return GSSEncMode.ALLOW;
        }
        for (final GSSEncMode mode : GSSEncMode.VALUES) {
            if (mode.value.equalsIgnoreCase(gssEncMode)) {
                return mode;
            }
        }
        throw new PSQLException(GT.tr("Invalid gssEncMode value: {0}", gssEncMode), PSQLState.CONNECTION_UNABLE_TO_CONNECT);
    }
    
    static {
        VALUES = values();
    }
}
