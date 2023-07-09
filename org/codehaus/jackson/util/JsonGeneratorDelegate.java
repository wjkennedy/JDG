// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.util;

import org.codehaus.jackson.JsonNode;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.codehaus.jackson.SerializableString;
import org.codehaus.jackson.io.SerializedString;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.Base64Variant;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.FormatSchema;
import org.codehaus.jackson.JsonStreamContext;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonParser;
import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;

public class JsonGeneratorDelegate extends JsonGenerator
{
    protected JsonGenerator delegate;
    
    public JsonGeneratorDelegate(final JsonGenerator d) {
        this.delegate = d;
    }
    
    @Override
    public void close() throws IOException {
        this.delegate.close();
    }
    
    @Override
    public void copyCurrentEvent(final JsonParser jp) throws IOException, JsonProcessingException {
        this.delegate.copyCurrentEvent(jp);
    }
    
    @Override
    public void copyCurrentStructure(final JsonParser jp) throws IOException, JsonProcessingException {
        this.delegate.copyCurrentStructure(jp);
    }
    
    @Override
    public JsonGenerator disable(final Feature f) {
        return this.delegate.disable(f);
    }
    
    @Override
    public JsonGenerator enable(final Feature f) {
        return this.delegate.enable(f);
    }
    
    @Override
    public void flush() throws IOException {
        this.delegate.flush();
    }
    
    @Override
    public ObjectCodec getCodec() {
        return this.delegate.getCodec();
    }
    
    @Override
    public JsonStreamContext getOutputContext() {
        return this.delegate.getOutputContext();
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
    public Object getOutputTarget() {
        return this.delegate.getOutputTarget();
    }
    
    @Override
    public boolean isClosed() {
        return this.delegate.isClosed();
    }
    
    @Override
    public boolean isEnabled(final Feature f) {
        return this.delegate.isEnabled(f);
    }
    
    @Override
    public JsonGenerator setCodec(final ObjectCodec oc) {
        this.delegate.setCodec(oc);
        return this;
    }
    
    @Override
    public JsonGenerator useDefaultPrettyPrinter() {
        this.delegate.useDefaultPrettyPrinter();
        return this;
    }
    
    @Override
    public void writeBinary(final Base64Variant b64variant, final byte[] data, final int offset, final int len) throws IOException, JsonGenerationException {
        this.delegate.writeBinary(b64variant, data, offset, len);
    }
    
    @Override
    public void writeBoolean(final boolean state) throws IOException, JsonGenerationException {
        this.delegate.writeBoolean(state);
    }
    
    @Override
    public void writeEndArray() throws IOException, JsonGenerationException {
        this.delegate.writeEndArray();
    }
    
    @Override
    public void writeEndObject() throws IOException, JsonGenerationException {
        this.delegate.writeEndObject();
    }
    
    @Override
    public void writeFieldName(final String name) throws IOException, JsonGenerationException {
        this.delegate.writeFieldName(name);
    }
    
    @Override
    public void writeFieldName(final SerializedString name) throws IOException, JsonGenerationException {
        this.delegate.writeFieldName(name);
    }
    
    @Override
    public void writeFieldName(final SerializableString name) throws IOException, JsonGenerationException {
        this.delegate.writeFieldName(name);
    }
    
    @Override
    public void writeNull() throws IOException, JsonGenerationException {
        this.delegate.writeNull();
    }
    
    @Override
    public void writeNumber(final int v) throws IOException, JsonGenerationException {
        this.delegate.writeNumber(v);
    }
    
    @Override
    public void writeNumber(final long v) throws IOException, JsonGenerationException {
        this.delegate.writeNumber(v);
    }
    
    @Override
    public void writeNumber(final BigInteger v) throws IOException, JsonGenerationException {
        this.delegate.writeNumber(v);
    }
    
    @Override
    public void writeNumber(final double v) throws IOException, JsonGenerationException {
        this.delegate.writeNumber(v);
    }
    
    @Override
    public void writeNumber(final float v) throws IOException, JsonGenerationException {
        this.delegate.writeNumber(v);
    }
    
    @Override
    public void writeNumber(final BigDecimal v) throws IOException, JsonGenerationException {
        this.delegate.writeNumber(v);
    }
    
    @Override
    public void writeNumber(final String encodedValue) throws IOException, JsonGenerationException, UnsupportedOperationException {
        this.delegate.writeNumber(encodedValue);
    }
    
    @Override
    public void writeObject(final Object pojo) throws IOException, JsonProcessingException {
        this.delegate.writeObject(pojo);
    }
    
    @Override
    public void writeRaw(final String text) throws IOException, JsonGenerationException {
        this.delegate.writeRaw(text);
    }
    
    @Override
    public void writeRaw(final String text, final int offset, final int len) throws IOException, JsonGenerationException {
        this.delegate.writeRaw(text, offset, len);
    }
    
    @Override
    public void writeRaw(final char[] text, final int offset, final int len) throws IOException, JsonGenerationException {
        this.delegate.writeRaw(text, offset, len);
    }
    
    @Override
    public void writeRaw(final char c) throws IOException, JsonGenerationException {
        this.delegate.writeRaw(c);
    }
    
    @Override
    public void writeRawValue(final String text) throws IOException, JsonGenerationException {
        this.delegate.writeRawValue(text);
    }
    
    @Override
    public void writeRawValue(final String text, final int offset, final int len) throws IOException, JsonGenerationException {
        this.delegate.writeRawValue(text, offset, len);
    }
    
    @Override
    public void writeRawValue(final char[] text, final int offset, final int len) throws IOException, JsonGenerationException {
        this.delegate.writeRawValue(text, offset, len);
    }
    
    @Override
    public void writeStartArray() throws IOException, JsonGenerationException {
        this.delegate.writeStartArray();
    }
    
    @Override
    public void writeStartObject() throws IOException, JsonGenerationException {
        this.delegate.writeStartObject();
    }
    
    @Override
    public void writeString(final String text) throws IOException, JsonGenerationException {
        this.delegate.writeString(text);
    }
    
    @Override
    public void writeString(final char[] text, final int offset, final int len) throws IOException, JsonGenerationException {
        this.delegate.writeString(text, offset, len);
    }
    
    @Override
    public void writeString(final SerializableString text) throws IOException, JsonGenerationException {
        this.delegate.writeString(text);
    }
    
    @Override
    public void writeRawUTF8String(final byte[] text, final int offset, final int length) throws IOException, JsonGenerationException {
        this.delegate.writeRawUTF8String(text, offset, length);
    }
    
    @Override
    public void writeUTF8String(final byte[] text, final int offset, final int length) throws IOException, JsonGenerationException {
        this.delegate.writeUTF8String(text, offset, length);
    }
    
    @Override
    public void writeTree(final JsonNode rootNode) throws IOException, JsonProcessingException {
        this.delegate.writeTree(rootNode);
    }
}
