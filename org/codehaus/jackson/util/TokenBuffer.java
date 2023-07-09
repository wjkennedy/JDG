// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.util;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.impl.JsonReadContext;
import org.codehaus.jackson.impl.JsonParserMinimalBase;
import org.codehaus.jackson.JsonStreamContext;
import org.codehaus.jackson.Base64Variant;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.io.SerializedString;
import java.io.IOException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.JsonGenerationException;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.codehaus.jackson.SerializableString;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.impl.JsonWriteContext;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.JsonGenerator;

public class TokenBuffer extends JsonGenerator
{
    protected static final int DEFAULT_PARSER_FEATURES;
    protected ObjectCodec _objectCodec;
    protected int _generatorFeatures;
    protected boolean _closed;
    protected Segment _first;
    protected Segment _last;
    protected int _appendOffset;
    protected JsonWriteContext _writeContext;
    
    public TokenBuffer(final ObjectCodec codec) {
        this._objectCodec = codec;
        this._generatorFeatures = TokenBuffer.DEFAULT_PARSER_FEATURES;
        this._writeContext = JsonWriteContext.createRootContext();
        final Segment segment = new Segment();
        this._last = segment;
        this._first = segment;
        this._appendOffset = 0;
    }
    
    public JsonParser asParser() {
        return this.asParser(this._objectCodec);
    }
    
    public JsonParser asParser(final ObjectCodec codec) {
        return new Parser(this._first, codec);
    }
    
    public JsonParser asParser(final JsonParser src) {
        final Parser p = new Parser(this._first, src.getCodec());
        p.setLocation(src.getTokenLocation());
        return p;
    }
    
    public void serialize(final JsonGenerator jgen) throws IOException, JsonGenerationException {
        Segment segment = this._first;
        int ptr = -1;
        while (true) {
            if (++ptr >= 16) {
                ptr = 0;
                segment = segment.next();
                if (segment == null) {
                    break;
                }
            }
            final JsonToken t = segment.type(ptr);
            if (t == null) {
                break;
            }
            switch (t) {
                case START_OBJECT: {
                    jgen.writeStartObject();
                    continue;
                }
                case END_OBJECT: {
                    jgen.writeEndObject();
                    continue;
                }
                case START_ARRAY: {
                    jgen.writeStartArray();
                    continue;
                }
                case END_ARRAY: {
                    jgen.writeEndArray();
                    continue;
                }
                case FIELD_NAME: {
                    final Object ob = segment.get(ptr);
                    if (ob instanceof SerializableString) {
                        jgen.writeFieldName((SerializableString)ob);
                    }
                    else {
                        jgen.writeFieldName((String)ob);
                    }
                    continue;
                }
                case VALUE_STRING: {
                    final Object ob = segment.get(ptr);
                    if (ob instanceof SerializableString) {
                        jgen.writeString((SerializableString)ob);
                    }
                    else {
                        jgen.writeString((String)ob);
                    }
                    continue;
                }
                case VALUE_NUMBER_INT: {
                    final Number n = (Number)segment.get(ptr);
                    if (n instanceof BigInteger) {
                        jgen.writeNumber((BigInteger)n);
                    }
                    else if (n instanceof Long) {
                        jgen.writeNumber(n.longValue());
                    }
                    else {
                        jgen.writeNumber(n.intValue());
                    }
                    continue;
                }
                case VALUE_NUMBER_FLOAT: {
                    final Object n2 = segment.get(ptr);
                    if (n2 instanceof BigDecimal) {
                        jgen.writeNumber((BigDecimal)n2);
                    }
                    else if (n2 instanceof Float) {
                        jgen.writeNumber((float)n2);
                    }
                    else if (n2 instanceof Double) {
                        jgen.writeNumber((double)n2);
                    }
                    else if (n2 == null) {
                        jgen.writeNull();
                    }
                    else {
                        if (!(n2 instanceof String)) {
                            throw new JsonGenerationException("Unrecognized value type for VALUE_NUMBER_FLOAT: " + n2.getClass().getName() + ", can not serialize");
                        }
                        jgen.writeNumber((String)n2);
                    }
                    continue;
                }
                case VALUE_TRUE: {
                    jgen.writeBoolean(true);
                    continue;
                }
                case VALUE_FALSE: {
                    jgen.writeBoolean(false);
                    continue;
                }
                case VALUE_NULL: {
                    jgen.writeNull();
                    continue;
                }
                case VALUE_EMBEDDED_OBJECT: {
                    jgen.writeObject(segment.get(ptr));
                    continue;
                }
                default: {
                    throw new RuntimeException("Internal error: should never end up through this code path");
                }
            }
        }
    }
    
    @Override
    public String toString() {
        final int MAX_COUNT = 100;
        final StringBuilder sb = new StringBuilder();
        sb.append("[TokenBuffer: ");
        final JsonParser jp = this.asParser();
        int count = 0;
        while (true) {
            JsonToken t;
            try {
                t = jp.nextToken();
            }
            catch (final IOException ioe) {
                throw new IllegalStateException(ioe);
            }
            if (t == null) {
                break;
            }
            if (count < 100) {
                if (count > 0) {
                    sb.append(", ");
                }
                sb.append(t.toString());
            }
            ++count;
        }
        if (count >= 100) {
            sb.append(" ... (truncated ").append(count - 100).append(" entries)");
        }
        sb.append(']');
        return sb.toString();
    }
    
    @Override
    public JsonGenerator enable(final Feature f) {
        this._generatorFeatures |= f.getMask();
        return this;
    }
    
    @Override
    public JsonGenerator disable(final Feature f) {
        this._generatorFeatures &= ~f.getMask();
        return this;
    }
    
    @Override
    public boolean isEnabled(final Feature f) {
        return (this._generatorFeatures & f.getMask()) != 0x0;
    }
    
    @Override
    public JsonGenerator useDefaultPrettyPrinter() {
        return this;
    }
    
    @Override
    public JsonGenerator setCodec(final ObjectCodec oc) {
        this._objectCodec = oc;
        return this;
    }
    
    @Override
    public ObjectCodec getCodec() {
        return this._objectCodec;
    }
    
    @Override
    public final JsonWriteContext getOutputContext() {
        return this._writeContext;
    }
    
    @Override
    public void flush() throws IOException {
    }
    
    @Override
    public void close() throws IOException {
        this._closed = true;
    }
    
    @Override
    public boolean isClosed() {
        return this._closed;
    }
    
    @Override
    public final void writeStartArray() throws IOException, JsonGenerationException {
        this._append(JsonToken.START_ARRAY);
        this._writeContext = this._writeContext.createChildArrayContext();
    }
    
    @Override
    public final void writeEndArray() throws IOException, JsonGenerationException {
        this._append(JsonToken.END_ARRAY);
        final JsonWriteContext c = this._writeContext.getParent();
        if (c != null) {
            this._writeContext = c;
        }
    }
    
    @Override
    public final void writeStartObject() throws IOException, JsonGenerationException {
        this._append(JsonToken.START_OBJECT);
        this._writeContext = this._writeContext.createChildObjectContext();
    }
    
    @Override
    public final void writeEndObject() throws IOException, JsonGenerationException {
        this._append(JsonToken.END_OBJECT);
        final JsonWriteContext c = this._writeContext.getParent();
        if (c != null) {
            this._writeContext = c;
        }
    }
    
    @Override
    public final void writeFieldName(final String name) throws IOException, JsonGenerationException {
        this._append(JsonToken.FIELD_NAME, name);
        this._writeContext.writeFieldName(name);
    }
    
    @Override
    public void writeFieldName(final SerializableString name) throws IOException, JsonGenerationException {
        this._append(JsonToken.FIELD_NAME, name);
        this._writeContext.writeFieldName(name.getValue());
    }
    
    @Override
    public void writeFieldName(final SerializedString name) throws IOException, JsonGenerationException {
        this._append(JsonToken.FIELD_NAME, name);
        this._writeContext.writeFieldName(name.getValue());
    }
    
    @Override
    public void writeString(final String text) throws IOException, JsonGenerationException {
        if (text == null) {
            this.writeNull();
        }
        else {
            this._append(JsonToken.VALUE_STRING, text);
        }
    }
    
    @Override
    public void writeString(final char[] text, final int offset, final int len) throws IOException, JsonGenerationException {
        this.writeString(new String(text, offset, len));
    }
    
    @Override
    public void writeString(final SerializableString text) throws IOException, JsonGenerationException {
        if (text == null) {
            this.writeNull();
        }
        else {
            this._append(JsonToken.VALUE_STRING, text);
        }
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
        this._reportUnsupportedOperation();
    }
    
    @Override
    public void writeRaw(final String text, final int offset, final int len) throws IOException, JsonGenerationException {
        this._reportUnsupportedOperation();
    }
    
    @Override
    public void writeRaw(final char[] text, final int offset, final int len) throws IOException, JsonGenerationException {
        this._reportUnsupportedOperation();
    }
    
    @Override
    public void writeRaw(final char c) throws IOException, JsonGenerationException {
        this._reportUnsupportedOperation();
    }
    
    @Override
    public void writeRawValue(final String text) throws IOException, JsonGenerationException {
        this._reportUnsupportedOperation();
    }
    
    @Override
    public void writeRawValue(final String text, final int offset, final int len) throws IOException, JsonGenerationException {
        this._reportUnsupportedOperation();
    }
    
    @Override
    public void writeRawValue(final char[] text, final int offset, final int len) throws IOException, JsonGenerationException {
        this._reportUnsupportedOperation();
    }
    
    @Override
    public void writeNumber(final int i) throws IOException, JsonGenerationException {
        this._append(JsonToken.VALUE_NUMBER_INT, i);
    }
    
    @Override
    public void writeNumber(final long l) throws IOException, JsonGenerationException {
        this._append(JsonToken.VALUE_NUMBER_INT, l);
    }
    
    @Override
    public void writeNumber(final double d) throws IOException, JsonGenerationException {
        this._append(JsonToken.VALUE_NUMBER_FLOAT, d);
    }
    
    @Override
    public void writeNumber(final float f) throws IOException, JsonGenerationException {
        this._append(JsonToken.VALUE_NUMBER_FLOAT, f);
    }
    
    @Override
    public void writeNumber(final BigDecimal dec) throws IOException, JsonGenerationException {
        if (dec == null) {
            this.writeNull();
        }
        else {
            this._append(JsonToken.VALUE_NUMBER_FLOAT, dec);
        }
    }
    
    @Override
    public void writeNumber(final BigInteger v) throws IOException, JsonGenerationException {
        if (v == null) {
            this.writeNull();
        }
        else {
            this._append(JsonToken.VALUE_NUMBER_INT, v);
        }
    }
    
    @Override
    public void writeNumber(final String encodedValue) throws IOException, JsonGenerationException {
        this._append(JsonToken.VALUE_NUMBER_FLOAT, encodedValue);
    }
    
    @Override
    public void writeBoolean(final boolean state) throws IOException, JsonGenerationException {
        this._append(state ? JsonToken.VALUE_TRUE : JsonToken.VALUE_FALSE);
    }
    
    @Override
    public void writeNull() throws IOException, JsonGenerationException {
        this._append(JsonToken.VALUE_NULL);
    }
    
    @Override
    public void writeObject(final Object value) throws IOException, JsonProcessingException {
        this._append(JsonToken.VALUE_EMBEDDED_OBJECT, value);
    }
    
    @Override
    public void writeTree(final JsonNode rootNode) throws IOException, JsonProcessingException {
        this._append(JsonToken.VALUE_EMBEDDED_OBJECT, rootNode);
    }
    
    @Override
    public void writeBinary(final Base64Variant b64variant, final byte[] data, final int offset, final int len) throws IOException, JsonGenerationException {
        final byte[] copy = new byte[len];
        System.arraycopy(data, offset, copy, 0, len);
        this.writeObject(copy);
    }
    
    @Override
    public void copyCurrentEvent(final JsonParser jp) throws IOException, JsonProcessingException {
        Label_0333: {
            switch (jp.getCurrentToken()) {
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
                            break Label_0333;
                        }
                        case BIG_INTEGER: {
                            this.writeNumber(jp.getBigIntegerValue());
                            break Label_0333;
                        }
                        default: {
                            this.writeNumber(jp.getLongValue());
                            break Label_0333;
                        }
                    }
                    break;
                }
                case VALUE_NUMBER_FLOAT: {
                    switch (jp.getNumberType()) {
                        case BIG_DECIMAL: {
                            this.writeNumber(jp.getDecimalValue());
                            break Label_0333;
                        }
                        case FLOAT: {
                            this.writeNumber(jp.getFloatValue());
                            break Label_0333;
                        }
                        default: {
                            this.writeNumber(jp.getDoubleValue());
                            break Label_0333;
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
                    throw new RuntimeException("Internal error: should never end up through this code path");
                }
            }
        }
    }
    
    @Override
    public void copyCurrentStructure(final JsonParser jp) throws IOException, JsonProcessingException {
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
    
    protected final void _append(final JsonToken type) {
        final Segment next = this._last.append(this._appendOffset, type);
        if (next == null) {
            ++this._appendOffset;
        }
        else {
            this._last = next;
            this._appendOffset = 1;
        }
    }
    
    protected final void _append(final JsonToken type, final Object value) {
        final Segment next = this._last.append(this._appendOffset, type, value);
        if (next == null) {
            ++this._appendOffset;
        }
        else {
            this._last = next;
            this._appendOffset = 1;
        }
    }
    
    protected void _reportUnsupportedOperation() {
        throw new UnsupportedOperationException("Called operation not supported for TokenBuffer");
    }
    
    static {
        DEFAULT_PARSER_FEATURES = JsonParser.Feature.collectDefaults();
    }
    
    protected static final class Parser extends JsonParserMinimalBase
    {
        protected ObjectCodec _codec;
        protected Segment _segment;
        protected int _segmentPtr;
        protected JsonReadContext _parsingContext;
        protected boolean _closed;
        protected transient ByteArrayBuilder _byteBuilder;
        protected JsonLocation _location;
        
        public Parser(final Segment firstSeg, final ObjectCodec codec) {
            super(0);
            this._location = null;
            this._segment = firstSeg;
            this._segmentPtr = -1;
            this._codec = codec;
            this._parsingContext = JsonReadContext.createRootContext(-1, -1);
        }
        
        public void setLocation(final JsonLocation l) {
            this._location = l;
        }
        
        @Override
        public ObjectCodec getCodec() {
            return this._codec;
        }
        
        @Override
        public void setCodec(final ObjectCodec c) {
            this._codec = c;
        }
        
        public JsonToken peekNextToken() throws IOException, JsonParseException {
            if (this._closed) {
                return null;
            }
            Segment seg = this._segment;
            int ptr = this._segmentPtr + 1;
            if (ptr >= 16) {
                ptr = 0;
                seg = ((seg == null) ? null : seg.next());
            }
            return (seg == null) ? null : seg.type(ptr);
        }
        
        @Override
        public void close() throws IOException {
            if (!this._closed) {
                this._closed = true;
            }
        }
        
        @Override
        public JsonToken nextToken() throws IOException, JsonParseException {
            if (this._closed || this._segment == null) {
                return null;
            }
            if (++this._segmentPtr >= 16) {
                this._segmentPtr = 0;
                this._segment = this._segment.next();
                if (this._segment == null) {
                    return null;
                }
            }
            this._currToken = this._segment.type(this._segmentPtr);
            if (this._currToken == JsonToken.FIELD_NAME) {
                final Object ob = this._currentObject();
                final String name = (String)((ob instanceof String) ? ob : ob.toString());
                this._parsingContext.setCurrentName(name);
            }
            else if (this._currToken == JsonToken.START_OBJECT) {
                this._parsingContext = this._parsingContext.createChildObjectContext(-1, -1);
            }
            else if (this._currToken == JsonToken.START_ARRAY) {
                this._parsingContext = this._parsingContext.createChildArrayContext(-1, -1);
            }
            else if (this._currToken == JsonToken.END_OBJECT || this._currToken == JsonToken.END_ARRAY) {
                this._parsingContext = this._parsingContext.getParent();
                if (this._parsingContext == null) {
                    this._parsingContext = JsonReadContext.createRootContext(-1, -1);
                }
            }
            return this._currToken;
        }
        
        @Override
        public boolean isClosed() {
            return this._closed;
        }
        
        @Override
        public JsonStreamContext getParsingContext() {
            return this._parsingContext;
        }
        
        @Override
        public JsonLocation getTokenLocation() {
            return this.getCurrentLocation();
        }
        
        @Override
        public JsonLocation getCurrentLocation() {
            return (this._location == null) ? JsonLocation.NA : this._location;
        }
        
        @Override
        public String getCurrentName() {
            return this._parsingContext.getCurrentName();
        }
        
        @Override
        public String getText() {
            if (this._currToken == JsonToken.VALUE_STRING || this._currToken == JsonToken.FIELD_NAME) {
                final Object ob = this._currentObject();
                if (ob instanceof String) {
                    return (String)ob;
                }
                return (ob == null) ? null : ob.toString();
            }
            else {
                if (this._currToken == null) {
                    return null;
                }
                switch (this._currToken) {
                    case VALUE_NUMBER_INT:
                    case VALUE_NUMBER_FLOAT: {
                        final Object ob = this._currentObject();
                        return (ob == null) ? null : ob.toString();
                    }
                    default: {
                        return this._currToken.asString();
                    }
                }
            }
        }
        
        @Override
        public char[] getTextCharacters() {
            final String str = this.getText();
            return (char[])((str == null) ? null : str.toCharArray());
        }
        
        @Override
        public int getTextLength() {
            final String str = this.getText();
            return (str == null) ? 0 : str.length();
        }
        
        @Override
        public int getTextOffset() {
            return 0;
        }
        
        @Override
        public boolean hasTextCharacters() {
            return false;
        }
        
        @Override
        public BigInteger getBigIntegerValue() throws IOException, JsonParseException {
            final Number n = this.getNumberValue();
            if (n instanceof BigInteger) {
                return (BigInteger)n;
            }
            switch (this.getNumberType()) {
                case BIG_DECIMAL: {
                    return ((BigDecimal)n).toBigInteger();
                }
                default: {
                    return BigInteger.valueOf(n.longValue());
                }
            }
        }
        
        @Override
        public BigDecimal getDecimalValue() throws IOException, JsonParseException {
            final Number n = this.getNumberValue();
            if (n instanceof BigDecimal) {
                return (BigDecimal)n;
            }
            switch (this.getNumberType()) {
                case INT:
                case LONG: {
                    return BigDecimal.valueOf(n.longValue());
                }
                case BIG_INTEGER: {
                    return new BigDecimal((BigInteger)n);
                }
                default: {
                    return BigDecimal.valueOf(n.doubleValue());
                }
            }
        }
        
        @Override
        public double getDoubleValue() throws IOException, JsonParseException {
            return this.getNumberValue().doubleValue();
        }
        
        @Override
        public float getFloatValue() throws IOException, JsonParseException {
            return this.getNumberValue().floatValue();
        }
        
        @Override
        public int getIntValue() throws IOException, JsonParseException {
            if (this._currToken == JsonToken.VALUE_NUMBER_INT) {
                return ((Number)this._currentObject()).intValue();
            }
            return this.getNumberValue().intValue();
        }
        
        @Override
        public long getLongValue() throws IOException, JsonParseException {
            return this.getNumberValue().longValue();
        }
        
        @Override
        public NumberType getNumberType() throws IOException, JsonParseException {
            final Number n = this.getNumberValue();
            if (n instanceof Integer) {
                return NumberType.INT;
            }
            if (n instanceof Long) {
                return NumberType.LONG;
            }
            if (n instanceof Double) {
                return NumberType.DOUBLE;
            }
            if (n instanceof BigDecimal) {
                return NumberType.BIG_DECIMAL;
            }
            if (n instanceof Float) {
                return NumberType.FLOAT;
            }
            if (n instanceof BigInteger) {
                return NumberType.BIG_INTEGER;
            }
            return null;
        }
        
        @Override
        public final Number getNumberValue() throws IOException, JsonParseException {
            this._checkIsNumber();
            return (Number)this._currentObject();
        }
        
        @Override
        public Object getEmbeddedObject() {
            if (this._currToken == JsonToken.VALUE_EMBEDDED_OBJECT) {
                return this._currentObject();
            }
            return null;
        }
        
        @Override
        public byte[] getBinaryValue(final Base64Variant b64variant) throws IOException, JsonParseException {
            if (this._currToken == JsonToken.VALUE_EMBEDDED_OBJECT) {
                final Object ob = this._currentObject();
                if (ob instanceof byte[]) {
                    return (byte[])ob;
                }
            }
            if (this._currToken != JsonToken.VALUE_STRING) {
                throw this._constructError("Current token (" + this._currToken + ") not VALUE_STRING (or VALUE_EMBEDDED_OBJECT with byte[]), can not access as binary");
            }
            final String str = this.getText();
            if (str == null) {
                return null;
            }
            ByteArrayBuilder builder = this._byteBuilder;
            if (builder == null) {
                builder = (this._byteBuilder = new ByteArrayBuilder(100));
            }
            else {
                this._byteBuilder.reset();
            }
            this._decodeBase64(str, builder, b64variant);
            return builder.toByteArray();
        }
        
        protected final Object _currentObject() {
            return this._segment.get(this._segmentPtr);
        }
        
        protected final void _checkIsNumber() throws JsonParseException {
            if (this._currToken == null || !this._currToken.isNumeric()) {
                throw this._constructError("Current token (" + this._currToken + ") not numeric, can not use numeric value accessors");
            }
        }
        
        @Override
        protected void _handleEOF() throws JsonParseException {
            this._throwInternal();
        }
    }
    
    protected static final class Segment
    {
        public static final int TOKENS_PER_SEGMENT = 16;
        private static final JsonToken[] TOKEN_TYPES_BY_INDEX;
        protected Segment _next;
        protected long _tokenTypes;
        protected final Object[] _tokens;
        
        public Segment() {
            this._tokens = new Object[16];
        }
        
        public JsonToken type(final int index) {
            long l = this._tokenTypes;
            if (index > 0) {
                l >>= index << 2;
            }
            final int ix = (int)l & 0xF;
            return Segment.TOKEN_TYPES_BY_INDEX[ix];
        }
        
        public Object get(final int index) {
            return this._tokens[index];
        }
        
        public Segment next() {
            return this._next;
        }
        
        public Segment append(final int index, final JsonToken tokenType) {
            if (index < 16) {
                this.set(index, tokenType);
                return null;
            }
            (this._next = new Segment()).set(0, tokenType);
            return this._next;
        }
        
        public Segment append(final int index, final JsonToken tokenType, final Object value) {
            if (index < 16) {
                this.set(index, tokenType, value);
                return null;
            }
            (this._next = new Segment()).set(0, tokenType, value);
            return this._next;
        }
        
        public void set(final int index, final JsonToken tokenType) {
            long typeCode = tokenType.ordinal();
            if (index > 0) {
                typeCode <<= index << 2;
            }
            this._tokenTypes |= typeCode;
        }
        
        public void set(final int index, final JsonToken tokenType, final Object value) {
            this._tokens[index] = value;
            long typeCode = tokenType.ordinal();
            if (index > 0) {
                typeCode <<= index << 2;
            }
            this._tokenTypes |= typeCode;
        }
        
        static {
            TOKEN_TYPES_BY_INDEX = new JsonToken[16];
            final JsonToken[] t = JsonToken.values();
            System.arraycopy(t, 1, Segment.TOKEN_TYPES_BY_INDEX, 1, Math.min(15, t.length - 1));
        }
    }
}
