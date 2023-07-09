// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson;

import java.util.Collection;
import java.util.Map;
import java.util.Iterator;
import java.util.Collections;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.io.IOException;
import java.util.List;

public abstract class JsonNode implements Iterable<JsonNode>
{
    protected static final List<JsonNode> NO_NODES;
    protected static final List<String> NO_STRINGS;
    
    protected JsonNode() {
    }
    
    public boolean isValueNode() {
        return false;
    }
    
    public boolean isContainerNode() {
        return false;
    }
    
    public boolean isMissingNode() {
        return false;
    }
    
    public boolean isArray() {
        return false;
    }
    
    public boolean isObject() {
        return false;
    }
    
    public boolean isPojo() {
        return false;
    }
    
    public boolean isNumber() {
        return false;
    }
    
    public boolean isIntegralNumber() {
        return false;
    }
    
    public boolean isFloatingPointNumber() {
        return false;
    }
    
    public boolean isInt() {
        return false;
    }
    
    public boolean isLong() {
        return false;
    }
    
    public boolean isDouble() {
        return false;
    }
    
    public boolean isBigDecimal() {
        return false;
    }
    
    public boolean isBigInteger() {
        return false;
    }
    
    public boolean isTextual() {
        return false;
    }
    
    public boolean isBoolean() {
        return false;
    }
    
    public boolean isNull() {
        return false;
    }
    
    public boolean isBinary() {
        return false;
    }
    
    public abstract JsonToken asToken();
    
    public abstract JsonParser.NumberType getNumberType();
    
    public String getTextValue() {
        return null;
    }
    
    public byte[] getBinaryValue() throws IOException {
        return null;
    }
    
    public boolean getBooleanValue() {
        return false;
    }
    
    public Number getNumberValue() {
        return null;
    }
    
    public int getIntValue() {
        return 0;
    }
    
    public long getLongValue() {
        return 0L;
    }
    
    public double getDoubleValue() {
        return 0.0;
    }
    
    public BigDecimal getDecimalValue() {
        return BigDecimal.ZERO;
    }
    
    public BigInteger getBigIntegerValue() {
        return BigInteger.ZERO;
    }
    
    public JsonNode get(final int index) {
        return null;
    }
    
    public JsonNode get(final String fieldName) {
        return null;
    }
    
    public abstract String asText();
    
    public int asInt() {
        return this.asInt(0);
    }
    
    public int asInt(final int defaultValue) {
        return defaultValue;
    }
    
    public long asLong() {
        return this.asLong(0L);
    }
    
    public long asLong(final long defaultValue) {
        return defaultValue;
    }
    
    public double asDouble() {
        return this.asDouble(0.0);
    }
    
    public double asDouble(final double defaultValue) {
        return defaultValue;
    }
    
    public boolean asBoolean() {
        return this.asBoolean(false);
    }
    
    public boolean asBoolean(final boolean defaultValue) {
        return defaultValue;
    }
    
    @Deprecated
    public String getValueAsText() {
        return this.asText();
    }
    
    @Deprecated
    public int getValueAsInt() {
        return this.asInt(0);
    }
    
    @Deprecated
    public int getValueAsInt(final int defaultValue) {
        return this.asInt(defaultValue);
    }
    
    @Deprecated
    public long getValueAsLong() {
        return this.asLong(0L);
    }
    
    @Deprecated
    public long getValueAsLong(final long defaultValue) {
        return this.asLong(defaultValue);
    }
    
    @Deprecated
    public double getValueAsDouble() {
        return this.asDouble(0.0);
    }
    
    @Deprecated
    public double getValueAsDouble(final double defaultValue) {
        return this.asDouble(defaultValue);
    }
    
    @Deprecated
    public boolean getValueAsBoolean() {
        return this.asBoolean(false);
    }
    
    @Deprecated
    public boolean getValueAsBoolean(final boolean defaultValue) {
        return this.asBoolean(defaultValue);
    }
    
    public boolean has(final String fieldName) {
        return this.get(fieldName) != null;
    }
    
    public boolean has(final int index) {
        return this.get(index) != null;
    }
    
    public abstract JsonNode findValue(final String p0);
    
    public final List<JsonNode> findValues(final String fieldName) {
        final List<JsonNode> result = this.findValues(fieldName, null);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }
    
    public final List<String> findValuesAsText(final String fieldName) {
        final List<String> result = this.findValuesAsText(fieldName, null);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }
    
    public abstract JsonNode findPath(final String p0);
    
    public abstract JsonNode findParent(final String p0);
    
    public final List<JsonNode> findParents(final String fieldName) {
        final List<JsonNode> result = this.findParents(fieldName, null);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }
    
    public abstract List<JsonNode> findValues(final String p0, final List<JsonNode> p1);
    
    public abstract List<String> findValuesAsText(final String p0, final List<String> p1);
    
    public abstract List<JsonNode> findParents(final String p0, final List<JsonNode> p1);
    
    public int size() {
        return 0;
    }
    
    public final Iterator<JsonNode> iterator() {
        return this.getElements();
    }
    
    public Iterator<JsonNode> getElements() {
        return JsonNode.NO_NODES.iterator();
    }
    
    public Iterator<String> getFieldNames() {
        return JsonNode.NO_STRINGS.iterator();
    }
    
    public Iterator<Map.Entry<String, JsonNode>> getFields() {
        final Collection<Map.Entry<String, JsonNode>> coll = (Collection<Map.Entry<String, JsonNode>>)Collections.emptyList();
        return coll.iterator();
    }
    
    public abstract JsonNode path(final String p0);
    
    @Deprecated
    public final JsonNode getPath(final String fieldName) {
        return this.path(fieldName);
    }
    
    public abstract JsonNode path(final int p0);
    
    @Deprecated
    public final JsonNode getPath(final int index) {
        return this.path(index);
    }
    
    public JsonNode with(final String propertyName) {
        throw new UnsupportedOperationException("JsonNode not of type ObjectNode (but " + this.getClass().getName() + "), can not call with() on it");
    }
    
    public abstract JsonParser traverse();
    
    @Override
    public abstract String toString();
    
    @Override
    public abstract boolean equals(final Object p0);
    
    static {
        NO_NODES = Collections.emptyList();
        NO_STRINGS = Collections.emptyList();
    }
}
