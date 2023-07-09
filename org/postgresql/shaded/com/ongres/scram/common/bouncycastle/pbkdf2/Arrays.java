// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2;

public final class Arrays
{
    private Arrays() {
    }
    
    public static byte[] copyOfRange(final byte[] data, final int from, final int to) {
        final int newLength = getLength(from, to);
        final byte[] tmp = new byte[newLength];
        if (data.length - from < newLength) {
            System.arraycopy(data, from, tmp, 0, data.length - from);
        }
        else {
            System.arraycopy(data, from, tmp, 0, newLength);
        }
        return tmp;
    }
    
    private static int getLength(final int from, final int to) {
        final int newLength = to - from;
        if (newLength < 0) {
            final StringBuffer sb = new StringBuffer(from);
            sb.append(" > ").append(to);
            throw new IllegalArgumentException(sb.toString());
        }
        return newLength;
    }
}
