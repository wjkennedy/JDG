// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import org.postgresql.util.JdbcBlackHole;
import org.postgresql.util.internal.Nullness;
import org.postgresql.util.GettableHashMap;
import org.postgresql.core.Version;
import org.postgresql.core.ServerVersion;
import org.postgresql.util.Gettable;
import java.sql.SQLException;
import org.postgresql.core.Field;
import org.postgresql.core.BaseConnection;
import org.postgresql.PGResultSetMetaData;
import java.sql.ResultSetMetaData;

public class PgResultSetMetaData implements ResultSetMetaData, PGResultSetMetaData
{
    protected final BaseConnection connection;
    protected final Field[] fields;
    private boolean fieldInfoFetched;
    
    public PgResultSetMetaData(final BaseConnection connection, final Field[] fields) {
        this.connection = connection;
        this.fields = fields;
        this.fieldInfoFetched = false;
    }
    
    @Override
    public int getColumnCount() throws SQLException {
        return this.fields.length;
    }
    
    @Override
    public boolean isAutoIncrement(final int column) throws SQLException {
        this.fetchFieldMetaData();
        final Field field = this.getField(column);
        final FieldMetadata metadata = field.getMetadata();
        return metadata != null && metadata.autoIncrement;
    }
    
    @Override
    public boolean isCaseSensitive(final int column) throws SQLException {
        final Field field = this.getField(column);
        return this.connection.getTypeInfo().isCaseSensitive(field.getOID());
    }
    
    @Override
    public boolean isSearchable(final int column) throws SQLException {
        return true;
    }
    
    @Override
    public boolean isCurrency(final int column) throws SQLException {
        final String typeName = this.getPGType(column);
        return "cash".equals(typeName) || "money".equals(typeName);
    }
    
    @Override
    public int isNullable(final int column) throws SQLException {
        this.fetchFieldMetaData();
        final Field field = this.getField(column);
        final FieldMetadata metadata = field.getMetadata();
        return (metadata == null) ? 1 : metadata.nullable;
    }
    
    @Override
    public boolean isSigned(final int column) throws SQLException {
        final Field field = this.getField(column);
        return this.connection.getTypeInfo().isSigned(field.getOID());
    }
    
    @Override
    public int getColumnDisplaySize(final int column) throws SQLException {
        final Field field = this.getField(column);
        return this.connection.getTypeInfo().getDisplaySize(field.getOID(), field.getMod());
    }
    
    @Override
    public String getColumnLabel(final int column) throws SQLException {
        final Field field = this.getField(column);
        return field.getColumnLabel();
    }
    
    @Override
    public String getColumnName(final int column) throws SQLException {
        return this.getColumnLabel(column);
    }
    
    @Override
    public String getBaseColumnName(final int column) throws SQLException {
        final Field field = this.getField(column);
        if (field.getTableOid() == 0) {
            return "";
        }
        this.fetchFieldMetaData();
        final FieldMetadata metadata = field.getMetadata();
        return (metadata == null) ? "" : metadata.columnName;
    }
    
    @Override
    public String getSchemaName(final int column) throws SQLException {
        return "";
    }
    
    private boolean populateFieldsWithMetadata(final Gettable<FieldMetadata.Key, FieldMetadata> metadata) {
        boolean allOk = true;
        for (final Field field : this.fields) {
            if (field.getMetadata() == null) {
                final FieldMetadata fieldMetadata = metadata.get(new FieldMetadata.Key(field.getTableOid(), field.getPositionInTable()));
                if (fieldMetadata == null) {
                    allOk = false;
                }
                else {
                    field.setMetadata(fieldMetadata);
                }
            }
        }
        this.fieldInfoFetched |= allOk;
        return allOk;
    }
    
    private void fetchFieldMetaData() throws SQLException {
        if (this.fieldInfoFetched) {
            return;
        }
        if (this.populateFieldsWithMetadata(this.connection.getFieldMetadataCache())) {
            return;
        }
        final StringBuilder sql = new StringBuilder("SELECT c.oid, a.attnum, a.attname, c.relname, n.nspname, a.attnotnull OR (t.typtype = 'd' AND t.typnotnull), ");
        if (this.connection.haveMinimumServerVersion(ServerVersion.v10)) {
            sql.append("a.attidentity != '' OR pg_catalog.pg_get_expr(d.adbin, d.adrelid) LIKE '%nextval(%' ");
        }
        else {
            sql.append("pg_catalog.pg_get_expr(d.adbin, d.adrelid) LIKE '%nextval(%' ");
        }
        sql.append("FROM pg_catalog.pg_class c JOIN pg_catalog.pg_namespace n ON (c.relnamespace = n.oid) JOIN pg_catalog.pg_attribute a ON (c.oid = a.attrelid) JOIN pg_catalog.pg_type t ON (a.atttypid = t.oid) LEFT JOIN pg_catalog.pg_attrdef d ON (d.adrelid = a.attrelid AND d.adnum = a.attnum) JOIN (");
        boolean hasSourceInfo = false;
        for (final Field field : this.fields) {
            if (field.getMetadata() == null) {
                if (hasSourceInfo) {
                    sql.append(" UNION ALL ");
                }
                sql.append("SELECT ");
                sql.append(field.getTableOid());
                if (!hasSourceInfo) {
                    sql.append(" AS oid ");
                }
                sql.append(", ");
                sql.append(field.getPositionInTable());
                if (!hasSourceInfo) {
                    sql.append(" AS attnum");
                }
                if (!hasSourceInfo) {
                    hasSourceInfo = true;
                }
            }
        }
        sql.append(") vals ON (c.oid = vals.oid AND a.attnum = vals.attnum) ");
        if (!hasSourceInfo) {
            this.fieldInfoFetched = true;
            return;
        }
        final Statement stmt = this.connection.createStatement();
        ResultSet rs = null;
        final GettableHashMap<FieldMetadata.Key, FieldMetadata> md = new GettableHashMap<FieldMetadata.Key, FieldMetadata>();
        try {
            rs = stmt.executeQuery(sql.toString());
            while (rs.next()) {
                final int table = (int)rs.getLong(1);
                final int column = (int)rs.getLong(2);
                final String columnName = Nullness.castNonNull(rs.getString(3));
                final String tableName = Nullness.castNonNull(rs.getString(4));
                final String schemaName = Nullness.castNonNull(rs.getString(5));
                final int nullable = rs.getBoolean(6) ? 0 : 1;
                final boolean autoIncrement = rs.getBoolean(7);
                final FieldMetadata fieldMetadata = new FieldMetadata(columnName, tableName, schemaName, nullable, autoIncrement);
                final FieldMetadata.Key key = new FieldMetadata.Key(table, column);
                md.put(key, fieldMetadata);
            }
        }
        finally {
            JdbcBlackHole.close(rs);
            JdbcBlackHole.close(stmt);
        }
        this.populateFieldsWithMetadata(md);
        this.connection.getFieldMetadataCache().putAll(md);
    }
    
    @Override
    public String getBaseSchemaName(final int column) throws SQLException {
        this.fetchFieldMetaData();
        final Field field = this.getField(column);
        final FieldMetadata metadata = field.getMetadata();
        return (metadata == null) ? "" : metadata.schemaName;
    }
    
    @Override
    public int getPrecision(final int column) throws SQLException {
        final Field field = this.getField(column);
        return this.connection.getTypeInfo().getPrecision(field.getOID(), field.getMod());
    }
    
    @Override
    public int getScale(final int column) throws SQLException {
        final Field field = this.getField(column);
        return this.connection.getTypeInfo().getScale(field.getOID(), field.getMod());
    }
    
    @Override
    public String getTableName(final int column) throws SQLException {
        return this.getBaseTableName(column);
    }
    
    @Override
    public String getBaseTableName(final int column) throws SQLException {
        this.fetchFieldMetaData();
        final Field field = this.getField(column);
        final FieldMetadata metadata = field.getMetadata();
        return (metadata == null) ? "" : metadata.tableName;
    }
    
    @Override
    public String getCatalogName(final int column) throws SQLException {
        return "";
    }
    
    @Override
    public int getColumnType(final int column) throws SQLException {
        return this.getSQLType(column);
    }
    
    @Override
    public int getFormat(final int column) throws SQLException {
        return this.getField(column).getFormat();
    }
    
    @Override
    public String getColumnTypeName(final int column) throws SQLException {
        final String type = this.getPGType(column);
        if (this.isAutoIncrement(column)) {
            if ("int4".equals(type)) {
                return "serial";
            }
            if ("int8".equals(type)) {
                return "bigserial";
            }
            if ("int2".equals(type) && this.connection.haveMinimumServerVersion(ServerVersion.v9_2)) {
                return "smallserial";
            }
        }
        return Nullness.castNonNull(type);
    }
    
    @Override
    public boolean isReadOnly(final int column) throws SQLException {
        return false;
    }
    
    @Override
    public boolean isWritable(final int column) throws SQLException {
        return !this.isReadOnly(column);
    }
    
    @Override
    public boolean isDefinitelyWritable(final int column) throws SQLException {
        return false;
    }
    
    protected Field getField(final int columnIndex) throws SQLException {
        if (columnIndex < 1 || columnIndex > this.fields.length) {
            throw new PSQLException(GT.tr("The column index is out of range: {0}, number of columns: {1}.", columnIndex, this.fields.length), PSQLState.INVALID_PARAMETER_VALUE);
        }
        return this.fields[columnIndex - 1];
    }
    
    protected String getPGType(final int columnIndex) throws SQLException {
        return this.connection.getTypeInfo().getPGType(this.getField(columnIndex).getOID());
    }
    
    protected int getSQLType(final int columnIndex) throws SQLException {
        return this.connection.getTypeInfo().getSQLType(this.getField(columnIndex).getOID());
    }
    
    @Override
    public String getColumnClassName(final int column) throws SQLException {
        final Field field = this.getField(column);
        final String result = this.connection.getTypeInfo().getJavaClass(field.getOID());
        if (result != null) {
            return result;
        }
        final int sqlType = this.getSQLType(column);
        switch (sqlType) {
            case 2003: {
                return "java.sql.Array";
            }
            default: {
                final String type = this.getPGType(column);
                if ("unknown".equals(type)) {
                    return "java.lang.String";
                }
                return "java.lang.Object";
            }
        }
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
}
