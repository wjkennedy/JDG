// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.sym;

public final class Name2 extends Name
{
    final int mQuad1;
    final int mQuad2;
    
    Name2(final String name, final int hash, final int quad1, final int quad2) {
        super(name, hash);
        this.mQuad1 = quad1;
        this.mQuad2 = quad2;
    }
    
    @Override
    public boolean equals(final int quad) {
        return false;
    }
    
    @Override
    public boolean equals(final int quad1, final int quad2) {
        return quad1 == this.mQuad1 && quad2 == this.mQuad2;
    }
    
    @Override
    public boolean equals(final int[] quads, final int qlen) {
        return qlen == 2 && quads[0] == this.mQuad1 && quads[1] == this.mQuad2;
    }
}
