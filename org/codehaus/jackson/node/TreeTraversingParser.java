// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.node;

import org.codehaus.jackson.Base64Variant;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonStreamContext;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonParseException;
import java.io.IOException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.impl.JsonParserMinimalBase;

public class TreeTraversingParser extends JsonParserMinimalBase
{
    protected ObjectCodec _objectCodec;
    protected NodeCursor _nodeCursor;
    protected JsonToken _nextToken;
    protected boolean _startContainer;
    protected boolean _closed;
    
    public TreeTraversingParser(final JsonNode n) {
        this(n, null);
    }
    
    public TreeTraversingParser(final JsonNode n, final ObjectCodec codec) {
        super(0);
        this._objectCodec = codec;
        if (n.isArray()) {
            this._nextToken = JsonToken.START_ARRAY;
            this._nodeCursor = new NodeCursor.Array(n, null);
        }
        else if (n.isObject()) {
            this._nextToken = JsonToken.START_OBJECT;
            this._nodeCursor = new NodeCursor.Object(n, null);
        }
        else {
            this._nodeCursor = new NodeCursor.RootValue(n, null);
        }
    }
    
    @Override
    public void setCodec(final ObjectCodec c) {
        this._objectCodec = c;
    }
    
    @Override
    public ObjectCodec getCodec() {
        return this._objectCodec;
    }
    
    @Override
    public void close() throws IOException {
        if (!this._closed) {
            this._closed = true;
            this._nodeCursor = null;
            this._currToken = null;
        }
    }
    
    @Override
    public JsonToken nextToken() throws IOException, JsonParseException {
        if (this._nextToken != null) {
            this._currToken = this._nextToken;
            this._nextToken = null;
            return this._currToken;
        }
        if (this._startContainer) {
            this._startContainer = false;
            if (!this._nodeCursor.currentHasChildren()) {
                return this._currToken = ((this._currToken == JsonToken.START_OBJECT) ? JsonToken.END_OBJECT : JsonToken.END_ARRAY);
            }
            this._nodeCursor = this._nodeCursor.iterateChildren();
            this._currToken = this._nodeCursor.nextToken();
            if (this._currToken == JsonToken.START_OBJECT || this._currToken == JsonToken.START_ARRAY) {
                this._startContainer = true;
            }
            return this._currToken;
        }
        else {
            if (this._nodeCursor == null) {
                this._closed = true;
                return null;
            }
            this._currToken = this._nodeCursor.nextToken();
            if (this._currToken != null) {
                if (this._currToken == JsonToken.START_OBJECT || this._currToken == JsonToken.START_ARRAY) {
                    this._startContainer = true;
                }
                return this._currToken;
            }
            this._currToken = this._nodeCursor.endToken();
            this._nodeCursor = this._nodeCursor.getParent();
            return this._currToken;
        }
    }
    
    @Override
    public JsonParser skipChildren() throws IOException, JsonParseException {
        if (this._currToken == JsonToken.START_OBJECT) {
            this._startContainer = false;
            this._currToken = JsonToken.END_OBJECT;
        }
        else if (this._currToken == JsonToken.START_ARRAY) {
            this._startContainer = false;
            this._currToken = JsonToken.END_ARRAY;
        }
        return this;
    }
    
    @Override
    public boolean isClosed() {
        return this._closed;
    }
    
    @Override
    public String getCurrentName() {
        return (this._nodeCursor == null) ? null : this._nodeCursor.getCurrentName();
    }
    
    @Override
    public JsonStreamContext getParsingContext() {
        return this._nodeCursor;
    }
    
    @Override
    public JsonLocation getTokenLocation() {
        return JsonLocation.NA;
    }
    
    @Override
    public JsonLocation getCurrentLocation() {
        return JsonLocation.NA;
    }
    
    @Override
    public String getText() {
        if (this._closed) {
            return null;
        }
        switch (this._currToken) {
            case FIELD_NAME: {
                return this._nodeCursor.getCurrentName();
            }
            case VALUE_STRING: {
                return this.currentNode().getTextValue();
            }
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT: {
                return String.valueOf(this.currentNode().getNumberValue());
            }
            case VALUE_EMBEDDED_OBJECT: {
                final JsonNode n = this.currentNode();
                if (n != null && n.isBinary()) {
                    return n.asText();
                }
                break;
            }
        }
        return (this._currToken == null) ? null : this._currToken.asString();
    }
    
    @Override
    public char[] getTextCharacters() throws IOException, JsonParseException {
        return this.getText().toCharArray();
    }
    
    @Override
    public int getTextLength() throws IOException, JsonParseException {
        return this.getText().length();
    }
    
    @Override
    public int getTextOffset() throws IOException, JsonParseException {
        return 0;
    }
    
    @Override
    public boolean hasTextCharacters() {
        return false;
    }
    
    @Override
    public NumberType getNumberType() throws IOException, JsonParseException {
        final JsonNode n = this.currentNumericNode();
        return (n == null) ? null : n.getNumberType();
    }
    
    @Override
    public BigInteger getBigIntegerValue() throws IOException, JsonParseException {
        return this.currentNumericNode().getBigIntegerValue();
    }
    
    @Override
    public BigDecimal getDecimalValue() throws IOException, JsonParseException {
        return this.currentNumericNode().getDecimalValue();
    }
    
    @Override
    public double getDoubleValue() throws IOException, JsonParseException {
        return this.currentNumericNode().getDoubleValue();
    }
    
    @Override
    public float getFloatValue() throws IOException, JsonParseException {
        return (float)this.currentNumericNode().getDoubleValue();
    }
    
    @Override
    public long getLongValue() throws IOException, JsonParseException {
        return this.currentNumericNode().getLongValue();
    }
    
    @Override
    public int getIntValue() throws IOException, JsonParseException {
        return this.currentNumericNode().getIntValue();
    }
    
    @Override
    public Number getNumberValue() throws IOException, JsonParseException {
        return this.currentNumericNode().getNumberValue();
    }
    
    @Override
    public Object getEmbeddedObject() {
        if (!this._closed) {
            final JsonNode n = this.currentNode();
            if (n != null) {
                if (n.isPojo()) {
                    return ((POJONode)n).getPojo();
                }
                if (n.isBinary()) {
                    return ((BinaryNode)n).getBinaryValue();
                }
            }
        }
        return null;
    }
    
    @Override
    public byte[] getBinaryValue(final Base64Variant b64variant) throws IOException, JsonParseException {
        final JsonNode n = this.currentNode();
        if (n != null) {
            final byte[] data = n.getBinaryValue();
            if (data != null) {
                return data;
            }
            if (n.isPojo()) {
                final Object ob = ((POJONode)n).getPojo();
                if (ob instanceof byte[]) {
                    return (byte[])ob;
                }
            }
        }
        return null;
    }
    
    protected JsonNode currentNode() {
        if (this._closed || this._nodeCursor == null) {
            return null;
        }
        return this._nodeCursor.currentNode();
    }
    
    protected JsonNode currentNumericNode() throws JsonParseException {
        final JsonNode n = this.currentNode();
        if (n == null || !n.isNumber()) {
            final JsonToken t = (n == null) ? null : n.asToken();
            throw this._constructError("Current token (" + t + ") not numeric, can not use numeric value accessors");
        }
        return n;
    }
    
    @Override
    protected void _handleEOF() throws JsonParseException {
        this._throwInternal();
    }
}
