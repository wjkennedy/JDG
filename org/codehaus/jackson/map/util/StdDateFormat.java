// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.util;

import java.text.SimpleDateFormat;
import java.text.FieldPosition;
import org.codehaus.jackson.io.NumberInput;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.TimeZone;
import java.text.DateFormat;

public class StdDateFormat extends DateFormat
{
    protected static final String DATE_FORMAT_STR_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    protected static final String DATE_FORMAT_STR_ISO8601_Z = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    protected static final String DATE_FORMAT_STR_PLAIN = "yyyy-MM-dd";
    protected static final String DATE_FORMAT_STR_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
    protected static final String[] ALL_FORMATS;
    protected static final DateFormat DATE_FORMAT_RFC1123;
    protected static final DateFormat DATE_FORMAT_ISO8601;
    protected static final DateFormat DATE_FORMAT_ISO8601_Z;
    protected static final DateFormat DATE_FORMAT_PLAIN;
    public static final StdDateFormat instance;
    protected transient DateFormat _formatRFC1123;
    protected transient DateFormat _formatISO8601;
    protected transient DateFormat _formatISO8601_z;
    protected transient DateFormat _formatPlain;
    
    @Override
    public StdDateFormat clone() {
        return new StdDateFormat();
    }
    
    public static DateFormat getBlueprintISO8601Format() {
        return StdDateFormat.DATE_FORMAT_ISO8601;
    }
    
    public static DateFormat getISO8601Format(final TimeZone tz) {
        final DateFormat df = (DateFormat)StdDateFormat.DATE_FORMAT_ISO8601.clone();
        df.setTimeZone(tz);
        return df;
    }
    
    public static DateFormat getBlueprintRFC1123Format() {
        return StdDateFormat.DATE_FORMAT_RFC1123;
    }
    
    public static DateFormat getRFC1123Format(final TimeZone tz) {
        final DateFormat df = (DateFormat)StdDateFormat.DATE_FORMAT_RFC1123.clone();
        df.setTimeZone(tz);
        return df;
    }
    
    @Override
    public Date parse(String dateStr) throws ParseException {
        dateStr = dateStr.trim();
        final ParsePosition pos = new ParsePosition(0);
        final Date result = this.parse(dateStr, pos);
        if (result != null) {
            return result;
        }
        final StringBuilder sb = new StringBuilder();
        for (final String f : StdDateFormat.ALL_FORMATS) {
            if (sb.length() > 0) {
                sb.append("\", \"");
            }
            else {
                sb.append('\"');
            }
            sb.append(f);
        }
        sb.append('\"');
        throw new ParseException(String.format("Can not parse date \"%s\": not compatible with any of standard forms (%s)", dateStr, sb.toString()), pos.getErrorIndex());
    }
    
    @Override
    public Date parse(final String dateStr, final ParsePosition pos) {
        if (this.looksLikeISO8601(dateStr)) {
            return this.parseAsISO8601(dateStr, pos);
        }
        int i = dateStr.length();
        while (--i >= 0) {
            final char ch = dateStr.charAt(i);
            if (ch < '0') {
                break;
            }
            if (ch > '9') {
                break;
            }
        }
        if (i < 0 && NumberInput.inLongRange(dateStr, false)) {
            return new Date(Long.parseLong(dateStr));
        }
        return this.parseAsRFC1123(dateStr, pos);
    }
    
    @Override
    public StringBuffer format(final Date date, final StringBuffer toAppendTo, final FieldPosition fieldPosition) {
        if (this._formatISO8601 == null) {
            this._formatISO8601 = (DateFormat)StdDateFormat.DATE_FORMAT_ISO8601.clone();
        }
        return this._formatISO8601.format(date, toAppendTo, fieldPosition);
    }
    
    protected boolean looksLikeISO8601(final String dateStr) {
        return dateStr.length() >= 5 && Character.isDigit(dateStr.charAt(0)) && Character.isDigit(dateStr.charAt(3)) && dateStr.charAt(4) == '-';
    }
    
    protected Date parseAsISO8601(String dateStr, final ParsePosition pos) {
        int len = dateStr.length();
        char c = dateStr.charAt(len - 1);
        DateFormat df;
        if (len <= 10 && Character.isDigit(c)) {
            df = this._formatPlain;
            if (df == null) {
                final DateFormat formatPlain = (DateFormat)StdDateFormat.DATE_FORMAT_PLAIN.clone();
                this._formatPlain = formatPlain;
                df = formatPlain;
            }
        }
        else if (c == 'Z') {
            df = this._formatISO8601_z;
            if (df == null) {
                final DateFormat formatISO8601_z = (DateFormat)StdDateFormat.DATE_FORMAT_ISO8601_Z.clone();
                this._formatISO8601_z = formatISO8601_z;
                df = formatISO8601_z;
            }
            if (dateStr.charAt(len - 4) == ':') {
                final StringBuilder sb = new StringBuilder(dateStr);
                sb.insert(len - 1, ".000");
                dateStr = sb.toString();
            }
        }
        else if (hasTimeZone(dateStr)) {
            c = dateStr.charAt(len - 3);
            if (c == ':') {
                final StringBuilder sb = new StringBuilder(dateStr);
                sb.delete(len - 3, len - 2);
                dateStr = sb.toString();
            }
            else if (c == '+' || c == '-') {
                dateStr += "00";
            }
            len = dateStr.length();
            c = dateStr.charAt(len - 9);
            if (Character.isDigit(c)) {
                final StringBuilder sb = new StringBuilder(dateStr);
                sb.insert(len - 5, ".000");
                dateStr = sb.toString();
            }
            df = this._formatISO8601;
            if (this._formatISO8601 == null) {
                final DateFormat formatISO8601 = (DateFormat)StdDateFormat.DATE_FORMAT_ISO8601.clone();
                this._formatISO8601 = formatISO8601;
                df = formatISO8601;
            }
        }
        else {
            final StringBuilder sb = new StringBuilder(dateStr);
            final int timeLen = len - dateStr.lastIndexOf(84) - 1;
            if (timeLen <= 8) {
                sb.append(".000");
            }
            sb.append('Z');
            dateStr = sb.toString();
            df = this._formatISO8601_z;
            if (df == null) {
                final DateFormat formatISO8601_z2 = (DateFormat)StdDateFormat.DATE_FORMAT_ISO8601_Z.clone();
                this._formatISO8601_z = formatISO8601_z2;
                df = formatISO8601_z2;
            }
        }
        return df.parse(dateStr, pos);
    }
    
    protected Date parseAsRFC1123(final String dateStr, final ParsePosition pos) {
        if (this._formatRFC1123 == null) {
            this._formatRFC1123 = (DateFormat)StdDateFormat.DATE_FORMAT_RFC1123.clone();
        }
        return this._formatRFC1123.parse(dateStr, pos);
    }
    
    private static final boolean hasTimeZone(final String str) {
        final int len = str.length();
        if (len >= 6) {
            char c = str.charAt(len - 6);
            if (c == '+' || c == '-') {
                return true;
            }
            c = str.charAt(len - 5);
            if (c == '+' || c == '-') {
                return true;
            }
            c = str.charAt(len - 3);
            if (c == '+' || c == '-') {
                return true;
            }
        }
        return false;
    }
    
    static {
        ALL_FORMATS = new String[] { "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "EEE, dd MMM yyyy HH:mm:ss zzz", "yyyy-MM-dd" };
        final TimeZone gmt = TimeZone.getTimeZone("GMT");
        (DATE_FORMAT_RFC1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")).setTimeZone(gmt);
        (DATE_FORMAT_ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")).setTimeZone(gmt);
        (DATE_FORMAT_ISO8601_Z = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")).setTimeZone(gmt);
        (DATE_FORMAT_PLAIN = new SimpleDateFormat("yyyy-MM-dd")).setTimeZone(gmt);
        instance = new StdDateFormat();
    }
}
