// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.ds;

import java.util.concurrent.ConcurrentHashMap;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.StringRefAddr;
import javax.naming.Reference;
import org.postgresql.util.internal.Nullness;
import java.sql.Connection;
import java.sql.SQLException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import java.util.Stack;
import java.util.concurrent.ConcurrentMap;
import javax.sql.DataSource;
import org.postgresql.ds.common.BaseDataSource;

@Deprecated
public class PGPoolingDataSource extends BaseDataSource implements DataSource
{
    protected static ConcurrentMap<String, PGPoolingDataSource> dataSources;
    protected String dataSourceName;
    private int initialConnections;
    private int maxConnections;
    private boolean initialized;
    private final Stack<PooledConnection> available;
    private final Stack<PooledConnection> used;
    private boolean isClosed;
    private final Object lock;
    private PGConnectionPoolDataSource source;
    private final ConnectionEventListener connectionEventListener;
    
    public PGPoolingDataSource() {
        this.initialConnections = 0;
        this.maxConnections = 0;
        this.initialized = false;
        this.available = new Stack<PooledConnection>();
        this.used = new Stack<PooledConnection>();
        this.lock = new Object();
        this.connectionEventListener = new ConnectionEventListener() {
            @Override
            public void connectionClosed(final ConnectionEvent event) {
                ((PooledConnection)event.getSource()).removeConnectionEventListener(this);
                synchronized (PGPoolingDataSource.this.lock) {
                    if (PGPoolingDataSource.this.isClosed) {
                        return;
                    }
                    final boolean removed = PGPoolingDataSource.this.used.remove(event.getSource());
                    if (removed) {
                        PGPoolingDataSource.this.available.push(event.getSource());
                        PGPoolingDataSource.this.lock.notify();
                    }
                }
            }
            
            @Override
            public void connectionErrorOccurred(final ConnectionEvent event) {
                ((PooledConnection)event.getSource()).removeConnectionEventListener(this);
                synchronized (PGPoolingDataSource.this.lock) {
                    if (PGPoolingDataSource.this.isClosed) {
                        return;
                    }
                    PGPoolingDataSource.this.used.remove(event.getSource());
                    PGPoolingDataSource.this.lock.notify();
                }
            }
        };
    }
    
    public static PGPoolingDataSource getDataSource(final String name) {
        return PGPoolingDataSource.dataSources.get(name);
    }
    
    @Override
    public String getDescription() {
        return "Pooling DataSource '" + this.dataSourceName + " from " + "PostgreSQL JDBC Driver 42.2.25";
    }
    
    @Override
    public void setServerName(final String serverName) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
        }
        super.setServerName(serverName);
    }
    
    @Override
    public void setDatabaseName(final String databaseName) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
        }
        super.setDatabaseName(databaseName);
    }
    
    @Override
    public void setUser(final String user) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
        }
        super.setUser(user);
    }
    
    @Override
    public void setPassword(final String password) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
        }
        super.setPassword(password);
    }
    
    @Override
    public void setPortNumber(final int portNumber) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
        }
        super.setPortNumber(portNumber);
    }
    
    public int getInitialConnections() {
        return this.initialConnections;
    }
    
    public void setInitialConnections(final int initialConnections) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
        }
        this.initialConnections = initialConnections;
    }
    
    public int getMaxConnections() {
        return this.maxConnections;
    }
    
    public void setMaxConnections(final int maxConnections) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
        }
        this.maxConnections = maxConnections;
    }
    
    public String getDataSourceName() {
        return this.dataSourceName;
    }
    
    public void setDataSourceName(final String dataSourceName) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
        }
        if (this.dataSourceName != null && dataSourceName != null && dataSourceName.equals(this.dataSourceName)) {
            return;
        }
        final PGPoolingDataSource previous = PGPoolingDataSource.dataSources.putIfAbsent(dataSourceName, this);
        if (previous != null) {
            throw new IllegalArgumentException("DataSource with name '" + dataSourceName + "' already exists!");
        }
        if (this.dataSourceName != null) {
            PGPoolingDataSource.dataSources.remove(this.dataSourceName);
        }
        this.dataSourceName = dataSourceName;
    }
    
    public void initialize() throws SQLException {
        synchronized (this.lock) {
            final PGConnectionPoolDataSource source = this.createConnectionPool();
            this.source = source;
            try {
                source.initializeFrom(this);
            }
            catch (final Exception e) {
                throw new PSQLException(GT.tr("Failed to setup DataSource.", new Object[0]), PSQLState.UNEXPECTED_ERROR, e);
            }
            while (this.available.size() < this.initialConnections) {
                this.available.push(source.getPooledConnection());
            }
            this.initialized = true;
        }
    }
    
    protected boolean isInitialized() {
        return this.initialized;
    }
    
    protected PGConnectionPoolDataSource createConnectionPool() {
        return new PGConnectionPoolDataSource();
    }
    
    @Override
    public Connection getConnection(final String user, final String password) throws SQLException {
        if (user == null || (user.equals(this.getUser()) && ((password == null && this.getPassword() == null) || (password != null && password.equals(this.getPassword()))))) {
            return this.getConnection();
        }
        if (!this.initialized) {
            this.initialize();
        }
        return super.getConnection(user, password);
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        if (!this.initialized) {
            this.initialize();
        }
        return this.getPooledConnection();
    }
    
    public void close() {
        synchronized (this.lock) {
            this.isClosed = true;
            while (!this.available.isEmpty()) {
                final PooledConnection pci = this.available.pop();
                try {
                    pci.close();
                }
                catch (final SQLException ex) {}
            }
            while (!this.used.isEmpty()) {
                final PooledConnection pci = this.used.pop();
                pci.removeConnectionEventListener(this.connectionEventListener);
                try {
                    pci.close();
                }
                catch (final SQLException ex2) {}
            }
        }
        this.removeStoredDataSource();
    }
    
    protected void removeStoredDataSource() {
        PGPoolingDataSource.dataSources.remove(Nullness.castNonNull(this.dataSourceName));
    }
    
    protected void addDataSource(final String dataSourceName) {
        PGPoolingDataSource.dataSources.put(dataSourceName, this);
    }
    
    private Connection getPooledConnection() throws SQLException {
        PooledConnection pc = null;
        Label_0145: {
            synchronized (this.lock) {
                if (this.isClosed) {
                    throw new PSQLException(GT.tr("DataSource has been closed.", new Object[0]), PSQLState.CONNECTION_DOES_NOT_EXIST);
                }
                while (this.available.isEmpty()) {
                    if (this.maxConnections == 0 || this.used.size() < this.maxConnections) {
                        pc = Nullness.castNonNull(this.source).getPooledConnection();
                        this.used.push(pc);
                        break Label_0145;
                    }
                    try {
                        this.lock.wait(1000L);
                    }
                    catch (final InterruptedException ex) {}
                }
                pc = this.available.pop();
                this.used.push(pc);
            }
        }
        pc.addConnectionEventListener(this.connectionEventListener);
        return pc.getConnection();
    }
    
    @Override
    public Reference getReference() throws NamingException {
        final Reference ref = super.getReference();
        ref.add(new StringRefAddr("dataSourceName", this.dataSourceName));
        if (this.initialConnections > 0) {
            ref.add(new StringRefAddr("initialConnections", Integer.toString(this.initialConnections)));
        }
        if (this.maxConnections > 0) {
            ref.add(new StringRefAddr("maxConnections", Integer.toString(this.maxConnections)));
        }
        return ref;
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(this.getClass());
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        if (iface.isAssignableFrom(this.getClass())) {
            return iface.cast(this);
        }
        throw new SQLException("Cannot unwrap to " + iface.getName());
    }
    
    static {
        PGPoolingDataSource.dataSources = new ConcurrentHashMap<String, PGPoolingDataSource>();
    }
}
