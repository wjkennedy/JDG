// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;

public class MD5Digest
{
    private MD5Digest() {
    }
    
    public static byte[] encode(final byte[] user, final byte[] password, final byte[] salt) {
        final byte[] hexDigest = new byte[35];
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password);
            md.update(user);
            final byte[] tempDigest = md.digest();
            bytesToHex(tempDigest, hexDigest, 0);
            md.update(hexDigest, 0, 32);
            md.update(salt);
            final byte[] passDigest = md.digest();
            bytesToHex(passDigest, hexDigest, 3);
            hexDigest[0] = 109;
            hexDigest[1] = 100;
            hexDigest[2] = 53;
        }
        catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to encode password with MD5", e);
        }
        return hexDigest;
    }
    
    private static void bytesToHex(final byte[] bytes, final byte[] hex, final int offset) {
        final char[] lookup = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        int pos = offset;
        for (int i = 0; i < 16; ++i) {
            final int c = bytes[i] & 0xFF;
            int j = c >> 4;
            hex[pos++] = (byte)lookup[j];
            j = (c & 0xF);
            hex[pos++] = (byte)lookup[j];
        }
    }
}
