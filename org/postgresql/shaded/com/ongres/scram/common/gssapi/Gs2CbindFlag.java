// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.gssapi;

import org.postgresql.shaded.com.ongres.scram.common.util.CharAttribute;

public enum Gs2CbindFlag implements CharAttribute
{
    CLIENT_NOT('n'), 
    CLIENT_YES_SERVER_NOT('y'), 
    CHANNEL_BINDING_REQUIRED('p');
    
    private final char flag;
    
    private Gs2CbindFlag(final char flag) {
        this.flag = flag;
    }
    
    @Override
    public char getChar() {
        return this.flag;
    }
    
    public static Gs2CbindFlag byChar(final char c) {
        switch (c) {
            case 'n': {
                return Gs2CbindFlag.CLIENT_NOT;
            }
            case 'y': {
                return Gs2CbindFlag.CLIENT_YES_SERVER_NOT;
            }
            case 'p': {
                return Gs2CbindFlag.CHANNEL_BINDING_REQUIRED;
            }
            default: {
                throw new IllegalArgumentException("Invalid Gs2CbindFlag character '" + c + "'");
            }
        }
    }
}
