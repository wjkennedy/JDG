// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.util;

import java.security.InvalidKeyException;
import java.security.Key;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.security.SecureRandom;

public class CryptoUtil
{
    private static final int MIN_ASCII_PRINTABLE_RANGE = 33;
    private static final int MAX_ASCII_PRINTABLE_RANGE = 126;
    private static final int EXCLUDED_CHAR = 44;
    
    public static String nonce(final int size, final SecureRandom random) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        final char[] chars = new char[size];
        int r;
        for (int i = 0; i < size; chars[i++] = (char)r) {
            r = random.nextInt(94) + 33;
            if (r != 44) {}
        }
        return new String(chars);
    }
    
    public static String nonce(final int size) {
        return nonce(size, SecureRandomHolder.INSTANCE);
    }
    
    public static byte[] hi(final SecretKeyFactory secretKeyFactory, final int keyLength, final char[] value, final byte[] salt, final int iterations) {
        try {
            final PBEKeySpec spec = new PBEKeySpec(value, salt, iterations, keyLength);
            final SecretKey key = secretKeyFactory.generateSecret(spec);
            return key.getEncoded();
        }
        catch (final InvalidKeySpecException e) {
            throw new RuntimeException("Platform error: unsupported PBEKeySpec");
        }
    }
    
    public static byte[] hmac(final SecretKeySpec secretKeySpec, final Mac mac, final byte[] message) {
        try {
            mac.init(secretKeySpec);
        }
        catch (final InvalidKeyException e) {
            throw new RuntimeException("Platform error: unsupported key for HMAC algorithm");
        }
        return mac.doFinal(message);
    }
    
    public static byte[] xor(final byte[] value1, final byte[] value2) throws IllegalArgumentException {
        Preconditions.checkNotNull(value1, "value1");
        Preconditions.checkNotNull(value2, "value2");
        Preconditions.checkArgument(value1.length == value2.length, "Both values must have the same length");
        final byte[] result = new byte[value1.length];
        for (int i = 0; i < value1.length; ++i) {
            result[i] = (byte)(value1[i] ^ value2[i]);
        }
        return result;
    }
    
    private static class SecureRandomHolder
    {
        private static final SecureRandom INSTANCE;
        
        static {
            INSTANCE = new SecureRandom();
        }
    }
}
