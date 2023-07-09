// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import java.io.IOException;

public class EncodingPredictor
{
    private static final Translation[] FATAL_TRANSLATIONS;
    
    public static DecodeResult decode(final byte[] bytes, final int offset, final int length) {
        final Encoding defaultEncoding = Encoding.defaultEncoding();
        for (final Translation tr : EncodingPredictor.FATAL_TRANSLATIONS) {
            for (final String encoding : tr.encodings) {
                final Encoding encoder = Encoding.getDatabaseEncoding(encoding);
                Label_0288: {
                    if (encoder != defaultEncoding) {
                        if (tr.fatalText != null) {
                            byte[] encoded;
                            try {
                                final byte[] tmp = encoder.encode(tr.fatalText);
                                encoded = new byte[tmp.length + 2];
                                encoded[0] = 83;
                                encoded[encoded.length - 1] = 0;
                                System.arraycopy(tmp, 0, encoded, 1, tmp.length);
                            }
                            catch (final IOException e) {
                                break Label_0288;
                            }
                            if (!arrayContains(bytes, offset, length, encoded, 0, encoded.length)) {
                                break Label_0288;
                            }
                        }
                        if (tr.texts != null) {
                            boolean foundOne = false;
                            for (final String text : tr.texts) {
                                try {
                                    final byte[] textBytes = encoder.encode(text);
                                    if (arrayContains(bytes, offset, length, textBytes, 0, textBytes.length)) {
                                        foundOne = true;
                                        break;
                                    }
                                }
                                catch (final IOException ex) {}
                            }
                            if (!foundOne) {
                                break Label_0288;
                            }
                        }
                        try {
                            final String decoded = encoder.decode(bytes, offset, length);
                            if (decoded.indexOf(65533) == -1) {
                                return new DecodeResult(decoded, encoder.name());
                            }
                        }
                        catch (final IOException ex2) {}
                    }
                }
            }
        }
        return null;
    }
    
    private static boolean arrayContains(final byte[] first, final int firstOffset, final int firstLength, final byte[] second, final int secondOffset, final int secondLength) {
        if (firstLength < secondLength) {
            return false;
        }
        for (int i = 0; i < firstLength; ++i) {
            while (i < firstLength && first[firstOffset + i] != second[secondOffset]) {
                ++i;
            }
            int j;
            for (j = 1; j < secondLength && first[firstOffset + i + j] == second[secondOffset + j]; ++j) {}
            if (j == secondLength) {
                return true;
            }
        }
        return false;
    }
    
    static {
        FATAL_TRANSLATIONS = new Translation[] { new Translation("\u0412\u0410\u0416\u041d\u041e", null, "ru", new String[] { "WIN", "ALT", "KOI8" }), new Translation("\u81f4\u547d\u9519\u8bef", null, "zh_CN", new String[] { "EUC_CN", "GBK", "BIG5" }), new Translation("KATASTROFALNY", null, "pl", new String[] { "LATIN2" }), new Translation("FATALE", null, "it", new String[] { "LATIN1", "LATIN9" }), new Translation("FATAL", new String[] { "\u306f\u5b58\u5728\u3057\u307e\u305b\u3093", "\u30ed\u30fc\u30eb", "\u30e6\u30fc\u30b6" }, "ja", new String[] { "EUC_JP", "SJIS" }), new Translation(null, null, "fr/de/es/pt_BR", new String[] { "LATIN1", "LATIN3", "LATIN4", "LATIN5", "LATIN7", "LATIN9" }) };
    }
    
    public static class DecodeResult
    {
        public final String result;
        public final String encoding;
        
        DecodeResult(final String result, final String encoding) {
            this.result = result;
            this.encoding = encoding;
        }
    }
    
    static class Translation
    {
        public final String fatalText;
        private final String[] texts;
        public final String language;
        public final String[] encodings;
        
        Translation(final String fatalText, final String[] texts, final String language, final String... encodings) {
            this.fatalText = fatalText;
            this.texts = texts;
            this.language = language;
            this.encodings = encodings;
        }
    }
}
