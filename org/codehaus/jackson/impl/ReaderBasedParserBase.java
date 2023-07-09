// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.impl;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonParseException;
import java.io.IOException;
import java.io.Writer;
import org.codehaus.jackson.io.IOContext;
import java.io.Reader;

@Deprecated
public abstract class ReaderBasedParserBase extends JsonParserBase
{
    protected Reader _reader;
    protected char[] _inputBuffer;
    
    protected ReaderBasedParserBase(final IOContext ctxt, final int features, final Reader r) {
        super(ctxt, features);
        this._reader = r;
        this._inputBuffer = ctxt.allocTokenBuffer();
    }
    
    @Override
    public int releaseBuffered(final Writer w) throws IOException {
        final int count = this._inputEnd - this._inputPtr;
        if (count < 1) {
            return 0;
        }
        final int origPtr = this._inputPtr;
        w.write(this._inputBuffer, origPtr, count);
        return count;
    }
    
    @Override
    public Object getInputSource() {
        return this._reader;
    }
    
    @Override
    protected final boolean loadMore() throws IOException {
        this._currInputProcessed += this._inputEnd;
        this._currInputRowStart -= this._inputEnd;
        if (this._reader != null) {
            final int count = this._reader.read(this._inputBuffer, 0, this._inputBuffer.length);
            if (count > 0) {
                this._inputPtr = 0;
                this._inputEnd = count;
                return true;
            }
            this._closeInput();
            if (count == 0) {
                throw new IOException("Reader returned 0 characters when trying to read " + this._inputEnd);
            }
        }
        return false;
    }
    
    protected char getNextChar(final String eofMsg) throws IOException, JsonParseException {
        if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
            this._reportInvalidEOF(eofMsg);
        }
        return this._inputBuffer[this._inputPtr++];
    }
    
    @Override
    protected void _closeInput() throws IOException {
        if (this._reader != null) {
            if (this._ioContext.isResourceManaged() || this.isEnabled(Feature.AUTO_CLOSE_SOURCE)) {
                this._reader.close();
            }
            this._reader = null;
        }
    }
    
    @Override
    protected void _releaseBuffers() throws IOException {
        super._releaseBuffers();
        final char[] buf = this._inputBuffer;
        if (buf != null) {
            this._inputBuffer = null;
            this._ioContext.releaseTokenBuffer(buf);
        }
    }
    
    protected final boolean _matchToken(final String matchStr, int i) throws IOException, JsonParseException {
        final int len = matchStr.length();
        do {
            if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
                this._reportInvalidEOFInValue();
            }
            if (this._inputBuffer[this._inputPtr] != matchStr.charAt(i)) {
                this._reportInvalidToken(matchStr.substring(0, i), "'null', 'true', 'false' or NaN");
            }
            ++this._inputPtr;
        } while (++i < len);
        if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
            return true;
        }
        final char c = this._inputBuffer[this._inputPtr];
        if (Character.isJavaIdentifierPart(c)) {
            ++this._inputPtr;
            this._reportInvalidToken(matchStr.substring(0, i), "'null', 'true', 'false' or NaN");
        }
        return true;
    }
    
    protected void _reportInvalidToken(final String matchedPart, final String msg) throws IOException, JsonParseException {
        final StringBuilder sb = new StringBuilder(matchedPart);
        while (true) {
            while (this._inputPtr < this._inputEnd || this.loadMore()) {
                final char c = this._inputBuffer[this._inputPtr];
                if (!Character.isJavaIdentifierPart(c)) {
                    this._reportError("Unrecognized token '" + sb.toString() + "': was expecting ");
                    return;
                }
                ++this._inputPtr;
                sb.append(c);
            }
            continue;
        }
    }
}
