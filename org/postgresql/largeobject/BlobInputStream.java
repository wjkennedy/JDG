// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.largeobject;

import java.sql.SQLException;
import java.io.IOException;
import java.io.InputStream;

public class BlobInputStream extends InputStream
{
    private LargeObject lo;
    private long apos;
    private byte[] buffer;
    private int bpos;
    private int bsize;
    private long mpos;
    private long limit;
    
    public BlobInputStream(final LargeObject lo) {
        this(lo, 1024);
    }
    
    public BlobInputStream(final LargeObject lo, final int bsize) {
        this(lo, bsize, -1L);
    }
    
    public BlobInputStream(final LargeObject lo, final int bsize, final long limit) {
        this.mpos = 0L;
        this.limit = -1L;
        this.lo = lo;
        this.buffer = null;
        this.bpos = 0;
        this.apos = 0L;
        this.bsize = bsize;
        this.limit = limit;
    }
    
    @Override
    public int read() throws IOException {
        final LargeObject lo = this.getLo();
        try {
            if (this.limit > 0L && this.apos >= this.limit) {
                return -1;
            }
            if (this.buffer == null || this.bpos >= this.buffer.length) {
                this.buffer = lo.read(this.bsize);
                this.bpos = 0;
            }
            if (this.buffer == null || this.bpos >= this.buffer.length) {
                return -1;
            }
            int ret = this.buffer[this.bpos] & 0x7F;
            if ((this.buffer[this.bpos] & 0x80) == 0x80) {
                ret |= 0x80;
            }
            ++this.bpos;
            ++this.apos;
            return ret;
        }
        catch (final SQLException se) {
            throw new IOException(se.toString());
        }
    }
    
    @Override
    public void close() throws IOException {
        if (this.lo != null) {
            try {
                this.lo.close();
                this.lo = null;
            }
            catch (final SQLException se) {
                throw new IOException(se.toString());
            }
        }
    }
    
    @Override
    public synchronized void mark(final int readlimit) {
        this.mpos = this.apos;
    }
    
    @Override
    public synchronized void reset() throws IOException {
        final LargeObject lo = this.getLo();
        try {
            if (this.mpos <= 2147483647L) {
                lo.seek((int)this.mpos);
            }
            else {
                lo.seek64(this.mpos, 0);
            }
            this.buffer = null;
            this.apos = this.mpos;
        }
        catch (final SQLException se) {
            throw new IOException(se.toString());
        }
    }
    
    @Override
    public boolean markSupported() {
        return true;
    }
    
    private LargeObject getLo() throws IOException {
        if (this.lo == null) {
            throw new IOException("BlobOutputStream is closed");
        }
        return this.lo;
    }
}
