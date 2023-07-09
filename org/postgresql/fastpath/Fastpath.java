// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.fastpath;

import org.postgresql.util.internal.Nullness;
import java.sql.ResultSet;
import java.util.logging.Level;
import org.postgresql.core.ParameterList;
import java.sql.SQLException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import org.postgresql.util.ByteConverter;
import java.util.HashMap;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.QueryExecutor;
import java.util.Map;

@Deprecated
public class Fastpath
{
    private static final long NUM_OIDS = 4294967296L;
    private final Map<String, Integer> func;
    private final QueryExecutor executor;
    private final BaseConnection connection;
    
    public Fastpath(final BaseConnection conn) {
        this.func = new HashMap<String, Integer>();
        this.connection = conn;
        this.executor = conn.getQueryExecutor();
    }
    
    @Deprecated
    public Object fastpath(final int fnId, final boolean resultType, final FastpathArg[] args) throws SQLException {
        final byte[] returnValue = this.fastpath(fnId, args);
        if (!resultType || returnValue == null) {
            return returnValue;
        }
        if (returnValue.length == 4) {
            return ByteConverter.int4(returnValue, 0);
        }
        if (returnValue.length == 8) {
            return ByteConverter.int8(returnValue, 0);
        }
        throw new PSQLException(GT.tr("Fastpath call {0} - No result was returned and we expected a numeric.", fnId), PSQLState.NO_DATA);
    }
    
    public byte[] fastpath(final int fnId, final FastpathArg[] args) throws SQLException {
        final ParameterList params = this.executor.createFastpathParameters(args.length);
        for (int i = 0; i < args.length; ++i) {
            args[i].populateParameter(params, i + 1);
        }
        return this.executor.fastpathCall(fnId, params, this.connection.getAutoCommit());
    }
    
    @Deprecated
    public Object fastpath(final String name, final boolean resulttype, final FastpathArg[] args) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "Fastpath: calling {0}", name);
        return this.fastpath(this.getID(name), resulttype, args);
    }
    
    public byte[] fastpath(final String name, final FastpathArg[] args) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "Fastpath: calling {0}", name);
        return this.fastpath(this.getID(name), args);
    }
    
    public int getInteger(final String name, final FastpathArg[] args) throws SQLException {
        final byte[] returnValue = this.fastpath(name, args);
        if (returnValue == null) {
            throw new PSQLException(GT.tr("Fastpath call {0} - No result was returned and we expected an integer.", name), PSQLState.NO_DATA);
        }
        if (returnValue.length == 4) {
            return ByteConverter.int4(returnValue, 0);
        }
        throw new PSQLException(GT.tr("Fastpath call {0} - No result was returned or wrong size while expecting an integer.", name), PSQLState.NO_DATA);
    }
    
    public long getLong(final String name, final FastpathArg[] args) throws SQLException {
        final byte[] returnValue = this.fastpath(name, args);
        if (returnValue == null) {
            throw new PSQLException(GT.tr("Fastpath call {0} - No result was returned and we expected a long.", name), PSQLState.NO_DATA);
        }
        if (returnValue.length == 8) {
            return ByteConverter.int8(returnValue, 0);
        }
        throw new PSQLException(GT.tr("Fastpath call {0} - No result was returned or wrong size while expecting a long.", name), PSQLState.NO_DATA);
    }
    
    public long getOID(final String name, final FastpathArg[] args) throws SQLException {
        long oid = this.getInteger(name, args);
        if (oid < 0L) {
            oid += 4294967296L;
        }
        return oid;
    }
    
    public byte[] getData(final String name, final FastpathArg[] args) throws SQLException {
        return this.fastpath(name, args);
    }
    
    public void addFunction(final String name, final int fnid) {
        this.func.put(name, fnid);
    }
    
    public void addFunctions(final ResultSet rs) throws SQLException {
        while (rs.next()) {
            this.func.put(Nullness.castNonNull(rs.getString(1)), rs.getInt(2));
        }
    }
    
    public int getID(final String name) throws SQLException {
        final Integer id = this.func.get(name);
        if (id == null) {
            throw new PSQLException(GT.tr("The fastpath function {0} is unknown.", name), PSQLState.UNEXPECTED_ERROR);
        }
        return id;
    }
    
    public static FastpathArg createOIDArg(long oid) {
        if (oid > 2147483647L) {
            oid -= 4294967296L;
        }
        return new FastpathArg((int)oid);
    }
}
