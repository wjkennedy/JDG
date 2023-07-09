// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.db.jdbc;

import org.slf4j.LoggerFactory;
import java.sql.BatchUpdateException;
import java.util.Map;
import java.sql.SQLException;
import org.ofbiz.core.entity.GenericEntityException;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.ofbiz.core.entity.model.ModelField;
import java.util.ArrayList;
import org.ofbiz.core.entity.model.ModelReader;
import org.ofbiz.core.entity.DelegatorInterface;
import com.atlassian.jira.plugins.datagenerator.db.postgres.JiraSequenceIdGenerator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.ofbiz.core.entity.model.ModelEntity;
import org.slf4j.Logger;
import com.atlassian.jira.plugins.datagenerator.db.EntityHandler;

public class JdbcEntityHandler implements EntityHandler
{
    private static final Logger LOG;
    private static final int BATCH_SIZE = 1000;
    private final String[] fieldNames;
    private final ModelEntity modelEntity;
    private final PreparedStatement preparedStatement;
    private final Connection connection;
    private final JiraSequenceIdGenerator sequenceIdGenerator;
    private int counter;
    
    public JdbcEntityHandler(final JiraSequenceIdGenerator sequenceIdGenerator, final Connection connection, final DelegatorInterface delegator, final String entityName) throws GenericEntityException, SQLException {
        final ModelReader modelReader = ModelReader.getModelReader(delegator.getDelegatorName());
        this.modelEntity = modelReader.getModelEntity(entityName);
        final String tableName = this.modelEntity.getTableName(delegator.getEntityHelper(this.modelEntity).getHelperName());
        this.sequenceIdGenerator = sequenceIdGenerator;
        final int fieldCount = this.modelEntity.getFieldsSize();
        final List<String> columnNames = new ArrayList<String>(fieldCount);
        final List<String> fieldNames = new ArrayList<String>(fieldCount);
        final List<ModelField> fieldsCopy = this.modelEntity.getFieldsCopy();
        for (final ModelField field : fieldsCopy) {
            columnNames.add(field.getColName());
            fieldNames.add(field.getName());
        }
        final String query = "INSERT INTO " + tableName + " (" + StringUtils.join((Iterable)columnNames, ',') + ") VALUES (" + StringUtils.repeat("?", ",", fieldCount) + ')';
        JdbcEntityHandler.LOG.info("Preparing statement: " + query);
        this.connection = connection;
        this.preparedStatement = connection.prepareStatement(query);
        this.fieldNames = fieldNames.toArray(new String[fieldCount]);
    }
    
    @Override
    public Long getNextSequenceId() {
        return this.sequenceIdGenerator.getNextSequenceId(this.modelEntity.getEntityName());
    }
    
    @Override
    public void store(final Map<String, Object> entity) throws SQLException {
        int arrayIdx = 0;
        int columnIdx = 1;
        while (arrayIdx < this.fieldNames.length) {
            this.preparedStatement.setObject(columnIdx++, entity.get(this.fieldNames[arrayIdx++]));
        }
        this.preparedStatement.addBatch();
        if (++this.counter % 1000 == 0) {
            this.executeBatch();
        }
    }
    
    @Override
    public void close() throws SQLException {
        if (this.counter % 1000 != 0) {
            this.executeBatch();
        }
        this.preparedStatement.close();
    }
    
    private void executeBatch() throws SQLException {
        try {
            this.preparedStatement.executeBatch();
            this.connection.commit();
        }
        catch (final BatchUpdateException ex) {
            JdbcEntityHandler.LOG.error("Error executing batch update: " + ex);
            for (SQLException next = ex.getNextException(); next != null; next = next.getNextException()) {
                JdbcEntityHandler.LOG.error("  Chained exception: " + next);
            }
            this.connection.rollback();
            throw ex;
        }
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)JdbcEntityHandler.class);
    }
}
