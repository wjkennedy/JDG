// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

import org.postgresql.jdbc2.ArrayAssistant;
import org.postgresql.jdbc2.ArrayAssistantRegistry;
import java.util.List;
import org.postgresql.core.BaseStatement;
import org.postgresql.core.Field;
import org.postgresql.core.Tuple;
import java.util.ArrayList;
import org.postgresql.util.ByteConverter;
import java.sql.ResultSet;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import org.postgresql.Driver;
import java.util.Map;
import org.postgresql.util.internal.Nullness;
import java.sql.SQLException;
import org.postgresql.core.BaseConnection;
import java.sql.Array;

public class PgArray implements Array
{
    protected BaseConnection connection;
    private final int oid;
    protected String fieldString;
    protected ArrayDecoding.PgArrayList arrayList;
    protected byte[] fieldBytes;
    
    private PgArray(final BaseConnection connection, final int oid) throws SQLException {
        this.connection = connection;
        this.oid = oid;
    }
    
    public PgArray(final BaseConnection connection, final int oid, final String fieldString) throws SQLException {
        this(connection, oid);
        this.fieldString = fieldString;
    }
    
    public PgArray(final BaseConnection connection, final int oid, final byte[] fieldBytes) throws SQLException {
        this(connection, oid);
        this.fieldBytes = fieldBytes;
    }
    
    private BaseConnection getConnection() {
        return Nullness.castNonNull(this.connection);
    }
    
    @Override
    public Object getArray() throws SQLException {
        return this.getArrayImpl(1L, 0, null);
    }
    
    @Override
    public Object getArray(final long index, final int count) throws SQLException {
        return this.getArrayImpl(index, count, null);
    }
    
    public Object getArrayImpl(final Map<String, Class<?>> map) throws SQLException {
        return this.getArrayImpl(1L, 0, map);
    }
    
    @Override
    public Object getArray(final Map<String, Class<?>> map) throws SQLException {
        return this.getArrayImpl(map);
    }
    
    @Override
    public Object getArray(final long index, final int count, final Map<String, Class<?>> map) throws SQLException {
        return this.getArrayImpl(index, count, map);
    }
    
    public Object getArrayImpl(final long index, int count, final Map<String, Class<?>> map) throws SQLException {
        if (map != null && !map.isEmpty()) {
            throw Driver.notImplemented(this.getClass(), "getArrayImpl(long,int,Map)");
        }
        if (index < 1L) {
            throw new PSQLException(GT.tr("The array index is out of range: {0}", index), PSQLState.DATA_ERROR);
        }
        if (this.fieldBytes != null) {
            return this.readBinaryArray(this.fieldBytes, (int)index, count);
        }
        if (this.fieldString == null) {
            return null;
        }
        final ArrayDecoding.PgArrayList arrayList = this.buildArrayList(this.fieldString);
        if (count == 0) {
            count = arrayList.size();
        }
        if (index - 1L + count > arrayList.size()) {
            throw new PSQLException(GT.tr("The array index is out of range: {0}, number of elements: {1}.", index + count, arrayList.size()), PSQLState.DATA_ERROR);
        }
        return this.buildArray(arrayList, (int)index, count);
    }
    
    private Object readBinaryArray(final byte[] fieldBytes, final int index, final int count) throws SQLException {
        return ArrayDecoding.readBinaryArray(index, count, fieldBytes, this.getConnection());
    }
    
    private ResultSet readBinaryResultSet(final byte[] fieldBytes, final int index, final int count) throws SQLException {
        final int dimensions = ByteConverter.int4(fieldBytes, 0);
        final int elementOid = ByteConverter.int4(fieldBytes, 8);
        int pos = 12;
        final int[] dims = new int[dimensions];
        for (int d = 0; d < dimensions; ++d) {
            dims[d] = ByteConverter.int4(fieldBytes, pos);
            pos += 4;
            pos += 4;
        }
        if (count > 0 && dimensions > 0) {
            dims[0] = Math.min(count, dims[0]);
        }
        final List<Tuple> rows = new ArrayList<Tuple>();
        final Field[] fields = new Field[2];
        this.storeValues(fieldBytes, rows, fields, elementOid, dims, pos, 0, index);
        final BaseStatement stat = (BaseStatement)this.getConnection().createStatement(1004, 1007);
        return stat.createDriverResultSet(fields, rows);
    }
    
    private int storeValues(final byte[] fieldBytes, final List<Tuple> rows, final Field[] fields, final int elementOid, final int[] dims, int pos, final int thisDimension, final int index) throws SQLException {
        if (dims.length == 0) {
            (fields[0] = new Field("INDEX", 23)).setFormat(1);
            (fields[1] = new Field("VALUE", elementOid)).setFormat(1);
            for (int i = 1; i < index; ++i) {
                final int len = ByteConverter.int4(fieldBytes, pos);
                pos += 4;
                if (len != -1) {
                    pos += len;
                }
            }
        }
        else if (thisDimension == dims.length - 1) {
            (fields[0] = new Field("INDEX", 23)).setFormat(1);
            (fields[1] = new Field("VALUE", elementOid)).setFormat(1);
            for (int i = 1; i < index; ++i) {
                final int len = ByteConverter.int4(fieldBytes, pos);
                pos += 4;
                if (len != -1) {
                    pos += len;
                }
            }
            for (int i = 0; i < dims[thisDimension]; ++i) {
                final byte[][] rowData = { new byte[4], null };
                ByteConverter.int4(rowData[0], 0, i + index);
                rows.add(new Tuple(rowData));
                final int len2 = ByteConverter.int4(fieldBytes, pos);
                pos += 4;
                if (len2 != -1) {
                    System.arraycopy(fieldBytes, pos, rowData[1] = new byte[len2], 0, rowData[1].length);
                    pos += len2;
                }
            }
        }
        else {
            (fields[0] = new Field("INDEX", 23)).setFormat(1);
            (fields[1] = new Field("VALUE", this.oid)).setFormat(1);
            final int nextDimension = thisDimension + 1;
            final int dimensionsLeft = dims.length - nextDimension;
            for (int j = 1; j < index; ++j) {
                pos = this.calcRemainingDataLength(fieldBytes, dims, pos, elementOid, nextDimension);
            }
            for (int j = 0; j < dims[thisDimension]; ++j) {
                final byte[][] rowData2 = { new byte[4], null };
                ByteConverter.int4(rowData2[0], 0, j + index);
                rows.add(new Tuple(rowData2));
                final int dataEndPos = this.calcRemainingDataLength(fieldBytes, dims, pos, elementOid, nextDimension);
                final int dataLength = dataEndPos - pos;
                ByteConverter.int4(rowData2[1] = new byte[12 + 8 * dimensionsLeft + dataLength], 0, dimensionsLeft);
                System.arraycopy(fieldBytes, 4, rowData2[1], 4, 8);
                System.arraycopy(fieldBytes, 12 + nextDimension * 8, rowData2[1], 12, dimensionsLeft * 8);
                System.arraycopy(fieldBytes, pos, rowData2[1], 12 + dimensionsLeft * 8, dataLength);
                pos = dataEndPos;
            }
        }
        return pos;
    }
    
    private int calcRemainingDataLength(final byte[] fieldBytes, final int[] dims, int pos, final int elementOid, final int thisDimension) {
        if (thisDimension == dims.length - 1) {
            for (int i = 0; i < dims[thisDimension]; ++i) {
                final int len = ByteConverter.int4(fieldBytes, pos);
                pos += 4;
                if (len != -1) {
                    pos += len;
                }
            }
        }
        else {
            pos = this.calcRemainingDataLength(fieldBytes, dims, elementOid, pos, thisDimension + 1);
        }
        return pos;
    }
    
    private synchronized ArrayDecoding.PgArrayList buildArrayList(final String fieldString) throws SQLException {
        if (this.arrayList == null) {
            this.arrayList = ArrayDecoding.buildArrayList(fieldString, this.getConnection().getTypeInfo().getArrayDelimiter(this.oid));
        }
        return this.arrayList;
    }
    
    private Object buildArray(final ArrayDecoding.PgArrayList input, final int index, final int count) throws SQLException {
        final BaseConnection connection = this.getConnection();
        return ArrayDecoding.readStringArray(index, count, connection.getTypeInfo().getPGArrayElement(this.oid), input, connection);
    }
    
    @Override
    public int getBaseType() throws SQLException {
        return this.getConnection().getTypeInfo().getSQLType(this.getBaseTypeName());
    }
    
    @Override
    public String getBaseTypeName() throws SQLException {
        final int elementOID = this.getConnection().getTypeInfo().getPGArrayElement(this.oid);
        return Nullness.castNonNull(this.getConnection().getTypeInfo().getPGType(elementOID));
    }
    
    @Override
    public ResultSet getResultSet() throws SQLException {
        return this.getResultSetImpl(1L, 0, null);
    }
    
    @Override
    public ResultSet getResultSet(final long index, final int count) throws SQLException {
        return this.getResultSetImpl(index, count, null);
    }
    
    @Override
    public ResultSet getResultSet(final Map<String, Class<?>> map) throws SQLException {
        return this.getResultSetImpl(map);
    }
    
    @Override
    public ResultSet getResultSet(final long index, final int count, final Map<String, Class<?>> map) throws SQLException {
        return this.getResultSetImpl(index, count, map);
    }
    
    public ResultSet getResultSetImpl(final Map<String, Class<?>> map) throws SQLException {
        return this.getResultSetImpl(1L, 0, map);
    }
    
    public ResultSet getResultSetImpl(long index, int count, final Map<String, Class<?>> map) throws SQLException {
        if (map != null && !map.isEmpty()) {
            throw Driver.notImplemented(this.getClass(), "getResultSetImpl(long,int,Map)");
        }
        if (index < 1L) {
            throw new PSQLException(GT.tr("The array index is out of range: {0}", index), PSQLState.DATA_ERROR);
        }
        if (this.fieldBytes != null) {
            return this.readBinaryResultSet(this.fieldBytes, (int)index, count);
        }
        final ArrayDecoding.PgArrayList arrayList = this.buildArrayList(Nullness.castNonNull(this.fieldString));
        if (count == 0) {
            count = arrayList.size();
        }
        if (--index + count > arrayList.size()) {
            throw new PSQLException(GT.tr("The array index is out of range: {0}, number of elements: {1}.", index + count, arrayList.size()), PSQLState.DATA_ERROR);
        }
        final List<Tuple> rows = new ArrayList<Tuple>();
        final Field[] fields = new Field[2];
        if (arrayList.dimensionsCount <= 1) {
            final int baseOid = this.getConnection().getTypeInfo().getPGArrayElement(this.oid);
            fields[0] = new Field("INDEX", 23);
            fields[1] = new Field("VALUE", baseOid);
            for (int i = 0; i < count; ++i) {
                final int offset = (int)index + i;
                final byte[][] t = new byte[2][0];
                final String v = ((ArrayList<String>)arrayList).get(offset);
                t[0] = this.getConnection().encodeString(Integer.toString(offset + 1));
                t[1] = (byte[])((v == null) ? null : this.getConnection().encodeString(v));
                rows.add(new Tuple(t));
            }
        }
        else {
            fields[0] = new Field("INDEX", 23);
            fields[1] = new Field("VALUE", this.oid);
            for (int j = 0; j < count; ++j) {
                final int offset2 = (int)index + j;
                final byte[][] t2 = new byte[2][0];
                final Object v2 = arrayList.get(offset2);
                t2[0] = this.getConnection().encodeString(Integer.toString(offset2 + 1));
                t2[1] = (byte[])((v2 == null) ? null : this.getConnection().encodeString(this.toString((ArrayDecoding.PgArrayList)v2)));
                rows.add(new Tuple(t2));
            }
        }
        final BaseStatement stat = (BaseStatement)this.getConnection().createStatement(1004, 1007);
        return stat.createDriverResultSet(fields, rows);
    }
    
    @Override
    public String toString() {
        if (this.fieldString == null && this.fieldBytes != null) {
            try {
                final Object array = this.readBinaryArray(this.fieldBytes, 1, 0);
                final ArrayEncoding.ArrayEncoder arraySupport = ArrayEncoding.getArrayEncoder(array);
                assert arraySupport != null;
                this.fieldString = arraySupport.toArrayString(this.connection.getTypeInfo().getArrayDelimiter(this.oid), array);
            }
            catch (final SQLException e) {
                this.fieldString = "NULL";
            }
        }
        return this.fieldString;
    }
    
    private String toString(final ArrayDecoding.PgArrayList list) throws SQLException {
        if (list == null) {
            return "NULL";
        }
        final StringBuilder b = new StringBuilder().append('{');
        final char delim = this.getConnection().getTypeInfo().getArrayDelimiter(this.oid);
        for (int i = 0; i < list.size(); ++i) {
            final Object v = list.get(i);
            if (i > 0) {
                b.append(delim);
            }
            if (v == null) {
                b.append("NULL");
            }
            else if (v instanceof ArrayDecoding.PgArrayList) {
                b.append(this.toString((ArrayDecoding.PgArrayList)v));
            }
            else {
                escapeArrayElement(b, (String)v);
            }
        }
        b.append('}');
        return b.toString();
    }
    
    public static void escapeArrayElement(final StringBuilder b, final String s) {
        b.append('\"');
        for (int j = 0; j < s.length(); ++j) {
            final char c = s.charAt(j);
            if (c == '\"' || c == '\\') {
                b.append('\\');
            }
            b.append(c);
        }
        b.append('\"');
    }
    
    public boolean isBinary() {
        return this.fieldBytes != null;
    }
    
    public byte[] toBytes() {
        return this.fieldBytes;
    }
    
    @Override
    public void free() throws SQLException {
        this.connection = null;
        this.fieldString = null;
        this.fieldBytes = null;
        this.arrayList = null;
    }
    
    static {
        ArrayAssistantRegistry.register(2950, new UUIDArrayAssistant());
        ArrayAssistantRegistry.register(2951, new UUIDArrayAssistant());
    }
}
