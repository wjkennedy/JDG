// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

import org.postgresql.core.ResultHandlerBase;
import org.postgresql.xml.LegacyInsecurePGXmlFactoryFactory;
import org.postgresql.xml.DefaultPGXmlFactoryFactory;
import org.postgresql.core.SqlCommand;
import java.sql.Savepoint;
import java.security.Permission;
import java.sql.SQLClientInfoException;
import java.sql.ClientInfoStatus;
import java.util.concurrent.Executor;
import java.sql.Struct;
import java.sql.NClob;
import java.sql.SQLXML;
import java.sql.Clob;
import java.sql.Blob;
import java.sql.Array;
import org.postgresql.replication.PGReplicationConnectionImpl;
import org.postgresql.replication.PGReplicationConnection;
import java.util.TimerTask;
import org.postgresql.Driver;
import org.postgresql.PGNotification;
import org.postgresql.core.Utils;
import java.io.IOException;
import org.checkerframework.dataflow.qual.Pure;
import org.postgresql.core.Encoding;
import java.util.NoSuchElementException;
import org.postgresql.core.ResultHandler;
import org.postgresql.core.ParameterList;
import org.postgresql.core.TransactionState;
import java.util.Enumeration;
import org.postgresql.util.PGInterval;
import org.postgresql.util.PGmoney;
import org.postgresql.geometric.PGpolygon;
import org.postgresql.geometric.PGpoint;
import org.postgresql.geometric.PGpath;
import org.postgresql.geometric.PGlseg;
import org.postgresql.geometric.PGline;
import org.postgresql.geometric.PGcircle;
import org.postgresql.geometric.PGbox;
import org.postgresql.util.PGBinaryObject;
import org.postgresql.util.PGobject;
import org.postgresql.util.internal.Nullness;
import org.postgresql.core.BaseStatement;
import java.sql.ResultSet;
import org.postgresql.core.ReplicationProtocol;
import java.sql.CallableStatement;
import java.sql.Statement;
import java.util.Iterator;
import org.postgresql.core.Oid;
import java.util.StringTokenizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;
import org.postgresql.core.Provider;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import java.util.Collection;
import java.util.HashSet;
import org.postgresql.core.Version;
import org.postgresql.core.ServerVersion;
import org.postgresql.core.ConnectionFactory;
import org.postgresql.PGProperty;
import java.util.HashMap;
import org.postgresql.util.HostSpec;
import java.util.logging.Level;
import java.sql.SQLException;
import org.postgresql.copy.CopyManager;
import java.sql.DatabaseMetaData;
import org.postgresql.largeobject.LargeObjectManager;
import org.postgresql.fastpath.Fastpath;
import java.util.Map;
import org.postgresql.xml.PGXmlFactoryFactory;
import org.postgresql.util.LruCache;
import java.sql.PreparedStatement;
import java.util.Timer;
import java.sql.SQLWarning;
import org.postgresql.core.TypeInfo;
import org.postgresql.core.CachedQuery;
import org.postgresql.core.Query;
import org.postgresql.core.QueryExecutor;
import java.util.Properties;
import java.sql.SQLPermission;
import java.util.Set;
import java.util.logging.Logger;
import org.postgresql.core.BaseConnection;

public class PgConnection implements BaseConnection
{
    private static final Logger LOGGER;
    private static final Set<Integer> SUPPORTED_BINARY_OIDS;
    private static final SQLPermission SQL_PERMISSION_ABORT;
    private static final SQLPermission SQL_PERMISSION_NETWORK_TIMEOUT;
    private final Properties clientInfo;
    private final String creatingURL;
    private final ReadOnlyBehavior readOnlyBehavior;
    private Throwable openStackTrace;
    private final QueryExecutor queryExecutor;
    private final Query commitQuery;
    private final Query rollbackQuery;
    private final CachedQuery setSessionReadOnly;
    private final CachedQuery setSessionNotReadOnly;
    private final TypeInfo typeCache;
    private boolean disableColumnSanitiser;
    protected int prepareThreshold;
    protected int defaultFetchSize;
    protected boolean forcebinary;
    private int rsHoldability;
    private int savepointId;
    private boolean autoCommit;
    private boolean readOnly;
    private boolean hideUnprivilegedObjects;
    private final boolean logServerErrorDetail;
    private final boolean bindStringAsVarchar;
    private SQLWarning firstWarning;
    private volatile Timer cancelTimer;
    private PreparedStatement checkConnectionQuery;
    private final boolean replicationConnection;
    private final LruCache<FieldMetadata.Key, FieldMetadata> fieldMetadataCache;
    private final String xmlFactoryFactoryClass;
    private PGXmlFactoryFactory xmlFactoryFactory;
    private final TimestampUtils timestampUtils;
    protected Map<String, Class<?>> typemap;
    private Fastpath fastpath;
    private LargeObjectManager largeobject;
    protected DatabaseMetaData metadata;
    private CopyManager copyManager;
    
    final CachedQuery borrowQuery(final String sql) throws SQLException {
        return this.queryExecutor.borrowQuery(sql);
    }
    
    final CachedQuery borrowCallableQuery(final String sql) throws SQLException {
        return this.queryExecutor.borrowCallableQuery(sql);
    }
    
    private CachedQuery borrowReturningQuery(final String sql, final String[] columnNames) throws SQLException {
        return this.queryExecutor.borrowReturningQuery(sql, columnNames);
    }
    
    @Override
    public CachedQuery createQuery(final String sql, final boolean escapeProcessing, final boolean isParameterized, final String... columnNames) throws SQLException {
        return this.queryExecutor.createQuery(sql, escapeProcessing, isParameterized, columnNames);
    }
    
    void releaseQuery(final CachedQuery cachedQuery) {
        this.queryExecutor.releaseQuery(cachedQuery);
    }
    
    @Override
    public void setFlushCacheOnDeallocate(final boolean flushCacheOnDeallocate) {
        this.queryExecutor.setFlushCacheOnDeallocate(flushCacheOnDeallocate);
        PgConnection.LOGGER.log(Level.FINE, "  setFlushCacheOnDeallocate = {0}", flushCacheOnDeallocate);
    }
    
    public PgConnection(final HostSpec[] hostSpecs, final String user, final String database, final Properties info, final String url) throws SQLException {
        this.disableColumnSanitiser = false;
        this.forcebinary = false;
        this.rsHoldability = 2;
        this.savepointId = 0;
        this.autoCommit = true;
        this.readOnly = false;
        this.typemap = new HashMap<String, Class<?>>();
        PgConnection.LOGGER.log(Level.FINE, "PostgreSQL JDBC Driver 42.2.25");
        this.creatingURL = url;
        this.readOnlyBehavior = getReadOnlyBehavior(PGProperty.READ_ONLY_MODE.get(info));
        this.setDefaultFetchSize(PGProperty.DEFAULT_ROW_FETCH_SIZE.getInt(info));
        this.setPrepareThreshold(PGProperty.PREPARE_THRESHOLD.getInt(info));
        if (this.prepareThreshold == -1) {
            this.setForceBinary(true);
        }
        this.queryExecutor = ConnectionFactory.openConnection(hostSpecs, user, database, info);
        if (PgConnection.LOGGER.isLoggable(Level.WARNING) && !this.haveMinimumServerVersion(ServerVersion.v8_2)) {
            PgConnection.LOGGER.log(Level.WARNING, "Unsupported Server Version: {0}", this.queryExecutor.getServerVersion());
        }
        this.setSessionReadOnly = this.createQuery("SET SESSION CHARACTERISTICS AS TRANSACTION READ ONLY", false, true, new String[0]);
        this.setSessionNotReadOnly = this.createQuery("SET SESSION CHARACTERISTICS AS TRANSACTION READ WRITE", false, true, new String[0]);
        if (PGProperty.READ_ONLY.getBoolean(info)) {
            this.setReadOnly(true);
        }
        this.hideUnprivilegedObjects = PGProperty.HIDE_UNPRIVILEGED_OBJECTS.getBoolean(info);
        final Set<Integer> binaryOids = getBinaryOids(info);
        final Set<Integer> useBinarySendForOids = new HashSet<Integer>(binaryOids);
        final Set<Integer> useBinaryReceiveForOids = new HashSet<Integer>(binaryOids);
        useBinarySendForOids.remove(1082);
        this.queryExecutor.setBinaryReceiveOids(useBinaryReceiveForOids);
        this.queryExecutor.setBinarySendOids(useBinarySendForOids);
        if (PgConnection.LOGGER.isLoggable(Level.FINEST)) {
            PgConnection.LOGGER.log(Level.FINEST, "    types using binary send = {0}", this.oidsToString(useBinarySendForOids));
            PgConnection.LOGGER.log(Level.FINEST, "    types using binary receive = {0}", this.oidsToString(useBinaryReceiveForOids));
            PgConnection.LOGGER.log(Level.FINEST, "    integer date/time = {0}", this.queryExecutor.getIntegerDateTimes());
        }
        final String stringType = PGProperty.STRING_TYPE.get(info);
        if (stringType != null) {
            if (stringType.equalsIgnoreCase("unspecified")) {
                this.bindStringAsVarchar = false;
            }
            else {
                if (!stringType.equalsIgnoreCase("varchar")) {
                    throw new PSQLException(GT.tr("Unsupported value for stringtype parameter: {0}", stringType), PSQLState.INVALID_PARAMETER_VALUE);
                }
                this.bindStringAsVarchar = true;
            }
        }
        else {
            this.bindStringAsVarchar = true;
        }
        this.timestampUtils = new TimestampUtils(!this.queryExecutor.getIntegerDateTimes(), new QueryExecutorTimeZoneProvider(this.queryExecutor));
        this.commitQuery = this.createQuery("COMMIT", false, true, new String[0]).query;
        this.rollbackQuery = this.createQuery("ROLLBACK", false, true, new String[0]).query;
        final int unknownLength = PGProperty.UNKNOWN_LENGTH.getInt(info);
        this.typeCache = this.createTypeInfo(this, unknownLength);
        this.initObjectTypes(info);
        if (PGProperty.LOG_UNCLOSED_CONNECTIONS.getBoolean(info)) {
            this.openStackTrace = new Throwable("Connection was created at this point:");
        }
        this.logServerErrorDetail = PGProperty.LOG_SERVER_ERROR_DETAIL.getBoolean(info);
        this.disableColumnSanitiser = PGProperty.DISABLE_COLUMN_SANITISER.getBoolean(info);
        if (this.haveMinimumServerVersion(ServerVersion.v8_3)) {
            this.typeCache.addCoreType("uuid", 2950, 1111, "java.util.UUID", 2951);
            this.typeCache.addCoreType("xml", 142, 2009, "java.sql.SQLXML", 143);
        }
        this.clientInfo = new Properties();
        if (this.haveMinimumServerVersion(ServerVersion.v9_0)) {
            String appName = PGProperty.APPLICATION_NAME.get(info);
            if (appName == null) {
                appName = "";
            }
            this.clientInfo.put("ApplicationName", appName);
        }
        this.fieldMetadataCache = new LruCache<FieldMetadata.Key, FieldMetadata>(Math.max(0, PGProperty.DATABASE_METADATA_CACHE_FIELDS.getInt(info)), Math.max(0, PGProperty.DATABASE_METADATA_CACHE_FIELDS_MIB.getInt(info) * 1024 * 1024), false);
        this.replicationConnection = (PGProperty.REPLICATION.get(info) != null);
        this.xmlFactoryFactoryClass = PGProperty.XML_FACTORY_FACTORY.get(info);
    }
    
    private static ReadOnlyBehavior getReadOnlyBehavior(final String property) {
        try {
            return ReadOnlyBehavior.valueOf(property);
        }
        catch (final IllegalArgumentException e) {
            try {
                return ReadOnlyBehavior.valueOf(property.toLowerCase(Locale.US));
            }
            catch (final IllegalArgumentException e2) {
                return ReadOnlyBehavior.transaction;
            }
        }
    }
    
    private static Set<Integer> getSupportedBinaryOids() {
        return new HashSet<Integer>(Arrays.asList(17, 21, 23, 20, 700, 701, 1083, 1082, 1266, 1114, 1184, 1001, 1005, 1007, 1016, 1028, 1021, 1022, 1015, 1009, 600, 603, 2950));
    }
    
    private static Set<Integer> getBinaryOids(final Properties info) throws PSQLException {
        final boolean binaryTransfer = PGProperty.BINARY_TRANSFER.getBoolean(info);
        final Set<Integer> binaryOids = new HashSet<Integer>(32);
        if (binaryTransfer) {
            binaryOids.addAll(PgConnection.SUPPORTED_BINARY_OIDS);
        }
        String oids = PGProperty.BINARY_TRANSFER_ENABLE.get(info);
        if (oids != null) {
            binaryOids.addAll(getOidSet(oids));
        }
        oids = PGProperty.BINARY_TRANSFER_DISABLE.get(info);
        if (oids != null) {
            binaryOids.removeAll(getOidSet(oids));
        }
        return binaryOids;
    }
    
    private static Set<Integer> getOidSet(final String oidList) throws PSQLException {
        final Set<Integer> oids = new HashSet<Integer>();
        final StringTokenizer tokenizer = new StringTokenizer(oidList, ",");
        while (tokenizer.hasMoreTokens()) {
            final String oid = tokenizer.nextToken();
            oids.add(Oid.valueOf(oid));
        }
        return oids;
    }
    
    private String oidsToString(final Set<Integer> oids) {
        final StringBuilder sb = new StringBuilder();
        for (final Integer oid : oids) {
            sb.append(Oid.toString(oid));
            sb.append(',');
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        else {
            sb.append(" <none>");
        }
        return sb.toString();
    }
    
    @Override
    public TimestampUtils getTimestampUtils() {
        return this.timestampUtils;
    }
    
    @Override
    public Statement createStatement() throws SQLException {
        return this.createStatement(1003, 1007);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql) throws SQLException {
        return this.prepareStatement(sql, 1003, 1007);
    }
    
    @Override
    public CallableStatement prepareCall(final String sql) throws SQLException {
        return this.prepareCall(sql, 1003, 1007);
    }
    
    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        this.checkClosed();
        return this.typemap;
    }
    
    @Override
    public QueryExecutor getQueryExecutor() {
        return this.queryExecutor;
    }
    
    @Override
    public ReplicationProtocol getReplicationProtocol() {
        return this.queryExecutor.getReplicationProtocol();
    }
    
    public void addWarning(final SQLWarning warn) {
        if (this.firstWarning != null) {
            this.firstWarning.setNextWarning(warn);
        }
        else {
            this.firstWarning = warn;
        }
    }
    
    @Override
    public ResultSet execSQLQuery(final String s) throws SQLException {
        return this.execSQLQuery(s, 1003, 1007);
    }
    
    @Override
    public ResultSet execSQLQuery(final String s, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        BaseStatement stat;
        boolean hasResultSet;
        for (stat = (BaseStatement)this.createStatement(resultSetType, resultSetConcurrency), hasResultSet = stat.executeWithFlags(s, 16); !hasResultSet && stat.getUpdateCount() != -1; hasResultSet = stat.getMoreResults()) {}
        if (!hasResultSet) {
            throw new PSQLException(GT.tr("No results were returned by the query.", new Object[0]), PSQLState.NO_DATA);
        }
        final SQLWarning warnings = stat.getWarnings();
        if (warnings != null) {
            this.addWarning(warnings);
        }
        return Nullness.castNonNull(stat.getResultSet(), "hasResultSet==true, yet getResultSet()==null");
    }
    
    @Override
    public void execSQLUpdate(final String s) throws SQLException {
        final BaseStatement stmt = (BaseStatement)this.createStatement();
        if (stmt.executeWithFlags(s, 22)) {
            throw new PSQLException(GT.tr("A result was returned when none was expected.", new Object[0]), PSQLState.TOO_MANY_RESULTS);
        }
        final SQLWarning warnings = stmt.getWarnings();
        if (warnings != null) {
            this.addWarning(warnings);
        }
        stmt.close();
    }
    
    void execSQLUpdate(final CachedQuery query) throws SQLException {
        final BaseStatement stmt = (BaseStatement)this.createStatement();
        if (stmt.executeWithFlags(query, 22)) {
            throw new PSQLException(GT.tr("A result was returned when none was expected.", new Object[0]), PSQLState.TOO_MANY_RESULTS);
        }
        final SQLWarning warnings = stmt.getWarnings();
        if (warnings != null) {
            this.addWarning(warnings);
        }
        stmt.close();
    }
    
    public void setCursorName(final String cursor) throws SQLException {
        this.checkClosed();
    }
    
    public String getCursorName() throws SQLException {
        this.checkClosed();
        return null;
    }
    
    public String getURL() throws SQLException {
        return this.creatingURL;
    }
    
    public String getUserName() throws SQLException {
        return this.queryExecutor.getUser();
    }
    
    @Override
    public Fastpath getFastpathAPI() throws SQLException {
        this.checkClosed();
        if (this.fastpath == null) {
            this.fastpath = new Fastpath(this);
        }
        return this.fastpath;
    }
    
    @Override
    public LargeObjectManager getLargeObjectAPI() throws SQLException {
        this.checkClosed();
        if (this.largeobject == null) {
            this.largeobject = new LargeObjectManager(this);
        }
        return this.largeobject;
    }
    
    @Override
    public Object getObject(final String type, final String value, final byte[] byteValue) throws SQLException {
        if (this.typemap != null) {
            final Class<?> c = this.typemap.get(type);
            if (c != null) {
                throw new PSQLException(GT.tr("Custom type maps are not supported.", new Object[0]), PSQLState.NOT_IMPLEMENTED);
            }
        }
        PGobject obj = null;
        if (PgConnection.LOGGER.isLoggable(Level.FINEST)) {
            PgConnection.LOGGER.log(Level.FINEST, "Constructing object from type={0} value=<{1}>", new Object[] { type, value });
        }
        try {
            final Class<? extends PGobject> klass = this.typeCache.getPGobject(type);
            if (klass != null) {
                obj = (PGobject)klass.newInstance();
                obj.setType(type);
                if (byteValue != null && obj instanceof PGBinaryObject) {
                    final PGBinaryObject binObj = (PGBinaryObject)obj;
                    binObj.setByteValue(byteValue, 0);
                }
                else {
                    obj.setValue(value);
                }
            }
            else {
                obj = new PGobject();
                obj.setType(type);
                obj.setValue(value);
            }
            return obj;
        }
        catch (final SQLException sx) {
            throw sx;
        }
        catch (final Exception ex) {
            throw new PSQLException(GT.tr("Failed to create object for: {0}.", type), PSQLState.CONNECTION_FAILURE, ex);
        }
    }
    
    protected TypeInfo createTypeInfo(final BaseConnection conn, final int unknownLength) {
        return new TypeInfoCache(conn, unknownLength);
    }
    
    @Override
    public TypeInfo getTypeInfo() {
        return this.typeCache;
    }
    
    @Override
    public void addDataType(final String type, final String name) {
        try {
            this.addDataType(type, Class.forName(name).asSubclass(PGobject.class));
        }
        catch (final Exception e) {
            throw new RuntimeException("Cannot register new type: " + e);
        }
    }
    
    @Override
    public void addDataType(final String type, final Class<? extends PGobject> klass) throws SQLException {
        this.checkClosed();
        this.typeCache.addDataType(type, klass);
    }
    
    private void initObjectTypes(final Properties info) throws SQLException {
        this.addDataType("box", PGbox.class);
        this.addDataType("circle", PGcircle.class);
        this.addDataType("line", PGline.class);
        this.addDataType("lseg", PGlseg.class);
        this.addDataType("path", PGpath.class);
        this.addDataType("point", PGpoint.class);
        this.addDataType("polygon", PGpolygon.class);
        this.addDataType("money", PGmoney.class);
        this.addDataType("interval", PGInterval.class);
        final Enumeration<?> e = info.propertyNames();
        while (e.hasMoreElements()) {
            final String propertyName = (String)e.nextElement();
            if (propertyName != null && propertyName.startsWith("datatype.")) {
                final String typeName = propertyName.substring(9);
                final String className = Nullness.castNonNull(info.getProperty(propertyName));
                Class<?> klass;
                try {
                    klass = Class.forName(className);
                }
                catch (final ClassNotFoundException cnfe) {
                    throw new PSQLException(GT.tr("Unable to load the class {0} responsible for the datatype {1}", className, typeName), PSQLState.SYSTEM_ERROR, cnfe);
                }
                this.addDataType(typeName, klass.asSubclass(PGobject.class));
            }
        }
    }
    
    @Override
    public void close() throws SQLException {
        if (this.queryExecutor == null) {
            return;
        }
        this.releaseTimer();
        this.queryExecutor.close();
        this.openStackTrace = null;
    }
    
    @Override
    public String nativeSQL(final String sql) throws SQLException {
        this.checkClosed();
        final CachedQuery cachedQuery = this.queryExecutor.createQuery(sql, false, true, new String[0]);
        return cachedQuery.query.getNativeSql();
    }
    
    @Override
    public synchronized SQLWarning getWarnings() throws SQLException {
        this.checkClosed();
        final SQLWarning newWarnings = this.queryExecutor.getWarnings();
        if (this.firstWarning == null) {
            this.firstWarning = newWarnings;
        }
        else if (newWarnings != null) {
            this.firstWarning.setNextWarning(newWarnings);
        }
        return this.firstWarning;
    }
    
    @Override
    public synchronized void clearWarnings() throws SQLException {
        this.checkClosed();
        this.queryExecutor.getWarnings();
        this.firstWarning = null;
    }
    
    @Override
    public void setReadOnly(final boolean readOnly) throws SQLException {
        this.checkClosed();
        if (this.queryExecutor.getTransactionState() != TransactionState.IDLE) {
            throw new PSQLException(GT.tr("Cannot change transaction read-only property in the middle of a transaction.", new Object[0]), PSQLState.ACTIVE_SQL_TRANSACTION);
        }
        if (readOnly != this.readOnly && this.autoCommit && this.readOnlyBehavior == ReadOnlyBehavior.always) {
            this.execSQLUpdate(readOnly ? this.setSessionReadOnly : this.setSessionNotReadOnly);
        }
        this.readOnly = readOnly;
        PgConnection.LOGGER.log(Level.FINE, "  setReadOnly = {0}", readOnly);
    }
    
    @Override
    public boolean isReadOnly() throws SQLException {
        this.checkClosed();
        return this.readOnly;
    }
    
    @Override
    public boolean hintReadOnly() {
        return this.readOnly && this.readOnlyBehavior != ReadOnlyBehavior.ignore;
    }
    
    @Override
    public void setAutoCommit(final boolean autoCommit) throws SQLException {
        this.checkClosed();
        if (this.autoCommit == autoCommit) {
            return;
        }
        if (!this.autoCommit) {
            this.commit();
        }
        if (this.readOnly && this.readOnlyBehavior == ReadOnlyBehavior.always) {
            if (autoCommit) {
                this.autoCommit = true;
                this.execSQLUpdate(this.setSessionReadOnly);
            }
            else {
                this.execSQLUpdate(this.setSessionNotReadOnly);
            }
        }
        this.autoCommit = autoCommit;
        PgConnection.LOGGER.log(Level.FINE, "  setAutoCommit = {0}", autoCommit);
    }
    
    @Override
    public boolean getAutoCommit() throws SQLException {
        this.checkClosed();
        return this.autoCommit;
    }
    
    private void executeTransactionCommand(final Query query) throws SQLException {
        int flags = 22;
        if (this.prepareThreshold == 0) {
            flags |= 0x1;
        }
        try {
            this.getQueryExecutor().execute(query, null, new TransactionCommandHandler(), 0, 0, flags);
        }
        catch (final SQLException e) {
            if (query.getSubqueries() != null || !this.queryExecutor.willHealOnRetry(e)) {
                throw e;
            }
            query.close();
            this.getQueryExecutor().execute(query, null, new TransactionCommandHandler(), 0, 0, flags);
        }
    }
    
    @Override
    public void commit() throws SQLException {
        this.checkClosed();
        if (this.autoCommit) {
            throw new PSQLException(GT.tr("Cannot commit when autoCommit is enabled.", new Object[0]), PSQLState.NO_ACTIVE_SQL_TRANSACTION);
        }
        if (this.queryExecutor.getTransactionState() != TransactionState.IDLE) {
            this.executeTransactionCommand(this.commitQuery);
        }
    }
    
    protected void checkClosed() throws SQLException {
        if (this.isClosed()) {
            throw new PSQLException(GT.tr("This connection has been closed.", new Object[0]), PSQLState.CONNECTION_DOES_NOT_EXIST);
        }
    }
    
    @Override
    public void rollback() throws SQLException {
        this.checkClosed();
        if (this.autoCommit) {
            throw new PSQLException(GT.tr("Cannot rollback when autoCommit is enabled.", new Object[0]), PSQLState.NO_ACTIVE_SQL_TRANSACTION);
        }
        if (this.queryExecutor.getTransactionState() != TransactionState.IDLE) {
            this.executeTransactionCommand(this.rollbackQuery);
        }
        else {
            PgConnection.LOGGER.log(Level.FINE, "Rollback requested but no transaction in progress");
        }
    }
    
    @Override
    public TransactionState getTransactionState() {
        return this.queryExecutor.getTransactionState();
    }
    
    @Override
    public int getTransactionIsolation() throws SQLException {
        this.checkClosed();
        String level = null;
        final ResultSet rs = this.execSQLQuery("SHOW TRANSACTION ISOLATION LEVEL");
        if (rs.next()) {
            level = rs.getString(1);
        }
        rs.close();
        if (level == null) {
            return 2;
        }
        level = level.toUpperCase(Locale.US);
        if (level.equals("READ COMMITTED")) {
            return 2;
        }
        if (level.equals("READ UNCOMMITTED")) {
            return 1;
        }
        if (level.equals("REPEATABLE READ")) {
            return 4;
        }
        if (level.equals("SERIALIZABLE")) {
            return 8;
        }
        return 2;
    }
    
    @Override
    public void setTransactionIsolation(final int level) throws SQLException {
        this.checkClosed();
        if (this.queryExecutor.getTransactionState() != TransactionState.IDLE) {
            throw new PSQLException(GT.tr("Cannot change transaction isolation level in the middle of a transaction.", new Object[0]), PSQLState.ACTIVE_SQL_TRANSACTION);
        }
        final String isolationLevelName = this.getIsolationLevelName(level);
        if (isolationLevelName == null) {
            throw new PSQLException(GT.tr("Transaction isolation level {0} not supported.", level), PSQLState.NOT_IMPLEMENTED);
        }
        final String isolationLevelSQL = "SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL " + isolationLevelName;
        this.execSQLUpdate(isolationLevelSQL);
        PgConnection.LOGGER.log(Level.FINE, "  setTransactionIsolation = {0}", isolationLevelName);
    }
    
    protected String getIsolationLevelName(final int level) {
        switch (level) {
            case 2: {
                return "READ COMMITTED";
            }
            case 8: {
                return "SERIALIZABLE";
            }
            case 1: {
                return "READ UNCOMMITTED";
            }
            case 4: {
                return "REPEATABLE READ";
            }
            default: {
                return null;
            }
        }
    }
    
    @Override
    public void setCatalog(final String catalog) throws SQLException {
        this.checkClosed();
    }
    
    @Override
    public String getCatalog() throws SQLException {
        this.checkClosed();
        return this.queryExecutor.getDatabase();
    }
    
    public boolean getHideUnprivilegedObjects() {
        return this.hideUnprivilegedObjects;
    }
    
    @Override
    protected void finalize() throws Throwable {
        try {
            if (this.openStackTrace != null) {
                PgConnection.LOGGER.log(Level.WARNING, GT.tr("Finalizing a Connection that was never closed:", new Object[0]), this.openStackTrace);
            }
            this.close();
        }
        finally {
            super.finalize();
        }
    }
    
    public String getDBVersionNumber() {
        return this.queryExecutor.getServerVersion();
    }
    
    public int getServerMajorVersion() {
        try {
            final StringTokenizer versionTokens = new StringTokenizer(this.queryExecutor.getServerVersion(), ".");
            return integerPart(versionTokens.nextToken());
        }
        catch (final NoSuchElementException e) {
            return 0;
        }
    }
    
    public int getServerMinorVersion() {
        try {
            final StringTokenizer versionTokens = new StringTokenizer(this.queryExecutor.getServerVersion(), ".");
            versionTokens.nextToken();
            return integerPart(versionTokens.nextToken());
        }
        catch (final NoSuchElementException e) {
            return 0;
        }
    }
    
    @Override
    public boolean haveMinimumServerVersion(final int ver) {
        return this.queryExecutor.getServerVersionNum() >= ver;
    }
    
    @Override
    public boolean haveMinimumServerVersion(final Version ver) {
        return this.haveMinimumServerVersion(ver.getVersionNum());
    }
    
    @Pure
    @Override
    public Encoding getEncoding() {
        return this.queryExecutor.getEncoding();
    }
    
    @Override
    public byte[] encodeString(final String str) throws SQLException {
        try {
            return this.getEncoding().encode(str);
        }
        catch (final IOException ioe) {
            throw new PSQLException(GT.tr("Unable to translate data into the desired encoding.", new Object[0]), PSQLState.DATA_ERROR, ioe);
        }
    }
    
    @Override
    public String escapeString(final String str) throws SQLException {
        return Utils.escapeLiteral(null, str, this.queryExecutor.getStandardConformingStrings()).toString();
    }
    
    @Override
    public boolean getStandardConformingStrings() {
        return this.queryExecutor.getStandardConformingStrings();
    }
    
    @Override
    public boolean isClosed() throws SQLException {
        return this.queryExecutor.isClosed();
    }
    
    @Override
    public void cancelQuery() throws SQLException {
        this.checkClosed();
        this.queryExecutor.sendQueryCancel();
    }
    
    @Override
    public PGNotification[] getNotifications() throws SQLException {
        return this.getNotifications(-1);
    }
    
    @Override
    public PGNotification[] getNotifications(final int timeoutMillis) throws SQLException {
        this.checkClosed();
        this.getQueryExecutor().processNotifies(timeoutMillis);
        final PGNotification[] notifications = this.queryExecutor.getNotifications();
        return notifications;
    }
    
    @Override
    public int getPrepareThreshold() {
        return this.prepareThreshold;
    }
    
    @Override
    public void setDefaultFetchSize(final int fetchSize) throws SQLException {
        if (fetchSize < 0) {
            throw new PSQLException(GT.tr("Fetch size must be a value greater to or equal to 0.", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE);
        }
        this.defaultFetchSize = fetchSize;
        PgConnection.LOGGER.log(Level.FINE, "  setDefaultFetchSize = {0}", fetchSize);
    }
    
    @Override
    public int getDefaultFetchSize() {
        return this.defaultFetchSize;
    }
    
    @Override
    public void setPrepareThreshold(final int newThreshold) {
        this.prepareThreshold = newThreshold;
        PgConnection.LOGGER.log(Level.FINE, "  setPrepareThreshold = {0}", newThreshold);
    }
    
    public boolean getForceBinary() {
        return this.forcebinary;
    }
    
    public void setForceBinary(final boolean newValue) {
        this.forcebinary = newValue;
        PgConnection.LOGGER.log(Level.FINE, "  setForceBinary = {0}", newValue);
    }
    
    public void setTypeMapImpl(final Map<String, Class<?>> map) throws SQLException {
        this.typemap = map;
    }
    
    @Override
    public Logger getLogger() {
        return PgConnection.LOGGER;
    }
    
    public int getProtocolVersion() {
        return this.queryExecutor.getProtocolVersion();
    }
    
    @Override
    public boolean getStringVarcharFlag() {
        return this.bindStringAsVarchar;
    }
    
    @Override
    public CopyManager getCopyAPI() throws SQLException {
        this.checkClosed();
        if (this.copyManager == null) {
            this.copyManager = new CopyManager(this);
        }
        return this.copyManager;
    }
    
    @Override
    public boolean binaryTransferSend(final int oid) {
        return this.queryExecutor.useBinaryForSend(oid);
    }
    
    @Override
    public int getBackendPID() {
        return this.queryExecutor.getBackendPID();
    }
    
    @Override
    public boolean isColumnSanitiserDisabled() {
        return this.disableColumnSanitiser;
    }
    
    public void setDisableColumnSanitiser(final boolean disableColumnSanitiser) {
        this.disableColumnSanitiser = disableColumnSanitiser;
        PgConnection.LOGGER.log(Level.FINE, "  setDisableColumnSanitiser = {0}", disableColumnSanitiser);
    }
    
    @Override
    public PreferQueryMode getPreferQueryMode() {
        return this.queryExecutor.getPreferQueryMode();
    }
    
    @Override
    public AutoSave getAutosave() {
        return this.queryExecutor.getAutoSave();
    }
    
    @Override
    public void setAutosave(final AutoSave autoSave) {
        this.queryExecutor.setAutoSave(autoSave);
        PgConnection.LOGGER.log(Level.FINE, "  setAutosave = {0}", autoSave.value());
    }
    
    protected void abort() {
        this.queryExecutor.abort();
    }
    
    private synchronized Timer getTimer() {
        if (this.cancelTimer == null) {
            this.cancelTimer = Driver.getSharedTimer().getTimer();
        }
        return this.cancelTimer;
    }
    
    private synchronized void releaseTimer() {
        if (this.cancelTimer != null) {
            this.cancelTimer = null;
            Driver.getSharedTimer().releaseTimer();
        }
    }
    
    @Override
    public void addTimerTask(final TimerTask timerTask, final long milliSeconds) {
        final Timer timer = this.getTimer();
        timer.schedule(timerTask, milliSeconds);
    }
    
    @Override
    public void purgeTimerTasks() {
        final Timer timer = this.cancelTimer;
        if (timer != null) {
            timer.purge();
        }
    }
    
    @Override
    public String escapeIdentifier(final String identifier) throws SQLException {
        return Utils.escapeIdentifier(null, identifier).toString();
    }
    
    @Override
    public String escapeLiteral(final String literal) throws SQLException {
        return Utils.escapeLiteral(null, literal, this.queryExecutor.getStandardConformingStrings()).toString();
    }
    
    @Override
    public LruCache<FieldMetadata.Key, FieldMetadata> getFieldMetadataCache() {
        return this.fieldMetadataCache;
    }
    
    @Override
    public PGReplicationConnection getReplicationAPI() {
        return new PGReplicationConnectionImpl(this);
    }
    
    private static int integerPart(final String dirtyString) {
        int start;
        for (start = 0; start < dirtyString.length() && !Character.isDigit(dirtyString.charAt(start)); ++start) {}
        int end;
        for (end = start; end < dirtyString.length() && Character.isDigit(dirtyString.charAt(end)); ++end) {}
        if (start == end) {
            return 0;
        }
        return Integer.parseInt(dirtyString.substring(start, end));
    }
    
    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        this.checkClosed();
        return new PgStatement(this, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        this.checkClosed();
        return new PgPreparedStatement(this, sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        this.checkClosed();
        return new PgCallableStatement(this, sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        this.checkClosed();
        if (this.metadata == null) {
            this.metadata = new PgDatabaseMetaData(this);
        }
        return this.metadata;
    }
    
    @Override
    public void setTypeMap(final Map<String, Class<?>> map) throws SQLException {
        this.setTypeMapImpl(map);
        PgConnection.LOGGER.log(Level.FINE, "  setTypeMap = {0}", map);
    }
    
    protected Array makeArray(final int oid, final String fieldString) throws SQLException {
        return new PgArray(this, oid, fieldString);
    }
    
    protected Blob makeBlob(final long oid) throws SQLException {
        return new PgBlob(this, oid);
    }
    
    protected Clob makeClob(final long oid) throws SQLException {
        return new PgClob(this, oid);
    }
    
    protected SQLXML makeSQLXML() throws SQLException {
        return new PgSQLXML(this);
    }
    
    @Override
    public Clob createClob() throws SQLException {
        this.checkClosed();
        throw Driver.notImplemented(this.getClass(), "createClob()");
    }
    
    @Override
    public Blob createBlob() throws SQLException {
        this.checkClosed();
        throw Driver.notImplemented(this.getClass(), "createBlob()");
    }
    
    @Override
    public NClob createNClob() throws SQLException {
        this.checkClosed();
        throw Driver.notImplemented(this.getClass(), "createNClob()");
    }
    
    @Override
    public SQLXML createSQLXML() throws SQLException {
        this.checkClosed();
        return this.makeSQLXML();
    }
    
    @Override
    public Struct createStruct(final String typeName, final Object[] attributes) throws SQLException {
        this.checkClosed();
        throw Driver.notImplemented(this.getClass(), "createStruct(String, Object[])");
    }
    
    @Override
    public Array createArrayOf(final String typeName, final Object elements) throws SQLException {
        this.checkClosed();
        final TypeInfo typeInfo = this.getTypeInfo();
        final int oid = typeInfo.getPGArrayType(typeName);
        final char delim = typeInfo.getArrayDelimiter(oid);
        if (oid == 0) {
            throw new PSQLException(GT.tr("Unable to find server array type for provided name {0}.", typeName), PSQLState.INVALID_NAME);
        }
        if (elements == null) {
            return this.makeArray(oid, null);
        }
        final ArrayEncoding.ArrayEncoder arraySupport = ArrayEncoding.getArrayEncoder(elements);
        if (arraySupport.supportBinaryRepresentation(oid) && this.getPreferQueryMode() != PreferQueryMode.SIMPLE) {
            return new PgArray(this, oid, arraySupport.toBinaryRepresentation(this, elements, oid));
        }
        final String arrayString = arraySupport.toArrayString(delim, elements);
        return this.makeArray(oid, arrayString);
    }
    
    @Override
    public Array createArrayOf(final String typeName, final Object[] elements) throws SQLException {
        return this.createArrayOf(typeName, (Object)elements);
    }
    
    @Override
    public boolean isValid(final int timeout) throws SQLException {
        if (timeout < 0) {
            throw new PSQLException(GT.tr("Invalid timeout ({0}<0).", timeout), PSQLState.INVALID_PARAMETER_VALUE);
        }
        if (this.isClosed()) {
            return false;
        }
        boolean changedNetworkTimeout = false;
        try {
            final int oldNetworkTimeout = this.getNetworkTimeout();
            final int newNetworkTimeout = (int)Math.min(timeout * 1000L, 2147483647L);
            try {
                if (newNetworkTimeout != 0 && (oldNetworkTimeout == 0 || newNetworkTimeout < oldNetworkTimeout)) {
                    changedNetworkTimeout = true;
                    this.setNetworkTimeout(null, newNetworkTimeout);
                }
                if (this.replicationConnection) {
                    final Statement statement = this.createStatement();
                    statement.execute("IDENTIFY_SYSTEM");
                    statement.close();
                }
                else {
                    if (this.checkConnectionQuery == null) {
                        this.checkConnectionQuery = this.prepareStatement("");
                    }
                    this.checkConnectionQuery.executeUpdate();
                }
                return true;
            }
            finally {
                if (changedNetworkTimeout) {
                    this.setNetworkTimeout(null, oldNetworkTimeout);
                }
            }
        }
        catch (final SQLException e) {
            if (PSQLState.IN_FAILED_SQL_TRANSACTION.getState().equals(e.getSQLState())) {
                return true;
            }
            PgConnection.LOGGER.log(Level.FINE, GT.tr("Validating connection.", new Object[0]), e);
            return false;
        }
    }
    
    @Override
    public void setClientInfo(final String name, String value) throws SQLClientInfoException {
        try {
            this.checkClosed();
        }
        catch (final SQLException cause) {
            final Map<String, ClientInfoStatus> failures = new HashMap<String, ClientInfoStatus>();
            failures.put(name, ClientInfoStatus.REASON_UNKNOWN);
            throw new SQLClientInfoException(GT.tr("This connection has been closed.", new Object[0]), failures, cause);
        }
        if (!this.haveMinimumServerVersion(ServerVersion.v9_0) || !"ApplicationName".equals(name)) {
            this.addWarning(new SQLWarning(GT.tr("ClientInfo property not supported.", new Object[0]), PSQLState.NOT_IMPLEMENTED.getState()));
            return;
        }
        if (value == null) {
            value = "";
        }
        final String oldValue = this.queryExecutor.getApplicationName();
        if (value.equals(oldValue)) {
            return;
        }
        try {
            final StringBuilder sql = new StringBuilder("SET application_name = '");
            Utils.escapeLiteral(sql, value, this.getStandardConformingStrings());
            sql.append("'");
            this.execSQLUpdate(sql.toString());
        }
        catch (final SQLException sqle) {
            final Map<String, ClientInfoStatus> failures2 = new HashMap<String, ClientInfoStatus>();
            failures2.put(name, ClientInfoStatus.REASON_UNKNOWN);
            throw new SQLClientInfoException(GT.tr("Failed to set ClientInfo property: {0}", "ApplicationName"), sqle.getSQLState(), failures2, sqle);
        }
        if (PgConnection.LOGGER.isLoggable(Level.FINE)) {
            PgConnection.LOGGER.log(Level.FINE, "  setClientInfo = {0} {1}", new Object[] { name, value });
        }
        this.clientInfo.put(name, value);
    }
    
    @Override
    public void setClientInfo(final Properties properties) throws SQLClientInfoException {
        try {
            this.checkClosed();
        }
        catch (final SQLException cause) {
            final Map<String, ClientInfoStatus> failures = new HashMap<String, ClientInfoStatus>();
            for (final Map.Entry<Object, Object> e : properties.entrySet()) {
                failures.put(e.getKey(), ClientInfoStatus.REASON_UNKNOWN);
            }
            throw new SQLClientInfoException(GT.tr("This connection has been closed.", new Object[0]), failures, cause);
        }
        final Map<String, ClientInfoStatus> failures2 = new HashMap<String, ClientInfoStatus>();
        for (final String name : new String[] { "ApplicationName" }) {
            try {
                this.setClientInfo(name, properties.getProperty(name, null));
            }
            catch (final SQLClientInfoException e2) {
                failures2.putAll(e2.getFailedProperties());
            }
        }
        if (!failures2.isEmpty()) {
            throw new SQLClientInfoException(GT.tr("One or more ClientInfo failed.", new Object[0]), PSQLState.NOT_IMPLEMENTED.getState(), failures2);
        }
    }
    
    @Override
    public String getClientInfo(final String name) throws SQLException {
        this.checkClosed();
        this.clientInfo.put("ApplicationName", this.queryExecutor.getApplicationName());
        return this.clientInfo.getProperty(name);
    }
    
    @Override
    public Properties getClientInfo() throws SQLException {
        this.checkClosed();
        this.clientInfo.put("ApplicationName", this.queryExecutor.getApplicationName());
        return this.clientInfo;
    }
    
    public <T> T createQueryObject(final Class<T> ifc) throws SQLException {
        this.checkClosed();
        throw Driver.notImplemented(this.getClass(), "createQueryObject(Class<T>)");
    }
    
    @Override
    public boolean getLogServerErrorDetail() {
        return this.logServerErrorDetail;
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        this.checkClosed();
        return iface.isAssignableFrom(this.getClass());
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        this.checkClosed();
        if (iface.isAssignableFrom(this.getClass())) {
            return iface.cast(this);
        }
        throw new SQLException("Cannot unwrap to " + iface.getName());
    }
    
    @Override
    public String getSchema() throws SQLException {
        this.checkClosed();
        final Statement stmt = this.createStatement();
        try {
            final ResultSet rs = stmt.executeQuery("select current_schema()");
            try {
                if (!rs.next()) {
                    return null;
                }
                return rs.getString(1);
            }
            finally {
                rs.close();
            }
        }
        finally {
            stmt.close();
        }
    }
    
    @Override
    public void setSchema(final String schema) throws SQLException {
        this.checkClosed();
        final Statement stmt = this.createStatement();
        try {
            if (schema == null) {
                stmt.executeUpdate("SET SESSION search_path TO DEFAULT");
            }
            else {
                final StringBuilder sb = new StringBuilder();
                sb.append("SET SESSION search_path TO '");
                Utils.escapeLiteral(sb, schema, this.getStandardConformingStrings());
                sb.append("'");
                stmt.executeUpdate(sb.toString());
                PgConnection.LOGGER.log(Level.FINE, "  setSchema = {0}", schema);
            }
        }
        finally {
            stmt.close();
        }
    }
    
    @Override
    public void abort(final Executor executor) throws SQLException {
        if (executor == null) {
            throw new SQLException("executor is null");
        }
        if (this.isClosed()) {
            return;
        }
        PgConnection.SQL_PERMISSION_ABORT.checkGuard(this);
        final AbortCommand command = new AbortCommand();
        executor.execute(command);
    }
    
    @Override
    public void setNetworkTimeout(final Executor executor, final int milliseconds) throws SQLException {
        this.checkClosed();
        if (milliseconds < 0) {
            throw new PSQLException(GT.tr("Network timeout must be a value greater than or equal to 0.", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE);
        }
        final SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(PgConnection.SQL_PERMISSION_NETWORK_TIMEOUT);
        }
        try {
            this.queryExecutor.setNetworkTimeout(milliseconds);
        }
        catch (final IOException ioe) {
            throw new PSQLException(GT.tr("Unable to set network timeout.", new Object[0]), PSQLState.COMMUNICATION_ERROR, ioe);
        }
    }
    
    @Override
    public int getNetworkTimeout() throws SQLException {
        this.checkClosed();
        try {
            return this.queryExecutor.getNetworkTimeout();
        }
        catch (final IOException ioe) {
            throw new PSQLException(GT.tr("Unable to get network timeout.", new Object[0]), PSQLState.COMMUNICATION_ERROR, ioe);
        }
    }
    
    @Override
    public void setHoldability(final int holdability) throws SQLException {
        this.checkClosed();
        switch (holdability) {
            case 2: {
                this.rsHoldability = holdability;
                break;
            }
            case 1: {
                this.rsHoldability = holdability;
                break;
            }
            default: {
                throw new PSQLException(GT.tr("Unknown ResultSet holdability setting: {0}.", holdability), PSQLState.INVALID_PARAMETER_VALUE);
            }
        }
        PgConnection.LOGGER.log(Level.FINE, "  setHoldability = {0}", holdability);
    }
    
    @Override
    public int getHoldability() throws SQLException {
        this.checkClosed();
        return this.rsHoldability;
    }
    
    @Override
    public Savepoint setSavepoint() throws SQLException {
        this.checkClosed();
        if (this.getAutoCommit()) {
            throw new PSQLException(GT.tr("Cannot establish a savepoint in auto-commit mode.", new Object[0]), PSQLState.NO_ACTIVE_SQL_TRANSACTION);
        }
        final PSQLSavepoint savepoint = new PSQLSavepoint(this.savepointId++);
        final String pgName = savepoint.getPGName();
        final Statement stmt = this.createStatement();
        stmt.executeUpdate("SAVEPOINT " + pgName);
        stmt.close();
        return savepoint;
    }
    
    @Override
    public Savepoint setSavepoint(final String name) throws SQLException {
        this.checkClosed();
        if (this.getAutoCommit()) {
            throw new PSQLException(GT.tr("Cannot establish a savepoint in auto-commit mode.", new Object[0]), PSQLState.NO_ACTIVE_SQL_TRANSACTION);
        }
        final PSQLSavepoint savepoint = new PSQLSavepoint(name);
        final Statement stmt = this.createStatement();
        stmt.executeUpdate("SAVEPOINT " + savepoint.getPGName());
        stmt.close();
        return savepoint;
    }
    
    @Override
    public void rollback(final Savepoint savepoint) throws SQLException {
        this.checkClosed();
        final PSQLSavepoint pgSavepoint = (PSQLSavepoint)savepoint;
        this.execSQLUpdate("ROLLBACK TO SAVEPOINT " + pgSavepoint.getPGName());
    }
    
    @Override
    public void releaseSavepoint(final Savepoint savepoint) throws SQLException {
        this.checkClosed();
        final PSQLSavepoint pgSavepoint = (PSQLSavepoint)savepoint;
        this.execSQLUpdate("RELEASE SAVEPOINT " + pgSavepoint.getPGName());
        pgSavepoint.invalidate();
    }
    
    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency) throws SQLException {
        this.checkClosed();
        return this.createStatement(resultSetType, resultSetConcurrency, this.getHoldability());
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        this.checkClosed();
        return this.prepareStatement(sql, resultSetType, resultSetConcurrency, this.getHoldability());
    }
    
    @Override
    public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        this.checkClosed();
        return this.prepareCall(sql, resultSetType, resultSetConcurrency, this.getHoldability());
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys) throws SQLException {
        if (autoGeneratedKeys != 1) {
            return this.prepareStatement(sql);
        }
        return this.prepareStatement(sql, (String[])null);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes) throws SQLException {
        if (columnIndexes != null && columnIndexes.length == 0) {
            return this.prepareStatement(sql);
        }
        this.checkClosed();
        throw new PSQLException(GT.tr("Returning autogenerated keys is not supported.", new Object[0]), PSQLState.NOT_IMPLEMENTED);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final String[] columnNames) throws SQLException {
        if (columnNames != null && columnNames.length == 0) {
            return this.prepareStatement(sql);
        }
        final CachedQuery cachedQuery = this.borrowReturningQuery(sql, columnNames);
        final PgPreparedStatement ps = new PgPreparedStatement(this, cachedQuery, 1003, 1007, this.getHoldability());
        final Query query = cachedQuery.query;
        final SqlCommand sqlCommand = query.getSqlCommand();
        if (sqlCommand != null) {
            ps.wantsGeneratedKeysAlways = sqlCommand.isReturningKeywordPresent();
        }
        return ps;
    }
    
    @Override
    public final Map<String, String> getParameterStatuses() {
        return this.queryExecutor.getParameterStatuses();
    }
    
    @Override
    public final String getParameterStatus(final String parameterName) {
        return this.queryExecutor.getParameterStatus(parameterName);
    }
    
    @Override
    public PGXmlFactoryFactory getXmlFactoryFactory() throws SQLException {
        PGXmlFactoryFactory xmlFactoryFactory = this.xmlFactoryFactory;
        if (xmlFactoryFactory != null) {
            return xmlFactoryFactory;
        }
        if (this.xmlFactoryFactoryClass == null || this.xmlFactoryFactoryClass.equals("")) {
            xmlFactoryFactory = DefaultPGXmlFactoryFactory.INSTANCE;
        }
        else if (this.xmlFactoryFactoryClass.equals("LEGACY_INSECURE")) {
            xmlFactoryFactory = LegacyInsecurePGXmlFactoryFactory.INSTANCE;
        }
        else {
            Class<?> clazz;
            try {
                clazz = Class.forName(this.xmlFactoryFactoryClass);
            }
            catch (final ClassNotFoundException ex) {
                throw new PSQLException(GT.tr("Could not instantiate xmlFactoryFactory: {0}", this.xmlFactoryFactoryClass), PSQLState.INVALID_PARAMETER_VALUE, ex);
            }
            if (!clazz.isAssignableFrom(PGXmlFactoryFactory.class)) {
                throw new PSQLException(GT.tr("Connection property xmlFactoryFactory must implement PGXmlFactoryFactory: {0}", this.xmlFactoryFactoryClass), PSQLState.INVALID_PARAMETER_VALUE);
            }
            try {
                xmlFactoryFactory = (PGXmlFactoryFactory)clazz.newInstance();
            }
            catch (final Exception ex2) {
                throw new PSQLException(GT.tr("Could not instantiate xmlFactoryFactory: {0}", this.xmlFactoryFactoryClass), PSQLState.INVALID_PARAMETER_VALUE, ex2);
            }
        }
        return this.xmlFactoryFactory = xmlFactoryFactory;
    }
    
    static {
        LOGGER = Logger.getLogger(PgConnection.class.getName());
        SUPPORTED_BINARY_OIDS = getSupportedBinaryOids();
        SQL_PERMISSION_ABORT = new SQLPermission("callAbort");
        SQL_PERMISSION_NETWORK_TIMEOUT = new SQLPermission("setNetworkTimeout");
    }
    
    private enum ReadOnlyBehavior
    {
        ignore, 
        transaction, 
        always;
    }
    
    private class TransactionCommandHandler extends ResultHandlerBase
    {
        @Override
        public void handleCompletion() throws SQLException {
            final SQLWarning warning = this.getWarning();
            if (warning != null) {
                PgConnection.this.addWarning(warning);
            }
            super.handleCompletion();
        }
    }
    
    public class AbortCommand implements Runnable
    {
        @Override
        public void run() {
            PgConnection.this.abort();
        }
    }
}
