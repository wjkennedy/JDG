// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import java.io.EOFException;
import java.net.SocketTimeoutException;
import java.io.IOException;
import java.io.InputStream;

public class VisibleBufferedInputStream extends InputStream
{
    private static final int MINIMUM_READ = 1024;
    private static final int STRING_SCAN_SPAN = 1024;
    private final InputStream wrapped;
    private byte[] buffer;
    private int index;
    private int endIndex;
    private boolean timeoutRequested;
    
    public VisibleBufferedInputStream(final InputStream in, final int bufferSize) {
        this.timeoutRequested = false;
        this.wrapped = in;
        this.buffer = new byte[(bufferSize < 1024) ? 1024 : bufferSize];
    }
    
    @Override
    public int read() throws IOException {
        if (this.ensureBytes(1)) {
            return this.buffer[this.index++] & 0xFF;
        }
        return -1;
    }
    
    public int peek() throws IOException {
        if (this.ensureBytes(1)) {
            return this.buffer[this.index] & 0xFF;
        }
        return -1;
    }
    
    public byte readRaw() {
        return this.buffer[this.index++];
    }
    
    public boolean ensureBytes(final int n) throws IOException {
        return this.ensureBytes(n, true);
    }
    
    public boolean ensureBytes(final int n, final boolean block) throws IOException {
        for (int required = n - this.endIndex + this.index; required > 0; required = n - this.endIndex + this.index) {
            if (!this.readMore(required, block)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean readMore(final int wanted, final boolean block) throws IOException {
        if (this.endIndex == this.index) {
            this.index = 0;
            this.endIndex = 0;
        }
        int canFit = this.buffer.length - this.endIndex;
        if (canFit < wanted) {
            if (this.index + canFit > wanted + 1024) {
                this.compact();
            }
            else {
                this.doubleBuffer();
            }
            canFit = this.buffer.length - this.endIndex;
        }
        int read = 0;
        try {
            read = this.wrapped.read(this.buffer, this.endIndex, canFit);
            if (!block && read == 0) {
                return false;
            }
        }
        catch (final SocketTimeoutException e) {
            if (!block) {
                return false;
            }
            if (this.timeoutRequested) {
                throw e;
            }
        }
        if (read < 0) {
            return false;
        }
        this.endIndex += read;
        return true;
    }
    
    private void doubleBuffer() {
        final byte[] buf = new byte[this.buffer.length * 2];
        this.moveBufferTo(buf);
        this.buffer = buf;
    }
    
    private void compact() {
        this.moveBufferTo(this.buffer);
    }
    
    private void moveBufferTo(final byte[] dest) {
        final int size = this.endIndex - this.index;
        System.arraycopy(this.buffer, this.index, dest, 0, size);
        this.index = 0;
        this.endIndex = size;
    }
    
    @Override
    public int read(final byte[] to, int off, int len) throws IOException {
        if ((off | len | off + len | to.length - (off + len)) < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return 0;
        }
        int avail = this.endIndex - this.index;
        if (len - avail < 1024) {
            this.ensureBytes(len);
            avail = this.endIndex - this.index;
        }
        if (avail > 0) {
            if (len <= avail) {
                System.arraycopy(this.buffer, this.index, to, off, len);
                this.index += len;
                return len;
            }
            System.arraycopy(this.buffer, this.index, to, off, avail);
            len -= avail;
            off += avail;
        }
        int read = avail;
        this.index = 0;
        this.endIndex = 0;
        while (true) {
            int r;
            try {
                r = this.wrapped.read(to, off, len);
            }
            catch (final SocketTimeoutException e) {
                if (read == 0 && this.timeoutRequested) {
                    throw e;
                }
                return read;
            }
            if (r <= 0) {
                return (read == 0) ? r : read;
            }
            read += r;
            off += r;
            len -= r;
            if (len <= 0) {
                return read;
            }
        }
    }
    
    @Override
    public long skip(long n) throws IOException {
        final int avail = this.endIndex - this.index;
        if (avail >= n) {
            this.index += (int)n;
            return n;
        }
        n -= avail;
        this.index = 0;
        this.endIndex = 0;
        return avail + this.wrapped.skip(n);
    }
    
    @Override
    public int available() throws IOException {
        final int avail = this.endIndex - this.index;
        return (avail > 0) ? avail : this.wrapped.available();
    }
    
    @Override
    public void close() throws IOException {
        this.wrapped.close();
    }
    
    public byte[] getBuffer() {
        return this.buffer;
    }
    
    public int getIndex() {
        return this.index;
    }
    
    public int scanCStringLength() throws IOException {
        int pos = this.index;
        while (true) {
            if (pos < this.endIndex) {
                if (this.buffer[pos++] == 0) {
                    return pos - this.index;
                }
                continue;
            }
            else {
                if (!this.readMore(1024, true)) {
                    throw new EOFException();
                }
                pos = this.index;
            }
        }
    }
    
    public void setTimeoutRequested(final boolean timeoutRequested) {
        this.timeoutRequested = timeoutRequested;
    }
    
    public InputStream getWrapped() {
        return this.wrapped;
    }
}
