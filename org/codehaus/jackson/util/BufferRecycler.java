// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.util;

public class BufferRecycler
{
    public static final int DEFAULT_WRITE_CONCAT_BUFFER_LEN = 2000;
    protected final byte[][] _byteBuffers;
    protected final char[][] _charBuffers;
    
    public BufferRecycler() {
        this._byteBuffers = new byte[ByteBufferType.values().length][];
        this._charBuffers = new char[CharBufferType.values().length][];
    }
    
    public final byte[] allocByteBuffer(final ByteBufferType type) {
        final int ix = type.ordinal();
        byte[] buffer = this._byteBuffers[ix];
        if (buffer == null) {
            buffer = this.balloc(type.size);
        }
        else {
            this._byteBuffers[ix] = null;
        }
        return buffer;
    }
    
    public final void releaseByteBuffer(final ByteBufferType type, final byte[] buffer) {
        this._byteBuffers[type.ordinal()] = buffer;
    }
    
    public final char[] allocCharBuffer(final CharBufferType type) {
        return this.allocCharBuffer(type, 0);
    }
    
    public final char[] allocCharBuffer(final CharBufferType type, int minSize) {
        if (type.size > minSize) {
            minSize = type.size;
        }
        final int ix = type.ordinal();
        char[] buffer = this._charBuffers[ix];
        if (buffer == null || buffer.length < minSize) {
            buffer = this.calloc(minSize);
        }
        else {
            this._charBuffers[ix] = null;
        }
        return buffer;
    }
    
    public final void releaseCharBuffer(final CharBufferType type, final char[] buffer) {
        this._charBuffers[type.ordinal()] = buffer;
    }
    
    private final byte[] balloc(final int size) {
        return new byte[size];
    }
    
    private final char[] calloc(final int size) {
        return new char[size];
    }
    
    public enum ByteBufferType
    {
        READ_IO_BUFFER(4000), 
        WRITE_ENCODING_BUFFER(4000), 
        WRITE_CONCAT_BUFFER(2000);
        
        private final int size;
        
        private ByteBufferType(final int size) {
            this.size = size;
        }
    }
    
    public enum CharBufferType
    {
        TOKEN_BUFFER(2000), 
        CONCAT_BUFFER(2000), 
        TEXT_BUFFER(200), 
        NAME_COPY_BUFFER(200);
        
        private final int size;
        
        private CharBufferType(final int size) {
            this.size = size;
        }
    }
}
