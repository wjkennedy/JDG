// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2;

public class KeyParameter implements CipherParameters
{
    private byte[] key;
    
    public KeyParameter(final byte[] key) {
        this(key, 0, key.length);
    }
    
    public KeyParameter(final byte[] key, final int keyOff, final int keyLen) {
        System.arraycopy(key, keyOff, this.key = new byte[keyLen], 0, keyLen);
    }
    
    public byte[] getKey() {
        return this.key;
    }
}
