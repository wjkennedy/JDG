// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.node;

import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.io.NumberOutput;
import java.math.BigInteger;
import java.math.BigDecimal;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

public final class DoubleNode extends NumericNode
{
    protected final double _value;
    
    public DoubleNode(final double v) {
        this._value = v;
    }
    
    public static DoubleNode valueOf(final double v) {
        return new DoubleNode(v);
    }
    
    @Override
    public JsonToken asToken() {
        return JsonToken.VALUE_NUMBER_FLOAT;
    }
    
    @Override
    public JsonParser.NumberType getNumberType() {
        return JsonParser.NumberType.DOUBLE;
    }
    
    @Override
    public boolean isFloatingPointNumber() {
        return true;
    }
    
    @Override
    public boolean isDouble() {
        return true;
    }
    
    @Override
    public Number getNumberValue() {
        return this._value;
    }
    
    @Override
    public int getIntValue() {
        return (int)this._value;
    }
    
    @Override
    public long getLongValue() {
        return (long)this._value;
    }
    
    @Override
    public double getDoubleValue() {
        return this._value;
    }
    
    @Override
    public BigDecimal getDecimalValue() {
        return BigDecimal.valueOf(this._value);
    }
    
    @Override
    public BigInteger getBigIntegerValue() {
        return this.getDecimalValue().toBigInteger();
    }
    
    @Override
    public String asText() {
        return NumberOutput.toString(this._value);
    }
    
    @Override
    public final void serialize(final JsonGenerator jg, final SerializerProvider provider) throws IOException, JsonProcessingException {
        jg.writeNumber(this._value);
    }
    
    @Override
    public boolean equals(final Object o) {
        return o == this || (o != null && o.getClass() == this.getClass() && ((DoubleNode)o)._value == this._value);
    }
    
    @Override
    public int hashCode() {
        final long l = Double.doubleToLongBits(this._value);
        return (int)l ^ (int)(l >> 32);
    }
}
