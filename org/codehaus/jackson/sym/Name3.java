// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.sym;

public final class Name3 extends Name
{
    final int mQuad1;
    final int mQuad2;
    final int mQuad3;
    
    Name3(final String name, final int hash, final int q1, final int q2, final int q3) {
        super(name, hash);
        this.mQuad1 = q1;
        this.mQuad2 = q2;
        this.mQuad3 = q3;
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
        return qlen == 3 && quads[0] == this.mQuad1 && quads[1] == this.mQuad2 && quads[2] == this.mQuad3;
    }
}
