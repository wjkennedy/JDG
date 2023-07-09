// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.node;

import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonToken;

public final class NullNode extends ValueNode
{
    public static final NullNode instance;
    
    private NullNode() {
    }
    
    public static NullNode getInstance() {
        return NullNode.instance;
    }
    
    @Override
    public JsonToken asToken() {
        return JsonToken.VALUE_NULL;
    }
    
    @Override
    public boolean isNull() {
        return true;
    }
    
    @Override
    public String asText() {
        return "null";
    }
    
    @Override
    public int asInt(final int defaultValue) {
        return 0;
    }
    
    @Override
    public long asLong(final long defaultValue) {
        return 0L;
    }
    
    @Override
    public double asDouble(final double defaultValue) {
        return 0.0;
    }
    
    @Override
    public final void serialize(final JsonGenerator jg, final SerializerProvider provider) throws IOException, JsonProcessingException {
        jg.writeNull();
    }
    
    @Override
    public boolean equals(final Object o) {
        return o == this;
    }
    
    static {
        instance = new NullNode();
    }
}
