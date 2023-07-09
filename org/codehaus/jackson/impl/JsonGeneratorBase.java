// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.impl;

import org.codehaus.jackson.JsonStreamContext;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.PrettyPrinter;
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import org.codehaus.jackson.util.VersionUtil;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.JsonGenerator;

public abstract class JsonGeneratorBase extends JsonGenerator
{
    protected ObjectCodec _objectCodec;
    protected int _features;
    protected boolean _cfgNumbersAsStrings;
    protected JsonWriteContext _writeContext;
    protected boolean _closed;
    
    protected JsonGeneratorBase(final int features, final ObjectCodec codec) {
        this._features = features;
        this._writeContext = JsonWriteContext.createRootContext();
        this._objectCodec = codec;
        this._cfgNumbersAsStrings = this.isEnabled(Feature.WRITE_NUMBERS_AS_STRINGS);
    }
    
    @Override
    public Version version() {
        return VersionUtil.versionFor(this.getClass());
    }
    
    @Override
    public JsonGenerator enable(final Feature f) {
        this._features |= f.getMask();
        if (f == Feature.WRITE_NUMBERS_AS_STRINGS) {
            this._cfgNumbersAsStrings = true;
        }
        else if (f == Feature.ESCAPE_NON_ASCII) {
            this.setHighestNonEscapedChar(127);
        }
        return this;
    }
    
    @Override
    public JsonGenerator disable(final Feature f) {
        this._features &= ~f.getMask();
        if (f == Feature.WRITE_NUMBERS_AS_STRINGS) {
            this._cfgNumbersAsStrings = false;
        }
        else if (f == Feature.ESCAPE_NON_ASCII) {
            this.setHighestNonEscapedChar(0);
        }
        return this;
    }
    
    @Override
    public final boolean isEnabled(final Feature f) {
        return (this._features & f.getMask()) != 0x0;
    }
    
    @Override
    public JsonGenerator useDefaultPrettyPrinter() {
        return this.setPrettyPrinter(new DefaultPrettyPrinter());
    }
    
    @Override
    public JsonGenerator setCodec(final ObjectCodec oc) {
        this._objectCodec = oc;
        return this;
    }
    
    @Override
    public final ObjectCodec getCodec() {
        return this._objectCodec;
    }
    
    @Override
    public final JsonWriteContext getOutputContext() {
        return this._writeContext;
    }
    
    @Override
    public void writeStartArray() throws IOException, JsonGenerationException {
        this._verifyValueWrite("start an array");
        this._writeContext = this._writeContext.createChildArrayContext();
        if (this._cfgPrettyPrinter != null) {
            this._cfgPrettyPrinter.writeStartArray(this);
        }
        else {
            this._writeStartArray();
        }
    }
    
    @Deprecated
    protected void _writeStartArray() throws IOException, JsonGenerationException {
    }
    
    @Override
    public void writeEndArray() throws IOException, JsonGenerationException {
        if (!this._writeContext.inArray()) {
            this._reportError("Current context not an ARRAY but " + this._writeContext.getTypeDesc());
        }
        if (this._cfgPrettyPrinter != null) {
            this._cfgPrettyPrinter.writeEndArray(this, this._writeContext.getEntryCount());
        }
        else {
            this._writeEndArray();
        }
        this._writeContext = this._writeContext.getParent();
    }
    
    @Deprecated
    protected void _writeEndArray() throws IOException, JsonGenerationException {
    }
    
    @Override
    public void writeStartObject() throws IOException, JsonGenerationException {
        this._verifyValueWrite("start an object");
        this._writeContext = this._writeContext.createChildObjectContext();
        if (this._cfgPrettyPrinter != null) {
            this._cfgPrettyPrinter.writeStartObject(this);
        }
        else {
            this._writeStartObject();
        }
    }
    
    @Deprecated
    protected void _writeStartObject() throws IOException, JsonGenerationException {
    }
    
    @Override
    public void writeEndObject() throws IOException, JsonGenerationException {
        if (!this._writeContext.inObject()) {
            this._reportError("Current context not an object but " + this._writeContext.getTypeDesc());
        }
        this._writeContext = this._writeContext.getParent();
        if (this._cfgPrettyPrinter != null) {
            this._cfgPrettyPrinter.writeEndObject(this, this._writeContext.getEntryCount());
        }
        else {
            this._writeEndObject();
        }
    }
    
    @Deprecated
    protected void _writeEndObject() throws IOException, JsonGenerationException {
    }
    
    @Override
    public void writeRawValue(final String text) throws IOException, JsonGenerationException {
        this._verifyValueWrite("write raw value");
        this.writeRaw(text);
    }
    
    @Override
    public void writeRawValue(final String text, final int offset, final int len) throws IOException, JsonGenerationException {
        this._verifyValueWrite("write raw value");
        this.writeRaw(text, offset, len);
    }
    
    @Override
    public void writeRawValue(final char[] text, final int offset, final int len) throws IOException, JsonGenerationException {
        this._verifyValueWrite("write raw value");
        this.writeRaw(text, offset, len);
    }
    
    @Override
    public void writeObject(final Object value) throws IOException, JsonProcessingException {
        if (value == null) {
            this.writeNull();
        }
        else {
            if (this._objectCodec != null) {
                this._objectCodec.writeValue(this, value);
                return;
            }
            this._writeSimpleObject(value);
        }
    }
    
    @Override
    public void writeTree(final JsonNode rootNode) throws IOException, JsonProcessingException {
        if (rootNode == null) {
            this.writeNull();
        }
        else {
            if (this._objectCodec == null) {
                throw new IllegalStateException("No ObjectCodec defined for the generator, can not serialize JsonNode-based trees");
            }
            this._objectCodec.writeTree(this, rootNode);
        }
    }
    
    @Override
    public abstract void flush() throws IOException;
    
    @Override
    public void close() throws IOException {
        this._closed = true;
    }
    
    @Override
    public boolean isClosed() {
        return this._closed;
    }
    
    @Override
    public final void copyCurrentEvent(final JsonParser jp) throws IOException, JsonProcessingException {
        final JsonToken t = jp.getCurrentToken();
        if (t == null) {
            this._reportError("No current event to copy");
        }
        Label_0339: {
            switch (t) {
                case START_OBJECT: {
                    this.writeStartObject();
                    break;
                }
                case END_OBJECT: {
                    this.writeEndObject();
                    break;
                }
                case START_ARRAY: {
                    this.writeStartArray();
                    break;
                }
                case END_ARRAY: {
                    this.writeEndArray();
                    break;
                }
                case FIELD_NAME: {
                    this.writeFieldName(jp.getCurrentName());
                    break;
                }
                case VALUE_STRING: {
                    if (jp.hasTextCharacters()) {
                        this.writeString(jp.getTextCharacters(), jp.getTextOffset(), jp.getTextLength());
                        break;
                    }
                    this.writeString(jp.getText());
                    break;
                }
                case VALUE_NUMBER_INT: {
                    switch (jp.getNumberType()) {
                        case INT: {
                            this.writeNumber(jp.getIntValue());
                            break Label_0339;
                        }
                        case BIG_INTEGER: {
                            this.writeNumber(jp.getBigIntegerValue());
                            break Label_0339;
                        }
                        default: {
                            this.writeNumber(jp.getLongValue());
                            break Label_0339;
                        }
                    }
                    break;
                }
                case VALUE_NUMBER_FLOAT: {
                    switch (jp.getNumberType()) {
                        case BIG_DECIMAL: {
                            this.writeNumber(jp.getDecimalValue());
                            break Label_0339;
                        }
                        case FLOAT: {
                            this.writeNumber(jp.getFloatValue());
                            break Label_0339;
                        }
                        default: {
                            this.writeNumber(jp.getDoubleValue());
                            break Label_0339;
                        }
                    }
                    break;
                }
                case VALUE_TRUE: {
                    this.writeBoolean(true);
                    break;
                }
                case VALUE_FALSE: {
                    this.writeBoolean(false);
                    break;
                }
                case VALUE_NULL: {
                    this.writeNull();
                    break;
                }
                case VALUE_EMBEDDED_OBJECT: {
                    this.writeObject(jp.getEmbeddedObject());
                    break;
                }
                default: {
                    this._cantHappen();
                    break;
                }
            }
        }
    }
    
    @Override
    public final void copyCurrentStructure(final JsonParser jp) throws IOException, JsonProcessingException {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.FIELD_NAME) {
            this.writeFieldName(jp.getCurrentName());
            t = jp.nextToken();
        }
        switch (t) {
            case START_ARRAY: {
                this.writeStartArray();
                while (jp.nextToken() != JsonToken.END_ARRAY) {
                    this.copyCurrentStructure(jp);
                }
                this.writeEndArray();
                break;
            }
            case START_OBJECT: {
                this.writeStartObject();
                while (jp.nextToken() != JsonToken.END_OBJECT) {
                    this.copyCurrentStructure(jp);
                }
                this.writeEndObject();
                break;
            }
            default: {
                this.copyCurrentEvent(jp);
                break;
            }
        }
    }
    
    protected abstract void _releaseBuffers();
    
    protected abstract void _verifyValueWrite(final String p0) throws IOException, JsonGenerationException;
    
    protected void _reportError(final String msg) throws JsonGenerationException {
        throw new JsonGenerationException(msg);
    }
    
    protected void _cantHappen() {
        throw new RuntimeException("Internal error: should never end up through this code path");
    }
    
    protected void _writeSimpleObject(final Object value) throws IOException, JsonGenerationException {
        if (value == null) {
            this.writeNull();
            return;
        }
        if (value instanceof String) {
            this.writeString((String)value);
            return;
        }
        if (value instanceof Number) {
            final Number n = (Number)value;
            if (n instanceof Integer) {
                this.writeNumber(n.intValue());
                return;
            }
            if (n instanceof Long) {
                this.writeNumber(n.longValue());
                return;
            }
            if (n instanceof Double) {
                this.writeNumber(n.doubleValue());
                return;
            }
            if (n instanceof Float) {
                this.writeNumber(n.floatValue());
                return;
            }
            if (n instanceof Short) {
                this.writeNumber(n.shortValue());
                return;
            }
            if (n instanceof Byte) {
                this.writeNumber(n.byteValue());
                return;
            }
            if (n instanceof BigInteger) {
                this.writeNumber((BigInteger)n);
                return;
            }
            if (n instanceof BigDecimal) {
                this.writeNumber((BigDecimal)n);
                return;
            }
            if (n instanceof AtomicInteger) {
                this.writeNumber(((AtomicInteger)n).get());
                return;
            }
            if (n instanceof AtomicLong) {
                this.writeNumber(((AtomicLong)n).get());
                return;
            }
        }
        else {
            if (value instanceof byte[]) {
                this.writeBinary((byte[])value);
                return;
            }
            if (value instanceof Boolean) {
                this.writeBoolean((boolean)value);
                return;
            }
            if (value instanceof AtomicBoolean) {
                this.writeBoolean(((AtomicBoolean)value).get());
                return;
            }
        }
        throw new IllegalStateException("No ObjectCodec defined for the generator, can only serialize simple wrapper types (type passed " + value.getClass().getName() + ")");
    }
    
    protected final void _throwInternal() {
        throw new RuntimeException("Internal error: this code path should never get executed");
    }
    
    protected void _reportUnsupportedOperation() {
        throw new UnsupportedOperationException("Operation not supported by generator of type " + this.getClass().getName());
    }
}
