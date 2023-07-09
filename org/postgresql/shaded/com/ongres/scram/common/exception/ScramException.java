// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.exception;

import javax.security.sasl.SaslException;

public class ScramException extends SaslException
{
    public ScramException(final String detail) {
        super(detail);
    }
    
    public ScramException(final String detail, final Throwable ex) {
        super(detail, ex);
    }
}
