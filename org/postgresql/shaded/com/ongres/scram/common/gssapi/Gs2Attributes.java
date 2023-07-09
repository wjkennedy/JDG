// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.gssapi;

import org.postgresql.shaded.com.ongres.scram.common.ScramAttributes;
import org.postgresql.shaded.com.ongres.scram.common.util.CharAttribute;

public enum Gs2Attributes implements CharAttribute
{
    CLIENT_NOT(Gs2CbindFlag.CLIENT_NOT.getChar()), 
    CLIENT_YES_SERVER_NOT(Gs2CbindFlag.CLIENT_YES_SERVER_NOT.getChar()), 
    CHANNEL_BINDING_REQUIRED(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED.getChar()), 
    AUTHZID(ScramAttributes.AUTHZID.getChar());
    
    private final char flag;
    
    private Gs2Attributes(final char flag) {
        this.flag = flag;
    }
    
    @Override
    public char getChar() {
        return this.flag;
    }
    
    public static Gs2Attributes byChar(final char c) {
        switch (c) {
            case 'n': {
                return Gs2Attributes.CLIENT_NOT;
            }
            case 'y': {
                return Gs2Attributes.CLIENT_YES_SERVER_NOT;
            }
            case 'p': {
                return Gs2Attributes.CHANNEL_BINDING_REQUIRED;
            }
            case 'a': {
                return Gs2Attributes.AUTHZID;
            }
            default: {
                throw new IllegalArgumentException("Invalid GS2Attribute character '" + c + "'");
            }
        }
    }
    
    public static Gs2Attributes byGS2CbindFlag(final Gs2CbindFlag cbindFlag) {
        return byChar(cbindFlag.getChar());
    }
}
