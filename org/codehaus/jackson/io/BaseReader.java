// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

abstract class BaseReader extends Reader
{
    protected static final int LAST_VALID_UNICODE_CHAR = 1114111;
    protected static final char NULL_CHAR = '\0';
    protected static final char NULL_BYTE = '\0';
    protected final IOContext _context;
    protected InputStream _in;
    protected byte[] _buffer;
    protected int _ptr;
    protected int _length;
    protected char[] _tmpBuf;
    
    protected BaseReader(final IOContext context, final InputStream in, final byte[] buf, final int ptr, final int len) {
        this._tmpBuf = null;
        this._context = context;
        this._in = in;
        this._buffer = buf;
        this._ptr = ptr;
        this._length = len;
    }
    
    @Override
    public void close() throws IOException {
        final InputStream in = this._in;
        if (in != null) {
            this._in = null;
            this.freeBuffers();
            in.close();
        }
    }
    
    @Override
    public int read() throws IOException {
        if (this._tmpBuf == null) {
            this._tmpBuf = new char[1];
        }
        if (this.read(this._tmpBuf, 0, 1) < 1) {
            return -1;
        }
        return this._tmpBuf[0];
    }
    
    public final void freeBuffers() {
        final byte[] buf = this._buffer;
        if (buf != null) {
            this._buffer = null;
            this._context.releaseReadIOBuffer(buf);
        }
    }
    
    protected void reportBounds(final char[] cbuf, final int start, final int len) throws IOException {
        throw new ArrayIndexOutOfBoundsException("read(buf," + start + "," + len + "), cbuf[" + cbuf.length + "]");
    }
    
    protected void reportStrangeStream() throws IOException {
        throw new IOException("Strange I/O stream, returned 0 bytes on read");
    }
}
