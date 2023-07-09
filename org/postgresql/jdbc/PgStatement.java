// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

import org.postgresql.core.ResultHandlerBase;
import org.postgresql.Driver;
import java.sql.Connection;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;
import java.sql.SQLWarning;
import org.postgresql.core.ResultHandler;
import org.postgresql.core.SqlCommand;
import org.postgresql.core.CachedQuery;
import org.postgresql.core.QueryExecutor;
import org.postgresql.util.internal.Nullness;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import java.sql.ResultSet;
import org.postgresql.core.ResultCursor;
import org.postgresql.core.Tuple;
import java.util.List;
import org.postgresql.core.Field;
import java.sql.SQLException;
import org.postgresql.core.BaseConnection;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.TimerTask;
import org.postgresql.core.ParameterList;
import org.postgresql.core.Query;
import java.util.ArrayList;
import org.postgresql.core.BaseStatement;
import java.sql.Statement;

public class PgStatement implements Statement, BaseStatement
{
    private static final String[] NO_RETURNING_COLUMNS;
    private static final boolean DEFAULT_FORCE_BINARY_TRANSFERS;
    private boolean forceBinaryTransfers;
    protected ArrayList<Query> batchStatements;
    protected ArrayList<ParameterList> batchParameters;
    protected final int resultsettype;
    protected final int concurrency;
    private final int rsHoldability;
    private boolean poolable;
    private boolean closeOnCompletion;
    protected int fetchdirection;
    private volatile TimerTask cancelTimerTask;
    private static final AtomicReferenceFieldUpdater<PgStatement, TimerTask> CANCEL_TIMER_UPDATER;
    private volatile StatementCancelState statementState;
    private static final AtomicReferenceFieldUpdater<PgStatement, StatementCancelState> STATE_UPDATER;
    protected boolean wantsGeneratedKeysOnce;
    public boolean wantsGeneratedKeysAlways;
    protected final BaseConnection connection;
    protected volatile PSQLWarningWrapper warnings;
    protected int maxrows;
    protected int fetchSize;
    protected long timeout;
    protected boolean replaceProcessingEnabled;
    protected ResultWrapper result;
    protected ResultWrapper firstUnclosedResult;
    protected ResultWrapper generatedKeys;
    protected int mPrepareThreshold;
    protected int maxFieldSize;
    private volatile boolean isClosed;
    
    PgStatement(final PgConnection c, final int rsType, final int rsConcurrency, final int rsHoldability) throws SQLException {
        this.forceBinaryTransfers = PgStatement.DEFAULT_FORCE_BINARY_TRANSFERS;
        this.batchStatements = null;
        this.batchParameters = null;
        this.closeOnCompletion = false;
        this.fetchdirection = 1000;
        this.cancelTimerTask = null;
        this.statementState = StatementCancelState.IDLE;
        this.wantsGeneratedKeysOnce = false;
        this.wantsGeneratedKeysAlways = false;
        this.warnings = null;
        this.maxrows = 0;
        this.fetchSize = 0;
        this.timeout = 0L;
        this.replaceProcessingEnabled = true;
        this.result = null;
        this.firstUnclosedResult = null;
        this.generatedKeys = null;
        this.maxFieldSize = 0;
        this.isClosed = false;
        this.connection = c;
        this.forceBinaryTransfers |= c.getForceBinary();
        this.resultsettype = rsType;
        this.concurrency = rsConcurrency;
        this.setFetchSize(c.getDefaultFetchSize());
        this.setPrepareThreshold(c.getPrepareThreshold());
        this.rsHoldability = rsHoldability;
    }
    
    @Override
    public ResultSet createResultSet(final Query originalQuery, final Field[] fields, final List<Tuple> tuples, final ResultCursor cursor) throws SQLException {
        final PgResultSet newResult = new PgResultSet(originalQuery, this, fields, tuples, cursor, this.getMaxRows(), this.getMaxFieldSize(), this.getResultSetType(), this.getResultSetConcurrency(), this.getResultSetHoldability());
        newResult.setFetchSize(this.getFetchSize());
        newResult.setFetchDirection(this.getFetchDirection());
        return newResult;
    }
    
    public BaseConnection getPGConnection() {
        return this.connection;
    }
    
    public String getFetchingCursorName() {
        return null;
    }
    
    @Override
    public int getFetchSize() {
        return this.fetchSize;
    }
    
    protected boolean wantsScrollableResultSet() {
        return this.resultsettype != 1003;
    }
    
    protected boolean wantsHoldableResultSet() {
        return this.rsHoldability == 1;
    }
    
    @Override
    public ResultSet executeQuery(final String sql) throws SQLException {
        if (!this.executeWithFlags(sql, 0)) {
            throw new PSQLException(GT.tr("No results were returned by the query.", new Object[0]), PSQLState.NO_DATA);
        }
        return this.getSingleResultSet();
    }
    
    protected ResultSet getSingleResultSet() throws SQLException {
        synchronized (this) {
            this.checkClosed();
            final ResultWrapper result = Nullness.castNonNull(this.result);
            if (result.getNext() != null) {
                throw new PSQLException(GT.tr("Multiple ResultSets were returned by the query.", new Object[0]), PSQLState.TOO_MANY_RESULTS);
            }
            return Nullness.castNonNull(result.getResultSet(), "result.getResultSet()");
        }
    }
    
    @Override
    public int executeUpdate(final String sql) throws SQLException {
        this.executeWithFlags(sql, 4);
        this.checkNoResultUpdate();
        return this.getUpdateCount();
    }
    
    protected final void checkNoResultUpdate() throws SQLException {
        synchronized (this) {
            this.checkClosed();
            for (ResultWrapper iter = this.result; iter != null; iter = iter.getNext()) {
                if (iter.getResultSet() != null) {
                    throw new PSQLException(GT.tr("A result was returned when none was expected.", new Object[0]), PSQLState.TOO_MANY_RESULTS);
                }
            }
        }
    }
    
    @Override
    public boolean execute(final String sql) throws SQLException {
        return this.executeWithFlags(sql, 0);
    }
    
    @Override
    public boolean executeWithFlags(final String sql, final int flags) throws SQLException {
        return this.executeCachedSql(sql, flags, PgStatement.NO_RETURNING_COLUMNS);
    }
    
    private boolean executeCachedSql(final String sql, final int flags, final String[] columnNames) throws SQLException {
        final PreferQueryMode preferQueryMode = this.connection.getPreferQueryMode();
        final boolean shouldUseParameterized = false;
        final QueryExecutor queryExecutor = this.connection.getQueryExecutor();
        final Object key = queryExecutor.createQueryKey(sql, this.replaceProcessingEnabled, shouldUseParameterized, columnNames);
        final boolean shouldCache = preferQueryMode == PreferQueryMode.EXTENDED_CACHE_EVERYTHING;
        CachedQuery cachedQuery;
        if (shouldCache) {
            cachedQuery = queryExecutor.borrowQueryByKey(key);
        }
        else {
            cachedQuery = queryExecutor.createQueryByKey(key);
        }
        if (this.wantsGeneratedKeysOnce) {
            final SqlCommand sqlCommand = cachedQuery.query.getSqlCommand();
            this.wantsGeneratedKeysOnce = (sqlCommand != null && sqlCommand.isReturningKeywordPresent());
        }
        boolean res;
        try {
            res = this.executeWithFlags(cachedQuery, flags);
        }
        finally {
            if (shouldCache) {
                queryExecutor.releaseQuery(cachedQuery);
            }
        }
        return res;
    }
    
    @Override
    public boolean executeWithFlags(final CachedQuery simpleQuery, int flags) throws SQLException {
        this.checkClosed();
        if (this.connection.getPreferQueryMode().compareTo(PreferQueryMode.EXTENDED) < 0) {
            flags |= 0x400;
        }
        this.execute(simpleQuery, null, flags);
        synchronized (this) {
            this.checkClosed();
            return this.result != null && this.result.getResultSet() != null;
        }
    }
    
    @Override
    public boolean executeWithFlags(final int flags) throws SQLException {
        this.checkClosed();
        throw new PSQLException(GT.tr("Can''t use executeWithFlags(int) on a Statement.", new Object[0]), PSQLState.WRONG_OBJECT_TYPE);
    }
    
    private void closeUnclosedProcessedResults() throws SQLException {
        synchronized (this) {
            ResultWrapper resultWrapper = this.firstUnclosedResult;
            for (ResultWrapper currentResult = this.result; resultWrapper != currentResult && resultWrapper != null; resultWrapper = resultWrapper.getNext()) {
                final PgResultSet rs = (PgResultSet)resultWrapper.getResultSet();
                if (rs != null) {
                    rs.closeInternally();
                }
            }
            this.firstUnclosedResult = resultWrapper;
        }
    }
    
    protected void closeForNextExecution() throws SQLException {
        this.clearWarnings();
        synchronized (this) {
            this.closeUnclosedProcessedResults();
            if (this.result != null && this.result.getResultSet() != null) {
                this.result.getResultSet().close();
            }
            this.result = null;
            final ResultWrapper generatedKeys = this.generatedKeys;
            if (generatedKeys != null) {
                try (final ResultSet resultSet = generatedKeys.getResultSet()) {}
                this.generatedKeys = null;
            }
        }
    }
    
    protected boolean isOneShotQuery(final CachedQuery cachedQuery) {
        if (cachedQuery == null) {
            return true;
        }
        cachedQuery.increaseExecuteCount();
        return (this.mPrepareThreshold == 0 || cachedQuery.getExecuteCount() < this.mPrepareThreshold) && !this.getForceBinaryTransfer();
    }
    
    protected final void execute(final CachedQuery cachedQuery, final ParameterList queryParameters, final int flags) throws SQLException {
        try {
            this.executeInternal(cachedQuery, queryParameters, flags);
        }
        catch (final SQLException e) {
            if (cachedQuery.query.getSubqueries() != null || !this.connection.getQueryExecutor().willHealOnRetry(e)) {
                throw e;
            }
            cachedQuery.query.close();
            this.executeInternal(cachedQuery, queryParameters, flags);
        }
    }
    
    private void executeInternal(final CachedQuery cachedQuery, final ParameterList queryParameters, int flags) throws SQLException {
        this.closeForNextExecution();
        if (this.fetchSize > 0 && !this.wantsScrollableResultSet() && !this.connection.getAutoCommit() && !this.wantsHoldableResultSet()) {
            flags |= 0x8;
        }
        if (this.wantsGeneratedKeysOnce || this.wantsGeneratedKeysAlways) {
            flags |= 0x40;
            if ((flags & 0x4) != 0x0) {
                flags &= 0xFFFFFFFB;
            }
        }
        if (this.isOneShotQuery(cachedQuery)) {
            flags |= 0x1;
        }
        if (this.connection.getAutoCommit()) {
            flags |= 0x10;
        }
        if (this.connection.hintReadOnly()) {
            flags |= 0x800;
        }
        if (this.concurrency != 1007) {
            flags |= 0x100;
        }
        final Query queryToExecute = cachedQuery.query;
        if (queryToExecute.isEmpty()) {
            flags |= 0x10;
        }
        if (!queryToExecute.isStatementDescribed() && this.forceBinaryTransfers && (flags & 0x400) == 0x0) {
            final int flags2 = flags | 0x20;
            final StatementResultHandler handler2 = new StatementResultHandler();
            this.connection.getQueryExecutor().execute(queryToExecute, queryParameters, handler2, 0, 0, flags2);
            final ResultWrapper result2 = handler2.getResults();
            if (result2 != null) {
                Nullness.castNonNull(result2.getResultSet(), "result2.getResultSet()").close();
            }
        }
        final StatementResultHandler handler3 = new StatementResultHandler();
        synchronized (this) {
            this.result = null;
        }
        try {
            this.startTimer();
            this.connection.getQueryExecutor().execute(queryToExecute, queryParameters, handler3, this.maxrows, this.fetchSize, flags);
        }
        finally {
            this.killTimerTask();
        }
        synchronized (this) {
            this.checkClosed();
            final ResultWrapper results;
            final ResultWrapper currentResult = results = handler3.getResults();
            this.firstUnclosedResult = results;
            this.result = results;
            if (this.wantsGeneratedKeysOnce || this.wantsGeneratedKeysAlways) {
                this.generatedKeys = currentResult;
                this.result = Nullness.castNonNull(currentResult, "handler.getResults()").getNext();
                if (this.wantsGeneratedKeysOnce) {
                    this.wantsGeneratedKeysOnce = false;
                }
            }
        }
    }
    
    @Override
    public void setCursorName(final String name) throws SQLException {
        this.checkClosed();
    }
    
    @Override
    public int getUpdateCount() throws SQLException {
        synchronized (this) {
            this.checkClosed();
            if (this.result == null || this.result.getResultSet() != null) {
                return -1;
            }
            final long count = this.result.getUpdateCount();
            return (count > 2147483647L) ? -2 : ((int)count);
        }
    }
    
    @Override
    public boolean getMoreResults() throws SQLException {
        return this.getMoreResults(3);
    }
    
    @Override
    public int getMaxRows() throws SQLException {
        this.checkClosed();
        return this.maxrows;
    }
    
    @Override
    public void setMaxRows(final int max) throws SQLException {
        this.checkClosed();
        if (max < 0) {
            throw new PSQLException(GT.tr("Maximum number of rows must be a value grater than or equal to 0.", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE);
        }
        this.maxrows = max;
    }
    
    @Override
    public void setEscapeProcessing(final boolean enable) throws SQLException {
        this.checkClosed();
        this.replaceProcessingEnabled = enable;
    }
    
    @Override
    public int getQueryTimeout() throws SQLException {
        this.checkClosed();
        final long seconds = this.timeout / 1000L;
        if (seconds >= 2147483647L) {
            return Integer.MAX_VALUE;
        }
        return (int)seconds;
    }
    
    @Override
    public void setQueryTimeout(final int seconds) throws SQLException {
        this.setQueryTimeoutMs(seconds * 1000L);
    }
    
    public long getQueryTimeoutMs() throws SQLException {
        this.checkClosed();
        return this.timeout;
    }
    
    public void setQueryTimeoutMs(final long millis) throws SQLException {
        this.checkClosed();
        if (millis < 0L) {
            throw new PSQLException(GT.tr("Query timeout must be a value greater than or equals to 0.", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE);
        }
        this.timeout = millis;
    }
    
    public void addWarning(final SQLWarning warn) {
        final PSQLWarningWrapper warnWrap = this.warnings;
        if (warnWrap == null) {
            this.warnings = new PSQLWarningWrapper(warn);
        }
        else {
            warnWrap.addWarning(warn);
        }
    }
    
    @Override
    public SQLWarning getWarnings() throws SQLException {
        this.checkClosed();
        final PSQLWarningWrapper warnWrap = this.warnings;
        return (warnWrap != null) ? warnWrap.getFirstWarning() : null;
    }
    
    @Override
    public int getMaxFieldSize() throws SQLException {
        return this.maxFieldSize;
    }
    
    @Override
    public void setMaxFieldSize(final int max) throws SQLException {
        this.checkClosed();
        if (max < 0) {
            throw new PSQLException(GT.tr("The maximum field size must be a value greater than or equal to 0.", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE);
        }
        this.maxFieldSize = max;
    }
    
    @Override
    public void clearWarnings() throws SQLException {
        this.warnings = null;
    }
    
    @Override
    public ResultSet getResultSet() throws SQLException {
        synchronized (this) {
            this.checkClosed();
            if (this.result == null) {
                return null;
            }
            return this.result.getResultSet();
        }
    }
    
    @Override
    public final void close() throws SQLException {
        synchronized (this) {
            if (this.isClosed) {
                return;
            }
            this.isClosed = true;
        }
        this.cancel();
        this.closeForNextExecution();
        this.closeImpl();
    }
    
    protected void closeImpl() throws SQLException {
    }
    
    @Override
    public long getLastOID() throws SQLException {
        synchronized (this) {
            this.checkClosed();
            if (this.result == null) {
                return 0L;
            }
            return this.result.getInsertOID();
        }
    }
    
    @Override
    public void setPrepareThreshold(int newThreshold) throws SQLException {
        this.checkClosed();
        if (newThreshold < 0) {
            this.forceBinaryTransfers = true;
            newThreshold = 1;
        }
        this.mPrepareThreshold = newThreshold;
    }
    
    @Override
    public int getPrepareThreshold() {
        return this.mPrepareThreshold;
    }
    
    @Override
    public void setUseServerPrepare(final boolean flag) throws SQLException {
        this.setPrepareThreshold(flag ? 1 : 0);
    }
    
    @Override
    public boolean isUseServerPrepare() {
        return false;
    }
    
    protected void checkClosed() throws SQLException {
        if (this.isClosed()) {
            throw new PSQLException(GT.tr("This statement has been closed.", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
    }
    
    @Override
    public void addBatch(final String sql) throws SQLException {
        this.checkClosed();
        ArrayList<Query> batchStatements = this.batchStatements;
        if (batchStatements == null) {
            batchStatements = (this.batchStatements = new ArrayList<Query>());
        }
        ArrayList<ParameterList> batchParameters = this.batchParameters;
        if (batchParameters == null) {
            batchParameters = (this.batchParameters = new ArrayList<ParameterList>());
        }
        final boolean shouldUseParameterized = false;
        final CachedQuery cachedQuery = this.connection.createQuery(sql, this.replaceProcessingEnabled, shouldUseParameterized, new String[0]);
        batchStatements.add(cachedQuery.query);
        batchParameters.add(null);
    }
    
    @Override
    public void clearBatch() throws SQLException {
        if (this.batchStatements != null) {
            this.batchStatements.clear();
        }
        if (this.batchParameters != null) {
            this.batchParameters.clear();
        }
    }
    
    protected BatchResultHandler createBatchHandler(final Query[] queries, final ParameterList[] parameterLists) {
        return new BatchResultHandler(this, queries, parameterLists, this.wantsGeneratedKeysAlways);
    }
    
    @RequiresNonNull({ "batchStatements", "batchParameters" })
    private BatchResultHandler internalExecuteBatch() throws SQLException {
        this.transformQueriesAndParameters();
        final ArrayList<Query> batchStatements = Nullness.castNonNull(this.batchStatements);
        final ArrayList<ParameterList> batchParameters = Nullness.castNonNull(this.batchParameters);
        final Query[] queries = batchStatements.toArray(new Query[0]);
        final ParameterList[] parameterLists = batchParameters.toArray(new ParameterList[0]);
        batchStatements.clear();
        batchParameters.clear();
        boolean preDescribe = false;
        int flags;
        if (this.wantsGeneratedKeysAlways) {
            flags = 320;
        }
        else {
            flags = 4;
        }
        final PreferQueryMode preferQueryMode = this.connection.getPreferQueryMode();
        if (preferQueryMode == PreferQueryMode.SIMPLE || (preferQueryMode == PreferQueryMode.EXTENDED_FOR_PREPARED && parameterLists[0] == null)) {
            flags |= 0x400;
        }
        final boolean sameQueryAhead = queries.length > 1 && queries[0] == queries[1];
        if (!sameQueryAhead || this.isOneShotQuery(null)) {
            flags |= 0x1;
        }
        else {
            preDescribe = ((this.wantsGeneratedKeysAlways || sameQueryAhead) && !queries[0].isStatementDescribed());
            flags |= 0x200;
        }
        if (this.connection.getAutoCommit()) {
            flags |= 0x10;
        }
        if (this.connection.hintReadOnly()) {
            flags |= 0x800;
        }
        final BatchResultHandler handler = this.createBatchHandler(queries, parameterLists);
        if ((preDescribe || this.forceBinaryTransfers) && (flags & 0x400) == 0x0) {
            final int flags2 = flags | 0x20;
            final StatementResultHandler handler2 = new StatementResultHandler();
            try {
                this.connection.getQueryExecutor().execute(queries[0], parameterLists[0], handler2, 0, 0, flags2);
            }
            catch (final SQLException e) {
                handler.handleError(e);
                handler.handleCompletion();
            }
            final ResultWrapper result2 = handler2.getResults();
            if (result2 != null) {
                Nullness.castNonNull(result2.getResultSet(), "result2.getResultSet()").close();
            }
        }
        synchronized (this) {
            this.result = null;
        }
        try {
            this.startTimer();
            this.connection.getQueryExecutor().execute(queries, parameterLists, handler, this.maxrows, this.fetchSize, flags);
        }
        finally {
            this.killTimerTask();
            synchronized (this) {
                this.checkClosed();
                if (this.wantsGeneratedKeysAlways) {
                    this.generatedKeys = new ResultWrapper(handler.getGeneratedKeys());
                }
            }
        }
        return handler;
    }
    
    @Override
    public int[] executeBatch() throws SQLException {
        this.checkClosed();
        this.closeForNextExecution();
        if (this.batchStatements == null || this.batchStatements.isEmpty() || this.batchParameters == null) {
            return new int[0];
        }
        return this.internalExecuteBatch().getUpdateCount();
    }
    
    @Override
    public void cancel() throws SQLException {
        if (this.statementState == StatementCancelState.IDLE) {
            return;
        }
        if (!PgStatement.STATE_UPDATER.compareAndSet(this, StatementCancelState.IN_QUERY, StatementCancelState.CANCELING)) {
            return;
        }
        synchronized (this.connection) {
            try {
                this.connection.cancelQuery();
            }
            finally {
                PgStatement.STATE_UPDATER.set(this, StatementCancelState.CANCELLED);
                this.connection.notifyAll();
            }
        }
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        return this.connection;
    }
    
    @Override
    public int getFetchDirection() {
        return this.fetchdirection;
    }
    
    @Override
    public int getResultSetConcurrency() {
        return this.concurrency;
    }
    
    @Override
    public int getResultSetType() {
        return this.resultsettype;
    }
    
    @Override
    public void setFetchDirection(final int direction) throws SQLException {
        switch (direction) {
            case 1000:
            case 1001:
            case 1002: {
                this.fetchdirection = direction;
                return;
            }
            default: {
                throw new PSQLException(GT.tr("Invalid fetch direction constant: {0}.", direction), PSQLState.INVALID_PARAMETER_VALUE);
            }
        }
    }
    
    @Override
    public void setFetchSize(final int rows) throws SQLException {
        this.checkClosed();
        if (rows < 0) {
            throw new PSQLException(GT.tr("Fetch size must be a value greater to or equal to 0.", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE);
        }
        this.fetchSize = rows;
    }
    
    private void startTimer() {
        this.cleanupTimer();
        PgStatement.STATE_UPDATER.set(this, StatementCancelState.IN_QUERY);
        if (this.timeout == 0L) {
            return;
        }
        final TimerTask cancelTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    if (!PgStatement.CANCEL_TIMER_UPDATER.compareAndSet(PgStatement.this, this, null)) {
                        return;
                    }
                    PgStatement.this.cancel();
                }
                catch (final SQLException ex) {}
            }
        };
        PgStatement.CANCEL_TIMER_UPDATER.set(this, cancelTask);
        this.connection.addTimerTask(cancelTask, this.timeout);
    }
    
    private boolean cleanupTimer() {
        final TimerTask timerTask = PgStatement.CANCEL_TIMER_UPDATER.get(this);
        if (timerTask == null) {
            return this.timeout == 0L;
        }
        if (!PgStatement.CANCEL_TIMER_UPDATER.compareAndSet(this, timerTask, null)) {
            return false;
        }
        timerTask.cancel();
        this.connection.purgeTimerTasks();
        return true;
    }
    
    private void killTimerTask() {
        final boolean timerTaskIsClear = this.cleanupTimer();
        if (timerTaskIsClear && PgStatement.STATE_UPDATER.compareAndSet(this, StatementCancelState.IN_QUERY, StatementCancelState.IDLE)) {
            return;
        }
        boolean interrupted = false;
        synchronized (this.connection) {
            while (!PgStatement.STATE_UPDATER.compareAndSet(this, StatementCancelState.CANCELLED, StatementCancelState.IDLE)) {
                try {
                    this.connection.wait(10L);
                }
                catch (final InterruptedException e) {
                    interrupted = true;
                }
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }
    
    protected boolean getForceBinaryTransfer() {
        return this.forceBinaryTransfers;
    }
    
    @Override
    public long getLargeUpdateCount() throws SQLException {
        synchronized (this) {
            this.checkClosed();
            if (this.result == null || this.result.getResultSet() != null) {
                return -1L;
            }
            return this.result.getUpdateCount();
        }
    }
    
    @Override
    public void setLargeMaxRows(final long max) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setLargeMaxRows");
    }
    
    @Override
    public long getLargeMaxRows() throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getLargeMaxRows");
    }
    
    @Override
    public long[] executeLargeBatch() throws SQLException {
        this.checkClosed();
        this.closeForNextExecution();
        if (this.batchStatements == null || this.batchStatements.isEmpty() || this.batchParameters == null) {
            return new long[0];
        }
        return this.internalExecuteBatch().getLargeUpdateCount();
    }
    
    @Override
    public long executeLargeUpdate(final String sql) throws SQLException {
        this.executeWithFlags(sql, 4);
        this.checkNoResultUpdate();
        return this.getLargeUpdateCount();
    }
    
    @Override
    public long executeLargeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
        if (autoGeneratedKeys == 2) {
            return this.executeLargeUpdate(sql);
        }
        return this.executeLargeUpdate(sql, (String[])null);
    }
    
    @Override
    public long executeLargeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        if (columnIndexes == null || columnIndexes.length == 0) {
            return this.executeLargeUpdate(sql);
        }
        throw new PSQLException(GT.tr("Returning autogenerated keys by column index is not supported.", new Object[0]), PSQLState.NOT_IMPLEMENTED);
    }
    
    @Override
    public long executeLargeUpdate(final String sql, final String[] columnNames) throws SQLException {
        if (columnNames != null && columnNames.length == 0) {
            return this.executeLargeUpdate(sql);
        }
        this.wantsGeneratedKeysOnce = true;
        if (!this.executeCachedSql(sql, 0, columnNames)) {}
        return this.getLargeUpdateCount();
    }
    
    @Override
    public boolean isClosed() throws SQLException {
        return this.isClosed;
    }
    
    @Override
    public void setPoolable(final boolean poolable) throws SQLException {
        this.checkClosed();
        this.poolable = poolable;
    }
    
    @Override
    public boolean isPoolable() throws SQLException {
        this.checkClosed();
        return this.poolable;
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
    
    @Override
    public void closeOnCompletion() throws SQLException {
        this.closeOnCompletion = true;
    }
    
    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return this.closeOnCompletion;
    }
    
    protected void checkCompletion() throws SQLException {
        if (!this.closeOnCompletion) {
            return;
        }
        synchronized (this) {
            for (ResultWrapper result = this.firstUnclosedResult; result != null; result = result.getNext()) {
                final ResultSet resultSet = result.getResultSet();
                if (resultSet != null && !resultSet.isClosed()) {
                    return;
                }
            }
        }
        this.closeOnCompletion = false;
        try {
            this.close();
        }
        finally {
            this.closeOnCompletion = true;
        }
    }
    
    @Override
    public boolean getMoreResults(final int current) throws SQLException {
        synchronized (this) {
            this.checkClosed();
            if (current == 1 && this.result != null && this.result.getResultSet() != null) {
                this.result.getResultSet().close();
            }
            if (this.result != null) {
                this.result = this.result.getNext();
            }
            if (current == 3) {
                this.closeUnclosedProcessedResults();
            }
            return this.result != null && this.result.getResultSet() != null;
        }
    }
    
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        synchronized (this) {
            this.checkClosed();
            if (this.generatedKeys == null || this.generatedKeys.getResultSet() == null) {
                return this.createDriverResultSet(new Field[0], new ArrayList<Tuple>());
            }
            return this.generatedKeys.getResultSet();
        }
    }
    
    @Override
    public int executeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
        if (autoGeneratedKeys == 2) {
            return this.executeUpdate(sql);
        }
        return this.executeUpdate(sql, (String[])null);
    }
    
    @Override
    public int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        if (columnIndexes == null || columnIndexes.length == 0) {
            return this.executeUpdate(sql);
        }
        throw new PSQLException(GT.tr("Returning autogenerated keys by column index is not supported.", new Object[0]), PSQLState.NOT_IMPLEMENTED);
    }
    
    @Override
    public int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
        if (columnNames != null && columnNames.length == 0) {
            return this.executeUpdate(sql);
        }
        this.wantsGeneratedKeysOnce = true;
        if (!this.executeCachedSql(sql, 0, columnNames)) {}
        return this.getUpdateCount();
    }
    
    @Override
    public boolean execute(final String sql, final int autoGeneratedKeys) throws SQLException {
        if (autoGeneratedKeys == 2) {
            return this.execute(sql);
        }
        return this.execute(sql, (String[])null);
    }
    
    @Override
    public boolean execute(final String sql, final int[] columnIndexes) throws SQLException {
        if (columnIndexes != null && columnIndexes.length == 0) {
            return this.execute(sql);
        }
        throw new PSQLException(GT.tr("Returning autogenerated keys by column index is not supported.", new Object[0]), PSQLState.NOT_IMPLEMENTED);
    }
    
    @Override
    public boolean execute(final String sql, final String[] columnNames) throws SQLException {
        if (columnNames != null && columnNames.length == 0) {
            return this.execute(sql);
        }
        this.wantsGeneratedKeysOnce = true;
        return this.executeCachedSql(sql, 0, columnNames);
    }
    
    @Override
    public int getResultSetHoldability() throws SQLException {
        return this.rsHoldability;
    }
    
    @Override
    public ResultSet createDriverResultSet(final Field[] fields, final List<Tuple> tuples) throws SQLException {
        return this.createResultSet(null, fields, tuples, null);
    }
    
    protected void transformQueriesAndParameters() throws SQLException {
    }
    
    static {
        NO_RETURNING_COLUMNS = new String[0];
        DEFAULT_FORCE_BINARY_TRANSFERS = Boolean.getBoolean("org.postgresql.forceBinary");
        CANCEL_TIMER_UPDATER = AtomicReferenceFieldUpdater.newUpdater(PgStatement.class, TimerTask.class, "cancelTimerTask");
        STATE_UPDATER = AtomicReferenceFieldUpdater.newUpdater(PgStatement.class, StatementCancelState.class, "statementState");
    }
    
    public class StatementResultHandler extends ResultHandlerBase
    {
        private ResultWrapper results;
        private ResultWrapper lastResult;
        
        ResultWrapper getResults() {
            return this.results;
        }
        
        private void append(final ResultWrapper newResult) {
            if (this.results == null) {
                this.results = newResult;
                this.lastResult = newResult;
            }
            else {
                Nullness.castNonNull(this.lastResult).append(newResult);
            }
        }
        
        @Override
        public void handleResultRows(final Query fromQuery, final Field[] fields, final List<Tuple> tuples, final ResultCursor cursor) {
            try {
                final ResultSet rs = PgStatement.this.createResultSet(fromQuery, fields, tuples, cursor);
                this.append(new ResultWrapper(rs));
            }
            catch (final SQLException e) {
                this.handleError(e);
            }
        }
        
        @Override
        public void handleCommandStatus(final String status, final long updateCount, final long insertOID) {
            this.append(new ResultWrapper(updateCount, insertOID));
        }
        
        @Override
        public void handleWarning(final SQLWarning warning) {
            PgStatement.this.addWarning(warning);
        }
    }
}
