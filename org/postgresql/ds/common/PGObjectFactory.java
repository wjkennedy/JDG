// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.ds.common;

import javax.naming.RefAddr;
import org.postgresql.ds.PGConnectionPoolDataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.postgresql.ds.PGPoolingDataSource;
import org.postgresql.util.internal.Nullness;
import javax.naming.Reference;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

public class PGObjectFactory implements ObjectFactory
{
    @Override
    public Object getObjectInstance(final Object obj, final Name name, final Context nameCtx, final Hashtable<?, ?> environment) throws Exception {
        final Reference ref = (Reference)obj;
        final String className = ref.getClassName();
        if (className.equals("org.postgresql.ds.PGSimpleDataSource") || className.equals("org.postgresql.jdbc2.optional.SimpleDataSource") || className.equals("org.postgresql.jdbc3.Jdbc3SimpleDataSource")) {
            return this.loadSimpleDataSource(ref);
        }
        if (className.equals("org.postgresql.ds.PGConnectionPoolDataSource") || className.equals("org.postgresql.jdbc2.optional.ConnectionPool") || className.equals("org.postgresql.jdbc3.Jdbc3ConnectionPool")) {
            return this.loadConnectionPool(ref);
        }
        if (className.equals("org.postgresql.ds.PGPoolingDataSource") || className.equals("org.postgresql.jdbc2.optional.PoolingDataSource") || className.equals("org.postgresql.jdbc3.Jdbc3PoolingDataSource")) {
            return this.loadPoolingDataSource(ref);
        }
        return null;
    }
    
    private Object loadPoolingDataSource(final Reference ref) {
        final String name = Nullness.castNonNull(this.getProperty(ref, "dataSourceName"));
        PGPoolingDataSource pds = PGPoolingDataSource.getDataSource(name);
        if (pds != null) {
            return pds;
        }
        pds = new PGPoolingDataSource();
        pds.setDataSourceName(name);
        this.loadBaseDataSource(pds, ref);
        final String min = this.getProperty(ref, "initialConnections");
        if (min != null) {
            pds.setInitialConnections(Integer.parseInt(min));
        }
        final String max = this.getProperty(ref, "maxConnections");
        if (max != null) {
            pds.setMaxConnections(Integer.parseInt(max));
        }
        return pds;
    }
    
    private Object loadSimpleDataSource(final Reference ref) {
        final PGSimpleDataSource ds = new PGSimpleDataSource();
        return this.loadBaseDataSource(ds, ref);
    }
    
    private Object loadConnectionPool(final Reference ref) {
        final PGConnectionPoolDataSource cp = new PGConnectionPoolDataSource();
        return this.loadBaseDataSource(cp, ref);
    }
    
    protected Object loadBaseDataSource(final BaseDataSource ds, final Reference ref) {
        ds.setFromReference(ref);
        return ds;
    }
    
    protected String getProperty(final Reference ref, final String s) {
        final RefAddr addr = ref.get(s);
        if (addr == null) {
            return null;
        }
        return (String)addr.getContent();
    }
}
