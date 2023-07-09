// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.xa;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.postgresql.util.PSQLException;
import org.postgresql.core.TransactionState;
import org.postgresql.util.PSQLState;
import java.sql.ResultSet;
import org.postgresql.util.internal.Nullness;
import java.util.LinkedList;
import java.sql.Statement;
import javax.transaction.xa.XAException;
import org.postgresql.util.GT;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import org.postgresql.PGConnection;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.logging.Level;
import javax.transaction.xa.Xid;
import org.postgresql.core.BaseConnection;
import java.util.logging.Logger;
import javax.transaction.xa.XAResource;
import javax.sql.XAConnection;
import org.postgresql.ds.PGPooledConnection;

public class PGXAConnection extends PGPooledConnection implements XAConnection, XAResource
{
    private static final Logger LOGGER;
    private final BaseConnection conn;
    private Xid currentXid;
    private State state;
    private Xid preparedXid;
    private boolean committedOrRolledBack;
    private boolean localAutoCommitMode;
    
    private void debug(final String s) {
        if (PGXAConnection.LOGGER.isLoggable(Level.FINEST)) {
            PGXAConnection.LOGGER.log(Level.FINEST, "XAResource {0}: {1}", new Object[] { Integer.toHexString(this.hashCode()), s });
        }
    }
    
    public PGXAConnection(final BaseConnection conn) throws SQLException {
        super(conn, true, true);
        this.localAutoCommitMode = true;
        this.conn = conn;
        this.state = State.IDLE;
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        final Connection conn = super.getConnection();
        if (this.state == State.IDLE) {
            conn.setAutoCommit(true);
        }
        final ConnectionHandler handler = new ConnectionHandler(conn);
        return (Connection)Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { Connection.class, PGConnection.class }, handler);
    }
    
    @Override
    public XAResource getXAResource() {
        return this;
    }
    
    @Override
    public void start(final Xid xid, final int flags) throws XAException {
        if (PGXAConnection.LOGGER.isLoggable(Level.FINEST)) {
            this.debug("starting transaction xid = " + xid);
        }
        if (flags != 0 && flags != 134217728 && flags != 2097152) {
            throw new PGXAException(GT.tr("Invalid flags {0}", flags), -5);
        }
        if (xid == null) {
            throw new PGXAException(GT.tr("xid must not be null", new Object[0]), -5);
        }
        if (this.state == State.ACTIVE) {
            throw new PGXAException(GT.tr("Connection is busy with another transaction", new Object[0]), -6);
        }
        if (flags == 134217728) {
            throw new PGXAException(GT.tr("suspend/resume not implemented", new Object[0]), -3);
        }
        if (flags == 2097152) {
            if (this.state != State.ENDED) {
                throw new PGXAException(GT.tr("Invalid protocol state requested. Attempted transaction interleaving is not supported. xid={0}, currentXid={1}, state={2}, flags={3}", xid, this.currentXid, this.state, flags), -3);
            }
            if (!xid.equals(this.currentXid)) {
                throw new PGXAException(GT.tr("Invalid protocol state requested. Attempted transaction interleaving is not supported. xid={0}, currentXid={1}, state={2}, flags={3}", xid, this.currentXid, this.state, flags), -3);
            }
        }
        else if (this.state == State.ENDED) {
            throw new PGXAException(GT.tr("Invalid protocol state requested. Attempted transaction interleaving is not supported. xid={0}, currentXid={1}, state={2}, flags={3}", xid, this.currentXid, this.state, flags), -3);
        }
        if (flags == 0) {
            try {
                this.localAutoCommitMode = this.conn.getAutoCommit();
                this.conn.setAutoCommit(false);
            }
            catch (final SQLException ex) {
                throw new PGXAException(GT.tr("Error disabling autocommit", new Object[0]), ex, -3);
            }
        }
        this.state = State.ACTIVE;
        this.currentXid = xid;
        this.preparedXid = null;
        this.committedOrRolledBack = false;
    }
    
    @Override
    public void end(final Xid xid, final int flags) throws XAException {
        if (PGXAConnection.LOGGER.isLoggable(Level.FINEST)) {
            this.debug("ending transaction xid = " + xid);
        }
        if (flags != 33554432 && flags != 536870912 && flags != 67108864) {
            throw new PGXAException(GT.tr("Invalid flags {0}", flags), -5);
        }
        if (xid == null) {
            throw new PGXAException(GT.tr("xid must not be null", new Object[0]), -5);
        }
        if (this.state != State.ACTIVE || !xid.equals(this.currentXid)) {
            throw new PGXAException(GT.tr("tried to call end without corresponding start call. state={0}, start xid={1}, currentXid={2}, preparedXid={3}", this.state, xid, this.currentXid, this.preparedXid), -6);
        }
        if (flags == 33554432) {
            throw new PGXAException(GT.tr("suspend/resume not implemented", new Object[0]), -3);
        }
        this.state = State.ENDED;
    }
    
    @Override
    public int prepare(final Xid xid) throws XAException {
        if (PGXAConnection.LOGGER.isLoggable(Level.FINEST)) {
            this.debug("preparing transaction xid = " + xid);
        }
        if (this.currentXid == null && this.preparedXid != null) {
            if (PGXAConnection.LOGGER.isLoggable(Level.FINEST)) {
                this.debug("Prepare xid " + xid + " but current connection is not attached to a transaction while it was prepared in past with prepared xid " + this.preparedXid);
            }
            throw new PGXAException(GT.tr("Preparing already prepared transaction, the prepared xid {0}, prepare xid={1}", this.preparedXid, xid), -6);
        }
        if (this.currentXid == null) {
            throw new PGXAException(GT.tr("Current connection does not have an associated xid. prepare xid={0}", xid), -4);
        }
        if (!this.currentXid.equals(xid)) {
            if (PGXAConnection.LOGGER.isLoggable(Level.FINEST)) {
                this.debug("Error to prepare xid " + xid + ", the current connection already bound with xid " + this.currentXid);
            }
            throw new PGXAException(GT.tr("Not implemented: Prepare must be issued using the same connection that started the transaction. currentXid={0}, prepare xid={1}", this.currentXid, xid), -3);
        }
        if (this.state != State.ENDED) {
            throw new PGXAException(GT.tr("Prepare called before end. prepare xid={0}, state={1}", xid), -5);
        }
        this.state = State.IDLE;
        this.preparedXid = this.currentXid;
        this.currentXid = null;
        try {
            final String s = RecoveredXid.xidToString(xid);
            final Statement stmt = this.conn.createStatement();
            try {
                stmt.executeUpdate("PREPARE TRANSACTION '" + s + "'");
            }
            finally {
                stmt.close();
            }
            this.conn.setAutoCommit(this.localAutoCommitMode);
            return 0;
        }
        catch (final SQLException ex) {
            throw new PGXAException(GT.tr("Error preparing transaction. prepare xid={0}", xid), ex, this.mapSQLStateToXAErrorCode(ex));
        }
    }
    
    @Override
    public Xid[] recover(final int flag) throws XAException {
        if (flag != 16777216 && flag != 8388608 && flag != 0 && flag != 25165824) {
            throw new PGXAException(GT.tr("Invalid flags {0}", flag), -5);
        }
        if ((flag & 0x1000000) == 0x0) {
            return new Xid[0];
        }
        try {
            final Statement stmt = this.conn.createStatement();
            try {
                final ResultSet rs = stmt.executeQuery("SELECT gid FROM pg_prepared_xacts where database = current_database()");
                final LinkedList<Xid> l = new LinkedList<Xid>();
                while (rs.next()) {
                    final Xid recoveredXid = RecoveredXid.stringToXid(Nullness.castNonNull(rs.getString(1)));
                    if (recoveredXid != null) {
                        l.add(recoveredXid);
                    }
                }
                rs.close();
                return l.toArray(new Xid[0]);
            }
            finally {
                stmt.close();
            }
        }
        catch (final SQLException ex) {
            throw new PGXAException(GT.tr("Error during recover", new Object[0]), ex, -3);
        }
    }
    
    @Override
    public void rollback(final Xid xid) throws XAException {
        if (PGXAConnection.LOGGER.isLoggable(Level.FINEST)) {
            this.debug("rolling back xid = " + xid);
        }
        try {
            if (this.currentXid != null && this.currentXid.equals(xid)) {
                this.state = State.IDLE;
                this.currentXid = null;
                this.conn.rollback();
                this.conn.setAutoCommit(this.localAutoCommitMode);
            }
            else {
                final String s = RecoveredXid.xidToString(xid);
                this.conn.setAutoCommit(true);
                final Statement stmt = this.conn.createStatement();
                try {
                    stmt.executeUpdate("ROLLBACK PREPARED '" + s + "'");
                }
                finally {
                    stmt.close();
                }
            }
            this.committedOrRolledBack = true;
        }
        catch (final SQLException ex) {
            int errorCode = -3;
            if (PSQLState.UNDEFINED_OBJECT.getState().equals(ex.getSQLState()) && (this.committedOrRolledBack || !xid.equals(this.preparedXid))) {
                if (PGXAConnection.LOGGER.isLoggable(Level.FINEST)) {
                    this.debug("rolling back xid " + xid + " while the connection prepared xid is " + this.preparedXid + (this.committedOrRolledBack ? ", but the connection was already committed/rolled-back" : ""));
                }
                errorCode = -4;
            }
            if (PSQLState.isConnectionError(ex.getSQLState())) {
                if (PGXAConnection.LOGGER.isLoggable(Level.FINEST)) {
                    this.debug("rollback connection failure (sql error code " + ex.getSQLState() + "), reconnection could be expected");
                }
                errorCode = -7;
            }
            throw new PGXAException(GT.tr("Error rolling back prepared transaction. rollback xid={0}, preparedXid={1}, currentXid={2}", xid, this.preparedXid), ex, errorCode);
        }
    }
    
    @Override
    public void commit(final Xid xid, final boolean onePhase) throws XAException {
        if (PGXAConnection.LOGGER.isLoggable(Level.FINEST)) {
            this.debug("committing xid = " + xid + (onePhase ? " (one phase) " : " (two phase)"));
        }
        if (xid == null) {
            throw new PGXAException(GT.tr("xid must not be null", new Object[0]), -5);
        }
        if (onePhase) {
            this.commitOnePhase(xid);
        }
        else {
            this.commitPrepared(xid);
        }
    }
    
    private void commitOnePhase(final Xid xid) throws XAException {
        try {
            if (xid.equals(this.preparedXid)) {
                throw new PGXAException(GT.tr("One-phase commit called for xid {0} but connection was prepared with xid {1}", xid, this.preparedXid), -6);
            }
            if (this.currentXid == null && !this.committedOrRolledBack) {
                throw new PGXAException(GT.tr("Not implemented: one-phase commit must be issued using the same connection that was used to start it", xid), -3);
            }
            if (!xid.equals(this.currentXid) || this.committedOrRolledBack) {
                throw new PGXAException(GT.tr("One-phase commit with unknown xid. commit xid={0}, currentXid={1}", xid, this.currentXid), -4);
            }
            if (this.state != State.ENDED) {
                throw new PGXAException(GT.tr("commit called before end. commit xid={0}, state={1}", xid, this.state), -6);
            }
            this.state = State.IDLE;
            this.currentXid = null;
            this.committedOrRolledBack = true;
            this.conn.commit();
            this.conn.setAutoCommit(this.localAutoCommitMode);
        }
        catch (final SQLException ex) {
            throw new PGXAException(GT.tr("Error during one-phase commit. commit xid={0}", xid), ex, this.mapSQLStateToXAErrorCode(ex));
        }
    }
    
    private void commitPrepared(final Xid xid) throws XAException {
        try {
            if (this.state != State.IDLE || this.conn.getTransactionState() != TransactionState.IDLE) {
                throw new PGXAException(GT.tr("Not implemented: 2nd phase commit must be issued using an idle connection. commit xid={0}, currentXid={1}, state={2}, transactionState={3}", xid, this.currentXid, this.state, this.conn.getTransactionState()), -3);
            }
            final String s = RecoveredXid.xidToString(xid);
            this.localAutoCommitMode = this.conn.getAutoCommit();
            this.conn.setAutoCommit(true);
            final Statement stmt = this.conn.createStatement();
            try {
                stmt.executeUpdate("COMMIT PREPARED '" + s + "'");
            }
            finally {
                stmt.close();
                this.conn.setAutoCommit(this.localAutoCommitMode);
            }
            this.committedOrRolledBack = true;
        }
        catch (final SQLException ex) {
            int errorCode = -3;
            if (PSQLState.UNDEFINED_OBJECT.getState().equals(ex.getSQLState()) && (this.committedOrRolledBack || !xid.equals(this.preparedXid))) {
                if (PGXAConnection.LOGGER.isLoggable(Level.FINEST)) {
                    this.debug("committing xid " + xid + " while the connection prepared xid is " + this.preparedXid + (this.committedOrRolledBack ? ", but the connection was already committed/rolled-back" : ""));
                }
                errorCode = -4;
            }
            if (PSQLState.isConnectionError(ex.getSQLState())) {
                if (PGXAConnection.LOGGER.isLoggable(Level.FINEST)) {
                    this.debug("commit connection failure (sql error code " + ex.getSQLState() + "), reconnection could be expected");
                }
                errorCode = -7;
            }
            throw new PGXAException(GT.tr("Error committing prepared transaction. commit xid={0}, preparedXid={1}, currentXid={2}", xid, this.preparedXid, this.currentXid), ex, errorCode);
        }
    }
    
    @Override
    public boolean isSameRM(final XAResource xares) throws XAException {
        return xares == this;
    }
    
    @Override
    public void forget(final Xid xid) throws XAException {
        throw new PGXAException(GT.tr("Heuristic commit/rollback not supported. forget xid={0}", xid), -4);
    }
    
    @Override
    public int getTransactionTimeout() {
        return 0;
    }
    
    @Override
    public boolean setTransactionTimeout(final int seconds) {
        return false;
    }
    
    private int mapSQLStateToXAErrorCode(final SQLException sqlException) {
        if (this.isPostgreSQLIntegrityConstraintViolation(sqlException)) {
            return 103;
        }
        return -7;
    }
    
    private boolean isPostgreSQLIntegrityConstraintViolation(final SQLException sqlException) {
        if (!(sqlException instanceof PSQLException)) {
            return false;
        }
        final String sqlState = sqlException.getSQLState();
        return sqlState != null && sqlState.length() == 5 && sqlState.startsWith("23");
    }
    
    static {
        LOGGER = Logger.getLogger(PGXAConnection.class.getName());
    }
    
    private class ConnectionHandler implements InvocationHandler
    {
        private final Connection con;
        
        ConnectionHandler(final Connection con) {
            this.con = con;
        }
        
        @Override
        public Object invoke(final Object proxy, final Method method, Object[] args) throws Throwable {
            if (PGXAConnection.this.state != State.IDLE) {
                final String methodName = method.getName();
                if (methodName.equals("commit") || methodName.equals("rollback") || methodName.equals("setSavePoint") || (methodName.equals("setAutoCommit") && Nullness.castNonNull(args[0]))) {
                    throw new PSQLException(GT.tr("Transaction control methods setAutoCommit(true), commit, rollback and setSavePoint not allowed while an XA transaction is active.", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
                }
            }
            try {
                if (method.getName().equals("equals") && args.length == 1) {
                    final Object arg = args[0];
                    if (arg != null && Proxy.isProxyClass(arg.getClass())) {
                        final InvocationHandler h = Proxy.getInvocationHandler(arg);
                        if (h instanceof ConnectionHandler) {
                            args = new Object[] { ((ConnectionHandler)h).con };
                        }
                    }
                }
                return method.invoke(this.con, args);
            }
            catch (final InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }
    
    private enum State
    {
        IDLE, 
        ACTIVE, 
        ENDED;
    }
}
