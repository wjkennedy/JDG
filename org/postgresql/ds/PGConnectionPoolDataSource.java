// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.ds;

import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import javax.sql.PooledConnection;
import java.io.Serializable;
import javax.sql.ConnectionPoolDataSource;
import org.postgresql.ds.common.BaseDataSource;

public class PGConnectionPoolDataSource extends BaseDataSource implements ConnectionPoolDataSource, Serializable
{
    private boolean defaultAutoCommit;
    
    public PGConnectionPoolDataSource() {
        this.defaultAutoCommit = true;
    }
    
    @Override
    public String getDescription() {
        return "ConnectionPoolDataSource from PostgreSQL JDBC Driver 42.2.25";
    }
    
    @Override
    public PooledConnection getPooledConnection() throws SQLException {
        return new PGPooledConnection(this.getConnection(), this.defaultAutoCommit);
    }
    
    @Override
    public PooledConnection getPooledConnection(final String user, final String password) throws SQLException {
        return new PGPooledConnection(this.getConnection(user, password), this.defaultAutoCommit);
    }
    
    public boolean isDefaultAutoCommit() {
        return this.defaultAutoCommit;
    }
    
    public void setDefaultAutoCommit(final boolean defaultAutoCommit) {
        this.defaultAutoCommit = defaultAutoCommit;
    }
    
    private void writeObject(final ObjectOutputStream out) throws IOException {
        this.writeBaseObject(out);
        out.writeBoolean(this.defaultAutoCommit);
    }
    
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.readBaseObject(in);
        this.defaultAutoCommit = in.readBoolean();
    }
}
