// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.message;

import org.postgresql.shaded.com.ongres.scram.common.util.StringWritableCsv;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import org.postgresql.shaded.com.ongres.scram.common.ScramAttributeValue;
import org.postgresql.shaded.com.ongres.scram.common.ScramStringFormatting;
import org.postgresql.shaded.com.ongres.scram.common.ScramAttributes;
import org.postgresql.shaded.com.ongres.scram.common.gssapi.Gs2Header;
import org.postgresql.shaded.com.ongres.scram.common.util.StringWritable;

public class ClientFinalMessage implements StringWritable
{
    private final String cbind;
    private final String nonce;
    private final byte[] proof;
    
    private static String generateCBind(final Gs2Header gs2Header, final byte[] cbindData) {
        final StringBuffer sb = new StringBuffer();
        gs2Header.writeTo(sb).append(',');
        if (null != cbindData) {
            new ScramAttributeValue(ScramAttributes.CHANNEL_BINDING, ScramStringFormatting.base64Encode(cbindData)).writeTo(sb);
        }
        return sb.toString();
    }
    
    public ClientFinalMessage(final Gs2Header gs2Header, final byte[] cbindData, final String nonce, final byte[] proof) {
        this.cbind = generateCBind(Preconditions.checkNotNull(gs2Header, "gs2Header"), cbindData);
        this.nonce = Preconditions.checkNotEmpty(nonce, "nonce");
        this.proof = Preconditions.checkNotNull(proof, "proof");
    }
    
    private static StringBuffer writeToWithoutProof(final StringBuffer sb, final String cbind, final String nonce) {
        return StringWritableCsv.writeTo(sb, new ScramAttributeValue(ScramAttributes.CHANNEL_BINDING, ScramStringFormatting.base64Encode(cbind)), new ScramAttributeValue(ScramAttributes.NONCE, nonce));
    }
    
    private static StringBuffer writeToWithoutProof(final StringBuffer sb, final Gs2Header gs2Header, final byte[] cbindData, final String nonce) {
        return writeToWithoutProof(sb, generateCBind(Preconditions.checkNotNull(gs2Header, "gs2Header"), cbindData), nonce);
    }
    
    public static StringBuffer writeToWithoutProof(final Gs2Header gs2Header, final byte[] cbindData, final String nonce) {
        return writeToWithoutProof(new StringBuffer(), gs2Header, cbindData, nonce);
    }
    
    @Override
    public StringBuffer writeTo(final StringBuffer sb) {
        writeToWithoutProof(sb, this.cbind, this.nonce);
        return StringWritableCsv.writeTo(sb, null, new ScramAttributeValue(ScramAttributes.CLIENT_PROOF, ScramStringFormatting.base64Encode(this.proof)));
    }
    
    @Override
    public String toString() {
        return this.writeTo(new StringBuffer()).toString();
    }
}
