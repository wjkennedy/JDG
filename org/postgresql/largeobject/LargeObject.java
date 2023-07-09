// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.largeobject;

import java.io.OutputStream;
import java.io.InputStream;
import org.postgresql.util.internal.Nullness;
import java.io.IOException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import java.sql.SQLException;
import org.postgresql.fastpath.FastpathArg;
import org.postgresql.core.BaseConnection;
import org.postgresql.fastpath.Fastpath;

public class LargeObject implements AutoCloseable
{
    public static final int SEEK_SET = 0;
    public static final int SEEK_CUR = 1;
    public static final int SEEK_END = 2;
    private final Fastpath fp;
    private final long oid;
    private final int mode;
    private final int fd;
    private BlobOutputStream os;
    private boolean closed;
    private BaseConnection conn;
    private final boolean commitOnClose;
    
    protected LargeObject(final Fastpath fp, final long oid, final int mode, final BaseConnection conn, final boolean commitOnClose) throws SQLException {
        this.closed = false;
        this.fp = fp;
        this.oid = oid;
        this.mode = mode;
        if (commitOnClose) {
            this.commitOnClose = true;
            this.conn = conn;
        }
        else {
            this.commitOnClose = false;
        }
        final FastpathArg[] args = { Fastpath.createOIDArg(oid), new FastpathArg(mode) };
        this.fd = fp.getInteger("lo_open", args);
    }
    
    protected LargeObject(final Fastpath fp, final long oid, final int mode) throws SQLException {
        this(fp, oid, mode, null, false);
    }
    
    public LargeObject copy() throws SQLException {
        return new LargeObject(this.fp, this.oid, this.mode);
    }
    
    @Deprecated
    public int getOID() {
        return (int)this.oid;
    }
    
    public long getLongOID() {
        return this.oid;
    }
    
    @Override
    public void close() throws SQLException {
        if (!this.closed) {
            if (this.os != null) {
                try {
                    this.os.flush();
                }
                catch (final IOException ioe) {
                    throw new PSQLException("Exception flushing output stream", PSQLState.DATA_ERROR, ioe);
                }
                finally {
                    this.os = null;
                }
            }
            final FastpathArg[] args = { new FastpathArg(this.fd) };
            this.fp.fastpath("lo_close", args);
            this.closed = true;
            final BaseConnection conn = this.conn;
            if (this.commitOnClose && conn != null) {
                conn.commit();
            }
        }
    }
    
    public byte[] read(final int len) throws SQLException {
        final FastpathArg[] args = { new FastpathArg(this.fd), new FastpathArg(len) };
        return Nullness.castNonNull(this.fp.getData("loread", args));
    }
    
    public int read(final byte[] buf, final int off, int len) throws SQLException {
        final byte[] b = this.read(len);
        if (b == null) {
            return 0;
        }
        if (b.length < len) {
            len = b.length;
        }
        System.arraycopy(b, 0, buf, off, len);
        return len;
    }
    
    public void write(final byte[] buf) throws SQLException {
        final FastpathArg[] args = { new FastpathArg(this.fd), new FastpathArg(buf) };
        this.fp.fastpath("lowrite", args);
    }
    
    public void write(final byte[] buf, final int off, final int len) throws SQLException {
        final FastpathArg[] args = { new FastpathArg(this.fd), new FastpathArg(buf, off, len) };
        this.fp.fastpath("lowrite", args);
    }
    
    public void seek(final int pos, final int ref) throws SQLException {
        final FastpathArg[] args = { new FastpathArg(this.fd), new FastpathArg(pos), new FastpathArg(ref) };
        this.fp.fastpath("lo_lseek", args);
    }
    
    public void seek64(final long pos, final int ref) throws SQLException {
        final FastpathArg[] args = { new FastpathArg(this.fd), new FastpathArg(pos), new FastpathArg(ref) };
        this.fp.fastpath("lo_lseek64", args);
    }
    
    public void seek(final int pos) throws SQLException {
        this.seek(pos, 0);
    }
    
    public int tell() throws SQLException {
        final FastpathArg[] args = { new FastpathArg(this.fd) };
        return this.fp.getInteger("lo_tell", args);
    }
    
    public long tell64() throws SQLException {
        final FastpathArg[] args = { new FastpathArg(this.fd) };
        return this.fp.getLong("lo_tell64", args);
    }
    
    public int size() throws SQLException {
        final int cp = this.tell();
        this.seek(0, 2);
        final int sz = this.tell();
        this.seek(cp, 0);
        return sz;
    }
    
    public long size64() throws SQLException {
        final long cp = this.tell64();
        this.seek64(0L, 2);
        final long sz = this.tell64();
        this.seek64(cp, 0);
        return sz;
    }
    
    public void truncate(final int len) throws SQLException {
        final FastpathArg[] args = { new FastpathArg(this.fd), new FastpathArg(len) };
        this.fp.getInteger("lo_truncate", args);
    }
    
    public void truncate64(final long len) throws SQLException {
        final FastpathArg[] args = { new FastpathArg(this.fd), new FastpathArg(len) };
        this.fp.getInteger("lo_truncate64", args);
    }
    
    public InputStream getInputStream() throws SQLException {
        return new BlobInputStream(this, 4096);
    }
    
    public InputStream getInputStream(final long limit) throws SQLException {
        return new BlobInputStream(this, 4096, limit);
    }
    
    public OutputStream getOutputStream() throws SQLException {
        if (this.os == null) {
            this.os = new BlobOutputStream(this, 4096);
        }
        return this.os;
    }
}
