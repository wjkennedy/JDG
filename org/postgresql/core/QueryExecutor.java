// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import java.util.Map;
import java.io.IOException;
import org.postgresql.jdbc.AutoSave;
import org.postgresql.jdbc.PreferQueryMode;
import org.postgresql.jdbc.EscapeSyntaxCallMode;
import java.util.TimeZone;
import java.sql.SQLWarning;
import org.postgresql.PGNotification;
import org.postgresql.util.HostSpec;
import java.util.Set;
import org.postgresql.copy.CopyOperation;
import java.util.List;
import org.postgresql.jdbc.BatchResultHandler;
import java.sql.SQLException;
import org.postgresql.core.v3.TypeTransferModeRegistry;

public interface QueryExecutor extends TypeTransferModeRegistry
{
    public static final int QUERY_ONESHOT = 1;
    public static final int QUERY_NO_METADATA = 2;
    public static final int QUERY_NO_RESULTS = 4;
    public static final int QUERY_FORWARD_CURSOR = 8;
    public static final int QUERY_SUPPRESS_BEGIN = 16;
    public static final int QUERY_DESCRIBE_ONLY = 32;
    public static final int QUERY_BOTH_ROWS_AND_STATUS = 64;
    public static final int QUERY_FORCE_DESCRIBE_PORTAL = 512;
    @Deprecated
    public static final int QUERY_DISALLOW_BATCHING = 128;
    public static final int QUERY_NO_BINARY_TRANSFER = 256;
    public static final int QUERY_EXECUTE_AS_SIMPLE = 1024;
    public static final int MAX_SAVE_POINTS = 1000;
    public static final int QUERY_READ_ONLY_HINT = 2048;
    
    void execute(final Query p0, final ParameterList p1, final ResultHandler p2, final int p3, final int p4, final int p5) throws SQLException;
    
    void execute(final Query[] p0, final ParameterList[] p1, final BatchResultHandler p2, final int p3, final int p4, final int p5) throws SQLException;
    
    void fetch(final ResultCursor p0, final ResultHandler p1, final int p2) throws SQLException;
    
    Query createSimpleQuery(final String p0) throws SQLException;
    
    boolean isReWriteBatchedInsertsEnabled();
    
    CachedQuery createQuery(final String p0, final boolean p1, final boolean p2, final String... p3) throws SQLException;
    
    Object createQueryKey(final String p0, final boolean p1, final boolean p2, final String... p3);
    
    CachedQuery createQueryByKey(final Object p0) throws SQLException;
    
    CachedQuery borrowQueryByKey(final Object p0) throws SQLException;
    
    CachedQuery borrowQuery(final String p0) throws SQLException;
    
    CachedQuery borrowCallableQuery(final String p0) throws SQLException;
    
    CachedQuery borrowReturningQuery(final String p0, final String[] p1) throws SQLException;
    
    void releaseQuery(final CachedQuery p0);
    
    Query wrap(final List<NativeQuery> p0);
    
    void processNotifies() throws SQLException;
    
    void processNotifies(final int p0) throws SQLException;
    
    @Deprecated
    ParameterList createFastpathParameters(final int p0);
    
    @Deprecated
    byte[] fastpathCall(final int p0, final ParameterList p1, final boolean p2) throws SQLException;
    
    CopyOperation startCopy(final String p0, final boolean p1) throws SQLException;
    
    int getProtocolVersion();
    
    void setBinaryReceiveOids(final Set<Integer> p0);
    
    void setBinarySendOids(final Set<Integer> p0);
    
    boolean getIntegerDateTimes();
    
    HostSpec getHostSpec();
    
    String getUser();
    
    String getDatabase();
    
    void sendQueryCancel() throws SQLException;
    
    int getBackendPID();
    
    void abort();
    
    void close();
    
    boolean isClosed();
    
    String getServerVersion();
    
    PGNotification[] getNotifications() throws SQLException;
    
    SQLWarning getWarnings();
    
    int getServerVersionNum();
    
    TransactionState getTransactionState();
    
    boolean getStandardConformingStrings();
    
    TimeZone getTimeZone();
    
    Encoding getEncoding();
    
    String getApplicationName();
    
    boolean isColumnSanitiserDisabled();
    
    EscapeSyntaxCallMode getEscapeSyntaxCallMode();
    
    PreferQueryMode getPreferQueryMode();
    
    AutoSave getAutoSave();
    
    void setAutoSave(final AutoSave p0);
    
    boolean willHealOnRetry(final SQLException p0);
    
    void setFlushCacheOnDeallocate(final boolean p0);
    
    ReplicationProtocol getReplicationProtocol();
    
    void setNetworkTimeout(final int p0) throws IOException;
    
    int getNetworkTimeout() throws IOException;
    
    Map<String, String> getParameterStatuses();
    
    String getParameterStatus(final String p0);
}
