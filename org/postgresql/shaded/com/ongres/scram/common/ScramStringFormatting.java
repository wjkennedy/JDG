// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common;

import java.nio.charset.StandardCharsets;
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.base64.Base64;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;

public class ScramStringFormatting
{
    public static String toSaslName(final String value) {
        if (null == value || value.isEmpty()) {
            return value;
        }
        int nComma = 0;
        int nEqual = 0;
        final char[] charArray;
        final char[] originalChars = charArray = value.toCharArray();
        for (final char c : charArray) {
            if (',' == c) {
                ++nComma;
            }
            else if ('=' == c) {
                ++nEqual;
            }
        }
        if (nComma == 0 && nEqual == 0) {
            return value;
        }
        final char[] saslChars = new char[originalChars.length + nComma * 2 + nEqual * 2];
        int i = 0;
        for (final char c2 : originalChars) {
            if (',' == c2) {
                saslChars[i++] = '=';
                saslChars[i++] = '2';
                saslChars[i++] = 'C';
            }
            else if ('=' == c2) {
                saslChars[i++] = '=';
                saslChars[i++] = '3';
                saslChars[i++] = 'D';
            }
            else {
                saslChars[i++] = c2;
            }
        }
        return new String(saslChars);
    }
    
    public static String fromSaslName(final String value) throws IllegalArgumentException {
        if (null == value || value.isEmpty()) {
            return value;
        }
        int nEqual = 0;
        final char[] orig = value.toCharArray();
        for (int i = 0; i < orig.length; ++i) {
            if (orig[i] == ',') {
                throw new IllegalArgumentException("Invalid ',' character present in saslName");
            }
            if (orig[i] == '=') {
                ++nEqual;
                if (i + 2 > orig.length - 1) {
                    throw new IllegalArgumentException("Invalid '=' character present in saslName");
                }
                if ((orig[i + 1] != '2' || orig[i + 2] != 'C') && (orig[i + 1] != '3' || orig[i + 2] != 'D')) {
                    throw new IllegalArgumentException("Invalid char '=" + orig[i + 1] + orig[i + 2] + "' found in saslName");
                }
            }
        }
        if (nEqual == 0) {
            return value;
        }
        final char[] replaced = new char[orig.length - nEqual * 2];
        int r = 0;
        int o = 0;
        while (r < replaced.length) {
            if ('=' == orig[o]) {
                if (orig[o + 1] == '2' && orig[o + 2] == 'C') {
                    replaced[r] = ',';
                }
                else if (orig[o + 1] == '3' && orig[o + 2] == 'D') {
                    replaced[r] = '=';
                }
                o += 3;
            }
            else {
                replaced[r] = orig[o];
                ++o;
            }
            ++r;
        }
        return new String(replaced);
    }
    
    public static String base64Encode(final byte[] value) throws IllegalArgumentException {
        return Base64.toBase64String(Preconditions.checkNotNull(value, "value"));
    }
    
    public static String base64Encode(final String value) throws IllegalArgumentException {
        return base64Encode(Preconditions.checkNotEmpty(value, "value").getBytes(StandardCharsets.UTF_8));
    }
    
    public static byte[] base64Decode(final String value) throws IllegalArgumentException {
        return Base64.decode(Preconditions.checkNotEmpty(value, "value"));
    }
}
