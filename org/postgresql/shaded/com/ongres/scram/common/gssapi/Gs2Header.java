// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.gssapi;

import org.postgresql.shaded.com.ongres.scram.common.util.StringWritableCsv;
import org.postgresql.shaded.com.ongres.scram.common.util.StringWritable;
import org.postgresql.shaded.com.ongres.scram.common.ScramStringFormatting;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import org.postgresql.shaded.com.ongres.scram.common.util.AbstractStringWritable;

public class Gs2Header extends AbstractStringWritable
{
    private final Gs2AttributeValue cbind;
    private final Gs2AttributeValue authzid;
    
    public Gs2Header(final Gs2CbindFlag cbindFlag, final String cbName, final String authzid) throws IllegalArgumentException {
        Preconditions.checkNotNull(cbindFlag, "cbindFlag");
        if (cbindFlag == Gs2CbindFlag.CHANNEL_BINDING_REQUIRED ^ cbName != null) {
            throw new IllegalArgumentException("Specify channel binding flag and value together, or none");
        }
        this.cbind = new Gs2AttributeValue(Gs2Attributes.byGS2CbindFlag(cbindFlag), cbName);
        this.authzid = ((authzid == null) ? null : new Gs2AttributeValue(Gs2Attributes.AUTHZID, ScramStringFormatting.toSaslName(authzid)));
    }
    
    public Gs2Header(final Gs2CbindFlag cbindFlag, final String cbName) throws IllegalArgumentException {
        this(cbindFlag, cbName, null);
    }
    
    public Gs2Header(final Gs2CbindFlag cbindFlag) {
        this(cbindFlag, null, null);
    }
    
    public Gs2CbindFlag getChannelBindingFlag() {
        return Gs2CbindFlag.byChar(this.cbind.getChar());
    }
    
    public String getChannelBindingName() {
        return this.cbind.getValue();
    }
    
    public String getAuthzid() {
        return (this.authzid != null) ? this.authzid.getValue() : null;
    }
    
    @Override
    public StringBuffer writeTo(final StringBuffer sb) {
        return StringWritableCsv.writeTo(sb, this.cbind, this.authzid);
    }
    
    public static Gs2Header parseFrom(final String message) throws IllegalArgumentException {
        Preconditions.checkNotNull(message, "Null message");
        final String[] gs2HeaderSplit = StringWritableCsv.parseFrom(message, 2);
        if (gs2HeaderSplit.length == 0) {
            throw new IllegalArgumentException("Invalid number of fields for the GS2 Header");
        }
        final Gs2AttributeValue gs2cbind = Gs2AttributeValue.parse(gs2HeaderSplit[0]);
        return new Gs2Header(Gs2CbindFlag.byChar(gs2cbind.getChar()), gs2cbind.getValue(), (gs2HeaderSplit[1] == null || gs2HeaderSplit[1].isEmpty()) ? null : Gs2AttributeValue.parse(gs2HeaderSplit[1]).getValue());
    }
}
