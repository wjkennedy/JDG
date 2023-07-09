// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.node;

import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonToken;

public final class BooleanNode extends ValueNode
{
    public static final BooleanNode TRUE;
    public static final BooleanNode FALSE;
    
    private BooleanNode() {
    }
    
    public static BooleanNode getTrue() {
        return BooleanNode.TRUE;
    }
    
    public static BooleanNode getFalse() {
        return BooleanNode.FALSE;
    }
    
    public static BooleanNode valueOf(final boolean b) {
        return b ? BooleanNode.TRUE : BooleanNode.FALSE;
    }
    
    @Override
    public JsonToken asToken() {
        return (this == BooleanNode.TRUE) ? JsonToken.VALUE_TRUE : JsonToken.VALUE_FALSE;
    }
    
    @Override
    public boolean isBoolean() {
        return true;
    }
    
    @Override
    public boolean getBooleanValue() {
        return this == BooleanNode.TRUE;
    }
    
    @Override
    public String asText() {
        return (this == BooleanNode.TRUE) ? "true" : "false";
    }
    
    @Override
    public boolean asBoolean() {
        return this == BooleanNode.TRUE;
    }
    
    @Override
    public boolean asBoolean(final boolean defaultValue) {
        return this == BooleanNode.TRUE;
    }
    
    @Override
    public int asInt(final int defaultValue) {
        return (this == BooleanNode.TRUE) ? 1 : 0;
    }
    
    @Override
    public long asLong(final long defaultValue) {
        return (this == BooleanNode.TRUE) ? 1 : 0;
    }
    
    @Override
    public double asDouble(final double defaultValue) {
        return (this == BooleanNode.TRUE) ? 1.0 : 0.0;
    }
    
    @Override
    public final void serialize(final JsonGenerator jg, final SerializerProvider provider) throws IOException, JsonProcessingException {
        jg.writeBoolean(this == BooleanNode.TRUE);
    }
    
    @Override
    public boolean equals(final Object o) {
        return o == this;
    }
    
    static {
        TRUE = new BooleanNode();
        FALSE = new BooleanNode();
    }
}
