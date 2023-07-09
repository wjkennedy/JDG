// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import java.io.UnsupportedEncodingException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.InputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.logging.Logger;

public class Encoding
{
    private static final Logger LOGGER;
    private static final Encoding DEFAULT_ENCODING;
    private static final HashMap<String, String[]> encodings;
    private static final UTFEncodingProvider UTF_ENCODING_PROVIDER;
    private final Charset encoding;
    private final boolean fastASCIINumbers;
    
    private Encoding() {
        this(Charset.defaultCharset());
    }
    
    protected Encoding(final Charset encoding, final boolean fastASCIINumbers) {
        if (encoding == null) {
            throw new NullPointerException("Null encoding charset not supported");
        }
        this.encoding = encoding;
        this.fastASCIINumbers = fastASCIINumbers;
        if (Encoding.LOGGER.isLoggable(Level.FINEST)) {
            Encoding.LOGGER.log(Level.FINEST, "Creating new Encoding {0} with fastASCIINumbers {1}", new Object[] { encoding, fastASCIINumbers });
        }
    }
    
    protected Encoding(final Charset encoding) {
        this(encoding, testAsciiNumbers(encoding));
    }
    
    public boolean hasAsciiNumbers() {
        return this.fastASCIINumbers;
    }
    
    public static Encoding getJVMEncoding(final String jvmEncoding) {
        if ("UTF-8".equals(jvmEncoding)) {
            return Encoding.UTF_ENCODING_PROVIDER.getEncoding();
        }
        if (Charset.isSupported(jvmEncoding)) {
            return new Encoding(Charset.forName(jvmEncoding));
        }
        return Encoding.DEFAULT_ENCODING;
    }
    
    public static Encoding getDatabaseEncoding(final String databaseEncoding) {
        if ("UTF8".equals(databaseEncoding)) {
            return Encoding.UTF_ENCODING_PROVIDER.getEncoding();
        }
        final String[] candidates = Encoding.encodings.get(databaseEncoding);
        if (candidates != null) {
            for (final String candidate : candidates) {
                Encoding.LOGGER.log(Level.FINEST, "Search encoding candidate {0}", candidate);
                if (Charset.isSupported(candidate)) {
                    return new Encoding(Charset.forName(candidate));
                }
            }
        }
        if (Charset.isSupported(databaseEncoding)) {
            return new Encoding(Charset.forName(databaseEncoding));
        }
        Encoding.LOGGER.log(Level.FINEST, "{0} encoding not found, returning default encoding", databaseEncoding);
        return Encoding.DEFAULT_ENCODING;
    }
    
    public String name() {
        return this.encoding.name();
    }
    
    public byte[] encode(final String s) throws IOException {
        if (s == null) {
            return null;
        }
        return s.getBytes(this.encoding);
    }
    
    public String decode(final byte[] encodedString, final int offset, final int length) throws IOException {
        return new String(encodedString, offset, length, this.encoding);
    }
    
    public String decode(final byte[] encodedString) throws IOException {
        return this.decode(encodedString, 0, encodedString.length);
    }
    
    public Reader getDecodingReader(final InputStream in) throws IOException {
        return new InputStreamReader(in, this.encoding);
    }
    
    public Writer getEncodingWriter(final OutputStream out) throws IOException {
        return new OutputStreamWriter(out, this.encoding);
    }
    
    public static Encoding defaultEncoding() {
        return Encoding.DEFAULT_ENCODING;
    }
    
    @Override
    public String toString() {
        return this.encoding.name();
    }
    
    private static boolean testAsciiNumbers(final Charset encoding) {
        try {
            final String test = "-0123456789";
            final byte[] bytes = test.getBytes(encoding);
            final String res = new String(bytes, "US-ASCII");
            return test.equals(res);
        }
        catch (final UnsupportedEncodingException e) {
            return false;
        }
    }
    
    static {
        LOGGER = Logger.getLogger(Encoding.class.getName());
        DEFAULT_ENCODING = new Encoding();
        (encodings = new HashMap<String, String[]>()).put("SQL_ASCII", new String[] { "ASCII", "US-ASCII" });
        Encoding.encodings.put("UNICODE", new String[] { "UTF-8", "UTF8" });
        Encoding.encodings.put("UTF8", new String[] { "UTF-8", "UTF8" });
        Encoding.encodings.put("LATIN1", new String[] { "ISO8859_1" });
        Encoding.encodings.put("LATIN2", new String[] { "ISO8859_2" });
        Encoding.encodings.put("LATIN3", new String[] { "ISO8859_3" });
        Encoding.encodings.put("LATIN4", new String[] { "ISO8859_4" });
        Encoding.encodings.put("ISO_8859_5", new String[] { "ISO8859_5" });
        Encoding.encodings.put("ISO_8859_6", new String[] { "ISO8859_6" });
        Encoding.encodings.put("ISO_8859_7", new String[] { "ISO8859_7" });
        Encoding.encodings.put("ISO_8859_8", new String[] { "ISO8859_8" });
        Encoding.encodings.put("LATIN5", new String[] { "ISO8859_9" });
        Encoding.encodings.put("LATIN7", new String[] { "ISO8859_13" });
        Encoding.encodings.put("LATIN9", new String[] { "ISO8859_15_FDIS" });
        Encoding.encodings.put("EUC_JP", new String[] { "EUC_JP" });
        Encoding.encodings.put("EUC_CN", new String[] { "EUC_CN" });
        Encoding.encodings.put("EUC_KR", new String[] { "EUC_KR" });
        Encoding.encodings.put("JOHAB", new String[] { "Johab" });
        Encoding.encodings.put("EUC_TW", new String[] { "EUC_TW" });
        Encoding.encodings.put("SJIS", new String[] { "MS932", "SJIS" });
        Encoding.encodings.put("BIG5", new String[] { "Big5", "MS950", "Cp950" });
        Encoding.encodings.put("GBK", new String[] { "GBK", "MS936" });
        Encoding.encodings.put("UHC", new String[] { "MS949", "Cp949", "Cp949C" });
        Encoding.encodings.put("TCVN", new String[] { "Cp1258" });
        Encoding.encodings.put("WIN1256", new String[] { "Cp1256" });
        Encoding.encodings.put("WIN1250", new String[] { "Cp1250" });
        Encoding.encodings.put("WIN874", new String[] { "MS874", "Cp874" });
        Encoding.encodings.put("WIN", new String[] { "Cp1251" });
        Encoding.encodings.put("ALT", new String[] { "Cp866" });
        Encoding.encodings.put("KOI8", new String[] { "KOI8_U", "KOI8_R" });
        Encoding.encodings.put("UNKNOWN", new String[0]);
        Encoding.encodings.put("MULE_INTERNAL", new String[0]);
        Encoding.encodings.put("LATIN6", new String[0]);
        Encoding.encodings.put("LATIN8", new String[0]);
        Encoding.encodings.put("LATIN10", new String[0]);
        final JavaVersion runtimeVersion = JavaVersion.getRuntimeVersion();
        if (JavaVersion.v1_8.compareTo(runtimeVersion) >= 0) {
            UTF_ENCODING_PROVIDER = new UTFEncodingProvider() {
                @Override
                public Encoding getEncoding() {
                    return new CharOptimizedUTF8Encoder();
                }
            };
        }
        else {
            UTF_ENCODING_PROVIDER = new UTFEncodingProvider() {
                @Override
                public Encoding getEncoding() {
                    return new ByteOptimizedUTF8Encoder();
                }
            };
        }
    }
    
    private interface UTFEncodingProvider
    {
        Encoding getEncoding();
    }
}
