// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.sym;

public final class NameN extends Name
{
    final int[] mQuads;
    final int mQuadLen;
    
    NameN(final String name, final int hash, final int[] quads, final int quadLen) {
        super(name, hash);
        if (quadLen < 3) {
            throw new IllegalArgumentException("Qlen must >= 3");
        }
        this.mQuads = quads;
        this.mQuadLen = quadLen;
    }
    
    @Override
    public boolean equals(final int quad) {
        return false;
    }
    
    @Override
    public boolean equals(final int quad1, final int quad2) {
        return false;
    }
    
    @Override
    public boolean equals(final int[] quads, final int qlen) {
        if (qlen != this.mQuadLen) {
            return false;
        }
        for (int i = 0; i < qlen; ++i) {
            if (quads[i] != this.mQuads[i]) {
                return false;
            }
        }
        return true;
    }
}
