// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import java.io.IOException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import java.sql.SQLException;
import java.nio.charset.Charset;

public class Utils
{
    private static final Charset utf8Charset;
    
    public static String toHexString(final byte[] data) {
        final StringBuilder sb = new StringBuilder(data.length * 2);
        for (final byte element : data) {
            sb.append(Integer.toHexString(element >> 4 & 0xF));
            sb.append(Integer.toHexString(element & 0xF));
        }
        return sb.toString();
    }
    
    public static byte[] encodeUTF8(final String str) {
        return str.getBytes(Utils.utf8Charset);
    }
    
    public static StringBuilder escapeLiteral(StringBuilder sbuf, final String value, final boolean standardConformingStrings) throws SQLException {
        if (sbuf == null) {
            sbuf = new StringBuilder((value.length() + 10) / 10 * 11);
        }
        doAppendEscapedLiteral(sbuf, value, standardConformingStrings);
        return sbuf;
    }
    
    private static void doAppendEscapedLiteral(final Appendable sbuf, final String value, final boolean standardConformingStrings) throws SQLException {
        try {
            if (standardConformingStrings) {
                for (int i = 0; i < value.length(); ++i) {
                    final char ch = value.charAt(i);
                    if (ch == '\0') {
                        throw new PSQLException(GT.tr("Zero bytes may not occur in string parameters.", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE);
                    }
                    if (ch == '\'') {
                        sbuf.append('\'');
                    }
                    sbuf.append(ch);
                }
            }
            else {
                for (int i = 0; i < value.length(); ++i) {
                    final char ch = value.charAt(i);
                    if (ch == '\0') {
                        throw new PSQLException(GT.tr("Zero bytes may not occur in string parameters.", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE);
                    }
                    if (ch == '\\' || ch == '\'') {
                        sbuf.append(ch);
                    }
                    sbuf.append(ch);
                }
            }
        }
        catch (final IOException e) {
            throw new PSQLException(GT.tr("No IOException expected from StringBuffer or StringBuilder", new Object[0]), PSQLState.UNEXPECTED_ERROR, e);
        }
    }
    
    public static StringBuilder escapeIdentifier(StringBuilder sbuf, final String value) throws SQLException {
        if (sbuf == null) {
            sbuf = new StringBuilder(2 + (value.length() + 10) / 10 * 11);
        }
        doAppendEscapedIdentifier(sbuf, value);
        return sbuf;
    }
    
    private static void doAppendEscapedIdentifier(final Appendable sbuf, final String value) throws SQLException {
        try {
            sbuf.append('\"');
            for (int i = 0; i < value.length(); ++i) {
                final char ch = value.charAt(i);
                if (ch == '\0') {
                    throw new PSQLException(GT.tr("Zero bytes may not occur in identifiers.", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE);
                }
                if (ch == '\"') {
                    sbuf.append(ch);
                }
                sbuf.append(ch);
            }
            sbuf.append('\"');
        }
        catch (final IOException e) {
            throw new PSQLException(GT.tr("No IOException expected from StringBuffer or StringBuilder", new Object[0]), PSQLState.UNEXPECTED_ERROR, e);
        }
    }
    
    @Deprecated
    public static int parseServerVersionStr(final String serverVersion) throws NumberFormatException {
        return ServerVersion.parseServerVersionStr(serverVersion);
    }
    
    static {
        utf8Charset = Charset.forName("UTF-8");
    }
}
