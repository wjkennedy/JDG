// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.db;

import java.sql.SQLException;
import org.ofbiz.core.entity.GenericEntityException;

public interface ConnectionManager
{
    EntityHandler getEntityHandler(final String p0) throws GenericEntityException, SQLException;
    
    void close() throws SQLException;
}
