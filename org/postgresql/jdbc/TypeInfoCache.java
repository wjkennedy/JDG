// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

import org.postgresql.core.Version;
import org.postgresql.core.ServerVersion;
import org.postgresql.util.internal.Nullness;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import org.postgresql.core.BaseStatement;
import java.util.logging.Level;
import java.sql.ResultSet;
import java.util.Iterator;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.sql.PreparedStatement;
import org.postgresql.core.BaseConnection;
import org.postgresql.util.PGobject;
import java.util.Map;
import java.util.logging.Logger;
import org.postgresql.core.TypeInfo;

public class TypeInfoCache implements TypeInfo
{
    private static final Logger LOGGER;
    private Map<String, Integer> pgNameToSQLType;
    private Map<Integer, Integer> oidToSQLType;
    private Map<String, String> pgNameToJavaClass;
    private Map<Integer, String> oidToPgName;
    private Map<String, Integer> pgNameToOid;
    private Map<String, Class<? extends PGobject>> pgNameToPgObject;
    private Map<Integer, Integer> pgArrayToPgType;
    private Map<Integer, Character> arrayOidToDelimiter;
    private final BaseConnection conn;
    private final int unknownLength;
    private PreparedStatement getOidStatementSimple;
    private PreparedStatement getOidStatementComplexNonArray;
    private PreparedStatement getOidStatementComplexArray;
    private PreparedStatement getNameStatement;
    private PreparedStatement getArrayElementOidStatement;
    private PreparedStatement getArrayDelimiterStatement;
    private PreparedStatement getTypeInfoStatement;
    private PreparedStatement getAllTypeInfoStatement;
    private static final Object[][] types;
    private static final HashMap<String, String> typeAliases;
    
    public TypeInfoCache(final BaseConnection conn, final int unknownLength) {
        this.conn = conn;
        this.unknownLength = unknownLength;
        this.oidToPgName = new HashMap<Integer, String>((int)Math.round(TypeInfoCache.types.length * 1.5));
        this.pgNameToOid = new HashMap<String, Integer>((int)Math.round(TypeInfoCache.types.length * 1.5));
        this.pgNameToJavaClass = new HashMap<String, String>((int)Math.round(TypeInfoCache.types.length * 1.5));
        this.pgNameToPgObject = new HashMap<String, Class<? extends PGobject>>((int)Math.round(TypeInfoCache.types.length * 1.5));
        this.pgArrayToPgType = new HashMap<Integer, Integer>((int)Math.round(TypeInfoCache.types.length * 1.5));
        this.arrayOidToDelimiter = new HashMap<Integer, Character>((int)Math.round(TypeInfoCache.types.length * 2.5));
        this.pgNameToSQLType = Collections.synchronizedMap(new HashMap<String, Integer>((int)Math.round(TypeInfoCache.types.length * 1.5)));
        this.oidToSQLType = Collections.synchronizedMap(new HashMap<Integer, Integer>((int)Math.round(TypeInfoCache.types.length * 1.5)));
        for (final Object[] type : TypeInfoCache.types) {
            final String pgTypeName = (String)type[0];
            final Integer oid = (Integer)type[1];
            final Integer sqlType = (Integer)type[2];
            final String javaClass = (String)type[3];
            final Integer arrayOid = (Integer)type[4];
            this.addCoreType(pgTypeName, oid, sqlType, javaClass, arrayOid);
        }
        this.pgNameToJavaClass.put("hstore", Map.class.getName());
    }
    
    @Override
    public synchronized void addCoreType(final String pgTypeName, final Integer oid, final Integer sqlType, final String javaClass, final Integer arrayOid) {
        this.pgNameToJavaClass.put(pgTypeName, javaClass);
        this.pgNameToOid.put(pgTypeName, oid);
        this.oidToPgName.put(oid, pgTypeName);
        this.pgArrayToPgType.put(arrayOid, oid);
        this.pgNameToSQLType.put(pgTypeName, sqlType);
        this.oidToSQLType.put(oid, sqlType);
        final Character delim = ',';
        this.arrayOidToDelimiter.put(oid, delim);
        this.arrayOidToDelimiter.put(arrayOid, delim);
        String pgArrayTypeName = pgTypeName + "[]";
        this.pgNameToJavaClass.put(pgArrayTypeName, "java.sql.Array");
        this.pgNameToSQLType.put(pgArrayTypeName, 2003);
        this.oidToSQLType.put(arrayOid, 2003);
        this.pgNameToOid.put(pgArrayTypeName, arrayOid);
        pgArrayTypeName = "_" + pgTypeName;
        if (!this.pgNameToJavaClass.containsKey(pgArrayTypeName)) {
            this.pgNameToJavaClass.put(pgArrayTypeName, "java.sql.Array");
            this.pgNameToSQLType.put(pgArrayTypeName, 2003);
            this.pgNameToOid.put(pgArrayTypeName, arrayOid);
            this.oidToPgName.put(arrayOid, pgArrayTypeName);
        }
    }
    
    @Override
    public synchronized void addDataType(final String type, final Class<? extends PGobject> klass) throws SQLException {
        this.pgNameToPgObject.put(type, klass);
        this.pgNameToJavaClass.put(type, klass.getName());
    }
    
    @Override
    public Iterator<String> getPGTypeNamesWithSQLTypes() {
        return this.pgNameToSQLType.keySet().iterator();
    }
    
    @Override
    public Iterator<Integer> getPGTypeOidsWithSQLTypes() {
        return this.oidToSQLType.keySet().iterator();
    }
    
    private String getSQLTypeQuery(final boolean typoidParam) {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT typinput='array_in'::regproc as is_array, typtype, typname, pg_type.oid ");
        sql.append("  FROM pg_catalog.pg_type ");
        sql.append("  LEFT JOIN (select ns.oid as nspoid, ns.nspname, r.r ");
        sql.append("          from pg_namespace as ns ");
        sql.append("          join ( select s.r, (current_schemas(false))[s.r] as nspname ");
        sql.append("                   from generate_series(1, array_upper(current_schemas(false), 1)) as s(r) ) as r ");
        sql.append("         using ( nspname ) ");
        sql.append("       ) as sp ");
        sql.append("    ON sp.nspoid = typnamespace ");
        if (typoidParam) {
            sql.append(" WHERE pg_type.oid = ? ");
        }
        sql.append(" ORDER BY sp.r, pg_type.oid DESC;");
        return sql.toString();
    }
    
    private int getSQLTypeFromQueryResult(final ResultSet rs) throws SQLException {
        Integer type = null;
        final boolean isArray = rs.getBoolean("is_array");
        final String typtype = rs.getString("typtype");
        if (isArray) {
            type = 2003;
        }
        else if ("c".equals(typtype)) {
            type = 2002;
        }
        else if ("d".equals(typtype)) {
            type = 2001;
        }
        else if ("e".equals(typtype)) {
            type = 12;
        }
        if (type == null) {
            type = 1111;
        }
        return type;
    }
    
    private PreparedStatement prepareGetAllTypeInfoStatement() throws SQLException {
        PreparedStatement getAllTypeInfoStatement = this.getAllTypeInfoStatement;
        if (getAllTypeInfoStatement == null) {
            getAllTypeInfoStatement = this.conn.prepareStatement(this.getSQLTypeQuery(false));
            this.getAllTypeInfoStatement = getAllTypeInfoStatement;
        }
        return getAllTypeInfoStatement;
    }
    
    public void cacheSQLTypes() throws SQLException {
        TypeInfoCache.LOGGER.log(Level.FINEST, "caching all SQL typecodes");
        final PreparedStatement getAllTypeInfoStatement = this.prepareGetAllTypeInfoStatement();
        if (!((BaseStatement)getAllTypeInfoStatement).executeWithFlags(16)) {
            throw new PSQLException(GT.tr("No results were returned by the query.", new Object[0]), PSQLState.NO_DATA);
        }
        final ResultSet rs = Nullness.castNonNull(getAllTypeInfoStatement.getResultSet());
        while (rs.next()) {
            final String typeName = Nullness.castNonNull(rs.getString("typname"));
            final Integer type = this.getSQLTypeFromQueryResult(rs);
            if (!this.pgNameToSQLType.containsKey(typeName)) {
                this.pgNameToSQLType.put(typeName, type);
            }
            final Integer typeOid = this.longOidToInt(Nullness.castNonNull(rs.getLong("oid")));
            if (!this.oidToSQLType.containsKey(typeOid)) {
                this.oidToSQLType.put(typeOid, type);
            }
        }
        rs.close();
    }
    
    private PreparedStatement prepareGetTypeInfoStatement() throws SQLException {
        PreparedStatement getTypeInfoStatement = this.getTypeInfoStatement;
        if (getTypeInfoStatement == null) {
            getTypeInfoStatement = this.conn.prepareStatement(this.getSQLTypeQuery(true));
            this.getTypeInfoStatement = getTypeInfoStatement;
        }
        return getTypeInfoStatement;
    }
    
    @Override
    public synchronized int getSQLType(final String pgTypeName) throws SQLException {
        if (pgTypeName.endsWith("[]")) {
            return 2003;
        }
        Integer i = this.pgNameToSQLType.get(pgTypeName);
        if (i != null) {
            return i;
        }
        i = this.getSQLType(Nullness.castNonNull(this.getPGType(pgTypeName)));
        this.pgNameToSQLType.put(pgTypeName, i);
        return i;
    }
    
    @Override
    public synchronized int getSQLType(final int typeOid) throws SQLException {
        if (typeOid == 0) {
            return 1111;
        }
        final Integer i = this.oidToSQLType.get(typeOid);
        if (i != null) {
            return i;
        }
        TypeInfoCache.LOGGER.log(Level.FINEST, "querying SQL typecode for pg type oid '{0}'", this.intOidToLong(typeOid));
        final PreparedStatement getTypeInfoStatement = this.prepareGetTypeInfoStatement();
        getTypeInfoStatement.setLong(1, this.intOidToLong(typeOid));
        if (!((BaseStatement)getTypeInfoStatement).executeWithFlags(16)) {
            throw new PSQLException(GT.tr("No results were returned by the query.", new Object[0]), PSQLState.NO_DATA);
        }
        final ResultSet rs = Nullness.castNonNull(getTypeInfoStatement.getResultSet());
        int sqlType = 1111;
        if (rs.next()) {
            sqlType = this.getSQLTypeFromQueryResult(rs);
        }
        rs.close();
        this.oidToSQLType.put(typeOid, sqlType);
        return sqlType;
    }
    
    private PreparedStatement getOidStatement(final String pgTypeName) throws SQLException {
        final boolean isArray = pgTypeName.endsWith("[]");
        final boolean hasQuote = pgTypeName.contains("\"");
        final int dotIndex = pgTypeName.indexOf(46);
        if (dotIndex == -1 && !hasQuote && !isArray) {
            if (this.getOidStatementSimple == null) {
                final String sql = "SELECT pg_type.oid, typname   FROM pg_catalog.pg_type   LEFT   JOIN (select ns.oid as nspoid, ns.nspname, r.r           from pg_namespace as ns           join ( select s.r, (current_schemas(false))[s.r] as nspname                    from generate_series(1, array_upper(current_schemas(false), 1)) as s(r) ) as r          using ( nspname )        ) as sp     ON sp.nspoid = typnamespace  WHERE typname = ?  ORDER BY sp.r, pg_type.oid DESC LIMIT 1;";
                this.getOidStatementSimple = this.conn.prepareStatement(sql);
            }
            final String lcName = pgTypeName.toLowerCase();
            this.getOidStatementSimple.setString(1, lcName);
            return this.getOidStatementSimple;
        }
        PreparedStatement oidStatementComplex;
        if (isArray) {
            if (this.getOidStatementComplexArray == null) {
                String sql2;
                if (this.conn.haveMinimumServerVersion(ServerVersion.v8_3)) {
                    sql2 = "SELECT t.typarray, arr.typname   FROM pg_catalog.pg_type t  JOIN pg_catalog.pg_namespace n ON t.typnamespace = n.oid  JOIN pg_catalog.pg_type arr ON arr.oid = t.typarray WHERE t.typname = ? AND (n.nspname = ? OR ? AND n.nspname = ANY (current_schemas(true))) ORDER BY t.oid DESC LIMIT 1";
                }
                else {
                    sql2 = "SELECT t.oid, t.typname   FROM pg_catalog.pg_type t  JOIN pg_catalog.pg_namespace n ON t.typnamespace = n.oid WHERE t.typelem = (SELECT oid FROM pg_catalog.pg_type WHERE typname = ?) AND substring(t.typname, 1, 1) = '_' AND t.typlen = -1 AND (n.nspname = ? OR ? AND n.nspname = ANY (current_schemas(true))) ORDER BY t.typelem DESC LIMIT 1";
                }
                this.getOidStatementComplexArray = this.conn.prepareStatement(sql2);
            }
            oidStatementComplex = this.getOidStatementComplexArray;
        }
        else {
            if (this.getOidStatementComplexNonArray == null) {
                final String sql2 = "SELECT t.oid, t.typname   FROM pg_catalog.pg_type t  JOIN pg_catalog.pg_namespace n ON t.typnamespace = n.oid WHERE t.typname = ? AND (n.nspname = ? OR ? AND n.nspname = ANY (current_schemas(true))) ORDER BY t.oid DESC LIMIT 1";
                this.getOidStatementComplexNonArray = this.conn.prepareStatement(sql2);
            }
            oidStatementComplex = this.getOidStatementComplexNonArray;
        }
        final String fullName = isArray ? pgTypeName.substring(0, pgTypeName.length() - 2) : pgTypeName;
        String schema;
        String name;
        if (dotIndex == -1) {
            schema = null;
            name = fullName;
        }
        else if (fullName.startsWith("\"")) {
            if (fullName.endsWith("\"")) {
                final String[] parts = fullName.split("\"\\.\"");
                schema = ((parts.length == 2) ? (parts[0] + "\"") : null);
                name = ((parts.length == 2) ? ("\"" + parts[1]) : parts[0]);
            }
            else {
                final int lastDotIndex = fullName.lastIndexOf(46);
                name = fullName.substring(lastDotIndex + 1);
                schema = fullName.substring(0, lastDotIndex);
            }
        }
        else {
            schema = fullName.substring(0, dotIndex);
            name = fullName.substring(dotIndex + 1);
        }
        if (schema != null && schema.startsWith("\"") && schema.endsWith("\"")) {
            schema = schema.substring(1, schema.length() - 1);
        }
        else if (schema != null) {
            schema = schema.toLowerCase();
        }
        if (name.startsWith("\"") && name.endsWith("\"")) {
            name = name.substring(1, name.length() - 1);
        }
        else {
            name = name.toLowerCase();
        }
        oidStatementComplex.setString(1, name);
        oidStatementComplex.setString(2, schema);
        oidStatementComplex.setBoolean(3, schema == null);
        return oidStatementComplex;
    }
    
    @Override
    public synchronized int getPGType(final String pgTypeName) throws SQLException {
        if (pgTypeName == null) {
            return 0;
        }
        Integer oid = this.pgNameToOid.get(pgTypeName);
        if (oid != null) {
            return oid;
        }
        final PreparedStatement oidStatement = this.getOidStatement(pgTypeName);
        if (!((BaseStatement)oidStatement).executeWithFlags(16)) {
            throw new PSQLException(GT.tr("No results were returned by the query.", new Object[0]), PSQLState.NO_DATA);
        }
        oid = 0;
        final ResultSet rs = Nullness.castNonNull(oidStatement.getResultSet());
        if (rs.next()) {
            oid = (int)rs.getLong(1);
            final String internalName = Nullness.castNonNull(rs.getString(2));
            this.oidToPgName.put(oid, internalName);
            this.pgNameToOid.put(internalName, oid);
        }
        this.pgNameToOid.put(pgTypeName, oid);
        rs.close();
        return oid;
    }
    
    @Override
    public synchronized String getPGType(final int oid) throws SQLException {
        if (oid == 0) {
            return null;
        }
        String pgTypeName = this.oidToPgName.get(oid);
        if (pgTypeName != null) {
            return pgTypeName;
        }
        final PreparedStatement getNameStatement = this.prepareGetNameStatement();
        getNameStatement.setInt(1, oid);
        if (!((BaseStatement)getNameStatement).executeWithFlags(16)) {
            throw new PSQLException(GT.tr("No results were returned by the query.", new Object[0]), PSQLState.NO_DATA);
        }
        final ResultSet rs = Nullness.castNonNull(getNameStatement.getResultSet());
        if (rs.next()) {
            final boolean onPath = rs.getBoolean(1);
            final String schema = Nullness.castNonNull(rs.getString(2), "schema");
            final String name = Nullness.castNonNull(rs.getString(3), "name");
            if (onPath) {
                pgTypeName = name;
                this.pgNameToOid.put(schema + "." + name, oid);
            }
            else {
                pgTypeName = "\"" + schema + "\".\"" + name + "\"";
                if (schema.equals(schema.toLowerCase()) && schema.indexOf(46) == -1 && name.equals(name.toLowerCase()) && name.indexOf(46) == -1) {
                    this.pgNameToOid.put(schema + "." + name, oid);
                }
            }
            this.pgNameToOid.put(pgTypeName, oid);
            this.oidToPgName.put(oid, pgTypeName);
        }
        rs.close();
        return pgTypeName;
    }
    
    private PreparedStatement prepareGetNameStatement() throws SQLException {
        PreparedStatement getNameStatement = this.getNameStatement;
        if (getNameStatement == null) {
            final String sql = "SELECT n.nspname = ANY(current_schemas(true)), n.nspname, t.typname FROM pg_catalog.pg_type t JOIN pg_catalog.pg_namespace n ON t.typnamespace = n.oid WHERE t.oid = ?";
            getNameStatement = (this.getNameStatement = this.conn.prepareStatement(sql));
        }
        return getNameStatement;
    }
    
    @Override
    public int getPGArrayType(String elementTypeName) throws SQLException {
        elementTypeName = this.getTypeForAlias(elementTypeName);
        return this.getPGType(elementTypeName + "[]");
    }
    
    protected synchronized int convertArrayToBaseOid(final int oid) {
        final Integer i = this.pgArrayToPgType.get(oid);
        if (i == null) {
            return oid;
        }
        return i;
    }
    
    @Override
    public synchronized char getArrayDelimiter(final int oid) throws SQLException {
        if (oid == 0) {
            return ',';
        }
        Character delim = this.arrayOidToDelimiter.get(oid);
        if (delim != null) {
            return delim;
        }
        final PreparedStatement getArrayDelimiterStatement = this.prepareGetArrayDelimiterStatement();
        getArrayDelimiterStatement.setInt(1, oid);
        if (!((BaseStatement)getArrayDelimiterStatement).executeWithFlags(16)) {
            throw new PSQLException(GT.tr("No results were returned by the query.", new Object[0]), PSQLState.NO_DATA);
        }
        final ResultSet rs = Nullness.castNonNull(getArrayDelimiterStatement.getResultSet());
        if (!rs.next()) {
            throw new PSQLException(GT.tr("No results were returned by the query.", new Object[0]), PSQLState.NO_DATA);
        }
        final String s = Nullness.castNonNull(rs.getString(1));
        delim = s.charAt(0);
        this.arrayOidToDelimiter.put(oid, delim);
        rs.close();
        return delim;
    }
    
    private PreparedStatement prepareGetArrayDelimiterStatement() throws SQLException {
        PreparedStatement getArrayDelimiterStatement = this.getArrayDelimiterStatement;
        if (getArrayDelimiterStatement == null) {
            final String sql = "SELECT e.typdelim FROM pg_catalog.pg_type t, pg_catalog.pg_type e WHERE t.oid = ? and t.typelem = e.oid";
            getArrayDelimiterStatement = (this.getArrayDelimiterStatement = this.conn.prepareStatement(sql));
        }
        return getArrayDelimiterStatement;
    }
    
    @Override
    public synchronized int getPGArrayElement(final int oid) throws SQLException {
        if (oid == 0) {
            return 0;
        }
        Integer pgType = this.pgArrayToPgType.get(oid);
        if (pgType != null) {
            return pgType;
        }
        final PreparedStatement getArrayElementOidStatement = this.prepareGetArrayElementOidStatement();
        getArrayElementOidStatement.setInt(1, oid);
        if (!((BaseStatement)getArrayElementOidStatement).executeWithFlags(16)) {
            throw new PSQLException(GT.tr("No results were returned by the query.", new Object[0]), PSQLState.NO_DATA);
        }
        final ResultSet rs = Nullness.castNonNull(getArrayElementOidStatement.getResultSet());
        if (!rs.next()) {
            throw new PSQLException(GT.tr("No results were returned by the query.", new Object[0]), PSQLState.NO_DATA);
        }
        pgType = (int)rs.getLong(1);
        final boolean onPath = rs.getBoolean(2);
        final String schema = rs.getString(3);
        final String name = Nullness.castNonNull(rs.getString(4));
        this.pgArrayToPgType.put(oid, pgType);
        this.pgNameToOid.put(schema + "." + name, pgType);
        final String fullName = "\"" + schema + "\".\"" + name + "\"";
        this.pgNameToOid.put(fullName, pgType);
        if (onPath && name.equals(name.toLowerCase())) {
            this.oidToPgName.put(pgType, name);
            this.pgNameToOid.put(name, pgType);
        }
        else {
            this.oidToPgName.put(pgType, fullName);
        }
        rs.close();
        return pgType;
    }
    
    private PreparedStatement prepareGetArrayElementOidStatement() throws SQLException {
        PreparedStatement getArrayElementOidStatement = this.getArrayElementOidStatement;
        if (getArrayElementOidStatement == null) {
            final String sql = "SELECT e.oid, n.nspname = ANY(current_schemas(true)), n.nspname, e.typname FROM pg_catalog.pg_type t JOIN pg_catalog.pg_type e ON t.typelem = e.oid JOIN pg_catalog.pg_namespace n ON t.typnamespace = n.oid WHERE t.oid = ?";
            getArrayElementOidStatement = (this.getArrayElementOidStatement = this.conn.prepareStatement(sql));
        }
        return getArrayElementOidStatement;
    }
    
    @Override
    public synchronized Class<? extends PGobject> getPGobject(final String type) {
        return this.pgNameToPgObject.get(type);
    }
    
    @Override
    public synchronized String getJavaClass(final int oid) throws SQLException {
        final String pgTypeName = this.getPGType(oid);
        if (pgTypeName == null) {
            return "java.lang.String";
        }
        String result = this.pgNameToJavaClass.get(pgTypeName);
        if (result != null) {
            return result;
        }
        if (this.getSQLType(pgTypeName) == 2003) {
            result = "java.sql.Array";
            this.pgNameToJavaClass.put(pgTypeName, result);
        }
        return (result == null) ? "java.lang.String" : result;
    }
    
    @Override
    public String getTypeForAlias(final String alias) {
        if (alias == null) {
            return null;
        }
        String type = TypeInfoCache.typeAliases.get(alias);
        if (type != null) {
            return type;
        }
        if (alias.indexOf(34) == -1) {
            type = TypeInfoCache.typeAliases.get(alias.toLowerCase());
            if (type != null) {
                return type;
            }
        }
        return alias;
    }
    
    @Override
    public int getPrecision(int oid, final int typmod) {
        oid = this.convertArrayToBaseOid(oid);
        switch (oid) {
            case 21: {
                return 5;
            }
            case 23:
            case 26: {
                return 10;
            }
            case 20: {
                return 19;
            }
            case 700: {
                return 8;
            }
            case 701: {
                return 17;
            }
            case 1700: {
                if (typmod == -1) {
                    return 0;
                }
                return (typmod - 4 & 0xFFFF0000) >> 16;
            }
            case 16:
            case 18: {
                return 1;
            }
            case 1042:
            case 1043: {
                if (typmod == -1) {
                    return this.unknownLength;
                }
                return typmod - 4;
            }
            case 1082:
            case 1083:
            case 1114:
            case 1184:
            case 1186:
            case 1266: {
                return this.getDisplaySize(oid, typmod);
            }
            case 1560: {
                return typmod;
            }
            case 1562: {
                if (typmod == -1) {
                    return this.unknownLength;
                }
                return typmod;
            }
            default: {
                return this.unknownLength;
            }
        }
    }
    
    @Override
    public int getScale(int oid, final int typmod) {
        oid = this.convertArrayToBaseOid(oid);
        switch (oid) {
            case 700: {
                return 8;
            }
            case 701: {
                return 17;
            }
            case 1700: {
                if (typmod == -1) {
                    return 0;
                }
                return typmod - 4 & 0xFFFF;
            }
            case 1083:
            case 1114:
            case 1184:
            case 1266: {
                if (typmod == -1) {
                    return 6;
                }
                return typmod;
            }
            case 1186: {
                if (typmod == -1) {
                    return 6;
                }
                return typmod & 0xFFFF;
            }
            default: {
                return 0;
            }
        }
    }
    
    @Override
    public boolean isCaseSensitive(int oid) {
        oid = this.convertArrayToBaseOid(oid);
        switch (oid) {
            case 16:
            case 20:
            case 21:
            case 23:
            case 26:
            case 700:
            case 701:
            case 1082:
            case 1083:
            case 1114:
            case 1184:
            case 1186:
            case 1266:
            case 1560:
            case 1562:
            case 1700: {
                return false;
            }
            default: {
                return true;
            }
        }
    }
    
    @Override
    public boolean isSigned(int oid) {
        oid = this.convertArrayToBaseOid(oid);
        switch (oid) {
            case 20:
            case 21:
            case 23:
            case 700:
            case 701:
            case 1700: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    @Override
    public int getDisplaySize(int oid, final int typmod) {
        oid = this.convertArrayToBaseOid(oid);
        switch (oid) {
            case 21: {
                return 6;
            }
            case 23: {
                return 11;
            }
            case 26: {
                return 10;
            }
            case 20: {
                return 20;
            }
            case 700: {
                return 15;
            }
            case 701: {
                return 25;
            }
            case 18: {
                return 1;
            }
            case 16: {
                return 1;
            }
            case 1082: {
                return 13;
            }
            case 1083:
            case 1114:
            case 1184:
            case 1266: {
                int secondSize = 0;
                switch (typmod) {
                    case -1: {
                        secondSize = 7;
                        break;
                    }
                    case 0: {
                        secondSize = 0;
                        break;
                    }
                    case 1: {
                        secondSize = 3;
                        break;
                    }
                    default: {
                        secondSize = typmod + 1;
                        break;
                    }
                }
                switch (oid) {
                    case 1083: {
                        return 8 + secondSize;
                    }
                    case 1266: {
                        return 8 + secondSize + 6;
                    }
                    case 1114: {
                        return 22 + secondSize;
                    }
                    case 1184: {
                        return 22 + secondSize + 6;
                    }
                    default: {
                        return 49;
                    }
                }
                break;
            }
            case 1186: {
                return 49;
            }
            case 1042:
            case 1043: {
                if (typmod == -1) {
                    return this.unknownLength;
                }
                return typmod - 4;
            }
            case 1700: {
                if (typmod == -1) {
                    return 131089;
                }
                final int precision = typmod - 4 >> 16 & 0xFFFF;
                final int scale = typmod - 4 & 0xFFFF;
                return 1 + precision + ((scale != 0) ? 1 : 0);
            }
            case 1560: {
                return typmod;
            }
            case 1562: {
                if (typmod == -1) {
                    return this.unknownLength;
                }
                return typmod;
            }
            case 17:
            case 25: {
                return this.unknownLength;
            }
            default: {
                return this.unknownLength;
            }
        }
    }
    
    @Override
    public int getMaximumPrecision(int oid) {
        oid = this.convertArrayToBaseOid(oid);
        switch (oid) {
            case 1700: {
                return 1000;
            }
            case 1083:
            case 1266: {
                return 6;
            }
            case 1114:
            case 1184:
            case 1186: {
                return 6;
            }
            case 1042:
            case 1043: {
                return 10485760;
            }
            case 1560:
            case 1562: {
                return 83886080;
            }
            default: {
                return 0;
            }
        }
    }
    
    @Override
    public boolean requiresQuoting(final int oid) throws SQLException {
        final int sqlType = this.getSQLType(oid);
        return this.requiresQuotingSqlType(sqlType);
    }
    
    @Override
    public boolean requiresQuotingSqlType(final int sqlType) throws SQLException {
        switch (sqlType) {
            case -6:
            case -5:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8: {
                return false;
            }
            default: {
                return true;
            }
        }
    }
    
    @Override
    public int longOidToInt(final long oid) throws SQLException {
        if ((oid & 0xFFFFFFFF00000000L) != 0x0L) {
            throw new PSQLException(GT.tr("Value is not an OID: {0}", oid), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
        }
        return (int)oid;
    }
    
    @Override
    public long intOidToLong(final int oid) {
        return (long)oid & 0xFFFFFFFFL;
    }
    
    static {
        LOGGER = Logger.getLogger(TypeInfoCache.class.getName());
        types = new Object[][] { { "int2", 21, 5, "java.lang.Integer", 1005 }, { "int4", 23, 4, "java.lang.Integer", 1007 }, { "oid", 26, -5, "java.lang.Long", 1028 }, { "int8", 20, -5, "java.lang.Long", 1016 }, { "money", 790, 8, "java.lang.Double", 791 }, { "numeric", 1700, 2, "java.math.BigDecimal", 1231 }, { "float4", 700, 7, "java.lang.Float", 1021 }, { "float8", 701, 8, "java.lang.Double", 1022 }, { "char", 18, 1, "java.lang.String", 1002 }, { "bpchar", 1042, 1, "java.lang.String", 1014 }, { "varchar", 1043, 12, "java.lang.String", 1015 }, { "text", 25, 12, "java.lang.String", 1009 }, { "name", 19, 12, "java.lang.String", 1003 }, { "bytea", 17, -2, "[B", 1001 }, { "bool", 16, -7, "java.lang.Boolean", 1000 }, { "bit", 1560, -7, "java.lang.Boolean", 1561 }, { "date", 1082, 91, "java.sql.Date", 1182 }, { "time", 1083, 92, "java.sql.Time", 1183 }, { "timetz", 1266, 92, "java.sql.Time", 1270 }, { "timestamp", 1114, 93, "java.sql.Timestamp", 1115 }, { "timestamptz", 1184, 93, "java.sql.Timestamp", 1185 }, { "refcursor", 1790, 2012, "java.sql.ResultSet", 2201 }, { "json", 114, 1111, "org.postgresql.util.PGobject", 199 }, { "point", 600, 1111, "org.postgresql.geometric.PGpoint", 1017 } };
        (typeAliases = new HashMap<String, String>()).put("smallint", "int2");
        TypeInfoCache.typeAliases.put("integer", "int4");
        TypeInfoCache.typeAliases.put("int", "int4");
        TypeInfoCache.typeAliases.put("bigint", "int8");
        TypeInfoCache.typeAliases.put("float", "float8");
        TypeInfoCache.typeAliases.put("boolean", "bool");
        TypeInfoCache.typeAliases.put("decimal", "numeric");
    }
}
