// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

import org.postgresql.core.JavaVersion;
import org.postgresql.util.internal.Nullness;
import org.postgresql.util.ByteConverter;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.TemporalAmount;
import java.time.chrono.ChronoLocalDate;
import java.sql.Date;
import java.time.ZoneId;
import java.time.Instant;
import java.sql.Time;
import java.time.ZoneOffset;
import java.time.temporal.TemporalField;
import java.time.chrono.IsoEra;
import java.time.temporal.ChronoField;
import java.time.format.DateTimeParseException;
import java.sql.Timestamp;
import java.sql.SQLException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import java.util.SimpleTimeZone;
import java.util.GregorianCalendar;
import org.postgresql.core.Provider;
import java.util.Calendar;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.LocalTime;
import java.time.Duration;
import java.util.TimeZone;
import java.util.HashMap;

public class TimestampUtils
{
    private static final int ONEDAY = 86400000;
    private static final char[] ZEROS;
    private static final char[][] NUMBERS;
    private static final HashMap<String, TimeZone> GMT_ZONES;
    private static final int MAX_NANOS_BEFORE_WRAP_ON_ROUND = 999999500;
    private static final Duration ONE_MICROSECOND;
    private static final LocalTime MAX_TIME;
    private static final OffsetDateTime MAX_OFFSET_DATETIME;
    private static final LocalDateTime MAX_LOCAL_DATETIME;
    private static final LocalDate MIN_LOCAL_DATE;
    private static final LocalDateTime MIN_LOCAL_DATETIME;
    private static final OffsetDateTime MIN_OFFSET_DATETIME;
    private static final Field DEFAULT_TIME_ZONE_FIELD;
    private TimeZone prevDefaultZoneFieldValue;
    private TimeZone defaultTimeZoneCache;
    private final StringBuilder sbuf;
    private final Calendar calendarWithUserTz;
    private final TimeZone utcTz;
    private Calendar calCache;
    private int calCacheZone;
    private final boolean usesDouble;
    private final Provider<TimeZone> timeZoneProvider;
    
    public TimestampUtils(final boolean usesDouble, final Provider<TimeZone> timeZoneProvider) {
        this.sbuf = new StringBuilder();
        this.calendarWithUserTz = new GregorianCalendar();
        this.utcTz = TimeZone.getTimeZone("UTC");
        this.usesDouble = usesDouble;
        this.timeZoneProvider = timeZoneProvider;
    }
    
    private Calendar getCalendar(final int sign, final int hr, final int min, final int sec) {
        final int rawOffset = sign * (((hr * 60 + min) * 60 + sec) * 1000);
        if (this.calCache != null && this.calCacheZone == rawOffset) {
            return this.calCache;
        }
        final StringBuilder zoneID = new StringBuilder("GMT");
        zoneID.append((sign < 0) ? '-' : '+');
        if (hr < 10) {
            zoneID.append('0');
        }
        zoneID.append(hr);
        if (min < 10) {
            zoneID.append('0');
        }
        zoneID.append(min);
        if (sec < 10) {
            zoneID.append('0');
        }
        zoneID.append(sec);
        final TimeZone syntheticTZ = new SimpleTimeZone(rawOffset, zoneID.toString());
        this.calCache = new GregorianCalendar(syntheticTZ);
        this.calCacheZone = rawOffset;
        return this.calCache;
    }
    
    private ParsedTimestamp parseBackendTimestamp(final String str) throws SQLException {
        final char[] s = str.toCharArray();
        final int slen = s.length;
        final ParsedTimestamp result = new ParsedTimestamp();
        try {
            int start = skipWhitespace(s, 0);
            int end = firstNonDigit(s, start);
            if (charAt(s, end) == '-') {
                result.hasDate = true;
                result.year = number(s, start, end);
                start = end + 1;
                end = firstNonDigit(s, start);
                result.month = number(s, start, end);
                final char sep = charAt(s, end);
                if (sep != '-') {
                    throw new NumberFormatException("Expected date to be dash-separated, got '" + sep + "'");
                }
                start = end + 1;
                end = firstNonDigit(s, start);
                result.day = number(s, start, end);
                start = skipWhitespace(s, end);
            }
            if (Character.isDigit(charAt(s, start))) {
                result.hasTime = true;
                end = firstNonDigit(s, start);
                result.hour = number(s, start, end);
                char sep = charAt(s, end);
                if (sep != ':') {
                    throw new NumberFormatException("Expected time to be colon-separated, got '" + sep + "'");
                }
                start = end + 1;
                end = firstNonDigit(s, start);
                result.minute = number(s, start, end);
                sep = charAt(s, end);
                if (sep != ':') {
                    throw new NumberFormatException("Expected time to be colon-separated, got '" + sep + "'");
                }
                start = end + 1;
                end = firstNonDigit(s, start);
                result.second = number(s, start, end);
                start = end;
                if (charAt(s, start) == '.') {
                    end = firstNonDigit(s, start + 1);
                    int num = number(s, start + 1, end);
                    for (int numlength = end - (start + 1); numlength < 9; ++numlength) {
                        num *= 10;
                    }
                    result.nanos = num;
                    start = end;
                }
                start = skipWhitespace(s, start);
            }
            char sep = charAt(s, start);
            if (sep == '-' || sep == '+') {
                final int tzsign = (sep == '-') ? -1 : 1;
                end = firstNonDigit(s, start + 1);
                final int tzhr = number(s, start + 1, end);
                start = end;
                sep = charAt(s, start);
                int tzmin;
                if (sep == ':') {
                    end = firstNonDigit(s, start + 1);
                    tzmin = number(s, start + 1, end);
                    start = end;
                }
                else {
                    tzmin = 0;
                }
                int tzsec = 0;
                sep = charAt(s, start);
                if (sep == ':') {
                    end = firstNonDigit(s, start + 1);
                    tzsec = number(s, start + 1, end);
                    start = end;
                }
                result.tz = this.getCalendar(tzsign, tzhr, tzmin, tzsec);
                start = skipWhitespace(s, start);
            }
            if (result.hasDate && start < slen) {
                final String eraString = new String(s, start, slen - start);
                if (eraString.startsWith("AD")) {
                    result.era = 1;
                    start += 2;
                }
                else if (eraString.startsWith("BC")) {
                    result.era = 0;
                    start += 2;
                }
            }
            if (start < slen) {
                throw new NumberFormatException("Trailing junk on timestamp: '" + new String(s, start, slen - start) + "'");
            }
            if (!result.hasTime && !result.hasDate) {
                throw new NumberFormatException("Timestamp has neither date nor time");
            }
        }
        catch (final NumberFormatException nfe) {
            throw new PSQLException(GT.tr("Bad value for type timestamp/date/time: {1}", str), PSQLState.BAD_DATETIME_FORMAT, nfe);
        }
        return result;
    }
    
    public synchronized Timestamp toTimestamp(final Calendar cal, final String s) throws SQLException {
        if (s == null) {
            return null;
        }
        final int slen = s.length();
        if (slen == 8 && s.equals("infinity")) {
            return new Timestamp(9223372036825200000L);
        }
        if (slen == 9 && s.equals("-infinity")) {
            return new Timestamp(-9223372036832400000L);
        }
        final ParsedTimestamp ts = this.parseBackendTimestamp(s);
        final Calendar useCal = (ts.tz != null) ? ts.tz : this.setupCalendar(cal);
        useCal.set(0, ts.era);
        useCal.set(1, ts.year);
        useCal.set(2, ts.month - 1);
        useCal.set(5, ts.day);
        useCal.set(11, ts.hour);
        useCal.set(12, ts.minute);
        useCal.set(13, ts.second);
        useCal.set(14, 0);
        final Timestamp result = new Timestamp(useCal.getTimeInMillis());
        result.setNanos(ts.nanos);
        return result;
    }
    
    public LocalTime toLocalTime(final String s) throws SQLException {
        if (s == null) {
            return null;
        }
        if (s.equals("24:00:00")) {
            return LocalTime.MAX;
        }
        try {
            return LocalTime.parse(s);
        }
        catch (final DateTimeParseException nfe) {
            throw new PSQLException(GT.tr("Bad value for type timestamp/date/time: {1}", s), PSQLState.BAD_DATETIME_FORMAT, nfe);
        }
    }
    
    public LocalDateTime toLocalDateTime(final String s) throws SQLException {
        if (s == null) {
            return null;
        }
        final int slen = s.length();
        if (slen == 8 && s.equals("infinity")) {
            return LocalDateTime.MAX;
        }
        if (slen == 9 && s.equals("-infinity")) {
            return LocalDateTime.MIN;
        }
        final ParsedTimestamp ts = this.parseBackendTimestamp(s);
        final LocalDateTime result = LocalDateTime.of(ts.year, ts.month, ts.day, ts.hour, ts.minute, ts.second, ts.nanos);
        if (ts.era == 0) {
            return result.with((TemporalField)ChronoField.ERA, (long)IsoEra.BCE.getValue());
        }
        return result;
    }
    
    public OffsetDateTime toOffsetDateTime(final String s) throws SQLException {
        if (s == null) {
            return null;
        }
        final int slen = s.length();
        if (slen == 8 && s.equals("infinity")) {
            return OffsetDateTime.MAX;
        }
        if (slen == 9 && s.equals("-infinity")) {
            return OffsetDateTime.MIN;
        }
        final ParsedTimestamp ts = this.parseBackendTimestamp(s);
        final Calendar tz = ts.tz;
        int offsetSeconds;
        if (tz == null) {
            offsetSeconds = 0;
        }
        else {
            offsetSeconds = tz.get(15) / 1000;
        }
        final ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(offsetSeconds);
        final OffsetDateTime result = OffsetDateTime.of(ts.year, ts.month, ts.day, ts.hour, ts.minute, ts.second, ts.nanos, zoneOffset).withOffsetSameInstant(ZoneOffset.UTC);
        if (ts.era == 0) {
            return result.with((TemporalField)ChronoField.ERA, (long)IsoEra.BCE.getValue());
        }
        return result;
    }
    
    public OffsetDateTime toOffsetDateTime(final Time t) {
        return t.toLocalTime().atDate(LocalDate.of(1970, 1, 1)).atOffset(ZoneOffset.UTC);
    }
    
    public OffsetDateTime toOffsetDateTimeBin(final byte[] bytes) throws PSQLException {
        final ParsedBinaryTimestamp parsedTimestamp = this.toProlepticParsedTimestampBin(bytes);
        if (parsedTimestamp.infinity == Infinity.POSITIVE) {
            return OffsetDateTime.MAX;
        }
        if (parsedTimestamp.infinity == Infinity.NEGATIVE) {
            return OffsetDateTime.MIN;
        }
        final Instant instant = Instant.ofEpochSecond(parsedTimestamp.millis / 1000L, parsedTimestamp.nanos);
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
    
    public synchronized Time toTime(final Calendar cal, final String s) throws SQLException {
        if (s == null) {
            return null;
        }
        final ParsedTimestamp ts = this.parseBackendTimestamp(s);
        final Calendar useCal = (ts.tz != null) ? ts.tz : this.setupCalendar(cal);
        if (ts.tz == null) {
            useCal.set(0, ts.era);
            useCal.set(1, ts.year);
            useCal.set(2, ts.month - 1);
            useCal.set(5, ts.day);
        }
        else {
            useCal.set(0, 1);
            useCal.set(1, 1970);
            useCal.set(2, 0);
            useCal.set(5, 1);
        }
        useCal.set(11, ts.hour);
        useCal.set(12, ts.minute);
        useCal.set(13, ts.second);
        useCal.set(14, 0);
        final long timeMillis = useCal.getTimeInMillis() + ts.nanos / 1000000;
        if (ts.tz != null || (ts.year == 1970 && ts.era == 1)) {
            return new Time(timeMillis);
        }
        return this.convertToTime(timeMillis, useCal.getTimeZone());
    }
    
    public synchronized Date toDate(final Calendar cal, final String s) throws SQLException {
        final Timestamp timestamp = this.toTimestamp(cal, s);
        if (timestamp == null) {
            return null;
        }
        return this.convertToDate(timestamp.getTime(), (cal == null) ? null : cal.getTimeZone());
    }
    
    private Calendar setupCalendar(final Calendar cal) {
        final TimeZone timeZone = (cal == null) ? null : cal.getTimeZone();
        return this.getSharedCalendar(timeZone);
    }
    
    public Calendar getSharedCalendar(TimeZone timeZone) {
        if (timeZone == null) {
            timeZone = this.getDefaultTz();
        }
        final Calendar tmp = this.calendarWithUserTz;
        tmp.setTimeZone(timeZone);
        return tmp;
    }
    
    private static boolean nanosExceed499(final int nanos) {
        return nanos % 1000 > 499;
    }
    
    public synchronized String toString(final Calendar cal, final Timestamp x) {
        return this.toString(cal, x, true);
    }
    
    public synchronized String toString(Calendar cal, final Timestamp x, final boolean withTimeZone) {
        if (x.getTime() == 9223372036825200000L) {
            return "infinity";
        }
        if (x.getTime() == -9223372036832400000L) {
            return "-infinity";
        }
        cal = this.setupCalendar(cal);
        long timeMillis = x.getTime();
        int nanos = x.getNanos();
        if (nanos >= 999999500) {
            nanos = 0;
            ++timeMillis;
        }
        else if (nanosExceed499(nanos)) {
            nanos += 1000 - nanos % 1000;
        }
        cal.setTimeInMillis(timeMillis);
        this.sbuf.setLength(0);
        appendDate(this.sbuf, cal);
        this.sbuf.append(' ');
        appendTime(this.sbuf, cal, nanos);
        if (withTimeZone) {
            this.appendTimeZone(this.sbuf, cal);
        }
        appendEra(this.sbuf, cal);
        return this.sbuf.toString();
    }
    
    public synchronized String toString(final Calendar cal, final Date x) {
        return this.toString(cal, x, true);
    }
    
    public synchronized String toString(Calendar cal, final Date x, final boolean withTimeZone) {
        if (x.getTime() == 9223372036825200000L) {
            return "infinity";
        }
        if (x.getTime() == -9223372036832400000L) {
            return "-infinity";
        }
        cal = this.setupCalendar(cal);
        cal.setTime(x);
        this.sbuf.setLength(0);
        appendDate(this.sbuf, cal);
        appendEra(this.sbuf, cal);
        if (withTimeZone) {
            this.sbuf.append(' ');
            this.appendTimeZone(this.sbuf, cal);
        }
        return this.sbuf.toString();
    }
    
    public synchronized String toString(final Calendar cal, final Time x) {
        return this.toString(cal, x, true);
    }
    
    public synchronized String toString(Calendar cal, final Time x, final boolean withTimeZone) {
        cal = this.setupCalendar(cal);
        cal.setTime(x);
        this.sbuf.setLength(0);
        appendTime(this.sbuf, cal, cal.get(14) * 1000000);
        if (withTimeZone) {
            this.appendTimeZone(this.sbuf, cal);
        }
        return this.sbuf.toString();
    }
    
    private static void appendDate(final StringBuilder sb, final Calendar cal) {
        final int year = cal.get(1);
        final int month = cal.get(2) + 1;
        final int day = cal.get(5);
        appendDate(sb, year, month, day);
    }
    
    private static void appendDate(final StringBuilder sb, final int year, final int month, final int day) {
        final int prevLength = sb.length();
        sb.append(year);
        final int leadingZerosForYear = 4 - (sb.length() - prevLength);
        if (leadingZerosForYear > 0) {
            sb.insert(prevLength, TimestampUtils.ZEROS, 0, leadingZerosForYear);
        }
        sb.append('-');
        sb.append(TimestampUtils.NUMBERS[month]);
        sb.append('-');
        sb.append(TimestampUtils.NUMBERS[day]);
    }
    
    private static void appendTime(final StringBuilder sb, final Calendar cal, final int nanos) {
        final int hours = cal.get(11);
        final int minutes = cal.get(12);
        final int seconds = cal.get(13);
        appendTime(sb, hours, minutes, seconds, nanos);
    }
    
    private static void appendTime(final StringBuilder sb, final int hours, final int minutes, final int seconds, final int nanos) {
        sb.append(TimestampUtils.NUMBERS[hours]);
        sb.append(':');
        sb.append(TimestampUtils.NUMBERS[minutes]);
        sb.append(':');
        sb.append(TimestampUtils.NUMBERS[seconds]);
        if (nanos < 1000) {
            return;
        }
        sb.append('.');
        final int len = sb.length();
        sb.append(nanos / 1000);
        final int needZeros = 6 - (sb.length() - len);
        if (needZeros > 0) {
            sb.insert(len, TimestampUtils.ZEROS, 0, needZeros);
        }
        for (int end = sb.length() - 1; sb.charAt(end) == '0'; --end) {
            sb.deleteCharAt(end);
        }
    }
    
    private void appendTimeZone(final StringBuilder sb, final Calendar cal) {
        final int offset = (cal.get(15) + cal.get(16)) / 1000;
        this.appendTimeZone(sb, offset);
    }
    
    private void appendTimeZone(final StringBuilder sb, final int offset) {
        final int absoff = Math.abs(offset);
        final int hours = absoff / 60 / 60;
        final int mins = (absoff - hours * 60 * 60) / 60;
        final int secs = absoff - hours * 60 * 60 - mins * 60;
        sb.append((offset >= 0) ? "+" : "-");
        sb.append(TimestampUtils.NUMBERS[hours]);
        if (mins == 0 && secs == 0) {
            return;
        }
        sb.append(':');
        sb.append(TimestampUtils.NUMBERS[mins]);
        if (secs != 0) {
            sb.append(':');
            sb.append(TimestampUtils.NUMBERS[secs]);
        }
    }
    
    private static void appendEra(final StringBuilder sb, final Calendar cal) {
        if (cal.get(0) == 0) {
            sb.append(" BC");
        }
    }
    
    public synchronized String toString(final LocalDate localDate) {
        if (LocalDate.MAX.equals(localDate)) {
            return "infinity";
        }
        if (localDate.isBefore(TimestampUtils.MIN_LOCAL_DATE)) {
            return "-infinity";
        }
        this.sbuf.setLength(0);
        appendDate(this.sbuf, localDate);
        appendEra(this.sbuf, localDate);
        return this.sbuf.toString();
    }
    
    public synchronized String toString(LocalTime localTime) {
        this.sbuf.setLength(0);
        if (localTime.isAfter(TimestampUtils.MAX_TIME)) {
            return "24:00:00";
        }
        final int nano = localTime.getNano();
        if (nanosExceed499(nano)) {
            localTime = localTime.plus((TemporalAmount)TimestampUtils.ONE_MICROSECOND);
        }
        appendTime(this.sbuf, localTime);
        return this.sbuf.toString();
    }
    
    public synchronized String toString(OffsetDateTime offsetDateTime) {
        if (offsetDateTime.isAfter(TimestampUtils.MAX_OFFSET_DATETIME)) {
            return "infinity";
        }
        if (offsetDateTime.isBefore(TimestampUtils.MIN_OFFSET_DATETIME)) {
            return "-infinity";
        }
        this.sbuf.setLength(0);
        final int nano = offsetDateTime.getNano();
        if (nanosExceed499(nano)) {
            offsetDateTime = offsetDateTime.plus((TemporalAmount)TimestampUtils.ONE_MICROSECOND);
        }
        final LocalDateTime localDateTime = offsetDateTime.toLocalDateTime();
        final LocalDate localDate = localDateTime.toLocalDate();
        appendDate(this.sbuf, localDate);
        this.sbuf.append(' ');
        appendTime(this.sbuf, localDateTime.toLocalTime());
        this.appendTimeZone(this.sbuf, offsetDateTime.getOffset());
        appendEra(this.sbuf, localDate);
        return this.sbuf.toString();
    }
    
    public synchronized String toString(final LocalDateTime localDateTime) {
        if (localDateTime.isAfter(TimestampUtils.MAX_LOCAL_DATETIME)) {
            return "infinity";
        }
        if (localDateTime.isBefore(TimestampUtils.MIN_LOCAL_DATETIME)) {
            return "-infinity";
        }
        final ZonedDateTime zonedDateTime = localDateTime.atZone(this.getDefaultTz().toZoneId());
        return this.toString(zonedDateTime.toOffsetDateTime());
    }
    
    private static void appendDate(final StringBuilder sb, final LocalDate localDate) {
        final int year = localDate.get(ChronoField.YEAR_OF_ERA);
        final int month = localDate.getMonthValue();
        final int day = localDate.getDayOfMonth();
        appendDate(sb, year, month, day);
    }
    
    private static void appendTime(final StringBuilder sb, final LocalTime localTime) {
        final int hours = localTime.getHour();
        final int minutes = localTime.getMinute();
        final int seconds = localTime.getSecond();
        final int nanos = localTime.getNano();
        appendTime(sb, hours, minutes, seconds, nanos);
    }
    
    private void appendTimeZone(final StringBuilder sb, final ZoneOffset offset) {
        final int offsetSeconds = offset.getTotalSeconds();
        this.appendTimeZone(sb, offsetSeconds);
    }
    
    private static void appendEra(final StringBuilder sb, final LocalDate localDate) {
        if (localDate.get(ChronoField.ERA) == IsoEra.BCE.getValue()) {
            sb.append(" BC");
        }
    }
    
    private static int skipWhitespace(final char[] s, final int start) {
        final int slen = s.length;
        for (int i = start; i < slen; ++i) {
            if (!Character.isSpace(s[i])) {
                return i;
            }
        }
        return slen;
    }
    
    private static int firstNonDigit(final char[] s, final int start) {
        final int slen = s.length;
        for (int i = start; i < slen; ++i) {
            if (!Character.isDigit(s[i])) {
                return i;
            }
        }
        return slen;
    }
    
    private static int number(final char[] s, final int start, final int end) {
        if (start >= end) {
            throw new NumberFormatException();
        }
        int n = 0;
        for (int i = start; i < end; ++i) {
            n = 10 * n + (s[i] - '0');
        }
        return n;
    }
    
    private static char charAt(final char[] s, final int pos) {
        if (pos >= 0 && pos < s.length) {
            return s[pos];
        }
        return '\0';
    }
    
    public Date toDateBin(TimeZone tz, final byte[] bytes) throws PSQLException {
        if (bytes.length != 4) {
            throw new PSQLException(GT.tr("Unsupported binary encoding of {0}.", "date"), PSQLState.BAD_DATETIME_FORMAT);
        }
        final int days = ByteConverter.int4(bytes, 0);
        if (tz == null) {
            tz = this.getDefaultTz();
        }
        final long secs = toJavaSecs(days * 86400L);
        long millis = secs * 1000L;
        if (millis <= -185543533774800000L) {
            millis = -9223372036832400000L;
        }
        else if (millis >= 185543533774800000L) {
            millis = 9223372036825200000L;
        }
        else {
            millis = this.guessTimestamp(millis, tz);
        }
        return new Date(millis);
    }
    
    private TimeZone getDefaultTz() {
        if (TimestampUtils.DEFAULT_TIME_ZONE_FIELD != null) {
            try {
                final TimeZone defaultTimeZone = (TimeZone)TimestampUtils.DEFAULT_TIME_ZONE_FIELD.get(null);
                if (defaultTimeZone == this.prevDefaultZoneFieldValue) {
                    return Nullness.castNonNull(this.defaultTimeZoneCache);
                }
                this.prevDefaultZoneFieldValue = defaultTimeZone;
            }
            catch (final Exception ex) {}
        }
        final TimeZone tz = TimeZone.getDefault();
        return this.defaultTimeZoneCache = tz;
    }
    
    public boolean hasFastDefaultTimeZone() {
        return TimestampUtils.DEFAULT_TIME_ZONE_FIELD != null;
    }
    
    public Time toTimeBin(TimeZone tz, final byte[] bytes) throws PSQLException {
        if (bytes.length != 8 && bytes.length != 12) {
            throw new PSQLException(GT.tr("Unsupported binary encoding of {0}.", "time"), PSQLState.BAD_DATETIME_FORMAT);
        }
        long millis;
        if (this.usesDouble) {
            final double time = ByteConverter.float8(bytes, 0);
            millis = (long)(time * 1000.0);
        }
        else {
            final long time2 = ByteConverter.int8(bytes, 0);
            millis = time2 / 1000L;
        }
        if (bytes.length == 12) {
            int timeOffset = ByteConverter.int4(bytes, 8);
            timeOffset *= -1000;
            millis -= timeOffset;
            return new Time(millis);
        }
        if (tz == null) {
            tz = this.getDefaultTz();
        }
        millis = this.guessTimestamp(millis, tz);
        return this.convertToTime(millis, tz);
    }
    
    public LocalTime toLocalTimeBin(final byte[] bytes) throws PSQLException {
        if (bytes.length != 8) {
            throw new PSQLException(GT.tr("Unsupported binary encoding of {0}.", "time"), PSQLState.BAD_DATETIME_FORMAT);
        }
        long micros;
        if (this.usesDouble) {
            final double seconds = ByteConverter.float8(bytes, 0);
            micros = (long)(seconds * 1000000.0);
        }
        else {
            micros = ByteConverter.int8(bytes, 0);
        }
        return LocalTime.ofNanoOfDay(micros * 1000L);
    }
    
    public Timestamp toTimestampBin(final TimeZone tz, final byte[] bytes, final boolean timestamptz) throws PSQLException {
        final ParsedBinaryTimestamp parsedTimestamp = this.toParsedTimestampBin(tz, bytes, timestamptz);
        if (parsedTimestamp.infinity == Infinity.POSITIVE) {
            return new Timestamp(9223372036825200000L);
        }
        if (parsedTimestamp.infinity == Infinity.NEGATIVE) {
            return new Timestamp(-9223372036832400000L);
        }
        final Timestamp ts = new Timestamp(parsedTimestamp.millis);
        ts.setNanos(parsedTimestamp.nanos);
        return ts;
    }
    
    private ParsedBinaryTimestamp toParsedTimestampBinPlain(final byte[] bytes) throws PSQLException {
        if (bytes.length != 8) {
            throw new PSQLException(GT.tr("Unsupported binary encoding of {0}.", "timestamp"), PSQLState.BAD_DATETIME_FORMAT);
        }
        long secs;
        int nanos;
        if (this.usesDouble) {
            final double time = ByteConverter.float8(bytes, 0);
            if (time == Double.POSITIVE_INFINITY) {
                final ParsedBinaryTimestamp ts = new ParsedBinaryTimestamp();
                ts.infinity = Infinity.POSITIVE;
                return ts;
            }
            if (time == Double.NEGATIVE_INFINITY) {
                final ParsedBinaryTimestamp ts = new ParsedBinaryTimestamp();
                ts.infinity = Infinity.NEGATIVE;
                return ts;
            }
            secs = (long)time;
            nanos = (int)((time - secs) * 1000000.0);
        }
        else {
            final long time2 = ByteConverter.int8(bytes, 0);
            if (time2 == Long.MAX_VALUE) {
                final ParsedBinaryTimestamp ts = new ParsedBinaryTimestamp();
                ts.infinity = Infinity.POSITIVE;
                return ts;
            }
            if (time2 == Long.MIN_VALUE) {
                final ParsedBinaryTimestamp ts = new ParsedBinaryTimestamp();
                ts.infinity = Infinity.NEGATIVE;
                return ts;
            }
            secs = time2 / 1000000L;
            nanos = (int)(time2 - secs * 1000000L);
        }
        if (nanos < 0) {
            --secs;
            nanos += 1000000;
        }
        nanos *= 1000;
        final long millis = secs * 1000L;
        final ParsedBinaryTimestamp ts = new ParsedBinaryTimestamp();
        ts.millis = millis;
        ts.nanos = nanos;
        return ts;
    }
    
    private ParsedBinaryTimestamp toParsedTimestampBin(final TimeZone tz, final byte[] bytes, final boolean timestamptz) throws PSQLException {
        final ParsedBinaryTimestamp ts = this.toParsedTimestampBinPlain(bytes);
        if (ts.infinity != null) {
            return ts;
        }
        long secs = ts.millis / 1000L;
        secs = toJavaSecs(secs);
        long millis = secs * 1000L;
        if (!timestamptz) {
            millis = this.guessTimestamp(millis, tz);
        }
        ts.millis = millis;
        return ts;
    }
    
    private ParsedBinaryTimestamp toProlepticParsedTimestampBin(final byte[] bytes) throws PSQLException {
        final ParsedBinaryTimestamp ts = this.toParsedTimestampBinPlain(bytes);
        if (ts.infinity != null) {
            return ts;
        }
        long secs = ts.millis / 1000L;
        secs += 946684800L;
        final long millis = secs * 1000L;
        ts.millis = millis;
        return ts;
    }
    
    public LocalDateTime toLocalDateTimeBin(final byte[] bytes) throws PSQLException {
        final ParsedBinaryTimestamp parsedTimestamp = this.toProlepticParsedTimestampBin(bytes);
        if (parsedTimestamp.infinity == Infinity.POSITIVE) {
            return LocalDateTime.MAX;
        }
        if (parsedTimestamp.infinity == Infinity.NEGATIVE) {
            return LocalDateTime.MIN;
        }
        return LocalDateTime.ofEpochSecond(parsedTimestamp.millis / 1000L, parsedTimestamp.nanos, ZoneOffset.UTC);
    }
    
    private long guessTimestamp(final long millis, TimeZone tz) {
        if (tz == null) {
            tz = this.getDefaultTz();
        }
        if (isSimpleTimeZone(tz.getID())) {
            return millis - tz.getRawOffset();
        }
        final Calendar cal = this.calendarWithUserTz;
        cal.setTimeZone(this.utcTz);
        cal.setTimeInMillis(millis);
        final int era = cal.get(0);
        final int year = cal.get(1);
        final int month = cal.get(2);
        final int day = cal.get(5);
        final int hour = cal.get(11);
        final int min = cal.get(12);
        final int sec = cal.get(13);
        final int ms = cal.get(14);
        cal.setTimeZone(tz);
        cal.set(0, era);
        cal.set(1, year);
        cal.set(2, month);
        cal.set(5, day);
        cal.set(11, hour);
        cal.set(12, min);
        cal.set(13, sec);
        cal.set(14, ms);
        return cal.getTimeInMillis();
    }
    
    private static boolean isSimpleTimeZone(final String id) {
        return id.startsWith("GMT") || id.startsWith("UTC");
    }
    
    public Date convertToDate(long millis, TimeZone tz) {
        if (millis <= -9223372036832400000L || millis >= 9223372036825200000L) {
            return new Date(millis);
        }
        if (tz == null) {
            tz = this.getDefaultTz();
        }
        if (isSimpleTimeZone(tz.getID())) {
            final int offset = tz.getRawOffset();
            millis += offset;
            millis = floorDiv(millis, 86400000L) * 86400000L;
            millis -= offset;
            return new Date(millis);
        }
        final Calendar cal = this.calendarWithUserTz;
        cal.setTimeZone(tz);
        cal.setTimeInMillis(millis);
        cal.set(11, 0);
        cal.set(12, 0);
        cal.set(13, 0);
        cal.set(14, 0);
        return new Date(cal.getTimeInMillis());
    }
    
    public Time convertToTime(long millis, TimeZone tz) {
        if (tz == null) {
            tz = this.getDefaultTz();
        }
        if (isSimpleTimeZone(tz.getID())) {
            final int offset = tz.getRawOffset();
            millis += offset;
            millis = floorMod(millis, 86400000L);
            millis -= offset;
            return new Time(millis);
        }
        final Calendar cal = this.calendarWithUserTz;
        cal.setTimeZone(tz);
        cal.setTimeInMillis(millis);
        cal.set(0, 1);
        cal.set(1, 1970);
        cal.set(2, 0);
        cal.set(5, 1);
        return new Time(cal.getTimeInMillis());
    }
    
    public String timeToString(final java.util.Date time, final boolean withTimeZone) {
        Calendar cal = null;
        if (withTimeZone) {
            cal = this.calendarWithUserTz;
            cal.setTimeZone(this.timeZoneProvider.get());
        }
        if (time instanceof Timestamp) {
            return this.toString(cal, (Timestamp)time, withTimeZone);
        }
        if (time instanceof Time) {
            return this.toString(cal, (Time)time, withTimeZone);
        }
        return this.toString(cal, (Date)time, withTimeZone);
    }
    
    private static long toJavaSecs(long secs) {
        secs += 946684800L;
        if (secs < -12219292800L) {
            secs += 864000L;
            if (secs < -14825808000L) {
                int extraLeaps = (int)((secs + 14825808000L) / 3155760000L);
                extraLeaps = --extraLeaps - extraLeaps / 4;
                secs += extraLeaps * 86400L;
            }
        }
        return secs;
    }
    
    private static long toPgSecs(long secs) {
        secs -= 946684800L;
        if (secs < -13165977600L) {
            secs -= 864000L;
            if (secs < -15773356800L) {
                int years = (int)((secs + 15773356800L) / -3155823050L);
                years = ++years - years / 4;
                secs += years * 86400L;
            }
        }
        return secs;
    }
    
    public void toBinDate(TimeZone tz, final byte[] bytes, final Date value) throws PSQLException {
        long millis = value.getTime();
        if (tz == null) {
            tz = this.getDefaultTz();
        }
        millis += tz.getOffset(millis);
        final long secs = toPgSecs(millis / 1000L);
        ByteConverter.int4(bytes, 0, (int)(secs / 86400L));
    }
    
    public static TimeZone parseBackendTimeZone(final String timeZone) {
        if (timeZone.startsWith("GMT")) {
            final TimeZone tz = TimestampUtils.GMT_ZONES.get(timeZone);
            if (tz != null) {
                return tz;
            }
        }
        return TimeZone.getTimeZone(timeZone);
    }
    
    private static long floorDiv(final long x, final long y) {
        long r = x / y;
        if ((x ^ y) < 0L && r * y != x) {
            --r;
        }
        return r;
    }
    
    private static long floorMod(final long x, final long y) {
        return x - floorDiv(x, y) * y;
    }
    
    static {
        ZEROS = new char[] { '0', '0', '0', '0', '0', '0', '0', '0', '0' };
        GMT_ZONES = new HashMap<String, TimeZone>();
        ONE_MICROSECOND = Duration.ofNanos(1000L);
        MAX_TIME = LocalTime.MAX.minus((TemporalAmount)Duration.ofNanos(500L));
        MAX_OFFSET_DATETIME = OffsetDateTime.MAX.minus((TemporalAmount)Duration.ofMillis(500L));
        MAX_LOCAL_DATETIME = LocalDateTime.MAX.minus((TemporalAmount)Duration.ofMillis(500L));
        MIN_LOCAL_DATE = LocalDate.of(4713, 1, 1).with((TemporalField)ChronoField.ERA, (long)IsoEra.BCE.getValue());
        MIN_LOCAL_DATETIME = TimestampUtils.MIN_LOCAL_DATE.atStartOfDay();
        MIN_OFFSET_DATETIME = TimestampUtils.MIN_LOCAL_DATETIME.atOffset(ZoneOffset.UTC);
        NUMBERS = new char[64][];
        for (int i = 0; i < TimestampUtils.NUMBERS.length; ++i) {
            TimestampUtils.NUMBERS[i] = (((i < 10) ? "0" : "") + Integer.toString(i)).toCharArray();
        }
        for (int i = -12; i <= 14; ++i) {
            TimeZone timeZone;
            String pgZoneName;
            if (i == 0) {
                timeZone = TimeZone.getTimeZone("GMT");
                pgZoneName = "GMT";
            }
            else {
                timeZone = TimeZone.getTimeZone("GMT" + ((i <= 0) ? "+" : "-") + Math.abs(i));
                pgZoneName = "GMT" + ((i >= 0) ? "+" : "-");
            }
            if (i == 0) {
                TimestampUtils.GMT_ZONES.put(pgZoneName, timeZone);
            }
            else {
                TimestampUtils.GMT_ZONES.put(pgZoneName + Math.abs(i), timeZone);
                TimestampUtils.GMT_ZONES.put(pgZoneName + new String(TimestampUtils.NUMBERS[Math.abs(i)]), timeZone);
            }
        }
        Field tzField;
        try {
            tzField = null;
            if (JavaVersion.getRuntimeVersion().compareTo(JavaVersion.v1_8) <= 0) {
                tzField = TimeZone.class.getDeclaredField("defaultTimeZone");
                tzField.setAccessible(true);
                final TimeZone defaultTz = TimeZone.getDefault();
                final Object tzFromField = tzField.get(null);
                if (defaultTz == null || !defaultTz.equals(tzFromField)) {
                    tzField = null;
                }
            }
        }
        catch (final Exception e) {
            tzField = null;
        }
        DEFAULT_TIME_ZONE_FIELD = tzField;
    }
    
    private static class ParsedTimestamp
    {
        boolean hasDate;
        int era;
        int year;
        int month;
        boolean hasTime;
        int day;
        int hour;
        int minute;
        int second;
        int nanos;
        Calendar tz;
        
        private ParsedTimestamp() {
            this.hasDate = false;
            this.era = 1;
            this.year = 1970;
            this.month = 1;
            this.hasTime = false;
            this.day = 1;
            this.hour = 0;
            this.minute = 0;
            this.second = 0;
            this.nanos = 0;
            this.tz = null;
        }
    }
    
    private static class ParsedBinaryTimestamp
    {
        Infinity infinity;
        long millis;
        int nanos;
        
        private ParsedBinaryTimestamp() {
            this.infinity = null;
            this.millis = 0L;
            this.nanos = 0;
        }
    }
    
    enum Infinity
    {
        POSITIVE, 
        NEGATIVE;
    }
}
