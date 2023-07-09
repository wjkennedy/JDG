// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

import org.postgresql.largeobject.LargeObject;
import java.io.InputStream;
import java.sql.SQLException;
import org.postgresql.core.BaseConnection;
import java.sql.Blob;

public class PgBlob extends AbstractBlobClob implements Blob
{
    public PgBlob(final BaseConnection conn, final long oid) throws SQLException {
        super(conn, oid);
    }
    
    @Override
    public synchronized InputStream getBinaryStream(final long pos, final long length) throws SQLException {
        this.checkFreed();
        final LargeObject subLO = this.getLo(false).copy();
        this.addSubLO(subLO);
        if (pos > 2147483647L) {
            subLO.seek64(pos - 1L, 0);
        }
        else {
            subLO.seek((int)pos - 1, 0);
        }
        return subLO.getInputStream(length);
    }
    
    @Override
    public synchronized int setBytes(final long pos, final byte[] bytes) throws SQLException {
        return this.setBytes(pos, bytes, 0, bytes.length);
    }
    
    @Override
    public synchronized int setBytes(final long pos, final byte[] bytes, final int offset, final int len) throws SQLException {
        this.assertPosition(pos);
        this.getLo(true).seek((int)(pos - 1L));
        this.getLo(true).write(bytes, offset, len);
        return len;
    }
}
