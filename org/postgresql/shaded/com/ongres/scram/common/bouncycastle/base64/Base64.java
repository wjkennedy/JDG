// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.bouncycastle.base64;

import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.Strings;

public class Base64
{
    private static final Encoder encoder;
    
    public static String toBase64String(final byte[] data) {
        return toBase64String(data, 0, data.length);
    }
    
    public static String toBase64String(final byte[] data, final int off, final int length) {
        final byte[] encoded = encode(data, off, length);
        return Strings.fromByteArray(encoded);
    }
    
    public static byte[] encode(final byte[] data) {
        return encode(data, 0, data.length);
    }
    
    public static byte[] encode(final byte[] data, final int off, final int length) {
        final int len = (length + 2) / 3 * 4;
        final ByteArrayOutputStream bOut = new ByteArrayOutputStream(len);
        try {
            Base64.encoder.encode(data, off, length, bOut);
        }
        catch (final Exception e) {
            throw new EncoderException("exception encoding base64 string: " + e.getMessage(), e);
        }
        return bOut.toByteArray();
    }
    
    public static int encode(final byte[] data, final OutputStream out) throws IOException {
        return Base64.encoder.encode(data, 0, data.length, out);
    }
    
    public static int encode(final byte[] data, final int off, final int length, final OutputStream out) throws IOException {
        return Base64.encoder.encode(data, off, length, out);
    }
    
    public static byte[] decode(final byte[] data) {
        final int len = data.length / 4 * 3;
        final ByteArrayOutputStream bOut = new ByteArrayOutputStream(len);
        try {
            Base64.encoder.decode(data, 0, data.length, bOut);
        }
        catch (final Exception e) {
            throw new DecoderException("unable to decode base64 data: " + e.getMessage(), e);
        }
        return bOut.toByteArray();
    }
    
    public static byte[] decode(final String data) {
        final int len = data.length() / 4 * 3;
        final ByteArrayOutputStream bOut = new ByteArrayOutputStream(len);
        try {
            Base64.encoder.decode(data, bOut);
        }
        catch (final Exception e) {
            throw new DecoderException("unable to decode base64 string: " + e.getMessage(), e);
        }
        return bOut.toByteArray();
    }
    
    public static int decode(final String data, final OutputStream out) throws IOException {
        return Base64.encoder.decode(data, out);
    }
    
    public static int decode(final byte[] base64Data, final int start, final int length, final OutputStream out) {
        try {
            return Base64.encoder.decode(base64Data, start, length, out);
        }
        catch (final Exception e) {
            throw new DecoderException("unable to decode base64 data: " + e.getMessage(), e);
        }
    }
    
    static {
        encoder = new Base64Encoder();
    }
}
