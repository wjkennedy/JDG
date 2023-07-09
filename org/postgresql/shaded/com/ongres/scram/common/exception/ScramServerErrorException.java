// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.exception;

import org.postgresql.shaded.com.ongres.scram.common.message.ServerFinalMessage;

public class ScramServerErrorException extends ScramException
{
    private final ServerFinalMessage.Error error;
    
    private static String toString(final ServerFinalMessage.Error error) {
        return "Server-final-message is an error message. Error: " + error.getErrorMessage();
    }
    
    public ScramServerErrorException(final ServerFinalMessage.Error error) {
        super(toString(error));
        this.error = error;
    }
    
    public ScramServerErrorException(final ServerFinalMessage.Error error, final Throwable ex) {
        super(toString(error), ex);
        this.error = error;
    }
    
    public ServerFinalMessage.Error getError() {
        return this.error;
    }
}
