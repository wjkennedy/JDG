// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.node;

import java.math.BigDecimal;
import java.math.BigInteger;

public class JsonNodeFactory
{
    public static final JsonNodeFactory instance;
    
    protected JsonNodeFactory() {
    }
    
    public BooleanNode booleanNode(final boolean v) {
        return v ? BooleanNode.getTrue() : BooleanNode.getFalse();
    }
    
    public NullNode nullNode() {
        return NullNode.getInstance();
    }
    
    public NumericNode numberNode(final byte v) {
        return IntNode.valueOf(v);
    }
    
    public ValueNode numberNode(final Byte value) {
        return (value == null) ? this.nullNode() : IntNode.valueOf(value);
    }
    
    public NumericNode numberNode(final short v) {
        return IntNode.valueOf(v);
    }
    
    public ValueNode numberNode(final Short value) {
        return (value == null) ? this.nullNode() : IntNode.valueOf(value);
    }
    
    public NumericNode numberNode(final int v) {
        return IntNode.valueOf(v);
    }
    
    public ValueNode numberNode(final Integer value) {
        return (value == null) ? this.nullNode() : IntNode.valueOf(value);
    }
    
    public NumericNode numberNode(final long v) {
        return LongNode.valueOf(v);
    }
    
    public ValueNode numberNode(final Long value) {
        return (value == null) ? this.nullNode() : LongNode.valueOf(value);
    }
    
    public NumericNode numberNode(final BigInteger v) {
        return BigIntegerNode.valueOf(v);
    }
    
    public NumericNode numberNode(final float v) {
        return DoubleNode.valueOf(v);
    }
    
    public ValueNode numberNode(final Float value) {
        return (value == null) ? this.nullNode() : DoubleNode.valueOf(value);
    }
    
    public NumericNode numberNode(final double v) {
        return DoubleNode.valueOf(v);
    }
    
    public ValueNode numberNode(final Double value) {
        return (value == null) ? this.nullNode() : DoubleNode.valueOf(value);
    }
    
    public NumericNode numberNode(final BigDecimal v) {
        return DecimalNode.valueOf(v);
    }
    
    public TextNode textNode(final String text) {
        return TextNode.valueOf(text);
    }
    
    public BinaryNode binaryNode(final byte[] data) {
        return BinaryNode.valueOf(data);
    }
    
    public BinaryNode binaryNode(final byte[] data, final int offset, final int length) {
        return BinaryNode.valueOf(data, offset, length);
    }
    
    public ArrayNode arrayNode() {
        return new ArrayNode(this);
    }
    
    public ObjectNode objectNode() {
        return new ObjectNode(this);
    }
    
    public POJONode POJONode(final Object pojo) {
        return new POJONode(pojo);
    }
    
    static {
        instance = new JsonNodeFactory();
    }
}
