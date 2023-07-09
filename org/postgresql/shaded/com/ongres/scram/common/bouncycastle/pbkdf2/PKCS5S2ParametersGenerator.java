// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2;

public class PKCS5S2ParametersGenerator extends PBEParametersGenerator
{
    private Mac hMac;
    private byte[] state;
    
    public PKCS5S2ParametersGenerator(final Digest digest) {
        this.hMac = new HMac(digest);
        this.state = new byte[this.hMac.getMacSize()];
    }
    
    private void F(final byte[] S, final int c, final byte[] iBuf, final byte[] out, final int outOff) {
        if (c == 0) {
            throw new IllegalArgumentException("iteration count must be at least 1.");
        }
        if (S != null) {
            this.hMac.update(S, 0, S.length);
        }
        this.hMac.update(iBuf, 0, iBuf.length);
        this.hMac.doFinal(this.state, 0);
        System.arraycopy(this.state, 0, out, outOff, this.state.length);
        for (int count = 1; count < c; ++count) {
            this.hMac.update(this.state, 0, this.state.length);
            this.hMac.doFinal(this.state, 0);
            for (int j = 0; j != this.state.length; ++j) {
                final int n = outOff + j;
                out[n] ^= this.state[j];
            }
        }
    }
    
    private byte[] generateDerivedKey(final int dkLen) {
        final int hLen = this.hMac.getMacSize();
        final int l = (dkLen + hLen - 1) / hLen;
        final byte[] iBuf = new byte[4];
        final byte[] outBytes = new byte[l * hLen];
        int outPos = 0;
        final CipherParameters param = new KeyParameter(this.password);
        this.hMac.init(param);
        for (int i = 1; i <= l; ++i) {
            int pos = 3;
            while (true) {
                final byte[] array = iBuf;
                final int n = pos;
                final byte b = (byte)(array[n] + 1);
                array[n] = b;
                if (b != 0) {
                    break;
                }
                --pos;
            }
            this.F(this.salt, this.iterationCount, iBuf, outBytes, outPos);
            outPos += hLen;
        }
        return outBytes;
    }
    
    @Override
    public CipherParameters generateDerivedParameters(int keySize) {
        keySize /= 8;
        final byte[] dKey = Arrays.copyOfRange(this.generateDerivedKey(keySize), 0, keySize);
        return new KeyParameter(dKey, 0, keySize);
    }
}
