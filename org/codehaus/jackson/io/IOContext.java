// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.io;

import org.codehaus.jackson.util.TextBuffer;
import org.codehaus.jackson.util.BufferRecycler;
import org.codehaus.jackson.JsonEncoding;

public final class IOContext
{
    protected final Object _sourceRef;
    protected JsonEncoding _encoding;
    protected final boolean _managedResource;
    protected final BufferRecycler _bufferRecycler;
    protected byte[] _readIOBuffer;
    protected byte[] _writeEncodingBuffer;
    protected char[] _tokenCBuffer;
    protected char[] _concatCBuffer;
    protected char[] _nameCopyBuffer;
    
    public IOContext(final BufferRecycler br, final Object sourceRef, final boolean managedResource) {
        this._readIOBuffer = null;
        this._writeEncodingBuffer = null;
        this._tokenCBuffer = null;
        this._concatCBuffer = null;
        this._nameCopyBuffer = null;
        this._bufferRecycler = br;
        this._sourceRef = sourceRef;
        this._managedResource = managedResource;
    }
    
    public void setEncoding(final JsonEncoding enc) {
        this._encoding = enc;
    }
    
    public final Object getSourceReference() {
        return this._sourceRef;
    }
    
    public final JsonEncoding getEncoding() {
        return this._encoding;
    }
    
    public final boolean isResourceManaged() {
        return this._managedResource;
    }
    
    public final TextBuffer constructTextBuffer() {
        return new TextBuffer(this._bufferRecycler);
    }
    
    public final byte[] allocReadIOBuffer() {
        if (this._readIOBuffer != null) {
            throw new IllegalStateException("Trying to call allocReadIOBuffer() second time");
        }
        return this._readIOBuffer = this._bufferRecycler.allocByteBuffer(BufferRecycler.ByteBufferType.READ_IO_BUFFER);
    }
    
    public final byte[] allocWriteEncodingBuffer() {
        if (this._writeEncodingBuffer != null) {
            throw new IllegalStateException("Trying to call allocWriteEncodingBuffer() second time");
        }
        return this._writeEncodingBuffer = this._bufferRecycler.allocByteBuffer(BufferRecycler.ByteBufferType.WRITE_ENCODING_BUFFER);
    }
    
    public final char[] allocTokenBuffer() {
        if (this._tokenCBuffer != null) {
            throw new IllegalStateException("Trying to call allocTokenBuffer() second time");
        }
        return this._tokenCBuffer = this._bufferRecycler.allocCharBuffer(BufferRecycler.CharBufferType.TOKEN_BUFFER);
    }
    
    public final char[] allocConcatBuffer() {
        if (this._concatCBuffer != null) {
            throw new IllegalStateException("Trying to call allocConcatBuffer() second time");
        }
        return this._concatCBuffer = this._bufferRecycler.allocCharBuffer(BufferRecycler.CharBufferType.CONCAT_BUFFER);
    }
    
    public final char[] allocNameCopyBuffer(final int minSize) {
        if (this._nameCopyBuffer != null) {
            throw new IllegalStateException("Trying to call allocNameCopyBuffer() second time");
        }
        return this._nameCopyBuffer = this._bufferRecycler.allocCharBuffer(BufferRecycler.CharBufferType.NAME_COPY_BUFFER, minSize);
    }
    
    public final void releaseReadIOBuffer(final byte[] buf) {
        if (buf != null) {
            if (buf != this._readIOBuffer) {
                throw new IllegalArgumentException("Trying to release buffer not owned by the context");
            }
            this._readIOBuffer = null;
            this._bufferRecycler.releaseByteBuffer(BufferRecycler.ByteBufferType.READ_IO_BUFFER, buf);
        }
    }
    
    public final void releaseWriteEncodingBuffer(final byte[] buf) {
        if (buf != null) {
            if (buf != this._writeEncodingBuffer) {
                throw new IllegalArgumentException("Trying to release buffer not owned by the context");
            }
            this._writeEncodingBuffer = null;
            this._bufferRecycler.releaseByteBuffer(BufferRecycler.ByteBufferType.WRITE_ENCODING_BUFFER, buf);
        }
    }
    
    public final void releaseTokenBuffer(final char[] buf) {
        if (buf != null) {
            if (buf != this._tokenCBuffer) {
                throw new IllegalArgumentException("Trying to release buffer not owned by the context");
            }
            this._tokenCBuffer = null;
            this._bufferRecycler.releaseCharBuffer(BufferRecycler.CharBufferType.TOKEN_BUFFER, buf);
        }
    }
    
    public final void releaseConcatBuffer(final char[] buf) {
        if (buf != null) {
            if (buf != this._concatCBuffer) {
                throw new IllegalArgumentException("Trying to release buffer not owned by the context");
            }
            this._concatCBuffer = null;
            this._bufferRecycler.releaseCharBuffer(BufferRecycler.CharBufferType.CONCAT_BUFFER, buf);
        }
    }
    
    public final void releaseNameCopyBuffer(final char[] buf) {
        if (buf != null) {
            if (buf != this._nameCopyBuffer) {
                throw new IllegalArgumentException("Trying to release buffer not owned by the context");
            }
            this._nameCopyBuffer = null;
            this._bufferRecycler.releaseCharBuffer(BufferRecycler.CharBufferType.NAME_COPY_BUFFER, buf);
        }
    }
}
