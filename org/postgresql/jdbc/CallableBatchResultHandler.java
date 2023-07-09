// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

import org.postgresql.core.ResultCursor;
import org.postgresql.core.Tuple;
import java.util.List;
import org.postgresql.core.Field;
import org.postgresql.core.ParameterList;
import org.postgresql.core.Query;

class CallableBatchResultHandler extends BatchResultHandler
{
    CallableBatchResultHandler(final PgStatement statement, final Query[] queries, final ParameterList[] parameterLists) {
        super(statement, queries, parameterLists, false);
    }
    
    @Override
    public void handleResultRows(final Query fromQuery, final Field[] fields, final List<Tuple> tuples, final ResultCursor cursor) {
    }
}
