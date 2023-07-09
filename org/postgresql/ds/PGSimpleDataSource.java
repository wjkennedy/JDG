// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.ds;

import java.sql.SQLException;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.sql.DataSource;
import org.postgresql.ds.common.BaseDataSource;

public class PGSimpleDataSource extends BaseDataSource implements DataSource, Serializable
{
    @Override
    public String getDescription() {
        return "Non-Pooling DataSource from PostgreSQL JDBC Driver 42.2.25";
    }
    
    private void writeObject(final ObjectOutputStream out) throws IOException {
        this.writeBaseObject(out);
    }
    
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.readBaseObject(in);
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(this.getClass());
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        if (iface.isAssignableFrom(this.getClass())) {
            return iface.cast(this);
        }
        throw new SQLException("Cannot unwrap to " + iface.getName());
    }
}
