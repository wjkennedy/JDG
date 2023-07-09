// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.node;

import java.math.BigInteger;
import java.math.BigDecimal;
import org.codehaus.jackson.JsonParser;

public abstract class NumericNode extends ValueNode
{
    protected NumericNode() {
    }
    
    @Override
    public final boolean isNumber() {
        return true;
    }
    
    @Override
    public abstract JsonParser.NumberType getNumberType();
    
    @Override
    public abstract Number getNumberValue();
    
    @Override
    public abstract int getIntValue();
    
    @Override
    public abstract long getLongValue();
    
    @Override
    public abstract double getDoubleValue();
    
    @Override
    public abstract BigDecimal getDecimalValue();
    
    @Override
    public abstract BigInteger getBigIntegerValue();
    
    @Override
    public abstract String asText();
    
    @Override
    public int asInt() {
        return this.getIntValue();
    }
    
    @Override
    public int asInt(final int defaultValue) {
        return this.getIntValue();
    }
    
    @Override
    public long asLong() {
        return this.getLongValue();
    }
    
    @Override
    public long asLong(final long defaultValue) {
        return this.getLongValue();
    }
    
    @Override
    public double asDouble() {
        return this.getDoubleValue();
    }
    
    @Override
    public double asDouble(final double defaultValue) {
        return this.getDoubleValue();
    }
}
