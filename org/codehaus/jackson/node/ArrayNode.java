// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.node;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import java.util.Iterator;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.JsonNode;
import java.util.ArrayList;

public final class ArrayNode extends ContainerNode
{
    protected ArrayList<JsonNode> _children;
    
    public ArrayNode(final JsonNodeFactory nc) {
        super(nc);
    }
    
    @Override
    public JsonToken asToken() {
        return JsonToken.START_ARRAY;
    }
    
    @Override
    public boolean isArray() {
        return true;
    }
    
    @Override
    public int size() {
        return (this._children == null) ? 0 : this._children.size();
    }
    
    @Override
    public Iterator<JsonNode> getElements() {
        return (this._children == null) ? NoNodesIterator.instance() : this._children.iterator();
    }
    
    @Override
    public JsonNode get(final int index) {
        if (index >= 0 && this._children != null && index < this._children.size()) {
            return this._children.get(index);
        }
        return null;
    }
    
    @Override
    public JsonNode get(final String fieldName) {
        return null;
    }
    
    @Override
    public JsonNode path(final String fieldName) {
        return MissingNode.getInstance();
    }
    
    @Override
    public JsonNode path(final int index) {
        if (index >= 0 && this._children != null && index < this._children.size()) {
            return this._children.get(index);
        }
        return MissingNode.getInstance();
    }
    
    @Override
    public final void serialize(final JsonGenerator jg, final SerializerProvider provider) throws IOException, JsonProcessingException {
        jg.writeStartArray();
        if (this._children != null) {
            for (final JsonNode n : this._children) {
                ((BaseJsonNode)n).serialize(jg, provider);
            }
        }
        jg.writeEndArray();
    }
    
    @Override
    public void serializeWithType(final JsonGenerator jg, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException, JsonProcessingException {
        typeSer.writeTypePrefixForArray(this, jg);
        if (this._children != null) {
            for (final JsonNode n : this._children) {
                ((BaseJsonNode)n).serialize(jg, provider);
            }
        }
        typeSer.writeTypeSuffixForArray(this, jg);
    }
    
    @Override
    public JsonNode findValue(final String fieldName) {
        if (this._children != null) {
            for (final JsonNode node : this._children) {
                final JsonNode value = node.findValue(fieldName);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }
    
    @Override
    public List<JsonNode> findValues(final String fieldName, List<JsonNode> foundSoFar) {
        if (this._children != null) {
            for (final JsonNode node : this._children) {
                foundSoFar = node.findValues(fieldName, foundSoFar);
            }
        }
        return foundSoFar;
    }
    
    @Override
    public List<String> findValuesAsText(final String fieldName, List<String> foundSoFar) {
        if (this._children != null) {
            for (final JsonNode node : this._children) {
                foundSoFar = node.findValuesAsText(fieldName, foundSoFar);
            }
        }
        return foundSoFar;
    }
    
    @Override
    public ObjectNode findParent(final String fieldName) {
        if (this._children != null) {
            for (final JsonNode node : this._children) {
                final JsonNode parent = node.findParent(fieldName);
                if (parent != null) {
                    return (ObjectNode)parent;
                }
            }
        }
        return null;
    }
    
    @Override
    public List<JsonNode> findParents(final String fieldName, List<JsonNode> foundSoFar) {
        if (this._children != null) {
            for (final JsonNode node : this._children) {
                foundSoFar = node.findParents(fieldName, foundSoFar);
            }
        }
        return foundSoFar;
    }
    
    public JsonNode set(final int index, JsonNode value) {
        if (value == null) {
            value = this.nullNode();
        }
        return this._set(index, value);
    }
    
    public void add(JsonNode value) {
        if (value == null) {
            value = this.nullNode();
        }
        this._add(value);
    }
    
    public JsonNode addAll(final ArrayNode other) {
        final int len = other.size();
        if (len > 0) {
            if (this._children == null) {
                this._children = new ArrayList<JsonNode>(len + 2);
            }
            other.addContentsTo(this._children);
        }
        return this;
    }
    
    public JsonNode addAll(final Collection<JsonNode> nodes) {
        final int len = nodes.size();
        if (len > 0) {
            if (this._children == null) {
                this._children = new ArrayList<JsonNode>(nodes);
            }
            else {
                this._children.addAll(nodes);
            }
        }
        return this;
    }
    
    public void insert(final int index, JsonNode value) {
        if (value == null) {
            value = this.nullNode();
        }
        this._insert(index, value);
    }
    
    public JsonNode remove(final int index) {
        if (index >= 0 && this._children != null && index < this._children.size()) {
            return this._children.remove(index);
        }
        return null;
    }
    
    @Override
    public ArrayNode removeAll() {
        this._children = null;
        return this;
    }
    
    public ArrayNode addArray() {
        final ArrayNode n = this.arrayNode();
        this._add(n);
        return n;
    }
    
    public ObjectNode addObject() {
        final ObjectNode n = this.objectNode();
        this._add(n);
        return n;
    }
    
    public void addPOJO(final Object value) {
        if (value == null) {
            this.addNull();
        }
        else {
            this._add(this.POJONode(value));
        }
    }
    
    public void addNull() {
        this._add(this.nullNode());
    }
    
    public void add(final int v) {
        this._add(this.numberNode(v));
    }
    
    public void add(final Integer value) {
        if (value == null) {
            this.addNull();
        }
        else {
            this._add(this.numberNode(value));
        }
    }
    
    public void add(final long v) {
        this._add(this.numberNode(v));
    }
    
    public void add(final Long value) {
        if (value == null) {
            this.addNull();
        }
        else {
            this._add(this.numberNode(value));
        }
    }
    
    public void add(final float v) {
        this._add(this.numberNode(v));
    }
    
    public void add(final Float value) {
        if (value == null) {
            this.addNull();
        }
        else {
            this._add(this.numberNode(value));
        }
    }
    
    public void add(final double v) {
        this._add(this.numberNode(v));
    }
    
    public void add(final Double value) {
        if (value == null) {
            this.addNull();
        }
        else {
            this._add(this.numberNode(value));
        }
    }
    
    public void add(final BigDecimal v) {
        if (v == null) {
            this.addNull();
        }
        else {
            this._add(this.numberNode(v));
        }
    }
    
    public void add(final String v) {
        if (v == null) {
            this.addNull();
        }
        else {
            this._add(this.textNode(v));
        }
    }
    
    public void add(final boolean v) {
        this._add(this.booleanNode(v));
    }
    
    public void add(final Boolean value) {
        if (value == null) {
            this.addNull();
        }
        else {
            this._add(this.booleanNode(value));
        }
    }
    
    public void add(final byte[] v) {
        if (v == null) {
            this.addNull();
        }
        else {
            this._add(this.binaryNode(v));
        }
    }
    
    public ArrayNode insertArray(final int index) {
        final ArrayNode n = this.arrayNode();
        this._insert(index, n);
        return n;
    }
    
    public ObjectNode insertObject(final int index) {
        final ObjectNode n = this.objectNode();
        this._insert(index, n);
        return n;
    }
    
    public void insertPOJO(final int index, final Object value) {
        if (value == null) {
            this.insertNull(index);
        }
        else {
            this._insert(index, this.POJONode(value));
        }
    }
    
    public void insertNull(final int index) {
        this._insert(index, this.nullNode());
    }
    
    public void insert(final int index, final int v) {
        this._insert(index, this.numberNode(v));
    }
    
    public void insert(final int index, final Integer value) {
        if (value == null) {
            this.insertNull(index);
        }
        else {
            this._insert(index, this.numberNode(value));
        }
    }
    
    public void insert(final int index, final long v) {
        this._insert(index, this.numberNode(v));
    }
    
    public void insert(final int index, final Long value) {
        if (value == null) {
            this.insertNull(index);
        }
        else {
            this._insert(index, this.numberNode(value));
        }
    }
    
    public void insert(final int index, final float v) {
        this._insert(index, this.numberNode(v));
    }
    
    public void insert(final int index, final Float value) {
        if (value == null) {
            this.insertNull(index);
        }
        else {
            this._insert(index, this.numberNode(value));
        }
    }
    
    public void insert(final int index, final double v) {
        this._insert(index, this.numberNode(v));
    }
    
    public void insert(final int index, final Double value) {
        if (value == null) {
            this.insertNull(index);
        }
        else {
            this._insert(index, this.numberNode(value));
        }
    }
    
    public void insert(final int index, final BigDecimal v) {
        if (v == null) {
            this.insertNull(index);
        }
        else {
            this._insert(index, this.numberNode(v));
        }
    }
    
    public void insert(final int index, final String v) {
        if (v == null) {
            this.insertNull(index);
        }
        else {
            this._insert(index, this.textNode(v));
        }
    }
    
    public void insert(final int index, final boolean v) {
        this._insert(index, this.booleanNode(v));
    }
    
    public void insert(final int index, final Boolean value) {
        if (value == null) {
            this.insertNull(index);
        }
        else {
            this._insert(index, this.booleanNode(value));
        }
    }
    
    public void insert(final int index, final byte[] v) {
        if (v == null) {
            this.insertNull(index);
        }
        else {
            this._insert(index, this.binaryNode(v));
        }
    }
    
    protected void addContentsTo(final List<JsonNode> dst) {
        if (this._children != null) {
            for (final JsonNode n : this._children) {
                dst.add(n);
            }
        }
    }
    
    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o.getClass() != this.getClass()) {
            return false;
        }
        final ArrayNode other = (ArrayNode)o;
        if (this._children == null || this._children.size() == 0) {
            return other.size() == 0;
        }
        return other._sameChildren(this._children);
    }
    
    @Override
    public int hashCode() {
        int hash;
        if (this._children == null) {
            hash = 1;
        }
        else {
            hash = this._children.size();
            for (final JsonNode n : this._children) {
                if (n != null) {
                    hash ^= n.hashCode();
                }
            }
        }
        return hash;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(16 + (this.size() << 4));
        sb.append('[');
        if (this._children != null) {
            for (int i = 0, len = this._children.size(); i < len; ++i) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(this._children.get(i).toString());
            }
        }
        sb.append(']');
        return sb.toString();
    }
    
    public JsonNode _set(final int index, final JsonNode value) {
        if (this._children == null || index < 0 || index >= this._children.size()) {
            throw new IndexOutOfBoundsException("Illegal index " + index + ", array size " + this.size());
        }
        return this._children.set(index, value);
    }
    
    private void _add(final JsonNode node) {
        if (this._children == null) {
            this._children = new ArrayList<JsonNode>();
        }
        this._children.add(node);
    }
    
    private void _insert(final int index, final JsonNode node) {
        if (this._children == null) {
            (this._children = new ArrayList<JsonNode>()).add(node);
            return;
        }
        if (index < 0) {
            this._children.add(0, node);
        }
        else if (index >= this._children.size()) {
            this._children.add(node);
        }
        else {
            this._children.add(index, node);
        }
    }
    
    private boolean _sameChildren(final ArrayList<JsonNode> otherChildren) {
        final int len = otherChildren.size();
        if (this.size() != len) {
            return false;
        }
        for (int i = 0; i < len; ++i) {
            if (!this._children.get(i).equals(otherChildren.get(i))) {
                return false;
            }
        }
        return true;
    }
}
