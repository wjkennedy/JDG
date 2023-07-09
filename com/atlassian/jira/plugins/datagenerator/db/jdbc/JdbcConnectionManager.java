// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.db.jdbc;

import com.atlassian.jira.plugins.datagenerator.db.EntityHandler;
import org.ofbiz.core.entity.GenericEntityException;
import java.sql.SQLException;
import org.ofbiz.core.entity.ConnectionFactory;
import com.atlassian.jira.plugins.datagenerator.db.postgres.JiraSequenceIdGenerator;
import java.sql.Connection;
import org.ofbiz.core.entity.DelegatorInterface;
import com.atlassian.jira.plugins.datagenerator.db.ConnectionManager;

public class JdbcConnectionManager implements ConnectionManager
{
    private final DelegatorInterface delegator;
    private final Connection connection;
    private final JiraSequenceIdGenerator sequenceIdGenerator;
    
    public JdbcConnectionManager(final JiraSequenceIdGenerator sequenceIdGenerator, final DelegatorInterface delegator) throws SQLException, GenericEntityException {
        this.delegator = delegator;
        this.sequenceIdGenerator = sequenceIdGenerator;
        (this.connection = ConnectionFactory.getConnection(delegator.getEntityHelperName("Issue"))).setAutoCommit(false);
    }
    
    @Override
    public EntityHandler getEntityHandler(final String entityName) throws GenericEntityException, SQLException {
        return new JdbcEntityHandler(this.sequenceIdGenerator, this.connection, this.delegator, entityName);
    }
    
    @Override
    public void close() throws SQLException {
        this.connection.close();
    }
}
