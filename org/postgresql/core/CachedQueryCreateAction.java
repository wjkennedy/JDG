// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import java.sql.SQLException;
import java.util.List;
import org.postgresql.jdbc.PreferQueryMode;
import org.postgresql.util.internal.Nullness;
import org.postgresql.util.LruCache;

class CachedQueryCreateAction implements LruCache.CreateAction<Object, CachedQuery>
{
    private static final String[] EMPTY_RETURNING;
    private final QueryExecutor queryExecutor;
    
    CachedQueryCreateAction(final QueryExecutor queryExecutor) {
        this.queryExecutor = queryExecutor;
    }
    
    @Override
    public CachedQuery create(final Object key) throws SQLException {
        assert key instanceof String || key instanceof BaseQueryKey : "Query key should be String or BaseQueryKey. Given " + key.getClass() + ", sql: " + key;
        String parsedSql;
        if (key instanceof BaseQueryKey) {
            final BaseQueryKey queryKey = (BaseQueryKey)key;
            parsedSql = queryKey.sql;
        }
        else {
            final BaseQueryKey queryKey = null;
            parsedSql = (String)key;
        }
        BaseQueryKey queryKey;
        if (key instanceof String || Nullness.castNonNull(queryKey).escapeProcessing) {
            parsedSql = Parser.replaceProcessing(parsedSql, true, this.queryExecutor.getStandardConformingStrings());
        }
        boolean isFunction;
        if (key instanceof CallableQueryKey) {
            final JdbcCallParseInfo callInfo = Parser.modifyJdbcCall(parsedSql, this.queryExecutor.getStandardConformingStrings(), this.queryExecutor.getServerVersionNum(), this.queryExecutor.getProtocolVersion(), this.queryExecutor.getEscapeSyntaxCallMode());
            parsedSql = callInfo.getSql();
            isFunction = callInfo.isFunction();
        }
        else {
            isFunction = false;
        }
        final boolean isParameterized = key instanceof String || Nullness.castNonNull(queryKey).isParameterized;
        final boolean splitStatements = isParameterized || this.queryExecutor.getPreferQueryMode().compareTo(PreferQueryMode.EXTENDED) >= 0;
        String[] returningColumns;
        if (key instanceof QueryWithReturningColumnsKey) {
            returningColumns = ((QueryWithReturningColumnsKey)key).columnNames;
        }
        else {
            returningColumns = CachedQueryCreateAction.EMPTY_RETURNING;
        }
        final List<NativeQuery> queries = Parser.parseJdbcSql(parsedSql, this.queryExecutor.getStandardConformingStrings(), isParameterized, splitStatements, this.queryExecutor.isReWriteBatchedInsertsEnabled(), returningColumns);
        final Query query = this.queryExecutor.wrap(queries);
        return new CachedQuery(key, query, isFunction);
    }
    
    static {
        EMPTY_RETURNING = new String[0];
    }
}
