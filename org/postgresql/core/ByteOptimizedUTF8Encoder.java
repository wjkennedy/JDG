// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import java.io.IOException;
import java.nio.charset.Charset;

final class ByteOptimizedUTF8Encoder extends OptimizedUTF8Encoder
{
    private static final Charset ASCII_CHARSET;
    
    @Override
    public String decode(final byte[] encodedString, final int offset, final int length) throws IOException {
        if (length <= 32) {
            return this.charDecode(encodedString, offset, length);
        }
        for (int i = offset, j = offset + length; i < j; ++i) {
            if (encodedString[i] < 0) {
                return this.slowDecode(encodedString, offset, length, i);
            }
        }
        return new String(encodedString, offset, length, ByteOptimizedUTF8Encoder.ASCII_CHARSET);
    }
    
    private synchronized String slowDecode(final byte[] encodedString, final int offset, final int length, final int curIdx) throws IOException {
        final char[] chars = this.getCharArray(length);
        int out = 0;
        for (int i = offset; i < curIdx; ++i) {
            chars[out++] = (char)encodedString[i];
        }
        return OptimizedUTF8Encoder.decodeToChars(encodedString, curIdx, length - (curIdx - offset), chars, out);
    }
    
    static {
        ASCII_CHARSET = Charset.forName("ascii");
    }
}
