// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core.v3;

import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import org.postgresql.util.ByteStreamWriter;
import java.sql.SQLException;
import org.postgresql.copy.CopyIn;

public class CopyInImpl extends CopyOperationImpl implements CopyIn
{
    @Override
    public void writeToCopy(final byte[] data, final int off, final int siz) throws SQLException {
        this.getQueryExecutor().writeToCopy(this, data, off, siz);
    }
    
    @Override
    public void writeToCopy(final ByteStreamWriter from) throws SQLException {
        this.getQueryExecutor().writeToCopy(this, from);
    }
    
    @Override
    public void flushCopy() throws SQLException {
        this.getQueryExecutor().flushCopy(this);
    }
    
    @Override
    public long endCopy() throws SQLException {
        return this.getQueryExecutor().endCopy(this);
    }
    
    @Override
    protected void handleCopydata(final byte[] data) throws PSQLException {
        throw new PSQLException(GT.tr("CopyIn copy direction can't receive data", new Object[0]), PSQLState.PROTOCOL_VIOLATION);
    }
}
