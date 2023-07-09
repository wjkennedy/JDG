// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.impl;

import org.codehaus.jackson.util.CharTypes;
import org.codehaus.jackson.JsonStreamContext;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.codehaus.jackson.io.NumberOutput;
import org.codehaus.jackson.Base64Variant;
import org.codehaus.jackson.io.SerializedString;
import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.SerializableString;
import org.codehaus.jackson.io.CharacterEscapes;
import java.io.Writer;
import org.codehaus.jackson.io.IOContext;

public final class WriterBasedGenerator extends JsonGeneratorBase
{
    protected static final int SHORT_WRITE = 32;
    protected static final char[] HEX_CHARS;
    protected static final int[] sOutputEscapes;
    protected final IOContext _ioContext;
    protected final Writer _writer;
    protected int[] _outputEscapes;
    protected int _maximumNonEscapedChar;
    protected CharacterEscapes _characterEscapes;
    protected SerializableString _currentEscape;
    protected char[] _outputBuffer;
    protected int _outputHead;
    protected int _outputTail;
    protected int _outputEnd;
    protected char[] _entityBuffer;
    
    public WriterBasedGenerator(final IOContext ctxt, final int features, final ObjectCodec codec, final Writer w) {
        super(features, codec);
        this._outputEscapes = WriterBasedGenerator.sOutputEscapes;
        this._outputHead = 0;
        this._outputTail = 0;
        this._ioContext = ctxt;
        this._writer = w;
        this._outputBuffer = ctxt.allocConcatBuffer();
        this._outputEnd = this._outputBuffer.length;
        if (this.isEnabled(Feature.ESCAPE_NON_ASCII)) {
            this.setHighestNonEscapedChar(127);
        }
    }
    
    @Override
    public JsonGenerator setHighestNonEscapedChar(final int charCode) {
        this._maximumNonEscapedChar = ((charCode < 0) ? 0 : charCode);
        return this;
    }
    
    @Override
    public int getHighestEscapedChar() {
        return this._maximumNonEscapedChar;
    }
    
    @Override
    public JsonGenerator setCharacterEscapes(final CharacterEscapes esc) {
        this._characterEscapes = esc;
        if (esc == null) {
            this._outputEscapes = WriterBasedGenerator.sOutputEscapes;
        }
        else {
            this._outputEscapes = esc.getEscapeCodesForAscii();
        }
        return this;
    }
    
    @Override
    public CharacterEscapes getCharacterEscapes() {
        return this._characterEscapes;
    }
    
    @Override
    public Object getOutputTarget() {
        return this._writer;
    }
    
    @Override
    public final void writeFieldName(final String name) throws IOException, JsonGenerationException {
        final int status = this._writeContext.writeFieldName(name);
        if (status == 4) {
            this._reportError("Can not write a field name, expecting a value");
        }
        this._writeFieldName(name, status == 1);
    }
    
    @Override
    public final void writeStringField(final String fieldName, final String value) throws IOException, JsonGenerationException {
        this.writeFieldName(fieldName);
        this.writeString(value);
    }
    
    @Override
    public final void writeFieldName(final SerializedString name) throws IOException, JsonGenerationException {
        final int status = this._writeContext.writeFieldName(name.getValue());
        if (status == 4) {
            this._reportError("Can not write a field name, expecting a value");
        }
        this._writeFieldName(name, status == 1);
    }
    
    @Override
    public final void writeFieldName(final SerializableString name) throws IOException, JsonGenerationException {
        final int status = this._writeContext.writeFieldName(name.getValue());
        if (status == 4) {
            this._reportError("Can not write a field name, expecting a value");
        }
        this._writeFieldName(name, status == 1);
    }
    
    @Override
    public final void writeStartArray() throws IOException, JsonGenerationException {
        this._verifyValueWrite("start an array");
        this._writeContext = this._writeContext.createChildArrayContext();
        if (this._cfgPrettyPrinter != null) {
            this._cfgPrettyPrinter.writeStartArray(this);
        }
        else {
            if (this._outputTail >= this._outputEnd) {
                this._flushBuffer();
            }
            this._outputBuffer[this._outputTail++] = '[';
        }
    }
    
    @Override
    public final void writeEndArray() throws IOException, JsonGenerationException {
        if (!this._writeContext.inArray()) {
            this._reportError("Current context not an ARRAY but " + this._writeContext.getTypeDesc());
        }
        if (this._cfgPrettyPrinter != null) {
            this._cfgPrettyPrinter.writeEndArray(this, this._writeContext.getEntryCount());
        }
        else {
            if (this._outputTail >= this._outputEnd) {
                this._flushBuffer();
            }
            this._outputBuffer[this._outputTail++] = ']';
        }
        this._writeContext = this._writeContext.getParent();
    }
    
    @Override
    public final void writeStartObject() throws IOException, JsonGenerationException {
        this._verifyValueWrite("start an object");
        this._writeContext = this._writeContext.createChildObjectContext();
        if (this._cfgPrettyPrinter != null) {
            this._cfgPrettyPrinter.writeStartObject(this);
        }
        else {
            if (this._outputTail >= this._outputEnd) {
                this._flushBuffer();
            }
            this._outputBuffer[this._outputTail++] = '{';
        }
    }
    
    @Override
    public final void writeEndObject() throws IOException, JsonGenerationException {
        if (!this._writeContext.inObject()) {
            this._reportError("Current context not an object but " + this._writeContext.getTypeDesc());
        }
        if (this._cfgPrettyPrinter != null) {
            this._cfgPrettyPrinter.writeEndObject(this, this._writeContext.getEntryCount());
        }
        else {
            if (this._outputTail >= this._outputEnd) {
                this._flushBuffer();
            }
            this._outputBuffer[this._outputTail++] = '}';
        }
        this._writeContext = this._writeContext.getParent();
    }
    
    protected void _writeFieldName(final String name, final boolean commaBefore) throws IOException, JsonGenerationException {
        if (this._cfgPrettyPrinter != null) {
            this._writePPFieldName(name, commaBefore);
            return;
        }
        if (this._outputTail + 1 >= this._outputEnd) {
            this._flushBuffer();
        }
        if (commaBefore) {
            this._outputBuffer[this._outputTail++] = ',';
        }
        if (!this.isEnabled(Feature.QUOTE_FIELD_NAMES)) {
            this._writeString(name);
            return;
        }
        this._outputBuffer[this._outputTail++] = '\"';
        this._writeString(name);
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = '\"';
    }
    
    public void _writeFieldName(final SerializableString name, final boolean commaBefore) throws IOException, JsonGenerationException {
        if (this._cfgPrettyPrinter != null) {
            this._writePPFieldName(name, commaBefore);
            return;
        }
        if (this._outputTail + 1 >= this._outputEnd) {
            this._flushBuffer();
        }
        if (commaBefore) {
            this._outputBuffer[this._outputTail++] = ',';
        }
        final char[] quoted = name.asQuotedChars();
        if (!this.isEnabled(Feature.QUOTE_FIELD_NAMES)) {
            this.writeRaw(quoted, 0, quoted.length);
            return;
        }
        this._outputBuffer[this._outputTail++] = '\"';
        final int qlen = quoted.length;
        if (this._outputTail + qlen + 1 >= this._outputEnd) {
            this.writeRaw(quoted, 0, qlen);
            if (this._outputTail >= this._outputEnd) {
                this._flushBuffer();
            }
            this._outputBuffer[this._outputTail++] = '\"';
        }
        else {
            System.arraycopy(quoted, 0, this._outputBuffer, this._outputTail, qlen);
            this._outputTail += qlen;
            this._outputBuffer[this._outputTail++] = '\"';
        }
    }
    
    protected final void _writePPFieldName(final String name, final boolean commaBefore) throws IOException, JsonGenerationException {
        if (commaBefore) {
            this._cfgPrettyPrinter.writeObjectEntrySeparator(this);
        }
        else {
            this._cfgPrettyPrinter.beforeObjectEntries(this);
        }
        if (this.isEnabled(Feature.QUOTE_FIELD_NAMES)) {
            if (this._outputTail >= this._outputEnd) {
                this._flushBuffer();
            }
            this._outputBuffer[this._outputTail++] = '\"';
            this._writeString(name);
            if (this._outputTail >= this._outputEnd) {
                this._flushBuffer();
            }
            this._outputBuffer[this._outputTail++] = '\"';
        }
        else {
            this._writeString(name);
        }
    }
    
    protected final void _writePPFieldName(final SerializableString name, final boolean commaBefore) throws IOException, JsonGenerationException {
        if (commaBefore) {
            this._cfgPrettyPrinter.writeObjectEntrySeparator(this);
        }
        else {
            this._cfgPrettyPrinter.beforeObjectEntries(this);
        }
        final char[] quoted = name.asQuotedChars();
        if (this.isEnabled(Feature.QUOTE_FIELD_NAMES)) {
            if (this._outputTail >= this._outputEnd) {
                this._flushBuffer();
            }
            this._outputBuffer[this._outputTail++] = '\"';
            this.writeRaw(quoted, 0, quoted.length);
            if (this._outputTail >= this._outputEnd) {
                this._flushBuffer();
            }
            this._outputBuffer[this._outputTail++] = '\"';
        }
        else {
            this.writeRaw(quoted, 0, quoted.length);
        }
    }
    
    @Override
    public void writeString(final String text) throws IOException, JsonGenerationException {
        this._verifyValueWrite("write text value");
        if (text == null) {
            this._writeNull();
            return;
        }
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = '\"';
        this._writeString(text);
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = '\"';
    }
    
    @Override
    public void writeString(final char[] text, final int offset, final int len) throws IOException, JsonGenerationException {
        this._verifyValueWrite("write text value");
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = '\"';
        this._writeString(text, offset, len);
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = '\"';
    }
    
    @Override
    public final void writeString(final SerializableString sstr) throws IOException, JsonGenerationException {
        this._verifyValueWrite("write text value");
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = '\"';
        final char[] text = sstr.asQuotedChars();
        final int len = text.length;
        if (len < 32) {
            final int room = this._outputEnd - this._outputTail;
            if (len > room) {
                this._flushBuffer();
            }
            System.arraycopy(text, 0, this._outputBuffer, this._outputTail, len);
            this._outputTail += len;
        }
        else {
            this._flushBuffer();
            this._writer.write(text, 0, len);
        }
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = '\"';
    }
    
    @Override
    public void writeRawUTF8String(final byte[] text, final int offset, final int length) throws IOException, JsonGenerationException {
        this._reportUnsupportedOperation();
    }
    
    @Override
    public void writeUTF8String(final byte[] text, final int offset, final int length) throws IOException, JsonGenerationException {
        this._reportUnsupportedOperation();
    }
    
    @Override
    public void writeRaw(final String text) throws IOException, JsonGenerationException {
        final int len = text.length();
        int room = this._outputEnd - this._outputTail;
        if (room == 0) {
            this._flushBuffer();
            room = this._outputEnd - this._outputTail;
        }
        if (room >= len) {
            text.getChars(0, len, this._outputBuffer, this._outputTail);
            this._outputTail += len;
        }
        else {
            this.writeRawLong(text);
        }
    }
    
    @Override
    public void writeRaw(final String text, final int start, final int len) throws IOException, JsonGenerationException {
        int room = this._outputEnd - this._outputTail;
        if (room < len) {
            this._flushBuffer();
            room = this._outputEnd - this._outputTail;
        }
        if (room >= len) {
            text.getChars(start, start + len, this._outputBuffer, this._outputTail);
            this._outputTail += len;
        }
        else {
            this.writeRawLong(text.substring(start, start + len));
        }
    }
    
    @Override
    public void writeRaw(final char[] text, final int offset, final int len) throws IOException, JsonGenerationException {
        if (len < 32) {
            final int room = this._outputEnd - this._outputTail;
            if (len > room) {
                this._flushBuffer();
            }
            System.arraycopy(text, offset, this._outputBuffer, this._outputTail, len);
            this._outputTail += len;
            return;
        }
        this._flushBuffer();
        this._writer.write(text, offset, len);
    }
    
    @Override
    public void writeRaw(final char c) throws IOException, JsonGenerationException {
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = c;
    }
    
    private void writeRawLong(final String text) throws IOException, JsonGenerationException {
        final int room = this._outputEnd - this._outputTail;
        text.getChars(0, room, this._outputBuffer, this._outputTail);
        this._outputTail += room;
        this._flushBuffer();
        int offset = room;
        int len;
        int amount;
        for (len = text.length() - room; len > this._outputEnd; len -= amount) {
            amount = this._outputEnd;
            text.getChars(offset, offset + amount, this._outputBuffer, 0);
            this._outputHead = 0;
            this._outputTail = amount;
            this._flushBuffer();
            offset += amount;
        }
        text.getChars(offset, offset + len, this._outputBuffer, 0);
        this._outputHead = 0;
        this._outputTail = len;
    }
    
    @Override
    public void writeBinary(final Base64Variant b64variant, final byte[] data, final int offset, final int len) throws IOException, JsonGenerationException {
        this._verifyValueWrite("write binary value");
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = '\"';
        this._writeBinary(b64variant, data, offset, offset + len);
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = '\"';
    }
    
    @Override
    public void writeNumber(final int i) throws IOException, JsonGenerationException {
        this._verifyValueWrite("write number");
        if (this._cfgNumbersAsStrings) {
            this._writeQuotedInt(i);
            return;
        }
        if (this._outputTail + 11 >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputTail = NumberOutput.outputInt(i, this._outputBuffer, this._outputTail);
    }
    
    private final void _writeQuotedInt(final int i) throws IOException {
        if (this._outputTail + 13 >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = '\"';
        this._outputTail = NumberOutput.outputInt(i, this._outputBuffer, this._outputTail);
        this._outputBuffer[this._outputTail++] = '\"';
    }
    
    @Override
    public void writeNumber(final long l) throws IOException, JsonGenerationException {
        this._verifyValueWrite("write number");
        if (this._cfgNumbersAsStrings) {
            this._writeQuotedLong(l);
            return;
        }
        if (this._outputTail + 21 >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputTail = NumberOutput.outputLong(l, this._outputBuffer, this._outputTail);
    }
    
    private final void _writeQuotedLong(final long l) throws IOException {
        if (this._outputTail + 23 >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = '\"';
        this._outputTail = NumberOutput.outputLong(l, this._outputBuffer, this._outputTail);
        this._outputBuffer[this._outputTail++] = '\"';
    }
    
    @Override
    public void writeNumber(final BigInteger value) throws IOException, JsonGenerationException {
        this._verifyValueWrite("write number");
        if (value == null) {
            this._writeNull();
        }
        else if (this._cfgNumbersAsStrings) {
            this._writeQuotedRaw(value);
        }
        else {
            this.writeRaw(value.toString());
        }
    }
    
    @Override
    public void writeNumber(final double d) throws IOException, JsonGenerationException {
        if (this._cfgNumbersAsStrings || ((Double.isNaN(d) || Double.isInfinite(d)) && this.isEnabled(Feature.QUOTE_NON_NUMERIC_NUMBERS))) {
            this.writeString(String.valueOf(d));
            return;
        }
        this._verifyValueWrite("write number");
        this.writeRaw(String.valueOf(d));
    }
    
    @Override
    public void writeNumber(final float f) throws IOException, JsonGenerationException {
        if (this._cfgNumbersAsStrings || ((Float.isNaN(f) || Float.isInfinite(f)) && this.isEnabled(Feature.QUOTE_NON_NUMERIC_NUMBERS))) {
            this.writeString(String.valueOf(f));
            return;
        }
        this._verifyValueWrite("write number");
        this.writeRaw(String.valueOf(f));
    }
    
    @Override
    public void writeNumber(final BigDecimal value) throws IOException, JsonGenerationException {
        this._verifyValueWrite("write number");
        if (value == null) {
            this._writeNull();
        }
        else if (this._cfgNumbersAsStrings) {
            this._writeQuotedRaw(value);
        }
        else {
            this.writeRaw(value.toString());
        }
    }
    
    @Override
    public void writeNumber(final String encodedValue) throws IOException, JsonGenerationException {
        this._verifyValueWrite("write number");
        if (this._cfgNumbersAsStrings) {
            this._writeQuotedRaw(encodedValue);
        }
        else {
            this.writeRaw(encodedValue);
        }
    }
    
    private final void _writeQuotedRaw(final Object value) throws IOException {
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = '\"';
        this.writeRaw(value.toString());
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = '\"';
    }
    
    @Override
    public void writeBoolean(final boolean state) throws IOException, JsonGenerationException {
        this._verifyValueWrite("write boolean value");
        if (this._outputTail + 5 >= this._outputEnd) {
            this._flushBuffer();
        }
        int ptr = this._outputTail;
        final char[] buf = this._outputBuffer;
        if (state) {
            buf[ptr] = 't';
            buf[++ptr] = 'r';
            buf[++ptr] = 'u';
            buf[++ptr] = 'e';
        }
        else {
            buf[ptr] = 'f';
            buf[++ptr] = 'a';
            buf[++ptr] = 'l';
            buf[++ptr] = 's';
            buf[++ptr] = 'e';
        }
        this._outputTail = ptr + 1;
    }
    
    @Override
    public void writeNull() throws IOException, JsonGenerationException {
        this._verifyValueWrite("write null value");
        this._writeNull();
    }
    
    @Override
    protected final void _verifyValueWrite(final String typeMsg) throws IOException, JsonGenerationException {
        final int status = this._writeContext.writeValue();
        if (status == 5) {
            this._reportError("Can not " + typeMsg + ", expecting field name");
        }
        if (this._cfgPrettyPrinter == null) {
            char c = '\0';
            switch (status) {
                case 1: {
                    c = ',';
                    break;
                }
                case 2: {
                    c = ':';
                    break;
                }
                case 3: {
                    c = ' ';
                    break;
                }
                default: {
                    return;
                }
            }
            if (this._outputTail >= this._outputEnd) {
                this._flushBuffer();
            }
            this._outputBuffer[this._outputTail] = c;
            ++this._outputTail;
            return;
        }
        this._verifyPrettyValueWrite(typeMsg, status);
    }
    
    protected final void _verifyPrettyValueWrite(final String typeMsg, final int status) throws IOException, JsonGenerationException {
        switch (status) {
            case 1: {
                this._cfgPrettyPrinter.writeArrayValueSeparator(this);
                break;
            }
            case 2: {
                this._cfgPrettyPrinter.writeObjectFieldValueSeparator(this);
                break;
            }
            case 3: {
                this._cfgPrettyPrinter.writeRootValueSeparator(this);
                break;
            }
            case 0: {
                if (this._writeContext.inArray()) {
                    this._cfgPrettyPrinter.beforeArrayValues(this);
                    break;
                }
                if (this._writeContext.inObject()) {
                    this._cfgPrettyPrinter.beforeObjectEntries(this);
                    break;
                }
                break;
            }
            default: {
                this._cantHappen();
                break;
            }
        }
    }
    
    @Override
    public final void flush() throws IOException {
        this._flushBuffer();
        if (this._writer != null && this.isEnabled(Feature.FLUSH_PASSED_TO_STREAM)) {
            this._writer.flush();
        }
    }
    
    @Override
    public void close() throws IOException {
        super.close();
        if (this._outputBuffer != null && this.isEnabled(Feature.AUTO_CLOSE_JSON_CONTENT)) {
            while (true) {
                final JsonStreamContext ctxt = this.getOutputContext();
                if (ctxt.inArray()) {
                    this.writeEndArray();
                }
                else {
                    if (!ctxt.inObject()) {
                        break;
                    }
                    this.writeEndObject();
                }
            }
        }
        this._flushBuffer();
        if (this._writer != null) {
            if (this._ioContext.isResourceManaged() || this.isEnabled(Feature.AUTO_CLOSE_TARGET)) {
                this._writer.close();
            }
            else if (this.isEnabled(Feature.FLUSH_PASSED_TO_STREAM)) {
                this._writer.flush();
            }
        }
        this._releaseBuffers();
    }
    
    @Override
    protected void _releaseBuffers() {
        final char[] buf = this._outputBuffer;
        if (buf != null) {
            this._outputBuffer = null;
            this._ioContext.releaseConcatBuffer(buf);
        }
    }
    
    private void _writeString(final String text) throws IOException, JsonGenerationException {
        final int len = text.length();
        if (len > this._outputEnd) {
            this._writeLongString(text);
            return;
        }
        if (this._outputTail + len > this._outputEnd) {
            this._flushBuffer();
        }
        text.getChars(0, len, this._outputBuffer, this._outputTail);
        if (this._characterEscapes != null) {
            this._writeStringCustom(len);
        }
        else if (this._maximumNonEscapedChar != 0) {
            this._writeStringASCII(len, this._maximumNonEscapedChar);
        }
        else {
            this._writeString2(len);
        }
    }
    
    private void _writeString2(final int len) throws IOException, JsonGenerationException {
        final int end = this._outputTail + len;
        final int[] escCodes = this._outputEscapes;
        final int escLen = escCodes.length;
    Label_0137:
        while (this._outputTail < end) {
            while (true) {
                final char c = this._outputBuffer[this._outputTail];
                if (c < escLen && escCodes[c] != 0) {
                    final int flushLen = this._outputTail - this._outputHead;
                    if (flushLen > 0) {
                        this._writer.write(this._outputBuffer, this._outputHead, flushLen);
                    }
                    final char c2 = this._outputBuffer[this._outputTail++];
                    this._prependOrWriteCharacterEscape(c2, escCodes[c2]);
                    break;
                }
                if (++this._outputTail >= end) {
                    break Label_0137;
                }
            }
        }
    }
    
    private void _writeLongString(final String text) throws IOException, JsonGenerationException {
        this._flushBuffer();
        final int textLen = text.length();
        int offset = 0;
        do {
            final int max = this._outputEnd;
            final int segmentLen = (offset + max > textLen) ? (textLen - offset) : max;
            text.getChars(offset, offset + segmentLen, this._outputBuffer, 0);
            if (this._characterEscapes != null) {
                this._writeSegmentCustom(segmentLen);
            }
            else if (this._maximumNonEscapedChar != 0) {
                this._writeSegmentASCII(segmentLen, this._maximumNonEscapedChar);
            }
            else {
                this._writeSegment(segmentLen);
            }
            offset += segmentLen;
        } while (offset < textLen);
    }
    
    private final void _writeSegment(final int end) throws IOException, JsonGenerationException {
        final int[] escCodes = this._outputEscapes;
        final int escLen = escCodes.length;
        int start;
        char c;
        for (int ptr = start = 0; ptr < end; ++ptr, start = this._prependOrWriteCharacterEscape(this._outputBuffer, ptr, end, c, escCodes[c])) {
            do {
                c = this._outputBuffer[ptr];
                if (c < escLen && escCodes[c] != 0) {
                    break;
                }
            } while (++ptr < end);
            final int flushLen = ptr - start;
            if (flushLen > 0) {
                this._writer.write(this._outputBuffer, start, flushLen);
                if (ptr >= end) {
                    break;
                }
            }
        }
    }
    
    private final void _writeString(final char[] text, int offset, int len) throws IOException, JsonGenerationException {
        if (this._characterEscapes != null) {
            this._writeStringCustom(text, offset, len);
            return;
        }
        if (this._maximumNonEscapedChar != 0) {
            this._writeStringASCII(text, offset, len, this._maximumNonEscapedChar);
            return;
        }
        len += offset;
        final int[] escCodes = this._outputEscapes;
        final int escLen = escCodes.length;
        while (offset < len) {
            final int start = offset;
            do {
                final char c = text[offset];
                if (c < escLen && escCodes[c] != 0) {
                    break;
                }
            } while (++offset < len);
            final int newAmount = offset - start;
            if (newAmount < 32) {
                if (this._outputTail + newAmount > this._outputEnd) {
                    this._flushBuffer();
                }
                if (newAmount > 0) {
                    System.arraycopy(text, start, this._outputBuffer, this._outputTail, newAmount);
                    this._outputTail += newAmount;
                }
            }
            else {
                this._flushBuffer();
                this._writer.write(text, start, newAmount);
            }
            if (offset >= len) {
                break;
            }
            final char c2 = text[offset++];
            this._appendCharacterEscape(c2, escCodes[c2]);
        }
    }
    
    private void _writeStringASCII(final int len, final int maxNonEscaped) throws IOException, JsonGenerationException {
        final int end = this._outputTail + len;
        final int[] escCodes = this._outputEscapes;
        final int escLimit = Math.min(escCodes.length, maxNonEscaped + 1);
        int escCode = 0;
    Label_0027:
        while (this._outputTail < end) {
            do {
                final char c = this._outputBuffer[this._outputTail];
                if (c < escLimit) {
                    escCode = escCodes[c];
                    if (escCode == 0) {
                        continue;
                    }
                }
                else {
                    if (c <= maxNonEscaped) {
                        continue;
                    }
                    escCode = -1;
                }
                final int flushLen = this._outputTail - this._outputHead;
                if (flushLen > 0) {
                    this._writer.write(this._outputBuffer, this._outputHead, flushLen);
                }
                ++this._outputTail;
                this._prependOrWriteCharacterEscape(c, escCode);
                continue Label_0027;
            } while (++this._outputTail < end);
            break;
        }
    }
    
    private final void _writeSegmentASCII(final int end, final int maxNonEscaped) throws IOException, JsonGenerationException {
        final int[] escCodes = this._outputEscapes;
        final int escLimit = Math.min(escCodes.length, maxNonEscaped + 1);
        int ptr = 0;
        int escCode = 0;
        int start = ptr;
        while (ptr < end) {
            char c;
            do {
                c = this._outputBuffer[ptr];
                if (c < escLimit) {
                    escCode = escCodes[c];
                    if (escCode != 0) {
                        break;
                    }
                    continue;
                }
                else {
                    if (c > maxNonEscaped) {
                        escCode = -1;
                        break;
                    }
                    continue;
                }
            } while (++ptr < end);
            final int flushLen = ptr - start;
            if (flushLen > 0) {
                this._writer.write(this._outputBuffer, start, flushLen);
                if (ptr >= end) {
                    break;
                }
            }
            ++ptr;
            start = this._prependOrWriteCharacterEscape(this._outputBuffer, ptr, end, c, escCode);
        }
    }
    
    private final void _writeStringASCII(final char[] text, int offset, int len, final int maxNonEscaped) throws IOException, JsonGenerationException {
        len += offset;
        final int[] escCodes = this._outputEscapes;
        final int escLimit = Math.min(escCodes.length, maxNonEscaped + 1);
        int escCode = 0;
        while (offset < len) {
            final int start = offset;
            char c;
            do {
                c = text[offset];
                if (c < escLimit) {
                    escCode = escCodes[c];
                    if (escCode != 0) {
                        break;
                    }
                    continue;
                }
                else {
                    if (c > maxNonEscaped) {
                        escCode = -1;
                        break;
                    }
                    continue;
                }
            } while (++offset < len);
            final int newAmount = offset - start;
            if (newAmount < 32) {
                if (this._outputTail + newAmount > this._outputEnd) {
                    this._flushBuffer();
                }
                if (newAmount > 0) {
                    System.arraycopy(text, start, this._outputBuffer, this._outputTail, newAmount);
                    this._outputTail += newAmount;
                }
            }
            else {
                this._flushBuffer();
                this._writer.write(text, start, newAmount);
            }
            if (offset >= len) {
                break;
            }
            ++offset;
            this._appendCharacterEscape(c, escCode);
        }
    }
    
    private void _writeStringCustom(final int len) throws IOException, JsonGenerationException {
        final int end = this._outputTail + len;
        final int[] escCodes = this._outputEscapes;
        final int maxNonEscaped = (this._maximumNonEscapedChar < 1) ? 65535 : this._maximumNonEscapedChar;
        final int escLimit = Math.min(escCodes.length, maxNonEscaped + 1);
        int escCode = 0;
        final CharacterEscapes customEscapes = this._characterEscapes;
    Label_0051:
        while (this._outputTail < end) {
            do {
                final char c = this._outputBuffer[this._outputTail];
                if (c < escLimit) {
                    escCode = escCodes[c];
                    if (escCode == 0) {
                        continue;
                    }
                }
                else if (c > maxNonEscaped) {
                    escCode = -1;
                }
                else {
                    if ((this._currentEscape = customEscapes.getEscapeSequence(c)) == null) {
                        continue;
                    }
                    escCode = -2;
                }
                final int flushLen = this._outputTail - this._outputHead;
                if (flushLen > 0) {
                    this._writer.write(this._outputBuffer, this._outputHead, flushLen);
                }
                ++this._outputTail;
                this._prependOrWriteCharacterEscape(c, escCode);
                continue Label_0051;
            } while (++this._outputTail < end);
            break;
        }
    }
    
    private final void _writeSegmentCustom(final int end) throws IOException, JsonGenerationException {
        final int[] escCodes = this._outputEscapes;
        final int maxNonEscaped = (this._maximumNonEscapedChar < 1) ? 65535 : this._maximumNonEscapedChar;
        final int escLimit = Math.min(escCodes.length, maxNonEscaped + 1);
        final CharacterEscapes customEscapes = this._characterEscapes;
        int ptr = 0;
        int escCode = 0;
        int start = ptr;
        while (ptr < end) {
            char c;
            do {
                c = this._outputBuffer[ptr];
                if (c < escLimit) {
                    escCode = escCodes[c];
                    if (escCode != 0) {
                        break;
                    }
                    continue;
                }
                else {
                    if (c > maxNonEscaped) {
                        escCode = -1;
                        break;
                    }
                    if ((this._currentEscape = customEscapes.getEscapeSequence(c)) != null) {
                        escCode = -2;
                        break;
                    }
                    continue;
                }
            } while (++ptr < end);
            final int flushLen = ptr - start;
            if (flushLen > 0) {
                this._writer.write(this._outputBuffer, start, flushLen);
                if (ptr >= end) {
                    break;
                }
            }
            ++ptr;
            start = this._prependOrWriteCharacterEscape(this._outputBuffer, ptr, end, c, escCode);
        }
    }
    
    private final void _writeStringCustom(final char[] text, int offset, int len) throws IOException, JsonGenerationException {
        len += offset;
        final int[] escCodes = this._outputEscapes;
        final int maxNonEscaped = (this._maximumNonEscapedChar < 1) ? 65535 : this._maximumNonEscapedChar;
        final int escLimit = Math.min(escCodes.length, maxNonEscaped + 1);
        final CharacterEscapes customEscapes = this._characterEscapes;
        int escCode = 0;
        while (offset < len) {
            final int start = offset;
            char c;
            do {
                c = text[offset];
                if (c < escLimit) {
                    escCode = escCodes[c];
                    if (escCode != 0) {
                        break;
                    }
                    continue;
                }
                else {
                    if (c > maxNonEscaped) {
                        escCode = -1;
                        break;
                    }
                    if ((this._currentEscape = customEscapes.getEscapeSequence(c)) != null) {
                        escCode = -2;
                        break;
                    }
                    continue;
                }
            } while (++offset < len);
            final int newAmount = offset - start;
            if (newAmount < 32) {
                if (this._outputTail + newAmount > this._outputEnd) {
                    this._flushBuffer();
                }
                if (newAmount > 0) {
                    System.arraycopy(text, start, this._outputBuffer, this._outputTail, newAmount);
                    this._outputTail += newAmount;
                }
            }
            else {
                this._flushBuffer();
                this._writer.write(text, start, newAmount);
            }
            if (offset >= len) {
                break;
            }
            ++offset;
            this._appendCharacterEscape(c, escCode);
        }
    }
    
    protected void _writeBinary(final Base64Variant b64variant, final byte[] input, int inputPtr, final int inputEnd) throws IOException, JsonGenerationException {
        final int safeInputEnd = inputEnd - 3;
        final int safeOutputEnd = this._outputEnd - 6;
        int chunksBeforeLF = b64variant.getMaxLineLength() >> 2;
        while (inputPtr <= safeInputEnd) {
            if (this._outputTail > safeOutputEnd) {
                this._flushBuffer();
            }
            int b24 = input[inputPtr++] << 8;
            b24 |= (input[inputPtr++] & 0xFF);
            b24 = (b24 << 8 | (input[inputPtr++] & 0xFF));
            this._outputTail = b64variant.encodeBase64Chunk(b24, this._outputBuffer, this._outputTail);
            if (--chunksBeforeLF <= 0) {
                this._outputBuffer[this._outputTail++] = '\\';
                this._outputBuffer[this._outputTail++] = 'n';
                chunksBeforeLF = b64variant.getMaxLineLength() >> 2;
            }
        }
        final int inputLeft = inputEnd - inputPtr;
        if (inputLeft > 0) {
            if (this._outputTail > safeOutputEnd) {
                this._flushBuffer();
            }
            int b25 = input[inputPtr++] << 16;
            if (inputLeft == 2) {
                b25 |= (input[inputPtr++] & 0xFF) << 8;
            }
            this._outputTail = b64variant.encodeBase64Partial(b25, inputLeft, this._outputBuffer, this._outputTail);
        }
    }
    
    private final void _writeNull() throws IOException {
        if (this._outputTail + 4 >= this._outputEnd) {
            this._flushBuffer();
        }
        int ptr = this._outputTail;
        final char[] buf = this._outputBuffer;
        buf[ptr] = 'n';
        buf[++ptr] = 'u';
        buf[++ptr] = 'l';
        buf[++ptr] = 'l';
        this._outputTail = ptr + 1;
    }
    
    private final void _prependOrWriteCharacterEscape(char ch, final int escCode) throws IOException, JsonGenerationException {
        if (escCode >= 0) {
            if (this._outputTail >= 2) {
                int ptr = this._outputTail - 2;
                this._outputHead = ptr;
                this._outputBuffer[ptr++] = '\\';
                this._outputBuffer[ptr] = (char)escCode;
                return;
            }
            char[] buf = this._entityBuffer;
            if (buf == null) {
                buf = this._allocateEntityBuffer();
            }
            this._outputHead = this._outputTail;
            buf[1] = (char)escCode;
            this._writer.write(buf, 0, 2);
        }
        else if (escCode != -2) {
            if (this._outputTail >= 6) {
                final char[] buf = this._outputBuffer;
                int ptr2 = this._outputTail - 6;
                buf[this._outputHead = ptr2] = '\\';
                buf[++ptr2] = 'u';
                if (ch > '\u00ff') {
                    final int hi = ch >> 8 & 0xFF;
                    buf[++ptr2] = WriterBasedGenerator.HEX_CHARS[hi >> 4];
                    buf[++ptr2] = WriterBasedGenerator.HEX_CHARS[hi & 0xF];
                    ch &= '\u00ff';
                }
                else {
                    buf[++ptr2] = '0';
                    buf[++ptr2] = '0';
                }
                buf[++ptr2] = WriterBasedGenerator.HEX_CHARS[ch >> 4];
                buf[++ptr2] = WriterBasedGenerator.HEX_CHARS[ch & '\u000f'];
                return;
            }
            char[] buf = this._entityBuffer;
            if (buf == null) {
                buf = this._allocateEntityBuffer();
            }
            this._outputHead = this._outputTail;
            if (ch > '\u00ff') {
                final int hi2 = ch >> 8 & 0xFF;
                final int lo = ch & '\u00ff';
                buf[10] = WriterBasedGenerator.HEX_CHARS[hi2 >> 4];
                buf[11] = WriterBasedGenerator.HEX_CHARS[hi2 & 0xF];
                buf[12] = WriterBasedGenerator.HEX_CHARS[lo >> 4];
                buf[13] = WriterBasedGenerator.HEX_CHARS[lo & 0xF];
                this._writer.write(buf, 8, 6);
            }
            else {
                buf[6] = WriterBasedGenerator.HEX_CHARS[ch >> 4];
                buf[7] = WriterBasedGenerator.HEX_CHARS[ch & '\u000f'];
                this._writer.write(buf, 2, 6);
            }
        }
        else {
            String escape;
            if (this._currentEscape == null) {
                escape = this._characterEscapes.getEscapeSequence(ch).getValue();
            }
            else {
                escape = this._currentEscape.getValue();
                this._currentEscape = null;
            }
            final int len = escape.length();
            if (this._outputTail >= len) {
                final int ptr3 = this._outputTail - len;
                this._outputHead = ptr3;
                escape.getChars(0, len, this._outputBuffer, ptr3);
                return;
            }
            this._outputHead = this._outputTail;
            this._writer.write(escape);
        }
    }
    
    private final int _prependOrWriteCharacterEscape(final char[] buffer, int ptr, final int end, char ch, final int escCode) throws IOException, JsonGenerationException {
        if (escCode >= 0) {
            if (ptr > 1 && ptr < end) {
                ptr -= 2;
                buffer[ptr] = '\\';
                buffer[ptr + 1] = (char)escCode;
            }
            else {
                char[] ent = this._entityBuffer;
                if (ent == null) {
                    ent = this._allocateEntityBuffer();
                }
                ent[1] = (char)escCode;
                this._writer.write(ent, 0, 2);
            }
            return ptr;
        }
        if (escCode != -2) {
            if (ptr > 5 && ptr < end) {
                ptr -= 6;
                buffer[ptr++] = '\\';
                buffer[ptr++] = 'u';
                if (ch > '\u00ff') {
                    final int hi = ch >> 8 & 0xFF;
                    buffer[ptr++] = WriterBasedGenerator.HEX_CHARS[hi >> 4];
                    buffer[ptr++] = WriterBasedGenerator.HEX_CHARS[hi & 0xF];
                    ch &= '\u00ff';
                }
                else {
                    buffer[ptr++] = '0';
                    buffer[ptr++] = '0';
                }
                buffer[ptr++] = WriterBasedGenerator.HEX_CHARS[ch >> 4];
                buffer[ptr] = WriterBasedGenerator.HEX_CHARS[ch & '\u000f'];
                ptr -= 5;
            }
            else {
                char[] ent = this._entityBuffer;
                if (ent == null) {
                    ent = this._allocateEntityBuffer();
                }
                this._outputHead = this._outputTail;
                if (ch > '\u00ff') {
                    final int hi2 = ch >> 8 & 0xFF;
                    final int lo = ch & '\u00ff';
                    ent[10] = WriterBasedGenerator.HEX_CHARS[hi2 >> 4];
                    ent[11] = WriterBasedGenerator.HEX_CHARS[hi2 & 0xF];
                    ent[12] = WriterBasedGenerator.HEX_CHARS[lo >> 4];
                    ent[13] = WriterBasedGenerator.HEX_CHARS[lo & 0xF];
                    this._writer.write(ent, 8, 6);
                }
                else {
                    ent[6] = WriterBasedGenerator.HEX_CHARS[ch >> 4];
                    ent[7] = WriterBasedGenerator.HEX_CHARS[ch & '\u000f'];
                    this._writer.write(ent, 2, 6);
                }
            }
            return ptr;
        }
        String escape;
        if (this._currentEscape == null) {
            escape = this._characterEscapes.getEscapeSequence(ch).getValue();
        }
        else {
            escape = this._currentEscape.getValue();
            this._currentEscape = null;
        }
        final int len = escape.length();
        if (ptr >= len && ptr < end) {
            ptr -= len;
            escape.getChars(0, len, buffer, ptr);
        }
        else {
            this._writer.write(escape);
        }
        return ptr;
    }
    
    private final void _appendCharacterEscape(char ch, final int escCode) throws IOException, JsonGenerationException {
        if (escCode >= 0) {
            if (this._outputTail + 2 > this._outputEnd) {
                this._flushBuffer();
            }
            this._outputBuffer[this._outputTail++] = '\\';
            this._outputBuffer[this._outputTail++] = (char)escCode;
            return;
        }
        if (escCode != -2) {
            if (this._outputTail + 2 > this._outputEnd) {
                this._flushBuffer();
            }
            int ptr = this._outputTail;
            final char[] buf = this._outputBuffer;
            buf[ptr++] = '\\';
            buf[ptr++] = 'u';
            if (ch > '\u00ff') {
                final int hi = ch >> 8 & 0xFF;
                buf[ptr++] = WriterBasedGenerator.HEX_CHARS[hi >> 4];
                buf[ptr++] = WriterBasedGenerator.HEX_CHARS[hi & 0xF];
                ch &= '\u00ff';
            }
            else {
                buf[ptr++] = '0';
                buf[ptr++] = '0';
            }
            buf[ptr++] = WriterBasedGenerator.HEX_CHARS[ch >> 4];
            buf[ptr] = WriterBasedGenerator.HEX_CHARS[ch & '\u000f'];
            this._outputTail = ptr;
            return;
        }
        String escape;
        if (this._currentEscape == null) {
            escape = this._characterEscapes.getEscapeSequence(ch).getValue();
        }
        else {
            escape = this._currentEscape.getValue();
            this._currentEscape = null;
        }
        final int len = escape.length();
        if (this._outputTail + len > this._outputEnd) {
            this._flushBuffer();
            if (len > this._outputEnd) {
                this._writer.write(escape);
                return;
            }
        }
        escape.getChars(0, len, this._outputBuffer, this._outputTail);
        this._outputTail += len;
    }
    
    private char[] _allocateEntityBuffer() {
        final char[] buf = { '\\', '\0', '\\', 'u', '0', '0', '\0', '\0', '\\', 'u', '\0', '\0', '\0', '\0' };
        return this._entityBuffer = buf;
    }
    
    protected final void _flushBuffer() throws IOException {
        final int len = this._outputTail - this._outputHead;
        if (len > 0) {
            final int offset = this._outputHead;
            final int n = 0;
            this._outputHead = n;
            this._outputTail = n;
            this._writer.write(this._outputBuffer, offset, len);
        }
    }
    
    static {
        HEX_CHARS = CharTypes.copyHexChars();
        sOutputEscapes = CharTypes.get7BitOutputEscapes();
    }
}
