// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common;

import org.postgresql.shaded.com.ongres.scram.common.exception.ScramParseException;
import org.postgresql.shaded.com.ongres.scram.common.util.CharAttribute;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import org.postgresql.shaded.com.ongres.scram.common.util.AbstractCharAttributeValue;

public class ScramAttributeValue extends AbstractCharAttributeValue
{
    public ScramAttributeValue(final ScramAttributes attribute, final String value) {
        super(attribute, Preconditions.checkNotNull(value, "value"));
    }
    
    public static StringBuffer writeTo(final StringBuffer sb, final ScramAttributes attribute, final String value) {
        return new ScramAttributeValue(attribute, value).writeTo(sb);
    }
    
    public static ScramAttributeValue parse(final String value) throws ScramParseException {
        if (null == value || value.length() < 3 || value.charAt(1) != '=') {
            throw new ScramParseException("Invalid ScramAttributeValue '" + value + "'");
        }
        return new ScramAttributeValue(ScramAttributes.byChar(value.charAt(0)), value.substring(2));
    }
}
