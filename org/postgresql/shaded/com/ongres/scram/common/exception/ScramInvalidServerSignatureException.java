// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.exception;

public class ScramInvalidServerSignatureException extends ScramException
{
    public ScramInvalidServerSignatureException(final String detail) {
        super(detail);
    }
    
    public ScramInvalidServerSignatureException(final String detail, final Throwable ex) {
        super(detail, ex);
    }
}
