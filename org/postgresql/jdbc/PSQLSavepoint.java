// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

import org.postgresql.core.Utils;
import java.sql.SQLException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import java.sql.Savepoint;

public class PSQLSavepoint implements Savepoint
{
    private boolean isValid;
    private final boolean isNamed;
    private int id;
    private String name;
    
    public PSQLSavepoint(final int id) {
        this.isValid = true;
        this.isNamed = false;
        this.id = id;
    }
    
    public PSQLSavepoint(final String name) {
        this.isValid = true;
        this.isNamed = true;
        this.name = name;
    }
    
    @Override
    public int getSavepointId() throws SQLException {
        if (!this.isValid) {
            throw new PSQLException(GT.tr("Cannot reference a savepoint after it has been released.", new Object[0]), PSQLState.INVALID_SAVEPOINT_SPECIFICATION);
        }
        if (this.isNamed) {
            throw new PSQLException(GT.tr("Cannot retrieve the id of a named savepoint.", new Object[0]), PSQLState.WRONG_OBJECT_TYPE);
        }
        return this.id;
    }
    
    @Override
    public String getSavepointName() throws SQLException {
        if (!this.isValid) {
            throw new PSQLException(GT.tr("Cannot reference a savepoint after it has been released.", new Object[0]), PSQLState.INVALID_SAVEPOINT_SPECIFICATION);
        }
        if (!this.isNamed || this.name == null) {
            throw new PSQLException(GT.tr("Cannot retrieve the name of an unnamed savepoint.", new Object[0]), PSQLState.WRONG_OBJECT_TYPE);
        }
        return this.name;
    }
    
    public void invalidate() {
        this.isValid = false;
    }
    
    public String getPGName() throws SQLException {
        if (!this.isValid) {
            throw new PSQLException(GT.tr("Cannot reference a savepoint after it has been released.", new Object[0]), PSQLState.INVALID_SAVEPOINT_SPECIFICATION);
        }
        if (this.isNamed && this.name != null) {
            return Utils.escapeIdentifier(null, this.name).toString();
        }
        return "JDBC_SAVEPOINT_" + this.id;
    }
}
