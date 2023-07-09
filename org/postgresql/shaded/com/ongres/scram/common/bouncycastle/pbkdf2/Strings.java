// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2;

import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;

public final class Strings
{
    public static byte[] toUTF8ByteArray(final char[] string) {
        final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        try {
            toUTF8ByteArray(string, bOut);
        }
        catch (final IOException e) {
            throw new IllegalStateException("cannot encode string to byte array!");
        }
        return bOut.toByteArray();
    }
    
    public static void toUTF8ByteArray(final char[] string, final OutputStream sOut) throws IOException {
        final char[] c = string;
        for (int i = 0; i < c.length; ++i) {
            char ch = c[i];
            if (ch < '\u0080') {
                sOut.write(ch);
            }
            else if (ch < '\u0800') {
                sOut.write(0xC0 | ch >> 6);
                sOut.write(0x80 | (ch & '?'));
            }
            else if (ch >= '\ud800' && ch <= '\udfff') {
                if (i + 1 >= c.length) {
                    throw new IllegalStateException("invalid UTF-16 codepoint");
                }
                final char W1 = ch;
                final char W2;
                ch = (W2 = c[++i]);
                if (W1 > '\udbff') {
                    throw new IllegalStateException("invalid UTF-16 codepoint");
                }
                final int codePoint = ((W1 & '\u03ff') << 10 | (W2 & '\u03ff')) + 65536;
                sOut.write(0xF0 | codePoint >> 18);
                sOut.write(0x80 | (codePoint >> 12 & 0x3F));
                sOut.write(0x80 | (codePoint >> 6 & 0x3F));
                sOut.write(0x80 | (codePoint & 0x3F));
            }
            else {
                sOut.write(0xE0 | ch >> 12);
                sOut.write(0x80 | (ch >> 6 & 0x3F));
                sOut.write(0x80 | (ch & '?'));
            }
        }
    }
    
    public static String fromByteArray(final byte[] bytes) {
        return new String(asCharArray(bytes));
    }
    
    public static char[] asCharArray(final byte[] bytes) {
        final char[] chars = new char[bytes.length];
        for (int i = 0; i != chars.length; ++i) {
            chars[i] = (char)(bytes[i] & 0xFF);
        }
        return chars;
    }
}
