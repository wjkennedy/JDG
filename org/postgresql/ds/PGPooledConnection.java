// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.ds;

import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import org.postgresql.PGStatement;
import java.sql.Statement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.sql.StatementEventListener;
import javax.sql.ConnectionEvent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import org.postgresql.PGConnection;
import org.postgresql.util.internal.Nullness;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import java.sql.SQLException;
import java.util.LinkedList;
import java.sql.Connection;
import javax.sql.ConnectionEventListener;
import java.util.List;
import javax.sql.PooledConnection;

public class PGPooledConnection implements PooledConnection
{
    private final List<ConnectionEventListener> listeners;
    private Connection con;
    private ConnectionHandler last;
    private final boolean autoCommit;
    private final boolean isXA;
    private static final String[] fatalClasses;
    
    public PGPooledConnection(final Connection con, final boolean autoCommit, final boolean isXA) {
        this.listeners = new LinkedList<ConnectionEventListener>();
        this.con = con;
        this.autoCommit = autoCommit;
        this.isXA = isXA;
    }
    
    public PGPooledConnection(final Connection con, final boolean autoCommit) {
        this(con, autoCommit, false);
    }
    
    @Override
    public void addConnectionEventListener(final ConnectionEventListener connectionEventListener) {
        this.listeners.add(connectionEventListener);
    }
    
    @Override
    public void removeConnectionEventListener(final ConnectionEventListener connectionEventListener) {
        this.listeners.remove(connectionEventListener);
    }
    
    @Override
    public void close() throws SQLException {
        if (this.last != null) {
            this.last.close();
            if (this.con != null && !this.con.isClosed() && !this.con.getAutoCommit()) {
                try {
                    this.con.rollback();
                }
                catch (final SQLException ex) {}
            }
        }
        if (this.con == null) {
            return;
        }
        try {
            this.con.close();
        }
        finally {
            this.con = null;
        }
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        if (this.con == null) {
            final PSQLException sqlException = new PSQLException(GT.tr("This PooledConnection has already been closed.", new Object[0]), PSQLState.CONNECTION_DOES_NOT_EXIST);
            this.fireConnectionFatalError(sqlException);
            throw sqlException;
        }
        try {
            if (this.last != null) {
                this.last.close();
                if (this.con != null) {
                    if (!this.con.getAutoCommit()) {
                        try {
                            this.con.rollback();
                        }
                        catch (final SQLException ex) {}
                    }
                    this.con.clearWarnings();
                }
            }
            if (!this.isXA && this.con != null) {
                this.con.setAutoCommit(this.autoCommit);
            }
        }
        catch (final SQLException sqlException2) {
            this.fireConnectionFatalError(sqlException2);
            throw (SQLException)sqlException2.fillInStackTrace();
        }
        final ConnectionHandler handler = new ConnectionHandler(Nullness.castNonNull(this.con));
        this.last = handler;
        final Connection proxyCon = (Connection)Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { Connection.class, PGConnection.class }, handler);
        handler.setProxy(proxyCon);
        return proxyCon;
    }
    
    void fireConnectionClosed() {
        ConnectionEvent evt = null;
        final ConnectionEventListener[] array;
        final ConnectionEventListener[] local = array = this.listeners.toArray(new ConnectionEventListener[0]);
        for (final ConnectionEventListener listener : array) {
            if (evt == null) {
                evt = this.createConnectionEvent(null);
            }
            listener.connectionClosed(evt);
        }
    }
    
    void fireConnectionFatalError(final SQLException e) {
        ConnectionEvent evt = null;
        final ConnectionEventListener[] array;
        final ConnectionEventListener[] local = array = this.listeners.toArray(new ConnectionEventListener[0]);
        for (final ConnectionEventListener listener : array) {
            if (evt == null) {
                evt = this.createConnectionEvent(e);
            }
            listener.connectionErrorOccurred(evt);
        }
    }
    
    protected ConnectionEvent createConnectionEvent(final SQLException e) {
        return (e == null) ? new ConnectionEvent(this) : new ConnectionEvent(this, e);
    }
    
    private static boolean isFatalState(final String state) {
        if (state == null) {
            return true;
        }
        if (state.length() < 2) {
            return true;
        }
        for (final String fatalClass : PGPooledConnection.fatalClasses) {
            if (state.startsWith(fatalClass)) {
                return true;
            }
        }
        return false;
    }
    
    private void fireConnectionError(final SQLException e) {
        if (!isFatalState(e.getSQLState())) {
            return;
        }
        this.fireConnectionFatalError(e);
    }
    
    @Override
    public void removeStatementEventListener(final StatementEventListener listener) {
    }
    
    @Override
    public void addStatementEventListener(final StatementEventListener listener) {
    }
    
    static {
        fatalClasses = new String[] { "08", "53", "57P01", "57P02", "57P03", "58", "60", "99", "F0", "XX" };
    }
    
    private class ConnectionHandler implements InvocationHandler
    {
        private Connection con;
        private Connection proxy;
        private boolean automatic;
        
        ConnectionHandler(final Connection con) {
            this.automatic = false;
            this.con = con;
        }
        
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            final String methodName = method.getName();
            if (method.getDeclaringClass() == Object.class) {
                if (methodName.equals("toString")) {
                    return "Pooled connection wrapping physical connection " + this.con;
                }
                if (methodName.equals("equals")) {
                    return proxy == args[0];
                }
                if (methodName.equals("hashCode")) {
                    return System.identityHashCode(proxy);
                }
                try {
                    return method.invoke(this.con, args);
                }
                catch (final InvocationTargetException e) {
                    throw e.getTargetException();
                }
            }
            if (methodName.equals("isClosed")) {
                return this.con == null || this.con.isClosed();
            }
            if (methodName.equals("close")) {
                if (this.con == null) {
                    return null;
                }
                SQLException ex = null;
                if (!this.con.isClosed()) {
                    if (!PGPooledConnection.this.isXA && !this.con.getAutoCommit()) {
                        try {
                            this.con.rollback();
                        }
                        catch (final SQLException e2) {
                            ex = e2;
                        }
                    }
                    this.con.clearWarnings();
                }
                this.con = null;
                this.proxy = null;
                PGPooledConnection.this.last = null;
                PGPooledConnection.this.fireConnectionClosed();
                if (ex != null) {
                    throw ex;
                }
                return null;
            }
            else {
                if (this.con == null || this.con.isClosed()) {
                    throw new PSQLException(this.automatic ? GT.tr("Connection has been closed automatically because a new connection was opened for the same PooledConnection or the PooledConnection has been closed.", new Object[0]) : GT.tr("Connection has been closed.", new Object[0]), PSQLState.CONNECTION_DOES_NOT_EXIST);
                }
                try {
                    if (methodName.equals("createStatement")) {
                        final Statement st = Nullness.castNonNull(method.invoke(this.con, args));
                        return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { Statement.class, PGStatement.class }, new StatementHandler(this, st));
                    }
                    if (methodName.equals("prepareCall")) {
                        final Statement st = Nullness.castNonNull(method.invoke(this.con, args));
                        return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { CallableStatement.class, PGStatement.class }, new StatementHandler(this, st));
                    }
                    if (methodName.equals("prepareStatement")) {
                        final Statement st = Nullness.castNonNull(method.invoke(this.con, args));
                        return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { PreparedStatement.class, PGStatement.class }, new StatementHandler(this, st));
                    }
                    return method.invoke(this.con, args);
                }
                catch (final InvocationTargetException ite) {
                    final Throwable te = ite.getTargetException();
                    if (te instanceof SQLException) {
                        PGPooledConnection.this.fireConnectionError((SQLException)te);
                    }
                    throw te;
                }
            }
        }
        
        Connection getProxy() {
            return Nullness.castNonNull(this.proxy);
        }
        
        void setProxy(final Connection proxy) {
            this.proxy = proxy;
        }
        
        public void close() {
            if (this.con != null) {
                this.automatic = true;
            }
            this.con = null;
            this.proxy = null;
        }
        
        public boolean isClosed() {
            return this.con == null;
        }
    }
    
    private class StatementHandler implements InvocationHandler
    {
        private ConnectionHandler con;
        private Statement st;
        
        StatementHandler(final ConnectionHandler con, final Statement st) {
            this.con = con;
            this.st = st;
        }
        
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            final String methodName = method.getName();
            if (method.getDeclaringClass() == Object.class) {
                if (methodName.equals("toString")) {
                    return "Pooled statement wrapping physical statement " + this.st;
                }
                if (methodName.equals("hashCode")) {
                    return System.identityHashCode(proxy);
                }
                if (methodName.equals("equals")) {
                    return proxy == args[0];
                }
                return method.invoke(this.st, args);
            }
            else {
                if (methodName.equals("isClosed")) {
                    return this.st == null || this.st.isClosed();
                }
                if (methodName.equals("close")) {
                    if (this.st == null || this.st.isClosed()) {
                        return null;
                    }
                    this.con = null;
                    final Statement oldSt = this.st;
                    this.st = null;
                    oldSt.close();
                    return null;
                }
                else {
                    if (this.st == null || this.st.isClosed()) {
                        throw new PSQLException(GT.tr("Statement has been closed.", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
                    }
                    if (methodName.equals("getConnection")) {
                        return Nullness.castNonNull(this.con).getProxy();
                    }
                    try {
                        return method.invoke(this.st, args);
                    }
                    catch (final InvocationTargetException ite) {
                        final Throwable te = ite.getTargetException();
                        if (te instanceof SQLException) {
                            PGPooledConnection.this.fireConnectionError((SQLException)te);
                        }
                        throw te;
                    }
                }
            }
        }
    }
}
