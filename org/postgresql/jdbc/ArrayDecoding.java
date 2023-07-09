// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

import java.sql.SQLFeatureNotSupportedException;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Calendar;
import java.io.UnsupportedEncodingException;
import org.postgresql.util.PGbytea;
import java.io.IOException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import java.util.List;
import org.postgresql.core.Parser;
import java.util.ArrayList;
import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import org.postgresql.jdbc2.ArrayAssistant;
import org.postgresql.Driver;
import org.postgresql.jdbc2.ArrayAssistantRegistry;
import org.postgresql.core.BaseConnection;
import java.util.Map;
import java.sql.Timestamp;
import java.sql.Time;
import java.sql.Date;
import java.math.BigDecimal;

final class ArrayDecoding
{
    private static final ArrayDecoder<Long[]> LONG_OBJ_ARRAY;
    private static final ArrayDecoder<Long[]> INT4_UNSIGNED_OBJ_ARRAY;
    private static final ArrayDecoder<Integer[]> INTEGER_OBJ_ARRAY;
    private static final ArrayDecoder<Short[]> SHORT_OBJ_ARRAY;
    private static final ArrayDecoder<Double[]> DOUBLE_OBJ_ARRAY;
    private static final ArrayDecoder<Float[]> FLOAT_OBJ_ARRAY;
    private static final ArrayDecoder<Boolean[]> BOOLEAN_OBJ_ARRAY;
    private static final ArrayDecoder<String[]> STRING_ARRAY;
    private static final ArrayDecoder<byte[][]> BYTE_ARRAY_ARRAY;
    private static final ArrayDecoder<BigDecimal[]> BIG_DECIMAL_STRING_DECODER;
    private static final ArrayDecoder<String[]> STRING_ONLY_DECODER;
    private static final ArrayDecoder<Date[]> DATE_DECODER;
    private static final ArrayDecoder<Time[]> TIME_DECODER;
    private static final ArrayDecoder<Timestamp[]> TIMESTAMP_DECODER;
    private static final Map<Integer, ArrayDecoder> OID_TO_DECODER;
    
    private static <A> ArrayDecoder<A> getDecoder(final int oid, final BaseConnection connection) throws SQLException {
        final Integer key = oid;
        final ArrayDecoder decoder = ArrayDecoding.OID_TO_DECODER.get(key);
        if (decoder != null) {
            return decoder;
        }
        final ArrayAssistant assistant = ArrayAssistantRegistry.getAssistant(oid);
        if (assistant != null) {
            return new ArrayAssistantObjectArrayDecoder(assistant);
        }
        final String typeName = connection.getTypeInfo().getPGType(oid);
        if (typeName == null) {
            throw Driver.notImplemented(PgArray.class, "readArray(data,oid)");
        }
        final int type = connection.getTypeInfo().getSQLType(typeName);
        if (type == 1 || type == 12) {
            return (ArrayDecoder<A>)ArrayDecoding.STRING_ONLY_DECODER;
        }
        return (ArrayDecoder<A>)new MappedTypeObjectArrayDecoder(typeName);
    }
    
    public static Object readBinaryArray(final int index, final int count, final byte[] bytes, final BaseConnection connection) throws SQLException {
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.BIG_ENDIAN);
        final int dimensions = buffer.getInt();
        final boolean hasNulls = buffer.getInt() != 0;
        final int elementOid = buffer.getInt();
        final ArrayDecoder decoder = getDecoder(elementOid, connection);
        if (!decoder.supportBinary()) {
            throw Driver.notImplemented(PgArray.class, "readBinaryArray(data,oid)");
        }
        if (dimensions == 0) {
            return decoder.createArray(0);
        }
        final int adjustedSkipIndex = (index > 0) ? (index - 1) : 0;
        if (dimensions == 1) {
            int length = buffer.getInt();
            buffer.position(buffer.position() + 4);
            if (count > 0) {
                length = Math.min(length, count);
            }
            final Object array = decoder.createArray(length);
            decoder.populateFromBinary(array, adjustedSkipIndex, length, buffer, connection);
            return array;
        }
        final int[] dimensionLengths = new int[dimensions];
        for (int i = 0; i < dimensions; ++i) {
            dimensionLengths[i] = buffer.getInt();
            buffer.position(buffer.position() + 4);
        }
        if (count > 0) {
            dimensionLengths[0] = Math.min(count, dimensionLengths[0]);
        }
        final Object[] array2 = decoder.createMultiDimensionalArray(dimensionLengths);
        storeValues(array2, decoder, buffer, adjustedSkipIndex, dimensionLengths, 0, connection);
        return array2;
    }
    
    private static <A> void storeValues(final A[] array, final ArrayDecoder<A> decoder, final ByteBuffer bytes, final int skip, final int[] dimensionLengths, final int dim, final BaseConnection connection) throws SQLException {
        assert dim <= dimensionLengths.length - 2;
        for (int i = 0; i < skip; ++i) {
            if (dim == dimensionLengths.length - 2) {
                decoder.populateFromBinary(array[0], 0, dimensionLengths[dim + 1], bytes, connection);
            }
            else {
                storeValues((Object[])(Object)array[0], (ArrayDecoder<Object>)decoder, bytes, 0, dimensionLengths, dim + 1, connection);
            }
        }
        for (int i = 0; i < dimensionLengths[dim]; ++i) {
            if (dim == dimensionLengths.length - 2) {
                decoder.populateFromBinary(array[i], 0, dimensionLengths[dim + 1], bytes, connection);
            }
            else {
                storeValues((Object[])(Object)array[i], (ArrayDecoder<Object>)decoder, bytes, 0, dimensionLengths, dim + 1, connection);
            }
        }
    }
    
    static PgArrayList buildArrayList(final String fieldString, final char delim) {
        final PgArrayList arrayList = new PgArrayList();
        if (fieldString == null) {
            return arrayList;
        }
        final char[] chars = fieldString.toCharArray();
        StringBuilder buffer = null;
        boolean insideString = false;
        boolean wasInsideString = false;
        final List<PgArrayList> dims = new ArrayList<PgArrayList>();
        PgArrayList curArray = arrayList;
        int startOffset = 0;
        if (chars[0] == '[') {
            while (chars[startOffset] != '=') {
                ++startOffset;
            }
            ++startOffset;
        }
        for (int i = startOffset; i < chars.length; ++i) {
            if (chars[i] == '\\') {
                ++i;
            }
            else {
                if (!insideString && chars[i] == '{') {
                    if (dims.isEmpty()) {
                        dims.add(arrayList);
                    }
                    else {
                        final PgArrayList a = new PgArrayList();
                        final PgArrayList p = dims.get(dims.size() - 1);
                        ((ArrayList<PgArrayList>)p).add(a);
                        dims.add(a);
                    }
                    curArray = dims.get(dims.size() - 1);
                    for (int t = i + 1; t < chars.length; ++t) {
                        if (!Character.isWhitespace(chars[t])) {
                            if (chars[t] != '{') {
                                break;
                            }
                            final PgArrayList list = curArray;
                            ++list.dimensionsCount;
                        }
                    }
                    buffer = new StringBuilder();
                    continue;
                }
                if (chars[i] == '\"') {
                    insideString = !insideString;
                    wasInsideString = true;
                    continue;
                }
                if (!insideString && Parser.isArrayWhiteSpace(chars[i])) {
                    continue;
                }
                if ((!insideString && (chars[i] == delim || chars[i] == '}')) || i == chars.length - 1) {
                    if (chars[i] != '\"' && chars[i] != '}' && chars[i] != delim && buffer != null) {
                        buffer.append(chars[i]);
                    }
                    final String b = (buffer == null) ? null : buffer.toString();
                    if (b != null && (!b.isEmpty() || wasInsideString)) {
                        ((ArrayList<String>)curArray).add((!wasInsideString && b.equals("NULL")) ? null : b);
                    }
                    wasInsideString = false;
                    buffer = new StringBuilder();
                    if (chars[i] == '}') {
                        dims.remove(dims.size() - 1);
                        if (!dims.isEmpty()) {
                            curArray = dims.get(dims.size() - 1);
                        }
                        buffer = null;
                    }
                    continue;
                }
            }
            if (buffer != null) {
                buffer.append(chars[i]);
            }
        }
        return arrayList;
    }
    
    public static Object readStringArray(final int index, final int count, final int oid, final PgArrayList list, final BaseConnection connection) throws SQLException {
        final ArrayDecoder decoder = getDecoder(oid, connection);
        final int dims = list.dimensionsCount;
        if (dims == 0) {
            return decoder.createArray(0);
        }
        boolean sublist = false;
        int adjustedSkipIndex = 0;
        if (index > 1) {
            sublist = true;
            adjustedSkipIndex = index - 1;
        }
        int adjustedCount = list.size();
        if (count > 0 && count != adjustedCount) {
            sublist = true;
            adjustedCount = Math.min(adjustedCount, count);
        }
        final List adjustedList = sublist ? list.subList(adjustedSkipIndex, adjustedSkipIndex + adjustedCount) : list;
        if (dims == 1) {
            int length = adjustedList.size();
            if (count > 0) {
                length = Math.min(length, count);
            }
            final Object array = decoder.createArray(length);
            decoder.populateFromString(array, adjustedList, connection);
            return array;
        }
        final int[] dimensionLengths = new int[dims];
        dimensionLengths[0] = adjustedCount;
        List tmpList = adjustedList.get(0);
        for (int i = 1; i < dims; ++i) {
            dimensionLengths[i] = tmpList.size();
            if (i != dims - 1) {
                tmpList = tmpList.get(0);
            }
        }
        final Object[] array2 = decoder.createMultiDimensionalArray(dimensionLengths);
        storeStringValues(array2, decoder, adjustedList, dimensionLengths, 0, connection);
        return array2;
    }
    
    private static <A> void storeStringValues(final A[] array, final ArrayDecoder<A> decoder, final List list, final int[] dimensionLengths, final int dim, final BaseConnection connection) throws SQLException {
        assert dim <= dimensionLengths.length - 2;
        for (int i = 0; i < dimensionLengths[dim]; ++i) {
            if (dim == dimensionLengths.length - 2) {
                decoder.populateFromString(array[i], list.get(i), connection);
            }
            else {
                storeStringValues((Object[])(Object)array[i], (ArrayDecoder<Object>)decoder, list.get(i), dimensionLengths, dim + 1, connection);
            }
        }
    }
    
    static {
        LONG_OBJ_ARRAY = new AbstractObjectArrayDecoder<Long[]>(Long.class) {
            @Override
            Object parseValue(final int length, final ByteBuffer bytes, final BaseConnection connection) {
                return bytes.getLong();
            }
            
            @Override
            Object parseValue(final String stringVal, final BaseConnection connection) throws SQLException {
                return PgResultSet.toLong(stringVal);
            }
        };
        INT4_UNSIGNED_OBJ_ARRAY = new AbstractObjectArrayDecoder<Long[]>(Long.class) {
            @Override
            Object parseValue(final int length, final ByteBuffer bytes, final BaseConnection connection) {
                final long value = (long)bytes.getInt() & 0xFFFFFFFFL;
                return value;
            }
            
            @Override
            Object parseValue(final String stringVal, final BaseConnection connection) throws SQLException {
                return PgResultSet.toLong(stringVal);
            }
        };
        INTEGER_OBJ_ARRAY = new AbstractObjectArrayDecoder<Integer[]>(Integer.class) {
            @Override
            Object parseValue(final int length, final ByteBuffer bytes, final BaseConnection connection) {
                return bytes.getInt();
            }
            
            @Override
            Object parseValue(final String stringVal, final BaseConnection connection) throws SQLException {
                return PgResultSet.toInt(stringVal);
            }
        };
        SHORT_OBJ_ARRAY = new AbstractObjectArrayDecoder<Short[]>(Short.class) {
            @Override
            Object parseValue(final int length, final ByteBuffer bytes, final BaseConnection connection) {
                return bytes.getShort();
            }
            
            @Override
            Object parseValue(final String stringVal, final BaseConnection connection) throws SQLException {
                return PgResultSet.toShort(stringVal);
            }
        };
        DOUBLE_OBJ_ARRAY = new AbstractObjectArrayDecoder<Double[]>(Double.class) {
            @Override
            Object parseValue(final int length, final ByteBuffer bytes, final BaseConnection connection) {
                return bytes.getDouble();
            }
            
            @Override
            Object parseValue(final String stringVal, final BaseConnection connection) throws SQLException {
                return PgResultSet.toDouble(stringVal);
            }
        };
        FLOAT_OBJ_ARRAY = new AbstractObjectArrayDecoder<Float[]>(Float.class) {
            @Override
            Object parseValue(final int length, final ByteBuffer bytes, final BaseConnection connection) {
                return bytes.getFloat();
            }
            
            @Override
            Object parseValue(final String stringVal, final BaseConnection connection) throws SQLException {
                return PgResultSet.toFloat(stringVal);
            }
        };
        BOOLEAN_OBJ_ARRAY = new AbstractObjectArrayDecoder<Boolean[]>(Boolean.class) {
            @Override
            Object parseValue(final int length, final ByteBuffer bytes, final BaseConnection connection) {
                return bytes.get() == 1;
            }
            
            @Override
            Object parseValue(final String stringVal, final BaseConnection connection) throws SQLException {
                return BooleanTypeUtil.fromString(stringVal);
            }
        };
        STRING_ARRAY = new AbstractObjectArrayDecoder<String[]>(String.class) {
            @Override
            Object parseValue(final int length, final ByteBuffer bytes, final BaseConnection connection) throws SQLException {
                assert bytes.hasArray();
                final byte[] byteArray = bytes.array();
                final int offset = bytes.arrayOffset() + bytes.position();
                String val;
                try {
                    val = connection.getEncoding().decode(byteArray, offset, length);
                }
                catch (final IOException e) {
                    throw new PSQLException(GT.tr("Invalid character data was found.  This is most likely caused by stored data containing characters that are invalid for the character set the database was created in.  The most common example of this is storing 8bit data in a SQL_ASCII database.", new Object[0]), PSQLState.DATA_ERROR, e);
                }
                bytes.position(bytes.position() + length);
                return val;
            }
            
            @Override
            Object parseValue(final String stringVal, final BaseConnection connection) throws SQLException {
                return stringVal;
            }
        };
        BYTE_ARRAY_ARRAY = new AbstractObjectArrayDecoder<byte[][]>(byte[].class) {
            @Override
            Object parseValue(final int length, final ByteBuffer bytes, final BaseConnection connection) throws SQLException {
                final byte[] array = new byte[length];
                bytes.get(array);
                return array;
            }
            
            @Override
            Object parseValue(final String stringVal, final BaseConnection connection) throws SQLException {
                try {
                    return PGbytea.toBytes(stringVal.getBytes("ascii"));
                }
                catch (final UnsupportedEncodingException e) {
                    throw new Error("ascii must be supported");
                }
            }
        };
        BIG_DECIMAL_STRING_DECODER = new AbstractObjectStringArrayDecoder<BigDecimal[]>(BigDecimal.class) {
            @Override
            Object parseValue(final String stringVal, final BaseConnection connection) throws SQLException {
                return PgResultSet.toBigDecimal(stringVal);
            }
        };
        STRING_ONLY_DECODER = new AbstractObjectStringArrayDecoder<String[]>(String.class) {
            @Override
            Object parseValue(final String stringVal, final BaseConnection connection) throws SQLException {
                return stringVal;
            }
        };
        DATE_DECODER = new AbstractObjectStringArrayDecoder<Date[]>(Date.class) {
            @Override
            Object parseValue(final String stringVal, final BaseConnection connection) throws SQLException {
                return connection.getTimestampUtils().toDate(null, stringVal);
            }
        };
        TIME_DECODER = new AbstractObjectStringArrayDecoder<Time[]>(Time.class) {
            @Override
            Object parseValue(final String stringVal, final BaseConnection connection) throws SQLException {
                return connection.getTimestampUtils().toTime(null, stringVal);
            }
        };
        TIMESTAMP_DECODER = new AbstractObjectStringArrayDecoder<Timestamp[]>(Timestamp.class) {
            @Override
            Object parseValue(final String stringVal, final BaseConnection connection) throws SQLException {
                return connection.getTimestampUtils().toTimestamp(null, stringVal);
            }
        };
        (OID_TO_DECODER = new HashMap<Integer, ArrayDecoder>(29)).put(26, ArrayDecoding.INT4_UNSIGNED_OBJ_ARRAY);
        ArrayDecoding.OID_TO_DECODER.put(20, ArrayDecoding.LONG_OBJ_ARRAY);
        ArrayDecoding.OID_TO_DECODER.put(23, ArrayDecoding.INTEGER_OBJ_ARRAY);
        ArrayDecoding.OID_TO_DECODER.put(21, ArrayDecoding.SHORT_OBJ_ARRAY);
        ArrayDecoding.OID_TO_DECODER.put(790, ArrayDecoding.DOUBLE_OBJ_ARRAY);
        ArrayDecoding.OID_TO_DECODER.put(701, ArrayDecoding.DOUBLE_OBJ_ARRAY);
        ArrayDecoding.OID_TO_DECODER.put(700, ArrayDecoding.FLOAT_OBJ_ARRAY);
        ArrayDecoding.OID_TO_DECODER.put(25, ArrayDecoding.STRING_ARRAY);
        ArrayDecoding.OID_TO_DECODER.put(1043, ArrayDecoding.STRING_ARRAY);
        ArrayDecoding.OID_TO_DECODER.put(3802, ArrayDecoding.STRING_ONLY_DECODER);
        ArrayDecoding.OID_TO_DECODER.put(1560, ArrayDecoding.BOOLEAN_OBJ_ARRAY);
        ArrayDecoding.OID_TO_DECODER.put(16, ArrayDecoding.BOOLEAN_OBJ_ARRAY);
        ArrayDecoding.OID_TO_DECODER.put(17, ArrayDecoding.BYTE_ARRAY_ARRAY);
        ArrayDecoding.OID_TO_DECODER.put(1700, ArrayDecoding.BIG_DECIMAL_STRING_DECODER);
        ArrayDecoding.OID_TO_DECODER.put(1042, ArrayDecoding.STRING_ONLY_DECODER);
        ArrayDecoding.OID_TO_DECODER.put(18, ArrayDecoding.STRING_ONLY_DECODER);
        ArrayDecoding.OID_TO_DECODER.put(1082, ArrayDecoding.DATE_DECODER);
        ArrayDecoding.OID_TO_DECODER.put(1083, ArrayDecoding.TIME_DECODER);
        ArrayDecoding.OID_TO_DECODER.put(1266, ArrayDecoding.TIME_DECODER);
        ArrayDecoding.OID_TO_DECODER.put(1114, ArrayDecoding.TIMESTAMP_DECODER);
        ArrayDecoding.OID_TO_DECODER.put(1184, ArrayDecoding.TIMESTAMP_DECODER);
    }
    
    static final class PgArrayList extends ArrayList<Object>
    {
        private static final long serialVersionUID = 1L;
        int dimensionsCount;
        
        PgArrayList() {
            this.dimensionsCount = 1;
        }
    }
    
    private abstract static class AbstractObjectStringArrayDecoder<A> implements ArrayDecoder<A>
    {
        final Class<?> baseClazz;
        
        AbstractObjectStringArrayDecoder(final Class<?> baseClazz) {
            this.baseClazz = baseClazz;
        }
        
        @Override
        public boolean supportBinary() {
            return false;
        }
        
        @Override
        public A createArray(final int size) {
            return (A)Array.newInstance(this.baseClazz, size);
        }
        
        @Override
        public Object[] createMultiDimensionalArray(final int[] sizes) {
            return (Object[])Array.newInstance(this.baseClazz, sizes);
        }
        
        @Override
        public void populateFromBinary(final A arr, final int index, final int count, final ByteBuffer bytes, final BaseConnection connection) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public void populateFromString(final A arr, final List<String> strings, final BaseConnection connection) throws SQLException {
            final Object[] array = (Object)arr;
            for (int i = 0, j = strings.size(); i < j; ++i) {
                final String stringVal = strings.get(i);
                array[i] = ((stringVal != null) ? this.parseValue(stringVal, connection) : null);
            }
        }
        
        abstract Object parseValue(final String p0, final BaseConnection p1) throws SQLException;
    }
    
    private abstract static class AbstractObjectArrayDecoder<A> extends AbstractObjectStringArrayDecoder<A>
    {
        AbstractObjectArrayDecoder(final Class<?> baseClazz) {
            super(baseClazz);
        }
        
        @Override
        public boolean supportBinary() {
            return true;
        }
        
        @Override
        public void populateFromBinary(final A arr, final int index, final int count, final ByteBuffer bytes, final BaseConnection connection) throws SQLException {
            final Object[] array = (Object)arr;
            for (int i = 0; i < index; ++i) {
                final int length = bytes.getInt();
                if (length > 0) {
                    bytes.position(bytes.position() + length);
                }
            }
            for (int i = 0; i < count; ++i) {
                final int length = bytes.getInt();
                if (length != -1) {
                    array[i] = this.parseValue(length, bytes, connection);
                }
                else {
                    array[i] = null;
                }
            }
        }
        
        abstract Object parseValue(final int p0, final ByteBuffer p1, final BaseConnection p2) throws SQLException;
    }
    
    private static final class ArrayAssistantObjectArrayDecoder extends AbstractObjectArrayDecoder
    {
        private final ArrayAssistant arrayAssistant;
        
        ArrayAssistantObjectArrayDecoder(final ArrayAssistant arrayAssistant) {
            super(arrayAssistant.baseType());
            this.arrayAssistant = arrayAssistant;
        }
        
        @Override
        Object parseValue(final int length, final ByteBuffer bytes, final BaseConnection connection) throws SQLException {
            assert bytes.hasArray();
            final byte[] byteArray = bytes.array();
            final int offset = bytes.arrayOffset() + bytes.position();
            final Object val = this.arrayAssistant.buildElement(byteArray, offset, length);
            bytes.position(bytes.position() + length);
            return val;
        }
        
        @Override
        Object parseValue(final String stringVal, final BaseConnection connection) throws SQLException {
            return this.arrayAssistant.buildElement(stringVal);
        }
    }
    
    private static final class MappedTypeObjectArrayDecoder extends AbstractObjectArrayDecoder<Object[]>
    {
        private final String typeName;
        
        MappedTypeObjectArrayDecoder(final String baseTypeName) {
            super(Object.class);
            this.typeName = baseTypeName;
        }
        
        @Override
        Object parseValue(final int length, final ByteBuffer bytes, final BaseConnection connection) throws SQLException {
            final byte[] copy = new byte[length];
            bytes.get(copy);
            return connection.getObject(this.typeName, null, copy);
        }
        
        @Override
        Object parseValue(final String stringVal, final BaseConnection connection) throws SQLException {
            return connection.getObject(this.typeName, stringVal, null);
        }
    }
    
    private interface ArrayDecoder<A>
    {
        A createArray(final int p0);
        
        Object[] createMultiDimensionalArray(final int[] p0);
        
        boolean supportBinary();
        
        void populateFromBinary(final A p0, final int p1, final int p2, final ByteBuffer p3, final BaseConnection p4) throws SQLException;
        
        void populateFromString(final A p0, final List<String> p1, final BaseConnection p2) throws SQLException;
    }
}
