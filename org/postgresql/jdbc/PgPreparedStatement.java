// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

import java.net.URL;
import java.sql.NClob;
import org.postgresql.util.ReaderInputStream;
import java.sql.RowId;
import java.sql.SQLType;
import java.sql.ParameterMetaData;
import org.postgresql.core.BaseConnection;
import org.postgresql.util.PGTime;
import org.postgresql.Driver;
import java.sql.Ref;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.io.Reader;
import java.io.OutputStream;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;
import org.postgresql.core.ResultHandler;
import java.sql.ResultSetMetaData;
import org.postgresql.core.v3.BatchedQuery;
import org.postgresql.core.Query;
import java.util.ArrayList;
import java.math.RoundingMode;
import java.math.BigInteger;
import org.postgresql.core.TypeInfo;
import org.postgresql.util.internal.Nullness;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Array;
import java.sql.Clob;
import java.sql.Blob;
import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import org.postgresql.util.PGTimestamp;
import java.time.LocalTime;
import java.time.LocalDate;
import java.sql.SQLXML;
import org.postgresql.core.Version;
import org.postgresql.core.ServerVersion;
import java.util.UUID;
import org.postgresql.util.HStoreConverter;
import java.util.Map;
import org.postgresql.util.PGBinaryObject;
import org.postgresql.util.PGobject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.sql.Timestamp;
import java.sql.Time;
import java.util.Calendar;
import java.sql.Date;
import org.postgresql.util.ByteStreamWriter;
import java.math.BigDecimal;
import org.postgresql.util.ByteConverter;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TimeZone;
import org.postgresql.core.ParameterList;
import org.postgresql.core.CachedQuery;
import java.sql.PreparedStatement;

class PgPreparedStatement extends PgStatement implements PreparedStatement
{
    protected final CachedQuery preparedQuery;
    protected final ParameterList preparedParameters;
    private TimeZone defaultTimeZone;
    
    PgPreparedStatement(final PgConnection connection, final String sql, final int rsType, final int rsConcurrency, final int rsHoldability) throws SQLException {
        this(connection, connection.borrowQuery(sql), rsType, rsConcurrency, rsHoldability);
    }
    
    PgPreparedStatement(final PgConnection connection, final CachedQuery query, final int rsType, final int rsConcurrency, final int rsHoldability) throws SQLException {
        super(connection, rsType, rsConcurrency, rsHoldability);
        this.preparedQuery = query;
        this.preparedParameters = this.preparedQuery.query.createParameterList();
        this.setPoolable(true);
    }
    
    @Override
    public ResultSet executeQuery(final String sql) throws SQLException {
        throw new PSQLException(GT.tr("Can''t use query methods that take a query string on a PreparedStatement.", new Object[0]), PSQLState.WRONG_OBJECT_TYPE);
    }
    
    @Override
    public ResultSet executeQuery() throws SQLException {
        if (!this.executeWithFlags(0)) {
            throw new PSQLException(GT.tr("No results were returned by the query.", new Object[0]), PSQLState.NO_DATA);
        }
        return this.getSingleResultSet();
    }
    
    @Override
    public int executeUpdate(final String sql) throws SQLException {
        throw new PSQLException(GT.tr("Can''t use query methods that take a query string on a PreparedStatement.", new Object[0]), PSQLState.WRONG_OBJECT_TYPE);
    }
    
    @Override
    public int executeUpdate() throws SQLException {
        this.executeWithFlags(4);
        this.checkNoResultUpdate();
        return this.getUpdateCount();
    }
    
    @Override
    public long executeLargeUpdate() throws SQLException {
        this.executeWithFlags(4);
        this.checkNoResultUpdate();
        return this.getLargeUpdateCount();
    }
    
    @Override
    public boolean execute(final String sql) throws SQLException {
        throw new PSQLException(GT.tr("Can''t use query methods that take a query string on a PreparedStatement.", new Object[0]), PSQLState.WRONG_OBJECT_TYPE);
    }
    
    @Override
    public boolean execute() throws SQLException {
        return this.executeWithFlags(0);
    }
    
    @Override
    public boolean executeWithFlags(int flags) throws SQLException {
        try {
            this.checkClosed();
            if (this.connection.getPreferQueryMode() == PreferQueryMode.SIMPLE) {
                flags |= 0x400;
            }
            this.execute(this.preparedQuery, this.preparedParameters, flags);
            synchronized (this) {
                this.checkClosed();
                return this.result != null && this.result.getResultSet() != null;
            }
        }
        finally {
            this.defaultTimeZone = null;
        }
    }
    
    @Override
    protected boolean isOneShotQuery(CachedQuery cachedQuery) {
        if (cachedQuery == null) {
            cachedQuery = this.preparedQuery;
        }
        return super.isOneShotQuery(cachedQuery);
    }
    
    public void closeImpl() throws SQLException {
        if (this.preparedQuery != null) {
            ((PgConnection)this.connection).releaseQuery(this.preparedQuery);
        }
    }
    
    @Override
    public void setNull(final int parameterIndex, final int sqlType) throws SQLException {
        this.checkClosed();
        if (parameterIndex < 1 || parameterIndex > this.preparedParameters.getParameterCount()) {
            throw new PSQLException(GT.tr("The column index is out of range: {0}, number of columns: {1}.", parameterIndex, this.preparedParameters.getParameterCount()), PSQLState.INVALID_PARAMETER_VALUE);
        }
        int oid = 0;
        switch (sqlType) {
            case 2009: {
                oid = 142;
                break;
            }
            case 4: {
                oid = 23;
                break;
            }
            case -6:
            case 5: {
                oid = 21;
                break;
            }
            case -5: {
                oid = 20;
                break;
            }
            case 7: {
                oid = 700;
                break;
            }
            case 6:
            case 8: {
                oid = 701;
                break;
            }
            case 2:
            case 3: {
                oid = 1700;
                break;
            }
            case 1: {
                oid = 1042;
                break;
            }
            case -1:
            case 12: {
                oid = (this.connection.getStringVarcharFlag() ? 1043 : 0);
                break;
            }
            case 91: {
                oid = 1082;
                break;
            }
            case 92:
            case 93:
            case 2013:
            case 2014: {
                oid = 0;
                break;
            }
            case -7:
            case 16: {
                oid = 16;
                break;
            }
            case -4:
            case -3:
            case -2: {
                oid = 17;
                break;
            }
            case 2004:
            case 2005: {
                oid = 26;
                break;
            }
            case 0:
            case 1111:
            case 2001:
            case 2002:
            case 2003: {
                oid = 0;
                break;
            }
            default: {
                throw new PSQLException(GT.tr("Unknown Types value.", new Object[0]), PSQLState.INVALID_PARAMETER_TYPE);
            }
        }
        this.preparedParameters.setNull(parameterIndex, oid);
    }
    
    @Override
    public void setBoolean(final int parameterIndex, final boolean x) throws SQLException {
        this.checkClosed();
        this.bindLiteral(parameterIndex, x ? "TRUE" : "FALSE", 16);
    }
    
    @Override
    public void setByte(final int parameterIndex, final byte x) throws SQLException {
        this.setShort(parameterIndex, x);
    }
    
    @Override
    public void setShort(final int parameterIndex, final short x) throws SQLException {
        this.checkClosed();
        if (this.connection.binaryTransferSend(21)) {
            final byte[] val = new byte[2];
            ByteConverter.int2(val, 0, x);
            this.bindBytes(parameterIndex, val, 21);
            return;
        }
        this.bindLiteral(parameterIndex, Integer.toString(x), 21);
    }
    
    @Override
    public void setInt(final int parameterIndex, final int x) throws SQLException {
        this.checkClosed();
        if (this.connection.binaryTransferSend(23)) {
            final byte[] val = new byte[4];
            ByteConverter.int4(val, 0, x);
            this.bindBytes(parameterIndex, val, 23);
            return;
        }
        this.bindLiteral(parameterIndex, Integer.toString(x), 23);
    }
    
    @Override
    public void setLong(final int parameterIndex, final long x) throws SQLException {
        this.checkClosed();
        if (this.connection.binaryTransferSend(20)) {
            final byte[] val = new byte[8];
            ByteConverter.int8(val, 0, x);
            this.bindBytes(parameterIndex, val, 20);
            return;
        }
        this.bindLiteral(parameterIndex, Long.toString(x), 20);
    }
    
    @Override
    public void setFloat(final int parameterIndex, final float x) throws SQLException {
        this.checkClosed();
        if (this.connection.binaryTransferSend(700)) {
            final byte[] val = new byte[4];
            ByteConverter.float4(val, 0, x);
            this.bindBytes(parameterIndex, val, 700);
            return;
        }
        this.bindLiteral(parameterIndex, Float.toString(x), 701);
    }
    
    @Override
    public void setDouble(final int parameterIndex, final double x) throws SQLException {
        this.checkClosed();
        if (this.connection.binaryTransferSend(701)) {
            final byte[] val = new byte[8];
            ByteConverter.float8(val, 0, x);
            this.bindBytes(parameterIndex, val, 701);
            return;
        }
        this.bindLiteral(parameterIndex, Double.toString(x), 701);
    }
    
    @Override
    public void setBigDecimal(final int parameterIndex, final BigDecimal x) throws SQLException {
        this.setNumber(parameterIndex, x);
    }
    
    @Override
    public void setString(final int parameterIndex, final String x) throws SQLException {
        this.checkClosed();
        this.setString(parameterIndex, x, this.getStringType());
    }
    
    private int getStringType() {
        return this.connection.getStringVarcharFlag() ? 1043 : 0;
    }
    
    protected void setString(final int parameterIndex, final String x, final int oid) throws SQLException {
        this.checkClosed();
        if (x == null) {
            this.preparedParameters.setNull(parameterIndex, oid);
        }
        else {
            this.bindString(parameterIndex, x, oid);
        }
    }
    
    @Override
    public void setBytes(final int parameterIndex, final byte[] x) throws SQLException {
        this.checkClosed();
        if (null == x) {
            this.setNull(parameterIndex, -3);
            return;
        }
        final byte[] copy = new byte[x.length];
        System.arraycopy(x, 0, copy, 0, x.length);
        this.preparedParameters.setBytea(parameterIndex, copy, 0, x.length);
    }
    
    private void setByteStreamWriter(final int parameterIndex, final ByteStreamWriter x) throws SQLException {
        this.preparedParameters.setBytea(parameterIndex, x);
    }
    
    @Override
    public void setDate(final int parameterIndex, final Date x) throws SQLException {
        this.setDate(parameterIndex, x, null);
    }
    
    @Override
    public void setTime(final int parameterIndex, final Time x) throws SQLException {
        this.setTime(parameterIndex, x, null);
    }
    
    @Override
    public void setTimestamp(final int parameterIndex, final Timestamp x) throws SQLException {
        this.setTimestamp(parameterIndex, x, null);
    }
    
    private void setCharacterStreamPost71(final int parameterIndex, final InputStream x, final int length, final String encoding) throws SQLException {
        if (x == null) {
            this.setNull(parameterIndex, 12);
            return;
        }
        if (length < 0) {
            throw new PSQLException(GT.tr("Invalid stream length {0}.", length), PSQLState.INVALID_PARAMETER_VALUE);
        }
        try {
            final InputStreamReader inStream = new InputStreamReader(x, encoding);
            final char[] chars = new char[length];
            int charsRead = 0;
            do {
                final int n = inStream.read(chars, charsRead, length - charsRead);
                if (n == -1) {
                    break;
                }
                charsRead += n;
            } while (charsRead != length);
            this.setString(parameterIndex, new String(chars, 0, charsRead), 1043);
        }
        catch (final UnsupportedEncodingException uee) {
            throw new PSQLException(GT.tr("The JVM claims not to support the {0} encoding.", encoding), PSQLState.UNEXPECTED_ERROR, uee);
        }
        catch (final IOException ioe) {
            throw new PSQLException(GT.tr("Provided InputStream failed.", new Object[0]), PSQLState.UNEXPECTED_ERROR, ioe);
        }
    }
    
    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        this.checkClosed();
        this.setCharacterStreamPost71(parameterIndex, x, length, "ASCII");
    }
    
    @Override
    public void setUnicodeStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        this.checkClosed();
        this.setCharacterStreamPost71(parameterIndex, x, length, "UTF-8");
    }
    
    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        this.checkClosed();
        if (x == null) {
            this.setNull(parameterIndex, -3);
            return;
        }
        if (length < 0) {
            throw new PSQLException(GT.tr("Invalid stream length {0}.", length), PSQLState.INVALID_PARAMETER_VALUE);
        }
        this.preparedParameters.setBytea(parameterIndex, x, length);
    }
    
    @Override
    public void clearParameters() throws SQLException {
        this.preparedParameters.clear();
    }
    
    private void setPGobject(final int parameterIndex, final PGobject x) throws SQLException {
        final String typename = x.getType();
        final int oid = this.connection.getTypeInfo().getPGType(typename);
        if (oid == 0) {
            throw new PSQLException(GT.tr("Unknown type {0}.", typename), PSQLState.INVALID_PARAMETER_TYPE);
        }
        if (x instanceof PGBinaryObject && this.connection.binaryTransferSend(oid)) {
            final PGBinaryObject binObj = (PGBinaryObject)x;
            final int length = binObj.lengthInBytes();
            if (length == 0) {
                this.preparedParameters.setNull(parameterIndex, oid);
                return;
            }
            final byte[] data = new byte[length];
            binObj.toBytes(data, 0);
            this.bindBytes(parameterIndex, data, oid);
        }
        else {
            this.setString(parameterIndex, x.getValue(), oid);
        }
    }
    
    private void setMap(final int parameterIndex, final Map<?, ?> x) throws SQLException {
        final int oid = this.connection.getTypeInfo().getPGType("hstore");
        if (oid == 0) {
            throw new PSQLException(GT.tr("No hstore extension installed.", new Object[0]), PSQLState.INVALID_PARAMETER_TYPE);
        }
        if (this.connection.binaryTransferSend(oid)) {
            final byte[] data = HStoreConverter.toBytes(x, this.connection.getEncoding());
            this.bindBytes(parameterIndex, data, oid);
        }
        else {
            this.setString(parameterIndex, HStoreConverter.toString(x), oid);
        }
    }
    
    private void setNumber(final int parameterIndex, final Number x) throws SQLException {
        this.checkClosed();
        if (x == null) {
            this.setNull(parameterIndex, 3);
        }
        else {
            this.bindLiteral(parameterIndex, x.toString(), 1700);
        }
    }
    
    @Override
    public void setObject(final int parameterIndex, final Object in, final int targetSqlType, final int scale) throws SQLException {
        this.checkClosed();
        if (in == null) {
            this.setNull(parameterIndex, targetSqlType);
            return;
        }
        if (targetSqlType == 1111 && in instanceof UUID && this.connection.haveMinimumServerVersion(ServerVersion.v8_3)) {
            this.setUuid(parameterIndex, (UUID)in);
            return;
        }
        switch (targetSqlType) {
            case 2009: {
                if (in instanceof SQLXML) {
                    this.setSQLXML(parameterIndex, (SQLXML)in);
                    break;
                }
                this.setSQLXML(parameterIndex, new PgSQLXML(this.connection, in.toString()));
                break;
            }
            case 4: {
                this.setInt(parameterIndex, castToInt(in));
                break;
            }
            case -6:
            case 5: {
                this.setShort(parameterIndex, castToShort(in));
                break;
            }
            case -5: {
                this.setLong(parameterIndex, castToLong(in));
                break;
            }
            case 7: {
                this.setFloat(parameterIndex, castToFloat(in));
                break;
            }
            case 6:
            case 8: {
                this.setDouble(parameterIndex, castToDouble(in));
                break;
            }
            case 2:
            case 3: {
                this.setBigDecimal(parameterIndex, castToBigDecimal(in, scale));
                break;
            }
            case 1: {
                this.setString(parameterIndex, castToString(in), 1042);
                break;
            }
            case 12: {
                this.setString(parameterIndex, castToString(in), this.getStringType());
                break;
            }
            case -1: {
                if (in instanceof InputStream) {
                    this.preparedParameters.setText(parameterIndex, (InputStream)in);
                    break;
                }
                this.setString(parameterIndex, castToString(in), this.getStringType());
                break;
            }
            case 91: {
                if (in instanceof Date) {
                    this.setDate(parameterIndex, (Date)in);
                    break;
                }
                Date tmpd;
                if (in instanceof java.util.Date) {
                    tmpd = new Date(((java.util.Date)in).getTime());
                }
                else {
                    if (in instanceof LocalDate) {
                        this.setDate(parameterIndex, (LocalDate)in);
                        break;
                    }
                    tmpd = this.connection.getTimestampUtils().toDate(this.getDefaultCalendar(), in.toString());
                }
                this.setDate(parameterIndex, tmpd);
                break;
            }
            case 92: {
                if (in instanceof Time) {
                    this.setTime(parameterIndex, (Time)in);
                    break;
                }
                Time tmpt;
                if (in instanceof java.util.Date) {
                    tmpt = new Time(((java.util.Date)in).getTime());
                }
                else {
                    if (in instanceof LocalTime) {
                        this.setTime(parameterIndex, (LocalTime)in);
                        break;
                    }
                    tmpt = this.connection.getTimestampUtils().toTime(this.getDefaultCalendar(), in.toString());
                }
                this.setTime(parameterIndex, tmpt);
                break;
            }
            case 93: {
                if (in instanceof PGTimestamp) {
                    this.setObject(parameterIndex, in);
                    break;
                }
                if (in instanceof Timestamp) {
                    this.setTimestamp(parameterIndex, (Timestamp)in);
                    break;
                }
                Timestamp tmpts;
                if (in instanceof java.util.Date) {
                    tmpts = new Timestamp(((java.util.Date)in).getTime());
                }
                else {
                    if (in instanceof LocalDateTime) {
                        this.setTimestamp(parameterIndex, (LocalDateTime)in);
                        break;
                    }
                    tmpts = this.connection.getTimestampUtils().toTimestamp(this.getDefaultCalendar(), in.toString());
                }
                this.setTimestamp(parameterIndex, tmpts);
                break;
            }
            case 2014: {
                if (in instanceof OffsetDateTime) {
                    this.setTimestamp(parameterIndex, (OffsetDateTime)in);
                    break;
                }
                if (in instanceof PGTimestamp) {
                    this.setObject(parameterIndex, in);
                    break;
                }
                throw new PSQLException(GT.tr("Cannot cast an instance of {0} to type {1}", in.getClass().getName(), "Types.TIMESTAMP_WITH_TIMEZONE"), PSQLState.INVALID_PARAMETER_TYPE);
            }
            case -7:
            case 16: {
                this.setBoolean(parameterIndex, BooleanTypeUtil.castToBoolean(in));
                break;
            }
            case -4:
            case -3:
            case -2: {
                this.setObject(parameterIndex, in);
                break;
            }
            case 2004: {
                if (in instanceof Blob) {
                    this.setBlob(parameterIndex, (Blob)in);
                    break;
                }
                if (in instanceof InputStream) {
                    final long oid = this.createBlob(parameterIndex, (InputStream)in, -1L);
                    this.setLong(parameterIndex, oid);
                    break;
                }
                throw new PSQLException(GT.tr("Cannot cast an instance of {0} to type {1}", in.getClass().getName(), "Types.BLOB"), PSQLState.INVALID_PARAMETER_TYPE);
            }
            case 2005: {
                if (in instanceof Clob) {
                    this.setClob(parameterIndex, (Clob)in);
                    break;
                }
                throw new PSQLException(GT.tr("Cannot cast an instance of {0} to type {1}", in.getClass().getName(), "Types.CLOB"), PSQLState.INVALID_PARAMETER_TYPE);
            }
            case 2003: {
                if (in instanceof Array) {
                    this.setArray(parameterIndex, (Array)in);
                    break;
                }
                try {
                    this.setObjectArray(parameterIndex, in);
                    break;
                }
                catch (final Exception e) {
                    throw new PSQLException(GT.tr("Cannot cast an instance of {0} to type {1}", in.getClass().getName(), "Types.ARRAY"), PSQLState.INVALID_PARAMETER_TYPE, e);
                }
            }
            case 2001: {
                this.bindString(parameterIndex, in.toString(), 0);
                break;
            }
            case 1111: {
                if (in instanceof PGobject) {
                    this.setPGobject(parameterIndex, (PGobject)in);
                    break;
                }
                if (in instanceof Map) {
                    this.setMap(parameterIndex, (Map<?, ?>)in);
                    break;
                }
                this.bindString(parameterIndex, in.toString(), 0);
                break;
            }
            default: {
                throw new PSQLException(GT.tr("Unsupported Types value: {0}", targetSqlType), PSQLState.INVALID_PARAMETER_TYPE);
            }
        }
    }
    
    private <A> void setObjectArray(final int parameterIndex, final A in) throws SQLException {
        final ArrayEncoding.ArrayEncoder<A> arraySupport = ArrayEncoding.getArrayEncoder(in);
        final TypeInfo typeInfo = this.connection.getTypeInfo();
        final int oid = arraySupport.getDefaultArrayTypeOid();
        if (arraySupport.supportBinaryRepresentation(oid) && this.connection.getPreferQueryMode() != PreferQueryMode.SIMPLE) {
            this.bindBytes(parameterIndex, arraySupport.toBinaryRepresentation(this.connection, in, oid), oid);
        }
        else {
            if (oid == 0) {
                throw new SQLFeatureNotSupportedException();
            }
            final int baseOid = typeInfo.getPGArrayElement(oid);
            final String baseType = Nullness.castNonNull(typeInfo.getPGType(baseOid));
            final Array array = this.getPGConnection().createArrayOf(baseType, in);
            this.setArray(parameterIndex, array);
        }
    }
    
    private static String asString(final Clob in) throws SQLException {
        return in.getSubString(1L, (int)in.length());
    }
    
    private static int castToInt(final Object in) throws SQLException {
        try {
            if (in instanceof String) {
                return Integer.parseInt((String)in);
            }
            if (in instanceof Number) {
                return ((Number)in).intValue();
            }
            if (in instanceof java.util.Date) {
                return (int)((java.util.Date)in).getTime();
            }
            if (in instanceof Boolean) {
                return ((boolean)in) ? 1 : 0;
            }
            if (in instanceof Clob) {
                return Integer.parseInt(asString((Clob)in));
            }
            if (in instanceof Character) {
                return Integer.parseInt(in.toString());
            }
        }
        catch (final Exception e) {
            throw cannotCastException(in.getClass().getName(), "int", e);
        }
        throw cannotCastException(in.getClass().getName(), "int");
    }
    
    private static short castToShort(final Object in) throws SQLException {
        try {
            if (in instanceof String) {
                return Short.parseShort((String)in);
            }
            if (in instanceof Number) {
                return ((Number)in).shortValue();
            }
            if (in instanceof java.util.Date) {
                return (short)((java.util.Date)in).getTime();
            }
            if (in instanceof Boolean) {
                return (short)(((boolean)in) ? 1 : 0);
            }
            if (in instanceof Clob) {
                return Short.parseShort(asString((Clob)in));
            }
            if (in instanceof Character) {
                return Short.parseShort(in.toString());
            }
        }
        catch (final Exception e) {
            throw cannotCastException(in.getClass().getName(), "short", e);
        }
        throw cannotCastException(in.getClass().getName(), "short");
    }
    
    private static long castToLong(final Object in) throws SQLException {
        try {
            if (in instanceof String) {
                return Long.parseLong((String)in);
            }
            if (in instanceof Number) {
                return ((Number)in).longValue();
            }
            if (in instanceof java.util.Date) {
                return ((java.util.Date)in).getTime();
            }
            if (in instanceof Boolean) {
                return ((boolean)in) ? 1 : 0;
            }
            if (in instanceof Clob) {
                return Long.parseLong(asString((Clob)in));
            }
            if (in instanceof Character) {
                return Long.parseLong(in.toString());
            }
        }
        catch (final Exception e) {
            throw cannotCastException(in.getClass().getName(), "long", e);
        }
        throw cannotCastException(in.getClass().getName(), "long");
    }
    
    private static float castToFloat(final Object in) throws SQLException {
        try {
            if (in instanceof String) {
                return Float.parseFloat((String)in);
            }
            if (in instanceof Number) {
                return ((Number)in).floatValue();
            }
            if (in instanceof java.util.Date) {
                return (float)((java.util.Date)in).getTime();
            }
            if (in instanceof Boolean) {
                return in ? 1.0f : 0.0f;
            }
            if (in instanceof Clob) {
                return Float.parseFloat(asString((Clob)in));
            }
            if (in instanceof Character) {
                return Float.parseFloat(in.toString());
            }
        }
        catch (final Exception e) {
            throw cannotCastException(in.getClass().getName(), "float", e);
        }
        throw cannotCastException(in.getClass().getName(), "float");
    }
    
    private static double castToDouble(final Object in) throws SQLException {
        try {
            if (in instanceof String) {
                return Double.parseDouble((String)in);
            }
            if (in instanceof Number) {
                return ((Number)in).doubleValue();
            }
            if (in instanceof java.util.Date) {
                return (double)((java.util.Date)in).getTime();
            }
            if (in instanceof Boolean) {
                return in ? 1.0 : 0.0;
            }
            if (in instanceof Clob) {
                return Double.parseDouble(asString((Clob)in));
            }
            if (in instanceof Character) {
                return Double.parseDouble(in.toString());
            }
        }
        catch (final Exception e) {
            throw cannotCastException(in.getClass().getName(), "double", e);
        }
        throw cannotCastException(in.getClass().getName(), "double");
    }
    
    private static BigDecimal castToBigDecimal(final Object in, final int scale) throws SQLException {
        try {
            BigDecimal rc = null;
            if (in instanceof String) {
                rc = new BigDecimal((String)in);
            }
            else if (in instanceof BigDecimal) {
                rc = (BigDecimal)in;
            }
            else if (in instanceof BigInteger) {
                rc = new BigDecimal((BigInteger)in);
            }
            else if (in instanceof Long || in instanceof Integer || in instanceof Short || in instanceof Byte) {
                rc = BigDecimal.valueOf(((Number)in).longValue());
            }
            else if (in instanceof Double || in instanceof Float) {
                rc = BigDecimal.valueOf(((Number)in).doubleValue());
            }
            else if (in instanceof java.util.Date) {
                rc = BigDecimal.valueOf(((java.util.Date)in).getTime());
            }
            else if (in instanceof Boolean) {
                rc = (in ? BigDecimal.ONE : BigDecimal.ZERO);
            }
            else if (in instanceof Clob) {
                rc = new BigDecimal(asString((Clob)in));
            }
            else if (in instanceof Character) {
                rc = new BigDecimal(new char[] { (char)in });
            }
            if (rc != null) {
                if (scale >= 0) {
                    rc = rc.setScale(scale, RoundingMode.HALF_UP);
                }
                return rc;
            }
        }
        catch (final Exception e) {
            throw cannotCastException(in.getClass().getName(), "BigDecimal", e);
        }
        throw cannotCastException(in.getClass().getName(), "BigDecimal");
    }
    
    private static String castToString(final Object in) throws SQLException {
        try {
            if (in instanceof String) {
                return (String)in;
            }
            if (in instanceof Clob) {
                return asString((Clob)in);
            }
            return in.toString();
        }
        catch (final Exception e) {
            throw cannotCastException(in.getClass().getName(), "String", e);
        }
    }
    
    private static PSQLException cannotCastException(final String fromType, final String toType) {
        return cannotCastException(fromType, toType, null);
    }
    
    private static PSQLException cannotCastException(final String fromType, final String toType, final Exception cause) {
        return new PSQLException(GT.tr("Cannot convert an instance of {0} to type {1}", fromType, toType), PSQLState.INVALID_PARAMETER_TYPE, cause);
    }
    
    @Override
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType) throws SQLException {
        this.setObject(parameterIndex, x, targetSqlType, -1);
    }
    
    @Override
    public void setObject(final int parameterIndex, final Object x) throws SQLException {
        this.checkClosed();
        if (x == null) {
            this.setNull(parameterIndex, 1111);
        }
        else if (x instanceof UUID && this.connection.haveMinimumServerVersion(ServerVersion.v8_3)) {
            this.setUuid(parameterIndex, (UUID)x);
        }
        else if (x instanceof SQLXML) {
            this.setSQLXML(parameterIndex, (SQLXML)x);
        }
        else if (x instanceof String) {
            this.setString(parameterIndex, (String)x);
        }
        else if (x instanceof BigDecimal) {
            this.setBigDecimal(parameterIndex, (BigDecimal)x);
        }
        else if (x instanceof Short) {
            this.setShort(parameterIndex, (short)x);
        }
        else if (x instanceof Integer) {
            this.setInt(parameterIndex, (int)x);
        }
        else if (x instanceof Long) {
            this.setLong(parameterIndex, (long)x);
        }
        else if (x instanceof Float) {
            this.setFloat(parameterIndex, (float)x);
        }
        else if (x instanceof Double) {
            this.setDouble(parameterIndex, (double)x);
        }
        else if (x instanceof byte[]) {
            this.setBytes(parameterIndex, (byte[])x);
        }
        else if (x instanceof ByteStreamWriter) {
            this.setByteStreamWriter(parameterIndex, (ByteStreamWriter)x);
        }
        else if (x instanceof Date) {
            this.setDate(parameterIndex, (Date)x);
        }
        else if (x instanceof Time) {
            this.setTime(parameterIndex, (Time)x);
        }
        else if (x instanceof Timestamp) {
            this.setTimestamp(parameterIndex, (Timestamp)x);
        }
        else if (x instanceof Boolean) {
            this.setBoolean(parameterIndex, (boolean)x);
        }
        else if (x instanceof Byte) {
            this.setByte(parameterIndex, (byte)x);
        }
        else if (x instanceof Blob) {
            this.setBlob(parameterIndex, (Blob)x);
        }
        else if (x instanceof Clob) {
            this.setClob(parameterIndex, (Clob)x);
        }
        else if (x instanceof Array) {
            this.setArray(parameterIndex, (Array)x);
        }
        else if (x instanceof PGobject) {
            this.setPGobject(parameterIndex, (PGobject)x);
        }
        else if (x instanceof Character) {
            this.setString(parameterIndex, ((Character)x).toString());
        }
        else if (x instanceof LocalDate) {
            this.setDate(parameterIndex, (LocalDate)x);
        }
        else if (x instanceof LocalTime) {
            this.setTime(parameterIndex, (LocalTime)x);
        }
        else if (x instanceof LocalDateTime) {
            this.setTimestamp(parameterIndex, (LocalDateTime)x);
        }
        else if (x instanceof OffsetDateTime) {
            this.setTimestamp(parameterIndex, (OffsetDateTime)x);
        }
        else if (x instanceof Map) {
            this.setMap(parameterIndex, (Map<?, ?>)x);
        }
        else {
            if (!(x instanceof Number)) {
                if (x.getClass().isArray()) {
                    try {
                        this.setObjectArray(parameterIndex, x);
                        return;
                    }
                    catch (final Exception e) {
                        throw new PSQLException(GT.tr("Cannot cast an instance of {0} to type {1}", x.getClass().getName(), "Types.ARRAY"), PSQLState.INVALID_PARAMETER_TYPE, e);
                    }
                }
                throw new PSQLException(GT.tr("Can''t infer the SQL type to use for an instance of {0}. Use setObject() with an explicit Types value to specify the type to use.", x.getClass().getName()), PSQLState.INVALID_PARAMETER_TYPE);
            }
            this.setNumber(parameterIndex, (Number)x);
        }
    }
    
    @Override
    public String toString() {
        if (this.preparedQuery == null) {
            return super.toString();
        }
        return this.preparedQuery.query.toString(this.preparedParameters);
    }
    
    protected void bindLiteral(final int paramIndex, final String s, final int oid) throws SQLException {
        this.preparedParameters.setLiteralParameter(paramIndex, s, oid);
    }
    
    protected void bindBytes(final int paramIndex, final byte[] b, final int oid) throws SQLException {
        this.preparedParameters.setBinaryParameter(paramIndex, b, oid);
    }
    
    private void bindString(final int paramIndex, final String s, final int oid) throws SQLException {
        this.preparedParameters.setStringParameter(paramIndex, s, oid);
    }
    
    @Override
    public boolean isUseServerPrepare() {
        return this.preparedQuery != null && this.mPrepareThreshold != 0 && this.preparedQuery.getExecuteCount() + 1 >= this.mPrepareThreshold;
    }
    
    @Override
    public void addBatch(final String sql) throws SQLException {
        this.checkClosed();
        throw new PSQLException(GT.tr("Can''t use query methods that take a query string on a PreparedStatement.", new Object[0]), PSQLState.WRONG_OBJECT_TYPE);
    }
    
    @Override
    public void addBatch() throws SQLException {
        this.checkClosed();
        ArrayList<Query> batchStatements = this.batchStatements;
        if (batchStatements == null) {
            batchStatements = (this.batchStatements = new ArrayList<Query>());
        }
        ArrayList<ParameterList> batchParameters = this.batchParameters;
        if (batchParameters == null) {
            batchParameters = (this.batchParameters = new ArrayList<ParameterList>());
        }
        batchParameters.add(this.preparedParameters.copy());
        final Query query = this.preparedQuery.query;
        if (!(query instanceof BatchedQuery) || batchStatements.isEmpty()) {
            batchStatements.add(query);
        }
    }
    
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        this.checkClosed();
        ResultSet rs = this.getResultSet();
        if (rs == null || ((PgResultSet)rs).isResultSetClosed()) {
            final int flags = 49;
            final StatementResultHandler handler = new StatementResultHandler();
            this.connection.getQueryExecutor().execute(this.preparedQuery.query, this.preparedParameters, handler, 0, 0, flags);
            final ResultWrapper wrapper = handler.getResults();
            if (wrapper != null) {
                rs = wrapper.getResultSet();
            }
        }
        if (rs != null) {
            return rs.getMetaData();
        }
        return null;
    }
    
    @Override
    public void setArray(final int i, final Array x) throws SQLException {
        this.checkClosed();
        if (null == x) {
            this.setNull(i, 2003);
            return;
        }
        final String typename = x.getBaseTypeName();
        final int oid = this.connection.getTypeInfo().getPGArrayType(typename);
        if (oid == 0) {
            throw new PSQLException(GT.tr("Unknown type {0}.", typename), PSQLState.INVALID_PARAMETER_TYPE);
        }
        if (x instanceof PgArray) {
            final PgArray arr = (PgArray)x;
            final byte[] bytes = arr.toBytes();
            if (bytes != null) {
                this.bindBytes(i, bytes, oid);
                return;
            }
        }
        this.setString(i, x.toString(), oid);
    }
    
    protected long createBlob(final int i, final InputStream inputStream, final long length) throws SQLException {
        final LargeObjectManager lom = this.connection.getLargeObjectAPI();
        final long oid = lom.createLO();
        final LargeObject lob = lom.open(oid);
        final OutputStream outputStream = lob.getOutputStream();
        final byte[] buf = new byte[4096];
        try {
            long remaining;
            if (length > 0L) {
                remaining = length;
            }
            else {
                remaining = Long.MAX_VALUE;
            }
            for (int numRead = inputStream.read(buf, 0, (length > 0L && remaining < buf.length) ? ((int)remaining) : buf.length); numRead != -1 && remaining > 0L; numRead = inputStream.read(buf, 0, (length > 0L && remaining < buf.length) ? ((int)remaining) : buf.length)) {
                remaining -= numRead;
                outputStream.write(buf, 0, numRead);
            }
        }
        catch (final IOException se) {
            throw new PSQLException(GT.tr("Unexpected error writing large object to database.", new Object[0]), PSQLState.UNEXPECTED_ERROR, se);
        }
        finally {
            try {
                outputStream.close();
            }
            catch (final Exception ex) {}
        }
        return oid;
    }
    
    @Override
    public void setBlob(final int i, final Blob x) throws SQLException {
        this.checkClosed();
        if (x == null) {
            this.setNull(i, 2004);
            return;
        }
        final InputStream inStream = x.getBinaryStream();
        try {
            final long oid = this.createBlob(i, inStream, x.length());
            this.setLong(i, oid);
        }
        finally {
            try {
                inStream.close();
            }
            catch (final Exception ex) {}
        }
    }
    
    private String readerToString(final Reader value, final int maxLength) throws SQLException {
        try {
            final int bufferSize = Math.min(maxLength, 1024);
            final StringBuilder v = new StringBuilder(bufferSize);
            final char[] buf = new char[bufferSize];
            int nRead = 0;
            while (nRead > -1 && v.length() < maxLength) {
                nRead = value.read(buf, 0, Math.min(bufferSize, maxLength - v.length()));
                if (nRead > 0) {
                    v.append(buf, 0, nRead);
                }
            }
            return v.toString();
        }
        catch (final IOException ioe) {
            throw new PSQLException(GT.tr("Provided Reader failed.", new Object[0]), PSQLState.UNEXPECTED_ERROR, ioe);
        }
    }
    
    @Override
    public void setCharacterStream(final int i, final Reader x, final int length) throws SQLException {
        this.checkClosed();
        if (x == null) {
            this.setNull(i, 12);
            return;
        }
        if (length < 0) {
            throw new PSQLException(GT.tr("Invalid stream length {0}.", length), PSQLState.INVALID_PARAMETER_VALUE);
        }
        this.setString(i, this.readerToString(x, length));
    }
    
    @Override
    public void setClob(final int i, final Clob x) throws SQLException {
        this.checkClosed();
        if (x == null) {
            this.setNull(i, 2005);
            return;
        }
        final Reader inStream = x.getCharacterStream();
        final int length = (int)x.length();
        final LargeObjectManager lom = this.connection.getLargeObjectAPI();
        final long oid = lom.createLO();
        final LargeObject lob = lom.open(oid);
        final Charset connectionCharset = Charset.forName(this.connection.getEncoding().name());
        final OutputStream los = lob.getOutputStream();
        final Writer lw = new OutputStreamWriter(los, connectionCharset);
        try {
            for (int c = inStream.read(), p = 0; c > -1 && p < length; c = inStream.read(), ++p) {
                lw.write(c);
            }
            lw.close();
        }
        catch (final IOException se) {
            throw new PSQLException(GT.tr("Unexpected error writing large object to database.", new Object[0]), PSQLState.UNEXPECTED_ERROR, se);
        }
        this.setLong(i, oid);
    }
    
    @Override
    public void setNull(final int parameterIndex, final int t, final String typeName) throws SQLException {
        if (typeName == null) {
            this.setNull(parameterIndex, t);
            return;
        }
        this.checkClosed();
        final TypeInfo typeInfo = this.connection.getTypeInfo();
        final int oid = typeInfo.getPGType(typeName);
        this.preparedParameters.setNull(parameterIndex, oid);
    }
    
    @Override
    public void setRef(final int i, final Ref x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setRef(int,Ref)");
    }
    
    @Override
    public void setDate(final int i, final Date d, Calendar cal) throws SQLException {
        this.checkClosed();
        if (d == null) {
            this.setNull(i, 91);
            return;
        }
        if (this.connection.binaryTransferSend(1082)) {
            final byte[] val = new byte[4];
            final TimeZone tz = (cal != null) ? cal.getTimeZone() : null;
            this.connection.getTimestampUtils().toBinDate(tz, val, d);
            this.preparedParameters.setBinaryParameter(i, val, 1082);
            return;
        }
        if (cal == null) {
            cal = this.getDefaultCalendar();
        }
        this.bindString(i, this.connection.getTimestampUtils().toString(cal, d), 0);
    }
    
    @Override
    public void setTime(final int i, final Time t, Calendar cal) throws SQLException {
        this.checkClosed();
        if (t == null) {
            this.setNull(i, 92);
            return;
        }
        int oid = 0;
        if (t instanceof PGTime) {
            final PGTime pgTime = (PGTime)t;
            if (pgTime.getCalendar() == null) {
                oid = 1083;
            }
            else {
                oid = 1266;
                cal = pgTime.getCalendar();
            }
        }
        if (cal == null) {
            cal = this.getDefaultCalendar();
        }
        this.bindString(i, this.connection.getTimestampUtils().toString(cal, t), oid);
    }
    
    @Override
    public void setTimestamp(final int i, final Timestamp t, Calendar cal) throws SQLException {
        this.checkClosed();
        if (t == null) {
            this.setNull(i, 93);
            return;
        }
        int oid = 0;
        if (t instanceof PGTimestamp) {
            final PGTimestamp pgTimestamp = (PGTimestamp)t;
            if (pgTimestamp.getCalendar() == null) {
                oid = 1114;
            }
            else {
                oid = 1184;
                cal = pgTimestamp.getCalendar();
            }
        }
        if (cal == null) {
            cal = this.getDefaultCalendar();
        }
        this.bindString(i, this.connection.getTimestampUtils().toString(cal, t), oid);
    }
    
    private void setDate(final int i, final LocalDate localDate) throws SQLException {
        final int oid = 1082;
        this.bindString(i, this.connection.getTimestampUtils().toString(localDate), oid);
    }
    
    private void setTime(final int i, final LocalTime localTime) throws SQLException {
        final int oid = 1083;
        this.bindString(i, this.connection.getTimestampUtils().toString(localTime), oid);
    }
    
    private void setTimestamp(final int i, final LocalDateTime localDateTime) throws SQLException {
        final int oid = 1114;
        this.bindString(i, this.connection.getTimestampUtils().toString(localDateTime), oid);
    }
    
    private void setTimestamp(final int i, final OffsetDateTime offsetDateTime) throws SQLException {
        final int oid = 1184;
        this.bindString(i, this.connection.getTimestampUtils().toString(offsetDateTime), oid);
    }
    
    public ParameterMetaData createParameterMetaData(final BaseConnection conn, final int[] oids) throws SQLException {
        return new PgParameterMetaData(conn, oids);
    }
    
    @Override
    public void setObject(final int parameterIndex, final Object x, final SQLType targetSqlType, final int scaleOrLength) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setObject");
    }
    
    @Override
    public void setObject(final int parameterIndex, final Object x, final SQLType targetSqlType) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setObject");
    }
    
    @Override
    public void setRowId(final int parameterIndex, final RowId x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setRowId(int, RowId)");
    }
    
    @Override
    public void setNString(final int parameterIndex, final String value) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setNString(int, String)");
    }
    
    @Override
    public void setNCharacterStream(final int parameterIndex, final Reader value, final long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setNCharacterStream(int, Reader, long)");
    }
    
    @Override
    public void setNCharacterStream(final int parameterIndex, final Reader value) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setNCharacterStream(int, Reader)");
    }
    
    @Override
    public void setCharacterStream(final int parameterIndex, final Reader value, final long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setCharacterStream(int, Reader, long)");
    }
    
    @Override
    public void setCharacterStream(final int parameterIndex, final Reader value) throws SQLException {
        if (this.connection.getPreferQueryMode() == PreferQueryMode.SIMPLE) {
            final String s = (value != null) ? this.readerToString(value, Integer.MAX_VALUE) : null;
            this.setString(parameterIndex, s);
            return;
        }
        final InputStream is = (value != null) ? new ReaderInputStream(value) : null;
        this.setObject(parameterIndex, is, -1);
    }
    
    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream value, final long length) throws SQLException {
        if (length > 2147483647L) {
            throw new PSQLException(GT.tr("Object is too large to send over the protocol.", new Object[0]), PSQLState.NUMERIC_CONSTANT_OUT_OF_RANGE);
        }
        if (value == null) {
            this.preparedParameters.setNull(parameterIndex, 17);
        }
        else {
            this.preparedParameters.setBytea(parameterIndex, value, (int)length);
        }
    }
    
    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream value) throws SQLException {
        if (value == null) {
            this.preparedParameters.setNull(parameterIndex, 17);
        }
        else {
            this.preparedParameters.setBytea(parameterIndex, value);
        }
    }
    
    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream value, final long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setAsciiStream(int, InputStream, long)");
    }
    
    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream value) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setAsciiStream(int, InputStream)");
    }
    
    @Override
    public void setNClob(final int parameterIndex, final NClob value) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setNClob(int, NClob)");
    }
    
    @Override
    public void setClob(final int parameterIndex, final Reader reader, final long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setClob(int, Reader, long)");
    }
    
    @Override
    public void setClob(final int parameterIndex, final Reader reader) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setClob(int, Reader)");
    }
    
    @Override
    public void setBlob(final int parameterIndex, final InputStream inputStream, final long length) throws SQLException {
        this.checkClosed();
        if (inputStream == null) {
            this.setNull(parameterIndex, 2004);
            return;
        }
        if (length < 0L) {
            throw new PSQLException(GT.tr("Invalid stream length {0}.", length), PSQLState.INVALID_PARAMETER_VALUE);
        }
        final long oid = this.createBlob(parameterIndex, inputStream, length);
        this.setLong(parameterIndex, oid);
    }
    
    @Override
    public void setBlob(final int parameterIndex, final InputStream inputStream) throws SQLException {
        this.checkClosed();
        if (inputStream == null) {
            this.setNull(parameterIndex, 2004);
            return;
        }
        final long oid = this.createBlob(parameterIndex, inputStream, -1L);
        this.setLong(parameterIndex, oid);
    }
    
    @Override
    public void setNClob(final int parameterIndex, final Reader reader, final long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setNClob(int, Reader, long)");
    }
    
    @Override
    public void setNClob(final int parameterIndex, final Reader reader) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setNClob(int, Reader)");
    }
    
    @Override
    public void setSQLXML(final int parameterIndex, final SQLXML xmlObject) throws SQLException {
        this.checkClosed();
        final String stringValue = (xmlObject == null) ? null : xmlObject.getString();
        if (stringValue == null) {
            this.setNull(parameterIndex, 2009);
        }
        else {
            this.setString(parameterIndex, stringValue, 142);
        }
    }
    
    private void setUuid(final int parameterIndex, final UUID uuid) throws SQLException {
        if (this.connection.binaryTransferSend(2950)) {
            final byte[] val = new byte[16];
            ByteConverter.int8(val, 0, uuid.getMostSignificantBits());
            ByteConverter.int8(val, 8, uuid.getLeastSignificantBits());
            this.bindBytes(parameterIndex, val, 2950);
        }
        else {
            this.bindLiteral(parameterIndex, uuid.toString(), 2950);
        }
    }
    
    @Override
    public void setURL(final int parameterIndex, final URL x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setURL(int,URL)");
    }
    
    @Override
    public int[] executeBatch() throws SQLException {
        try {
            if (this.batchParameters != null && this.batchParameters.size() > 1 && this.mPrepareThreshold > 0) {
                this.preparedQuery.increaseExecuteCount(this.mPrepareThreshold);
            }
            return super.executeBatch();
        }
        finally {
            this.defaultTimeZone = null;
        }
    }
    
    private Calendar getDefaultCalendar() {
        final TimestampUtils timestampUtils = this.connection.getTimestampUtils();
        if (timestampUtils.hasFastDefaultTimeZone()) {
            return timestampUtils.getSharedCalendar(null);
        }
        final Calendar sharedCalendar = timestampUtils.getSharedCalendar(this.defaultTimeZone);
        if (this.defaultTimeZone == null) {
            this.defaultTimeZone = sharedCalendar.getTimeZone();
        }
        return sharedCalendar;
    }
    
    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        final int flags = 49;
        final StatementResultHandler handler = new StatementResultHandler();
        this.connection.getQueryExecutor().execute(this.preparedQuery.query, this.preparedParameters, handler, 0, 0, flags);
        final int[] oids = this.preparedParameters.getTypeOIDs();
        return this.createParameterMetaData(this.connection, oids);
    }
    
    @Override
    protected void transformQueriesAndParameters() throws SQLException {
        final ArrayList<ParameterList> batchParameters = this.batchParameters;
        if (batchParameters == null || batchParameters.size() <= 1 || !(this.preparedQuery.query instanceof BatchedQuery)) {
            return;
        }
        final BatchedQuery originalQuery = (BatchedQuery)this.preparedQuery.query;
        final int bindCount = originalQuery.getBindCount();
        final int highestBlockCount = 128;
        final int maxValueBlocks = (bindCount == 0) ? 1024 : Integer.highestOneBit(Math.min(Math.max(1, 32766 / bindCount), 128));
        int unprocessedBatchCount = batchParameters.size();
        final int fullValueBlocksCount = unprocessedBatchCount / maxValueBlocks;
        final int partialValueBlocksCount = Integer.bitCount(unprocessedBatchCount % maxValueBlocks);
        final int count = fullValueBlocksCount + partialValueBlocksCount;
        final ArrayList<Query> newBatchStatements = new ArrayList<Query>(count);
        final ArrayList<ParameterList> newBatchParameters = new ArrayList<ParameterList>(count);
        int offset = 0;
        for (int i = 0; i < count; ++i) {
            int valueBlock;
            if (unprocessedBatchCount >= maxValueBlocks) {
                valueBlock = maxValueBlocks;
            }
            else {
                valueBlock = Integer.highestOneBit(unprocessedBatchCount);
            }
            final BatchedQuery bq = originalQuery.deriveForMultiBatch(valueBlock);
            final ParameterList newPl = bq.createParameterList();
            for (int j = 0; j < valueBlock; ++j) {
                final ParameterList pl = batchParameters.get(offset++);
                if (pl != null) {
                    newPl.appendAll(pl);
                }
            }
            newBatchStatements.add(bq);
            newBatchParameters.add(newPl);
            unprocessedBatchCount -= valueBlock;
        }
        this.batchStatements = newBatchStatements;
        this.batchParameters = newBatchParameters;
    }
}
