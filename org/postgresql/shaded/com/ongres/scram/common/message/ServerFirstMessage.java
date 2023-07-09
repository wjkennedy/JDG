// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.message;

import org.postgresql.shaded.com.ongres.scram.common.exception.ScramParseException;
import org.postgresql.shaded.com.ongres.scram.common.util.StringWritableCsv;
import org.postgresql.shaded.com.ongres.scram.common.ScramAttributeValue;
import org.postgresql.shaded.com.ongres.scram.common.ScramAttributes;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import org.postgresql.shaded.com.ongres.scram.common.util.StringWritable;

public class ServerFirstMessage implements StringWritable
{
    public static final int ITERATION_MIN_VALUE = 4096;
    private final String clientNonce;
    private final String serverNonce;
    private final String salt;
    private final int iteration;
    
    public ServerFirstMessage(final String clientNonce, final String serverNonce, final String salt, final int iteration) throws IllegalArgumentException {
        this.clientNonce = Preconditions.checkNotEmpty(clientNonce, "clientNonce");
        this.serverNonce = Preconditions.checkNotEmpty(serverNonce, "serverNonce");
        this.salt = Preconditions.checkNotEmpty(salt, "salt");
        Preconditions.checkArgument(iteration >= 4096, "iteration must be >= 4096");
        this.iteration = iteration;
    }
    
    public String getClientNonce() {
        return this.clientNonce;
    }
    
    public String getServerNonce() {
        return this.serverNonce;
    }
    
    public String getNonce() {
        return this.clientNonce + this.serverNonce;
    }
    
    public String getSalt() {
        return this.salt;
    }
    
    public int getIteration() {
        return this.iteration;
    }
    
    @Override
    public StringBuffer writeTo(final StringBuffer sb) {
        return StringWritableCsv.writeTo(sb, new ScramAttributeValue(ScramAttributes.NONCE, this.getNonce()), new ScramAttributeValue(ScramAttributes.SALT, this.salt), new ScramAttributeValue(ScramAttributes.ITERATION, this.iteration + ""));
    }
    
    public static ServerFirstMessage parseFrom(final String serverFirstMessage, final String clientNonce) throws ScramParseException, IllegalArgumentException {
        Preconditions.checkNotEmpty(serverFirstMessage, "serverFirstMessage");
        Preconditions.checkNotEmpty(clientNonce, "clientNonce");
        final String[] attributeValues = StringWritableCsv.parseFrom(serverFirstMessage, 3, 0);
        if (attributeValues.length != 3) {
            throw new ScramParseException("Invalid server-first-message");
        }
        final ScramAttributeValue nonce = ScramAttributeValue.parse(attributeValues[0]);
        if (ScramAttributes.NONCE.getChar() != nonce.getChar()) {
            throw new ScramParseException("serverNonce must be the 1st element of the server-first-message");
        }
        if (!nonce.getValue().startsWith(clientNonce)) {
            throw new ScramParseException("parsed serverNonce does not start with client serverNonce");
        }
        final ScramAttributeValue salt = ScramAttributeValue.parse(attributeValues[1]);
        if (ScramAttributes.SALT.getChar() != salt.getChar()) {
            throw new ScramParseException("salt must be the 2nd element of the server-first-message");
        }
        final ScramAttributeValue iteration = ScramAttributeValue.parse(attributeValues[2]);
        if (ScramAttributes.ITERATION.getChar() != iteration.getChar()) {
            throw new ScramParseException("iteration must be the 3rd element of the server-first-message");
        }
        int iterationInt;
        try {
            iterationInt = Integer.parseInt(iteration.getValue());
        }
        catch (final NumberFormatException e) {
            throw new ScramParseException("invalid iteration");
        }
        return new ServerFirstMessage(clientNonce, nonce.getValue().substring(clientNonce.length()), salt.getValue(), iterationInt);
    }
    
    @Override
    public String toString() {
        return this.writeTo(new StringBuffer()).toString();
    }
}
