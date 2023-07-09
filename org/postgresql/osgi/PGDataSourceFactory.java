// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.osgi;

import org.postgresql.xa.PGXADataSource;
import javax.sql.XADataSource;
import org.postgresql.jdbc2.optional.ConnectionPool;
import javax.sql.ConnectionPoolDataSource;
import org.postgresql.jdbc2.optional.SimpleDataSource;
import org.postgresql.jdbc2.optional.PoolingDataSource;
import javax.sql.DataSource;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import org.postgresql.util.internal.Nullness;
import java.util.Properties;
import org.postgresql.ds.common.BaseDataSource;
import org.osgi.service.jdbc.DataSourceFactory;

public class PGDataSourceFactory implements DataSourceFactory
{
    private void configureBaseDataSource(final BaseDataSource ds, final Properties props) throws SQLException {
        if (props.containsKey("url")) {
            ds.setUrl(Nullness.castNonNull(props.getProperty("url")));
        }
        if (props.containsKey("serverName")) {
            ds.setServerName(Nullness.castNonNull(props.getProperty("serverName")));
        }
        if (props.containsKey("portNumber")) {
            ds.setPortNumber(Integer.parseInt(Nullness.castNonNull(props.getProperty("portNumber"))));
        }
        if (props.containsKey("databaseName")) {
            ds.setDatabaseName(props.getProperty("databaseName"));
        }
        if (props.containsKey("user")) {
            ds.setUser(props.getProperty("user"));
        }
        if (props.containsKey("password")) {
            ds.setPassword(props.getProperty("password"));
        }
        for (final Map.Entry<Object, Object> entry : props.entrySet()) {
            ds.setProperty(entry.getKey(), entry.getValue());
        }
    }
    
    public Driver createDriver(final Properties props) throws SQLException {
        if (props != null && !props.isEmpty()) {
            throw new PSQLException(GT.tr("Unsupported properties: {0}", props.stringPropertyNames()), PSQLState.INVALID_PARAMETER_VALUE);
        }
        return new org.postgresql.Driver();
    }
    
    private DataSource createPoolingDataSource(final Properties props) throws SQLException {
        final PoolingDataSource dataSource = new PoolingDataSource();
        if (props.containsKey("initialPoolSize")) {
            final String initialPoolSize = Nullness.castNonNull(props.getProperty("initialPoolSize"));
            dataSource.setInitialConnections(Integer.parseInt(initialPoolSize));
        }
        if (props.containsKey("maxPoolSize")) {
            final String maxPoolSize = Nullness.castNonNull(props.getProperty("maxPoolSize"));
            dataSource.setMaxConnections(Integer.parseInt(maxPoolSize));
        }
        if (props.containsKey("dataSourceName")) {
            dataSource.setDataSourceName(Nullness.castNonNull(props.getProperty("dataSourceName")));
        }
        this.configureBaseDataSource(dataSource, props);
        return dataSource;
    }
    
    private DataSource createSimpleDataSource(final Properties props) throws SQLException {
        final SimpleDataSource dataSource = new SimpleDataSource();
        this.configureBaseDataSource(dataSource, props);
        return dataSource;
    }
    
    public DataSource createDataSource(Properties props) throws SQLException {
        props = new SingleUseProperties(props);
        if (props.containsKey("initialPoolSize") || props.containsKey("minPoolSize") || props.containsKey("maxPoolSize") || props.containsKey("maxIdleTime") || props.containsKey("maxStatements")) {
            return this.createPoolingDataSource(props);
        }
        return this.createSimpleDataSource(props);
    }
    
    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties props) throws SQLException {
        props = new SingleUseProperties(props);
        final ConnectionPool dataSource = new ConnectionPool();
        this.configureBaseDataSource(dataSource, props);
        return dataSource;
    }
    
    public XADataSource createXADataSource(Properties props) throws SQLException {
        props = new SingleUseProperties(props);
        final PGXADataSource dataSource = new PGXADataSource();
        this.configureBaseDataSource(dataSource, props);
        return dataSource;
    }
    
    private static class SingleUseProperties extends Properties
    {
        private static final long serialVersionUID = 1L;
        
        SingleUseProperties(final Properties initialProperties) {
            if (initialProperties != null) {
                this.putAll(initialProperties);
            }
        }
        
        @Override
        public String getProperty(final String key) {
            final String value = super.getProperty(key);
            this.remove(key);
            return value;
        }
    }
}
