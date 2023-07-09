// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

import org.postgresql.core.ResultHandlerBase;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLType;
import java.time.LocalDate;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.sql.SQLXML;
import java.util.Collection;
import java.util.UUID;
import java.math.RoundingMode;
import org.postgresql.core.TypeInfo;
import org.postgresql.util.PGtokenizer;
import java.util.Locale;
import java.io.ByteArrayInputStream;
import org.postgresql.util.ByteConverter;
import org.postgresql.core.Encoding;
import org.postgresql.util.PGbytea;
import org.postgresql.util.PGobject;
import java.util.StringTokenizer;
import org.postgresql.core.ParameterList;
import java.util.ArrayList;
import org.postgresql.PGResultSetMetaData;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.Iterator;
import org.postgresql.util.JdbcBlackHole;
import org.postgresql.core.ResultHandler;
import java.sql.Statement;
import java.sql.Ref;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;
import java.sql.Time;
import org.postgresql.core.Oid;
import java.sql.Timestamp;
import java.sql.Date;
import java.sql.Clob;
import java.io.CharArrayReader;
import java.io.Reader;
import java.sql.Blob;
import java.math.BigDecimal;
import java.sql.Array;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.dataflow.qual.Pure;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;
import org.postgresql.util.HStoreConverter;
import org.postgresql.core.Utils;
import java.util.Calendar;
import org.postgresql.util.internal.Nullness;
import org.postgresql.Driver;
import java.util.logging.Level;
import java.net.URL;
import java.sql.SQLException;
import java.math.BigInteger;
import java.sql.ResultSetMetaData;
import java.util.Map;
import org.postgresql.core.ResultCursor;
import java.sql.SQLWarning;
import org.postgresql.core.Tuple;
import org.postgresql.core.Query;
import org.postgresql.core.Field;
import org.postgresql.core.BaseStatement;
import org.postgresql.core.BaseConnection;
import java.util.TimeZone;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.HashMap;
import org.postgresql.PGRefCursorResultSet;
import java.sql.ResultSet;

public class PgResultSet implements ResultSet, PGRefCursorResultSet
{
    private boolean updateable;
    private boolean doingUpdates;
    private HashMap<String, Object> updateValues;
    private boolean usingOID;
    private List<PrimaryKey> primaryKeys;
    private boolean singleTable;
    private String onlyTable;
    private String tableName;
    private PreparedStatement deleteStatement;
    private final int resultsettype;
    private final int resultsetconcurrency;
    private int fetchdirection;
    private TimeZone defaultTimeZone;
    protected final BaseConnection connection;
    protected final BaseStatement statement;
    protected final Field[] fields;
    protected final Query originalQuery;
    protected final int maxRows;
    protected final int maxFieldSize;
    protected List<Tuple> rows;
    protected int currentRow;
    protected int rowOffset;
    protected Tuple thisRow;
    protected SQLWarning warnings;
    protected boolean wasNullFlag;
    protected boolean onInsertRow;
    private Tuple rowBuffer;
    protected int fetchSize;
    protected ResultCursor cursor;
    private Map<String, Integer> columnNameIndexMap;
    private ResultSetMetaData rsMetaData;
    private String refCursorName;
    private static final BigInteger BYTEMAX;
    private static final BigInteger BYTEMIN;
    private static final NumberFormatException FAST_NUMBER_FAILED;
    private static final BigInteger SHORTMAX;
    private static final BigInteger SHORTMIN;
    private static final BigInteger INTMAX;
    private static final BigInteger INTMIN;
    private static final BigInteger LONGMAX;
    private static final BigInteger LONGMIN;
    
    protected ResultSetMetaData createMetaData() throws SQLException {
        return new PgResultSetMetaData(this.connection, this.fields);
    }
    
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        this.checkClosed();
        if (this.rsMetaData == null) {
            this.rsMetaData = this.createMetaData();
        }
        return this.rsMetaData;
    }
    
    PgResultSet(final Query originalQuery, final BaseStatement statement, final Field[] fields, final List<Tuple> tuples, final ResultCursor cursor, final int maxRows, final int maxFieldSize, final int rsType, final int rsConcurrency, final int rsHoldability) throws SQLException {
        this.updateable = false;
        this.doingUpdates = false;
        this.updateValues = null;
        this.usingOID = false;
        this.singleTable = false;
        this.onlyTable = "";
        this.tableName = null;
        this.deleteStatement = null;
        this.fetchdirection = 1002;
        this.currentRow = -1;
        this.warnings = null;
        this.wasNullFlag = false;
        this.onInsertRow = false;
        this.rowBuffer = null;
        if (tuples == null) {
            throw new NullPointerException("tuples must be non-null");
        }
        if (fields == null) {
            throw new NullPointerException("fields must be non-null");
        }
        this.originalQuery = originalQuery;
        this.connection = (BaseConnection)statement.getConnection();
        this.statement = statement;
        this.fields = fields;
        this.rows = tuples;
        this.cursor = cursor;
        this.maxRows = maxRows;
        this.maxFieldSize = maxFieldSize;
        this.resultsettype = rsType;
        this.resultsetconcurrency = rsConcurrency;
    }
    
    @Override
    public URL getURL(final int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getURL columnIndex: {0}", columnIndex);
        this.checkClosed();
        throw Driver.notImplemented(this.getClass(), "getURL(int)");
    }
    
    @Override
    public URL getURL(final String columnName) throws SQLException {
        return this.getURL(this.findColumn(columnName));
    }
    
    @RequiresNonNull({ "thisRow" })
    protected Object internalGetObject(final int columnIndex, final Field field) throws SQLException {
        Nullness.castNonNull(this.thisRow, "thisRow");
        switch (this.getSQLType(columnIndex)) {
            case -7:
            case 16: {
                return this.getBoolean(columnIndex);
            }
            case 2009: {
                return this.getSQLXML(columnIndex);
            }
            case -6:
            case 4:
            case 5: {
                return this.getInt(columnIndex);
            }
            case -5: {
                return this.getLong(columnIndex);
            }
            case 2:
            case 3: {
                return this.getNumeric(columnIndex, (field.getMod() == -1) ? -1 : (field.getMod() - 4 & 0xFFFF), true);
            }
            case 7: {
                return this.getFloat(columnIndex);
            }
            case 6:
            case 8: {
                return this.getDouble(columnIndex);
            }
            case -1:
            case 1:
            case 12: {
                return this.getString(columnIndex);
            }
            case 91: {
                return this.getDate(columnIndex);
            }
            case 92: {
                return this.getTime(columnIndex);
            }
            case 93: {
                return this.getTimestamp(columnIndex, null);
            }
            case -4:
            case -3:
            case -2: {
                return this.getBytes(columnIndex);
            }
            case 2003: {
                return this.getArray(columnIndex);
            }
            case 2005: {
                return this.getClob(columnIndex);
            }
            case 2004: {
                return this.getBlob(columnIndex);
            }
            default: {
                final String type = this.getPGType(columnIndex);
                if (type.equals("unknown")) {
                    return this.getString(columnIndex);
                }
                if (type.equals("uuid")) {
                    if (this.isBinary(columnIndex)) {
                        return this.getUUID(Nullness.castNonNull(this.thisRow.get(columnIndex - 1)));
                    }
                    return this.getUUID(Nullness.castNonNull(this.getString(columnIndex)));
                }
                else {
                    if (type.equals("refcursor")) {
                        final String cursorName = Nullness.castNonNull(this.getString(columnIndex));
                        final StringBuilder sb = new StringBuilder("FETCH ALL IN ");
                        Utils.escapeIdentifier(sb, cursorName);
                        final ResultSet rs = this.connection.execSQLQuery(sb.toString(), this.resultsettype, 1007);
                        sb.setLength(0);
                        sb.append("CLOSE ");
                        Utils.escapeIdentifier(sb, cursorName);
                        this.connection.execSQLUpdate(sb.toString());
                        ((PgResultSet)rs).setRefCursor(cursorName);
                        return rs;
                    }
                    if (!"hstore".equals(type)) {
                        return null;
                    }
                    if (this.isBinary(columnIndex)) {
                        return HStoreConverter.fromBytes(Nullness.castNonNull(this.thisRow.get(columnIndex - 1)), this.connection.getEncoding());
                    }
                    return HStoreConverter.fromString(Nullness.castNonNull(this.getString(columnIndex)));
                }
                break;
            }
        }
    }
    
    @Pure
    @EnsuresNonNull({ "rows" })
    private void checkScrollable() throws SQLException {
        this.checkClosed();
        if (this.resultsettype == 1003) {
            throw new PSQLException(GT.tr("Operation requires a scrollable ResultSet, but this ResultSet is FORWARD_ONLY.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
    }
    
    @Override
    public boolean absolute(final int index) throws SQLException {
        this.checkScrollable();
        if (index == 0) {
            this.beforeFirst();
            return false;
        }
        final int rows_size = this.rows.size();
        int internalIndex;
        if (index < 0) {
            if (index < -rows_size) {
                this.beforeFirst();
                return false;
            }
            internalIndex = rows_size + index;
        }
        else {
            if (index > rows_size) {
                this.afterLast();
                return false;
            }
            internalIndex = index - 1;
        }
        this.currentRow = internalIndex;
        this.initRowBuffer();
        this.onInsertRow = false;
        return true;
    }
    
    @Override
    public void afterLast() throws SQLException {
        this.checkScrollable();
        final int rows_size = this.rows.size();
        if (rows_size > 0) {
            this.currentRow = rows_size;
        }
        this.onInsertRow = false;
        this.thisRow = null;
        this.rowBuffer = null;
    }
    
    @Override
    public void beforeFirst() throws SQLException {
        this.checkScrollable();
        if (!this.rows.isEmpty()) {
            this.currentRow = -1;
        }
        this.onInsertRow = false;
        this.thisRow = null;
        this.rowBuffer = null;
    }
    
    @Override
    public boolean first() throws SQLException {
        this.checkScrollable();
        if (this.rows.size() <= 0) {
            return false;
        }
        this.currentRow = 0;
        this.initRowBuffer();
        this.onInsertRow = false;
        return true;
    }
    
    @Override
    public Array getArray(final String colName) throws SQLException {
        return this.getArray(this.findColumn(colName));
    }
    
    protected Array makeArray(final int oid, final byte[] value) throws SQLException {
        return new PgArray(this.connection, oid, value);
    }
    
    protected Array makeArray(final int oid, final String value) throws SQLException {
        return new PgArray(this.connection, oid, value);
    }
    
    @Pure
    @Override
    public Array getArray(final int i) throws SQLException {
        final byte[] value = this.getRawValue(i);
        if (value == null) {
            return null;
        }
        final int oid = this.fields[i - 1].getOID();
        if (this.isBinary(i)) {
            return this.makeArray(oid, value);
        }
        return this.makeArray(oid, Nullness.castNonNull(this.getFixedString(i)));
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
        return this.getBigDecimal(columnIndex, -1);
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnName) throws SQLException {
        return this.getBigDecimal(this.findColumn(columnName));
    }
    
    @Override
    public Blob getBlob(final String columnName) throws SQLException {
        return this.getBlob(this.findColumn(columnName));
    }
    
    protected Blob makeBlob(final long oid) throws SQLException {
        return new PgBlob(this.connection, oid);
    }
    
    @Pure
    @Override
    public Blob getBlob(final int i) throws SQLException {
        final byte[] value = this.getRawValue(i);
        if (value == null) {
            return null;
        }
        return this.makeBlob(this.getLong(i));
    }
    
    @Override
    public Reader getCharacterStream(final String columnName) throws SQLException {
        return this.getCharacterStream(this.findColumn(columnName));
    }
    
    @Override
    public Reader getCharacterStream(final int i) throws SQLException {
        final String value = this.getString(i);
        if (value == null) {
            return null;
        }
        return new CharArrayReader(value.toCharArray());
    }
    
    @Override
    public Clob getClob(final String columnName) throws SQLException {
        return this.getClob(this.findColumn(columnName));
    }
    
    protected Clob makeClob(final long oid) throws SQLException {
        return new PgClob(this.connection, oid);
    }
    
    @Pure
    @Override
    public Clob getClob(final int i) throws SQLException {
        final byte[] value = this.getRawValue(i);
        if (value == null) {
            return null;
        }
        return this.makeClob(this.getLong(i));
    }
    
    @Override
    public int getConcurrency() throws SQLException {
        this.checkClosed();
        return this.resultsetconcurrency;
    }
    
    @Override
    public Date getDate(final int i, Calendar cal) throws SQLException {
        final byte[] value = this.getRawValue(i);
        if (value == null) {
            return null;
        }
        if (cal == null) {
            cal = this.getDefaultCalendar();
        }
        if (!this.isBinary(i)) {
            return this.connection.getTimestampUtils().toDate(cal, Nullness.castNonNull(this.getString(i)));
        }
        final int col = i - 1;
        final int oid = this.fields[col].getOID();
        final TimeZone tz = cal.getTimeZone();
        if (oid == 1082) {
            return this.connection.getTimestampUtils().toDateBin(tz, value);
        }
        if (oid == 1114 || oid == 1184) {
            final Timestamp timestamp = Nullness.castNonNull(this.getTimestamp(i, cal));
            return this.connection.getTimestampUtils().convertToDate(timestamp.getTime(), tz);
        }
        throw new PSQLException(GT.tr("Cannot convert the column of type {0} to requested type {1}.", Oid.toString(oid), "date"), PSQLState.DATA_TYPE_MISMATCH);
    }
    
    @Override
    public Time getTime(final int i, Calendar cal) throws SQLException {
        final byte[] value = this.getRawValue(i);
        if (value == null) {
            return null;
        }
        if (cal == null) {
            cal = this.getDefaultCalendar();
        }
        if (!this.isBinary(i)) {
            final String string = this.getString(i);
            return this.connection.getTimestampUtils().toTime(cal, string);
        }
        final int col = i - 1;
        final int oid = this.fields[col].getOID();
        final TimeZone tz = cal.getTimeZone();
        if (oid == 1083 || oid == 1266) {
            return this.connection.getTimestampUtils().toTimeBin(tz, value);
        }
        if (oid != 1114 && oid != 1184) {
            throw new PSQLException(GT.tr("Cannot convert the column of type {0} to requested type {1}.", Oid.toString(oid), "time"), PSQLState.DATA_TYPE_MISMATCH);
        }
        final Timestamp timestamp = this.getTimestamp(i, cal);
        if (timestamp == null) {
            return null;
        }
        final long timeMillis = timestamp.getTime();
        if (oid == 1184) {
            return new Time(timeMillis % TimeUnit.DAYS.toMillis(1L));
        }
        return this.connection.getTimestampUtils().convertToTime(timeMillis, tz);
    }
    
    private LocalTime getLocalTime(final int i) throws SQLException {
        final byte[] value = this.getRawValue(i);
        if (value == null) {
            return null;
        }
        if (!this.isBinary(i)) {
            final String string = this.getString(i);
            return this.connection.getTimestampUtils().toLocalTime(string);
        }
        final int col = i - 1;
        final int oid = this.fields[col].getOID();
        if (oid == 1083) {
            return this.connection.getTimestampUtils().toLocalTimeBin(value);
        }
        throw new PSQLException(GT.tr("Cannot convert the column of type {0} to requested type {1}.", Oid.toString(oid), "time"), PSQLState.DATA_TYPE_MISMATCH);
    }
    
    @Pure
    @Override
    public Timestamp getTimestamp(final int i, Calendar cal) throws SQLException {
        final byte[] value = this.getRawValue(i);
        if (value == null) {
            return null;
        }
        if (cal == null) {
            cal = this.getDefaultCalendar();
        }
        final int col = i - 1;
        final int oid = this.fields[col].getOID();
        if (this.isBinary(i)) {
            if (oid == 1184 || oid == 1114) {
                final boolean hasTimeZone = oid == 1184;
                final TimeZone tz = cal.getTimeZone();
                return this.connection.getTimestampUtils().toTimestampBin(tz, value, hasTimeZone);
            }
            long millis;
            if (oid == 1083 || oid == 1266) {
                final Time time = this.getTime(i, cal);
                if (time == null) {
                    return null;
                }
                millis = time.getTime();
            }
            else {
                if (oid != 1082) {
                    throw new PSQLException(GT.tr("Cannot convert the column of type {0} to requested type {1}.", Oid.toString(oid), "timestamp"), PSQLState.DATA_TYPE_MISMATCH);
                }
                final Date date = this.getDate(i, cal);
                if (date == null) {
                    return null;
                }
                millis = date.getTime();
            }
            return new Timestamp(millis);
        }
        else {
            final String string = Nullness.castNonNull(this.getString(i));
            if (oid == 1083 || oid == 1266) {
                return new Timestamp(this.connection.getTimestampUtils().toTime(cal, string).getTime());
            }
            return this.connection.getTimestampUtils().toTimestamp(cal, string);
        }
    }
    
    private OffsetDateTime getOffsetDateTime(final int i) throws SQLException {
        final byte[] value = this.getRawValue(i);
        if (value == null) {
            return null;
        }
        final int col = i - 1;
        final int oid = this.fields[col].getOID();
        if (this.isBinary(i)) {
            if (oid == 1184 || oid == 1114) {
                return this.connection.getTimestampUtils().toOffsetDateTimeBin(value);
            }
            if (oid != 1266) {
                throw new PSQLException(GT.tr("Cannot convert the column of type {0} to requested type {1}.", Oid.toString(oid), "timestamptz"), PSQLState.DATA_TYPE_MISMATCH);
            }
            final Time time = this.getTime(i);
            if (time == null) {
                return null;
            }
            return this.connection.getTimestampUtils().toOffsetDateTime(time);
        }
        else {
            final String string = Nullness.castNonNull(this.getString(i));
            if (oid == 1266) {
                final Calendar cal = this.getDefaultCalendar();
                final Time time2 = this.connection.getTimestampUtils().toTime(cal, string);
                return this.connection.getTimestampUtils().toOffsetDateTime(time2);
            }
            return this.connection.getTimestampUtils().toOffsetDateTime(string);
        }
    }
    
    private LocalDateTime getLocalDateTime(final int i) throws SQLException {
        final byte[] value = this.getRawValue(i);
        if (value == null) {
            return null;
        }
        final int col = i - 1;
        final int oid = this.fields[col].getOID();
        if (oid != 1114) {
            throw new PSQLException(GT.tr("Cannot convert the column of type {0} to requested type {1}.", Oid.toString(oid), "timestamp"), PSQLState.DATA_TYPE_MISMATCH);
        }
        if (this.isBinary(i)) {
            return this.connection.getTimestampUtils().toLocalDateTimeBin(value);
        }
        final String string = Nullness.castNonNull(this.getString(i));
        return this.connection.getTimestampUtils().toLocalDateTime(string);
    }
    
    @Override
    public Date getDate(final String c, final Calendar cal) throws SQLException {
        return this.getDate(this.findColumn(c), cal);
    }
    
    @Override
    public Time getTime(final String c, final Calendar cal) throws SQLException {
        return this.getTime(this.findColumn(c), cal);
    }
    
    @Override
    public Timestamp getTimestamp(final String c, final Calendar cal) throws SQLException {
        return this.getTimestamp(this.findColumn(c), cal);
    }
    
    @Override
    public int getFetchDirection() throws SQLException {
        this.checkClosed();
        return this.fetchdirection;
    }
    
    public Object getObjectImpl(final String columnName, final Map<String, Class<?>> map) throws SQLException {
        return this.getObjectImpl(this.findColumn(columnName), map);
    }
    
    public Object getObjectImpl(final int i, final Map<String, Class<?>> map) throws SQLException {
        this.checkClosed();
        if (map == null || map.isEmpty()) {
            return this.getObject(i);
        }
        throw Driver.notImplemented(this.getClass(), "getObjectImpl(int,Map)");
    }
    
    @Override
    public Ref getRef(final String columnName) throws SQLException {
        return this.getRef(this.findColumn(columnName));
    }
    
    @Override
    public Ref getRef(final int i) throws SQLException {
        this.checkClosed();
        throw Driver.notImplemented(this.getClass(), "getRef(int)");
    }
    
    @Override
    public int getRow() throws SQLException {
        this.checkClosed();
        if (this.onInsertRow) {
            return 0;
        }
        final int rows_size = this.rows.size();
        if (this.currentRow < 0 || this.currentRow >= rows_size) {
            return 0;
        }
        return this.rowOffset + this.currentRow + 1;
    }
    
    @Override
    public Statement getStatement() throws SQLException {
        this.checkClosed();
        return this.statement;
    }
    
    @Override
    public int getType() throws SQLException {
        this.checkClosed();
        return this.resultsettype;
    }
    
    @Pure
    @Override
    public boolean isAfterLast() throws SQLException {
        this.checkClosed();
        if (this.onInsertRow) {
            return false;
        }
        Nullness.castNonNull(this.rows, "rows");
        final int rows_size = this.rows.size();
        return this.rowOffset + rows_size != 0 && this.currentRow >= rows_size;
    }
    
    @Pure
    @Override
    public boolean isBeforeFirst() throws SQLException {
        this.checkClosed();
        return !this.onInsertRow && this.rowOffset + this.currentRow < 0 && !Nullness.castNonNull(this.rows, "rows").isEmpty();
    }
    
    @Override
    public boolean isFirst() throws SQLException {
        this.checkClosed();
        if (this.onInsertRow) {
            return false;
        }
        final int rows_size = this.rows.size();
        return this.rowOffset + rows_size != 0 && this.rowOffset + this.currentRow == 0;
    }
    
    @Override
    public boolean isLast() throws SQLException {
        this.checkClosed();
        if (this.onInsertRow) {
            return false;
        }
        List<Tuple> rows = Nullness.castNonNull(this.rows, "rows");
        final int rows_size = rows.size();
        if (rows_size == 0) {
            return false;
        }
        if (this.currentRow != rows_size - 1) {
            return false;
        }
        final ResultCursor cursor = this.cursor;
        if (cursor == null) {
            return true;
        }
        if (this.maxRows > 0 && this.rowOffset + this.currentRow == this.maxRows) {
            return true;
        }
        this.rowOffset += rows_size - 1;
        int fetchRows = this.fetchSize;
        if (this.maxRows != 0 && (fetchRows == 0 || this.rowOffset + fetchRows > this.maxRows)) {
            fetchRows = this.maxRows - this.rowOffset;
        }
        this.connection.getQueryExecutor().fetch(cursor, new CursorResultHandler(), fetchRows);
        rows = Nullness.castNonNull(this.rows, "rows");
        rows.add(0, Nullness.castNonNull(this.thisRow));
        this.currentRow = 0;
        return rows.size() == 1;
    }
    
    @Override
    public boolean last() throws SQLException {
        this.checkScrollable();
        final List<Tuple> rows = Nullness.castNonNull(this.rows, "rows");
        final int rows_size = rows.size();
        if (rows_size <= 0) {
            return false;
        }
        this.currentRow = rows_size - 1;
        this.initRowBuffer();
        this.onInsertRow = false;
        return true;
    }
    
    @Override
    public boolean previous() throws SQLException {
        this.checkScrollable();
        if (this.onInsertRow) {
            throw new PSQLException(GT.tr("Can''t use relative move methods while on the insert row.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        if (this.currentRow - 1 < 0) {
            this.currentRow = -1;
            this.thisRow = null;
            this.rowBuffer = null;
            return false;
        }
        --this.currentRow;
        this.initRowBuffer();
        return true;
    }
    
    @Override
    public boolean relative(final int rows) throws SQLException {
        this.checkScrollable();
        if (this.onInsertRow) {
            throw new PSQLException(GT.tr("Can''t use relative move methods while on the insert row.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        final int index = this.currentRow + 1 + rows;
        if (index < 0) {
            this.beforeFirst();
            return false;
        }
        return this.absolute(index);
    }
    
    @Override
    public void setFetchDirection(final int direction) throws SQLException {
        this.checkClosed();
        switch (direction) {
            case 1000: {
                break;
            }
            case 1001:
            case 1002: {
                this.checkScrollable();
                break;
            }
            default: {
                throw new PSQLException(GT.tr("Invalid fetch direction constant: {0}.", direction), PSQLState.INVALID_PARAMETER_VALUE);
            }
        }
        this.fetchdirection = direction;
    }
    
    @Override
    public synchronized void cancelRowUpdates() throws SQLException {
        this.checkClosed();
        if (this.onInsertRow) {
            throw new PSQLException(GT.tr("Cannot call cancelRowUpdates() when on the insert row.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        if (this.doingUpdates) {
            this.doingUpdates = false;
            this.clearRowBuffer(true);
        }
    }
    
    @Override
    public synchronized void deleteRow() throws SQLException {
        this.checkUpdateable();
        if (this.onInsertRow) {
            throw new PSQLException(GT.tr("Cannot call deleteRow() when on the insert row.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        if (this.isBeforeFirst()) {
            throw new PSQLException(GT.tr("Currently positioned before the start of the ResultSet.  You cannot call deleteRow() here.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        if (this.isAfterLast()) {
            throw new PSQLException(GT.tr("Currently positioned after the end of the ResultSet.  You cannot call deleteRow() here.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        final List<Tuple> rows = Nullness.castNonNull(this.rows, "rows");
        if (rows.isEmpty()) {
            throw new PSQLException(GT.tr("There are no rows in this ResultSet.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        final List<PrimaryKey> primaryKeys = Nullness.castNonNull(this.primaryKeys, "primaryKeys");
        final int numKeys = primaryKeys.size();
        if (this.deleteStatement == null) {
            final StringBuilder deleteSQL = new StringBuilder("DELETE FROM ").append(this.onlyTable).append(this.tableName).append(" where ");
            for (int i = 0; i < numKeys; ++i) {
                Utils.escapeIdentifier(deleteSQL, primaryKeys.get(i).name);
                deleteSQL.append(" = ?");
                if (i < numKeys - 1) {
                    deleteSQL.append(" and ");
                }
            }
            this.deleteStatement = this.connection.prepareStatement(deleteSQL.toString());
        }
        this.deleteStatement.clearParameters();
        for (int j = 0; j < numKeys; ++j) {
            this.deleteStatement.setObject(j + 1, primaryKeys.get(j).getValue());
        }
        this.deleteStatement.executeUpdate();
        rows.remove(this.currentRow);
        --this.currentRow;
        this.moveToCurrentRow();
    }
    
    @Override
    public synchronized void insertRow() throws SQLException {
        this.checkUpdateable();
        Nullness.castNonNull(this.rows, "rows");
        if (!this.onInsertRow) {
            throw new PSQLException(GT.tr("Not on the insert row.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        final HashMap<String, Object> updateValues = this.updateValues;
        if (updateValues == null || updateValues.isEmpty()) {
            throw new PSQLException(GT.tr("You must specify at least one column value to insert a row.", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE);
        }
        final StringBuilder insertSQL = new StringBuilder("INSERT INTO ").append(this.tableName).append(" (");
        final StringBuilder paramSQL = new StringBuilder(") values (");
        final Iterator<String> columnNames = updateValues.keySet().iterator();
        final int numColumns = updateValues.size();
        int i = 0;
        while (columnNames.hasNext()) {
            final String columnName = columnNames.next();
            Utils.escapeIdentifier(insertSQL, columnName);
            if (i < numColumns - 1) {
                insertSQL.append(", ");
                paramSQL.append("?,");
            }
            else {
                paramSQL.append("?)");
            }
            ++i;
        }
        insertSQL.append(paramSQL.toString());
        PreparedStatement insertStatement = null;
        final Tuple rowBuffer = Nullness.castNonNull(this.rowBuffer);
        try {
            insertStatement = this.connection.prepareStatement(insertSQL.toString(), 1);
            final Iterator<Object> values = updateValues.values().iterator();
            int j = 1;
            while (values.hasNext()) {
                insertStatement.setObject(j, values.next());
                ++j;
            }
            insertStatement.executeUpdate();
            if (this.usingOID) {
                final long insertedOID = ((PgStatement)insertStatement).getLastOID();
                updateValues.put("oid", insertedOID);
            }
            this.updateRowBuffer(insertStatement, rowBuffer, Nullness.castNonNull(updateValues));
        }
        finally {
            JdbcBlackHole.close(insertStatement);
        }
        Nullness.castNonNull(this.rows).add(rowBuffer);
        this.thisRow = rowBuffer;
        this.clearRowBuffer(false);
    }
    
    @Override
    public synchronized void moveToCurrentRow() throws SQLException {
        this.checkUpdateable();
        Nullness.castNonNull(this.rows, "rows");
        if (this.currentRow < 0 || this.currentRow >= this.rows.size()) {
            this.thisRow = null;
            this.rowBuffer = null;
        }
        else {
            this.initRowBuffer();
        }
        this.onInsertRow = false;
        this.doingUpdates = false;
    }
    
    @Override
    public synchronized void moveToInsertRow() throws SQLException {
        this.checkUpdateable();
        this.clearRowBuffer(false);
        this.onInsertRow = true;
        this.doingUpdates = false;
    }
    
    private synchronized void clearRowBuffer(final boolean copyCurrentRow) throws SQLException {
        if (copyCurrentRow) {
            this.rowBuffer = Nullness.castNonNull(this.thisRow, "thisRow").updateableCopy();
        }
        else {
            this.rowBuffer = new Tuple(this.fields.length);
        }
        final HashMap<String, Object> updateValues = this.updateValues;
        if (updateValues != null) {
            updateValues.clear();
        }
    }
    
    @Override
    public boolean rowDeleted() throws SQLException {
        this.checkClosed();
        return false;
    }
    
    @Override
    public boolean rowInserted() throws SQLException {
        this.checkClosed();
        return false;
    }
    
    @Override
    public boolean rowUpdated() throws SQLException {
        this.checkClosed();
        return false;
    }
    
    @Override
    public synchronized void updateAsciiStream(final int columnIndex, final InputStream x, final int length) throws SQLException {
        if (x == null) {
            this.updateNull(columnIndex);
            return;
        }
        try {
            final InputStreamReader reader = new InputStreamReader(x, "ASCII");
            final char[] data = new char[length];
            int numRead = 0;
            do {
                final int n = reader.read(data, numRead, length - numRead);
                if (n == -1) {
                    break;
                }
                numRead += n;
            } while (numRead != length);
            this.updateString(columnIndex, new String(data, 0, numRead));
        }
        catch (final UnsupportedEncodingException uee) {
            throw new PSQLException(GT.tr("The JVM claims not to support the encoding: {0}", "ASCII"), PSQLState.UNEXPECTED_ERROR, uee);
        }
        catch (final IOException ie) {
            throw new PSQLException(GT.tr("Provided InputStream failed.", new Object[0]), (PSQLState)null, ie);
        }
    }
    
    @Override
    public synchronized void updateBigDecimal(final int columnIndex, final BigDecimal x) throws SQLException {
        this.updateValue(columnIndex, x);
    }
    
    @Override
    public synchronized void updateBinaryStream(final int columnIndex, final InputStream x, final int length) throws SQLException {
        if (x == null) {
            this.updateNull(columnIndex);
            return;
        }
        final byte[] data = new byte[length];
        int numRead = 0;
        try {
            do {
                final int n = x.read(data, numRead, length - numRead);
                if (n == -1) {
                    break;
                }
                numRead += n;
            } while (numRead != length);
        }
        catch (final IOException ie) {
            throw new PSQLException(GT.tr("Provided InputStream failed.", new Object[0]), (PSQLState)null, ie);
        }
        if (numRead == length) {
            this.updateBytes(columnIndex, data);
        }
        else {
            final byte[] data2 = new byte[numRead];
            System.arraycopy(data, 0, data2, 0, numRead);
            this.updateBytes(columnIndex, data2);
        }
    }
    
    @Override
    public synchronized void updateBoolean(final int columnIndex, final boolean x) throws SQLException {
        this.updateValue(columnIndex, x);
    }
    
    @Override
    public synchronized void updateByte(final int columnIndex, final byte x) throws SQLException {
        this.updateValue(columnIndex, String.valueOf(x));
    }
    
    @Override
    public synchronized void updateBytes(final int columnIndex, final byte[] x) throws SQLException {
        this.updateValue(columnIndex, x);
    }
    
    @Override
    public synchronized void updateCharacterStream(final int columnIndex, final Reader x, final int length) throws SQLException {
        if (x == null) {
            this.updateNull(columnIndex);
            return;
        }
        try {
            final char[] data = new char[length];
            int numRead = 0;
            do {
                final int n = x.read(data, numRead, length - numRead);
                if (n == -1) {
                    break;
                }
                numRead += n;
            } while (numRead != length);
            this.updateString(columnIndex, new String(data, 0, numRead));
        }
        catch (final IOException ie) {
            throw new PSQLException(GT.tr("Provided Reader failed.", new Object[0]), (PSQLState)null, ie);
        }
    }
    
    @Override
    public synchronized void updateDate(final int columnIndex, final Date x) throws SQLException {
        this.updateValue(columnIndex, x);
    }
    
    @Override
    public synchronized void updateDouble(final int columnIndex, final double x) throws SQLException {
        this.updateValue(columnIndex, x);
    }
    
    @Override
    public synchronized void updateFloat(final int columnIndex, final float x) throws SQLException {
        this.updateValue(columnIndex, x);
    }
    
    @Override
    public synchronized void updateInt(final int columnIndex, final int x) throws SQLException {
        this.updateValue(columnIndex, x);
    }
    
    @Override
    public synchronized void updateLong(final int columnIndex, final long x) throws SQLException {
        this.updateValue(columnIndex, x);
    }
    
    @Override
    public synchronized void updateNull(final int columnIndex) throws SQLException {
        this.checkColumnIndex(columnIndex);
        final String columnTypeName = this.getPGType(columnIndex);
        this.updateValue(columnIndex, new NullObject(columnTypeName));
    }
    
    @Override
    public synchronized void updateObject(final int columnIndex, final Object x) throws SQLException {
        this.updateValue(columnIndex, x);
    }
    
    @Override
    public synchronized void updateObject(final int columnIndex, final Object x, final int scale) throws SQLException {
        this.updateObject(columnIndex, x);
    }
    
    @Override
    public void refreshRow() throws SQLException {
        this.checkUpdateable();
        if (this.onInsertRow) {
            throw new PSQLException(GT.tr("Can''t refresh the insert row.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        if (this.isBeforeFirst() || this.isAfterLast() || Nullness.castNonNull(this.rows, "rows").isEmpty()) {
            return;
        }
        final StringBuilder selectSQL = new StringBuilder("select ");
        final ResultSetMetaData rsmd = this.getMetaData();
        final PGResultSetMetaData pgmd = (PGResultSetMetaData)rsmd;
        for (int i = 1; i <= rsmd.getColumnCount(); ++i) {
            if (i > 1) {
                selectSQL.append(", ");
            }
            selectSQL.append(pgmd.getBaseColumnName(i));
        }
        selectSQL.append(" from ").append(this.onlyTable).append(this.tableName).append(" where ");
        final List<PrimaryKey> primaryKeys = Nullness.castNonNull(this.primaryKeys, "primaryKeys");
        final int numKeys = primaryKeys.size();
        for (int j = 0; j < numKeys; ++j) {
            final PrimaryKey primaryKey = primaryKeys.get(j);
            selectSQL.append(primaryKey.name).append(" = ?");
            if (j < numKeys - 1) {
                selectSQL.append(" and ");
            }
        }
        final String sqlText = selectSQL.toString();
        if (this.connection.getLogger().isLoggable(Level.FINE)) {
            this.connection.getLogger().log(Level.FINE, "selecting {0}", sqlText);
        }
        PreparedStatement selectStatement = null;
        try {
            selectStatement = this.connection.prepareStatement(sqlText, 1004, 1008);
            for (int k = 0; k < numKeys; ++k) {
                selectStatement.setObject(k + 1, primaryKeys.get(k).getValue());
            }
            final PgResultSet rs = (PgResultSet)selectStatement.executeQuery();
            if (rs.next()) {
                if (rs.thisRow == null) {
                    this.rowBuffer = null;
                }
                else {
                    this.rowBuffer = Nullness.castNonNull(rs.thisRow).updateableCopy();
                }
            }
            Nullness.castNonNull(this.rows).set(this.currentRow, Nullness.castNonNull(this.rowBuffer));
            this.thisRow = this.rowBuffer;
            this.connection.getLogger().log(Level.FINE, "done updates");
            rs.close();
        }
        finally {
            JdbcBlackHole.close(selectStatement);
        }
    }
    
    @Override
    public synchronized void updateRow() throws SQLException {
        this.checkUpdateable();
        if (this.onInsertRow) {
            throw new PSQLException(GT.tr("Cannot call updateRow() when on the insert row.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        final List<Tuple> rows = Nullness.castNonNull(this.rows, "rows");
        if (this.isBeforeFirst() || this.isAfterLast() || rows.isEmpty()) {
            throw new PSQLException(GT.tr("Cannot update the ResultSet because it is either before the start or after the end of the results.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        if (!this.doingUpdates) {
            return;
        }
        final StringBuilder updateSQL = new StringBuilder("UPDATE " + this.onlyTable + this.tableName + " SET  ");
        final HashMap<String, Object> updateValues = Nullness.castNonNull(this.updateValues);
        final int numColumns = updateValues.size();
        final Iterator<String> columns = updateValues.keySet().iterator();
        int i = 0;
        while (columns.hasNext()) {
            final String column = columns.next();
            Utils.escapeIdentifier(updateSQL, column);
            updateSQL.append(" = ?");
            if (i < numColumns - 1) {
                updateSQL.append(", ");
            }
            ++i;
        }
        updateSQL.append(" WHERE ");
        final List<PrimaryKey> primaryKeys = Nullness.castNonNull(this.primaryKeys, "primaryKeys");
        final int numKeys = primaryKeys.size();
        for (int j = 0; j < numKeys; ++j) {
            final PrimaryKey primaryKey = primaryKeys.get(j);
            Utils.escapeIdentifier(updateSQL, primaryKey.name);
            updateSQL.append(" = ?");
            if (j < numKeys - 1) {
                updateSQL.append(" and ");
            }
        }
        final String sqlText = updateSQL.toString();
        if (this.connection.getLogger().isLoggable(Level.FINE)) {
            this.connection.getLogger().log(Level.FINE, "updating {0}", sqlText);
        }
        PreparedStatement updateStatement = null;
        try {
            updateStatement = this.connection.prepareStatement(sqlText);
            int k = 0;
            for (final Object o : updateValues.values()) {
                updateStatement.setObject(k + 1, o);
                ++k;
            }
            for (int l = 0; l < numKeys; ++l, ++k) {
                updateStatement.setObject(k + 1, primaryKeys.get(l).getValue());
            }
            updateStatement.executeUpdate();
        }
        finally {
            JdbcBlackHole.close(updateStatement);
        }
        final Tuple rowBuffer = Nullness.castNonNull(this.rowBuffer, "rowBuffer");
        this.updateRowBuffer(null, rowBuffer, updateValues);
        this.connection.getLogger().log(Level.FINE, "copying data");
        this.thisRow = rowBuffer.readOnlyCopy();
        rows.set(this.currentRow, rowBuffer);
        this.connection.getLogger().log(Level.FINE, "done updates");
        updateValues.clear();
        this.doingUpdates = false;
    }
    
    @Override
    public synchronized void updateShort(final int columnIndex, final short x) throws SQLException {
        this.updateValue(columnIndex, x);
    }
    
    @Override
    public synchronized void updateString(final int columnIndex, final String x) throws SQLException {
        this.updateValue(columnIndex, x);
    }
    
    @Override
    public synchronized void updateTime(final int columnIndex, final Time x) throws SQLException {
        this.updateValue(columnIndex, x);
    }
    
    @Override
    public synchronized void updateTimestamp(final int columnIndex, final Timestamp x) throws SQLException {
        this.updateValue(columnIndex, x);
    }
    
    @Override
    public synchronized void updateNull(final String columnName) throws SQLException {
        this.updateNull(this.findColumn(columnName));
    }
    
    @Override
    public synchronized void updateBoolean(final String columnName, final boolean x) throws SQLException {
        this.updateBoolean(this.findColumn(columnName), x);
    }
    
    @Override
    public synchronized void updateByte(final String columnName, final byte x) throws SQLException {
        this.updateByte(this.findColumn(columnName), x);
    }
    
    @Override
    public synchronized void updateShort(final String columnName, final short x) throws SQLException {
        this.updateShort(this.findColumn(columnName), x);
    }
    
    @Override
    public synchronized void updateInt(final String columnName, final int x) throws SQLException {
        this.updateInt(this.findColumn(columnName), x);
    }
    
    @Override
    public synchronized void updateLong(final String columnName, final long x) throws SQLException {
        this.updateLong(this.findColumn(columnName), x);
    }
    
    @Override
    public synchronized void updateFloat(final String columnName, final float x) throws SQLException {
        this.updateFloat(this.findColumn(columnName), x);
    }
    
    @Override
    public synchronized void updateDouble(final String columnName, final double x) throws SQLException {
        this.updateDouble(this.findColumn(columnName), x);
    }
    
    @Override
    public synchronized void updateBigDecimal(final String columnName, final BigDecimal x) throws SQLException {
        this.updateBigDecimal(this.findColumn(columnName), x);
    }
    
    @Override
    public synchronized void updateString(final String columnName, final String x) throws SQLException {
        this.updateString(this.findColumn(columnName), x);
    }
    
    @Override
    public synchronized void updateBytes(final String columnName, final byte[] x) throws SQLException {
        this.updateBytes(this.findColumn(columnName), x);
    }
    
    @Override
    public synchronized void updateDate(final String columnName, final Date x) throws SQLException {
        this.updateDate(this.findColumn(columnName), x);
    }
    
    @Override
    public synchronized void updateTime(final String columnName, final Time x) throws SQLException {
        this.updateTime(this.findColumn(columnName), x);
    }
    
    @Override
    public synchronized void updateTimestamp(final String columnName, final Timestamp x) throws SQLException {
        this.updateTimestamp(this.findColumn(columnName), x);
    }
    
    @Override
    public synchronized void updateAsciiStream(final String columnName, final InputStream x, final int length) throws SQLException {
        this.updateAsciiStream(this.findColumn(columnName), x, length);
    }
    
    @Override
    public synchronized void updateBinaryStream(final String columnName, final InputStream x, final int length) throws SQLException {
        this.updateBinaryStream(this.findColumn(columnName), x, length);
    }
    
    @Override
    public synchronized void updateCharacterStream(final String columnName, final Reader reader, final int length) throws SQLException {
        this.updateCharacterStream(this.findColumn(columnName), reader, length);
    }
    
    @Override
    public synchronized void updateObject(final String columnName, final Object x, final int scale) throws SQLException {
        this.updateObject(this.findColumn(columnName), x);
    }
    
    @Override
    public synchronized void updateObject(final String columnName, final Object x) throws SQLException {
        this.updateObject(this.findColumn(columnName), x);
    }
    
    boolean isUpdateable() throws SQLException {
        this.checkClosed();
        if (this.resultsetconcurrency == 1007) {
            throw new PSQLException(GT.tr("ResultSets with concurrency CONCUR_READ_ONLY cannot be updated.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        if (this.updateable) {
            return true;
        }
        this.connection.getLogger().log(Level.FINE, "checking if rs is updateable");
        this.parseQuery();
        if (this.tableName == null) {
            this.connection.getLogger().log(Level.FINE, "tableName is not found");
            return false;
        }
        if (!this.singleTable) {
            this.connection.getLogger().log(Level.FINE, "not a single table");
            return false;
        }
        this.usingOID = false;
        this.connection.getLogger().log(Level.FINE, "getting primary keys");
        final List<PrimaryKey> primaryKeys = new ArrayList<PrimaryKey>();
        this.primaryKeys = primaryKeys;
        int i = 0;
        int numPKcolumns = 0;
        final String[] s = quotelessTableName(Nullness.castNonNull(this.tableName));
        final String quotelessTableName = Nullness.castNonNull(s[0]);
        final String quotelessSchemaName = s[1];
        final ResultSet rs = ((PgDatabaseMetaData)this.connection.getMetaData()).getPrimaryUniqueKeys("", quotelessSchemaName, quotelessTableName);
        String lastConstraintName = null;
        while (rs.next()) {
            final String constraintName = Nullness.castNonNull(rs.getString(6));
            if (lastConstraintName == null || !lastConstraintName.equals(constraintName)) {
                if (lastConstraintName != null) {
                    if (i == numPKcolumns && numPKcolumns > 0) {
                        break;
                    }
                    this.connection.getLogger().log(Level.FINE, "no of keys={0} from constraint {1}", new Object[] { i, lastConstraintName });
                }
                i = 0;
                numPKcolumns = 0;
                primaryKeys.clear();
                lastConstraintName = constraintName;
            }
            ++numPKcolumns;
            final boolean isNotNull = rs.getBoolean("IS_NOT_NULL");
            if (isNotNull) {
                final String columnName = Nullness.castNonNull(rs.getString(4));
                final int index = this.findColumnIndex(columnName);
                if (index <= 0) {
                    continue;
                }
                ++i;
                primaryKeys.add(new PrimaryKey(index, columnName));
            }
        }
        rs.close();
        this.connection.getLogger().log(Level.FINE, "no of keys={0} from constraint {1}", new Object[] { i, lastConstraintName });
        this.updateable = (i == numPKcolumns && numPKcolumns > 0);
        this.connection.getLogger().log(Level.FINE, "checking primary key {0}", this.updateable);
        if (!this.updateable) {
            final int oidIndex = this.findColumnIndex("oid");
            if (oidIndex > 0) {
                primaryKeys.add(new PrimaryKey(oidIndex, "oid"));
                this.usingOID = true;
                this.updateable = true;
            }
        }
        if (!this.updateable) {
            throw new PSQLException(GT.tr("No eligible primary or unique key found for table {0}.", this.tableName), PSQLState.INVALID_CURSOR_STATE);
        }
        return this.updateable;
    }
    
    public static String[] quotelessTableName(final String fullname) {
        final String[] parts = { null, "" };
        StringBuilder acc = new StringBuilder();
        boolean betweenQuotes = false;
        for (int i = 0; i < fullname.length(); ++i) {
            final char c = fullname.charAt(i);
            switch (c) {
                case '\"': {
                    if (i < fullname.length() - 1 && fullname.charAt(i + 1) == '\"') {
                        ++i;
                        acc.append(c);
                        break;
                    }
                    betweenQuotes = !betweenQuotes;
                    break;
                }
                case '.': {
                    if (betweenQuotes) {
                        acc.append(c);
                        break;
                    }
                    parts[1] = acc.toString();
                    acc = new StringBuilder();
                    break;
                }
                default: {
                    acc.append(betweenQuotes ? c : Character.toLowerCase(c));
                    break;
                }
            }
        }
        parts[0] = acc.toString();
        return parts;
    }
    
    private void parseQuery() {
        final Query originalQuery = this.originalQuery;
        if (originalQuery == null) {
            return;
        }
        final String sql = originalQuery.toString(null);
        final StringTokenizer st = new StringTokenizer(sql, " \r\t\n");
        boolean tableFound = false;
        final boolean tablesChecked = false;
        String name = "";
        this.singleTable = true;
        while (!tableFound && !tablesChecked && st.hasMoreTokens()) {
            name = st.nextToken();
            if ("from".equalsIgnoreCase(name)) {
                this.tableName = st.nextToken();
                if ("only".equalsIgnoreCase(this.tableName)) {
                    this.tableName = st.nextToken();
                    this.onlyTable = "ONLY ";
                }
                tableFound = true;
            }
        }
    }
    
    private void setRowBufferColumn(final Tuple rowBuffer, final int columnIndex, final Object valueObject) throws SQLException {
        if (valueObject instanceof PGobject) {
            final String value = ((PGobject)valueObject).getValue();
            rowBuffer.set(columnIndex, (byte[])((value == null) ? null : this.connection.encodeString(value)));
        }
        else {
            if (valueObject == null) {
                rowBuffer.set(columnIndex, null);
                return;
            }
            switch (this.getSQLType(columnIndex + 1)) {
                case -7:
                case 16: {
                    rowBuffer.set(columnIndex, this.connection.encodeString(((boolean)valueObject) ? "t" : "f"));
                    return;
                }
                case 91: {
                    rowBuffer.set(columnIndex, this.connection.encodeString(this.connection.getTimestampUtils().toString(this.getDefaultCalendar(), (Date)valueObject)));
                    return;
                }
                case 92: {
                    rowBuffer.set(columnIndex, this.connection.encodeString(this.connection.getTimestampUtils().toString(this.getDefaultCalendar(), (Time)valueObject)));
                    return;
                }
                case 93: {
                    rowBuffer.set(columnIndex, this.connection.encodeString(this.connection.getTimestampUtils().toString(this.getDefaultCalendar(), (Timestamp)valueObject)));
                    return;
                }
                case 0: {
                    return;
                }
                case -4:
                case -3:
                case -2: {
                    if (this.isBinary(columnIndex + 1)) {
                        rowBuffer.set(columnIndex, (byte[])valueObject);
                        return;
                    }
                    try {
                        rowBuffer.set(columnIndex, PGbytea.toPGString((byte[])valueObject).getBytes(this.connection.getEncoding().name()));
                        return;
                    }
                    catch (final UnsupportedEncodingException e) {
                        throw new PSQLException(GT.tr("The JVM claims not to support the encoding: {0}", this.connection.getEncoding().name()), PSQLState.UNEXPECTED_ERROR, e);
                    }
                    break;
                }
            }
            rowBuffer.set(columnIndex, this.connection.encodeString(String.valueOf(valueObject)));
        }
    }
    
    private void updateRowBuffer(final PreparedStatement insertStatement, final Tuple rowBuffer, final HashMap<String, Object> updateValues) throws SQLException {
        for (final Map.Entry<String, Object> entry : updateValues.entrySet()) {
            final int columnIndex = this.findColumn(entry.getKey()) - 1;
            final Object valueObject = entry.getValue();
            this.setRowBufferColumn(rowBuffer, columnIndex, valueObject);
        }
        if (insertStatement == null) {
            return;
        }
        final ResultSet generatedKeys = insertStatement.getGeneratedKeys();
        try {
            generatedKeys.next();
            final List<PrimaryKey> primaryKeys = Nullness.castNonNull(this.primaryKeys);
            for (int numKeys = primaryKeys.size(), i = 0; i < numKeys; ++i) {
                final PrimaryKey key = primaryKeys.get(i);
                final int columnIndex2 = key.index - 1;
                final Object valueObject2 = generatedKeys.getObject(key.name);
                this.setRowBufferColumn(rowBuffer, columnIndex2, valueObject2);
            }
        }
        finally {
            generatedKeys.close();
        }
    }
    
    public BaseStatement getPGStatement() {
        return this.statement;
    }
    
    @Override
    public String getRefCursor() {
        return this.refCursorName;
    }
    
    private void setRefCursor(final String refCursorName) {
        this.refCursorName = refCursorName;
    }
    
    @Override
    public void setFetchSize(final int rows) throws SQLException {
        this.checkClosed();
        if (rows < 0) {
            throw new PSQLException(GT.tr("Fetch size must be a value greater to or equal to 0.", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE);
        }
        this.fetchSize = rows;
    }
    
    @Override
    public int getFetchSize() throws SQLException {
        this.checkClosed();
        return this.fetchSize;
    }
    
    @Override
    public boolean next() throws SQLException {
        this.checkClosed();
        Nullness.castNonNull(this.rows, "rows");
        if (this.onInsertRow) {
            throw new PSQLException(GT.tr("Can''t use relative move methods while on the insert row.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        if (this.currentRow + 1 >= this.rows.size()) {
            final ResultCursor cursor = this.cursor;
            if (cursor == null || (this.maxRows > 0 && this.rowOffset + this.rows.size() >= this.maxRows)) {
                this.currentRow = this.rows.size();
                this.thisRow = null;
                this.rowBuffer = null;
                return false;
            }
            this.rowOffset += this.rows.size();
            int fetchRows = this.fetchSize;
            if (this.maxRows != 0 && (fetchRows == 0 || this.rowOffset + fetchRows > this.maxRows)) {
                fetchRows = this.maxRows - this.rowOffset;
            }
            this.connection.getQueryExecutor().fetch(cursor, new CursorResultHandler(), fetchRows);
            this.currentRow = 0;
            if (this.rows == null || this.rows.isEmpty()) {
                this.thisRow = null;
                this.rowBuffer = null;
                return false;
            }
        }
        else {
            ++this.currentRow;
        }
        this.initRowBuffer();
        return true;
    }
    
    @Override
    public void close() throws SQLException {
        try {
            this.closeInternally();
        }
        finally {
            ((PgStatement)this.statement).checkCompletion();
        }
    }
    
    protected void closeInternally() throws SQLException {
        this.rows = null;
        JdbcBlackHole.close(this.deleteStatement);
        this.deleteStatement = null;
        if (this.cursor != null) {
            this.cursor.close();
            this.cursor = null;
        }
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        this.checkClosed();
        return this.wasNullFlag;
    }
    
    @Pure
    @Override
    public String getString(final int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getString columnIndex: {0}", columnIndex);
        final byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return null;
        }
        if (this.isBinary(columnIndex) && this.getSQLType(columnIndex) != 12) {
            final Field field = this.fields[columnIndex - 1];
            Object obj = this.internalGetObject(columnIndex, field);
            if (obj == null) {
                obj = this.getObject(columnIndex);
                if (obj == null) {
                    return null;
                }
                return obj.toString();
            }
            else {
                if (obj instanceof java.util.Date) {
                    final int oid = field.getOID();
                    return this.connection.getTimestampUtils().timeToString((java.util.Date)obj, oid == 1184 || oid == 1266);
                }
                if ("hstore".equals(this.getPGType(columnIndex))) {
                    return HStoreConverter.toString((Map<?, ?>)obj);
                }
                return this.trimString(columnIndex, obj.toString());
            }
        }
        else {
            final Encoding encoding = this.connection.getEncoding();
            try {
                return this.trimString(columnIndex, encoding.decode(value));
            }
            catch (final IOException ioe) {
                throw new PSQLException(GT.tr("Invalid character data was found.  This is most likely caused by stored data containing characters that are invalid for the character set the database was created in.  The most common example of this is storing 8bit data in a SQL_ASCII database.", new Object[0]), PSQLState.DATA_ERROR, ioe);
            }
        }
    }
    
    @Pure
    @Override
    public boolean getBoolean(final int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getBoolean columnIndex: {0}", columnIndex);
        final byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return false;
        }
        final int col = columnIndex - 1;
        if (16 == this.fields[col].getOID()) {
            final byte[] v = value;
            return 1 == v.length && 116 == v[0];
        }
        if (this.isBinary(columnIndex)) {
            return BooleanTypeUtil.castToBoolean(this.readDoubleValue(value, this.fields[col].getOID(), "boolean"));
        }
        final String stringValue = Nullness.castNonNull(this.getString(columnIndex));
        return BooleanTypeUtil.castToBoolean(stringValue);
    }
    
    @Override
    public byte getByte(final int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getByte columnIndex: {0}", columnIndex);
        final byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return 0;
        }
        if (this.isBinary(columnIndex)) {
            final int col = columnIndex - 1;
            return (byte)this.readLongValue(value, this.fields[col].getOID(), -128L, 127L, "byte");
        }
        String s = this.getString(columnIndex);
        if (s != null) {
            s = s.trim();
            if (s.isEmpty()) {
                return 0;
            }
            try {
                return Byte.parseByte(s);
            }
            catch (final NumberFormatException e) {
                try {
                    final BigDecimal n = new BigDecimal(s);
                    final BigInteger i = n.toBigInteger();
                    final int gt = i.compareTo(PgResultSet.BYTEMAX);
                    final int lt = i.compareTo(PgResultSet.BYTEMIN);
                    if (gt > 0 || lt < 0) {
                        throw new PSQLException(GT.tr("Bad value for type {0} : {1}", "byte", s), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
                    }
                    return i.byteValue();
                }
                catch (final NumberFormatException ex) {
                    throw new PSQLException(GT.tr("Bad value for type {0} : {1}", "byte", s), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
                }
            }
        }
        return 0;
    }
    
    @Override
    public short getShort(final int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getShort columnIndex: {0}", columnIndex);
        final byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return 0;
        }
        if (!this.isBinary(columnIndex)) {
            return toShort(this.getFixedString(columnIndex));
        }
        final int col = columnIndex - 1;
        final int oid = this.fields[col].getOID();
        if (oid == 21) {
            return ByteConverter.int2(value, 0);
        }
        return (short)this.readLongValue(value, oid, -32768L, 32767L, "short");
    }
    
    @Pure
    @Override
    public int getInt(final int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getInt columnIndex: {0}", columnIndex);
        final byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return 0;
        }
        if (!this.isBinary(columnIndex)) {
            final Encoding encoding = this.connection.getEncoding();
            if (encoding.hasAsciiNumbers()) {
                try {
                    return this.getFastInt(value);
                }
                catch (final NumberFormatException ex) {}
            }
            return toInt(this.getFixedString(columnIndex));
        }
        final int col = columnIndex - 1;
        final int oid = this.fields[col].getOID();
        if (oid == 23) {
            return ByteConverter.int4(value, 0);
        }
        return (int)this.readLongValue(value, oid, -2147483648L, 2147483647L, "int");
    }
    
    @Pure
    @Override
    public long getLong(final int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getLong columnIndex: {0}", columnIndex);
        final byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return 0L;
        }
        if (!this.isBinary(columnIndex)) {
            final Encoding encoding = this.connection.getEncoding();
            if (encoding.hasAsciiNumbers()) {
                try {
                    return this.getFastLong(value);
                }
                catch (final NumberFormatException ex) {}
            }
            return toLong(this.getFixedString(columnIndex));
        }
        final int col = columnIndex - 1;
        final int oid = this.fields[col].getOID();
        if (oid == 20) {
            return ByteConverter.int8(value, 0);
        }
        return this.readLongValue(value, oid, Long.MIN_VALUE, Long.MAX_VALUE, "long");
    }
    
    private long getFastLong(final byte[] bytes) throws NumberFormatException {
        if (bytes.length == 0) {
            throw PgResultSet.FAST_NUMBER_FAILED;
        }
        long val = 0L;
        boolean neg;
        int start;
        if (bytes[0] == 45) {
            neg = true;
            start = 1;
            if (bytes.length == 1 || bytes.length > 19) {
                throw PgResultSet.FAST_NUMBER_FAILED;
            }
        }
        else {
            start = 0;
            neg = false;
            if (bytes.length > 18) {
                throw PgResultSet.FAST_NUMBER_FAILED;
            }
        }
        while (start < bytes.length) {
            final byte b = bytes[start++];
            if (b < 48 || b > 57) {
                throw PgResultSet.FAST_NUMBER_FAILED;
            }
            val *= 10L;
            val += b - 48;
        }
        if (neg) {
            val = -val;
        }
        return val;
    }
    
    private int getFastInt(final byte[] bytes) throws NumberFormatException {
        if (bytes.length == 0) {
            throw PgResultSet.FAST_NUMBER_FAILED;
        }
        int val = 0;
        boolean neg;
        int start;
        if (bytes[0] == 45) {
            neg = true;
            start = 1;
            if (bytes.length == 1 || bytes.length > 10) {
                throw PgResultSet.FAST_NUMBER_FAILED;
            }
        }
        else {
            start = 0;
            neg = false;
            if (bytes.length > 9) {
                throw PgResultSet.FAST_NUMBER_FAILED;
            }
        }
        while (start < bytes.length) {
            final byte b = bytes[start++];
            if (b < 48 || b > 57) {
                throw PgResultSet.FAST_NUMBER_FAILED;
            }
            val *= 10;
            val += b - 48;
        }
        if (neg) {
            val = -val;
        }
        return val;
    }
    
    private BigDecimal getFastBigDecimal(final byte[] bytes) throws NumberFormatException {
        if (bytes.length == 0) {
            throw PgResultSet.FAST_NUMBER_FAILED;
        }
        int scale = 0;
        long val = 0L;
        boolean neg;
        int start;
        if (bytes[0] == 45) {
            neg = true;
            start = 1;
            if (bytes.length == 1 || bytes.length > 19) {
                throw PgResultSet.FAST_NUMBER_FAILED;
            }
        }
        else {
            start = 0;
            neg = false;
            if (bytes.length > 18) {
                throw PgResultSet.FAST_NUMBER_FAILED;
            }
        }
        int periodsSeen = 0;
        while (start < bytes.length) {
            final byte b = bytes[start++];
            if (b < 48 || b > 57) {
                if (b != 46) {
                    throw PgResultSet.FAST_NUMBER_FAILED;
                }
                scale = bytes.length - start;
                ++periodsSeen;
            }
            else {
                val *= 10L;
                val += b - 48;
            }
        }
        final int numNonSignChars = neg ? (bytes.length - 1) : bytes.length;
        if (periodsSeen > 1 || periodsSeen == numNonSignChars) {
            throw PgResultSet.FAST_NUMBER_FAILED;
        }
        if (neg) {
            val = -val;
        }
        return BigDecimal.valueOf(val, scale);
    }
    
    @Pure
    @Override
    public float getFloat(final int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getFloat columnIndex: {0}", columnIndex);
        final byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return 0.0f;
        }
        if (!this.isBinary(columnIndex)) {
            return toFloat(this.getFixedString(columnIndex));
        }
        final int col = columnIndex - 1;
        final int oid = this.fields[col].getOID();
        if (oid == 700) {
            return ByteConverter.float4(value, 0);
        }
        return (float)this.readDoubleValue(value, oid, "float");
    }
    
    @Pure
    @Override
    public double getDouble(final int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getDouble columnIndex: {0}", columnIndex);
        final byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return 0.0;
        }
        if (!this.isBinary(columnIndex)) {
            return toDouble(this.getFixedString(columnIndex));
        }
        final int col = columnIndex - 1;
        final int oid = this.fields[col].getOID();
        if (oid == 701) {
            return ByteConverter.float8(value, 0);
        }
        return this.readDoubleValue(value, oid, "double");
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getBigDecimal columnIndex: {0}", columnIndex);
        return (BigDecimal)this.getNumeric(columnIndex, scale, false);
    }
    
    @Pure
    private Number getNumeric(final int columnIndex, final int scale, final boolean allowNaN) throws SQLException {
        final byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return null;
        }
        if (this.isBinary(columnIndex)) {
            final int sqlType = this.getSQLType(columnIndex);
            if (sqlType != 2 && sqlType != 3) {
                final Object obj = this.internalGetObject(columnIndex, this.fields[columnIndex - 1]);
                if (obj == null) {
                    return null;
                }
                if (obj instanceof Long || obj instanceof Integer || obj instanceof Byte) {
                    BigDecimal res = BigDecimal.valueOf(((Number)obj).longValue());
                    res = this.scaleBigDecimal(res, scale);
                    return res;
                }
                return this.toBigDecimal(this.trimMoney(String.valueOf(obj)), scale);
            }
            else {
                final Number num = ByteConverter.numeric(value);
                if (allowNaN && Double.isNaN(num.doubleValue())) {
                    return Double.NaN;
                }
                return num;
            }
        }
        else {
            final Encoding encoding = this.connection.getEncoding();
            if (encoding.hasAsciiNumbers()) {
                try {
                    BigDecimal res2 = this.getFastBigDecimal(value);
                    res2 = this.scaleBigDecimal(res2, scale);
                    return res2;
                }
                catch (final NumberFormatException ex) {}
            }
            final String stringValue = this.getFixedString(columnIndex);
            if (allowNaN && "NaN".equalsIgnoreCase(stringValue)) {
                return Double.NaN;
            }
            return this.toBigDecimal(stringValue, scale);
        }
    }
    
    @Pure
    @Override
    public byte[] getBytes(final int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getBytes columnIndex: {0}", columnIndex);
        final byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return null;
        }
        if (this.isBinary(columnIndex)) {
            return value;
        }
        if (this.fields[columnIndex - 1].getOID() == 17) {
            return this.trimBytes(columnIndex, PGbytea.toBytes(value));
        }
        return this.trimBytes(columnIndex, value);
    }
    
    @Pure
    @Override
    public Date getDate(final int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getDate columnIndex: {0}", columnIndex);
        return this.getDate(columnIndex, null);
    }
    
    @Pure
    @Override
    public Time getTime(final int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getTime columnIndex: {0}", columnIndex);
        return this.getTime(columnIndex, null);
    }
    
    @Pure
    @Override
    public Timestamp getTimestamp(final int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getTimestamp columnIndex: {0}", columnIndex);
        return this.getTimestamp(columnIndex, null);
    }
    
    @Pure
    @Override
    public InputStream getAsciiStream(final int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getAsciiStream columnIndex: {0}", columnIndex);
        final byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return null;
        }
        try {
            final String stringValue = Nullness.castNonNull(this.getString(columnIndex));
            return new ByteArrayInputStream(stringValue.getBytes("ASCII"));
        }
        catch (final UnsupportedEncodingException l_uee) {
            throw new PSQLException(GT.tr("The JVM claims not to support the encoding: {0}", "ASCII"), PSQLState.UNEXPECTED_ERROR, l_uee);
        }
    }
    
    @Pure
    @Override
    public InputStream getUnicodeStream(final int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getUnicodeStream columnIndex: {0}", columnIndex);
        final byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return null;
        }
        try {
            final String stringValue = Nullness.castNonNull(this.getString(columnIndex));
            return new ByteArrayInputStream(stringValue.getBytes("UTF-8"));
        }
        catch (final UnsupportedEncodingException l_uee) {
            throw new PSQLException(GT.tr("The JVM claims not to support the encoding: {0}", "UTF-8"), PSQLState.UNEXPECTED_ERROR, l_uee);
        }
    }
    
    @Pure
    @Override
    public InputStream getBinaryStream(final int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getBinaryStream columnIndex: {0}", columnIndex);
        final byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return null;
        }
        final byte[] b = this.getBytes(columnIndex);
        if (b != null) {
            return new ByteArrayInputStream(b);
        }
        return null;
    }
    
    @Pure
    @Override
    public String getString(final String columnName) throws SQLException {
        return this.getString(this.findColumn(columnName));
    }
    
    @Pure
    @Override
    public boolean getBoolean(final String columnName) throws SQLException {
        return this.getBoolean(this.findColumn(columnName));
    }
    
    @Pure
    @Override
    public byte getByte(final String columnName) throws SQLException {
        return this.getByte(this.findColumn(columnName));
    }
    
    @Pure
    @Override
    public short getShort(final String columnName) throws SQLException {
        return this.getShort(this.findColumn(columnName));
    }
    
    @Pure
    @Override
    public int getInt(final String columnName) throws SQLException {
        return this.getInt(this.findColumn(columnName));
    }
    
    @Pure
    @Override
    public long getLong(final String columnName) throws SQLException {
        return this.getLong(this.findColumn(columnName));
    }
    
    @Pure
    @Override
    public float getFloat(final String columnName) throws SQLException {
        return this.getFloat(this.findColumn(columnName));
    }
    
    @Pure
    @Override
    public double getDouble(final String columnName) throws SQLException {
        return this.getDouble(this.findColumn(columnName));
    }
    
    @Pure
    @Override
    public BigDecimal getBigDecimal(final String columnName, final int scale) throws SQLException {
        return this.getBigDecimal(this.findColumn(columnName), scale);
    }
    
    @Pure
    @Override
    public byte[] getBytes(final String columnName) throws SQLException {
        return this.getBytes(this.findColumn(columnName));
    }
    
    @Pure
    @Override
    public Date getDate(final String columnName) throws SQLException {
        return this.getDate(this.findColumn(columnName), null);
    }
    
    @Pure
    @Override
    public Time getTime(final String columnName) throws SQLException {
        return this.getTime(this.findColumn(columnName), null);
    }
    
    @Pure
    @Override
    public Timestamp getTimestamp(final String columnName) throws SQLException {
        return this.getTimestamp(this.findColumn(columnName), null);
    }
    
    @Pure
    @Override
    public InputStream getAsciiStream(final String columnName) throws SQLException {
        return this.getAsciiStream(this.findColumn(columnName));
    }
    
    @Pure
    @Override
    public InputStream getUnicodeStream(final String columnName) throws SQLException {
        return this.getUnicodeStream(this.findColumn(columnName));
    }
    
    @Pure
    @Override
    public InputStream getBinaryStream(final String columnName) throws SQLException {
        return this.getBinaryStream(this.findColumn(columnName));
    }
    
    @Pure
    @Override
    public SQLWarning getWarnings() throws SQLException {
        this.checkClosed();
        return this.warnings;
    }
    
    @Override
    public void clearWarnings() throws SQLException {
        this.checkClosed();
        this.warnings = null;
    }
    
    protected void addWarning(final SQLWarning warnings) {
        if (this.warnings != null) {
            this.warnings.setNextWarning(warnings);
        }
        else {
            this.warnings = warnings;
        }
    }
    
    @Override
    public String getCursorName() throws SQLException {
        this.checkClosed();
        return null;
    }
    
    @Override
    public Object getObject(final int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getObject columnIndex: {0}", columnIndex);
        final byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return null;
        }
        final Field field = this.fields[columnIndex - 1];
        if (field == null) {
            this.wasNullFlag = true;
            return null;
        }
        final Object result = this.internalGetObject(columnIndex, field);
        if (result != null) {
            return result;
        }
        if (this.isBinary(columnIndex)) {
            return this.connection.getObject(this.getPGType(columnIndex), null, value);
        }
        final String stringValue = Nullness.castNonNull(this.getString(columnIndex));
        return this.connection.getObject(this.getPGType(columnIndex), stringValue, null);
    }
    
    @Override
    public Object getObject(final String columnName) throws SQLException {
        return this.getObject(this.findColumn(columnName));
    }
    
    @Override
    public int findColumn(final String columnName) throws SQLException {
        this.checkClosed();
        final int col = this.findColumnIndex(columnName);
        if (col == 0) {
            throw new PSQLException(GT.tr("The column name {0} was not found in this ResultSet.", columnName), PSQLState.UNDEFINED_COLUMN);
        }
        return col;
    }
    
    public static Map<String, Integer> createColumnNameIndexMap(final Field[] fields, final boolean isSanitiserDisabled) {
        final Map<String, Integer> columnNameIndexMap = new HashMap<String, Integer>(fields.length * 2);
        for (int i = fields.length - 1; i >= 0; --i) {
            final String columnLabel = fields[i].getColumnLabel();
            if (isSanitiserDisabled) {
                columnNameIndexMap.put(columnLabel, i + 1);
            }
            else {
                columnNameIndexMap.put(columnLabel.toLowerCase(Locale.US), i + 1);
            }
        }
        return columnNameIndexMap;
    }
    
    private int findColumnIndex(final String columnName) {
        if (this.columnNameIndexMap == null) {
            if (this.originalQuery != null) {
                this.columnNameIndexMap = this.originalQuery.getResultSetColumnNameIndexMap();
            }
            if (this.columnNameIndexMap == null) {
                this.columnNameIndexMap = createColumnNameIndexMap(this.fields, this.connection.isColumnSanitiserDisabled());
            }
        }
        Integer index = this.columnNameIndexMap.get(columnName);
        if (index != null) {
            return index;
        }
        index = this.columnNameIndexMap.get(columnName.toLowerCase(Locale.US));
        if (index != null) {
            this.columnNameIndexMap.put(columnName, index);
            return index;
        }
        index = this.columnNameIndexMap.get(columnName.toUpperCase(Locale.US));
        if (index != null) {
            this.columnNameIndexMap.put(columnName, index);
            return index;
        }
        return 0;
    }
    
    public int getColumnOID(final int field) {
        return this.fields[field - 1].getOID();
    }
    
    public String getFixedString(final int col) throws SQLException {
        final String stringValue = Nullness.castNonNull(this.getString(col));
        return this.trimMoney(stringValue);
    }
    
    private String trimMoney(String s) {
        if (s == null) {
            return null;
        }
        if (s.length() < 2) {
            return s;
        }
        final char ch = s.charAt(0);
        if (ch > '-') {
            return s;
        }
        if (ch == '(') {
            s = "-" + PGtokenizer.removePara(s).substring(1);
        }
        else if (ch == '$') {
            s = s.substring(1);
        }
        else if (ch == '-' && s.charAt(1) == '$') {
            s = "-" + s.substring(2);
        }
        return s;
    }
    
    @Pure
    protected String getPGType(final int column) throws SQLException {
        final Field field = this.fields[column - 1];
        this.initSqlType(field);
        return field.getPGType();
    }
    
    @Pure
    protected int getSQLType(final int column) throws SQLException {
        final Field field = this.fields[column - 1];
        this.initSqlType(field);
        return field.getSQLType();
    }
    
    @Pure
    private void initSqlType(final Field field) throws SQLException {
        if (field.isTypeInitialized()) {
            return;
        }
        final TypeInfo typeInfo = this.connection.getTypeInfo();
        final int oid = field.getOID();
        final String pgType = Nullness.castNonNull(typeInfo.getPGType(oid));
        final int sqlType = typeInfo.getSQLType(pgType);
        field.setSQLType(sqlType);
        field.setPGType(pgType);
    }
    
    @EnsuresNonNull({ "updateValues", "rows" })
    private void checkUpdateable() throws SQLException {
        this.checkClosed();
        if (!this.isUpdateable()) {
            throw new PSQLException(GT.tr("ResultSet is not updateable.  The query that generated this result set must select only one table, and must select all primary keys from that table. See the JDBC 2.1 API Specification, section 5.6 for more details.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        if (this.updateValues == null) {
            this.updateValues = new HashMap<String, Object>((int)(this.fields.length / 0.75), 0.75f);
        }
        Nullness.castNonNull(this.updateValues, "updateValues");
        Nullness.castNonNull(this.rows, "rows");
    }
    
    @Pure
    @EnsuresNonNull({ "rows" })
    protected void checkClosed() throws SQLException {
        if (this.rows == null) {
            throw new PSQLException(GT.tr("This ResultSet is closed.", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
    }
    
    protected boolean isResultSetClosed() {
        return this.rows == null;
    }
    
    @Pure
    protected void checkColumnIndex(final int column) throws SQLException {
        if (column < 1 || column > this.fields.length) {
            throw new PSQLException(GT.tr("The column index is out of range: {0}, number of columns: {1}.", column, this.fields.length), PSQLState.INVALID_PARAMETER_VALUE);
        }
    }
    
    @EnsuresNonNull({ "thisRow" })
    protected byte[] getRawValue(final int column) throws SQLException {
        this.checkClosed();
        if (this.thisRow == null) {
            throw new PSQLException(GT.tr("ResultSet not positioned properly, perhaps you need to call next.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        this.checkColumnIndex(column);
        final byte[] bytes = this.thisRow.get(column - 1);
        this.wasNullFlag = (bytes == null);
        return bytes;
    }
    
    @Pure
    protected boolean isBinary(final int column) {
        return this.fields[column - 1].getFormat() == 1;
    }
    
    public static short toShort(String s) throws SQLException {
        if (s != null) {
            try {
                s = s.trim();
                return Short.parseShort(s);
            }
            catch (final NumberFormatException e) {
                try {
                    final BigDecimal n = new BigDecimal(s);
                    final BigInteger i = n.toBigInteger();
                    final int gt = i.compareTo(PgResultSet.SHORTMAX);
                    final int lt = i.compareTo(PgResultSet.SHORTMIN);
                    if (gt > 0 || lt < 0) {
                        throw new PSQLException(GT.tr("Bad value for type {0} : {1}", "short", s), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
                    }
                    return i.shortValue();
                }
                catch (final NumberFormatException ne) {
                    throw new PSQLException(GT.tr("Bad value for type {0} : {1}", "short", s), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
                }
            }
        }
        return 0;
    }
    
    public static int toInt(String s) throws SQLException {
        if (s != null) {
            try {
                s = s.trim();
                return Integer.parseInt(s);
            }
            catch (final NumberFormatException e) {
                try {
                    final BigDecimal n = new BigDecimal(s);
                    final BigInteger i = n.toBigInteger();
                    final int gt = i.compareTo(PgResultSet.INTMAX);
                    final int lt = i.compareTo(PgResultSet.INTMIN);
                    if (gt > 0 || lt < 0) {
                        throw new PSQLException(GT.tr("Bad value for type {0} : {1}", "int", s), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
                    }
                    return i.intValue();
                }
                catch (final NumberFormatException ne) {
                    throw new PSQLException(GT.tr("Bad value for type {0} : {1}", "int", s), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
                }
            }
        }
        return 0;
    }
    
    public static long toLong(String s) throws SQLException {
        if (s != null) {
            try {
                s = s.trim();
                return Long.parseLong(s);
            }
            catch (final NumberFormatException e) {
                try {
                    final BigDecimal n = new BigDecimal(s);
                    final BigInteger i = n.toBigInteger();
                    final int gt = i.compareTo(PgResultSet.LONGMAX);
                    final int lt = i.compareTo(PgResultSet.LONGMIN);
                    if (gt > 0 || lt < 0) {
                        throw new PSQLException(GT.tr("Bad value for type {0} : {1}", "long", s), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
                    }
                    return i.longValue();
                }
                catch (final NumberFormatException ne) {
                    throw new PSQLException(GT.tr("Bad value for type {0} : {1}", "long", s), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
                }
            }
        }
        return 0L;
    }
    
    public static BigDecimal toBigDecimal(String s) throws SQLException {
        if (s == null) {
            return null;
        }
        try {
            s = s.trim();
            return new BigDecimal(s);
        }
        catch (final NumberFormatException e) {
            throw new PSQLException(GT.tr("Bad value for type {0} : {1}", "BigDecimal", s), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
        }
    }
    
    public BigDecimal toBigDecimal(final String s, final int scale) throws SQLException {
        if (s == null) {
            return null;
        }
        final BigDecimal val = toBigDecimal(s);
        return this.scaleBigDecimal(val, scale);
    }
    
    private BigDecimal scaleBigDecimal(final BigDecimal val, final int scale) throws PSQLException {
        if (scale == -1) {
            return val;
        }
        try {
            return val.setScale(scale);
        }
        catch (final ArithmeticException e) {
            throw new PSQLException(GT.tr("Bad value for type {0} : {1}", "BigDecimal", val), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
        }
    }
    
    public static float toFloat(String s) throws SQLException {
        if (s != null) {
            try {
                s = s.trim();
                return Float.parseFloat(s);
            }
            catch (final NumberFormatException e) {
                throw new PSQLException(GT.tr("Bad value for type {0} : {1}", "float", s), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
            }
        }
        return 0.0f;
    }
    
    public static double toDouble(String s) throws SQLException {
        if (s != null) {
            try {
                s = s.trim();
                return Double.parseDouble(s);
            }
            catch (final NumberFormatException e) {
                throw new PSQLException(GT.tr("Bad value for type {0} : {1}", "double", s), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
            }
        }
        return 0.0;
    }
    
    @RequiresNonNull({ "rows" })
    private void initRowBuffer() {
        this.thisRow = Nullness.castNonNull(this.rows, "rows").get(this.currentRow);
        if (this.resultsetconcurrency == 1008) {
            this.rowBuffer = this.thisRow.updateableCopy();
        }
        else {
            this.rowBuffer = null;
        }
    }
    
    private boolean isColumnTrimmable(final int columnIndex) throws SQLException {
        switch (this.getSQLType(columnIndex)) {
            case -4:
            case -3:
            case -2:
            case -1:
            case 1:
            case 12: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    private byte[] trimBytes(final int columnIndex, final byte[] bytes) throws SQLException {
        if (this.maxFieldSize > 0 && bytes.length > this.maxFieldSize && this.isColumnTrimmable(columnIndex)) {
            final byte[] newBytes = new byte[this.maxFieldSize];
            System.arraycopy(bytes, 0, newBytes, 0, this.maxFieldSize);
            return newBytes;
        }
        return bytes;
    }
    
    private String trimString(final int columnIndex, final String string) throws SQLException {
        if (this.maxFieldSize > 0 && string.length() > this.maxFieldSize && this.isColumnTrimmable(columnIndex)) {
            return string.substring(0, this.maxFieldSize);
        }
        return string;
    }
    
    private double readDoubleValue(final byte[] bytes, final int oid, final String targetType) throws PSQLException {
        switch (oid) {
            case 21: {
                return ByteConverter.int2(bytes, 0);
            }
            case 23: {
                return ByteConverter.int4(bytes, 0);
            }
            case 20: {
                return (double)ByteConverter.int8(bytes, 0);
            }
            case 700: {
                return ByteConverter.float4(bytes, 0);
            }
            case 701: {
                return ByteConverter.float8(bytes, 0);
            }
            case 1700: {
                return ByteConverter.numeric(bytes).doubleValue();
            }
            default: {
                throw new PSQLException(GT.tr("Cannot convert the column of type {0} to requested type {1}.", Oid.toString(oid), targetType), PSQLState.DATA_TYPE_MISMATCH);
            }
        }
    }
    
    @Pure
    private long readLongValue(final byte[] bytes, final int oid, final long minVal, final long maxVal, final String targetType) throws PSQLException {
        long val = 0L;
        switch (oid) {
            case 21: {
                val = ByteConverter.int2(bytes, 0);
                break;
            }
            case 23: {
                val = ByteConverter.int4(bytes, 0);
                break;
            }
            case 20: {
                val = ByteConverter.int8(bytes, 0);
                break;
            }
            case 700: {
                val = (long)ByteConverter.float4(bytes, 0);
                break;
            }
            case 701: {
                val = (long)ByteConverter.float8(bytes, 0);
                break;
            }
            case 1700: {
                final Number num = ByteConverter.numeric(bytes);
                if (num instanceof BigDecimal) {
                    val = ((BigDecimal)num).setScale(0, RoundingMode.DOWN).longValueExact();
                    break;
                }
                val = num.longValue();
                break;
            }
            default: {
                throw new PSQLException(GT.tr("Cannot convert the column of type {0} to requested type {1}.", Oid.toString(oid), targetType), PSQLState.DATA_TYPE_MISMATCH);
            }
        }
        if (val < minVal || val > maxVal) {
            throw new PSQLException(GT.tr("Bad value for type {0} : {1}", targetType, val), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
        }
        return val;
    }
    
    protected void updateValue(final int columnIndex, final Object value) throws SQLException {
        this.checkUpdateable();
        if (!this.onInsertRow && (this.isBeforeFirst() || this.isAfterLast() || Nullness.castNonNull(this.rows, "rows").isEmpty())) {
            throw new PSQLException(GT.tr("Cannot update the ResultSet because it is either before the start or after the end of the results.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        this.checkColumnIndex(columnIndex);
        this.doingUpdates = !this.onInsertRow;
        if (value == null) {
            this.updateNull(columnIndex);
        }
        else {
            final PGResultSetMetaData md = (PGResultSetMetaData)this.getMetaData();
            Nullness.castNonNull(this.updateValues, "updateValues").put(md.getBaseColumnName(columnIndex), value);
        }
    }
    
    @Pure
    protected Object getUUID(final String data) throws SQLException {
        UUID uuid;
        try {
            uuid = UUID.fromString(data);
        }
        catch (final IllegalArgumentException iae) {
            throw new PSQLException(GT.tr("Invalid UUID data.", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE, iae);
        }
        return uuid;
    }
    
    @Pure
    protected Object getUUID(final byte[] data) throws SQLException {
        return new UUID(ByteConverter.int8(data, 0), ByteConverter.int8(data, 8));
    }
    
    void addRows(final List<Tuple> tuples) {
        Nullness.castNonNull(this.rows, "rows").addAll(tuples);
    }
    
    @Override
    public void updateRef(final int columnIndex, final Ref x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateRef(int,Ref)");
    }
    
    @Override
    public void updateRef(final String columnName, final Ref x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateRef(String,Ref)");
    }
    
    @Override
    public void updateBlob(final int columnIndex, final Blob x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateBlob(int,Blob)");
    }
    
    @Override
    public void updateBlob(final String columnName, final Blob x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateBlob(String,Blob)");
    }
    
    @Override
    public void updateClob(final int columnIndex, final Clob x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateClob(int,Clob)");
    }
    
    @Override
    public void updateClob(final String columnName, final Clob x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateClob(String,Clob)");
    }
    
    @Override
    public void updateArray(final int columnIndex, final Array x) throws SQLException {
        this.updateObject(columnIndex, x);
    }
    
    @Override
    public void updateArray(final String columnName, final Array x) throws SQLException {
        this.updateArray(this.findColumn(columnName), x);
    }
    
    @Override
    public <T> T getObject(final int columnIndex, final Class<T> type) throws SQLException {
        if (type == null) {
            throw new SQLException("type is null");
        }
        final int sqlType = this.getSQLType(columnIndex);
        if (type == BigDecimal.class) {
            if (sqlType == 2 || sqlType == 3) {
                return type.cast(this.getBigDecimal(columnIndex));
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        else if (type == String.class) {
            if (sqlType == 1 || sqlType == 12) {
                return type.cast(this.getString(columnIndex));
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        else if (type == Boolean.class) {
            if (sqlType != 16 && sqlType != -7) {
                throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
            }
            final boolean booleanValue = this.getBoolean(columnIndex);
            if (this.wasNull()) {
                return null;
            }
            return type.cast(booleanValue);
        }
        else if (type == Short.class) {
            if (sqlType != 5) {
                throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
            }
            final short shortValue = this.getShort(columnIndex);
            if (this.wasNull()) {
                return null;
            }
            return type.cast(shortValue);
        }
        else if (type == Integer.class) {
            if (sqlType != 4 && sqlType != 5) {
                throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
            }
            final int intValue = this.getInt(columnIndex);
            if (this.wasNull()) {
                return null;
            }
            return type.cast(intValue);
        }
        else if (type == Long.class) {
            if (sqlType != -5) {
                throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
            }
            final long longValue = this.getLong(columnIndex);
            if (this.wasNull()) {
                return null;
            }
            return type.cast(longValue);
        }
        else if (type == BigInteger.class) {
            if (sqlType != -5) {
                throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
            }
            final long longValue = this.getLong(columnIndex);
            if (this.wasNull()) {
                return null;
            }
            return type.cast(BigInteger.valueOf(longValue));
        }
        else if (type == Float.class) {
            if (sqlType != 7) {
                throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
            }
            final float floatValue = this.getFloat(columnIndex);
            if (this.wasNull()) {
                return null;
            }
            return type.cast(floatValue);
        }
        else if (type == Double.class) {
            if (sqlType != 6 && sqlType != 8) {
                throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
            }
            final double doubleValue = this.getDouble(columnIndex);
            if (this.wasNull()) {
                return null;
            }
            return type.cast(doubleValue);
        }
        else if (type == Date.class) {
            if (sqlType == 91) {
                return type.cast(this.getDate(columnIndex));
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        else if (type == Time.class) {
            if (sqlType == 92) {
                return type.cast(this.getTime(columnIndex));
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        else if (type == Timestamp.class) {
            if (sqlType == 93 || sqlType == 2014) {
                return type.cast(this.getTimestamp(columnIndex));
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        else if (type == Calendar.class) {
            if (sqlType != 93 && sqlType != 2014) {
                throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
            }
            final Timestamp timestampValue = this.getTimestamp(columnIndex);
            if (timestampValue == null) {
                return null;
            }
            final Calendar calendar = Calendar.getInstance(this.getDefaultCalendar().getTimeZone());
            calendar.setTimeInMillis(timestampValue.getTime());
            return type.cast(calendar);
        }
        else if (type == Blob.class) {
            if (sqlType == 2004 || sqlType == -2 || sqlType == -5) {
                return type.cast(this.getBlob(columnIndex));
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        else if (type == Clob.class) {
            if (sqlType == 2005 || sqlType == -5) {
                return type.cast(this.getClob(columnIndex));
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        else if (type == java.util.Date.class) {
            if (sqlType != 93) {
                throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
            }
            final Timestamp timestamp = this.getTimestamp(columnIndex);
            if (timestamp == null) {
                return null;
            }
            return type.cast(new java.util.Date(timestamp.getTime()));
        }
        else if (type == Array.class) {
            if (sqlType == 2003) {
                return type.cast(this.getArray(columnIndex));
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        else if (type == SQLXML.class) {
            if (sqlType == 2009) {
                return type.cast(this.getSQLXML(columnIndex));
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        else {
            if (type == UUID.class) {
                return type.cast(this.getObject(columnIndex));
            }
            if (type == InetAddress.class) {
                final String inetText = this.getString(columnIndex);
                if (inetText == null) {
                    return null;
                }
                final int slash = inetText.indexOf("/");
                try {
                    return type.cast(InetAddress.getByName((slash < 0) ? inetText : inetText.substring(0, slash)));
                }
                catch (final UnknownHostException ex) {
                    throw new PSQLException(GT.tr("Invalid Inet data.", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE, ex);
                }
            }
            if (type == LocalDate.class) {
                if (sqlType == 91) {
                    final Date dateValue = this.getDate(columnIndex);
                    if (dateValue == null) {
                        return null;
                    }
                    final long time = dateValue.getTime();
                    if (time == 9223372036825200000L) {
                        return type.cast(LocalDate.MAX);
                    }
                    if (time == -9223372036832400000L) {
                        return type.cast(LocalDate.MIN);
                    }
                    return type.cast(dateValue.toLocalDate());
                }
                else {
                    if (sqlType != 93) {
                        throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
                    }
                    final LocalDateTime localDateTimeValue = this.getLocalDateTime(columnIndex);
                    if (localDateTimeValue == null) {
                        return null;
                    }
                    return type.cast(localDateTimeValue.toLocalDate());
                }
            }
            else if (type == LocalTime.class) {
                if (sqlType == 92) {
                    return type.cast(this.getLocalTime(columnIndex));
                }
                throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
            }
            else if (type == LocalDateTime.class) {
                if (sqlType == 93) {
                    return type.cast(this.getLocalDateTime(columnIndex));
                }
                throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
            }
            else if (type == OffsetDateTime.class) {
                if (sqlType == 2014 || sqlType == 93) {
                    final OffsetDateTime offsetDateTime = this.getOffsetDateTime(columnIndex);
                    return type.cast(offsetDateTime);
                }
                throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
            }
            else {
                if (PGobject.class.isAssignableFrom(type)) {
                    Object object;
                    if (this.isBinary(columnIndex)) {
                        final byte[] byteValue = Nullness.castNonNull(this.thisRow, "thisRow").get(columnIndex - 1);
                        object = this.connection.getObject(this.getPGType(columnIndex), null, byteValue);
                    }
                    else {
                        object = this.connection.getObject(this.getPGType(columnIndex), this.getString(columnIndex), null);
                    }
                    return type.cast(object);
                }
                throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
            }
        }
    }
    
    @Override
    public <T> T getObject(final String columnLabel, final Class<T> type) throws SQLException {
        return this.getObject(this.findColumn(columnLabel), type);
    }
    
    @Override
    public Object getObject(final String s, final Map<String, Class<?>> map) throws SQLException {
        return this.getObjectImpl(s, map);
    }
    
    @Override
    public Object getObject(final int i, final Map<String, Class<?>> map) throws SQLException {
        return this.getObjectImpl(i, map);
    }
    
    @Override
    public void updateObject(final int columnIndex, final Object x, final SQLType targetSqlType, final int scaleOrLength) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateObject");
    }
    
    @Override
    public void updateObject(final String columnLabel, final Object x, final SQLType targetSqlType, final int scaleOrLength) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateObject");
    }
    
    @Override
    public void updateObject(final int columnIndex, final Object x, final SQLType targetSqlType) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateObject");
    }
    
    @Override
    public void updateObject(final String columnLabel, final Object x, final SQLType targetSqlType) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateObject");
    }
    
    @Override
    public RowId getRowId(final int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getRowId columnIndex: {0}", columnIndex);
        throw Driver.notImplemented(this.getClass(), "getRowId(int)");
    }
    
    @Override
    public RowId getRowId(final String columnName) throws SQLException {
        return this.getRowId(this.findColumn(columnName));
    }
    
    @Override
    public void updateRowId(final int columnIndex, final RowId x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateRowId(int, RowId)");
    }
    
    @Override
    public void updateRowId(final String columnName, final RowId x) throws SQLException {
        this.updateRowId(this.findColumn(columnName), x);
    }
    
    @Override
    public int getHoldability() throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getHoldability()");
    }
    
    @Override
    public boolean isClosed() throws SQLException {
        return this.rows == null;
    }
    
    @Override
    public void updateNString(final int columnIndex, final String nString) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateNString(int, String)");
    }
    
    @Override
    public void updateNString(final String columnName, final String nString) throws SQLException {
        this.updateNString(this.findColumn(columnName), nString);
    }
    
    @Override
    public void updateNClob(final int columnIndex, final NClob nClob) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateNClob(int, NClob)");
    }
    
    @Override
    public void updateNClob(final String columnName, final NClob nClob) throws SQLException {
        this.updateNClob(this.findColumn(columnName), nClob);
    }
    
    @Override
    public void updateNClob(final int columnIndex, final Reader reader) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateNClob(int, Reader)");
    }
    
    @Override
    public void updateNClob(final String columnName, final Reader reader) throws SQLException {
        this.updateNClob(this.findColumn(columnName), reader);
    }
    
    @Override
    public void updateNClob(final int columnIndex, final Reader reader, final long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateNClob(int, Reader, long)");
    }
    
    @Override
    public void updateNClob(final String columnName, final Reader reader, final long length) throws SQLException {
        this.updateNClob(this.findColumn(columnName), reader, length);
    }
    
    @Override
    public NClob getNClob(final int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getNClob columnIndex: {0}", columnIndex);
        throw Driver.notImplemented(this.getClass(), "getNClob(int)");
    }
    
    @Override
    public NClob getNClob(final String columnName) throws SQLException {
        return this.getNClob(this.findColumn(columnName));
    }
    
    @Override
    public void updateBlob(final int columnIndex, final InputStream inputStream, final long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateBlob(int, InputStream, long)");
    }
    
    @Override
    public void updateBlob(final String columnName, final InputStream inputStream, final long length) throws SQLException {
        this.updateBlob(this.findColumn(columnName), inputStream, length);
    }
    
    @Override
    public void updateBlob(final int columnIndex, final InputStream inputStream) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateBlob(int, InputStream)");
    }
    
    @Override
    public void updateBlob(final String columnName, final InputStream inputStream) throws SQLException {
        this.updateBlob(this.findColumn(columnName), inputStream);
    }
    
    @Override
    public void updateClob(final int columnIndex, final Reader reader, final long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateClob(int, Reader, long)");
    }
    
    @Override
    public void updateClob(final String columnName, final Reader reader, final long length) throws SQLException {
        this.updateClob(this.findColumn(columnName), reader, length);
    }
    
    @Override
    public void updateClob(final int columnIndex, final Reader reader) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateClob(int, Reader)");
    }
    
    @Override
    public void updateClob(final String columnName, final Reader reader) throws SQLException {
        this.updateClob(this.findColumn(columnName), reader);
    }
    
    @Pure
    @Override
    public SQLXML getSQLXML(final int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getSQLXML columnIndex: {0}", columnIndex);
        final String data = this.getString(columnIndex);
        if (data == null) {
            return null;
        }
        return new PgSQLXML(this.connection, data);
    }
    
    @Override
    public SQLXML getSQLXML(final String columnName) throws SQLException {
        return this.getSQLXML(this.findColumn(columnName));
    }
    
    @Override
    public void updateSQLXML(final int columnIndex, final SQLXML xmlObject) throws SQLException {
        this.updateValue(columnIndex, xmlObject);
    }
    
    @Override
    public void updateSQLXML(final String columnName, final SQLXML xmlObject) throws SQLException {
        this.updateSQLXML(this.findColumn(columnName), xmlObject);
    }
    
    @Override
    public String getNString(final int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getNString columnIndex: {0}", columnIndex);
        throw Driver.notImplemented(this.getClass(), "getNString(int)");
    }
    
    @Override
    public String getNString(final String columnName) throws SQLException {
        return this.getNString(this.findColumn(columnName));
    }
    
    @Override
    public Reader getNCharacterStream(final int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getNCharacterStream columnIndex: {0}", columnIndex);
        throw Driver.notImplemented(this.getClass(), "getNCharacterStream(int)");
    }
    
    @Override
    public Reader getNCharacterStream(final String columnName) throws SQLException {
        return this.getNCharacterStream(this.findColumn(columnName));
    }
    
    public void updateNCharacterStream(final int columnIndex, final Reader x, final int length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateNCharacterStream(int, Reader, int)");
    }
    
    public void updateNCharacterStream(final String columnName, final Reader x, final int length) throws SQLException {
        this.updateNCharacterStream(this.findColumn(columnName), x, length);
    }
    
    @Override
    public void updateNCharacterStream(final int columnIndex, final Reader x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateNCharacterStream(int, Reader)");
    }
    
    @Override
    public void updateNCharacterStream(final String columnName, final Reader x) throws SQLException {
        this.updateNCharacterStream(this.findColumn(columnName), x);
    }
    
    @Override
    public void updateNCharacterStream(final int columnIndex, final Reader x, final long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateNCharacterStream(int, Reader, long)");
    }
    
    @Override
    public void updateNCharacterStream(final String columnName, final Reader x, final long length) throws SQLException {
        this.updateNCharacterStream(this.findColumn(columnName), x, length);
    }
    
    @Override
    public void updateCharacterStream(final int columnIndex, final Reader reader, final long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateCharaceterStream(int, Reader, long)");
    }
    
    @Override
    public void updateCharacterStream(final String columnName, final Reader reader, final long length) throws SQLException {
        this.updateCharacterStream(this.findColumn(columnName), reader, length);
    }
    
    @Override
    public void updateCharacterStream(final int columnIndex, final Reader reader) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateCharaceterStream(int, Reader)");
    }
    
    @Override
    public void updateCharacterStream(final String columnName, final Reader reader) throws SQLException {
        this.updateCharacterStream(this.findColumn(columnName), reader);
    }
    
    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream inputStream, final long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateBinaryStream(int, InputStream, long)");
    }
    
    @Override
    public void updateBinaryStream(final String columnName, final InputStream inputStream, final long length) throws SQLException {
        this.updateBinaryStream(this.findColumn(columnName), inputStream, length);
    }
    
    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream inputStream) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateBinaryStream(int, InputStream)");
    }
    
    @Override
    public void updateBinaryStream(final String columnName, final InputStream inputStream) throws SQLException {
        this.updateBinaryStream(this.findColumn(columnName), inputStream);
    }
    
    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream inputStream, final long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateAsciiStream(int, InputStream, long)");
    }
    
    @Override
    public void updateAsciiStream(final String columnName, final InputStream inputStream, final long length) throws SQLException {
        this.updateAsciiStream(this.findColumn(columnName), inputStream, length);
    }
    
    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream inputStream) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateAsciiStream(int, InputStream)");
    }
    
    @Override
    public void updateAsciiStream(final String columnName, final InputStream inputStream) throws SQLException {
        this.updateAsciiStream(this.findColumn(columnName), inputStream);
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
    
    protected PgResultSet upperCaseFieldLabels() {
        for (final Field field : this.fields) {
            field.upperCaseLabel();
        }
        return this;
    }
    
    static {
        BYTEMAX = new BigInteger(Byte.toString((byte)127));
        BYTEMIN = new BigInteger(Byte.toString((byte)(-128)));
        FAST_NUMBER_FAILED = new NumberFormatException() {
            @Override
            public synchronized Throwable fillInStackTrace() {
                return this;
            }
        };
        SHORTMAX = new BigInteger(Short.toString((short)32767));
        SHORTMIN = new BigInteger(Short.toString((short)(-32768)));
        INTMAX = new BigInteger(Integer.toString(Integer.MAX_VALUE));
        INTMIN = new BigInteger(Integer.toString(Integer.MIN_VALUE));
        LONGMAX = new BigInteger(Long.toString(Long.MAX_VALUE));
        LONGMIN = new BigInteger(Long.toString(Long.MIN_VALUE));
    }
    
    public class CursorResultHandler extends ResultHandlerBase
    {
        @Override
        public void handleResultRows(final Query fromQuery, final Field[] fields, final List<Tuple> tuples, final ResultCursor cursor) {
            PgResultSet.this.rows = tuples;
            PgResultSet.this.cursor = cursor;
        }
        
        @Override
        public void handleCommandStatus(final String status, final long updateCount, final long insertOID) {
            this.handleError(new PSQLException(GT.tr("Unexpected command status: {0}.", status), PSQLState.PROTOCOL_VIOLATION));
        }
        
        @Override
        public void handleCompletion() throws SQLException {
            final SQLWarning warning = this.getWarning();
            if (warning != null) {
                PgResultSet.this.addWarning(warning);
            }
            super.handleCompletion();
        }
    }
    
    private class PrimaryKey
    {
        int index;
        String name;
        
        PrimaryKey(final int index, final String name) {
            this.index = index;
            this.name = name;
        }
        
        Object getValue() throws SQLException {
            return PgResultSet.this.getObject(this.index);
        }
    }
    
    static class NullObject extends PGobject
    {
        NullObject(final String type) {
            this.type = type;
        }
        
        @Override
        public String getValue() {
            return null;
        }
    }
}
