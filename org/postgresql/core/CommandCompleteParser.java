// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;

public final class CommandCompleteParser
{
    private long oid;
    private long rows;
    
    public long getOid() {
        return this.oid;
    }
    
    public long getRows() {
        return this.rows;
    }
    
    void set(final long oid, final long rows) {
        this.oid = oid;
        this.rows = rows;
    }
    
    public void parse(final String status) throws PSQLException {
        if (!Parser.isDigitAt(status, status.length() - 1)) {
            this.set(0L, 0L);
            return;
        }
        long oid = 0L;
        long rows = 0L;
        try {
            final int lastSpace = status.lastIndexOf(32);
            if (Parser.isDigitAt(status, lastSpace + 1)) {
                rows = Parser.parseLong(status, lastSpace + 1, status.length());
                if (Parser.isDigitAt(status, lastSpace - 1)) {
                    final int penultimateSpace = status.lastIndexOf(32, lastSpace - 1);
                    if (Parser.isDigitAt(status, penultimateSpace + 1)) {
                        oid = Parser.parseLong(status, penultimateSpace + 1, lastSpace);
                    }
                }
            }
        }
        catch (final NumberFormatException e) {
            throw new PSQLException(GT.tr("Unable to parse the count in command completion tag: {0}.", status), PSQLState.CONNECTION_FAILURE, e);
        }
        this.set(oid, rows);
    }
    
    @Override
    public String toString() {
        return "CommandStatus{oid=" + this.oid + ", rows=" + this.rows + '}';
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final CommandCompleteParser that = (CommandCompleteParser)o;
        return this.oid == that.oid && this.rows == that.rows;
    }
    
    @Override
    public int hashCode() {
        int result = (int)(this.oid ^ this.oid >>> 32);
        result = 31 * result + (int)(this.rows ^ this.rows >>> 32);
        return result;
    }
}
