// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.db.postgres;

import org.postgresql.util.ByteStreamWriter;
import org.ofbiz.core.entity.GenericEntityException;
import com.atlassian.jira.util.Supplier;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelReader;
import com.atlassian.jira.plugins.datagenerator.db.EntityHandler;
import java.util.Iterator;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.sql.SQLException;
import org.postgresql.copy.CopyManager;
import java.util.Collection;
import org.apache.commons.lang.StringUtils;
import org.postgresql.core.BaseConnection;
import org.postgresql.copy.CopyIn;
import org.ofbiz.core.entity.model.ModelField;
import java.util.List;
import com.google.common.collect.Sets;
import org.ofbiz.core.entity.config.JdbcDatasourceInfo;
import org.ofbiz.core.entity.DelegatorInterface;
import java.util.Properties;
import java.sql.Connection;
import java.util.Set;
import org.postgresql.Driver;
import com.atlassian.jira.plugins.datagenerator.db.ConnectionManager;

public class PostgresConnectionManager implements ConnectionManager
{
    private final Driver driver;
    private final Set<Connection> openConnections;
    private final Properties props;
    private final DelegatorInterface delegator;
    private final String databaseUri;
    private final JiraSequenceIdGenerator sequenceIdGenerator;
    
    public PostgresConnectionManager(final JiraSequenceIdGenerator sequenceIdGenerator, final JdbcDatasourceInfo jdbcDatasource, final DelegatorInterface delegator) {
        this.driver = new Driver();
        this.openConnections = Sets.newHashSet();
        this.props = new Properties();
        this.sequenceIdGenerator = sequenceIdGenerator;
        this.delegator = delegator;
        this.props.put("user", jdbcDatasource.getUsername());
        this.props.put("password", jdbcDatasource.getPassword());
        this.databaseUri = jdbcDatasource.getUri();
    }
    
    public CopyIn startCopy(final String tableName, final List<ModelField> fields) throws SQLException {
        final BaseConnection baseConnection = (BaseConnection)this.driver.connect(this.databaseUri, this.props);
        this.openConnections.add(baseConnection);
        baseConnection.setAutoCommit(false);
        final CopyManager copyManager = baseConnection.getCopyAPI();
        return new CopyInDelegate(copyManager.copyIn("COPY " + tableName + "(" + StringUtils.join((Collection)getColumnNames(fields), ",") + ") FROM STDIN"), (Connection)baseConnection);
    }
    
    public static List<String> getColumnNames(final List<ModelField> fields) {
        final List<String> names = new ArrayList<String>(fields.size());
        for (final ModelField field : fields) {
            names.add(field.getColName());
        }
        return (List<String>)ImmutableList.copyOf((Collection)names);
    }
    
    @Override
    public void close() {
        for (final Connection connection : this.openConnections) {
            try {
                connection.close();
            }
            catch (final SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    @Override
    public EntityHandler getEntityHandler(final String entityName) throws GenericEntityException {
        final ModelReader modelReader = ModelReader.getModelReader(this.delegator.getDelegatorName());
        final ModelEntity modelEntity = modelReader.getModelEntity(entityName);
        final String tableName = modelEntity.getTableName(this.delegator.getEntityHelper(modelEntity).getHelperName());
        final Supplier<CopyIn> copyInSupplier = (Supplier<CopyIn>)(() -> {
            try {
                return this.startCopy(tableName, modelEntity.getFieldsCopy());
            }
            catch (final SQLException sqle) {
                throw new RuntimeException(sqle);
            }
        });
        return new PostgresEntityHandler(this.sequenceIdGenerator, modelEntity, copyInSupplier);
    }
    
    private class CopyInDelegate implements CopyIn
    {
        private final CopyIn delegate;
        private final Connection connection;
        
        private CopyInDelegate(final CopyIn delegate, final Connection connection) {
            this.delegate = delegate;
            this.connection = connection;
        }
        
        @Override
        public long endCopy() throws SQLException {
            try {
                final long issueCount = this.delegate.endCopy();
                this.connection.commit();
                return issueCount;
            }
            finally {
                this.connection.close();
                PostgresConnectionManager.this.openConnections.remove(this.connection);
            }
        }
        
        @Override
        public void writeToCopy(final byte[] buf, final int off, final int siz) throws SQLException {
            this.delegate.writeToCopy(buf, off, siz);
        }
        
        @Override
        public void writeToCopy(final ByteStreamWriter buff) throws SQLException {
            this.delegate.writeToCopy(buff);
        }
        
        @Override
        public void flushCopy() throws SQLException {
            this.delegate.flushCopy();
        }
        
        @Override
        public int getFieldCount() {
            return this.delegate.getFieldCount();
        }
        
        @Override
        public int getFormat() {
            return this.delegate.getFormat();
        }
        
        @Override
        public int getFieldFormat(final int field) {
            return this.delegate.getFieldFormat(field);
        }
        
        @Override
        public boolean isActive() {
            return this.delegate.isActive();
        }
        
        @Override
        public void cancelCopy() throws SQLException {
            throw new UnsupportedOperationException("Not supported at the moment");
        }
        
        @Override
        public long getHandledRowCount() {
            return this.delegate.getHandledRowCount();
        }
    }
}
