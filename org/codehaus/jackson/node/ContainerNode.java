// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.node;

import java.util.NoSuchElementException;
import java.util.Iterator;
import java.math.BigDecimal;
import java.util.List;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonToken;

public abstract class ContainerNode extends BaseJsonNode
{
    JsonNodeFactory _nodeFactory;
    
    protected ContainerNode(final JsonNodeFactory nc) {
        this._nodeFactory = nc;
    }
    
    @Override
    public boolean isContainerNode() {
        return true;
    }
    
    @Override
    public abstract JsonToken asToken();
    
    @Override
    public String getValueAsText() {
        return null;
    }
    
    @Override
    public String asText() {
        return "";
    }
    
    @Override
    public abstract JsonNode findValue(final String p0);
    
    @Override
    public abstract ObjectNode findParent(final String p0);
    
    @Override
    public abstract List<JsonNode> findValues(final String p0, final List<JsonNode> p1);
    
    @Override
    public abstract List<JsonNode> findParents(final String p0, final List<JsonNode> p1);
    
    @Override
    public abstract List<String> findValuesAsText(final String p0, final List<String> p1);
    
    @Override
    public abstract int size();
    
    @Override
    public abstract JsonNode get(final int p0);
    
    @Override
    public abstract JsonNode get(final String p0);
    
    public final ArrayNode arrayNode() {
        return this._nodeFactory.arrayNode();
    }
    
    public final ObjectNode objectNode() {
        return this._nodeFactory.objectNode();
    }
    
    public final NullNode nullNode() {
        return this._nodeFactory.nullNode();
    }
    
    public final BooleanNode booleanNode(final boolean v) {
        return this._nodeFactory.booleanNode(v);
    }
    
    public final NumericNode numberNode(final byte v) {
        return this._nodeFactory.numberNode(v);
    }
    
    public final NumericNode numberNode(final short v) {
        return this._nodeFactory.numberNode(v);
    }
    
    public final NumericNode numberNode(final int v) {
        return this._nodeFactory.numberNode(v);
    }
    
    public final NumericNode numberNode(final long v) {
        return this._nodeFactory.numberNode(v);
    }
    
    public final NumericNode numberNode(final float v) {
        return this._nodeFactory.numberNode(v);
    }
    
    public final NumericNode numberNode(final double v) {
        return this._nodeFactory.numberNode(v);
    }
    
    public final NumericNode numberNode(final BigDecimal v) {
        return this._nodeFactory.numberNode(v);
    }
    
    public final TextNode textNode(final String text) {
        return this._nodeFactory.textNode(text);
    }
    
    public final BinaryNode binaryNode(final byte[] data) {
        return this._nodeFactory.binaryNode(data);
    }
    
    public final BinaryNode binaryNode(final byte[] data, final int offset, final int length) {
        return this._nodeFactory.binaryNode(data, offset, length);
    }
    
    public final POJONode POJONode(final Object pojo) {
        return this._nodeFactory.POJONode(pojo);
    }
    
    public abstract ContainerNode removeAll();
    
    protected static class NoNodesIterator implements Iterator<JsonNode>
    {
        static final NoNodesIterator instance;
        
        private NoNodesIterator() {
        }
        
        public static NoNodesIterator instance() {
            return NoNodesIterator.instance;
        }
        
        public boolean hasNext() {
            return false;
        }
        
        public JsonNode next() {
            throw new NoSuchElementException();
        }
        
        public void remove() {
            throw new IllegalStateException();
        }
        
        static {
            instance = new NoNodesIterator();
        }
    }
    
    protected static class NoStringsIterator implements Iterator<String>
    {
        static final NoStringsIterator instance;
        
        private NoStringsIterator() {
        }
        
        public static NoStringsIterator instance() {
            return NoStringsIterator.instance;
        }
        
        public boolean hasNext() {
            return false;
        }
        
        public String next() {
            throw new NoSuchElementException();
        }
        
        public void remove() {
            throw new IllegalStateException();
        }
        
        static {
            instance = new NoStringsIterator();
        }
    }
}
