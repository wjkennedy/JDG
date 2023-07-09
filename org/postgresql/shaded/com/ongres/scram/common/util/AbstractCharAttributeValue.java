// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.util;

public class AbstractCharAttributeValue extends AbstractStringWritable implements CharAttributeValue
{
    private final CharAttribute charAttribute;
    private final String value;
    
    public AbstractCharAttributeValue(final CharAttribute charAttribute, final String value) throws IllegalArgumentException {
        this.charAttribute = Preconditions.checkNotNull(charAttribute, "attribute");
        if (null != value && value.isEmpty()) {
            throw new IllegalArgumentException("Value should be either null or non-empty");
        }
        this.value = value;
    }
    
    @Override
    public char getChar() {
        return this.charAttribute.getChar();
    }
    
    @Override
    public String getValue() {
        return this.value;
    }
    
    @Override
    public StringBuffer writeTo(final StringBuffer sb) {
        sb.append(this.charAttribute.getChar());
        if (null != this.value) {
            sb.append('=').append(this.value);
        }
        return sb;
    }
}
