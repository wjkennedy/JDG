// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.io;

import java.io.IOException;
import org.codehaus.jackson.util.BufferRecycler;
import org.codehaus.jackson.util.TextBuffer;
import java.io.Writer;

public final class SegmentedStringWriter extends Writer
{
    protected final TextBuffer _buffer;
    
    public SegmentedStringWriter(final BufferRecycler br) {
        this._buffer = new TextBuffer(br);
    }
    
    @Override
    public Writer append(final char c) {
        this.write(c);
        return this;
    }
    
    @Override
    public Writer append(final CharSequence csq) {
        final String str = csq.toString();
        this._buffer.append(str, 0, str.length());
        return this;
    }
    
    @Override
    public Writer append(final CharSequence csq, final int start, final int end) {
        final String str = csq.subSequence(start, end).toString();
        this._buffer.append(str, 0, str.length());
        return this;
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public void flush() {
    }
    
    @Override
    public void write(final char[] cbuf) {
        this._buffer.append(cbuf, 0, cbuf.length);
    }
    
    @Override
    public void write(final char[] cbuf, final int off, final int len) {
        this._buffer.append(cbuf, off, len);
    }
    
    @Override
    public void write(final int c) {
        this._buffer.append((char)c);
    }
    
    @Override
    public void write(final String str) {
        this._buffer.append(str, 0, str.length());
    }
    
    @Override
    public void write(final String str, final int off, final int len) {
        this._buffer.append(str, off, len);
    }
    
    public String getAndClear() {
        final String result = this._buffer.contentsAsString();
        this._buffer.releaseBuffers();
        return result;
    }
}
