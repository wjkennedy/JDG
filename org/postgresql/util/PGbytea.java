// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import java.sql.SQLException;

public class PGbytea
{
    private static final int MAX_3_BUFF_SIZE = 2097152;
    
    public static byte[] toBytes(final byte[] s) throws SQLException {
        if (s == null) {
            return null;
        }
        if (s.length < 2 || s[0] != 92 || s[1] != 120) {
            return toBytesOctalEscaped(s);
        }
        return toBytesHexEscaped(s);
    }
    
    private static byte[] toBytesHexEscaped(final byte[] s) {
        final byte[] output = new byte[(s.length - 2) / 2];
        for (int i = 0; i < output.length; ++i) {
            final byte b1 = gethex(s[2 + i * 2]);
            final byte b2 = gethex(s[2 + i * 2 + 1]);
            output[i] = (byte)(b1 << 4 | (b2 & 0xFF));
        }
        return output;
    }
    
    private static byte gethex(final byte b) {
        if (b <= 57) {
            return (byte)(b - 48);
        }
        if (b >= 97) {
            return (byte)(b - 97 + 10);
        }
        return (byte)(b - 65 + 10);
    }
    
    private static byte[] toBytesOctalEscaped(final byte[] s) {
        final int slength = s.length;
        byte[] buf = null;
        int correctSize = slength;
        if (slength > 2097152) {
            for (int i = 0; i < slength; ++i) {
                final byte current = s[i];
                if (current == 92) {
                    final byte next = s[++i];
                    if (next == 92) {
                        --correctSize;
                    }
                    else {
                        correctSize -= 3;
                    }
                }
            }
            buf = new byte[correctSize];
        }
        else {
            buf = new byte[slength];
        }
        int bufpos = 0;
        for (int j = 0; j < slength; ++j) {
            final byte nextbyte = s[j];
            if (nextbyte == 92) {
                final byte secondbyte = s[++j];
                if (secondbyte == 92) {
                    buf[bufpos++] = 92;
                }
                else {
                    int thebyte = (secondbyte - 48) * 64 + (s[++j] - 48) * 8 + (s[++j] - 48);
                    if (thebyte > 127) {
                        thebyte -= 256;
                    }
                    buf[bufpos++] = (byte)thebyte;
                }
            }
            else {
                buf[bufpos++] = nextbyte;
            }
        }
        if (bufpos == correctSize) {
            return buf;
        }
        final byte[] result = new byte[bufpos];
        System.arraycopy(buf, 0, result, 0, bufpos);
        return result;
    }
    
    public static String toPGString(final byte[] buf) {
        if (buf == null) {
            return null;
        }
        final StringBuilder stringBuilder = new StringBuilder(2 * buf.length);
        for (int elementAsInt : buf) {
            final byte element = (byte)elementAsInt;
            if (elementAsInt < 0) {
                elementAsInt += 256;
            }
            if (elementAsInt < 32 || elementAsInt > 126) {
                stringBuilder.append("\\");
                stringBuilder.append((char)((elementAsInt >> 6 & 0x3) + 48));
                stringBuilder.append((char)((elementAsInt >> 3 & 0x7) + 48));
                stringBuilder.append((char)((elementAsInt & 0x7) + 48));
            }
            else if (element == 92) {
                stringBuilder.append("\\\\");
            }
            else {
                stringBuilder.append((char)element);
            }
        }
        return stringBuilder.toString();
    }
}
