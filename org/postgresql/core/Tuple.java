// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import org.checkerframework.dataflow.qual.Pure;

public class Tuple
{
    private final boolean forUpdate;
    final byte[][] data;
    
    public Tuple(final int length) {
        this(new byte[length][], true);
    }
    
    public Tuple(final byte[][] data) {
        this(data, false);
    }
    
    private Tuple(final byte[][] data, final boolean forUpdate) {
        this.data = data;
        this.forUpdate = forUpdate;
    }
    
    public int fieldCount() {
        return this.data.length;
    }
    
    public int length() {
        int length = 0;
        for (final byte[] field : this.data) {
            if (field != null) {
                length += field.length;
            }
        }
        return length;
    }
    
    @Pure
    public byte[] get(final int index) {
        return this.data[index];
    }
    
    public Tuple updateableCopy() {
        return this.copy(true);
    }
    
    public Tuple readOnlyCopy() {
        return this.copy(false);
    }
    
    private Tuple copy(final boolean forUpdate) {
        final byte[][] dataCopy = new byte[this.data.length][];
        System.arraycopy(this.data, 0, dataCopy, 0, this.data.length);
        return new Tuple(dataCopy, forUpdate);
    }
    
    public void set(final int index, final byte[] fieldData) {
        if (!this.forUpdate) {
            throw new IllegalArgumentException("Attempted to write to readonly tuple");
        }
        this.data[index] = fieldData;
    }
}
