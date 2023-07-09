// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common;

import java.util.HashMap;
import org.postgresql.shaded.com.ongres.scram.common.exception.ScramParseException;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import java.util.Map;
import org.postgresql.shaded.com.ongres.scram.common.util.CharAttribute;

public enum ScramAttributes implements CharAttribute
{
    USERNAME('n'), 
    AUTHZID('a'), 
    NONCE('r'), 
    CHANNEL_BINDING('c'), 
    SALT('s'), 
    ITERATION('i'), 
    CLIENT_PROOF('p'), 
    SERVER_SIGNATURE('v'), 
    ERROR('e');
    
    private final char attributeChar;
    private static final Map<Character, ScramAttributes> REVERSE_MAPPING;
    
    private ScramAttributes(final char attributeChar) {
        this.attributeChar = Preconditions.checkNotNull(attributeChar, "attributeChar");
    }
    
    @Override
    public char getChar() {
        return this.attributeChar;
    }
    
    public static ScramAttributes byChar(final char c) throws ScramParseException {
        if (!ScramAttributes.REVERSE_MAPPING.containsKey(c)) {
            throw new ScramParseException("Attribute with char '" + c + "' does not exist");
        }
        return ScramAttributes.REVERSE_MAPPING.get(c);
    }
    
    static {
        REVERSE_MAPPING = new HashMap<Character, ScramAttributes>();
        for (final ScramAttributes scramAttribute : values()) {
            ScramAttributes.REVERSE_MAPPING.put(scramAttribute.getChar(), scramAttribute);
        }
    }
}
