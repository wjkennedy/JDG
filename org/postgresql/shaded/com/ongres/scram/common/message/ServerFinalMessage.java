// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.message;

import java.util.HashMap;
import java.util.Map;
import org.postgresql.shaded.com.ongres.scram.common.exception.ScramParseException;
import org.postgresql.shaded.com.ongres.scram.common.util.StringWritableCsv;
import org.postgresql.shaded.com.ongres.scram.common.ScramStringFormatting;
import org.postgresql.shaded.com.ongres.scram.common.ScramAttributeValue;
import org.postgresql.shaded.com.ongres.scram.common.ScramAttributes;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import org.postgresql.shaded.com.ongres.scram.common.util.StringWritable;

public class ServerFinalMessage implements StringWritable
{
    private final byte[] verifier;
    private final Error error;
    
    public ServerFinalMessage(final byte[] verifier) throws IllegalArgumentException {
        this.verifier = Preconditions.checkNotNull(verifier, "verifier");
        this.error = null;
    }
    
    public ServerFinalMessage(final Error error) throws IllegalArgumentException {
        this.error = Preconditions.checkNotNull(error, "error");
        this.verifier = null;
    }
    
    public boolean isError() {
        return null != this.error;
    }
    
    @SuppressFBWarnings({ "EI_EXPOSE_REP" })
    public byte[] getVerifier() {
        return this.verifier;
    }
    
    public Error getError() {
        return this.error;
    }
    
    @Override
    public StringBuffer writeTo(final StringBuffer sb) {
        return StringWritableCsv.writeTo(sb, this.isError() ? new ScramAttributeValue(ScramAttributes.ERROR, this.error.errorMessage) : new ScramAttributeValue(ScramAttributes.SERVER_SIGNATURE, ScramStringFormatting.base64Encode(this.verifier)));
    }
    
    public static ServerFinalMessage parseFrom(final String serverFinalMessage) throws ScramParseException, IllegalArgumentException {
        Preconditions.checkNotEmpty(serverFinalMessage, "serverFinalMessage");
        final String[] attributeValues = StringWritableCsv.parseFrom(serverFinalMessage, 1, 0);
        if (attributeValues == null || attributeValues.length != 1) {
            throw new ScramParseException("Invalid server-final-message");
        }
        final ScramAttributeValue attributeValue = ScramAttributeValue.parse(attributeValues[0]);
        if (ScramAttributes.SERVER_SIGNATURE.getChar() == attributeValue.getChar()) {
            final byte[] verifier = ScramStringFormatting.base64Decode(attributeValue.getValue());
            return new ServerFinalMessage(verifier);
        }
        if (ScramAttributes.ERROR.getChar() == attributeValue.getChar()) {
            return new ServerFinalMessage(Error.getByErrorMessage(attributeValue.getValue()));
        }
        throw new ScramParseException("Invalid server-final-message: it must contain either a verifier or an error attribute");
    }
    
    @Override
    public String toString() {
        return this.writeTo(new StringBuffer()).toString();
    }
    
    public enum Error
    {
        INVALID_ENCODING("invalid-encoding"), 
        EXTENSIONS_NOT_SUPPORTED("extensions-not-supported"), 
        INVALID_PROOF("invalid-proof"), 
        CHANNEL_BINDINGS_DONT_MATCH("channel-bindings-dont-match"), 
        SERVER_DOES_SUPPORT_CHANNEL_BINDING("server-does-support-channel-binding"), 
        CHANNEL_BINDING_NOT_SUPPORTED("channel-binding-not-supported"), 
        UNSUPPORTED_CHANNEL_BINDING_TYPE("unsupported-channel-binding-type"), 
        UNKNOWN_USER("unknown-user"), 
        INVALID_USERNAME_ENCODING("invalid-username-encoding"), 
        NO_RESOURCES("no-resources"), 
        OTHER_ERROR("other-error");
        
        private static final Map<String, Error> BY_NAME_MAPPING;
        private final String errorMessage;
        
        private Error(final String errorMessage) {
            this.errorMessage = errorMessage;
        }
        
        public String getErrorMessage() {
            return this.errorMessage;
        }
        
        public static Error getByErrorMessage(final String errorMessage) throws IllegalArgumentException {
            Preconditions.checkNotEmpty(errorMessage, "errorMessage");
            if (!Error.BY_NAME_MAPPING.containsKey(errorMessage)) {
                throw new IllegalArgumentException("Invalid error message '" + errorMessage + "'");
            }
            return Error.BY_NAME_MAPPING.get(errorMessage);
        }
        
        private static Map<String, Error> valuesAsMap() {
            final Map<String, Error> map = new HashMap<String, Error>(values().length);
            for (final Error error : values()) {
                map.put(error.errorMessage, error);
            }
            return map;
        }
        
        static {
            BY_NAME_MAPPING = valuesAsMap();
        }
    }
}
