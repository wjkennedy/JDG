// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.message;

import org.postgresql.shaded.com.ongres.scram.common.exception.ScramParseException;
import org.postgresql.shaded.com.ongres.scram.common.util.StringWritableCsv;
import org.postgresql.shaded.com.ongres.scram.common.ScramAttributeValue;
import org.postgresql.shaded.com.ongres.scram.common.ScramStringFormatting;
import org.postgresql.shaded.com.ongres.scram.common.ScramAttributes;
import org.postgresql.shaded.com.ongres.scram.common.gssapi.Gs2CbindFlag;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import org.postgresql.shaded.com.ongres.scram.common.gssapi.Gs2Header;
import org.postgresql.shaded.com.ongres.scram.common.util.StringWritable;

public class ClientFirstMessage implements StringWritable
{
    private final Gs2Header gs2Header;
    private final String user;
    private final String nonce;
    
    public ClientFirstMessage(final Gs2Header gs2Header, final String user, final String nonce) throws IllegalArgumentException {
        this.gs2Header = Preconditions.checkNotNull(gs2Header, "gs2Header");
        this.user = Preconditions.checkNotEmpty(user, "user");
        this.nonce = Preconditions.checkNotEmpty(nonce, "nonce");
    }
    
    private static Gs2Header gs2Header(final Gs2CbindFlag gs2CbindFlag, final String authzid, final String cbindName) {
        Preconditions.checkNotNull(gs2CbindFlag, "gs2CbindFlag");
        if (Gs2CbindFlag.CHANNEL_BINDING_REQUIRED == gs2CbindFlag && null == cbindName) {
            throw new IllegalArgumentException("Channel binding name is required if channel binding is specified");
        }
        return new Gs2Header(gs2CbindFlag, cbindName, authzid);
    }
    
    public ClientFirstMessage(final Gs2CbindFlag gs2CbindFlag, final String authzid, final String cbindName, final String user, final String nonce) {
        this(gs2Header(gs2CbindFlag, authzid, cbindName), user, nonce);
    }
    
    public ClientFirstMessage(final String user, final String nonce) {
        this(gs2Header(Gs2CbindFlag.CLIENT_NOT, null, null), user, nonce);
    }
    
    public Gs2CbindFlag getChannelBindingFlag() {
        return this.gs2Header.getChannelBindingFlag();
    }
    
    public boolean isChannelBinding() {
        return this.gs2Header.getChannelBindingFlag() == Gs2CbindFlag.CHANNEL_BINDING_REQUIRED;
    }
    
    public String getChannelBindingName() {
        return this.gs2Header.getChannelBindingName();
    }
    
    public String getAuthzid() {
        return this.gs2Header.getAuthzid();
    }
    
    public Gs2Header getGs2Header() {
        return this.gs2Header;
    }
    
    public String getUser() {
        return this.user;
    }
    
    public String getNonce() {
        return this.nonce;
    }
    
    public StringBuffer writeToWithoutGs2Header(final StringBuffer sb) {
        return StringWritableCsv.writeTo(sb, new ScramAttributeValue(ScramAttributes.USERNAME, ScramStringFormatting.toSaslName(this.user)), new ScramAttributeValue(ScramAttributes.NONCE, this.nonce));
    }
    
    @Override
    public StringBuffer writeTo(final StringBuffer sb) {
        StringWritableCsv.writeTo(sb, this.gs2Header, null);
        return this.writeToWithoutGs2Header(sb);
    }
    
    public static ClientFirstMessage parseFrom(final String clientFirstMessage) throws ScramParseException, IllegalArgumentException {
        Preconditions.checkNotEmpty(clientFirstMessage, "clientFirstMessage");
        final Gs2Header gs2Header = Gs2Header.parseFrom(clientFirstMessage);
        String[] userNonceString;
        try {
            userNonceString = StringWritableCsv.parseFrom(clientFirstMessage, 2, 2);
        }
        catch (final IllegalArgumentException e) {
            throw new ScramParseException("Illegal series of attributes in client-first-message", e);
        }
        final ScramAttributeValue user = ScramAttributeValue.parse(userNonceString[0]);
        if (ScramAttributes.USERNAME.getChar() != user.getChar()) {
            throw new ScramParseException("user must be the 3rd element of the client-first-message");
        }
        final ScramAttributeValue nonce = ScramAttributeValue.parse(userNonceString[1]);
        if (ScramAttributes.NONCE.getChar() != nonce.getChar()) {
            throw new ScramParseException("nonce must be the 4th element of the client-first-message");
        }
        return new ClientFirstMessage(gs2Header, user.getValue(), nonce.getValue());
    }
    
    @Override
    public String toString() {
        return this.writeTo(new StringBuffer()).toString();
    }
}
