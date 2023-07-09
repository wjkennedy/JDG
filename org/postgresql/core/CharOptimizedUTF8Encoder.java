// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import java.io.IOException;

final class CharOptimizedUTF8Encoder extends OptimizedUTF8Encoder
{
    @Override
    public String decode(final byte[] encodedString, final int offset, final int length) throws IOException {
        return this.charDecode(encodedString, offset, length);
    }
}
