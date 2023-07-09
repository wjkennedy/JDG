// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.node;

import java.util.Arrays;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.Base64Variants;
import org.codehaus.jackson.JsonToken;

public final class BinaryNode extends ValueNode
{
    static final BinaryNode EMPTY_BINARY_NODE;
    final byte[] _data;
    
    public BinaryNode(final byte[] data) {
        this._data = data;
    }
    
    public BinaryNode(final byte[] data, final int offset, final int length) {
        if (offset == 0 && length == data.length) {
            this._data = data;
        }
        else {
            System.arraycopy(data, offset, this._data = new byte[length], 0, length);
        }
    }
    
    public static BinaryNode valueOf(final byte[] data) {
        if (data == null) {
            return null;
        }
        if (data.length == 0) {
            return BinaryNode.EMPTY_BINARY_NODE;
        }
        return new BinaryNode(data);
    }
    
    public static BinaryNode valueOf(final byte[] data, final int offset, final int length) {
        if (data == null) {
            return null;
        }
        if (length == 0) {
            return BinaryNode.EMPTY_BINARY_NODE;
        }
        return new BinaryNode(data, offset, length);
    }
    
    @Override
    public JsonToken asToken() {
        return JsonToken.VALUE_EMBEDDED_OBJECT;
    }
    
    @Override
    public boolean isBinary() {
        return true;
    }
    
    @Override
    public byte[] getBinaryValue() {
        return this._data;
    }
    
    @Override
    public String asText() {
        return Base64Variants.getDefaultVariant().encode(this._data, false);
    }
    
    @Override
    public final void serialize(final JsonGenerator jg, final SerializerProvider provider) throws IOException, JsonProcessingException {
        jg.writeBinary(this._data);
    }
    
    @Override
    public boolean equals(final Object o) {
        return o == this || (o != null && o.getClass() == this.getClass() && Arrays.equals(((BinaryNode)o)._data, this._data));
    }
    
    @Override
    public int hashCode() {
        return (this._data == null) ? -1 : this._data.length;
    }
    
    @Override
    public String toString() {
        return Base64Variants.getDefaultVariant().encode(this._data, true);
    }
    
    static {
        EMPTY_BINARY_NODE = new BinaryNode(new byte[0]);
    }
}
