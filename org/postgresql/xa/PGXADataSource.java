// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.xa;

import javax.naming.Reference;
import java.sql.Connection;
import org.postgresql.core.BaseConnection;
import java.sql.SQLException;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import org.postgresql.ds.common.BaseDataSource;

public class PGXADataSource extends BaseDataSource implements XADataSource
{
    @Override
    public XAConnection getXAConnection() throws SQLException {
        return this.getXAConnection(this.getUser(), this.getPassword());
    }
    
    @Override
    public XAConnection getXAConnection(final String user, final String password) throws SQLException {
        final Connection con = super.getConnection(user, password);
        return new PGXAConnection((BaseConnection)con);
    }
    
    @Override
    public String getDescription() {
        return "XA-enabled DataSource from PostgreSQL JDBC Driver 42.2.25";
    }
    
    @Override
    protected Reference createReference() {
        return new Reference(this.getClass().getName(), PGXADataSourceFactory.class.getName(), null);
    }
}
