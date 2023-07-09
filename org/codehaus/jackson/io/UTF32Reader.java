// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.io;

import java.io.CharConversionException;
import java.io.IOException;
import java.io.InputStream;

public class UTF32Reader extends BaseReader
{
    protected final boolean _bigEndian;
    protected char _surrogate;
    protected int _charCount;
    protected int _byteCount;
    protected final boolean _managedBuffers;
    
    public UTF32Reader(final IOContext ctxt, final InputStream in, final byte[] buf, final int ptr, final int len, final boolean isBigEndian) {
        super(ctxt, in, buf, ptr, len);
        this._surrogate = '\0';
        this._charCount = 0;
        this._byteCount = 0;
        this._bigEndian = isBigEndian;
        this._managedBuffers = (in != null);
    }
    
    @Override
    public int read(final char[] cbuf, final int start, int len) throws IOException {
        if (this._buffer == null) {
            return -1;
        }
        if (len < 1) {
            return len;
        }
        if (start < 0 || start + len > cbuf.length) {
            this.reportBounds(cbuf, start, len);
        }
        len += start;
        int outPtr = start;
        if (this._surrogate != '\0') {
            cbuf[outPtr++] = this._surrogate;
            this._surrogate = '\0';
        }
        else {
            final int left = this._length - this._ptr;
            if (left < 4 && !this.loadMore(left)) {
                return -1;
            }
        }
        while (outPtr < len) {
            final int ptr = this._ptr;
            int ch;
            if (this._bigEndian) {
                ch = (this._buffer[ptr] << 24 | (this._buffer[ptr + 1] & 0xFF) << 16 | (this._buffer[ptr + 2] & 0xFF) << 8 | (this._buffer[ptr + 3] & 0xFF));
            }
            else {
                ch = ((this._buffer[ptr] & 0xFF) | (this._buffer[ptr + 1] & 0xFF) << 8 | (this._buffer[ptr + 2] & 0xFF) << 16 | this._buffer[ptr + 3] << 24);
            }
            this._ptr += 4;
            if (ch > 65535) {
                if (ch > 1114111) {
                    this.reportInvalid(ch, outPtr - start, "(above " + Integer.toHexString(1114111) + ") ");
                }
                ch -= 65536;
                cbuf[outPtr++] = (char)(55296 + (ch >> 10));
                ch = (0xDC00 | (ch & 0x3FF));
                if (outPtr >= len) {
                    this._surrogate = (char)ch;
                    break;
                }
            }
            cbuf[outPtr++] = (char)ch;
            if (this._ptr >= this._length) {
                break;
            }
        }
        len = outPtr - start;
        this._charCount += len;
        return len;
    }
    
    private void reportUnexpectedEOF(final int gotBytes, final int needed) throws IOException {
        final int bytePos = this._byteCount + gotBytes;
        final int charPos = this._charCount;
        throw new CharConversionException("Unexpected EOF in the middle of a 4-byte UTF-32 char: got " + gotBytes + ", needed " + needed + ", at char #" + charPos + ", byte #" + bytePos + ")");
    }
    
    private void reportInvalid(final int value, final int offset, final String msg) throws IOException {
        final int bytePos = this._byteCount + this._ptr - 1;
        final int charPos = this._charCount + offset;
        throw new CharConversionException("Invalid UTF-32 character 0x" + Integer.toHexString(value) + msg + " at char #" + charPos + ", byte #" + bytePos + ")");
    }
    
    private boolean loadMore(final int available) throws IOException {
        this._byteCount += this._length - available;
        if (available > 0) {
            if (this._ptr > 0) {
                for (int i = 0; i < available; ++i) {
                    this._buffer[i] = this._buffer[this._ptr + i];
                }
                this._ptr = 0;
            }
            this._length = available;
        }
        else {
            this._ptr = 0;
            final int count = (this._in == null) ? -1 : this._in.read(this._buffer);
            if (count < 1) {
                this._length = 0;
                if (count < 0) {
                    if (this._managedBuffers) {
                        this.freeBuffers();
                    }
                    return false;
                }
                this.reportStrangeStream();
            }
            this._length = count;
        }
        while (this._length < 4) {
            final int count = (this._in == null) ? -1 : this._in.read(this._buffer, this._length, this._buffer.length - this._length);
            if (count < 1) {
                if (count < 0) {
                    if (this._managedBuffers) {
                        this.freeBuffers();
                    }
                    this.reportUnexpectedEOF(this._length, 4);
                }
                this.reportStrangeStream();
            }
            this._length += count;
        }
        return true;
    }
}
