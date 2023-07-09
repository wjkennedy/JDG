// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.util;

import org.codehaus.jackson.Base64Variant;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.codehaus.jackson.JsonStreamContext;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonToken;
import java.io.IOException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.FormatSchema;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.JsonParser;

public class JsonParserDelegate extends JsonParser
{
    protected JsonParser delegate;
    
    public JsonParserDelegate(final JsonParser d) {
        this.delegate = d;
    }
    
    @Override
    public void setCodec(final ObjectCodec c) {
        this.delegate.setCodec(c);
    }
    
    @Override
    public ObjectCodec getCodec() {
        return this.delegate.getCodec();
    }
    
    @Override
    public JsonParser enable(final Feature f) {
        this.delegate.enable(f);
        return this;
    }
    
    @Override
    public JsonParser disable(final Feature f) {
        this.delegate.disable(f);
        return this;
    }
    
    @Override
    public boolean isEnabled(final Feature f) {
        return this.delegate.isEnabled(f);
    }
    
    @Override
    public void setSchema(final FormatSchema schema) {
        this.delegate.setSchema(schema);
    }
    
    @Override
    public boolean canUseSchema(final FormatSchema schema) {
        return this.delegate.canUseSchema(schema);
    }
    
    @Override
    public Version version() {
        return this.delegate.version();
    }
    
    @Override
    public Object getInputSource() {
        return this.delegate.getInputSource();
    }
    
    @Override
    public void close() throws IOException {
        this.delegate.close();
    }
    
    @Override
    public boolean isClosed() {
        return this.delegate.isClosed();
    }
    
    @Override
    public JsonToken getCurrentToken() {
        return this.delegate.getCurrentToken();
    }
    
    @Override
    public boolean hasCurrentToken() {
        return this.delegate.hasCurrentToken();
    }
    
    @Override
    public void clearCurrentToken() {
        this.delegate.clearCurrentToken();
    }
    
    @Override
    public String getCurrentName() throws IOException, JsonParseException {
        return this.delegate.getCurrentName();
    }
    
    @Override
    public JsonLocation getCurrentLocation() {
        return this.delegate.getCurrentLocation();
    }
    
    @Override
    public JsonToken getLastClearedToken() {
        return this.delegate.getLastClearedToken();
    }
    
    @Override
    public JsonStreamContext getParsingContext() {
        return this.delegate.getParsingContext();
    }
    
    @Override
    public String getText() throws IOException, JsonParseException {
        return this.delegate.getText();
    }
    
    @Override
    public char[] getTextCharacters() throws IOException, JsonParseException {
        return this.delegate.getTextCharacters();
    }
    
    @Override
    public int getTextLength() throws IOException, JsonParseException {
        return this.delegate.getTextLength();
    }
    
    @Override
    public int getTextOffset() throws IOException, JsonParseException {
        return this.delegate.getTextOffset();
    }
    
    @Override
    public boolean getBooleanValue() throws IOException, JsonParseException {
        return this.delegate.getBooleanValue();
    }
    
    @Override
    public BigInteger getBigIntegerValue() throws IOException, JsonParseException {
        return this.delegate.getBigIntegerValue();
    }
    
    @Override
    public byte getByteValue() throws IOException, JsonParseException {
        return this.delegate.getByteValue();
    }
    
    @Override
    public short getShortValue() throws IOException, JsonParseException {
        return this.delegate.getShortValue();
    }
    
    @Override
    public BigDecimal getDecimalValue() throws IOException, JsonParseException {
        return this.delegate.getDecimalValue();
    }
    
    @Override
    public double getDoubleValue() throws IOException, JsonParseException {
        return this.delegate.getDoubleValue();
    }
    
    @Override
    public float getFloatValue() throws IOException, JsonParseException {
        return this.delegate.getFloatValue();
    }
    
    @Override
    public int getIntValue() throws IOException, JsonParseException {
        return this.delegate.getIntValue();
    }
    
    @Override
    public long getLongValue() throws IOException, JsonParseException {
        return this.delegate.getLongValue();
    }
    
    @Override
    public NumberType getNumberType() throws IOException, JsonParseException {
        return this.delegate.getNumberType();
    }
    
    @Override
    public Number getNumberValue() throws IOException, JsonParseException {
        return this.delegate.getNumberValue();
    }
    
    @Override
    public byte[] getBinaryValue(final Base64Variant b64variant) throws IOException, JsonParseException {
        return this.delegate.getBinaryValue(b64variant);
    }
    
    @Override
    public Object getEmbeddedObject() throws IOException, JsonParseException {
        return this.delegate.getEmbeddedObject();
    }
    
    @Override
    public JsonLocation getTokenLocation() {
        return this.delegate.getTokenLocation();
    }
    
    @Override
    public JsonToken nextToken() throws IOException, JsonParseException {
        return this.delegate.nextToken();
    }
    
    @Override
    public JsonParser skipChildren() throws IOException, JsonParseException {
        this.delegate.skipChildren();
        return this;
    }
}
