// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.node;

import java.util.NoSuchElementException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.JsonNode;
import java.util.LinkedHashMap;

public class ObjectNode extends ContainerNode
{
    protected LinkedHashMap<String, JsonNode> _children;
    
    public ObjectNode(final JsonNodeFactory nc) {
        super(nc);
        this._children = null;
    }
    
    @Override
    public JsonToken asToken() {
        return JsonToken.START_OBJECT;
    }
    
    @Override
    public boolean isObject() {
        return true;
    }
    
    @Override
    public int size() {
        return (this._children == null) ? 0 : this._children.size();
    }
    
    @Override
    public Iterator<JsonNode> getElements() {
        return (this._children == null) ? NoNodesIterator.instance() : this._children.values().iterator();
    }
    
    @Override
    public JsonNode get(final int index) {
        return null;
    }
    
    @Override
    public JsonNode get(final String fieldName) {
        if (this._children != null) {
            return this._children.get(fieldName);
        }
        return null;
    }
    
    @Override
    public Iterator<String> getFieldNames() {
        return (this._children == null) ? NoStringsIterator.instance() : this._children.keySet().iterator();
    }
    
    @Override
    public JsonNode path(final int index) {
        return MissingNode.getInstance();
    }
    
    @Override
    public JsonNode path(final String fieldName) {
        if (this._children != null) {
            final JsonNode n = this._children.get(fieldName);
            if (n != null) {
                return n;
            }
        }
        return MissingNode.getInstance();
    }
    
    @Override
    public Iterator<Map.Entry<String, JsonNode>> getFields() {
        if (this._children == null) {
            return NoFieldsIterator.instance;
        }
        return this._children.entrySet().iterator();
    }
    
    @Override
    public ObjectNode with(final String propertyName) {
        if (this._children == null) {
            this._children = new LinkedHashMap<String, JsonNode>();
        }
        else {
            final JsonNode n = this._children.get(propertyName);
            if (n != null) {
                if (n instanceof ObjectNode) {
                    return (ObjectNode)n;
                }
                throw new UnsupportedOperationException("Property '" + propertyName + "' has value that is not of type ObjectNode (but " + n.getClass().getName() + ")");
            }
        }
        final ObjectNode result = this.objectNode();
        this._children.put(propertyName, result);
        return result;
    }
    
    @Override
    public JsonNode findValue(final String fieldName) {
        if (this._children != null) {
            for (final Map.Entry<String, JsonNode> entry : this._children.entrySet()) {
                if (fieldName.equals(entry.getKey())) {
                    return entry.getValue();
                }
                final JsonNode value = entry.getValue().findValue(fieldName);
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
            for (final Map.Entry<String, JsonNode> entry : this._children.entrySet()) {
                if (fieldName.equals(entry.getKey())) {
                    if (foundSoFar == null) {
                        foundSoFar = new ArrayList<JsonNode>();
                    }
                    foundSoFar.add(entry.getValue());
                }
                else {
                    foundSoFar = entry.getValue().findValues(fieldName, foundSoFar);
                }
            }
        }
        return foundSoFar;
    }
    
    @Override
    public List<String> findValuesAsText(final String fieldName, List<String> foundSoFar) {
        if (this._children != null) {
            for (final Map.Entry<String, JsonNode> entry : this._children.entrySet()) {
                if (fieldName.equals(entry.getKey())) {
                    if (foundSoFar == null) {
                        foundSoFar = new ArrayList<String>();
                    }
                    foundSoFar.add(entry.getValue().asText());
                }
                else {
                    foundSoFar = entry.getValue().findValuesAsText(fieldName, foundSoFar);
                }
            }
        }
        return foundSoFar;
    }
    
    @Override
    public ObjectNode findParent(final String fieldName) {
        if (this._children != null) {
            for (final Map.Entry<String, JsonNode> entry : this._children.entrySet()) {
                if (fieldName.equals(entry.getKey())) {
                    return this;
                }
                final JsonNode value = entry.getValue().findParent(fieldName);
                if (value != null) {
                    return (ObjectNode)value;
                }
            }
        }
        return null;
    }
    
    @Override
    public List<JsonNode> findParents(final String fieldName, List<JsonNode> foundSoFar) {
        if (this._children != null) {
            for (final Map.Entry<String, JsonNode> entry : this._children.entrySet()) {
                if (fieldName.equals(entry.getKey())) {
                    if (foundSoFar == null) {
                        foundSoFar = new ArrayList<JsonNode>();
                    }
                    foundSoFar.add(this);
                }
                else {
                    foundSoFar = entry.getValue().findParents(fieldName, foundSoFar);
                }
            }
        }
        return foundSoFar;
    }
    
    @Override
    public final void serialize(final JsonGenerator jg, final SerializerProvider provider) throws IOException, JsonProcessingException {
        jg.writeStartObject();
        if (this._children != null) {
            for (final Map.Entry<String, JsonNode> en : this._children.entrySet()) {
                jg.writeFieldName(en.getKey());
                en.getValue().serialize(jg, provider);
            }
        }
        jg.writeEndObject();
    }
    
    @Override
    public void serializeWithType(final JsonGenerator jg, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException, JsonProcessingException {
        typeSer.writeTypePrefixForObject(this, jg);
        if (this._children != null) {
            for (final Map.Entry<String, JsonNode> en : this._children.entrySet()) {
                jg.writeFieldName(en.getKey());
                en.getValue().serialize(jg, provider);
            }
        }
        typeSer.writeTypeSuffixForObject(this, jg);
    }
    
    public JsonNode put(final String fieldName, JsonNode value) {
        if (value == null) {
            value = this.nullNode();
        }
        return this._put(fieldName, value);
    }
    
    public JsonNode remove(final String fieldName) {
        if (this._children != null) {
            return this._children.remove(fieldName);
        }
        return null;
    }
    
    public ObjectNode remove(final Collection<String> fieldNames) {
        if (this._children != null) {
            for (final String fieldName : fieldNames) {
                this._children.remove(fieldName);
            }
        }
        return this;
    }
    
    @Override
    public ObjectNode removeAll() {
        this._children = null;
        return this;
    }
    
    public JsonNode putAll(final Map<String, JsonNode> properties) {
        if (this._children == null) {
            this._children = new LinkedHashMap<String, JsonNode>(properties);
        }
        else {
            for (final Map.Entry<String, JsonNode> en : properties.entrySet()) {
                JsonNode n = en.getValue();
                if (n == null) {
                    n = this.nullNode();
                }
                this._children.put(en.getKey(), n);
            }
        }
        return this;
    }
    
    public JsonNode putAll(final ObjectNode other) {
        final int len = other.size();
        if (len > 0) {
            if (this._children == null) {
                this._children = new LinkedHashMap<String, JsonNode>(len);
            }
            other.putContentsTo(this._children);
        }
        return this;
    }
    
    public ObjectNode retain(final Collection<String> fieldNames) {
        if (this._children != null) {
            final Iterator<Map.Entry<String, JsonNode>> entries = this._children.entrySet().iterator();
            while (entries.hasNext()) {
                final Map.Entry<String, JsonNode> entry = entries.next();
                if (!fieldNames.contains(entry.getKey())) {
                    entries.remove();
                }
            }
        }
        return this;
    }
    
    public ObjectNode retain(final String... fieldNames) {
        return this.retain(Arrays.asList(fieldNames));
    }
    
    public ArrayNode putArray(final String fieldName) {
        final ArrayNode n = this.arrayNode();
        this._put(fieldName, n);
        return n;
    }
    
    public ObjectNode putObject(final String fieldName) {
        final ObjectNode n = this.objectNode();
        this._put(fieldName, n);
        return n;
    }
    
    public void putPOJO(final String fieldName, final Object pojo) {
        this._put(fieldName, this.POJONode(pojo));
    }
    
    public void putNull(final String fieldName) {
        this._put(fieldName, this.nullNode());
    }
    
    public void put(final String fieldName, final int v) {
        this._put(fieldName, this.numberNode(v));
    }
    
    public void put(final String fieldName, final Integer value) {
        if (value == null) {
            this._put(fieldName, this.nullNode());
        }
        else {
            this._put(fieldName, this.numberNode(value));
        }
    }
    
    public void put(final String fieldName, final long v) {
        this._put(fieldName, this.numberNode(v));
    }
    
    public void put(final String fieldName, final Long value) {
        if (value == null) {
            this._put(fieldName, this.nullNode());
        }
        else {
            this._put(fieldName, this.numberNode(value));
        }
    }
    
    public void put(final String fieldName, final float v) {
        this._put(fieldName, this.numberNode(v));
    }
    
    public void put(final String fieldName, final Float value) {
        if (value == null) {
            this._put(fieldName, this.nullNode());
        }
        else {
            this._put(fieldName, this.numberNode(value));
        }
    }
    
    public void put(final String fieldName, final double v) {
        this._put(fieldName, this.numberNode(v));
    }
    
    public void put(final String fieldName, final Double value) {
        if (value == null) {
            this._put(fieldName, this.nullNode());
        }
        else {
            this._put(fieldName, this.numberNode(value));
        }
    }
    
    public void put(final String fieldName, final BigDecimal v) {
        if (v == null) {
            this.putNull(fieldName);
        }
        else {
            this._put(fieldName, this.numberNode(v));
        }
    }
    
    public void put(final String fieldName, final String v) {
        if (v == null) {
            this.putNull(fieldName);
        }
        else {
            this._put(fieldName, this.textNode(v));
        }
    }
    
    public void put(final String fieldName, final boolean v) {
        this._put(fieldName, this.booleanNode(v));
    }
    
    public void put(final String fieldName, final Boolean value) {
        if (value == null) {
            this._put(fieldName, this.nullNode());
        }
        else {
            this._put(fieldName, this.booleanNode(value));
        }
    }
    
    public void put(final String fieldName, final byte[] v) {
        if (v == null) {
            this._put(fieldName, this.nullNode());
        }
        else {
            this._put(fieldName, this.binaryNode(v));
        }
    }
    
    protected void putContentsTo(final Map<String, JsonNode> dst) {
        if (this._children != null) {
            for (final Map.Entry<String, JsonNode> en : this._children.entrySet()) {
                dst.put(en.getKey(), en.getValue());
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
        final ObjectNode other = (ObjectNode)o;
        if (other.size() != this.size()) {
            return false;
        }
        if (this._children != null) {
            for (final Map.Entry<String, JsonNode> en : this._children.entrySet()) {
                final String key = en.getKey();
                final JsonNode value = en.getValue();
                final JsonNode otherValue = other.get(key);
                if (otherValue == null || !otherValue.equals(value)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        return (this._children == null) ? -1 : this._children.hashCode();
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(32 + (this.size() << 4));
        sb.append("{");
        if (this._children != null) {
            int count = 0;
            for (final Map.Entry<String, JsonNode> en : this._children.entrySet()) {
                if (count > 0) {
                    sb.append(",");
                }
                ++count;
                TextNode.appendQuoted(sb, en.getKey());
                sb.append(':');
                sb.append(en.getValue().toString());
            }
        }
        sb.append("}");
        return sb.toString();
    }
    
    private final JsonNode _put(final String fieldName, final JsonNode value) {
        if (this._children == null) {
            this._children = new LinkedHashMap<String, JsonNode>();
        }
        return this._children.put(fieldName, value);
    }
    
    protected static class NoFieldsIterator implements Iterator<Map.Entry<String, JsonNode>>
    {
        static final NoFieldsIterator instance;
        
        private NoFieldsIterator() {
        }
        
        public boolean hasNext() {
            return false;
        }
        
        public Map.Entry<String, JsonNode> next() {
            throw new NoSuchElementException();
        }
        
        public void remove() {
            throw new IllegalStateException();
        }
        
        static {
            instance = new NoFieldsIterator();
        }
    }
}
