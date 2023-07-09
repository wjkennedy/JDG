// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2;

public interface Digest
{
    String getAlgorithmName();
    
    int getDigestSize();
    
    void update(final byte p0);
    
    void update(final byte[] p0, final int p1, final int p2);
    
    int doFinal(final byte[] p0, final int p1);
    
    void reset();
}
