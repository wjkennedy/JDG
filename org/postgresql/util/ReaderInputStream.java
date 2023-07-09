// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import java.nio.charset.CharacterCodingException;
import java.io.IOException;
import java.nio.charset.CoderResult;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetEncoder;
import java.io.Reader;
import java.nio.charset.Charset;
import java.io.InputStream;

public class ReaderInputStream extends InputStream
{
    private static final int DEFAULT_CHAR_BUFFER_SIZE = 8192;
    private static final Charset UTF_8;
    private final Reader reader;
    private final CharsetEncoder encoder;
    private final ByteBuffer bbuf;
    private final CharBuffer cbuf;
    private boolean endOfInput;
    private final byte[] oneByte;
    
    public ReaderInputStream(final Reader reader) {
        this(reader, 8192);
    }
    
    ReaderInputStream(final Reader reader, final int charBufferSize) {
        this.oneByte = new byte[1];
        if (reader == null) {
            throw new IllegalArgumentException("reader cannot be null");
        }
        if (charBufferSize < 2) {
            throw new IllegalArgumentException("charBufferSize must be at least 2 chars");
        }
        this.reader = reader;
        this.encoder = ReaderInputStream.UTF_8.newEncoder();
        (this.bbuf = ByteBuffer.allocate(3 * charBufferSize)).flip();
        (this.cbuf = CharBuffer.allocate(charBufferSize)).flip();
    }
    
    private void advance() throws IOException {
        assert !this.endOfInput;
        assert !this.bbuf.hasRemaining() : "advance() should be called when output byte buffer is empty. bbuf: " + this.bbuf + ", as string: " + this.bbuf.asCharBuffer().toString();
        assert this.cbuf.remaining() < 2;
        if (this.cbuf.remaining() == 0) {
            this.cbuf.clear();
        }
        else {
            this.cbuf.compact();
        }
        final int n = this.reader.read(this.cbuf);
        this.cbuf.flip();
        this.endOfInput = (n == -1);
        this.bbuf.clear();
        CoderResult result = this.encoder.encode(this.cbuf, this.bbuf, this.endOfInput);
        this.checkEncodeResult(result);
        if (this.endOfInput) {
            result = this.encoder.flush(this.bbuf);
            this.checkEncodeResult(result);
        }
        this.bbuf.flip();
    }
    
    private void checkEncodeResult(final CoderResult result) throws CharacterCodingException {
        if (result.isError()) {
            result.throwException();
        }
    }
    
    @Override
    public int read() throws IOException {
        int res = 0;
        while (res != -1) {
            res = this.read(this.oneByte);
            if (res > 0) {
                return this.oneByte[0] & 0xFF;
            }
        }
        return -1;
    }
    
    @Override
    public int read(final byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return 0;
        }
        if (this.endOfInput && !this.bbuf.hasRemaining()) {
            return -1;
        }
        int totalRead = 0;
        while (len > 0 && !this.endOfInput) {
            if (this.bbuf.hasRemaining()) {
                final int remaining = Math.min(len, this.bbuf.remaining());
                this.bbuf.get(b, off, remaining);
                totalRead += remaining;
                off += remaining;
                len -= remaining;
                if (len == 0) {
                    return totalRead;
                }
            }
            this.advance();
        }
        if (this.endOfInput && !this.bbuf.hasRemaining() && totalRead == 0) {
            return -1;
        }
        return totalRead;
    }
    
    @Override
    public void close() throws IOException {
        this.endOfInput = true;
        this.reader.close();
    }
    
    static {
        UTF_8 = Charset.forName("UTF-8");
    }
}
