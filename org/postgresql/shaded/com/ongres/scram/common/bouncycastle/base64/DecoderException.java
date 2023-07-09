// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.bouncycastle.base64;

public class DecoderException extends IllegalStateException
{
    private Throwable cause;
    
    DecoderException(final String msg, final Throwable cause) {
        super(msg);
        this.cause = cause;
    }
    
    @Override
    public Throwable getCause() {
        return this.cause;
    }
}
