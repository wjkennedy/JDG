// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common;

import org.postgresql.shaded.com.ongres.scram.common.stringprep.StringPreparation;

public interface ScramMechanism
{
    String getName();
    
    byte[] digest(final byte[] p0) throws RuntimeException;
    
    byte[] hmac(final byte[] p0, final byte[] p1) throws RuntimeException;
    
    int algorithmKeyLength();
    
    boolean supportsChannelBinding();
    
    byte[] saltedPassword(final StringPreparation p0, final String p1, final byte[] p2, final int p3);
}
