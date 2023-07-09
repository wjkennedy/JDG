// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.copy;

import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import java.util.Arrays;
import java.io.IOException;
import org.postgresql.util.GT;
import org.postgresql.util.internal.Nullness;
import java.sql.SQLException;
import org.postgresql.PGConnection;
import java.io.InputStream;

public class PGCopyInputStream extends InputStream implements CopyOut
{
    private CopyOut op;
    private byte[] buf;
    private int at;
    private int len;
    
    public PGCopyInputStream(final PGConnection connection, final String sql) throws SQLException {
        this(connection.getCopyAPI().copyOut(sql));
    }
    
    public PGCopyInputStream(final CopyOut op) {
        this.op = op;
    }
    
    private CopyOut getOp() {
        return Nullness.castNonNull(this.op);
    }
    
    private byte[] fillBuffer() throws IOException {
        if (this.at >= this.len) {
            try {
                this.buf = this.getOp().readFromCopy();
            }
            catch (final SQLException sqle) {
                throw new IOException(GT.tr("Copying from database failed: {0}", sqle.getMessage()), sqle);
            }
            if (this.buf == null) {
                this.at = -1;
            }
            else {
                this.at = 0;
                this.len = this.buf.length;
            }
        }
        return this.buf;
    }
    
    private void checkClosed() throws IOException {
        if (this.op == null) {
            throw new IOException(GT.tr("This copy stream is closed.", new Object[0]));
        }
    }
    
    @Override
    public int available() throws IOException {
        this.checkClosed();
        return (this.buf != null) ? (this.len - this.at) : 0;
    }
    
    @Override
    public int read() throws IOException {
        this.checkClosed();
        final byte[] buf = this.fillBuffer();
        return (buf != null) ? (buf[this.at++] & 0xFF) : -1;
    }
    
    @Override
    public int read(final byte[] buf) throws IOException {
        return this.read(buf, 0, buf.length);
    }
    
    @Override
    public int read(final byte[] buf, final int off, final int siz) throws IOException {
        this.checkClosed();
        int got;
        byte[] data;
        int length;
        for (got = 0, data = this.fillBuffer(); got < siz && data != null; got += length, data = this.fillBuffer()) {
            length = Math.min(siz - got, this.len - this.at);
            System.arraycopy(data, this.at, buf, off + got, length);
            this.at += length;
        }
        return (got == 0 && data == null) ? -1 : got;
    }
    
    @Override
    public byte[] readFromCopy() throws SQLException {
        byte[] result = null;
        try {
            final byte[] buf = this.fillBuffer();
            if (buf != null) {
                if (this.at > 0 || this.len < buf.length) {
                    result = Arrays.copyOfRange(buf, this.at, this.len);
                }
                else {
                    result = buf;
                }
                this.at = this.len;
            }
        }
        catch (final IOException ioe) {
            throw new PSQLException(GT.tr("Read from copy failed.", new Object[0]), PSQLState.CONNECTION_FAILURE, ioe);
        }
        return result;
    }
    
    @Override
    public byte[] readFromCopy(final boolean block) throws SQLException {
        return this.readFromCopy();
    }
    
    @Override
    public void close() throws IOException {
        if (this.op == null) {
            return;
        }
        if (this.op.isActive()) {
            try {
                this.op.cancelCopy();
            }
            catch (final SQLException se) {
                throw new IOException("Failed to close copy reader.", se);
            }
        }
        this.op = null;
    }
    
    @Override
    public void cancelCopy() throws SQLException {
        this.getOp().cancelCopy();
    }
    
    @Override
    public int getFormat() {
        return this.getOp().getFormat();
    }
    
    @Override
    public int getFieldFormat(final int field) {
        return this.getOp().getFieldFormat(field);
    }
    
    @Override
    public int getFieldCount() {
        return this.getOp().getFieldCount();
    }
    
    @Override
    public boolean isActive() {
        return this.op != null && this.op.isActive();
    }
    
    @Override
    public long getHandledRowCount() {
        return this.getOp().getHandledRowCount();
    }
}
