// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2;

public abstract class GeneralDigest implements ExtendedDigest, Memoable
{
    private static final int BYTE_LENGTH = 64;
    private final byte[] xBuf;
    private int xBufOff;
    private long byteCount;
    
    protected GeneralDigest() {
        this.xBuf = new byte[4];
        this.xBufOff = 0;
    }
    
    protected GeneralDigest(final GeneralDigest t) {
        this.xBuf = new byte[4];
        this.copyIn(t);
    }
    
    protected GeneralDigest(final byte[] encodedState) {
        System.arraycopy(encodedState, 0, this.xBuf = new byte[4], 0, this.xBuf.length);
        this.xBufOff = Pack.bigEndianToInt(encodedState, 4);
        this.byteCount = Pack.bigEndianToLong(encodedState, 8);
    }
    
    protected void copyIn(final GeneralDigest t) {
        System.arraycopy(t.xBuf, 0, this.xBuf, 0, t.xBuf.length);
        this.xBufOff = t.xBufOff;
        this.byteCount = t.byteCount;
    }
    
    @Override
    public void update(final byte in) {
        this.xBuf[this.xBufOff++] = in;
        if (this.xBufOff == this.xBuf.length) {
            this.processWord(this.xBuf, 0);
            this.xBufOff = 0;
        }
        ++this.byteCount;
    }
    
    @Override
    public void update(final byte[] in, final int inOff, int len) {
        len = Math.max(0, len);
        int i = 0;
        if (this.xBufOff != 0) {
            while (i < len) {
                this.xBuf[this.xBufOff++] = in[inOff + i++];
                if (this.xBufOff == 4) {
                    this.processWord(this.xBuf, 0);
                    this.xBufOff = 0;
                    break;
                }
            }
        }
        for (int limit = (len - i & 0xFFFFFFFC) + i; i < limit; i += 4) {
            this.processWord(in, inOff + i);
        }
        while (i < len) {
            this.xBuf[this.xBufOff++] = in[inOff + i++];
        }
        this.byteCount += len;
    }
    
    public void finish() {
        final long bitLength = this.byteCount << 3;
        this.update((byte)(-128));
        while (this.xBufOff != 0) {
            this.update((byte)0);
        }
        this.processLength(bitLength);
        this.processBlock();
    }
    
    @Override
    public void reset() {
        this.byteCount = 0L;
        this.xBufOff = 0;
        for (int i = 0; i < this.xBuf.length; ++i) {
            this.xBuf[i] = 0;
        }
    }
    
    protected void populateState(final byte[] state) {
        System.arraycopy(this.xBuf, 0, state, 0, this.xBufOff);
        Pack.intToBigEndian(this.xBufOff, state, 4);
        Pack.longToBigEndian(this.byteCount, state, 8);
    }
    
    @Override
    public int getByteLength() {
        return 64;
    }
    
    protected abstract void processWord(final byte[] p0, final int p1);
    
    protected abstract void processLength(final long p0);
    
    protected abstract void processBlock();
}
