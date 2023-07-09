// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.io;

import java.io.IOException;
import java.io.InputStream;

public final class MergedStream extends InputStream
{
    protected final IOContext _context;
    final InputStream _in;
    byte[] _buffer;
    int _ptr;
    final int _end;
    
    public MergedStream(final IOContext context, final InputStream in, final byte[] buf, final int start, final int end) {
        this._context = context;
        this._in = in;
        this._buffer = buf;
        this._ptr = start;
        this._end = end;
    }
    
    @Override
    public int available() throws IOException {
        if (this._buffer != null) {
            return this._end - this._ptr;
        }
        return this._in.available();
    }
    
    @Override
    public void close() throws IOException {
        this.freeMergedBuffer();
        this._in.close();
    }
    
    @Override
    public void mark(final int readlimit) {
        if (this._buffer == null) {
            this._in.mark(readlimit);
        }
    }
    
    @Override
    public boolean markSupported() {
        return this._buffer == null && this._in.markSupported();
    }
    
    @Override
    public int read() throws IOException {
        if (this._buffer != null) {
            final int c = this._buffer[this._ptr++] & 0xFF;
            if (this._ptr >= this._end) {
                this.freeMergedBuffer();
            }
            return c;
        }
        return this._in.read();
    }
    
    @Override
    public int read(final byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }
    
    @Override
    public int read(final byte[] b, final int off, int len) throws IOException {
        if (this._buffer != null) {
            final int avail = this._end - this._ptr;
            if (len > avail) {
                len = avail;
            }
            System.arraycopy(this._buffer, this._ptr, b, off, len);
            this._ptr += len;
            if (this._ptr >= this._end) {
                this.freeMergedBuffer();
            }
            return len;
        }
        return this._in.read(b, off, len);
    }
    
    @Override
    public void reset() throws IOException {
        if (this._buffer == null) {
            this._in.reset();
        }
    }
    
    @Override
    public long skip(long n) throws IOException {
        long count = 0L;
        if (this._buffer != null) {
            final int amount = this._end - this._ptr;
            if (amount > n) {
                this._ptr += (int)n;
                return n;
            }
            this.freeMergedBuffer();
            count += amount;
            n -= amount;
        }
        if (n > 0L) {
            count += this._in.skip(n);
        }
        return count;
    }
    
    private void freeMergedBuffer() {
        final byte[] buf = this._buffer;
        if (buf != null) {
            this._buffer = null;
            if (this._context != null) {
                this._context.releaseReadIOBuffer(buf);
            }
        }
    }
}
