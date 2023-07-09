// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.largeobject;

import org.postgresql.fastpath.FastpathArg;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import org.postgresql.core.BaseConnection;
import org.postgresql.fastpath.Fastpath;

public class LargeObjectManager
{
    private Fastpath fp;
    private BaseConnection conn;
    public static final int WRITE = 131072;
    public static final int READ = 262144;
    public static final int READWRITE = 393216;
    
    public LargeObjectManager(final BaseConnection conn) throws SQLException {
        this.conn = conn;
        this.fp = conn.getFastpathAPI();
        String sql;
        if (conn.getMetaData().supportsSchemasInTableDefinitions()) {
            sql = "SELECT p.proname,p.oid  FROM pg_catalog.pg_proc p, pg_catalog.pg_namespace n  WHERE p.pronamespace=n.oid AND n.nspname='pg_catalog' AND (";
        }
        else {
            sql = "SELECT proname,oid FROM pg_proc WHERE ";
        }
        sql += " proname = 'lo_open' or proname = 'lo_close' or proname = 'lo_creat' or proname = 'lo_unlink' or proname = 'lo_lseek' or proname = 'lo_lseek64' or proname = 'lo_tell' or proname = 'lo_tell64' or proname = 'loread' or proname = 'lowrite' or proname = 'lo_truncate' or proname = 'lo_truncate64'";
        if (conn.getMetaData().supportsSchemasInTableDefinitions()) {
            sql += ")";
        }
        final Statement stmt = conn.createStatement();
        final ResultSet res = stmt.executeQuery(sql);
        this.fp.addFunctions(res);
        res.close();
        stmt.close();
        conn.getLogger().log(Level.FINE, "Large Object initialised");
    }
    
    @Deprecated
    public LargeObject open(final int oid) throws SQLException {
        return this.open((long)oid, false);
    }
    
    public LargeObject open(final int oid, final boolean commitOnClose) throws SQLException {
        return this.open((long)oid, commitOnClose);
    }
    
    public LargeObject open(final long oid) throws SQLException {
        return this.open(oid, 393216, false);
    }
    
    public LargeObject open(final long oid, final boolean commitOnClose) throws SQLException {
        return this.open(oid, 393216, commitOnClose);
    }
    
    @Deprecated
    public LargeObject open(final int oid, final int mode) throws SQLException {
        return this.open((long)oid, mode, false);
    }
    
    public LargeObject open(final int oid, final int mode, final boolean commitOnClose) throws SQLException {
        return this.open((long)oid, mode, commitOnClose);
    }
    
    public LargeObject open(final long oid, final int mode) throws SQLException {
        return this.open(oid, mode, false);
    }
    
    public LargeObject open(final long oid, final int mode, final boolean commitOnClose) throws SQLException {
        if (this.conn.getAutoCommit()) {
            throw new PSQLException(GT.tr("Large Objects may not be used in auto-commit mode.", new Object[0]), PSQLState.NO_ACTIVE_SQL_TRANSACTION);
        }
        return new LargeObject(this.fp, oid, mode, this.conn, commitOnClose);
    }
    
    @Deprecated
    public int create() throws SQLException {
        return this.create(393216);
    }
    
    public long createLO() throws SQLException {
        return this.createLO(393216);
    }
    
    public long createLO(final int mode) throws SQLException {
        if (this.conn.getAutoCommit()) {
            throw new PSQLException(GT.tr("Large Objects may not be used in auto-commit mode.", new Object[0]), PSQLState.NO_ACTIVE_SQL_TRANSACTION);
        }
        final FastpathArg[] args = { new FastpathArg(mode) };
        return this.fp.getOID("lo_creat", args);
    }
    
    @Deprecated
    public int create(final int mode) throws SQLException {
        final long oid = this.createLO(mode);
        return (int)oid;
    }
    
    public void delete(final long oid) throws SQLException {
        final FastpathArg[] args = { Fastpath.createOIDArg(oid) };
        this.fp.fastpath("lo_unlink", args);
    }
    
    @Deprecated
    public void unlink(final int oid) throws SQLException {
        this.delete((long)oid);
    }
    
    public void unlink(final long oid) throws SQLException {
        this.delete(oid);
    }
    
    @Deprecated
    public void delete(final int oid) throws SQLException {
        this.delete((long)oid);
    }
}
