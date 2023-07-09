// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.db;

import org.slf4j.LoggerFactory;
import java.sql.SQLException;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.config.JdbcDatasourceInfo;
import org.ofbiz.core.entity.config.DatasourceInfo;
import com.atlassian.jira.plugins.datagenerator.db.jdbc.JdbcConnectionManager;
import com.atlassian.jira.plugins.datagenerator.db.postgres.PostgresConnectionManager;
import org.ofbiz.core.entity.config.EntityConfigUtil;
import com.atlassian.jira.component.ComponentAccessor;
import org.ofbiz.core.entity.DelegatorInterface;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.plugins.datagenerator.config.GeneratorConfiguration;
import com.atlassian.jira.plugins.datagenerator.db.postgres.JiraSequenceIdGenerator;
import org.slf4j.Logger;

public class EntityManager implements AutoCloseable
{
    private static final Logger LOG;
    private final ConnectionManager connectionManager;
    
    public EntityManager(final JiraSequenceIdGenerator sequenceIdGenerator, final GeneratorConfiguration generatorConfiguration) throws GenericEntityException, SQLException {
        Assertions.notNull("GeneratorConfiguration", (Object)generatorConfiguration);
        final DelegatorInterface delegator = (DelegatorInterface)ComponentAccessor.getComponent((Class)DelegatorInterface.class);
        final String helperName = delegator.getEntityHelper("Issue").getHelperName();
        final DatasourceInfo datasourceInfo = EntityConfigUtil.getInstance().getDatasourceInfo(helperName);
        JdbcDatasourceInfo jdbcDatasource;
        if (datasourceInfo == null) {
            EntityManager.LOG.warn("Null DatasourceInfo for helper {}, using generic JDBC", (Object)helperName);
            jdbcDatasource = null;
        }
        else {
            jdbcDatasource = datasourceInfo.getJdbcDatasource();
            if (jdbcDatasource == null) {
                EntityManager.LOG.warn("Null JdbcDatasource for helper {}, using generic JDBC", (Object)helperName);
            }
        }
        if (!generatorConfiguration.system.forceJdbc && jdbcDatasource != null && "org.postgresql.Driver".equals(jdbcDatasource.getDriverClassName())) {
            EntityManager.LOG.info("Connecting to PostgreSQL");
            this.connectionManager = new PostgresConnectionManager(sequenceIdGenerator, jdbcDatasource, delegator);
        }
        else {
            EntityManager.LOG.info("Connecting to generic JDBC");
            this.connectionManager = new JdbcConnectionManager(sequenceIdGenerator, delegator);
        }
    }
    
    public EntityHandler getEntityHandler(final String name) throws GenericEntityException, SQLException {
        return this.connectionManager.getEntityHandler(name);
    }
    
    public void shutdown() throws SQLException {
        this.connectionManager.close();
    }
    
    @Override
    public void close() throws Exception {
        this.shutdown();
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)EntityManager.class);
    }
}
