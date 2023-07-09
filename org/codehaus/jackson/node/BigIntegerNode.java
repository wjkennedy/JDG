// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.node;

import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import java.math.BigDecimal;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import java.math.BigInteger;

public final class BigIntegerNode extends NumericNode
{
    protected final BigInteger _value;
    
    public BigIntegerNode(final BigInteger v) {
        this._value = v;
    }
    
    public static BigIntegerNode valueOf(final BigInteger v) {
        return new BigIntegerNode(v);
    }
    
    @Override
    public JsonToken asToken() {
        return JsonToken.VALUE_NUMBER_INT;
    }
    
    @Override
    public JsonParser.NumberType getNumberType() {
        return JsonParser.NumberType.BIG_INTEGER;
    }
    
    @Override
    public boolean isIntegralNumber() {
        return true;
    }
    
    @Override
    public boolean isBigInteger() {
        return true;
    }
    
    @Override
    public Number getNumberValue() {
        return this._value;
    }
    
    @Override
    public int getIntValue() {
        return this._value.intValue();
    }
    
    @Override
    public long getLongValue() {
        return this._value.longValue();
    }
    
    @Override
    public BigInteger getBigIntegerValue() {
        return this._value;
    }
    
    @Override
    public double getDoubleValue() {
        return this._value.doubleValue();
    }
    
    @Override
    public BigDecimal getDecimalValue() {
        return new BigDecimal(this._value);
    }
    
    @Override
    public String asText() {
        return this._value.toString();
    }
    
    @Override
    public boolean asBoolean(final boolean defaultValue) {
        return !BigInteger.ZERO.equals(this._value);
    }
    
    @Override
    public final void serialize(final JsonGenerator jg, final SerializerProvider provider) throws IOException, JsonProcessingException {
        jg.writeNumber(this._value);
    }
    
    @Override
    public boolean equals(final Object o) {
        return o == this || (o != null && o.getClass() == this.getClass() && ((BigIntegerNode)o)._value.equals(this._value));
    }
    
    @Override
    public int hashCode() {
        return this._value.hashCode();
    }
}
