// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core.v3;

import java.sql.SQLException;
import org.postgresql.copy.CopyOut;

public class CopyOutImpl extends CopyOperationImpl implements CopyOut
{
    private byte[] currentDataRow;
    
    @Override
    public byte[] readFromCopy() throws SQLException {
        return this.readFromCopy(true);
    }
    
    @Override
    public byte[] readFromCopy(final boolean block) throws SQLException {
        this.currentDataRow = null;
        this.getQueryExecutor().readFromCopy(this, block);
        return this.currentDataRow;
    }
    
    @Override
    protected void handleCopydata(final byte[] data) {
        this.currentDataRow = data;
    }
}
