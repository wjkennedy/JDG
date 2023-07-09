// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.xa;

import javax.transaction.xa.XAException;

public class PGXAException extends XAException
{
    PGXAException(final String message, final int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    PGXAException(final String message, final Throwable cause, final int errorCode) {
        super(message);
        this.initCause(cause);
        this.errorCode = errorCode;
    }
    
    PGXAException(final Throwable cause, final int errorCode) {
        super(errorCode);
        this.initCause(cause);
    }
}
