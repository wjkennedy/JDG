// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2;

public abstract class Pack
{
    public static int bigEndianToInt(final byte[] bs, int off) {
        int n = bs[off] << 24;
        n |= (bs[++off] & 0xFF) << 16;
        n |= (bs[++off] & 0xFF) << 8;
        n |= (bs[++off] & 0xFF);
        return n;
    }
    
    public static long bigEndianToLong(final byte[] bs, final int off) {
        final int hi = bigEndianToInt(bs, off);
        final int lo = bigEndianToInt(bs, off + 4);
        return ((long)hi & 0xFFFFFFFFL) << 32 | ((long)lo & 0xFFFFFFFFL);
    }
    
    public static void longToBigEndian(final long n, final byte[] bs, final int off) {
        intToBigEndian((int)(n >>> 32), bs, off);
        intToBigEndian((int)(n & 0xFFFFFFFFL), bs, off + 4);
    }
    
    public static byte[] longToBigEndian(final long[] ns) {
        final byte[] bs = new byte[8 * ns.length];
        longToBigEndian(ns, bs, 0);
        return bs;
    }
    
    public static void longToBigEndian(final long[] ns, final byte[] bs, int off) {
        for (int i = 0; i < ns.length; ++i) {
            longToBigEndian(ns[i], bs, off);
            off += 8;
        }
    }
    
    public static short littleEndianToShort(final byte[] bs, int off) {
        int n = bs[off] & 0xFF;
        n |= (bs[++off] & 0xFF) << 8;
        return (short)n;
    }
    
    public static void intToBigEndian(final int n, final byte[] bs, int off) {
        bs[off] = (byte)(n >>> 24);
        bs[++off] = (byte)(n >>> 16);
        bs[++off] = (byte)(n >>> 8);
        bs[++off] = (byte)n;
    }
}
