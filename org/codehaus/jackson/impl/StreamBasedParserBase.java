// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.impl;

import org.codehaus.jackson.JsonParser;
import java.io.IOException;
import java.io.OutputStream;
import org.codehaus.jackson.io.IOContext;
import java.io.InputStream;

@Deprecated
public abstract class StreamBasedParserBase extends JsonParserBase
{
    protected InputStream _inputStream;
    protected byte[] _inputBuffer;
    protected boolean _bufferRecyclable;
    
    protected StreamBasedParserBase(final IOContext ctxt, final int features, final InputStream in, final byte[] inputBuffer, final int start, final int end, final boolean bufferRecyclable) {
        super(ctxt, features);
        this._inputStream = in;
        this._inputBuffer = inputBuffer;
        this._inputPtr = start;
        this._inputEnd = end;
        this._bufferRecyclable = bufferRecyclable;
    }
    
    @Override
    public int releaseBuffered(final OutputStream out) throws IOException {
        final int count = this._inputEnd - this._inputPtr;
        if (count < 1) {
            return 0;
        }
        final int origPtr = this._inputPtr;
        out.write(this._inputBuffer, origPtr, count);
        return count;
    }
    
    @Override
    public Object getInputSource() {
        return this._inputStream;
    }
    
    @Override
    protected final boolean loadMore() throws IOException {
        this._currInputProcessed += this._inputEnd;
        this._currInputRowStart -= this._inputEnd;
        if (this._inputStream != null) {
            final int count = this._inputStream.read(this._inputBuffer, 0, this._inputBuffer.length);
            if (count > 0) {
                this._inputPtr = 0;
                this._inputEnd = count;
                return true;
            }
            this._closeInput();
            if (count == 0) {
                throw new IOException("InputStream.read() returned 0 characters when trying to read " + this._inputBuffer.length + " bytes");
            }
        }
        return false;
    }
    
    protected final boolean _loadToHaveAtLeast(final int minAvailable) throws IOException {
        if (this._inputStream == null) {
            return false;
        }
        final int amount = this._inputEnd - this._inputPtr;
        if (amount > 0 && this._inputPtr > 0) {
            this._currInputProcessed += this._inputPtr;
            this._currInputRowStart -= this._inputPtr;
            System.arraycopy(this._inputBuffer, this._inputPtr, this._inputBuffer, 0, amount);
            this._inputEnd = amount;
        }
        else {
            this._inputEnd = 0;
        }
        this._inputPtr = 0;
        while (this._inputEnd < minAvailable) {
            final int count = this._inputStream.read(this._inputBuffer, this._inputEnd, this._inputBuffer.length - this._inputEnd);
            if (count < 1) {
                this._closeInput();
                if (count == 0) {
                    throw new IOException("InputStream.read() returned 0 characters when trying to read " + amount + " bytes");
                }
                return false;
            }
            else {
                this._inputEnd += count;
            }
        }
        return true;
    }
    
    @Override
    protected void _closeInput() throws IOException {
        if (this._inputStream != null) {
            if (this._ioContext.isResourceManaged() || this.isEnabled(Feature.AUTO_CLOSE_SOURCE)) {
                this._inputStream.close();
            }
            this._inputStream = null;
        }
    }
    
    @Override
    protected void _releaseBuffers() throws IOException {
        super._releaseBuffers();
        if (this._bufferRecyclable) {
            final byte[] buf = this._inputBuffer;
            if (buf != null) {
                this._inputBuffer = null;
                this._ioContext.releaseReadIOBuffer(buf);
            }
        }
    }
}
