// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

import java.lang.reflect.Array;
import java.util.HashMap;
import org.postgresql.core.Encoding;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLException;
import org.postgresql.core.BaseConnection;
import org.postgresql.util.ByteConverter;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import java.util.Map;

final class ArrayEncoding
{
    private static final AbstractArrayEncoder<long[]> LONG_ARRAY;
    private static final AbstractArrayEncoder<Long[]> LONG_OBJ_ARRAY;
    private static final AbstractArrayEncoder<int[]> INT_ARRAY;
    private static final AbstractArrayEncoder<Integer[]> INT_OBJ_ARRAY;
    private static final AbstractArrayEncoder<short[]> SHORT_ARRAY;
    private static final AbstractArrayEncoder<Short[]> SHORT_OBJ_ARRAY;
    private static final AbstractArrayEncoder<double[]> DOUBLE_ARRAY;
    private static final AbstractArrayEncoder<Double[]> DOUBLE_OBJ_ARRAY;
    private static final AbstractArrayEncoder<float[]> FLOAT_ARRAY;
    private static final AbstractArrayEncoder<Float[]> FLOAT_OBJ_ARRAY;
    private static final AbstractArrayEncoder<boolean[]> BOOLEAN_ARRAY;
    private static final AbstractArrayEncoder<Boolean[]> BOOLEAN_OBJ_ARRAY;
    private static final AbstractArrayEncoder<String[]> STRING_ARRAY;
    private static final AbstractArrayEncoder<byte[][]> BYTEA_ARRAY;
    private static final AbstractArrayEncoder<Object[]> OBJECT_ARRAY;
    private static final Map<Class, AbstractArrayEncoder> ARRAY_CLASS_TO_ENCODER;
    
    public static <A> ArrayEncoder<A> getArrayEncoder(final A array) throws PSQLException {
        final Class<?> arrayClazz = array.getClass();
        Class<?> subClazz = arrayClazz.getComponentType();
        if (subClazz == null) {
            throw new PSQLException(GT.tr("Invalid elements {0}", array), PSQLState.INVALID_PARAMETER_TYPE);
        }
        AbstractArrayEncoder<A> support = ArrayEncoding.ARRAY_CLASS_TO_ENCODER.get(subClazz);
        if (support != null) {
            return support;
        }
        Class<?> subSubClazz = subClazz.getComponentType();
        if (subSubClazz != null) {
            subClazz = subSubClazz;
            int dimensions = 2;
            while (subClazz != null) {
                support = ArrayEncoding.ARRAY_CLASS_TO_ENCODER.get(subClazz);
                if (support != null) {
                    if (dimensions == 2) {
                        return (ArrayEncoder<A>)new TwoDimensionPrimitiveArrayEncoder((AbstractArrayEncoder<Object>)support);
                    }
                    return new RecursiveArrayEncoder(support, dimensions);
                }
                else {
                    subSubClazz = subClazz.getComponentType();
                    if (subSubClazz == null && Object.class.isAssignableFrom(subClazz)) {
                        if (dimensions == 2) {
                            return (ArrayEncoder<A>)new TwoDimensionPrimitiveArrayEncoder((AbstractArrayEncoder<Object>)ArrayEncoding.OBJECT_ARRAY);
                        }
                        return new RecursiveArrayEncoder(ArrayEncoding.OBJECT_ARRAY, dimensions);
                    }
                    else {
                        ++dimensions;
                        subClazz = subSubClazz;
                    }
                }
            }
            throw new PSQLException(GT.tr("Invalid elements {0}", array), PSQLState.INVALID_PARAMETER_TYPE);
        }
        if (Object.class.isAssignableFrom(subClazz)) {
            return (ArrayEncoder<A>)ArrayEncoding.OBJECT_ARRAY;
        }
        throw new PSQLException(GT.tr("Invalid elements {0}", array), PSQLState.INVALID_PARAMETER_TYPE);
    }
    
    static {
        LONG_ARRAY = new FixedSizePrimitiveArrayEncoder<long[]>(8, 20, 1016) {
            public void appendArray(final StringBuilder sb, final char delim, final long[] array) {
                sb.append('{');
                for (int i = 0; i < array.length; ++i) {
                    if (i > 0) {
                        sb.append(delim);
                    }
                    sb.append(array[i]);
                }
                sb.append('}');
            }
            
            @Override
            protected void write(final long[] array, final byte[] bytes, final int offset) {
                int idx = offset;
                for (int i = 0; i < array.length; ++i) {
                    bytes[idx + 3] = 8;
                    ByteConverter.int8(bytes, idx + 4, array[i]);
                    idx += 12;
                }
            }
        };
        LONG_OBJ_ARRAY = new NumberArrayEncoder<Long>(8, 20, 1016) {
            @Override
            protected void write(final Long number, final byte[] bytes, final int offset) {
                ByteConverter.int8(bytes, offset, number);
            }
        };
        INT_ARRAY = new FixedSizePrimitiveArrayEncoder<int[]>(4, 23, 1007) {
            public void appendArray(final StringBuilder sb, final char delim, final int[] array) {
                sb.append('{');
                for (int i = 0; i < array.length; ++i) {
                    if (i > 0) {
                        sb.append(delim);
                    }
                    sb.append(array[i]);
                }
                sb.append('}');
            }
            
            @Override
            protected void write(final int[] array, final byte[] bytes, final int offset) {
                int idx = offset;
                for (int i = 0; i < array.length; ++i) {
                    bytes[idx + 3] = 4;
                    ByteConverter.int4(bytes, idx + 4, array[i]);
                    idx += 8;
                }
            }
        };
        INT_OBJ_ARRAY = new NumberArrayEncoder<Integer>(4, 23, 1007) {
            @Override
            protected void write(final Integer number, final byte[] bytes, final int offset) {
                ByteConverter.int4(bytes, offset, number);
            }
        };
        SHORT_ARRAY = new FixedSizePrimitiveArrayEncoder<short[]>(2, 21, 1005) {
            public void appendArray(final StringBuilder sb, final char delim, final short[] array) {
                sb.append('{');
                for (int i = 0; i < array.length; ++i) {
                    if (i > 0) {
                        sb.append(delim);
                    }
                    sb.append(array[i]);
                }
                sb.append('}');
            }
            
            @Override
            protected void write(final short[] array, final byte[] bytes, final int offset) {
                int idx = offset;
                for (int i = 0; i < array.length; ++i) {
                    bytes[idx + 3] = 2;
                    ByteConverter.int2(bytes, idx + 4, array[i]);
                    idx += 6;
                }
            }
        };
        SHORT_OBJ_ARRAY = new NumberArrayEncoder<Short>(2, 21, 1005) {
            @Override
            protected void write(final Short number, final byte[] bytes, final int offset) {
                ByteConverter.int2(bytes, offset, number);
            }
        };
        DOUBLE_ARRAY = new FixedSizePrimitiveArrayEncoder<double[]>(8, 701, 1022) {
            public void appendArray(final StringBuilder sb, final char delim, final double[] array) {
                sb.append('{');
                for (int i = 0; i < array.length; ++i) {
                    if (i > 0) {
                        sb.append(delim);
                    }
                    sb.append('\"');
                    sb.append(array[i]);
                    sb.append('\"');
                }
                sb.append('}');
            }
            
            @Override
            protected void write(final double[] array, final byte[] bytes, final int offset) {
                int idx = offset;
                for (int i = 0; i < array.length; ++i) {
                    bytes[idx + 3] = 8;
                    ByteConverter.float8(bytes, idx + 4, array[i]);
                    idx += 12;
                }
            }
        };
        DOUBLE_OBJ_ARRAY = new NumberArrayEncoder<Double>(8, 701, 1022) {
            @Override
            protected void write(final Double number, final byte[] bytes, final int offset) {
                ByteConverter.float8(bytes, offset, number);
            }
        };
        FLOAT_ARRAY = new FixedSizePrimitiveArrayEncoder<float[]>(4, 700, 1021) {
            public void appendArray(final StringBuilder sb, final char delim, final float[] array) {
                sb.append('{');
                for (int i = 0; i < array.length; ++i) {
                    if (i > 0) {
                        sb.append(delim);
                    }
                    sb.append('\"');
                    sb.append(array[i]);
                    sb.append('\"');
                }
                sb.append('}');
            }
            
            @Override
            protected void write(final float[] array, final byte[] bytes, final int offset) {
                int idx = offset;
                for (int i = 0; i < array.length; ++i) {
                    bytes[idx + 3] = 4;
                    ByteConverter.float4(bytes, idx + 4, array[i]);
                    idx += 8;
                }
            }
        };
        FLOAT_OBJ_ARRAY = new NumberArrayEncoder<Float>(4, 700, 1021) {
            @Override
            protected void write(final Float number, final byte[] bytes, final int offset) {
                ByteConverter.float4(bytes, offset, number);
            }
        };
        BOOLEAN_ARRAY = new FixedSizePrimitiveArrayEncoder<boolean[]>(1, 16, 1000) {
            public void appendArray(final StringBuilder sb, final char delim, final boolean[] array) {
                sb.append('{');
                for (int i = 0; i < array.length; ++i) {
                    if (i > 0) {
                        sb.append(delim);
                    }
                    sb.append(array[i] ? '1' : '0');
                }
                sb.append('}');
            }
            
            @Override
            protected void write(final boolean[] array, final byte[] bytes, final int offset) {
                int idx = offset;
                for (int i = 0; i < array.length; ++i) {
                    bytes[idx + 3] = 1;
                    ByteConverter.bool(bytes, idx + 4, array[i]);
                    idx += 5;
                }
            }
        };
        BOOLEAN_OBJ_ARRAY = new AbstractArrayEncoder<Boolean[]>(16, 1000) {
            @Override
            public byte[] toBinaryRepresentation(final BaseConnection connection, final Boolean[] array, final int oid) throws SQLException, SQLFeatureNotSupportedException {
                assert oid == this.arrayOid;
                final int nullCount = this.countNulls(array);
                final byte[] bytes = this.writeBytes(array, nullCount, 20);
                ByteConverter.int4(bytes, 0, 1);
                ByteConverter.int4(bytes, 4, (nullCount != 0) ? 1 : 0);
                ByteConverter.int4(bytes, 8, this.getTypeOID(oid));
                ByteConverter.int4(bytes, 12, array.length);
                ByteConverter.int4(bytes, 16, 1);
                return bytes;
            }
            
            private byte[] writeBytes(final Boolean[] array, final int nullCount, final int offset) {
                final int length = offset + 4 * array.length + (array.length - nullCount);
                final byte[] bytes = new byte[length];
                int idx = offset;
                for (int i = 0; i < array.length; ++i) {
                    if (array[i] == null) {
                        ByteConverter.int4(bytes, idx, -1);
                        idx += 4;
                    }
                    else {
                        ByteConverter.int4(bytes, idx, 1);
                        idx += 4;
                        this.write(array[i], bytes, idx);
                        ++idx;
                    }
                }
                return bytes;
            }
            
            private void write(final Boolean bool, final byte[] bytes, final int idx) {
                ByteConverter.bool(bytes, idx, bool);
            }
            
            @Override
            byte[] toSingleDimensionBinaryRepresentation(final BaseConnection connection, final Boolean[] array) throws SQLException, SQLFeatureNotSupportedException {
                final int nullCount = this.countNulls(array);
                return this.writeBytes(array, nullCount, 0);
            }
            
            @Override
            void appendArray(final StringBuilder sb, final char delim, final Boolean[] array) {
                sb.append('{');
                for (int i = 0; i < array.length; ++i) {
                    if (i != 0) {
                        sb.append(delim);
                    }
                    if (array[i] == null) {
                        sb.append('N').append('U').append('L').append('L');
                    }
                    else {
                        sb.append(array[i] ? '1' : '0');
                    }
                }
                sb.append('}');
            }
        };
        STRING_ARRAY = new AbstractArrayEncoder<String[]>(1043, 1015) {
            @Override
            int countNulls(final String[] array) {
                int count = 0;
                for (int i = 0; i < array.length; ++i) {
                    if (array[i] == null) {
                        ++count;
                    }
                }
                return count;
            }
            
            @Override
            public boolean supportBinaryRepresentation(final int oid) {
                return oid == 1015 || oid == 1009;
            }
            
            @Override
            int getTypeOID(final int arrayOid) {
                if (arrayOid == 1015) {
                    return 1043;
                }
                if (arrayOid == 1009) {
                    return 25;
                }
                throw new IllegalStateException("Invalid array oid: " + arrayOid);
            }
            
            public void appendArray(final StringBuilder sb, final char delim, final String[] array) {
                sb.append('{');
                for (int i = 0; i < array.length; ++i) {
                    if (i > 0) {
                        sb.append(delim);
                    }
                    if (array[i] == null) {
                        sb.append('N').append('U').append('L').append('L');
                    }
                    else {
                        PgArray.escapeArrayElement(sb, array[i]);
                    }
                }
                sb.append('}');
            }
            
            @Override
            public byte[] toBinaryRepresentation(final BaseConnection connection, final String[] array, final int oid) throws SQLException {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.min(1024, array.length * 32 + 20));
                assert this.supportBinaryRepresentation(oid);
                final byte[] buffer = new byte[4];
                try {
                    ByteConverter.int4(buffer, 0, 1);
                    baos.write(buffer);
                    ByteConverter.int4(buffer, 0, (this.countNulls(array) > 0) ? 1 : 0);
                    baos.write(buffer);
                    ByteConverter.int4(buffer, 0, this.getTypeOID(oid));
                    baos.write(buffer);
                    ByteConverter.int4(buffer, 0, array.length);
                    baos.write(buffer);
                    ByteConverter.int4(buffer, 0, 1);
                    baos.write(buffer);
                    final Encoding encoding = connection.getEncoding();
                    for (int i = 0; i < array.length; ++i) {
                        final String string = array[i];
                        if (string != null) {
                            byte[] encoded;
                            try {
                                encoded = encoding.encode(string);
                            }
                            catch (final IOException e) {
                                throw new PSQLException(GT.tr("Unable to translate data into the desired encoding.", new Object[0]), PSQLState.DATA_ERROR, e);
                            }
                            ByteConverter.int4(buffer, 0, encoded.length);
                            baos.write(buffer);
                            baos.write(encoded);
                        }
                        else {
                            ByteConverter.int4(buffer, 0, -1);
                            baos.write(buffer);
                        }
                    }
                    return baos.toByteArray();
                }
                catch (final IOException e2) {
                    throw new AssertionError((Object)e2);
                }
            }
            
            @Override
            byte[] toSingleDimensionBinaryRepresentation(final BaseConnection connection, final String[] array) throws SQLException, SQLFeatureNotSupportedException {
                try {
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.min(1024, array.length * 32 + 20));
                    final byte[] buffer = new byte[4];
                    final Encoding encoding = connection.getEncoding();
                    for (int i = 0; i < array.length; ++i) {
                        final String string = array[i];
                        if (string != null) {
                            byte[] encoded;
                            try {
                                encoded = encoding.encode(string);
                            }
                            catch (final IOException e) {
                                throw new PSQLException(GT.tr("Unable to translate data into the desired encoding.", new Object[0]), PSQLState.DATA_ERROR, e);
                            }
                            ByteConverter.int4(buffer, 0, encoded.length);
                            baos.write(buffer);
                            baos.write(encoded);
                        }
                        else {
                            ByteConverter.int4(buffer, 0, -1);
                            baos.write(buffer);
                        }
                    }
                    return baos.toByteArray();
                }
                catch (final IOException e2) {
                    throw new AssertionError((Object)e2);
                }
            }
        };
        BYTEA_ARRAY = new AbstractArrayEncoder<byte[][]>(17, 1001) {
            private final char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
            
            @Override
            public byte[] toBinaryRepresentation(final BaseConnection connection, final byte[][] array, final int oid) throws SQLException, SQLFeatureNotSupportedException {
                assert oid == this.arrayOid;
                int length = 20;
                for (int i = 0; i < array.length; ++i) {
                    length += 4;
                    if (array[i] != null) {
                        length += array[i].length;
                    }
                }
                final byte[] bytes = new byte[length];
                ByteConverter.int4(bytes, 0, 1);
                ByteConverter.int4(bytes, 4, 0);
                ByteConverter.int4(bytes, 8, this.getTypeOID(oid));
                ByteConverter.int4(bytes, 12, array.length);
                ByteConverter.int4(bytes, 16, 1);
                this.write(array, bytes, 20);
                return bytes;
            }
            
            @Override
            byte[] toSingleDimensionBinaryRepresentation(final BaseConnection connection, final byte[][] array) throws SQLException, SQLFeatureNotSupportedException {
                int length = 0;
                for (int i = 0; i < array.length; ++i) {
                    length += 4;
                    if (array[i] != null) {
                        length += array[i].length;
                    }
                }
                final byte[] bytes = new byte[length];
                this.write(array, bytes, 0);
                return bytes;
            }
            
            @Override
            int countNulls(final byte[][] array) {
                int nulls = 0;
                for (int i = 0; i < array.length; ++i) {
                    if (array[i] == null) {
                        ++nulls;
                    }
                }
                return nulls;
            }
            
            private void write(final byte[][] array, final byte[] bytes, final int offset) {
                int idx = offset;
                for (int i = 0; i < array.length; ++i) {
                    if (array[i] != null) {
                        ByteConverter.int4(bytes, idx, array[i].length);
                        idx += 4;
                        System.arraycopy(array[i], 0, bytes, idx, array[i].length);
                        idx += array[i].length;
                    }
                    else {
                        ByteConverter.int4(bytes, idx, -1);
                        idx += 4;
                    }
                }
            }
            
            @Override
            void appendArray(final StringBuilder sb, final char delim, final byte[][] array) {
                sb.append('{');
                for (int i = 0; i < array.length; ++i) {
                    if (i > 0) {
                        sb.append(delim);
                    }
                    if (array[i] != null) {
                        sb.append("\"\\\\x");
                        for (int j = 0; j < array[i].length; ++j) {
                            final byte b = array[i][j];
                            sb.append(this.hexDigits[(b & 0xF0) >>> 4]);
                            sb.append(this.hexDigits[b & 0xF]);
                        }
                        sb.append('\"');
                    }
                    else {
                        sb.append("NULL");
                    }
                }
                sb.append('}');
            }
        };
        OBJECT_ARRAY = new AbstractArrayEncoder<Object[]>(0, 0) {
            @Override
            public int getDefaultArrayTypeOid() {
                return 0;
            }
            
            @Override
            public boolean supportBinaryRepresentation(final int oid) {
                return false;
            }
            
            @Override
            public byte[] toBinaryRepresentation(final BaseConnection connection, final Object[] array, final int oid) throws SQLException, SQLFeatureNotSupportedException {
                throw new SQLFeatureNotSupportedException();
            }
            
            @Override
            byte[] toSingleDimensionBinaryRepresentation(final BaseConnection connection, final Object[] array) throws SQLException, SQLFeatureNotSupportedException {
                throw new SQLFeatureNotSupportedException();
            }
            
            @Override
            void appendArray(final StringBuilder sb, final char delim, final Object[] array) {
                sb.append('{');
                for (int i = 0; i < array.length; ++i) {
                    if (i > 0) {
                        sb.append(delim);
                    }
                    if (array[i] == null) {
                        sb.append('N').append('U').append('L').append('L');
                    }
                    else {
                        PgArray.escapeArrayElement(sb, array[i].toString());
                    }
                }
                sb.append('}');
            }
        };
        (ARRAY_CLASS_TO_ENCODER = new HashMap<Class, AbstractArrayEncoder>(19)).put(Long.TYPE, ArrayEncoding.LONG_ARRAY);
        ArrayEncoding.ARRAY_CLASS_TO_ENCODER.put(Long.class, ArrayEncoding.LONG_OBJ_ARRAY);
        ArrayEncoding.ARRAY_CLASS_TO_ENCODER.put(Integer.TYPE, ArrayEncoding.INT_ARRAY);
        ArrayEncoding.ARRAY_CLASS_TO_ENCODER.put(Integer.class, ArrayEncoding.INT_OBJ_ARRAY);
        ArrayEncoding.ARRAY_CLASS_TO_ENCODER.put(Short.TYPE, ArrayEncoding.SHORT_ARRAY);
        ArrayEncoding.ARRAY_CLASS_TO_ENCODER.put(Short.class, ArrayEncoding.SHORT_OBJ_ARRAY);
        ArrayEncoding.ARRAY_CLASS_TO_ENCODER.put(Double.TYPE, ArrayEncoding.DOUBLE_ARRAY);
        ArrayEncoding.ARRAY_CLASS_TO_ENCODER.put(Double.class, ArrayEncoding.DOUBLE_OBJ_ARRAY);
        ArrayEncoding.ARRAY_CLASS_TO_ENCODER.put(Float.TYPE, ArrayEncoding.FLOAT_ARRAY);
        ArrayEncoding.ARRAY_CLASS_TO_ENCODER.put(Float.class, ArrayEncoding.FLOAT_OBJ_ARRAY);
        ArrayEncoding.ARRAY_CLASS_TO_ENCODER.put(Boolean.TYPE, ArrayEncoding.BOOLEAN_ARRAY);
        ArrayEncoding.ARRAY_CLASS_TO_ENCODER.put(Boolean.class, ArrayEncoding.BOOLEAN_OBJ_ARRAY);
        ArrayEncoding.ARRAY_CLASS_TO_ENCODER.put(byte[].class, ArrayEncoding.BYTEA_ARRAY);
        ArrayEncoding.ARRAY_CLASS_TO_ENCODER.put(String.class, ArrayEncoding.STRING_ARRAY);
    }
    
    private abstract static class AbstractArrayEncoder<A> implements ArrayEncoder<A>
    {
        private final int oid;
        final int arrayOid;
        
        AbstractArrayEncoder(final int oid, final int arrayOid) {
            this.oid = oid;
            this.arrayOid = arrayOid;
        }
        
        int getTypeOID(final int arrayOid) {
            return this.oid;
        }
        
        @Override
        public int getDefaultArrayTypeOid() {
            return this.arrayOid;
        }
        
        int countNulls(final A array) {
            int nulls = 0;
            for (int arrayLength = Array.getLength(array), i = 0; i < arrayLength; ++i) {
                if (Array.get(array, i) == null) {
                    ++nulls;
                }
            }
            return nulls;
        }
        
        abstract byte[] toSingleDimensionBinaryRepresentation(final BaseConnection p0, final A p1) throws SQLException, SQLFeatureNotSupportedException;
        
        @Override
        public String toArrayString(final char delim, final A array) {
            final StringBuilder sb = new StringBuilder(1024);
            this.appendArray(sb, delim, array);
            return sb.toString();
        }
        
        abstract void appendArray(final StringBuilder p0, final char p1, final A p2);
        
        @Override
        public boolean supportBinaryRepresentation(final int oid) {
            return oid == this.arrayOid;
        }
    }
    
    private abstract static class NumberArrayEncoder<N extends Number> extends AbstractArrayEncoder<N[]>
    {
        private final int fieldSize;
        
        NumberArrayEncoder(final int fieldSize, final int oid, final int arrayOid) {
            super(oid, arrayOid);
            this.fieldSize = fieldSize;
        }
        
        @Override
        final int countNulls(final N[] array) {
            int count = 0;
            for (int i = 0; i < array.length; ++i) {
                if (array[i] == null) {
                    ++count;
                }
            }
            return count;
        }
        
        @Override
        public final byte[] toBinaryRepresentation(final BaseConnection connection, final N[] array, final int oid) throws SQLException, SQLFeatureNotSupportedException {
            assert oid == this.arrayOid;
            final int nullCount = this.countNulls(array);
            final byte[] bytes = this.writeBytes(array, nullCount, 20);
            ByteConverter.int4(bytes, 0, 1);
            ByteConverter.int4(bytes, 4, (nullCount != 0) ? 1 : 0);
            ByteConverter.int4(bytes, 8, this.getTypeOID(oid));
            ByteConverter.int4(bytes, 12, array.length);
            ByteConverter.int4(bytes, 16, 1);
            return bytes;
        }
        
        @Override
        final byte[] toSingleDimensionBinaryRepresentation(final BaseConnection connection, final N[] array) throws SQLException, SQLFeatureNotSupportedException {
            final int nullCount = this.countNulls(array);
            return this.writeBytes(array, nullCount, 0);
        }
        
        private byte[] writeBytes(final N[] array, final int nullCount, final int offset) {
            final int length = offset + 4 * array.length + this.fieldSize * (array.length - nullCount);
            final byte[] bytes = new byte[length];
            int idx = offset;
            for (int i = 0; i < array.length; ++i) {
                if (array[i] == null) {
                    ByteConverter.int4(bytes, idx, -1);
                    idx += 4;
                }
                else {
                    ByteConverter.int4(bytes, idx, this.fieldSize);
                    idx += 4;
                    this.write(array[i], bytes, idx);
                    idx += this.fieldSize;
                }
            }
            return bytes;
        }
        
        protected abstract void write(final N p0, final byte[] p1, final int p2);
        
        @Override
        final void appendArray(final StringBuilder sb, final char delim, final N[] array) {
            sb.append('{');
            for (int i = 0; i < array.length; ++i) {
                if (i != 0) {
                    sb.append(delim);
                }
                if (array[i] == null) {
                    sb.append('N').append('U').append('L').append('L');
                }
                else {
                    sb.append('\"');
                    sb.append(array[i].toString());
                    sb.append('\"');
                }
            }
            sb.append('}');
        }
    }
    
    private abstract static class FixedSizePrimitiveArrayEncoder<A> extends AbstractArrayEncoder<A>
    {
        private final int fieldSize;
        
        FixedSizePrimitiveArrayEncoder(final int fieldSize, final int oid, final int arrayOid) {
            super(oid, arrayOid);
            this.fieldSize = fieldSize;
        }
        
        @Override
        final int countNulls(final A array) {
            return 0;
        }
        
        @Override
        public final byte[] toBinaryRepresentation(final BaseConnection connection, final A array, final int oid) throws SQLException, SQLFeatureNotSupportedException {
            assert oid == this.arrayOid;
            final int arrayLength = Array.getLength(array);
            final int length = 20 + (this.fieldSize + 4) * arrayLength;
            final byte[] bytes = new byte[length];
            ByteConverter.int4(bytes, 0, 1);
            ByteConverter.int4(bytes, 4, 0);
            ByteConverter.int4(bytes, 8, this.getTypeOID(oid));
            ByteConverter.int4(bytes, 12, arrayLength);
            ByteConverter.int4(bytes, 16, 1);
            this.write(array, bytes, 20);
            return bytes;
        }
        
        @Override
        final byte[] toSingleDimensionBinaryRepresentation(final BaseConnection connection, final A array) throws SQLException, SQLFeatureNotSupportedException {
            final int length = (this.fieldSize + 4) * Array.getLength(array);
            final byte[] bytes = new byte[length];
            this.write(array, bytes, 0);
            return bytes;
        }
        
        protected abstract void write(final A p0, final byte[] p1, final int p2);
    }
    
    private static final class TwoDimensionPrimitiveArrayEncoder<A> implements ArrayEncoder<A[]>
    {
        private final AbstractArrayEncoder<A> support;
        
        TwoDimensionPrimitiveArrayEncoder(final AbstractArrayEncoder<A> support) {
            this.support = support;
        }
        
        @Override
        public int getDefaultArrayTypeOid() {
            return this.support.getDefaultArrayTypeOid();
        }
        
        @Override
        public String toArrayString(final char delim, final A[] array) {
            final StringBuilder sb = new StringBuilder(1024);
            sb.append('{');
            for (int i = 0; i < array.length; ++i) {
                if (i > 0) {
                    sb.append(delim);
                }
                this.support.appendArray(sb, delim, array[i]);
            }
            sb.append('}');
            return sb.toString();
        }
        
        @Override
        public boolean supportBinaryRepresentation(final int oid) {
            return this.support.supportBinaryRepresentation(oid);
        }
        
        @Override
        public byte[] toBinaryRepresentation(final BaseConnection connection, final A[] array, final int oid) throws SQLException, SQLFeatureNotSupportedException {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.min(1024, array.length * 32 + 20));
            final byte[] buffer = new byte[4];
            boolean hasNulls = false;
            for (int i = 0; !hasNulls && i < array.length; ++i) {
                if (this.support.countNulls(array[i]) > 0) {
                    hasNulls = true;
                }
            }
            try {
                ByteConverter.int4(buffer, 0, 2);
                baos.write(buffer);
                ByteConverter.int4(buffer, 0, hasNulls ? 1 : 0);
                baos.write(buffer);
                ByteConverter.int4(buffer, 0, this.support.getTypeOID(oid));
                baos.write(buffer);
                ByteConverter.int4(buffer, 0, array.length);
                baos.write(buffer);
                ByteConverter.int4(buffer, 0, 1);
                baos.write(buffer);
                ByteConverter.int4(buffer, 0, (array.length > 0) ? Array.getLength(array[0]) : 0);
                baos.write(buffer);
                ByteConverter.int4(buffer, 0, 1);
                baos.write(buffer);
                for (int i = 0; i < array.length; ++i) {
                    baos.write(this.support.toSingleDimensionBinaryRepresentation(connection, array[i]));
                }
                return baos.toByteArray();
            }
            catch (final IOException e) {
                throw new AssertionError((Object)e);
            }
        }
    }
    
    private static final class RecursiveArrayEncoder implements ArrayEncoder
    {
        private final AbstractArrayEncoder support;
        private final int dimensions;
        
        RecursiveArrayEncoder(final AbstractArrayEncoder support, final int dimensions) {
            this.support = support;
            this.dimensions = dimensions;
            assert dimensions >= 2;
        }
        
        @Override
        public int getDefaultArrayTypeOid() {
            return this.support.getDefaultArrayTypeOid();
        }
        
        @Override
        public String toArrayString(final char delim, final Object array) {
            final StringBuilder sb = new StringBuilder(2048);
            this.arrayString(sb, array, delim, this.dimensions);
            return sb.toString();
        }
        
        private void arrayString(final StringBuilder sb, final Object array, final char delim, final int depth) {
            if (depth > 1) {
                sb.append('{');
                for (int i = 0, j = Array.getLength(array); i < j; ++i) {
                    if (i > 0) {
                        sb.append(delim);
                    }
                    this.arrayString(sb, Array.get(array, i), delim, depth - 1);
                }
                sb.append('}');
            }
            else {
                this.support.appendArray(sb, delim, array);
            }
        }
        
        @Override
        public boolean supportBinaryRepresentation(final int oid) {
            return this.support.supportBinaryRepresentation(oid);
        }
        
        private boolean hasNulls(final Object array, final int depth) {
            if (depth > 1) {
                for (int i = 0, j = Array.getLength(array); i < j; ++i) {
                    if (this.hasNulls(Array.get(array, i), depth - 1)) {
                        return true;
                    }
                }
                return false;
            }
            return this.support.countNulls(array) > 0;
        }
        
        @Override
        public byte[] toBinaryRepresentation(final BaseConnection connection, final Object array, final int oid) throws SQLException, SQLFeatureNotSupportedException {
            final boolean hasNulls = this.hasNulls(array, this.dimensions);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * this.dimensions);
            final byte[] buffer = new byte[4];
            try {
                ByteConverter.int4(buffer, 0, this.dimensions);
                baos.write(buffer);
                ByteConverter.int4(buffer, 0, hasNulls ? 1 : 0);
                baos.write(buffer);
                ByteConverter.int4(buffer, 0, this.support.getTypeOID(oid));
                baos.write(buffer);
                ByteConverter.int4(buffer, 0, Array.getLength(array));
                baos.write(buffer);
                ByteConverter.int4(buffer, 0, 1);
                baos.write(buffer);
                this.writeArray(connection, buffer, baos, array, this.dimensions, true);
                return baos.toByteArray();
            }
            catch (final IOException e) {
                throw new AssertionError((Object)e);
            }
        }
        
        private void writeArray(final BaseConnection connection, final byte[] buffer, final ByteArrayOutputStream baos, final Object array, final int depth, final boolean first) throws IOException, SQLException {
            final int length = Array.getLength(array);
            if (first) {
                ByteConverter.int4(buffer, 0, (length > 0) ? Array.getLength(Array.get(array, 0)) : 0);
                baos.write(buffer);
                ByteConverter.int4(buffer, 0, 1);
                baos.write(buffer);
            }
            for (int i = 0; i < length; ++i) {
                final Object subArray = Array.get(array, i);
                if (depth > 2) {
                    this.writeArray(connection, buffer, baos, subArray, depth - 1, i == 0);
                }
                else {
                    baos.write(this.support.toSingleDimensionBinaryRepresentation(connection, subArray));
                }
            }
        }
    }
    
    public interface ArrayEncoder<A>
    {
        int getDefaultArrayTypeOid();
        
        String toArrayString(final char p0, final A p1);
        
        boolean supportBinaryRepresentation(final int p0);
        
        byte[] toBinaryRepresentation(final BaseConnection p0, final A p1, final int p2) throws SQLException, SQLFeatureNotSupportedException;
    }
}
