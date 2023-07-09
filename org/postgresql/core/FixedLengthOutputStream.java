// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import java.io.IOException;
import java.io.OutputStream;

public class FixedLengthOutputStream extends OutputStream
{
    private final int size;
    private final OutputStream target;
    private int written;
    
    public FixedLengthOutputStream(final int size, final OutputStream target) {
        this.size = size;
        this.target = target;
    }
    
    @Override
    public void write(final int b) throws IOException {
        this.verifyAllowed(1);
        ++this.written;
        this.target.write(b);
    }
    
    @Override
    public void write(final byte[] buf, final int offset, final int len) throws IOException {
        if (offset < 0 || len < 0 || offset + len > buf.length) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return;
        }
        this.verifyAllowed(len);
        this.target.write(buf, offset, len);
        this.written += len;
    }
    
    public int remaining() {
        return this.size - this.written;
    }
    
    private void verifyAllowed(final int wanted) throws IOException {
        if (this.remaining() < wanted) {
            throw new IOException("Attempt to write more than the specified " + this.size + " bytes");
        }
    }
}
