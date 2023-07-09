// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

import org.postgresql.core.v3.BatchedQuery;
import java.sql.ResultSet;
import java.sql.BatchUpdateException;
import java.util.Arrays;
import java.sql.SQLWarning;
import java.util.Iterator;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import org.postgresql.util.internal.Nullness;
import java.sql.SQLException;
import org.postgresql.core.ResultCursor;
import org.postgresql.core.Field;
import java.util.ArrayList;
import org.postgresql.core.Tuple;
import java.util.List;
import org.postgresql.core.ParameterList;
import org.postgresql.core.Query;
import org.postgresql.core.ResultHandlerBase;

public class BatchResultHandler extends ResultHandlerBase
{
    private final PgStatement pgStatement;
    private int resultIndex;
    private final Query[] queries;
    private final long[] longUpdateCounts;
    private final ParameterList[] parameterLists;
    private final boolean expectGeneratedKeys;
    private PgResultSet generatedKeys;
    private int committedRows;
    private final List<List<Tuple>> allGeneratedRows;
    private List<Tuple> latestGeneratedRows;
    private PgResultSet latestGeneratedKeysRs;
    
    BatchResultHandler(final PgStatement pgStatement, final Query[] queries, final ParameterList[] parameterLists, final boolean expectGeneratedKeys) {
        this.resultIndex = 0;
        this.pgStatement = pgStatement;
        this.queries = queries;
        this.parameterLists = parameterLists;
        this.longUpdateCounts = new long[queries.length];
        this.allGeneratedRows = ((this.expectGeneratedKeys = expectGeneratedKeys) ? new ArrayList<List<Tuple>>() : null);
    }
    
    @Override
    public void handleResultRows(final Query fromQuery, final Field[] fields, final List<Tuple> tuples, final ResultCursor cursor) {
        ++this.resultIndex;
        if (!this.expectGeneratedKeys) {
            return;
        }
        if (this.generatedKeys == null) {
            try {
                this.latestGeneratedKeysRs = (PgResultSet)this.pgStatement.createResultSet(fromQuery, fields, new ArrayList<Tuple>(), cursor);
            }
            catch (final SQLException e) {
                this.handleError(e);
            }
        }
        this.latestGeneratedRows = tuples;
    }
    
    @Override
    public void handleCommandStatus(final String status, final long updateCount, final long insertOID) {
        final List<Tuple> latestGeneratedRows = this.latestGeneratedRows;
        if (latestGeneratedRows != null) {
            --this.resultIndex;
            if (updateCount > 0L && (this.getException() == null || this.isAutoCommit())) {
                final List<List<Tuple>> allGeneratedRows = Nullness.castNonNull(this.allGeneratedRows, "allGeneratedRows");
                allGeneratedRows.add(latestGeneratedRows);
                if (this.generatedKeys == null) {
                    this.generatedKeys = this.latestGeneratedKeysRs;
                }
            }
            this.latestGeneratedRows = null;
        }
        if (this.resultIndex >= this.queries.length) {
            this.handleError(new PSQLException(GT.tr("Too many update results were returned.", new Object[0]), PSQLState.TOO_MANY_RESULTS));
            return;
        }
        this.latestGeneratedKeysRs = null;
        this.longUpdateCounts[this.resultIndex++] = updateCount;
    }
    
    private boolean isAutoCommit() {
        try {
            return this.pgStatement.getConnection().getAutoCommit();
        }
        catch (final SQLException e) {
            assert false : "pgStatement.getConnection().getAutoCommit() should not throw";
            return false;
        }
    }
    
    @Override
    public void secureProgress() {
        if (this.isAutoCommit()) {
            this.committedRows = this.resultIndex;
            this.updateGeneratedKeys();
        }
    }
    
    private void updateGeneratedKeys() {
        final List<List<Tuple>> allGeneratedRows = this.allGeneratedRows;
        if (allGeneratedRows == null || allGeneratedRows.isEmpty()) {
            return;
        }
        final PgResultSet generatedKeys = Nullness.castNonNull(this.generatedKeys, "generatedKeys");
        for (final List<Tuple> rows : allGeneratedRows) {
            generatedKeys.addRows(rows);
        }
        allGeneratedRows.clear();
    }
    
    @Override
    public void handleWarning(final SQLWarning warning) {
        this.pgStatement.addWarning(warning);
    }
    
    @Override
    public void handleError(final SQLException newError) {
        if (this.getException() == null) {
            Arrays.fill(this.longUpdateCounts, this.committedRows, this.longUpdateCounts.length, -3L);
            if (this.allGeneratedRows != null) {
                this.allGeneratedRows.clear();
            }
            String queryString = "<unknown>";
            if (this.pgStatement.getPGConnection().getLogServerErrorDetail() && this.resultIndex < this.queries.length) {
                queryString = this.queries[this.resultIndex].toString((this.parameterLists == null) ? null : this.parameterLists[this.resultIndex]);
            }
            BatchUpdateException batchException = new BatchUpdateException(GT.tr("Batch entry {0} {1} was aborted: {2}  Call getNextException to see other errors in the batch.", this.resultIndex, queryString, newError.getMessage()), newError.getSQLState(), 0, this.uncompressLongUpdateCount(), newError);
            batchException = new BatchUpdateException(GT.tr("Batch entry {0} {1} was aborted: {2}  Call getNextException to see other errors in the batch.", this.resultIndex, queryString, newError.getMessage()), newError.getSQLState(), 0, this.uncompressUpdateCount(), newError);
            super.handleError(batchException);
        }
        ++this.resultIndex;
        super.handleError(newError);
    }
    
    @Override
    public void handleCompletion() throws SQLException {
        this.updateGeneratedKeys();
        SQLException batchException = this.getException();
        if (batchException != null) {
            if (this.isAutoCommit()) {
                BatchUpdateException newException = new BatchUpdateException(batchException.getMessage(), batchException.getSQLState(), 0, this.uncompressLongUpdateCount(), batchException.getCause());
                newException = new BatchUpdateException(batchException.getMessage(), batchException.getSQLState(), 0, this.uncompressUpdateCount(), batchException.getCause());
                final SQLException next = batchException.getNextException();
                if (next != null) {
                    newException.setNextException(next);
                }
                batchException = newException;
            }
            throw batchException;
        }
    }
    
    public ResultSet getGeneratedKeys() {
        return this.generatedKeys;
    }
    
    private int[] uncompressUpdateCount() {
        final long[] original = this.uncompressLongUpdateCount();
        final int[] copy = new int[original.length];
        for (int i = 0; i < original.length; ++i) {
            copy[i] = ((original[i] > 2147483647L) ? -2 : ((int)original[i]));
        }
        return copy;
    }
    
    public int[] getUpdateCount() {
        return this.uncompressUpdateCount();
    }
    
    private long[] uncompressLongUpdateCount() {
        if (!(this.queries[0] instanceof BatchedQuery)) {
            return this.longUpdateCounts;
        }
        int totalRows = 0;
        boolean hasRewrites = false;
        for (final Query query : this.queries) {
            final int batchSize = query.getBatchSize();
            totalRows += batchSize;
            hasRewrites |= (batchSize > 1);
        }
        if (!hasRewrites) {
            return this.longUpdateCounts;
        }
        final long[] newUpdateCounts = new long[totalRows];
        int offset = 0;
        for (int i = 0; i < this.queries.length; ++i) {
            final Query query = this.queries[i];
            final int batchSize = query.getBatchSize();
            long superBatchResult = this.longUpdateCounts[i];
            if (batchSize == 1) {
                newUpdateCounts[offset++] = superBatchResult;
            }
            else {
                if (superBatchResult > 0L) {
                    superBatchResult = -2L;
                }
                Arrays.fill(newUpdateCounts, offset, offset + batchSize, superBatchResult);
                offset += batchSize;
            }
        }
        return newUpdateCounts;
    }
    
    public long[] getLargeUpdateCount() {
        return this.uncompressLongUpdateCount();
    }
}
