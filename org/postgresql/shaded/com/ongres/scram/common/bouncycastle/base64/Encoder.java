// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.bouncycastle.base64;

import java.io.IOException;
import java.io.OutputStream;

public interface Encoder
{
    int encode(final byte[] p0, final int p1, final int p2, final OutputStream p3) throws IOException;
    
    int decode(final byte[] p0, final int p1, final int p2, final OutputStream p3) throws IOException;
    
    int decode(final String p0, final OutputStream p1) throws IOException;
}
