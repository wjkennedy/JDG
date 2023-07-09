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

public final class IntNode extends NumericNode
{
    static final int MIN_CANONICAL = -1;
    static final int MAX_CANONICAL = 10;
    private static final IntNode[] CANONICALS;
    final int _value;
    
    public IntNode(final int v) {
        this._value = v;
    }
    
    public static IntNode valueOf(final int i) {
        if (i > 10 || i < -1) {
            return new IntNode(i);
        }
        return IntNode.CANONICALS[i + 1];
    }
    
    @Override
    public JsonToken asToken() {
        return JsonToken.VALUE_NUMBER_INT;
    }
    
    @Override
    public JsonParser.NumberType getNumberType() {
        return JsonParser.NumberType.INT;
    }
    
    @Override
    public boolean isIntegralNumber() {
        return true;
    }
    
    @Override
    public boolean isInt() {
        return true;
    }
    
    @Override
    public Number getNumberValue() {
        return this._value;
    }
    
    @Override
    public int getIntValue() {
        return this._value;
    }
    
    @Override
    public long getLongValue() {
        return this._value;
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
        return BigInteger.valueOf(this._value);
    }
    
    @Override
    public String asText() {
        return NumberOutput.toString(this._value);
    }
    
    @Override
    public boolean asBoolean(final boolean defaultValue) {
        return this._value != 0;
    }
    
    @Override
    public final void serialize(final JsonGenerator jg, final SerializerProvider provider) throws IOException, JsonProcessingException {
        jg.writeNumber(this._value);
    }
    
    @Override
    public boolean equals(final Object o) {
        return o == this || (o != null && o.getClass() == this.getClass() && ((IntNode)o)._value == this._value);
    }
    
    @Override
    public int hashCode() {
        return this._value;
    }
    
    static {
        final int count = 12;
        CANONICALS = new IntNode[count];
        for (int i = 0; i < count; ++i) {
            IntNode.CANONICALS[i] = new IntNode(-1 + i);
        }
    }
}
