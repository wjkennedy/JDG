// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core.v3;

import java.util.Arrays;
import org.postgresql.core.ParameterList;
import java.io.IOException;
import org.postgresql.core.PGStream;
import org.postgresql.geometric.PGbox;
import org.postgresql.geometric.PGpoint;
import org.postgresql.jdbc.UUIDArrayAssistant;
import org.postgresql.core.Utils;
import org.postgresql.util.ByteStreamWriter;
import java.io.InputStream;
import org.postgresql.util.StreamWrapper;
import org.postgresql.util.ByteConverter;
import java.sql.SQLException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;

class SimpleParameterList implements V3ParameterList
{
    private static final byte IN = 1;
    private static final byte OUT = 2;
    private static final byte INOUT = 3;
    private static final byte TEXT = 0;
    private static final byte BINARY = 4;
    private final Object[] paramValues;
    private final int[] paramTypes;
    private final byte[] flags;
    private final byte[][] encoded;
    private final TypeTransferModeRegistry transferModeRegistry;
    private static final Object NULL_OBJECT;
    private int pos;
    
    SimpleParameterList(final int paramCount, final TypeTransferModeRegistry transferModeRegistry) {
        this.pos = 0;
        this.paramValues = new Object[paramCount];
        this.paramTypes = new int[paramCount];
        this.encoded = new byte[paramCount][];
        this.flags = new byte[paramCount];
        this.transferModeRegistry = transferModeRegistry;
    }
    
    @Override
    public void registerOutParameter(final int index, final int sqlType) throws SQLException {
        if (index < 1 || index > this.paramValues.length) {
            throw new PSQLException(GT.tr("The column index is out of range: {0}, number of columns: {1}.", index, this.paramValues.length), PSQLState.INVALID_PARAMETER_VALUE);
        }
        final byte[] flags = this.flags;
        final int n = index - 1;
        flags[n] |= 0x2;
    }
    
    private void bind(int index, final Object value, final int oid, final byte binary) throws SQLException {
        if (index < 1 || index > this.paramValues.length) {
            throw new PSQLException(GT.tr("The column index is out of range: {0}, number of columns: {1}.", index, this.paramValues.length), PSQLState.INVALID_PARAMETER_VALUE);
        }
        --index;
        this.encoded[index] = null;
        this.paramValues[index] = value;
        this.flags[index] = (byte)(this.direction(index) | 0x1 | binary);
        if (oid == 0 && this.paramTypes[index] != 0 && value == SimpleParameterList.NULL_OBJECT) {
            return;
        }
        this.paramTypes[index] = oid;
        this.pos = index + 1;
    }
    
    @Override
    public int getParameterCount() {
        return this.paramValues.length;
    }
    
    @Override
    public int getOutParameterCount() {
        int count = 0;
        for (int i = 0; i < this.paramTypes.length; ++i) {
            if ((this.direction(i) & 0x2) == 0x2) {
                ++count;
            }
        }
        if (count == 0) {
            count = 1;
        }
        return count;
    }
    
    @Override
    public int getInParameterCount() {
        int count = 0;
        for (int i = 0; i < this.paramTypes.length; ++i) {
            if (this.direction(i) != 2) {
                ++count;
            }
        }
        return count;
    }
    
    @Override
    public void setIntParameter(final int index, final int value) throws SQLException {
        final byte[] data = new byte[4];
        ByteConverter.int4(data, 0, value);
        this.bind(index, data, 23, (byte)4);
    }
    
    @Override
    public void setLiteralParameter(final int index, final String value, final int oid) throws SQLException {
        this.bind(index, value, oid, (byte)0);
    }
    
    @Override
    public void setStringParameter(final int index, final String value, final int oid) throws SQLException {
        this.bind(index, value, oid, (byte)0);
    }
    
    @Override
    public void setBinaryParameter(final int index, final byte[] value, final int oid) throws SQLException {
        this.bind(index, value, oid, (byte)4);
    }
    
    @Override
    public void setBytea(final int index, final byte[] data, final int offset, final int length) throws SQLException {
        this.bind(index, new StreamWrapper(data, offset, length), 17, (byte)4);
    }
    
    @Override
    public void setBytea(final int index, final InputStream stream, final int length) throws SQLException {
        this.bind(index, new StreamWrapper(stream, length), 17, (byte)4);
    }
    
    @Override
    public void setBytea(final int index, final InputStream stream) throws SQLException {
        this.bind(index, new StreamWrapper(stream), 17, (byte)4);
    }
    
    @Override
    public void setBytea(final int index, final ByteStreamWriter writer) throws SQLException {
        this.bind(index, writer, 17, (byte)4);
    }
    
    @Override
    public void setText(final int index, final InputStream stream) throws SQLException {
        this.bind(index, new StreamWrapper(stream), 25, (byte)0);
    }
    
    @Override
    public void setNull(final int index, final int oid) throws SQLException {
        byte binaryTransfer = 0;
        if (this.transferModeRegistry != null && this.transferModeRegistry.useBinaryForReceive(oid)) {
            binaryTransfer = 4;
        }
        this.bind(index, SimpleParameterList.NULL_OBJECT, oid, binaryTransfer);
    }
    
    @Override
    public String toString(int index, final boolean standardConformingStrings) {
        --index;
        final Object paramValue = this.paramValues[index];
        if (paramValue == null) {
            return "?";
        }
        if (paramValue == SimpleParameterList.NULL_OBJECT) {
            return "NULL";
        }
        if ((this.flags[index] & 0x4) != 0x4) {
            final String param = paramValue.toString();
            StringBuilder p = new StringBuilder(3 + (param.length() + 10) / 10 * 11);
            p.append('\'');
            try {
                p = Utils.escapeLiteral(p, param, standardConformingStrings);
            }
            catch (final SQLException sqle) {
                p.append(param);
            }
            p.append('\'');
            final int paramType = this.paramTypes[index];
            if (paramType == 1114) {
                p.append("::timestamp");
            }
            else if (paramType == 1184) {
                p.append("::timestamp with time zone");
            }
            else if (paramType == 1083) {
                p.append("::time");
            }
            else if (paramType == 1266) {
                p.append("::time with time zone");
            }
            else if (paramType == 1082) {
                p.append("::date");
            }
            else if (paramType == 1186) {
                p.append("::interval");
            }
            else if (paramType == 1700) {
                p.append("::numeric");
            }
            return p.toString();
        }
        switch (this.paramTypes[index]) {
            case 21: {
                final short s = ByteConverter.int2((byte[])paramValue, 0);
                return Short.toString(s);
            }
            case 23: {
                final int i = ByteConverter.int4((byte[])paramValue, 0);
                return Integer.toString(i);
            }
            case 20: {
                final long l = ByteConverter.int8((byte[])paramValue, 0);
                return Long.toString(l);
            }
            case 700: {
                final float f = ByteConverter.float4((byte[])paramValue, 0);
                if (Float.isNaN(f)) {
                    return "'NaN'::real";
                }
                return Float.toString(f);
            }
            case 701: {
                final double d = ByteConverter.float8((byte[])paramValue, 0);
                if (Double.isNaN(d)) {
                    return "'NaN'::double precision";
                }
                return Double.toString(d);
            }
            case 2950: {
                final String uuid = new UUIDArrayAssistant().buildElement((byte[])paramValue, 0, 16).toString();
                return "'" + uuid + "'::uuid";
            }
            case 600: {
                final PGpoint pgPoint = new PGpoint();
                pgPoint.setByteValue((byte[])paramValue, 0);
                return "'" + pgPoint.toString() + "'::point";
            }
            case 603: {
                final PGbox pgBox = new PGbox();
                pgBox.setByteValue((byte[])paramValue, 0);
                return "'" + pgBox.toString() + "'::box";
            }
            default: {
                return "?";
            }
        }
    }
    
    @Override
    public void checkAllParametersSet() throws SQLException {
        for (int i = 0; i < this.paramTypes.length; ++i) {
            if (this.direction(i) != 2 && this.paramValues[i] == null) {
                throw new PSQLException(GT.tr("No value specified for parameter {0}.", i + 1), PSQLState.INVALID_PARAMETER_VALUE);
            }
        }
    }
    
    @Override
    public void convertFunctionOutParameters() {
        for (int i = 0; i < this.paramTypes.length; ++i) {
            if (this.direction(i) == 2) {
                this.paramTypes[i] = 2278;
                this.paramValues[i] = "null";
            }
        }
    }
    
    private static void streamBytea(final PGStream pgStream, final StreamWrapper wrapper) throws IOException {
        final byte[] rawData = wrapper.getBytes();
        if (rawData != null) {
            pgStream.send(rawData, wrapper.getOffset(), wrapper.getLength());
            return;
        }
        pgStream.sendStream(wrapper.getStream(), wrapper.getLength());
    }
    
    private static void streamBytea(final PGStream pgStream, final ByteStreamWriter writer) throws IOException {
        pgStream.send(writer);
    }
    
    @Override
    public int[] getTypeOIDs() {
        return this.paramTypes;
    }
    
    int getTypeOID(final int index) {
        return this.paramTypes[index - 1];
    }
    
    boolean hasUnresolvedTypes() {
        for (final int paramType : this.paramTypes) {
            if (paramType == 0) {
                return true;
            }
        }
        return false;
    }
    
    void setResolvedType(final int index, final int oid) {
        if (this.paramTypes[index - 1] == 0) {
            this.paramTypes[index - 1] = oid;
        }
        else if (this.paramTypes[index - 1] != oid) {
            throw new IllegalArgumentException("Can't change resolved type for param: " + index + " from " + this.paramTypes[index - 1] + " to " + oid);
        }
    }
    
    boolean isNull(final int index) {
        return this.paramValues[index - 1] == SimpleParameterList.NULL_OBJECT;
    }
    
    boolean isBinary(final int index) {
        return (this.flags[index - 1] & 0x4) != 0x0;
    }
    
    private byte direction(final int index) {
        return (byte)(this.flags[index] & 0x3);
    }
    
    int getV3Length(int index) {
        --index;
        final Object value = this.paramValues[index];
        if (value == null || value == SimpleParameterList.NULL_OBJECT) {
            throw new IllegalArgumentException("can't getV3Length() on a null parameter");
        }
        if (value instanceof byte[]) {
            return ((byte[])value).length;
        }
        if (value instanceof StreamWrapper) {
            return ((StreamWrapper)value).getLength();
        }
        if (value instanceof ByteStreamWriter) {
            return ((ByteStreamWriter)value).getLength();
        }
        byte[] encoded = this.encoded[index];
        if (encoded == null) {
            encoded = (this.encoded[index] = Utils.encodeUTF8(value.toString()));
        }
        return encoded.length;
    }
    
    void writeV3Value(int index, final PGStream pgStream) throws IOException {
        --index;
        final Object paramValue = this.paramValues[index];
        if (paramValue == null || paramValue == SimpleParameterList.NULL_OBJECT) {
            throw new IllegalArgumentException("can't writeV3Value() on a null parameter");
        }
        if (paramValue instanceof byte[]) {
            pgStream.send((byte[])paramValue);
            return;
        }
        if (paramValue instanceof StreamWrapper) {
            streamBytea(pgStream, (StreamWrapper)paramValue);
            return;
        }
        if (paramValue instanceof ByteStreamWriter) {
            streamBytea(pgStream, (ByteStreamWriter)paramValue);
            return;
        }
        if (this.encoded[index] == null) {
            this.encoded[index] = Utils.encodeUTF8((String)paramValue);
        }
        pgStream.send(this.encoded[index]);
    }
    
    @Override
    public ParameterList copy() {
        final SimpleParameterList newCopy = new SimpleParameterList(this.paramValues.length, this.transferModeRegistry);
        System.arraycopy(this.paramValues, 0, newCopy.paramValues, 0, this.paramValues.length);
        System.arraycopy(this.paramTypes, 0, newCopy.paramTypes, 0, this.paramTypes.length);
        System.arraycopy(this.flags, 0, newCopy.flags, 0, this.flags.length);
        newCopy.pos = this.pos;
        return newCopy;
    }
    
    @Override
    public void clear() {
        Arrays.fill(this.paramValues, null);
        Arrays.fill(this.paramTypes, 0);
        Arrays.fill(this.encoded, null);
        Arrays.fill(this.flags, (byte)0);
        this.pos = 0;
    }
    
    @Override
    public SimpleParameterList[] getSubparams() {
        return null;
    }
    
    @Override
    public Object[] getValues() {
        return this.paramValues;
    }
    
    @Override
    public int[] getParamTypes() {
        return this.paramTypes;
    }
    
    @Override
    public byte[] getFlags() {
        return this.flags;
    }
    
    @Override
    public byte[][] getEncoding() {
        return this.encoded;
    }
    
    @Override
    public void appendAll(final ParameterList list) throws SQLException {
        if (list instanceof SimpleParameterList) {
            final SimpleParameterList spl = (SimpleParameterList)list;
            final int inParamCount = spl.getInParameterCount();
            if (this.pos + inParamCount > this.paramValues.length) {
                throw new PSQLException(GT.tr("Added parameters index out of range: {0}, number of columns: {1}.", this.pos + inParamCount, this.paramValues.length), PSQLState.INVALID_PARAMETER_VALUE);
            }
            System.arraycopy(spl.getValues(), 0, this.paramValues, this.pos, inParamCount);
            System.arraycopy(spl.getParamTypes(), 0, this.paramTypes, this.pos, inParamCount);
            System.arraycopy(spl.getFlags(), 0, this.flags, this.pos, inParamCount);
            System.arraycopy(spl.getEncoding(), 0, this.encoded, this.pos, inParamCount);
            this.pos += inParamCount;
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder ts = new StringBuilder("<[");
        if (this.paramValues.length > 0) {
            ts.append(this.toString(1, true));
            for (int c = 2; c <= this.paramValues.length; ++c) {
                ts.append(" ,").append(this.toString(c, true));
            }
        }
        ts.append("]>");
        return ts.toString();
    }
    
    static {
        NULL_OBJECT = new Object();
    }
}
