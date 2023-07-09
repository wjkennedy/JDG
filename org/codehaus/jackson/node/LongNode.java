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

public final class LongNode extends NumericNode
{
    final long _value;
    
    public LongNode(final long v) {
        this._value = v;
    }
    
    public static LongNode valueOf(final long l) {
        return new LongNode(l);
    }
    
    @Override
    public JsonToken asToken() {
        return JsonToken.VALUE_NUMBER_INT;
    }
    
    @Override
    public JsonParser.NumberType getNumberType() {
        return JsonParser.NumberType.LONG;
    }
    
    @Override
    public boolean isIntegralNumber() {
        return true;
    }
    
    @Override
    public boolean isLong() {
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
        return this._value;
    }
    
    @Override
    public double getDoubleValue() {
        return (double)this._value;
    }
    
    @Override
    public BigDecimal getDecimalValue() {
        return BigDecimal.valueOf(this._value);
    }
    
    @Override
    public BigInteger getBigIntegerValue() {
        return BigInteger.valueOf(this._value);
    }
    
    @Override
    public String asText() {
        return NumberOutput.toString(this._value);
    }
    
    @Override
    public boolean asBoolean(final boolean defaultValue) {
        return this._value != 0L;
    }
    
    @Override
    public final void serialize(final JsonGenerator jg, final SerializerProvider provider) throws IOException, JsonProcessingException {
        jg.writeNumber(this._value);
    }
    
    @Override
    public boolean equals(final Object o) {
        return o == this || (o != null && o.getClass() == this.getClass() && ((LongNode)o)._value == this._value);
    }
    
    @Override
    public int hashCode() {
        return (int)this._value ^ (int)(this._value >> 32);
    }
}
