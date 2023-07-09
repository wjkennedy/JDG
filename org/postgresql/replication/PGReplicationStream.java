// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.replication;

import java.sql.SQLException;
import java.nio.ByteBuffer;

public interface PGReplicationStream extends AutoCloseable
{
    ByteBuffer read() throws SQLException;
    
    ByteBuffer readPending() throws SQLException;
    
    LogSequenceNumber getLastReceiveLSN();
    
    LogSequenceNumber getLastFlushedLSN();
    
    LogSequenceNumber getLastAppliedLSN();
    
    void setFlushedLSN(final LogSequenceNumber p0);
    
    void setAppliedLSN(final LogSequenceNumber p0);
    
    void forceUpdateStatus() throws SQLException;
    
    boolean isClosed();
    
    void close() throws SQLException;
}
