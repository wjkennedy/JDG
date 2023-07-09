// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import java.util.Collections;
import java.util.Map;
import org.postgresql.util.ServerErrorMessage;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import java.util.logging.Level;
import org.postgresql.util.HostSpec;
import java.io.IOException;
import java.sql.SQLException;
import org.postgresql.PGProperty;
import java.util.Comparator;
import java.util.Properties;
import java.util.TreeMap;
import org.postgresql.util.LruCache;
import org.postgresql.PGNotification;
import java.util.ArrayList;
import java.sql.SQLWarning;
import org.postgresql.jdbc.AutoSave;
import org.postgresql.jdbc.PreferQueryMode;
import org.postgresql.jdbc.EscapeSyntaxCallMode;
import java.util.logging.Logger;

public abstract class QueryExecutorBase implements QueryExecutor
{
    private static final Logger LOGGER;
    protected final PGStream pgStream;
    private final String user;
    private final String database;
    private final int cancelSignalTimeout;
    private int cancelPid;
    private int cancelKey;
    private boolean closed;
    private String serverVersion;
    private int serverVersionNum;
    private TransactionState transactionState;
    private final boolean reWriteBatchedInserts;
    private final boolean columnSanitiserDisabled;
    private final EscapeSyntaxCallMode escapeSyntaxCallMode;
    private final PreferQueryMode preferQueryMode;
    private AutoSave autoSave;
    private boolean flushCacheOnDeallocate;
    protected final boolean logServerErrorDetail;
    private boolean standardConformingStrings;
    private SQLWarning warnings;
    private final ArrayList<PGNotification> notifications;
    private final LruCache<Object, CachedQuery> statementCache;
    private final CachedQueryCreateAction cachedQueryCreateAction;
    private final TreeMap<String, String> parameterStatuses;
    
    protected QueryExecutorBase(final PGStream pgStream, final String user, final String database, final int cancelSignalTimeout, final Properties info) throws SQLException {
        this.closed = false;
        this.serverVersionNum = 0;
        this.transactionState = TransactionState.IDLE;
        this.flushCacheOnDeallocate = true;
        this.standardConformingStrings = false;
        this.notifications = new ArrayList<PGNotification>();
        this.parameterStatuses = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        this.pgStream = pgStream;
        this.user = user;
        this.database = database;
        this.cancelSignalTimeout = cancelSignalTimeout;
        this.reWriteBatchedInserts = PGProperty.REWRITE_BATCHED_INSERTS.getBoolean(info);
        this.columnSanitiserDisabled = PGProperty.DISABLE_COLUMN_SANITISER.getBoolean(info);
        final String callMode = PGProperty.ESCAPE_SYNTAX_CALL_MODE.get(info);
        this.escapeSyntaxCallMode = EscapeSyntaxCallMode.of(callMode);
        final String preferMode = PGProperty.PREFER_QUERY_MODE.get(info);
        this.preferQueryMode = PreferQueryMode.of(preferMode);
        this.autoSave = AutoSave.of(PGProperty.AUTOSAVE.get(info));
        this.logServerErrorDetail = PGProperty.LOG_SERVER_ERROR_DETAIL.getBoolean(info);
        this.cachedQueryCreateAction = new CachedQueryCreateAction(this);
        this.statementCache = new LruCache<Object, CachedQuery>(Math.max(0, PGProperty.PREPARED_STATEMENT_CACHE_QUERIES.getInt(info)), Math.max(0, PGProperty.PREPARED_STATEMENT_CACHE_SIZE_MIB.getInt(info) * 1024 * 1024), false, this.cachedQueryCreateAction, new LruCache.EvictAction<CachedQuery>() {
            @Override
            public void evict(final CachedQuery cachedQuery) throws SQLException {
                cachedQuery.query.close();
            }
        });
    }
    
    protected abstract void sendCloseMessage() throws IOException;
    
    @Override
    public void setNetworkTimeout(final int milliseconds) throws IOException {
        this.pgStream.setNetworkTimeout(milliseconds);
    }
    
    @Override
    public int getNetworkTimeout() throws IOException {
        return this.pgStream.getNetworkTimeout();
    }
    
    @Override
    public HostSpec getHostSpec() {
        return this.pgStream.getHostSpec();
    }
    
    @Override
    public String getUser() {
        return this.user;
    }
    
    @Override
    public String getDatabase() {
        return this.database;
    }
    
    public void setBackendKeyData(final int cancelPid, final int cancelKey) {
        this.cancelPid = cancelPid;
        this.cancelKey = cancelKey;
    }
    
    @Override
    public int getBackendPID() {
        return this.cancelPid;
    }
    
    @Override
    public void abort() {
        try {
            this.pgStream.getSocket().close();
        }
        catch (final IOException ex) {}
        this.closed = true;
    }
    
    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        try {
            QueryExecutorBase.LOGGER.log(Level.FINEST, " FE=> Terminate");
            this.sendCloseMessage();
            this.pgStream.flush();
            this.pgStream.close();
        }
        catch (final IOException ioe) {
            QueryExecutorBase.LOGGER.log(Level.FINEST, "Discarding IOException on close:", ioe);
        }
        this.closed = true;
    }
    
    @Override
    public boolean isClosed() {
        return this.closed;
    }
    
    @Override
    public void sendQueryCancel() throws SQLException {
        if (this.cancelPid <= 0) {
            return;
        }
        PGStream cancelStream = null;
        try {
            if (QueryExecutorBase.LOGGER.isLoggable(Level.FINEST)) {
                QueryExecutorBase.LOGGER.log(Level.FINEST, " FE=> CancelRequest(pid={0},ckey={1})", new Object[] { this.cancelPid, this.cancelKey });
            }
            cancelStream = new PGStream(this.pgStream.getSocketFactory(), this.pgStream.getHostSpec(), this.cancelSignalTimeout);
            if (this.cancelSignalTimeout > 0) {
                cancelStream.setNetworkTimeout(this.cancelSignalTimeout);
            }
            cancelStream.sendInteger4(16);
            cancelStream.sendInteger2(1234);
            cancelStream.sendInteger2(5678);
            cancelStream.sendInteger4(this.cancelPid);
            cancelStream.sendInteger4(this.cancelKey);
            cancelStream.flush();
            cancelStream.receiveEOF();
        }
        catch (final IOException e) {
            QueryExecutorBase.LOGGER.log(Level.FINEST, "Ignoring exception on cancel request:", e);
        }
        finally {
            if (cancelStream != null) {
                try {
                    cancelStream.close();
                }
                catch (final IOException ex) {}
            }
        }
    }
    
    public synchronized void addWarning(final SQLWarning newWarning) {
        if (this.warnings == null) {
            this.warnings = newWarning;
        }
        else {
            this.warnings.setNextWarning(newWarning);
        }
    }
    
    public synchronized void addNotification(final PGNotification notification) {
        this.notifications.add(notification);
    }
    
    @Override
    public synchronized PGNotification[] getNotifications() throws SQLException {
        final PGNotification[] array = this.notifications.toArray(new PGNotification[0]);
        this.notifications.clear();
        return array;
    }
    
    @Override
    public synchronized SQLWarning getWarnings() {
        final SQLWarning chain = this.warnings;
        this.warnings = null;
        return chain;
    }
    
    @Override
    public String getServerVersion() {
        final String serverVersion = this.serverVersion;
        if (serverVersion == null) {
            throw new IllegalStateException("serverVersion must not be null");
        }
        return serverVersion;
    }
    
    @Override
    public int getServerVersionNum() {
        if (this.serverVersionNum != 0) {
            return this.serverVersionNum;
        }
        return this.serverVersionNum = Utils.parseServerVersionStr(this.getServerVersion());
    }
    
    public void setServerVersion(final String serverVersion) {
        this.serverVersion = serverVersion;
    }
    
    public void setServerVersionNum(final int serverVersionNum) {
        this.serverVersionNum = serverVersionNum;
    }
    
    public synchronized void setTransactionState(final TransactionState state) {
        this.transactionState = state;
    }
    
    public synchronized void setStandardConformingStrings(final boolean value) {
        this.standardConformingStrings = value;
    }
    
    @Override
    public synchronized boolean getStandardConformingStrings() {
        return this.standardConformingStrings;
    }
    
    @Override
    public synchronized TransactionState getTransactionState() {
        return this.transactionState;
    }
    
    public void setEncoding(final Encoding encoding) throws IOException {
        this.pgStream.setEncoding(encoding);
    }
    
    @Override
    public Encoding getEncoding() {
        return this.pgStream.getEncoding();
    }
    
    @Override
    public boolean isReWriteBatchedInsertsEnabled() {
        return this.reWriteBatchedInserts;
    }
    
    @Override
    public final CachedQuery borrowQuery(final String sql) throws SQLException {
        return this.statementCache.borrow(sql);
    }
    
    @Override
    public final CachedQuery borrowCallableQuery(final String sql) throws SQLException {
        return this.statementCache.borrow(new CallableQueryKey(sql));
    }
    
    @Override
    public final CachedQuery borrowReturningQuery(final String sql, final String[] columnNames) throws SQLException {
        return this.statementCache.borrow(new QueryWithReturningColumnsKey(sql, true, true, columnNames));
    }
    
    @Override
    public CachedQuery borrowQueryByKey(final Object key) throws SQLException {
        return this.statementCache.borrow(key);
    }
    
    @Override
    public void releaseQuery(final CachedQuery cachedQuery) {
        this.statementCache.put(cachedQuery.key, cachedQuery);
    }
    
    @Override
    public final Object createQueryKey(final String sql, final boolean escapeProcessing, final boolean isParameterized, final String... columnNames) {
        Object key;
        if (columnNames == null || columnNames.length != 0) {
            key = new QueryWithReturningColumnsKey(sql, isParameterized, escapeProcessing, columnNames);
        }
        else if (isParameterized) {
            key = sql;
        }
        else {
            key = new BaseQueryKey(sql, false, escapeProcessing);
        }
        return key;
    }
    
    @Override
    public CachedQuery createQueryByKey(final Object key) throws SQLException {
        return this.cachedQueryCreateAction.create(key);
    }
    
    @Override
    public final CachedQuery createQuery(final String sql, final boolean escapeProcessing, final boolean isParameterized, final String... columnNames) throws SQLException {
        final Object key = this.createQueryKey(sql, escapeProcessing, isParameterized, columnNames);
        return this.createQueryByKey(key);
    }
    
    @Override
    public boolean isColumnSanitiserDisabled() {
        return this.columnSanitiserDisabled;
    }
    
    @Override
    public EscapeSyntaxCallMode getEscapeSyntaxCallMode() {
        return this.escapeSyntaxCallMode;
    }
    
    @Override
    public PreferQueryMode getPreferQueryMode() {
        return this.preferQueryMode;
    }
    
    @Override
    public AutoSave getAutoSave() {
        return this.autoSave;
    }
    
    @Override
    public void setAutoSave(final AutoSave autoSave) {
        this.autoSave = autoSave;
    }
    
    protected boolean willHealViaReparse(final SQLException e) {
        if (e == null || e.getSQLState() == null) {
            return false;
        }
        if (PSQLState.INVALID_SQL_STATEMENT_NAME.getState().equals(e.getSQLState())) {
            return true;
        }
        if (!PSQLState.NOT_IMPLEMENTED.getState().equals(e.getSQLState())) {
            return false;
        }
        if (!(e instanceof PSQLException)) {
            return false;
        }
        final PSQLException pe = (PSQLException)e;
        final ServerErrorMessage serverErrorMessage = pe.getServerErrorMessage();
        if (serverErrorMessage == null) {
            return false;
        }
        final String routine = serverErrorMessage.getRoutine();
        return "RevalidateCachedQuery".equals(routine) || "RevalidateCachedPlan".equals(routine);
    }
    
    @Override
    public boolean willHealOnRetry(final SQLException e) {
        return (this.autoSave != AutoSave.NEVER || this.getTransactionState() != TransactionState.FAILED) && this.willHealViaReparse(e);
    }
    
    public boolean isFlushCacheOnDeallocate() {
        return this.flushCacheOnDeallocate;
    }
    
    @Override
    public void setFlushCacheOnDeallocate(final boolean flushCacheOnDeallocate) {
        this.flushCacheOnDeallocate = flushCacheOnDeallocate;
    }
    
    protected boolean hasNotifications() {
        return this.notifications.size() > 0;
    }
    
    @Override
    public final Map<String, String> getParameterStatuses() {
        return Collections.unmodifiableMap((Map<? extends String, ? extends String>)this.parameterStatuses);
    }
    
    @Override
    public final String getParameterStatus(final String parameterName) {
        return this.parameterStatuses.get(parameterName);
    }
    
    protected void onParameterStatus(final String parameterName, final String parameterStatus) {
        if (parameterName == null || parameterName.equals("")) {
            throw new IllegalStateException("attempt to set GUC_REPORT parameter with null or empty-string name");
        }
        this.parameterStatuses.put(parameterName, parameterStatus);
    }
    
    static {
        LOGGER = Logger.getLogger(QueryExecutorBase.class.getName());
    }
}
