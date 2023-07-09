// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.saslprep;

import java.util.Iterator;
import java.util.List;
import java.text.Normalizer;
import java.nio.CharBuffer;
import org.postgresql.shaded.com.ongres.stringprep.StringPrep;
import java.util.ArrayList;

public class SaslPrep
{
    private static final int MAX_UTF = 65535;
    
    public static String saslPrep(final String value, final boolean storedString) {
        List<Integer> valueBuilder = new ArrayList<Integer>();
        final List<Integer> codePoints = new ArrayList<Integer>();
        for (int i = 0; i < value.length(); ++i) {
            final int codePoint = value.codePointAt(i);
            codePoints.add(codePoint);
            if (codePoint > 65535) {
                ++i;
            }
            if (!StringPrep.prohibitionNonAsciiSpace(codePoint)) {
                valueBuilder.add(codePoint);
            }
        }
        final StringBuilder stringBuilder = new StringBuilder();
        for (final int codePoint2 : codePoints) {
            if (!StringPrep.mapToNothing(codePoint2)) {
                final char[] characters = Character.toChars(codePoint2);
                stringBuilder.append(characters);
            }
        }
        final String normalized = Normalizer.normalize(CharBuffer.wrap(stringBuilder.toString().toCharArray()), Normalizer.Form.NFKC);
        valueBuilder = new ArrayList<Integer>();
        for (int j = 0; j < normalized.length(); ++j) {
            final int codePoint3 = normalized.codePointAt(j);
            codePoints.add(codePoint3);
            if (codePoint3 > 65535) {
                ++j;
            }
            if (!StringPrep.prohibitionNonAsciiSpace(codePoint3)) {
                valueBuilder.add(codePoint3);
            }
        }
        for (final int character : valueBuilder) {
            if (StringPrep.prohibitionNonAsciiSpace(character) || StringPrep.prohibitionAsciiControl(character) || StringPrep.prohibitionNonAsciiControl(character) || StringPrep.prohibitionPrivateUse(character) || StringPrep.prohibitionNonCharacterCodePoints(character) || StringPrep.prohibitionSurrogateCodes(character) || StringPrep.prohibitionInappropriatePlainText(character) || StringPrep.prohibitionInappropriateCanonicalRepresentation(character) || StringPrep.prohibitionChangeDisplayProperties(character) || StringPrep.prohibitionTaggingCharacters(character)) {
                throw new IllegalArgumentException("Prohibited character " + String.valueOf(Character.toChars(character)));
            }
            if (storedString && StringPrep.unassignedCodePoints(character)) {
                throw new IllegalArgumentException("Prohibited character " + String.valueOf(Character.toChars(character)));
            }
        }
        StringPrep.bidirectional(valueBuilder);
        return normalized;
    }
}
