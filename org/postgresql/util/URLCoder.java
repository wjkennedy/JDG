// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public final class URLCoder
{
    private static final String ENCODING_FOR_URL;
    
    public static String decode(final String encoded) {
        try {
            return URLDecoder.decode(encoded, URLCoder.ENCODING_FOR_URL);
        }
        catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException("Unable to decode URL entry via " + URLCoder.ENCODING_FOR_URL + ". This should not happen", e);
        }
    }
    
    public static String encode(final String plain) {
        try {
            return URLEncoder.encode(plain, "UTF-8");
        }
        catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException("Unable to encode URL entry via " + URLCoder.ENCODING_FOR_URL + ". This should not happen", e);
        }
    }
    
    static {
        ENCODING_FOR_URL = System.getProperty("postgresql.url.encoding", "UTF-8");
    }
}
