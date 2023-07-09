// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql;

import java.util.Map;
import org.postgresql.replication.PGReplicationConnection;
import org.postgresql.jdbc.AutoSave;
import org.postgresql.jdbc.PreferQueryMode;
import org.postgresql.util.PGobject;
import org.postgresql.fastpath.Fastpath;
import org.postgresql.largeobject.LargeObjectManager;
import org.postgresql.copy.CopyManager;
import java.sql.SQLException;
import java.sql.Array;

public interface PGConnection
{
    Array createArrayOf(final String p0, final Object p1) throws SQLException;
    
    PGNotification[] getNotifications() throws SQLException;
    
    PGNotification[] getNotifications(final int p0) throws SQLException;
    
    CopyManager getCopyAPI() throws SQLException;
    
    LargeObjectManager getLargeObjectAPI() throws SQLException;
    
    @Deprecated
    Fastpath getFastpathAPI() throws SQLException;
    
    @Deprecated
    void addDataType(final String p0, final String p1);
    
    void addDataType(final String p0, final Class<? extends PGobject> p1) throws SQLException;
    
    void setPrepareThreshold(final int p0);
    
    int getPrepareThreshold();
    
    void setDefaultFetchSize(final int p0) throws SQLException;
    
    int getDefaultFetchSize();
    
    int getBackendPID();
    
    void cancelQuery() throws SQLException;
    
    String escapeIdentifier(final String p0) throws SQLException;
    
    String escapeLiteral(final String p0) throws SQLException;
    
    PreferQueryMode getPreferQueryMode();
    
    AutoSave getAutosave();
    
    void setAutosave(final AutoSave p0);
    
    PGReplicationConnection getReplicationAPI();
    
    Map<String, String> getParameterStatuses();
    
    String getParameterStatus(final String p0);
}
