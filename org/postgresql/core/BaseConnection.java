// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import org.postgresql.xml.PGXmlFactoryFactory;
import org.postgresql.jdbc.FieldMetadata;
import org.postgresql.util.LruCache;
import java.util.TimerTask;
import java.util.logging.Logger;
import org.postgresql.jdbc.TimestampUtils;
import org.checkerframework.dataflow.qual.Pure;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import org.postgresql.PGConnection;

public interface BaseConnection extends PGConnection, Connection
{
    void cancelQuery() throws SQLException;
    
    ResultSet execSQLQuery(final String p0) throws SQLException;
    
    ResultSet execSQLQuery(final String p0, final int p1, final int p2) throws SQLException;
    
    void execSQLUpdate(final String p0) throws SQLException;
    
    QueryExecutor getQueryExecutor();
    
    ReplicationProtocol getReplicationProtocol();
    
    Object getObject(final String p0, final String p1, final byte[] p2) throws SQLException;
    
    @Pure
    Encoding getEncoding() throws SQLException;
    
    TypeInfo getTypeInfo();
    
    boolean haveMinimumServerVersion(final int p0);
    
    boolean haveMinimumServerVersion(final Version p0);
    
    byte[] encodeString(final String p0) throws SQLException;
    
    String escapeString(final String p0) throws SQLException;
    
    boolean getStandardConformingStrings();
    
    TimestampUtils getTimestampUtils();
    
    Logger getLogger();
    
    boolean getStringVarcharFlag();
    
    TransactionState getTransactionState();
    
    boolean binaryTransferSend(final int p0);
    
    boolean isColumnSanitiserDisabled();
    
    void addTimerTask(final TimerTask p0, final long p1);
    
    void purgeTimerTasks();
    
    LruCache<FieldMetadata.Key, FieldMetadata> getFieldMetadataCache();
    
    CachedQuery createQuery(final String p0, final boolean p1, final boolean p2, final String... p3) throws SQLException;
    
    void setFlushCacheOnDeallocate(final boolean p0);
    
    boolean hintReadOnly();
    
    PGXmlFactoryFactory getXmlFactoryFactory() throws SQLException;
    
    boolean getLogServerErrorDetail();
}
