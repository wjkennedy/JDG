// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.sym;

public final class Name1 extends Name
{
    static final Name1 sEmptyName;
    final int mQuad;
    
    Name1(final String name, final int hash, final int quad) {
        super(name, hash);
        this.mQuad = quad;
    }
    
    static final Name1 getEmptyName() {
        return Name1.sEmptyName;
    }
    
    @Override
    public boolean equals(final int quad) {
        return quad == this.mQuad;
    }
    
    @Override
    public boolean equals(final int quad1, final int quad2) {
        return quad1 == this.mQuad && quad2 == 0;
    }
    
    @Override
    public boolean equals(final int[] quads, final int qlen) {
        return qlen == 1 && quads[0] == this.mQuad;
    }
    
    static {
        sEmptyName = new Name1("", 0, 0);
    }
}
