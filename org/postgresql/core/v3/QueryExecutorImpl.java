// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core.v3;

import java.util.Collection;
import org.postgresql.jdbc.TimestampUtils;
import org.postgresql.util.PSQLWarning;
import org.postgresql.core.EncodingPredictor;
import org.postgresql.util.ServerErrorMessage;
import org.postgresql.PGNotification;
import org.postgresql.core.Notification;
import java.util.Collections;
import java.util.ArrayList;
import java.lang.ref.Reference;
import org.postgresql.core.Encoding;
import org.postgresql.core.Oid;
import org.postgresql.util.ByteStreamWriter;
import org.postgresql.copy.CopyOut;
import org.postgresql.copy.CopyIn;
import org.postgresql.util.internal.Nullness;
import org.postgresql.core.Utils;
import org.postgresql.copy.CopyOperation;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;
import java.sql.SQLWarning;
import org.postgresql.core.ResultHandlerBase;
import org.postgresql.core.ResultCursor;
import org.postgresql.core.Tuple;
import org.postgresql.core.ResultHandlerDelegate;
import org.postgresql.jdbc.AutoSave;
import org.postgresql.core.TransactionState;
import org.postgresql.core.PGBindException;
import org.postgresql.jdbc.BatchResultHandler;
import java.util.logging.Level;
import org.postgresql.core.ResultHandler;
import org.postgresql.core.ParameterList;
import java.util.List;
import org.postgresql.core.Parser;
import org.postgresql.core.Query;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import java.io.IOException;
import org.postgresql.core.QueryExecutor;
import org.postgresql.core.v3.replication.V3ReplicationProtocol;
import org.postgresql.PGProperty;
import org.postgresql.core.SqlCommandType;
import org.postgresql.core.NativeQuery;
import org.postgresql.core.SqlCommand;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Properties;
import org.postgresql.core.PGStream;
import java.util.Deque;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.PhantomReference;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.postgresql.core.CommandCompleteParser;
import org.postgresql.core.ReplicationProtocol;
import java.sql.SQLException;
import java.util.Set;
import java.util.TimeZone;
import org.postgresql.core.Field;
import java.util.logging.Logger;
import org.postgresql.core.QueryExecutorBase;

public class QueryExecutorImpl extends QueryExecutorBase
{
    private static final Logger LOGGER;
    private static final Field[] NO_FIELDS;
    private TimeZone timeZone;
    private String applicationName;
    private boolean integerDateTimes;
    private final Set<Integer> useBinaryReceiveForOids;
    private final Set<Integer> useBinarySendForOids;
    private final SimpleQuery sync;
    private short deallocateEpoch;
    private String lastSetSearchPathQuery;
    private SQLException transactionFailCause;
    private final ReplicationProtocol replicationProtocol;
    private final CommandCompleteParser commandCompleteParser;
    private Object lockedFor;
    private static final int MAX_BUFFERED_RECV_BYTES = 64000;
    private static final int NODATA_QUERY_RESPONSE_SIZE_BYTES = 250;
    AtomicBoolean processingCopyResults;
    private final HashMap<PhantomReference<SimpleQuery>, String> parsedQueryMap;
    private final ReferenceQueue<SimpleQuery> parsedQueryCleanupQueue;
    private final HashMap<PhantomReference<Portal>, String> openPortalMap;
    private final ReferenceQueue<Portal> openPortalCleanupQueue;
    private static final Portal UNNAMED_PORTAL;
    private final Deque<SimpleQuery> pendingParseQueue;
    private final Deque<Portal> pendingBindQueue;
    private final Deque<ExecuteRequest> pendingExecuteQueue;
    private final Deque<DescribeRequest> pendingDescribeStatementQueue;
    private final Deque<SimpleQuery> pendingDescribePortalQueue;
    private long nextUniqueID;
    private final boolean allowEncodingChanges;
    private final boolean cleanupSavePoints;
    private int estimatedReceiveBufferBytes;
    private final SimpleQuery beginTransactionQuery;
    private final SimpleQuery beginReadOnlyTransactionQuery;
    private final SimpleQuery emptyQuery;
    private final SimpleQuery autoSaveQuery;
    private final SimpleQuery releaseAutoSave;
    private final SimpleQuery restoreToAutoSave;
    
    public QueryExecutorImpl(final PGStream pgStream, final String user, final String database, final int cancelSignalTimeout, final Properties info) throws SQLException, IOException {
        super(pgStream, user, database, cancelSignalTimeout, info);
        this.useBinaryReceiveForOids = new HashSet<Integer>();
        this.useBinarySendForOids = new HashSet<Integer>();
        this.sync = (SimpleQuery)this.createQuery("SYNC", false, true, new String[0]).query;
        this.commandCompleteParser = new CommandCompleteParser();
        this.processingCopyResults = new AtomicBoolean(false);
        this.parsedQueryMap = new HashMap<PhantomReference<SimpleQuery>, String>();
        this.parsedQueryCleanupQueue = new ReferenceQueue<SimpleQuery>();
        this.openPortalMap = new HashMap<PhantomReference<Portal>, String>();
        this.openPortalCleanupQueue = new ReferenceQueue<Portal>();
        this.pendingParseQueue = new ArrayDeque<SimpleQuery>();
        this.pendingBindQueue = new ArrayDeque<Portal>();
        this.pendingExecuteQueue = new ArrayDeque<ExecuteRequest>();
        this.pendingDescribeStatementQueue = new ArrayDeque<DescribeRequest>();
        this.pendingDescribePortalQueue = new ArrayDeque<SimpleQuery>();
        this.nextUniqueID = 1L;
        this.estimatedReceiveBufferBytes = 0;
        this.beginTransactionQuery = new SimpleQuery(new NativeQuery("BEGIN", new int[0], false, SqlCommand.BLANK), null, false);
        this.beginReadOnlyTransactionQuery = new SimpleQuery(new NativeQuery("BEGIN READ ONLY", new int[0], false, SqlCommand.BLANK), null, false);
        this.emptyQuery = new SimpleQuery(new NativeQuery("", new int[0], false, SqlCommand.createStatementTypeInfo(SqlCommandType.BLANK)), null, false);
        this.autoSaveQuery = new SimpleQuery(new NativeQuery("SAVEPOINT PGJDBC_AUTOSAVE", new int[0], false, SqlCommand.BLANK), null, false);
        this.releaseAutoSave = new SimpleQuery(new NativeQuery("RELEASE SAVEPOINT PGJDBC_AUTOSAVE", new int[0], false, SqlCommand.BLANK), null, false);
        this.restoreToAutoSave = new SimpleQuery(new NativeQuery("ROLLBACK TO SAVEPOINT PGJDBC_AUTOSAVE", new int[0], false, SqlCommand.BLANK), null, false);
        this.allowEncodingChanges = PGProperty.ALLOW_ENCODING_CHANGES.getBoolean(info);
        this.cleanupSavePoints = PGProperty.CLEANUP_SAVEPOINTS.getBoolean(info);
        this.replicationProtocol = new V3ReplicationProtocol(this, pgStream);
        this.readStartupMessages();
    }
    
    @Override
    public int getProtocolVersion() {
        return 3;
    }
    
    private void lock(final Object obtainer) throws PSQLException {
        if (this.lockedFor == obtainer) {
            throw new PSQLException(GT.tr("Tried to obtain lock while already holding it", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
        this.waitOnLock();
        this.lockedFor = obtainer;
    }
    
    private void unlock(final Object holder) throws PSQLException {
        if (this.lockedFor != holder) {
            throw new PSQLException(GT.tr("Tried to break lock on database connection", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
        this.lockedFor = null;
        this.notify();
    }
    
    private void waitOnLock() throws PSQLException {
        while (this.lockedFor != null) {
            try {
                this.wait();
                continue;
            }
            catch (final InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new PSQLException(GT.tr("Interrupted while waiting to obtain lock on database connection", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE, ie);
            }
            break;
        }
    }
    
    boolean hasLock(final Object holder) {
        return this.lockedFor == holder;
    }
    
    @Override
    public Query createSimpleQuery(final String sql) throws SQLException {
        final List<NativeQuery> queries = Parser.parseJdbcSql(sql, this.getStandardConformingStrings(), false, true, this.isReWriteBatchedInsertsEnabled(), new String[0]);
        return this.wrap(queries);
    }
    
    @Override
    public Query wrap(final List<NativeQuery> queries) {
        if (queries.isEmpty()) {
            return this.emptyQuery;
        }
        if (queries.size() != 1) {
            final SimpleQuery[] subqueries = new SimpleQuery[queries.size()];
            final int[] offsets = new int[subqueries.length];
            int offset = 0;
            for (int i = 0; i < queries.size(); ++i) {
                final NativeQuery nativeQuery = queries.get(i);
                offsets[i] = offset;
                subqueries[i] = new SimpleQuery(nativeQuery, this, this.isColumnSanitiserDisabled());
                offset += nativeQuery.bindPositions.length;
            }
            return new CompositeQuery(subqueries, offsets);
        }
        final NativeQuery firstQuery = queries.get(0);
        if (this.isReWriteBatchedInsertsEnabled() && firstQuery.getCommand().isBatchedReWriteCompatible()) {
            final int valuesBraceOpenPosition = firstQuery.getCommand().getBatchRewriteValuesBraceOpenPosition();
            final int valuesBraceClosePosition = firstQuery.getCommand().getBatchRewriteValuesBraceClosePosition();
            return new BatchedQuery(firstQuery, this, valuesBraceOpenPosition, valuesBraceClosePosition, this.isColumnSanitiserDisabled());
        }
        return new SimpleQuery(firstQuery, this, this.isColumnSanitiserDisabled());
    }
    
    private int updateQueryMode(final int flags) {
        switch (this.getPreferQueryMode()) {
            case SIMPLE: {
                return flags | 0x400;
            }
            case EXTENDED: {
                return flags & 0xFFFFFBFF;
            }
            default: {
                return flags;
            }
        }
    }
    
    @Override
    public synchronized void execute(final Query query, ParameterList parameters, ResultHandler handler, final int maxRows, final int fetchSize, int flags) throws SQLException {
        this.waitOnLock();
        if (QueryExecutorImpl.LOGGER.isLoggable(Level.FINEST)) {
            QueryExecutorImpl.LOGGER.log(Level.FINEST, "  simple execute, handler={0}, maxRows={1}, fetchSize={2}, flags={3}", new Object[] { handler, maxRows, fetchSize, flags });
        }
        if (parameters == null) {
            parameters = SimpleQuery.NO_PARAMETERS;
        }
        flags = this.updateQueryMode(flags);
        final boolean describeOnly = (0x20 & flags) != 0x0;
        ((V3ParameterList)parameters).convertFunctionOutParameters();
        if (!describeOnly) {
            ((V3ParameterList)parameters).checkAllParametersSet();
        }
        boolean autosave = false;
        try {
            try {
                handler = this.sendQueryPreamble(handler, flags);
                autosave = this.sendAutomaticSavepoint(query, flags);
                this.sendQuery(query, (V3ParameterList)parameters, maxRows, fetchSize, flags, handler, null);
                if ((flags & 0x400) == 0x0) {
                    this.sendSync();
                }
                this.processResults(handler, flags);
                this.estimatedReceiveBufferBytes = 0;
            }
            catch (final PGBindException se) {
                this.sendSync();
                this.processResults(handler, flags);
                this.estimatedReceiveBufferBytes = 0;
                handler.handleError(new PSQLException(GT.tr("Unable to bind parameter values for statement.", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE, se.getIOException()));
            }
        }
        catch (final IOException e) {
            this.abort();
            handler.handleError(new PSQLException(GT.tr("An I/O error occurred while sending to the backend.", new Object[0]), PSQLState.CONNECTION_FAILURE, e));
        }
        try {
            handler.handleCompletion();
            if (this.cleanupSavePoints) {
                this.releaseSavePoint(autosave, flags);
            }
        }
        catch (final SQLException e2) {
            this.rollbackIfRequired(autosave, e2);
        }
    }
    
    private boolean sendAutomaticSavepoint(final Query query, final int flags) throws IOException {
        if (((flags & 0x10) == 0x0 || this.getTransactionState() == TransactionState.OPEN) && query != this.restoreToAutoSave && !query.getNativeSql().equalsIgnoreCase("COMMIT") && this.getAutoSave() != AutoSave.NEVER && (this.getAutoSave() == AutoSave.ALWAYS || !(query instanceof SimpleQuery) || ((SimpleQuery)query).getFields() != null)) {
            this.sendOneQuery(this.autoSaveQuery, SimpleQuery.NO_PARAMETERS, 1, 0, 1030);
            return true;
        }
        return false;
    }
    
    private void releaseSavePoint(final boolean autosave, final int flags) throws SQLException {
        if (autosave && this.getAutoSave() == AutoSave.ALWAYS && this.getTransactionState() == TransactionState.OPEN) {
            try {
                this.sendOneQuery(this.releaseAutoSave, SimpleQuery.NO_PARAMETERS, 1, 0, 1030);
            }
            catch (final IOException ex) {
                throw new PSQLException(GT.tr("Error releasing savepoint", new Object[0]), PSQLState.IO_ERROR);
            }
        }
    }
    
    private void rollbackIfRequired(final boolean autosave, final SQLException e) throws SQLException {
        if (autosave && this.getTransactionState() == TransactionState.FAILED) {
            if (this.getAutoSave() != AutoSave.ALWAYS) {
                if (!this.willHealOnRetry(e)) {
                    throw e;
                }
            }
            try {
                this.execute(this.restoreToAutoSave, SimpleQuery.NO_PARAMETERS, new ResultHandlerDelegate(null), 1, 0, 1030);
            }
            catch (final SQLException e2) {
                e.setNextException(e2);
            }
        }
        throw e;
    }
    
    @Override
    public synchronized void execute(final Query[] queries, final ParameterList[] parameterLists, final BatchResultHandler batchHandler, final int maxRows, final int fetchSize, int flags) throws SQLException {
        this.waitOnLock();
        if (QueryExecutorImpl.LOGGER.isLoggable(Level.FINEST)) {
            QueryExecutorImpl.LOGGER.log(Level.FINEST, "  batch execute {0} queries, handler={1}, maxRows={2}, fetchSize={3}, flags={4}", new Object[] { queries.length, batchHandler, maxRows, fetchSize, flags });
        }
        flags = this.updateQueryMode(flags);
        final boolean describeOnly = (0x20 & flags) != 0x0;
        if (!describeOnly) {
            for (final ParameterList parameterList : parameterLists) {
                if (parameterList != null) {
                    ((V3ParameterList)parameterList).checkAllParametersSet();
                }
            }
        }
        boolean autosave = false;
        ResultHandler handler = batchHandler;
        try {
            handler = this.sendQueryPreamble(batchHandler, flags);
            autosave = this.sendAutomaticSavepoint(queries[0], flags);
            this.estimatedReceiveBufferBytes = 0;
            for (int i = 0; i < queries.length; ++i) {
                final Query query = queries[i];
                V3ParameterList parameters = (V3ParameterList)parameterLists[i];
                if (parameters == null) {
                    parameters = SimpleQuery.NO_PARAMETERS;
                }
                this.sendQuery(query, parameters, maxRows, fetchSize, flags, handler, batchHandler);
                if (handler.getException() != null) {
                    break;
                }
            }
            if (handler.getException() == null) {
                if ((flags & 0x400) == 0x0) {
                    this.sendSync();
                }
                this.processResults(handler, flags);
                this.estimatedReceiveBufferBytes = 0;
            }
        }
        catch (final IOException e) {
            this.abort();
            handler.handleError(new PSQLException(GT.tr("An I/O error occurred while sending to the backend.", new Object[0]), PSQLState.CONNECTION_FAILURE, e));
        }
        try {
            handler.handleCompletion();
            if (this.cleanupSavePoints) {
                this.releaseSavePoint(autosave, flags);
            }
        }
        catch (final SQLException e2) {
            this.rollbackIfRequired(autosave, e2);
        }
    }
    
    private ResultHandler sendQueryPreamble(final ResultHandler delegateHandler, final int flags) throws IOException {
        this.processDeadParsedQueries();
        this.processDeadPortals();
        if ((flags & 0x10) != 0x0 || this.getTransactionState() != TransactionState.IDLE) {
            return delegateHandler;
        }
        int beginFlags = 2;
        if ((flags & 0x1) != 0x0) {
            beginFlags |= 0x1;
        }
        beginFlags |= 0x400;
        beginFlags = this.updateQueryMode(beginFlags);
        final SimpleQuery beginQuery = ((flags & 0x800) == 0x0) ? this.beginTransactionQuery : this.beginReadOnlyTransactionQuery;
        this.sendOneQuery(beginQuery, SimpleQuery.NO_PARAMETERS, 0, 0, beginFlags);
        return new ResultHandlerDelegate(delegateHandler) {
            private boolean sawBegin = false;
            
            @Override
            public void handleResultRows(final Query fromQuery, final Field[] fields, final List<Tuple> tuples, final ResultCursor cursor) {
                if (this.sawBegin) {
                    super.handleResultRows(fromQuery, fields, tuples, cursor);
                }
            }
            
            @Override
            public void handleCommandStatus(final String status, final long updateCount, final long insertOID) {
                if (!this.sawBegin) {
                    this.sawBegin = true;
                    if (!status.equals("BEGIN")) {
                        this.handleError(new PSQLException(GT.tr("Expected command status BEGIN, got {0}.", status), PSQLState.PROTOCOL_VIOLATION));
                    }
                }
                else {
                    super.handleCommandStatus(status, updateCount, insertOID);
                }
            }
        };
    }
    
    @Override
    public synchronized byte[] fastpathCall(final int fnid, final ParameterList parameters, final boolean suppressBegin) throws SQLException {
        this.waitOnLock();
        if (!suppressBegin) {
            this.doSubprotocolBegin();
        }
        try {
            this.sendFastpathCall(fnid, (SimpleParameterList)parameters);
            return this.receiveFastpathResult();
        }
        catch (final IOException ioe) {
            this.abort();
            throw new PSQLException(GT.tr("An I/O error occurred while sending to the backend.", new Object[0]), PSQLState.CONNECTION_FAILURE, ioe);
        }
    }
    
    public void doSubprotocolBegin() throws SQLException {
        if (this.getTransactionState() == TransactionState.IDLE) {
            QueryExecutorImpl.LOGGER.log(Level.FINEST, "Issuing BEGIN before fastpath or copy call.");
            final ResultHandler handler = new ResultHandlerBase() {
                private boolean sawBegin = false;
                
                @Override
                public void handleCommandStatus(final String status, final long updateCount, final long insertOID) {
                    if (!this.sawBegin) {
                        if (!status.equals("BEGIN")) {
                            this.handleError(new PSQLException(GT.tr("Expected command status BEGIN, got {0}.", status), PSQLState.PROTOCOL_VIOLATION));
                        }
                        this.sawBegin = true;
                    }
                    else {
                        this.handleError(new PSQLException(GT.tr("Unexpected command status: {0}.", status), PSQLState.PROTOCOL_VIOLATION));
                    }
                }
                
                @Override
                public void handleWarning(final SQLWarning warning) {
                    this.handleError(warning);
                }
            };
            try {
                int beginFlags = 1027;
                beginFlags = this.updateQueryMode(beginFlags);
                this.sendOneQuery(this.beginTransactionQuery, SimpleQuery.NO_PARAMETERS, 0, 0, beginFlags);
                this.sendSync();
                this.processResults(handler, 0);
                this.estimatedReceiveBufferBytes = 0;
            }
            catch (final IOException ioe) {
                throw new PSQLException(GT.tr("An I/O error occurred while sending to the backend.", new Object[0]), PSQLState.CONNECTION_FAILURE, ioe);
            }
        }
    }
    
    @Override
    public ParameterList createFastpathParameters(final int count) {
        return new SimpleParameterList(count, this);
    }
    
    private void sendFastpathCall(final int fnid, final SimpleParameterList params) throws SQLException, IOException {
        if (QueryExecutorImpl.LOGGER.isLoggable(Level.FINEST)) {
            QueryExecutorImpl.LOGGER.log(Level.FINEST, " FE=> FunctionCall({0}, {1} params)", new Object[] { fnid, params.getParameterCount() });
        }
        final int paramCount = params.getParameterCount();
        int encodedSize = 0;
        for (int i = 1; i <= paramCount; ++i) {
            if (params.isNull(i)) {
                encodedSize += 4;
            }
            else {
                encodedSize += 4 + params.getV3Length(i);
            }
        }
        this.pgStream.sendChar(70);
        this.pgStream.sendInteger4(10 + 2 * paramCount + 2 + encodedSize + 2);
        this.pgStream.sendInteger4(fnid);
        this.pgStream.sendInteger2(paramCount);
        for (int i = 1; i <= paramCount; ++i) {
            this.pgStream.sendInteger2(params.isBinary(i) ? 1 : 0);
        }
        this.pgStream.sendInteger2(paramCount);
        for (int i = 1; i <= paramCount; ++i) {
            if (params.isNull(i)) {
                this.pgStream.sendInteger4(-1);
            }
            else {
                this.pgStream.sendInteger4(params.getV3Length(i));
                params.writeV3Value(i, this.pgStream);
            }
        }
        this.pgStream.sendInteger2(1);
        this.pgStream.flush();
    }
    
    @Override
    public synchronized void processNotifies() throws SQLException {
        this.processNotifies(-1);
    }
    
    @Override
    public synchronized void processNotifies(int timeoutMillis) throws SQLException {
        this.waitOnLock();
        if (this.getTransactionState() != TransactionState.IDLE) {
            return;
        }
        if (this.hasNotifications()) {
            timeoutMillis = -1;
        }
        final boolean useTimeout = timeoutMillis > 0;
        long startTime = 0L;
        int oldTimeout = 0;
        while (true) {
            if (useTimeout) {
                startTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
                try {
                    oldTimeout = this.pgStream.getSocket().getSoTimeout();
                }
                catch (final SocketException e) {
                    throw new PSQLException(GT.tr("An error occurred while trying to get the socket timeout.", new Object[0]), PSQLState.CONNECTION_FAILURE, e);
                }
                try {
                    while (timeoutMillis >= 0 || this.pgStream.hasMessagePending()) {
                        if (useTimeout && timeoutMillis >= 0) {
                            this.setSocketTimeout(timeoutMillis);
                        }
                        final int c = this.pgStream.receiveChar();
                        if (useTimeout && timeoutMillis >= 0) {
                            this.setSocketTimeout(0);
                        }
                        switch (c) {
                            case 65: {
                                this.receiveAsyncNotify();
                                timeoutMillis = -1;
                                continue;
                            }
                            case 69: {
                                throw this.receiveErrorResponse();
                            }
                            case 78: {
                                final SQLWarning warning = this.receiveNoticeResponse();
                                this.addWarning(warning);
                                if (!useTimeout) {
                                    continue;
                                }
                                final long newTimeMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
                                timeoutMillis += (int)(startTime - newTimeMillis);
                                startTime = newTimeMillis;
                                if (timeoutMillis != 0) {
                                    continue;
                                }
                                timeoutMillis = -1;
                                continue;
                            }
                            default: {
                                throw new PSQLException(GT.tr("Unknown Response Type {0}.", (char)c), PSQLState.CONNECTION_FAILURE);
                            }
                        }
                    }
                }
                catch (final SocketTimeoutException ex) {}
                catch (final IOException ioe) {
                    throw new PSQLException(GT.tr("An I/O error occurred while sending to the backend.", new Object[0]), PSQLState.CONNECTION_FAILURE, ioe);
                }
                finally {
                    if (useTimeout) {
                        this.setSocketTimeout(oldTimeout);
                    }
                }
                return;
            }
            continue;
        }
    }
    
    private void setSocketTimeout(final int millis) throws PSQLException {
        try {
            final Socket s = this.pgStream.getSocket();
            if (!s.isClosed()) {
                this.pgStream.setNetworkTimeout(millis);
            }
        }
        catch (final IOException e) {
            throw new PSQLException(GT.tr("An error occurred while trying to reset the socket timeout.", new Object[0]), PSQLState.CONNECTION_FAILURE, e);
        }
    }
    
    private byte[] receiveFastpathResult() throws IOException, SQLException {
        boolean endQuery = false;
        SQLException error = null;
        byte[] returnValue = null;
        while (!endQuery) {
            final int c = this.pgStream.receiveChar();
            switch (c) {
                case 65: {
                    this.receiveAsyncNotify();
                    continue;
                }
                case 69: {
                    final SQLException newError = this.receiveErrorResponse();
                    if (error == null) {
                        error = newError;
                        continue;
                    }
                    error.setNextException(newError);
                    continue;
                }
                case 78: {
                    final SQLWarning warning = this.receiveNoticeResponse();
                    this.addWarning(warning);
                    continue;
                }
                case 90: {
                    this.receiveRFQ();
                    endQuery = true;
                    continue;
                }
                case 86: {
                    final int msgLen = this.pgStream.receiveInteger4();
                    final int valueLen = this.pgStream.receiveInteger4();
                    QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE FunctionCallResponse({0} bytes)", valueLen);
                    if (valueLen != -1) {
                        final byte[] buf = new byte[valueLen];
                        this.pgStream.receive(buf, 0, valueLen);
                        returnValue = buf;
                        continue;
                    }
                    continue;
                }
                case 83: {
                    try {
                        this.receiveParameterStatus();
                    }
                    catch (final SQLException e) {
                        if (error == null) {
                            error = e;
                        }
                        else {
                            error.setNextException(e);
                        }
                        endQuery = true;
                    }
                    continue;
                }
                default: {
                    throw new PSQLException(GT.tr("Unknown Response Type {0}.", (char)c), PSQLState.CONNECTION_FAILURE);
                }
            }
        }
        if (error != null) {
            throw error;
        }
        return returnValue;
    }
    
    @Override
    public synchronized CopyOperation startCopy(final String sql, final boolean suppressBegin) throws SQLException {
        this.waitOnLock();
        if (!suppressBegin) {
            this.doSubprotocolBegin();
        }
        final byte[] buf = Utils.encodeUTF8(sql);
        try {
            QueryExecutorImpl.LOGGER.log(Level.FINEST, " FE=> Query(CopyStart)");
            this.pgStream.sendChar(81);
            this.pgStream.sendInteger4(buf.length + 4 + 1);
            this.pgStream.send(buf);
            this.pgStream.sendChar(0);
            this.pgStream.flush();
            return Nullness.castNonNull(this.processCopyResults(null, true));
        }
        catch (final IOException ioe) {
            throw new PSQLException(GT.tr("Database connection failed when starting copy", new Object[0]), PSQLState.CONNECTION_FAILURE, ioe);
        }
    }
    
    private synchronized void initCopy(final CopyOperationImpl op) throws SQLException, IOException {
        this.pgStream.receiveInteger4();
        final int rowFormat = this.pgStream.receiveChar();
        final int numFields = this.pgStream.receiveInteger2();
        final int[] fieldFormats = new int[numFields];
        for (int i = 0; i < numFields; ++i) {
            fieldFormats[i] = this.pgStream.receiveInteger2();
        }
        this.lock(op);
        op.init(this, rowFormat, fieldFormats);
    }
    
    public void cancelCopy(final CopyOperationImpl op) throws SQLException {
        if (!this.hasLock(op)) {
            throw new PSQLException(GT.tr("Tried to cancel an inactive copy operation", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
        SQLException error = null;
        int errors = 0;
        try {
            if (op instanceof CopyIn) {
                synchronized (this) {
                    QueryExecutorImpl.LOGGER.log(Level.FINEST, "FE => CopyFail");
                    final byte[] msg = Utils.encodeUTF8("Copy cancel requested");
                    this.pgStream.sendChar(102);
                    this.pgStream.sendInteger4(5 + msg.length);
                    this.pgStream.send(msg);
                    this.pgStream.sendChar(0);
                    this.pgStream.flush();
                    do {
                        try {
                            this.processCopyResults(op, true);
                        }
                        catch (final SQLException se) {
                            ++errors;
                            if (error != null) {
                                SQLException e;
                                SQLException next;
                                for (e = se; (next = e.getNextException()) != null; e = next) {}
                                e.setNextException(error);
                            }
                            error = se;
                        }
                    } while (this.hasLock(op));
                }
            }
            else if (op instanceof CopyOut) {
                this.sendQueryCancel();
            }
        }
        catch (final IOException ioe) {
            throw new PSQLException(GT.tr("Database connection failed when canceling copy operation", new Object[0]), PSQLState.CONNECTION_FAILURE, ioe);
        }
        finally {
            synchronized (this) {
                if (this.hasLock(op)) {
                    this.unlock(op);
                }
            }
        }
        if (op instanceof CopyIn) {
            if (errors < 1) {
                throw new PSQLException(GT.tr("Missing expected error response to copy cancel request", new Object[0]), PSQLState.COMMUNICATION_ERROR);
            }
            if (errors > 1) {
                throw new PSQLException(GT.tr("Got {0} error responses to single copy cancel request", String.valueOf(errors)), PSQLState.COMMUNICATION_ERROR, error);
            }
        }
    }
    
    public synchronized long endCopy(final CopyOperationImpl op) throws SQLException {
        if (!this.hasLock(op)) {
            throw new PSQLException(GT.tr("Tried to end inactive copy", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
        try {
            QueryExecutorImpl.LOGGER.log(Level.FINEST, " FE=> CopyDone");
            this.pgStream.sendChar(99);
            this.pgStream.sendInteger4(4);
            this.pgStream.flush();
            do {
                this.processCopyResults(op, true);
            } while (this.hasLock(op));
            return op.getHandledRowCount();
        }
        catch (final IOException ioe) {
            throw new PSQLException(GT.tr("Database connection failed when ending copy", new Object[0]), PSQLState.CONNECTION_FAILURE, ioe);
        }
    }
    
    public synchronized void writeToCopy(final CopyOperationImpl op, final byte[] data, final int off, final int siz) throws SQLException {
        if (!this.hasLock(op)) {
            throw new PSQLException(GT.tr("Tried to write to an inactive copy operation", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
        QueryExecutorImpl.LOGGER.log(Level.FINEST, " FE=> CopyData({0})", siz);
        try {
            this.pgStream.sendChar(100);
            this.pgStream.sendInteger4(siz + 4);
            this.pgStream.send(data, off, siz);
        }
        catch (final IOException ioe) {
            throw new PSQLException(GT.tr("Database connection failed when writing to copy", new Object[0]), PSQLState.CONNECTION_FAILURE, ioe);
        }
    }
    
    public synchronized void writeToCopy(final CopyOperationImpl op, final ByteStreamWriter from) throws SQLException {
        if (!this.hasLock(op)) {
            throw new PSQLException(GT.tr("Tried to write to an inactive copy operation", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
        final int siz = from.getLength();
        QueryExecutorImpl.LOGGER.log(Level.FINEST, " FE=> CopyData({0})", siz);
        try {
            this.pgStream.sendChar(100);
            this.pgStream.sendInteger4(siz + 4);
            this.pgStream.send(from);
        }
        catch (final IOException ioe) {
            throw new PSQLException(GT.tr("Database connection failed when writing to copy", new Object[0]), PSQLState.CONNECTION_FAILURE, ioe);
        }
    }
    
    public synchronized void flushCopy(final CopyOperationImpl op) throws SQLException {
        if (!this.hasLock(op)) {
            throw new PSQLException(GT.tr("Tried to write to an inactive copy operation", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
        try {
            this.pgStream.flush();
        }
        catch (final IOException ioe) {
            throw new PSQLException(GT.tr("Database connection failed when writing to copy", new Object[0]), PSQLState.CONNECTION_FAILURE, ioe);
        }
    }
    
    synchronized void readFromCopy(final CopyOperationImpl op, final boolean block) throws SQLException {
        if (!this.hasLock(op)) {
            throw new PSQLException(GT.tr("Tried to read from inactive copy", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
        try {
            this.processCopyResults(op, block);
        }
        catch (final IOException ioe) {
            throw new PSQLException(GT.tr("Database connection failed when reading from copy", new Object[0]), PSQLState.CONNECTION_FAILURE, ioe);
        }
    }
    
    CopyOperationImpl processCopyResults(CopyOperationImpl op, boolean block) throws SQLException, IOException {
        if (this.pgStream.isClosed()) {
            throw new PSQLException(GT.tr("PGStream is closed", new Object[0]), PSQLState.CONNECTION_DOES_NOT_EXIST);
        }
        if (!this.processingCopyResults.compareAndSet(false, true)) {
            QueryExecutorImpl.LOGGER.log(Level.INFO, "Ignoring request to process copy results, already processing");
            return null;
        }
        try {
            boolean endReceiving = false;
            SQLException error = null;
            SQLException errors = null;
            while (!endReceiving && (block || this.pgStream.hasMessagePending())) {
                if (!block) {
                    final int c = this.pgStream.peekChar();
                    if (c == 67) {
                        QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE CommandStatus, Ignored until CopyDone");
                        break;
                    }
                }
                final int c = this.pgStream.receiveChar();
                switch (c) {
                    case 65: {
                        QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE Asynchronous Notification while copying");
                        this.receiveAsyncNotify();
                        break;
                    }
                    case 78: {
                        QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE Notification while copying");
                        this.addWarning(this.receiveNoticeResponse());
                        break;
                    }
                    case 67: {
                        final String status = this.receiveCommandStatus();
                        try {
                            if (op == null) {
                                throw new PSQLException(GT.tr("Received CommandComplete ''{0}'' without an active copy operation", status), PSQLState.OBJECT_NOT_IN_STATE);
                            }
                            op.handleCommandStatus(status);
                        }
                        catch (final SQLException se) {
                            error = se;
                        }
                        block = true;
                        break;
                    }
                    case 69: {
                        error = this.receiveErrorResponse();
                        block = true;
                        break;
                    }
                    case 71: {
                        QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE CopyInResponse");
                        if (op != null) {
                            error = new PSQLException(GT.tr("Got CopyInResponse from server during an active {0}", op.getClass().getName()), PSQLState.OBJECT_NOT_IN_STATE);
                        }
                        op = new CopyInImpl();
                        this.initCopy(op);
                        endReceiving = true;
                        break;
                    }
                    case 72: {
                        QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE CopyOutResponse");
                        if (op != null) {
                            error = new PSQLException(GT.tr("Got CopyOutResponse from server during an active {0}", op.getClass().getName()), PSQLState.OBJECT_NOT_IN_STATE);
                        }
                        op = new CopyOutImpl();
                        this.initCopy(op);
                        endReceiving = true;
                        break;
                    }
                    case 87: {
                        QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE CopyBothResponse");
                        if (op != null) {
                            error = new PSQLException(GT.tr("Got CopyBothResponse from server during an active {0}", op.getClass().getName()), PSQLState.OBJECT_NOT_IN_STATE);
                        }
                        op = new CopyDualImpl();
                        this.initCopy(op);
                        endReceiving = true;
                        break;
                    }
                    case 100: {
                        QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE CopyData");
                        final int len = this.pgStream.receiveInteger4() - 4;
                        assert len > 0 : "Copy Data length must be greater than 4";
                        final byte[] buf = this.pgStream.receive(len);
                        if (op == null) {
                            error = new PSQLException(GT.tr("Got CopyData without an active copy operation", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
                        }
                        else if (!(op instanceof CopyOut)) {
                            error = new PSQLException(GT.tr("Unexpected copydata from server for {0}", op.getClass().getName()), PSQLState.COMMUNICATION_ERROR);
                        }
                        else {
                            op.handleCopydata(buf);
                        }
                        endReceiving = true;
                        break;
                    }
                    case 99: {
                        QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE CopyDone");
                        final int len = this.pgStream.receiveInteger4() - 4;
                        if (len > 0) {
                            this.pgStream.receive(len);
                        }
                        if (!(op instanceof CopyOut)) {
                            error = new PSQLException("Got CopyDone while not copying from server", PSQLState.OBJECT_NOT_IN_STATE);
                        }
                        block = true;
                        break;
                    }
                    case 83: {
                        try {
                            this.receiveParameterStatus();
                        }
                        catch (final SQLException e) {
                            error = e;
                            endReceiving = true;
                        }
                        break;
                    }
                    case 90: {
                        this.receiveRFQ();
                        if (op != null && this.hasLock(op)) {
                            this.unlock(op);
                        }
                        op = null;
                        endReceiving = true;
                        break;
                    }
                    case 84: {
                        QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE RowDescription (during copy ignored)");
                        this.skipMessage();
                        break;
                    }
                    case 68: {
                        QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE DataRow (during copy ignored)");
                        this.skipMessage();
                        break;
                    }
                    default: {
                        throw new IOException(GT.tr("Unexpected packet type during copy: {0}", Integer.toString(c)));
                    }
                }
                if (error != null) {
                    if (errors != null) {
                        error.setNextException(errors);
                    }
                    errors = error;
                    error = null;
                }
            }
            if (errors != null) {
                throw errors;
            }
            return op;
        }
        finally {
            this.processingCopyResults.set(false);
        }
    }
    
    private void flushIfDeadlockRisk(final Query query, boolean disallowBatching, final ResultHandler resultHandler, final BatchResultHandler batchHandler, final int flags) throws IOException {
        this.estimatedReceiveBufferBytes += 250;
        final SimpleQuery sq = (SimpleQuery)query;
        if (sq.isStatementDescribed()) {
            final int maxResultRowSize = sq.getMaxResultRowSize();
            if (maxResultRowSize >= 0) {
                this.estimatedReceiveBufferBytes += maxResultRowSize;
            }
            else {
                QueryExecutorImpl.LOGGER.log(Level.FINEST, "Couldn't estimate result size or result size unbounded, disabling batching for this query.");
                disallowBatching = true;
            }
        }
        if (disallowBatching || this.estimatedReceiveBufferBytes >= 64000) {
            QueryExecutorImpl.LOGGER.log(Level.FINEST, "Forcing Sync, receive buffer full or batching disallowed");
            this.sendSync();
            this.processResults(resultHandler, flags);
            this.estimatedReceiveBufferBytes = 0;
            if (batchHandler != null) {
                batchHandler.secureProgress();
            }
        }
    }
    
    private void sendQuery(final Query query, final V3ParameterList parameters, final int maxRows, final int fetchSize, final int flags, final ResultHandler resultHandler, final BatchResultHandler batchHandler) throws IOException, SQLException {
        final Query[] subqueries = query.getSubqueries();
        final SimpleParameterList[] subparams = parameters.getSubparams();
        final boolean disallowBatching = (flags & 0x80) != 0x0;
        if (subqueries == null) {
            this.flushIfDeadlockRisk(query, disallowBatching, resultHandler, batchHandler, flags);
            if (resultHandler.getException() == null) {
                this.sendOneQuery((SimpleQuery)query, (SimpleParameterList)parameters, maxRows, fetchSize, flags);
            }
        }
        else {
            for (int i = 0; i < subqueries.length; ++i) {
                final Query subquery = subqueries[i];
                this.flushIfDeadlockRisk(subquery, disallowBatching, resultHandler, batchHandler, flags);
                if (resultHandler.getException() != null) {
                    break;
                }
                SimpleParameterList subparam = SimpleQuery.NO_PARAMETERS;
                if (subparams != null) {
                    subparam = subparams[i];
                }
                this.sendOneQuery((SimpleQuery)subquery, subparam, maxRows, fetchSize, flags);
            }
        }
    }
    
    private void sendSync() throws IOException {
        QueryExecutorImpl.LOGGER.log(Level.FINEST, " FE=> Sync");
        this.pgStream.sendChar(83);
        this.pgStream.sendInteger4(4);
        this.pgStream.flush();
        this.pendingExecuteQueue.add(new ExecuteRequest(this.sync, null, true));
        this.pendingDescribePortalQueue.add(this.sync);
    }
    
    private void sendParse(final SimpleQuery query, final SimpleParameterList params, final boolean oneShot) throws IOException {
        final int[] typeOIDs = params.getTypeOIDs();
        if (query.isPreparedFor(typeOIDs, this.deallocateEpoch)) {
            return;
        }
        query.unprepare();
        this.processDeadParsedQueries();
        query.setFields(null);
        String statementName = null;
        if (!oneShot) {
            statementName = "S_" + this.nextUniqueID++;
            query.setStatementName(statementName, this.deallocateEpoch);
            query.setPrepareTypes(typeOIDs);
            this.registerParsedQuery(query, statementName);
        }
        final byte[] encodedStatementName = query.getEncodedStatementName();
        final String nativeSql = query.getNativeSql();
        if (QueryExecutorImpl.LOGGER.isLoggable(Level.FINEST)) {
            final StringBuilder sbuf = new StringBuilder(" FE=> Parse(stmt=" + statementName + ",query=\"");
            sbuf.append(nativeSql);
            sbuf.append("\",oids={");
            for (int i = 1; i <= params.getParameterCount(); ++i) {
                if (i != 1) {
                    sbuf.append(",");
                }
                sbuf.append(params.getTypeOID(i));
            }
            sbuf.append("})");
            QueryExecutorImpl.LOGGER.log(Level.FINEST, sbuf.toString());
        }
        final byte[] queryUtf8 = Utils.encodeUTF8(nativeSql);
        final int encodedSize = 4 + ((encodedStatementName == null) ? 0 : encodedStatementName.length) + 1 + queryUtf8.length + 1 + 2 + 4 * params.getParameterCount();
        this.pgStream.sendChar(80);
        this.pgStream.sendInteger4(encodedSize);
        if (encodedStatementName != null) {
            this.pgStream.send(encodedStatementName);
        }
        this.pgStream.sendChar(0);
        this.pgStream.send(queryUtf8);
        this.pgStream.sendChar(0);
        this.pgStream.sendInteger2(params.getParameterCount());
        for (int j = 1; j <= params.getParameterCount(); ++j) {
            this.pgStream.sendInteger4(params.getTypeOID(j));
        }
        this.pendingParseQueue.add(query);
    }
    
    private void sendBind(final SimpleQuery query, final SimpleParameterList params, final Portal portal, final boolean noBinaryTransfer) throws IOException {
        final String statementName = query.getStatementName();
        final byte[] encodedStatementName = query.getEncodedStatementName();
        final byte[] encodedPortalName = (byte[])((portal == null) ? null : portal.getEncodedPortalName());
        if (QueryExecutorImpl.LOGGER.isLoggable(Level.FINEST)) {
            final StringBuilder sbuf = new StringBuilder(" FE=> Bind(stmt=" + statementName + ",portal=" + portal);
            for (int i = 1; i <= params.getParameterCount(); ++i) {
                sbuf.append(",$").append(i).append("=<").append(params.toString(i, true)).append(">,type=").append(Oid.toString(params.getTypeOID(i)));
            }
            sbuf.append(")");
            QueryExecutorImpl.LOGGER.log(Level.FINEST, sbuf.toString());
        }
        long encodedSize = 0L;
        for (int j = 1; j <= params.getParameterCount(); ++j) {
            if (params.isNull(j)) {
                encodedSize += 4L;
            }
            else {
                encodedSize += 4L + params.getV3Length(j);
            }
        }
        final Field[] fields = query.getFields();
        if (!noBinaryTransfer && query.needUpdateFieldFormats() && fields != null) {
            for (final Field field : fields) {
                if (this.useBinary(field)) {
                    field.setFormat(1);
                    query.setHasBinaryFields(true);
                }
            }
        }
        if (noBinaryTransfer && query.hasBinaryFields() && fields != null) {
            for (final Field field : fields) {
                if (field.getFormat() != 0) {
                    field.setFormat(0);
                }
            }
            query.resetNeedUpdateFieldFormats();
            query.setHasBinaryFields(false);
        }
        final int numBinaryFields = (!noBinaryTransfer && query.hasBinaryFields() && fields != null) ? fields.length : 0;
        encodedSize = 4 + ((encodedPortalName == null) ? 0 : encodedPortalName.length) + 1 + ((encodedStatementName == null) ? 0 : encodedStatementName.length) + 1 + 2 + params.getParameterCount() * 2 + 2 + encodedSize + 2L + numBinaryFields * 2;
        if (encodedSize > 1073741823L) {
            throw new PGBindException(new IOException(GT.tr("Bind message length {0} too long.  This can be caused by very large or incorrect length specifications on InputStream parameters.", encodedSize)));
        }
        this.pgStream.sendChar(66);
        this.pgStream.sendInteger4((int)encodedSize);
        if (encodedPortalName != null) {
            this.pgStream.send(encodedPortalName);
        }
        this.pgStream.sendChar(0);
        if (encodedStatementName != null) {
            this.pgStream.send(encodedStatementName);
        }
        this.pgStream.sendChar(0);
        this.pgStream.sendInteger2(params.getParameterCount());
        for (int k = 1; k <= params.getParameterCount(); ++k) {
            this.pgStream.sendInteger2(params.isBinary(k) ? 1 : 0);
        }
        this.pgStream.sendInteger2(params.getParameterCount());
        PGBindException bindException = null;
        for (int l = 1; l <= params.getParameterCount(); ++l) {
            if (params.isNull(l)) {
                this.pgStream.sendInteger4(-1);
            }
            else {
                this.pgStream.sendInteger4(params.getV3Length(l));
                try {
                    params.writeV3Value(l, this.pgStream);
                }
                catch (final PGBindException be) {
                    bindException = be;
                }
            }
        }
        this.pgStream.sendInteger2(numBinaryFields);
        for (int l = 0; fields != null && l < numBinaryFields; ++l) {
            this.pgStream.sendInteger2(fields[l].getFormat());
        }
        this.pendingBindQueue.add((portal == null) ? QueryExecutorImpl.UNNAMED_PORTAL : portal);
        if (bindException != null) {
            throw bindException;
        }
    }
    
    private boolean useBinary(final Field field) {
        final int oid = field.getOID();
        return this.useBinaryForReceive(oid);
    }
    
    private void sendDescribePortal(final SimpleQuery query, final Portal portal) throws IOException {
        QueryExecutorImpl.LOGGER.log(Level.FINEST, " FE=> Describe(portal={0})", portal);
        final byte[] encodedPortalName = (byte[])((portal == null) ? null : portal.getEncodedPortalName());
        final int encodedSize = 5 + ((encodedPortalName == null) ? 0 : encodedPortalName.length) + 1;
        this.pgStream.sendChar(68);
        this.pgStream.sendInteger4(encodedSize);
        this.pgStream.sendChar(80);
        if (encodedPortalName != null) {
            this.pgStream.send(encodedPortalName);
        }
        this.pgStream.sendChar(0);
        this.pendingDescribePortalQueue.add(query);
        query.setPortalDescribed(true);
    }
    
    private void sendDescribeStatement(final SimpleQuery query, final SimpleParameterList params, final boolean describeOnly) throws IOException {
        QueryExecutorImpl.LOGGER.log(Level.FINEST, " FE=> Describe(statement={0})", query.getStatementName());
        final byte[] encodedStatementName = query.getEncodedStatementName();
        final int encodedSize = 5 + ((encodedStatementName == null) ? 0 : encodedStatementName.length) + 1;
        this.pgStream.sendChar(68);
        this.pgStream.sendInteger4(encodedSize);
        this.pgStream.sendChar(83);
        if (encodedStatementName != null) {
            this.pgStream.send(encodedStatementName);
        }
        this.pgStream.sendChar(0);
        this.pendingDescribeStatementQueue.add(new DescribeRequest(query, params, describeOnly, query.getStatementName()));
        this.pendingDescribePortalQueue.add(query);
        query.setStatementDescribed(true);
        query.setPortalDescribed(true);
    }
    
    private void sendExecute(final SimpleQuery query, final Portal portal, final int limit) throws IOException {
        if (QueryExecutorImpl.LOGGER.isLoggable(Level.FINEST)) {
            QueryExecutorImpl.LOGGER.log(Level.FINEST, " FE=> Execute(portal={0},limit={1})", new Object[] { portal, limit });
        }
        final byte[] encodedPortalName = (byte[])((portal == null) ? null : portal.getEncodedPortalName());
        final int encodedSize = (encodedPortalName == null) ? 0 : encodedPortalName.length;
        this.pgStream.sendChar(69);
        this.pgStream.sendInteger4(5 + encodedSize + 4);
        if (encodedPortalName != null) {
            this.pgStream.send(encodedPortalName);
        }
        this.pgStream.sendChar(0);
        this.pgStream.sendInteger4(limit);
        this.pendingExecuteQueue.add(new ExecuteRequest(query, portal, false));
    }
    
    private void sendClosePortal(final String portalName) throws IOException {
        QueryExecutorImpl.LOGGER.log(Level.FINEST, " FE=> ClosePortal({0})", portalName);
        final byte[] encodedPortalName = (byte[])((portalName == null) ? null : Utils.encodeUTF8(portalName));
        final int encodedSize = (encodedPortalName == null) ? 0 : encodedPortalName.length;
        this.pgStream.sendChar(67);
        this.pgStream.sendInteger4(6 + encodedSize);
        this.pgStream.sendChar(80);
        if (encodedPortalName != null) {
            this.pgStream.send(encodedPortalName);
        }
        this.pgStream.sendChar(0);
    }
    
    private void sendCloseStatement(final String statementName) throws IOException {
        QueryExecutorImpl.LOGGER.log(Level.FINEST, " FE=> CloseStatement({0})", statementName);
        final byte[] encodedStatementName = Utils.encodeUTF8(statementName);
        this.pgStream.sendChar(67);
        this.pgStream.sendInteger4(5 + encodedStatementName.length + 1);
        this.pgStream.sendChar(83);
        this.pgStream.send(encodedStatementName);
        this.pgStream.sendChar(0);
    }
    
    private void sendOneQuery(final SimpleQuery query, final SimpleParameterList params, final int maxRows, final int fetchSize, final int flags) throws IOException {
        final boolean asSimple = (flags & 0x400) != 0x0;
        if (asSimple) {
            assert (flags & 0x20) == 0x0 : "Simple mode does not support describe requests. sql = " + query.getNativeSql() + ", flags = " + flags;
            this.sendSimpleQuery(query, params);
        }
        else {
            assert !query.getNativeQuery().multiStatement : "Queries that might contain ; must be executed with QueryExecutor.QUERY_EXECUTE_AS_SIMPLE mode. Given query is " + query.getNativeSql();
            final boolean noResults = (flags & 0x4) != 0x0;
            final boolean noMeta = (flags & 0x2) != 0x0;
            final boolean describeOnly = (flags & 0x20) != 0x0;
            final boolean usePortal = (flags & 0x8) != 0x0 && !noResults && !noMeta && fetchSize > 0 && !describeOnly;
            final boolean oneShot = (flags & 0x1) != 0x0;
            final boolean noBinaryTransfer = (flags & 0x100) != 0x0;
            final boolean forceDescribePortal = (flags & 0x200) != 0x0;
            int rows;
            if (noResults) {
                rows = 1;
            }
            else if (!usePortal) {
                rows = maxRows;
            }
            else if (maxRows != 0 && fetchSize > maxRows) {
                rows = maxRows;
            }
            else {
                rows = fetchSize;
            }
            this.sendParse(query, params, oneShot);
            final boolean queryHasUnknown = query.hasUnresolvedTypes();
            final boolean paramsHasUnknown = params.hasUnresolvedTypes();
            final boolean describeStatement = describeOnly || (!oneShot && paramsHasUnknown && queryHasUnknown && !query.isStatementDescribed());
            if (!describeStatement && paramsHasUnknown && !queryHasUnknown) {
                final int[] queryOIDs = Nullness.castNonNull(query.getPrepareTypes());
                final int[] paramOIDs = params.getTypeOIDs();
                for (int i = 0; i < paramOIDs.length; ++i) {
                    if (paramOIDs[i] == 0) {
                        params.setResolvedType(i + 1, queryOIDs[i]);
                    }
                }
            }
            if (describeStatement) {
                this.sendDescribeStatement(query, params, describeOnly);
                if (describeOnly) {
                    return;
                }
            }
            Portal portal = null;
            if (usePortal) {
                final String portalName = "C_" + this.nextUniqueID++;
                portal = new Portal(query, portalName);
            }
            this.sendBind(query, params, portal, noBinaryTransfer);
            if (!noMeta && !describeStatement && (!query.isPortalDescribed() || forceDescribePortal)) {
                this.sendDescribePortal(query, portal);
            }
            this.sendExecute(query, portal, rows);
        }
    }
    
    private void sendSimpleQuery(final SimpleQuery query, final SimpleParameterList params) throws IOException {
        final String nativeSql = query.toString(params);
        QueryExecutorImpl.LOGGER.log(Level.FINEST, " FE=> SimpleQuery(query=\"{0}\")", nativeSql);
        final Encoding encoding = this.pgStream.getEncoding();
        final byte[] encoded = encoding.encode(nativeSql);
        this.pgStream.sendChar(81);
        this.pgStream.sendInteger4(encoded.length + 4 + 1);
        this.pgStream.send(encoded);
        this.pgStream.sendChar(0);
        this.pgStream.flush();
        this.pendingExecuteQueue.add(new ExecuteRequest(query, null, true));
        this.pendingDescribePortalQueue.add(query);
    }
    
    private void registerParsedQuery(final SimpleQuery query, final String statementName) {
        if (statementName == null) {
            return;
        }
        final PhantomReference<SimpleQuery> cleanupRef = new PhantomReference<SimpleQuery>(query, this.parsedQueryCleanupQueue);
        this.parsedQueryMap.put(cleanupRef, statementName);
        query.setCleanupRef(cleanupRef);
    }
    
    private void processDeadParsedQueries() throws IOException {
        Reference<? extends SimpleQuery> deadQuery;
        while ((deadQuery = this.parsedQueryCleanupQueue.poll()) != null) {
            final String statementName = Nullness.castNonNull(this.parsedQueryMap.remove(deadQuery));
            this.sendCloseStatement(statementName);
            deadQuery.clear();
        }
    }
    
    private void registerOpenPortal(final Portal portal) {
        if (portal == QueryExecutorImpl.UNNAMED_PORTAL) {
            return;
        }
        final String portalName = portal.getPortalName();
        final PhantomReference<Portal> cleanupRef = new PhantomReference<Portal>(portal, this.openPortalCleanupQueue);
        this.openPortalMap.put(cleanupRef, portalName);
        portal.setCleanupRef(cleanupRef);
    }
    
    private void processDeadPortals() throws IOException {
        Reference<? extends Portal> deadPortal;
        while ((deadPortal = this.openPortalCleanupQueue.poll()) != null) {
            final String portalName = Nullness.castNonNull(this.openPortalMap.remove(deadPortal));
            this.sendClosePortal(portalName);
            deadPortal.clear();
        }
    }
    
    protected void processResults(final ResultHandler handler, final int flags) throws IOException {
        final boolean noResults = (flags & 0x4) != 0x0;
        final boolean bothRowsAndStatus = (flags & 0x40) != 0x0;
        List<Tuple> tuples = null;
        boolean endQuery = false;
        boolean doneAfterRowDescNoData = false;
        while (!endQuery) {
            final int c = this.pgStream.receiveChar();
            switch (c) {
                case 65: {
                    this.receiveAsyncNotify();
                    continue;
                }
                case 49: {
                    this.pgStream.receiveInteger4();
                    final SimpleQuery parsedQuery = this.pendingParseQueue.removeFirst();
                    final String parsedStatementName = parsedQuery.getStatementName();
                    QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE ParseComplete [{0}]", parsedStatementName);
                    continue;
                }
                case 116: {
                    this.pgStream.receiveInteger4();
                    QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE ParameterDescription");
                    final DescribeRequest describeData = this.pendingDescribeStatementQueue.getFirst();
                    final SimpleQuery query = describeData.query;
                    final SimpleParameterList params = describeData.parameterList;
                    final boolean describeOnly = describeData.describeOnly;
                    final String origStatementName = describeData.statementName;
                    for (int numParams = this.pgStream.receiveInteger2(), i = 1; i <= numParams; ++i) {
                        final int typeOid = this.pgStream.receiveInteger4();
                        params.setResolvedType(i, typeOid);
                    }
                    if ((origStatementName == null && query.getStatementName() == null) || (origStatementName != null && origStatementName.equals(query.getStatementName()))) {
                        query.setPrepareTypes(params.getTypeOIDs());
                    }
                    if (describeOnly) {
                        doneAfterRowDescNoData = true;
                        continue;
                    }
                    this.pendingDescribeStatementQueue.removeFirst();
                    continue;
                }
                case 50: {
                    this.pgStream.receiveInteger4();
                    final Portal boundPortal = this.pendingBindQueue.removeFirst();
                    QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE BindComplete [{0}]", boundPortal);
                    this.registerOpenPortal(boundPortal);
                    continue;
                }
                case 51: {
                    this.pgStream.receiveInteger4();
                    QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE CloseComplete");
                    continue;
                }
                case 110: {
                    this.pgStream.receiveInteger4();
                    QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE NoData");
                    this.pendingDescribePortalQueue.removeFirst();
                    if (doneAfterRowDescNoData) {
                        final DescribeRequest describeData2 = this.pendingDescribeStatementQueue.removeFirst();
                        final SimpleQuery currentQuery = describeData2.query;
                        final Field[] fields = currentQuery.getFields();
                        if (fields == null) {
                            continue;
                        }
                        tuples = new ArrayList<Tuple>();
                        handler.handleResultRows(currentQuery, fields, tuples, null);
                        tuples = null;
                        continue;
                    }
                    continue;
                }
                case 115: {
                    this.pgStream.receiveInteger4();
                    QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE PortalSuspended");
                    final ExecuteRequest executeData = this.pendingExecuteQueue.removeFirst();
                    final SimpleQuery currentQuery = executeData.query;
                    final Portal currentPortal = executeData.portal;
                    final Field[] fields2 = currentQuery.getFields();
                    if (fields2 != null && tuples == null) {
                        tuples = (noResults ? Collections.emptyList() : new ArrayList<Tuple>());
                    }
                    if (fields2 != null && tuples != null) {
                        handler.handleResultRows(currentQuery, fields2, tuples, currentPortal);
                    }
                    tuples = null;
                    continue;
                }
                case 67: {
                    final String status = this.receiveCommandStatus();
                    if (this.isFlushCacheOnDeallocate() && (status.startsWith("DEALLOCATE ALL") || status.startsWith("DISCARD ALL"))) {
                        ++this.deallocateEpoch;
                    }
                    doneAfterRowDescNoData = false;
                    final ExecuteRequest executeData2 = Nullness.castNonNull(this.pendingExecuteQueue.peekFirst());
                    final SimpleQuery currentQuery2 = executeData2.query;
                    final Portal currentPortal2 = executeData2.portal;
                    if (status.startsWith("SET")) {
                        final String nativeSql = currentQuery2.getNativeQuery().nativeSql;
                        if (nativeSql.lastIndexOf("search_path", 1024) != -1 && !nativeSql.equals(this.lastSetSearchPathQuery)) {
                            this.lastSetSearchPathQuery = nativeSql;
                            ++this.deallocateEpoch;
                        }
                    }
                    if (!executeData2.asSimple) {
                        this.pendingExecuteQueue.removeFirst();
                    }
                    if (currentQuery2 == this.autoSaveQuery) {
                        continue;
                    }
                    if (currentQuery2 == this.releaseAutoSave) {
                        continue;
                    }
                    final Field[] fields3 = currentQuery2.getFields();
                    if (fields3 != null && tuples == null) {
                        tuples = (noResults ? Collections.emptyList() : new ArrayList<Tuple>());
                    }
                    if (fields3 == null && tuples != null) {
                        throw new IllegalStateException("Received resultset tuples, but no field structure for them");
                    }
                    if (fields3 != null && tuples != null) {
                        handler.handleResultRows(currentQuery2, fields3, tuples, null);
                        tuples = null;
                        if (bothRowsAndStatus) {
                            this.interpretCommandStatus(status, handler);
                        }
                    }
                    else {
                        this.interpretCommandStatus(status, handler);
                    }
                    if (executeData2.asSimple) {
                        currentQuery2.setFields(null);
                    }
                    if (currentPortal2 != null) {
                        currentPortal2.close();
                        continue;
                    }
                    continue;
                }
                case 68: {
                    Tuple tuple = null;
                    try {
                        tuple = this.pgStream.receiveTupleV3();
                    }
                    catch (final OutOfMemoryError oome) {
                        if (!noResults) {
                            handler.handleError(new PSQLException(GT.tr("Ran out of memory retrieving query results.", new Object[0]), PSQLState.OUT_OF_MEMORY, oome));
                        }
                    }
                    catch (final SQLException e) {
                        handler.handleError(e);
                    }
                    if (!noResults) {
                        if (tuples == null) {
                            tuples = new ArrayList<Tuple>();
                        }
                        if (tuple != null) {
                            tuples.add(tuple);
                        }
                    }
                    if (QueryExecutorImpl.LOGGER.isLoggable(Level.FINEST)) {
                        int length;
                        if (tuple == null) {
                            length = -1;
                        }
                        else {
                            length = tuple.length();
                        }
                        QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE DataRow(len={0})", length);
                        continue;
                    }
                    continue;
                }
                case 69: {
                    final SQLException error = this.receiveErrorResponse();
                    handler.handleError(error);
                    if (!this.willHealViaReparse(error)) {
                        continue;
                    }
                    ++this.deallocateEpoch;
                    if (QueryExecutorImpl.LOGGER.isLoggable(Level.FINEST)) {
                        QueryExecutorImpl.LOGGER.log(Level.FINEST, " FE: received {0}, will invalidate statements. deallocateEpoch is now {1}", new Object[] { error.getSQLState(), this.deallocateEpoch });
                        continue;
                    }
                    continue;
                }
                case 73: {
                    this.pgStream.receiveInteger4();
                    QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE EmptyQuery");
                    final ExecuteRequest executeData3 = this.pendingExecuteQueue.removeFirst();
                    final Portal currentPortal2 = executeData3.portal;
                    handler.handleCommandStatus("EMPTY", 0L, 0L);
                    if (currentPortal2 != null) {
                        currentPortal2.close();
                        continue;
                    }
                    continue;
                }
                case 78: {
                    QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE Notice");
                    final SQLWarning warning = this.receiveNoticeResponse();
                    handler.handleWarning(warning);
                    continue;
                }
                case 83: {
                    try {
                        this.receiveParameterStatus();
                    }
                    catch (final SQLException e2) {
                        handler.handleError(e2);
                        endQuery = true;
                    }
                    continue;
                }
                case 84: {
                    final Field[] fields2 = this.receiveFields();
                    tuples = new ArrayList<Tuple>();
                    final SimpleQuery query2 = Nullness.castNonNull(this.pendingDescribePortalQueue.peekFirst());
                    if (!this.pendingExecuteQueue.isEmpty() && !Nullness.castNonNull(this.pendingExecuteQueue.peekFirst()).asSimple) {
                        this.pendingDescribePortalQueue.removeFirst();
                    }
                    query2.setFields(fields2);
                    if (doneAfterRowDescNoData) {
                        final DescribeRequest describeData3 = this.pendingDescribeStatementQueue.removeFirst();
                        final SimpleQuery currentQuery3 = describeData3.query;
                        currentQuery3.setFields(fields2);
                        handler.handleResultRows(currentQuery3, fields2, tuples, null);
                        tuples = null;
                        continue;
                    }
                    continue;
                }
                case 90: {
                    this.receiveRFQ();
                    if (!this.pendingExecuteQueue.isEmpty() && Nullness.castNonNull(this.pendingExecuteQueue.peekFirst()).asSimple) {
                        tuples = null;
                        this.pgStream.clearResultBufferCount();
                        final ExecuteRequest executeRequest = this.pendingExecuteQueue.removeFirst();
                        executeRequest.query.setFields(null);
                        this.pendingDescribePortalQueue.removeFirst();
                        if (!this.pendingExecuteQueue.isEmpty()) {
                            if (this.getTransactionState() == TransactionState.IDLE) {
                                handler.secureProgress();
                                continue;
                            }
                            continue;
                        }
                    }
                    endQuery = true;
                    while (!this.pendingParseQueue.isEmpty()) {
                        final SimpleQuery failedQuery = this.pendingParseQueue.removeFirst();
                        failedQuery.unprepare();
                    }
                    this.pendingParseQueue.clear();
                    while (!this.pendingDescribeStatementQueue.isEmpty()) {
                        final DescribeRequest request = this.pendingDescribeStatementQueue.removeFirst();
                        QueryExecutorImpl.LOGGER.log(Level.FINEST, " FE marking setStatementDescribed(false) for query {0}", request.query);
                        request.query.setStatementDescribed(false);
                    }
                    while (!this.pendingDescribePortalQueue.isEmpty()) {
                        final SimpleQuery describePortalQuery = this.pendingDescribePortalQueue.removeFirst();
                        QueryExecutorImpl.LOGGER.log(Level.FINEST, " FE marking setPortalDescribed(false) for query {0}", describePortalQuery);
                        describePortalQuery.setPortalDescribed(false);
                    }
                    this.pendingBindQueue.clear();
                    this.pendingExecuteQueue.clear();
                    continue;
                }
                case 71: {
                    QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE CopyInResponse");
                    QueryExecutorImpl.LOGGER.log(Level.FINEST, " FE=> CopyFail");
                    final byte[] buf = Utils.encodeUTF8("COPY commands are only supported using the CopyManager API.");
                    this.pgStream.sendChar(102);
                    this.pgStream.sendInteger4(buf.length + 4 + 1);
                    this.pgStream.send(buf);
                    this.pgStream.sendChar(0);
                    this.pgStream.flush();
                    this.sendSync();
                    this.skipMessage();
                    continue;
                }
                case 72: {
                    QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE CopyOutResponse");
                    this.skipMessage();
                    handler.handleError(new PSQLException(GT.tr("COPY commands are only supported using the CopyManager API.", new Object[0]), PSQLState.NOT_IMPLEMENTED));
                    continue;
                }
                case 99: {
                    this.skipMessage();
                    QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE CopyDone");
                    continue;
                }
                case 100: {
                    this.skipMessage();
                    QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE CopyData");
                    continue;
                }
                default: {
                    throw new IOException("Unexpected packet type: " + c);
                }
            }
        }
    }
    
    private void skipMessage() throws IOException {
        final int len = this.pgStream.receiveInteger4();
        assert len >= 4 : "Length from skip message must be at least 4 ";
        this.pgStream.skip(len - 4);
    }
    
    @Override
    public synchronized void fetch(final ResultCursor cursor, ResultHandler handler, final int fetchSize) throws SQLException {
        this.waitOnLock();
        final Portal portal = (Portal)cursor;
        final ResultHandler delegateHandler = handler;
        final SimpleQuery query = Nullness.castNonNull(portal.getQuery());
        handler = new ResultHandlerDelegate(delegateHandler) {
            @Override
            public void handleCommandStatus(final String status, final long updateCount, final long insertOID) {
                this.handleResultRows(query, QueryExecutorImpl.NO_FIELDS, new ArrayList<Tuple>(), null);
            }
        };
        try {
            this.processDeadParsedQueries();
            this.processDeadPortals();
            this.sendExecute(query, portal, fetchSize);
            this.sendSync();
            this.processResults(handler, 0);
            this.estimatedReceiveBufferBytes = 0;
        }
        catch (final IOException e) {
            this.abort();
            handler.handleError(new PSQLException(GT.tr("An I/O error occurred while sending to the backend.", new Object[0]), PSQLState.CONNECTION_FAILURE, e));
        }
        handler.handleCompletion();
    }
    
    private Field[] receiveFields() throws IOException {
        this.pgStream.receiveInteger4();
        final int size = this.pgStream.receiveInteger2();
        final Field[] fields = new Field[size];
        if (QueryExecutorImpl.LOGGER.isLoggable(Level.FINEST)) {
            QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE RowDescription({0})", size);
        }
        for (int i = 0; i < fields.length; ++i) {
            final String columnLabel = this.pgStream.receiveString();
            final int tableOid = this.pgStream.receiveInteger4();
            final short positionInTable = (short)this.pgStream.receiveInteger2();
            final int typeOid = this.pgStream.receiveInteger4();
            final int typeLength = this.pgStream.receiveInteger2();
            final int typeModifier = this.pgStream.receiveInteger4();
            final int formatType = this.pgStream.receiveInteger2();
            (fields[i] = new Field(columnLabel, typeOid, typeLength, typeModifier, tableOid, positionInTable)).setFormat(formatType);
            QueryExecutorImpl.LOGGER.log(Level.FINEST, "        {0}", fields[i]);
        }
        return fields;
    }
    
    private void receiveAsyncNotify() throws IOException {
        final int len = this.pgStream.receiveInteger4();
        assert len > 4 : "Length for AsyncNotify must be at least 4";
        final int pid = this.pgStream.receiveInteger4();
        final String msg = this.pgStream.receiveString();
        final String param = this.pgStream.receiveString();
        this.addNotification(new Notification(msg, pid, param));
        if (QueryExecutorImpl.LOGGER.isLoggable(Level.FINEST)) {
            QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE AsyncNotify({0},{1},{2})", new Object[] { pid, msg, param });
        }
    }
    
    private SQLException receiveErrorResponse() throws IOException {
        final int elen = this.pgStream.receiveInteger4();
        assert elen > 4 : "Error response length must be greater than 4";
        final EncodingPredictor.DecodeResult totalMessage = this.pgStream.receiveErrorString(elen - 4);
        final ServerErrorMessage errorMsg = new ServerErrorMessage(totalMessage);
        if (QueryExecutorImpl.LOGGER.isLoggable(Level.FINEST)) {
            QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE ErrorMessage({0})", errorMsg.toString());
        }
        final PSQLException error = new PSQLException(errorMsg, this.logServerErrorDetail);
        if (this.transactionFailCause == null) {
            this.transactionFailCause = error;
        }
        else {
            error.initCause(this.transactionFailCause);
        }
        return error;
    }
    
    private SQLWarning receiveNoticeResponse() throws IOException {
        final int nlen = this.pgStream.receiveInteger4();
        assert nlen > 4 : "Notice Response length must be greater than 4";
        final ServerErrorMessage warnMsg = new ServerErrorMessage(this.pgStream.receiveString(nlen - 4));
        if (QueryExecutorImpl.LOGGER.isLoggable(Level.FINEST)) {
            QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE NoticeResponse({0})", warnMsg.toString());
        }
        return new PSQLWarning(warnMsg);
    }
    
    private String receiveCommandStatus() throws IOException {
        final int len = this.pgStream.receiveInteger4();
        final String status = this.pgStream.receiveString(len - 5);
        this.pgStream.receiveChar();
        QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE CommandStatus({0})", status);
        return status;
    }
    
    private void interpretCommandStatus(final String status, final ResultHandler handler) {
        try {
            this.commandCompleteParser.parse(status);
        }
        catch (final SQLException e) {
            handler.handleError(e);
            return;
        }
        final long oid = this.commandCompleteParser.getOid();
        final long count = this.commandCompleteParser.getRows();
        handler.handleCommandStatus(status, count, oid);
    }
    
    private void receiveRFQ() throws IOException {
        if (this.pgStream.receiveInteger4() != 5) {
            throw new IOException("unexpected length of ReadyForQuery message");
        }
        final char tStatus = (char)this.pgStream.receiveChar();
        if (QueryExecutorImpl.LOGGER.isLoggable(Level.FINEST)) {
            QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE ReadyForQuery({0})", tStatus);
        }
        switch (tStatus) {
            case 'I': {
                this.transactionFailCause = null;
                this.setTransactionState(TransactionState.IDLE);
                break;
            }
            case 'T': {
                this.transactionFailCause = null;
                this.setTransactionState(TransactionState.OPEN);
                break;
            }
            case 'E': {
                this.setTransactionState(TransactionState.FAILED);
                break;
            }
            default: {
                throw new IOException("unexpected transaction state in ReadyForQuery message: " + (int)tStatus);
            }
        }
    }
    
    @Override
    protected void sendCloseMessage() throws IOException {
        this.pgStream.sendChar(88);
        this.pgStream.sendInteger4(4);
    }
    
    public void readStartupMessages() throws IOException, SQLException {
        for (int i = 0; i < 1000; ++i) {
            final int beresp = this.pgStream.receiveChar();
            switch (beresp) {
                case 90: {
                    this.receiveRFQ();
                    return;
                }
                case 75: {
                    final int msgLen = this.pgStream.receiveInteger4();
                    if (msgLen != 12) {
                        throw new PSQLException(GT.tr("Protocol error.  Session setup failed.", new Object[0]), PSQLState.PROTOCOL_VIOLATION);
                    }
                    final int pid = this.pgStream.receiveInteger4();
                    final int ckey = this.pgStream.receiveInteger4();
                    if (QueryExecutorImpl.LOGGER.isLoggable(Level.FINEST)) {
                        QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE BackendKeyData(pid={0},ckey={1})", new Object[] { pid, ckey });
                    }
                    this.setBackendKeyData(pid, ckey);
                    break;
                }
                case 69: {
                    throw this.receiveErrorResponse();
                }
                case 78: {
                    this.addWarning(this.receiveNoticeResponse());
                    break;
                }
                case 83: {
                    this.receiveParameterStatus();
                    break;
                }
                default: {
                    if (QueryExecutorImpl.LOGGER.isLoggable(Level.FINEST)) {
                        QueryExecutorImpl.LOGGER.log(Level.FINEST, "  invalid message type={0}", (char)beresp);
                    }
                    throw new PSQLException(GT.tr("Protocol error.  Session setup failed.", new Object[0]), PSQLState.PROTOCOL_VIOLATION);
                }
            }
        }
        throw new PSQLException(GT.tr("Protocol error.  Session setup failed.", new Object[0]), PSQLState.PROTOCOL_VIOLATION);
    }
    
    public void receiveParameterStatus() throws IOException, SQLException {
        this.pgStream.receiveInteger4();
        final String name = this.pgStream.receiveString();
        final String value = this.pgStream.receiveString();
        if (QueryExecutorImpl.LOGGER.isLoggable(Level.FINEST)) {
            QueryExecutorImpl.LOGGER.log(Level.FINEST, " <=BE ParameterStatus({0} = {1})", new Object[] { name, value });
        }
        if (name != null && !name.equals("")) {
            this.onParameterStatus(name, value);
        }
        if (name.equals("client_encoding")) {
            if (this.allowEncodingChanges) {
                if (!value.equalsIgnoreCase("UTF8") && !value.equalsIgnoreCase("UTF-8")) {
                    QueryExecutorImpl.LOGGER.log(Level.FINE, "pgjdbc expects client_encoding to be UTF8 for proper operation. Actual encoding is {0}", value);
                }
                this.pgStream.setEncoding(Encoding.getDatabaseEncoding(value));
            }
            else if (!value.equalsIgnoreCase("UTF8") && !value.equalsIgnoreCase("UTF-8")) {
                this.close();
                throw new PSQLException(GT.tr("The server''s client_encoding parameter was changed to {0}. The JDBC driver requires client_encoding to be UTF8 for correct operation.", value), PSQLState.CONNECTION_FAILURE);
            }
        }
        if (name.equals("DateStyle") && !value.startsWith("ISO") && !value.toUpperCase().startsWith("ISO")) {
            this.close();
            throw new PSQLException(GT.tr("The server''s DateStyle parameter was changed to {0}. The JDBC driver requires DateStyle to begin with ISO for correct operation.", value), PSQLState.CONNECTION_FAILURE);
        }
        if (name.equals("standard_conforming_strings")) {
            if (value.equals("on")) {
                this.setStandardConformingStrings(true);
            }
            else {
                if (!value.equals("off")) {
                    this.close();
                    throw new PSQLException(GT.tr("The server''s standard_conforming_strings parameter was reported as {0}. The JDBC driver expected on or off.", value), PSQLState.CONNECTION_FAILURE);
                }
                this.setStandardConformingStrings(false);
            }
            return;
        }
        if ("TimeZone".equals(name)) {
            this.setTimeZone(TimestampUtils.parseBackendTimeZone(value));
        }
        else if ("application_name".equals(name)) {
            this.setApplicationName(value);
        }
        else if ("server_version_num".equals(name)) {
            this.setServerVersionNum(Integer.parseInt(value));
        }
        else if ("server_version".equals(name)) {
            this.setServerVersion(value);
        }
        else if ("integer_datetimes".equals(name)) {
            if ("on".equals(value)) {
                this.setIntegerDateTimes(true);
            }
            else {
                if (!"off".equals(value)) {
                    throw new PSQLException(GT.tr("Protocol error.  Session setup failed.", new Object[0]), PSQLState.PROTOCOL_VIOLATION);
                }
                this.setIntegerDateTimes(false);
            }
        }
    }
    
    public void setTimeZone(final TimeZone timeZone) {
        this.timeZone = timeZone;
    }
    
    @Override
    public TimeZone getTimeZone() {
        return this.timeZone;
    }
    
    public void setApplicationName(final String applicationName) {
        this.applicationName = applicationName;
    }
    
    @Override
    public String getApplicationName() {
        if (this.applicationName == null) {
            return "";
        }
        return this.applicationName;
    }
    
    @Override
    public ReplicationProtocol getReplicationProtocol() {
        return this.replicationProtocol;
    }
    
    @Override
    public boolean useBinaryForReceive(final int oid) {
        return this.useBinaryReceiveForOids.contains(oid);
    }
    
    @Override
    public void setBinaryReceiveOids(final Set<Integer> oids) {
        this.useBinaryReceiveForOids.clear();
        this.useBinaryReceiveForOids.addAll(oids);
    }
    
    @Override
    public boolean useBinaryForSend(final int oid) {
        return this.useBinarySendForOids.contains(oid);
    }
    
    @Override
    public void setBinarySendOids(final Set<Integer> oids) {
        this.useBinarySendForOids.clear();
        this.useBinarySendForOids.addAll(oids);
    }
    
    private void setIntegerDateTimes(final boolean state) {
        this.integerDateTimes = state;
    }
    
    @Override
    public boolean getIntegerDateTimes() {
        return this.integerDateTimes;
    }
    
    static {
        LOGGER = Logger.getLogger(QueryExecutorImpl.class.getName());
        NO_FIELDS = new Field[0];
        UNNAMED_PORTAL = new Portal(null, "unnamed");
    }
}
