// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import java.util.Iterator;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.postgresql.core.Encoding;

public class HStoreConverter
{
    public static Map<String, String> fromBytes(final byte[] b, final Encoding encoding) throws SQLException {
        final Map<String, String> m = new HashMap<String, String>();
        int pos = 0;
        final int numElements = ByteConverter.int4(b, pos);
        pos += 4;
        try {
            for (int i = 0; i < numElements; ++i) {
                final int keyLen = ByteConverter.int4(b, pos);
                pos += 4;
                final String key = encoding.decode(b, pos, keyLen);
                pos += keyLen;
                final int valLen = ByteConverter.int4(b, pos);
                pos += 4;
                String val;
                if (valLen == -1) {
                    val = null;
                }
                else {
                    val = encoding.decode(b, pos, valLen);
                    pos += valLen;
                }
                m.put(key, val);
            }
        }
        catch (final IOException ioe) {
            throw new PSQLException(GT.tr("Invalid character data was found.  This is most likely caused by stored data containing characters that are invalid for the character set the database was created in.  The most common example of this is storing 8bit data in a SQL_ASCII database.", new Object[0]), PSQLState.DATA_ERROR, ioe);
        }
        return m;
    }
    
    public static byte[] toBytes(final Map<?, ?> m, final Encoding encoding) throws SQLException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(4 + 10 * m.size());
        final byte[] lenBuf = new byte[4];
        try {
            ByteConverter.int4(lenBuf, 0, m.size());
            baos.write(lenBuf);
            for (final Map.Entry<?, ?> e : m.entrySet()) {
                final Object mapKey = e.getKey();
                if (mapKey == null) {
                    throw new PSQLException(GT.tr("hstore key must not be null", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE);
                }
                final byte[] key = encoding.encode(mapKey.toString());
                ByteConverter.int4(lenBuf, 0, key.length);
                baos.write(lenBuf);
                baos.write(key);
                if (e.getValue() == null) {
                    ByteConverter.int4(lenBuf, 0, -1);
                    baos.write(lenBuf);
                }
                else {
                    final byte[] val = encoding.encode(e.getValue().toString());
                    ByteConverter.int4(lenBuf, 0, val.length);
                    baos.write(lenBuf);
                    baos.write(val);
                }
            }
        }
        catch (final IOException ioe) {
            throw new PSQLException(GT.tr("Invalid character data was found.  This is most likely caused by stored data containing characters that are invalid for the character set the database was created in.  The most common example of this is storing 8bit data in a SQL_ASCII database.", new Object[0]), PSQLState.DATA_ERROR, ioe);
        }
        return baos.toByteArray();
    }
    
    public static String toString(final Map<?, ?> map) {
        if (map.isEmpty()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(map.size() * 8);
        for (final Map.Entry<?, ?> e : map.entrySet()) {
            appendEscaped(sb, e.getKey());
            sb.append("=>");
            appendEscaped(sb, e.getValue());
            sb.append(", ");
        }
        sb.setLength(sb.length() - 2);
        return sb.toString();
    }
    
    private static void appendEscaped(final StringBuilder sb, final Object val) {
        if (val != null) {
            sb.append('\"');
            final String s = val.toString();
            for (int pos = 0; pos < s.length(); ++pos) {
                final char ch = s.charAt(pos);
                if (ch == '\"' || ch == '\\') {
                    sb.append('\\');
                }
                sb.append(ch);
            }
            sb.append('\"');
        }
        else {
            sb.append("NULL");
        }
    }
    
    public static Map<String, String> fromString(final String s) {
        final Map<String, String> m = new HashMap<String, String>();
        int pos = 0;
        final StringBuilder sb = new StringBuilder();
        while (pos < s.length()) {
            sb.setLength(0);
            final int start = s.indexOf(34, pos);
            int end = appendUntilQuote(sb, s, start);
            final String key = sb.toString();
            pos = end + 3;
            String val;
            if (s.charAt(pos) == 'N') {
                val = null;
                pos += 4;
            }
            else {
                sb.setLength(0);
                end = appendUntilQuote(sb, s, pos);
                val = sb.toString();
                pos = end;
            }
            ++pos;
            m.put(key, val);
        }
        return m;
    }
    
    private static int appendUntilQuote(final StringBuilder sb, final String s, int pos) {
        ++pos;
        while (pos < s.length()) {
            char ch = s.charAt(pos);
            if (ch == '\"') {
                break;
            }
            if (ch == '\\') {
                ++pos;
                ch = s.charAt(pos);
            }
            sb.append(ch);
            ++pos;
        }
        return pos;
    }
}
