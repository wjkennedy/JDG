// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.node;

import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import java.math.BigInteger;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import java.math.BigDecimal;

public final class DecimalNode extends NumericNode
{
    protected final BigDecimal _value;
    
    public DecimalNode(final BigDecimal v) {
        this._value = v;
    }
    
    public static DecimalNode valueOf(final BigDecimal d) {
        return new DecimalNode(d);
    }
    
    @Override
    public JsonToken asToken() {
        return JsonToken.VALUE_NUMBER_FLOAT;
    }
    
    @Override
    public JsonParser.NumberType getNumberType() {
        return JsonParser.NumberType.BIG_DECIMAL;
    }
    
    @Override
    public boolean isFloatingPointNumber() {
        return true;
    }
    
    @Override
    public boolean isBigDecimal() {
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
        return this._value.toBigInteger();
    }
    
    @Override
    public double getDoubleValue() {
        return this._value.doubleValue();
    }
    
    @Override
    public BigDecimal getDecimalValue() {
        return this._value;
    }
    
    @Override
    public String asText() {
        return this._value.toString();
    }
    
    @Override
    public final void serialize(final JsonGenerator jg, final SerializerProvider provider) throws IOException, JsonProcessingException {
        jg.writeNumber(this._value);
    }
    
    @Override
    public boolean equals(final Object o) {
        return o == this || (o != null && o.getClass() == this.getClass() && ((DecimalNode)o)._value.equals(this._value));
    }
    
    @Override
    public int hashCode() {
        return this._value.hashCode();
    }
}
