// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.codehaus.jackson.io.SerializedString;
import java.io.IOException;
import org.codehaus.jackson.io.CharacterEscapes;
import java.io.Closeable;

public abstract class JsonGenerator implements Closeable, Versioned
{
    protected PrettyPrinter _cfgPrettyPrinter;
    
    protected JsonGenerator() {
    }
    
    public void setSchema(final FormatSchema schema) {
        throw new UnsupportedOperationException("Generator of type " + this.getClass().getName() + " does not support schema of type '" + schema.getSchemaType() + "'");
    }
    
    public boolean canUseSchema(final FormatSchema schema) {
        return false;
    }
    
    public Version version() {
        return Version.unknownVersion();
    }
    
    public Object getOutputTarget() {
        return null;
    }
    
    public abstract JsonGenerator enable(final Feature p0);
    
    public abstract JsonGenerator disable(final Feature p0);
    
    public JsonGenerator configure(final Feature f, final boolean state) {
        if (state) {
            this.enable(f);
        }
        else {
            this.disable(f);
        }
        return this;
    }
    
    public abstract boolean isEnabled(final Feature p0);
    
    public abstract JsonGenerator setCodec(final ObjectCodec p0);
    
    public abstract ObjectCodec getCodec();
    
    @Deprecated
    public void enableFeature(final Feature f) {
        this.enable(f);
    }
    
    @Deprecated
    public void disableFeature(final Feature f) {
        this.disable(f);
    }
    
    @Deprecated
    public void setFeature(final Feature f, final boolean state) {
        this.configure(f, state);
    }
    
    @Deprecated
    public boolean isFeatureEnabled(final Feature f) {
        return this.isEnabled(f);
    }
    
    public JsonGenerator setPrettyPrinter(final PrettyPrinter pp) {
        this._cfgPrettyPrinter = pp;
        return this;
    }
    
    public abstract JsonGenerator useDefaultPrettyPrinter();
    
    public JsonGenerator setHighestNonEscapedChar(final int charCode) {
        return this;
    }
    
    public int getHighestEscapedChar() {
        return 0;
    }
    
    public CharacterEscapes getCharacterEscapes() {
        return null;
    }
    
    public JsonGenerator setCharacterEscapes(final CharacterEscapes esc) {
        return this;
    }
    
    public abstract void writeStartArray() throws IOException, JsonGenerationException;
    
    public abstract void writeEndArray() throws IOException, JsonGenerationException;
    
    public abstract void writeStartObject() throws IOException, JsonGenerationException;
    
    public abstract void writeEndObject() throws IOException, JsonGenerationException;
    
    public abstract void writeFieldName(final String p0) throws IOException, JsonGenerationException;
    
    public void writeFieldName(final SerializedString name) throws IOException, JsonGenerationException {
        this.writeFieldName(name.getValue());
    }
    
    public void writeFieldName(final SerializableString name) throws IOException, JsonGenerationException {
        this.writeFieldName(name.getValue());
    }
    
    public abstract void writeString(final String p0) throws IOException, JsonGenerationException;
    
    public abstract void writeString(final char[] p0, final int p1, final int p2) throws IOException, JsonGenerationException;
    
    public void writeString(final SerializableString text) throws IOException, JsonGenerationException {
        this.writeString(text.getValue());
    }
    
    public abstract void writeRawUTF8String(final byte[] p0, final int p1, final int p2) throws IOException, JsonGenerationException;
    
    public abstract void writeUTF8String(final byte[] p0, final int p1, final int p2) throws IOException, JsonGenerationException;
    
    public abstract void writeRaw(final String p0) throws IOException, JsonGenerationException;
    
    public abstract void writeRaw(final String p0, final int p1, final int p2) throws IOException, JsonGenerationException;
    
    public abstract void writeRaw(final char[] p0, final int p1, final int p2) throws IOException, JsonGenerationException;
    
    public abstract void writeRaw(final char p0) throws IOException, JsonGenerationException;
    
    public abstract void writeRawValue(final String p0) throws IOException, JsonGenerationException;
    
    public abstract void writeRawValue(final String p0, final int p1, final int p2) throws IOException, JsonGenerationException;
    
    public abstract void writeRawValue(final char[] p0, final int p1, final int p2) throws IOException, JsonGenerationException;
    
    public abstract void writeBinary(final Base64Variant p0, final byte[] p1, final int p2, final int p3) throws IOException, JsonGenerationException;
    
    public void writeBinary(final byte[] data, final int offset, final int len) throws IOException, JsonGenerationException {
        this.writeBinary(Base64Variants.getDefaultVariant(), data, offset, len);
    }
    
    public void writeBinary(final byte[] data) throws IOException, JsonGenerationException {
        this.writeBinary(Base64Variants.getDefaultVariant(), data, 0, data.length);
    }
    
    public abstract void writeNumber(final int p0) throws IOException, JsonGenerationException;
    
    public abstract void writeNumber(final long p0) throws IOException, JsonGenerationException;
    
    public abstract void writeNumber(final BigInteger p0) throws IOException, JsonGenerationException;
    
    public abstract void writeNumber(final double p0) throws IOException, JsonGenerationException;
    
    public abstract void writeNumber(final float p0) throws IOException, JsonGenerationException;
    
    public abstract void writeNumber(final BigDecimal p0) throws IOException, JsonGenerationException;
    
    public abstract void writeNumber(final String p0) throws IOException, JsonGenerationException, UnsupportedOperationException;
    
    public abstract void writeBoolean(final boolean p0) throws IOException, JsonGenerationException;
    
    public abstract void writeNull() throws IOException, JsonGenerationException;
    
    public abstract void writeObject(final Object p0) throws IOException, JsonProcessingException;
    
    public abstract void writeTree(final JsonNode p0) throws IOException, JsonProcessingException;
    
    public void writeStringField(final String fieldName, final String value) throws IOException, JsonGenerationException {
        this.writeFieldName(fieldName);
        this.writeString(value);
    }
    
    public final void writeBooleanField(final String fieldName, final boolean value) throws IOException, JsonGenerationException {
        this.writeFieldName(fieldName);
        this.writeBoolean(value);
    }
    
    public final void writeNullField(final String fieldName) throws IOException, JsonGenerationException {
        this.writeFieldName(fieldName);
        this.writeNull();
    }
    
    public final void writeNumberField(final String fieldName, final int value) throws IOException, JsonGenerationException {
        this.writeFieldName(fieldName);
        this.writeNumber(value);
    }
    
    public final void writeNumberField(final String fieldName, final long value) throws IOException, JsonGenerationException {
        this.writeFieldName(fieldName);
        this.writeNumber(value);
    }
    
    public final void writeNumberField(final String fieldName, final double value) throws IOException, JsonGenerationException {
        this.writeFieldName(fieldName);
        this.writeNumber(value);
    }
    
    public final void writeNumberField(final String fieldName, final float value) throws IOException, JsonGenerationException {
        this.writeFieldName(fieldName);
        this.writeNumber(value);
    }
    
    public final void writeNumberField(final String fieldName, final BigDecimal value) throws IOException, JsonGenerationException {
        this.writeFieldName(fieldName);
        this.writeNumber(value);
    }
    
    public final void writeBinaryField(final String fieldName, final byte[] data) throws IOException, JsonGenerationException {
        this.writeFieldName(fieldName);
        this.writeBinary(data);
    }
    
    public final void writeArrayFieldStart(final String fieldName) throws IOException, JsonGenerationException {
        this.writeFieldName(fieldName);
        this.writeStartArray();
    }
    
    public final void writeObjectFieldStart(final String fieldName) throws IOException, JsonGenerationException {
        this.writeFieldName(fieldName);
        this.writeStartObject();
    }
    
    public final void writeObjectField(final String fieldName, final Object pojo) throws IOException, JsonProcessingException {
        this.writeFieldName(fieldName);
        this.writeObject(pojo);
    }
    
    public abstract void copyCurrentEvent(final JsonParser p0) throws IOException, JsonProcessingException;
    
    public abstract void copyCurrentStructure(final JsonParser p0) throws IOException, JsonProcessingException;
    
    public abstract JsonStreamContext getOutputContext();
    
    public abstract void flush() throws IOException;
    
    public abstract boolean isClosed();
    
    public abstract void close() throws IOException;
    
    public enum Feature
    {
        AUTO_CLOSE_TARGET(true), 
        AUTO_CLOSE_JSON_CONTENT(true), 
        QUOTE_FIELD_NAMES(true), 
        QUOTE_NON_NUMERIC_NUMBERS(true), 
        WRITE_NUMBERS_AS_STRINGS(false), 
        FLUSH_PASSED_TO_STREAM(true), 
        ESCAPE_NON_ASCII(false);
        
        final boolean _defaultState;
        final int _mask;
        
        public static int collectDefaults() {
            int flags = 0;
            for (final Feature f : values()) {
                if (f.enabledByDefault()) {
                    flags |= f.getMask();
                }
            }
            return flags;
        }
        
        private Feature(final boolean defaultState) {
            this._defaultState = defaultState;
            this._mask = 1 << this.ordinal();
        }
        
        public boolean enabledByDefault() {
            return this._defaultState;
        }
        
        public int getMask() {
            return this._mask;
        }
    }
}
