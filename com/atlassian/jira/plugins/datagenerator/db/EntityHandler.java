// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.db;

import java.sql.SQLException;
import java.util.Map;

public interface EntityHandler
{
    Long getNextSequenceId();
    
    void store(final Map<String, Object> p0) throws SQLException;
    
    void close() throws SQLException;
}
