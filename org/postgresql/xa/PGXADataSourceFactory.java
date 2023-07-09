// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.xa;

import org.postgresql.ds.common.BaseDataSource;
import javax.naming.Reference;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import org.postgresql.ds.common.PGObjectFactory;

public class PGXADataSourceFactory extends PGObjectFactory
{
    @Override
    public Object getObjectInstance(final Object obj, final Name name, final Context nameCtx, final Hashtable<?, ?> environment) throws Exception {
        final Reference ref = (Reference)obj;
        final String className = ref.getClassName();
        if (className.equals("org.postgresql.xa.PGXADataSource")) {
            return this.loadXADataSource(ref);
        }
        return null;
    }
    
    private Object loadXADataSource(final Reference ref) {
        final PGXADataSource ds = new PGXADataSource();
        return this.loadBaseDataSource(ds, ref);
    }
}
