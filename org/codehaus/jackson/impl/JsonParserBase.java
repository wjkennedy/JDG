// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.impl;

import org.codehaus.jackson.JsonStreamContext;
import org.codehaus.jackson.Base64Variant;
import org.codehaus.jackson.io.NumberInput;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonParseException;
import java.io.IOException;
import org.codehaus.jackson.util.VersionUtil;
import org.codehaus.jackson.Version;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.codehaus.jackson.util.ByteArrayBuilder;
import org.codehaus.jackson.util.TextBuffer;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.io.IOContext;

public abstract class JsonParserBase extends JsonParserMinimalBase
{
    protected final IOContext _ioContext;
    protected boolean _closed;
    protected int _inputPtr;
    protected int _inputEnd;
    protected long _currInputProcessed;
    protected int _currInputRow;
    protected int _currInputRowStart;
    protected long _tokenInputTotal;
    protected int _tokenInputRow;
    protected int _tokenInputCol;
    protected JsonReadContext _parsingContext;
    protected JsonToken _nextToken;
    protected final TextBuffer _textBuffer;
    protected char[] _nameCopyBuffer;
    protected boolean _nameCopied;
    protected ByteArrayBuilder _byteArrayBuilder;
    protected byte[] _binaryValue;
    protected static final int NR_UNKNOWN = 0;
    protected static final int NR_INT = 1;
    protected static final int NR_LONG = 2;
    protected static final int NR_BIGINT = 4;
    protected static final int NR_DOUBLE = 8;
    protected static final int NR_BIGDECIMAL = 16;
    static final BigInteger BI_MIN_INT;
    static final BigInteger BI_MAX_INT;
    static final BigInteger BI_MIN_LONG;
    static final BigInteger BI_MAX_LONG;
    static final BigDecimal BD_MIN_LONG;
    static final BigDecimal BD_MAX_LONG;
    static final BigDecimal BD_MIN_INT;
    static final BigDecimal BD_MAX_INT;
    static final long MIN_INT_L = -2147483648L;
    static final long MAX_INT_L = 2147483647L;
    static final double MIN_LONG_D = -9.223372036854776E18;
    static final double MAX_LONG_D = 9.223372036854776E18;
    static final double MIN_INT_D = -2.147483648E9;
    static final double MAX_INT_D = 2.147483647E9;
    protected static final int INT_0 = 48;
    protected static final int INT_1 = 49;
    protected static final int INT_2 = 50;
    protected static final int INT_3 = 51;
    protected static final int INT_4 = 52;
    protected static final int INT_5 = 53;
    protected static final int INT_6 = 54;
    protected static final int INT_7 = 55;
    protected static final int INT_8 = 56;
    protected static final int INT_9 = 57;
    protected static final int INT_MINUS = 45;
    protected static final int INT_PLUS = 43;
    protected static final int INT_DECIMAL_POINT = 46;
    protected static final int INT_e = 101;
    protected static final int INT_E = 69;
    protected static final char CHAR_NULL = '\0';
    protected int _numTypesValid;
    protected int _numberInt;
    protected long _numberLong;
    protected double _numberDouble;
    protected BigInteger _numberBigInt;
    protected BigDecimal _numberBigDecimal;
    protected boolean _numberNegative;
    protected int _intLength;
    protected int _fractLength;
    protected int _expLength;
    
    protected JsonParserBase(final IOContext ctxt, final int features) {
        this._inputPtr = 0;
        this._inputEnd = 0;
        this._currInputProcessed = 0L;
        this._currInputRow = 1;
        this._currInputRowStart = 0;
        this._tokenInputTotal = 0L;
        this._tokenInputRow = 1;
        this._tokenInputCol = 0;
        this._nameCopyBuffer = null;
        this._nameCopied = false;
        this._byteArrayBuilder = null;
        this._numTypesValid = 0;
        this._features = features;
        this._ioContext = ctxt;
        this._textBuffer = ctxt.constructTextBuffer();
        this._parsingContext = JsonReadContext.createRootContext();
    }
    
    @Override
    public Version version() {
        return VersionUtil.versionFor(this.getClass());
    }
    
    @Override
    public String getCurrentName() throws IOException, JsonParseException {
        if (this._currToken == JsonToken.START_OBJECT || this._currToken == JsonToken.START_ARRAY) {
            final JsonReadContext parent = this._parsingContext.getParent();
            return parent.getCurrentName();
        }
        return this._parsingContext.getCurrentName();
    }
    
    @Override
    public void close() throws IOException {
        if (!this._closed) {
            this._closed = true;
            try {
                this._closeInput();
            }
            finally {
                this._releaseBuffers();
            }
        }
    }
    
    @Override
    public boolean isClosed() {
        return this._closed;
    }
    
    @Override
    public JsonReadContext getParsingContext() {
        return this._parsingContext;
    }
    
    @Override
    public JsonLocation getTokenLocation() {
        return new JsonLocation(this._ioContext.getSourceReference(), this.getTokenCharacterOffset(), this.getTokenLineNr(), this.getTokenColumnNr());
    }
    
    @Override
    public JsonLocation getCurrentLocation() {
        final int col = this._inputPtr - this._currInputRowStart + 1;
        return new JsonLocation(this._ioContext.getSourceReference(), this._currInputProcessed + this._inputPtr - 1L, this._currInputRow, col);
    }
    
    @Override
    public boolean hasTextCharacters() {
        return this._currToken == JsonToken.VALUE_STRING || (this._currToken == JsonToken.FIELD_NAME && this._nameCopied);
    }
    
    public final long getTokenCharacterOffset() {
        return this._tokenInputTotal;
    }
    
    public final int getTokenLineNr() {
        return this._tokenInputRow;
    }
    
    public final int getTokenColumnNr() {
        final int col = this._tokenInputCol;
        return (col < 0) ? col : (col + 1);
    }
    
    protected final void loadMoreGuaranteed() throws IOException {
        if (!this.loadMore()) {
            this._reportInvalidEOF();
        }
    }
    
    protected abstract boolean loadMore() throws IOException;
    
    protected abstract void _finishString() throws IOException, JsonParseException;
    
    protected abstract void _closeInput() throws IOException;
    
    protected void _releaseBuffers() throws IOException {
        this._textBuffer.releaseBuffers();
        final char[] buf = this._nameCopyBuffer;
        if (buf != null) {
            this._nameCopyBuffer = null;
            this._ioContext.releaseNameCopyBuffer(buf);
        }
    }
    
    @Override
    protected void _handleEOF() throws JsonParseException {
        if (!this._parsingContext.inRoot()) {
            this._reportInvalidEOF(": expected close marker for " + this._parsingContext.getTypeDesc() + " (from " + this._parsingContext.getStartLocation(this._ioContext.getSourceReference()) + ")");
        }
    }
    
    protected void _reportMismatchedEndMarker(final int actCh, final char expCh) throws JsonParseException {
        final String startDesc = "" + this._parsingContext.getStartLocation(this._ioContext.getSourceReference());
        this._reportError("Unexpected close marker '" + (char)actCh + "': expected '" + expCh + "' (for " + this._parsingContext.getTypeDesc() + " starting at " + startDesc + ")");
    }
    
    public ByteArrayBuilder _getByteArrayBuilder() {
        if (this._byteArrayBuilder == null) {
            this._byteArrayBuilder = new ByteArrayBuilder();
        }
        else {
            this._byteArrayBuilder.reset();
        }
        return this._byteArrayBuilder;
    }
    
    protected final JsonToken reset(final boolean negative, final int intLen, final int fractLen, final int expLen) {
        if (fractLen < 1 && expLen < 1) {
            return this.resetInt(negative, intLen);
        }
        return this.resetFloat(negative, intLen, fractLen, expLen);
    }
    
    protected final JsonToken resetInt(final boolean negative, final int intLen) {
        this._numberNegative = negative;
        this._intLength = intLen;
        this._fractLength = 0;
        this._expLength = 0;
        this._numTypesValid = 0;
        return JsonToken.VALUE_NUMBER_INT;
    }
    
    protected final JsonToken resetFloat(final boolean negative, final int intLen, final int fractLen, final int expLen) {
        this._numberNegative = negative;
        this._intLength = intLen;
        this._fractLength = fractLen;
        this._expLength = expLen;
        this._numTypesValid = 0;
        return JsonToken.VALUE_NUMBER_FLOAT;
    }
    
    protected final JsonToken resetAsNaN(final String valueStr, final double value) {
        this._textBuffer.resetWithString(valueStr);
        this._numberDouble = value;
        this._numTypesValid = 8;
        return JsonToken.VALUE_NUMBER_FLOAT;
    }
    
    @Override
    public Number getNumberValue() throws IOException, JsonParseException {
        if (this._numTypesValid == 0) {
            this._parseNumericValue(0);
        }
        if (this._currToken == JsonToken.VALUE_NUMBER_INT) {
            if ((this._numTypesValid & 0x1) != 0x0) {
                return this._numberInt;
            }
            if ((this._numTypesValid & 0x2) != 0x0) {
                return this._numberLong;
            }
            if ((this._numTypesValid & 0x4) != 0x0) {
                return this._numberBigInt;
            }
            return this._numberBigDecimal;
        }
        else {
            if ((this._numTypesValid & 0x10) != 0x0) {
                return this._numberBigDecimal;
            }
            if ((this._numTypesValid & 0x8) == 0x0) {
                this._throwInternal();
            }
            return this._numberDouble;
        }
    }
    
    @Override
    public NumberType getNumberType() throws IOException, JsonParseException {
        if (this._numTypesValid == 0) {
            this._parseNumericValue(0);
        }
        if (this._currToken == JsonToken.VALUE_NUMBER_INT) {
            if ((this._numTypesValid & 0x1) != 0x0) {
                return NumberType.INT;
            }
            if ((this._numTypesValid & 0x2) != 0x0) {
                return NumberType.LONG;
            }
            return NumberType.BIG_INTEGER;
        }
        else {
            if ((this._numTypesValid & 0x10) != 0x0) {
                return NumberType.BIG_DECIMAL;
            }
            return NumberType.DOUBLE;
        }
    }
    
    @Override
    public int getIntValue() throws IOException, JsonParseException {
        if ((this._numTypesValid & 0x1) == 0x0) {
            if (this._numTypesValid == 0) {
                this._parseNumericValue(1);
            }
            if ((this._numTypesValid & 0x1) == 0x0) {
                this.convertNumberToInt();
            }
        }
        return this._numberInt;
    }
    
    @Override
    public long getLongValue() throws IOException, JsonParseException {
        if ((this._numTypesValid & 0x2) == 0x0) {
            if (this._numTypesValid == 0) {
                this._parseNumericValue(2);
            }
            if ((this._numTypesValid & 0x2) == 0x0) {
                this.convertNumberToLong();
            }
        }
        return this._numberLong;
    }
    
    @Override
    public BigInteger getBigIntegerValue() throws IOException, JsonParseException {
        if ((this._numTypesValid & 0x4) == 0x0) {
            if (this._numTypesValid == 0) {
                this._parseNumericValue(4);
            }
            if ((this._numTypesValid & 0x4) == 0x0) {
                this.convertNumberToBigInteger();
            }
        }
        return this._numberBigInt;
    }
    
    @Override
    public float getFloatValue() throws IOException, JsonParseException {
        final double value = this.getDoubleValue();
        return (float)value;
    }
    
    @Override
    public double getDoubleValue() throws IOException, JsonParseException {
        if ((this._numTypesValid & 0x8) == 0x0) {
            if (this._numTypesValid == 0) {
                this._parseNumericValue(8);
            }
            if ((this._numTypesValid & 0x8) == 0x0) {
                this.convertNumberToDouble();
            }
        }
        return this._numberDouble;
    }
    
    @Override
    public BigDecimal getDecimalValue() throws IOException, JsonParseException {
        if ((this._numTypesValid & 0x10) == 0x0) {
            if (this._numTypesValid == 0) {
                this._parseNumericValue(16);
            }
            if ((this._numTypesValid & 0x10) == 0x0) {
                this.convertNumberToBigDecimal();
            }
        }
        return this._numberBigDecimal;
    }
    
    protected void _parseNumericValue(final int expType) throws IOException, JsonParseException {
        if (this._currToken == JsonToken.VALUE_NUMBER_INT) {
            final char[] buf = this._textBuffer.getTextBuffer();
            int offset = this._textBuffer.getTextOffset();
            final int len = this._intLength;
            if (this._numberNegative) {
                ++offset;
            }
            if (len <= 9) {
                final int i = NumberInput.parseInt(buf, offset, len);
                this._numberInt = (this._numberNegative ? (-i) : i);
                this._numTypesValid = 1;
                return;
            }
            if (len <= 18) {
                long l = NumberInput.parseLong(buf, offset, len);
                if (this._numberNegative) {
                    l = -l;
                }
                if (len == 10) {
                    if (this._numberNegative) {
                        if (l >= -2147483648L) {
                            this._numberInt = (int)l;
                            this._numTypesValid = 1;
                            return;
                        }
                    }
                    else if (l <= 2147483647L) {
                        this._numberInt = (int)l;
                        this._numTypesValid = 1;
                        return;
                    }
                }
                this._numberLong = l;
                this._numTypesValid = 2;
                return;
            }
            this._parseSlowIntValue(expType, buf, offset, len);
        }
        else {
            if (this._currToken == JsonToken.VALUE_NUMBER_FLOAT) {
                this._parseSlowFloatValue(expType);
                return;
            }
            this._reportError("Current token (" + this._currToken + ") not numeric, can not use numeric value accessors");
        }
    }
    
    private final void _parseSlowFloatValue(final int expType) throws IOException, JsonParseException {
        try {
            if (expType == 16) {
                this._numberBigDecimal = this._textBuffer.contentsAsDecimal();
                this._numTypesValid = 16;
            }
            else {
                this._numberDouble = this._textBuffer.contentsAsDouble();
                this._numTypesValid = 8;
            }
        }
        catch (final NumberFormatException nex) {
            this._wrapError("Malformed numeric value '" + this._textBuffer.contentsAsString() + "'", nex);
        }
    }
    
    private final void _parseSlowIntValue(final int expType, final char[] buf, final int offset, final int len) throws IOException, JsonParseException {
        final String numStr = this._textBuffer.contentsAsString();
        try {
            if (NumberInput.inLongRange(buf, offset, len, this._numberNegative)) {
                this._numberLong = Long.parseLong(numStr);
                this._numTypesValid = 2;
            }
            else {
                this._numberBigInt = new BigInteger(numStr);
                this._numTypesValid = 4;
            }
        }
        catch (final NumberFormatException nex) {
            this._wrapError("Malformed numeric value '" + numStr + "'", nex);
        }
    }
    
    protected void convertNumberToInt() throws IOException, JsonParseException {
        if ((this._numTypesValid & 0x2) != 0x0) {
            final int result = (int)this._numberLong;
            if (result != this._numberLong) {
                this._reportError("Numeric value (" + this.getText() + ") out of range of int");
            }
            this._numberInt = result;
        }
        else if ((this._numTypesValid & 0x4) != 0x0) {
            if (JsonParserBase.BI_MIN_INT.compareTo(this._numberBigInt) > 0 || JsonParserBase.BI_MAX_INT.compareTo(this._numberBigInt) < 0) {
                this.reportOverflowInt();
            }
            this._numberInt = this._numberBigInt.intValue();
        }
        else if ((this._numTypesValid & 0x8) != 0x0) {
            if (this._numberDouble < -2.147483648E9 || this._numberDouble > 2.147483647E9) {
                this.reportOverflowInt();
            }
            this._numberInt = (int)this._numberDouble;
        }
        else if ((this._numTypesValid & 0x10) != 0x0) {
            if (JsonParserBase.BD_MIN_INT.compareTo(this._numberBigDecimal) > 0 || JsonParserBase.BD_MAX_INT.compareTo(this._numberBigDecimal) < 0) {
                this.reportOverflowInt();
            }
            this._numberInt = this._numberBigDecimal.intValue();
        }
        else {
            this._throwInternal();
        }
        this._numTypesValid |= 0x1;
    }
    
    protected void convertNumberToLong() throws IOException, JsonParseException {
        if ((this._numTypesValid & 0x1) != 0x0) {
            this._numberLong = this._numberInt;
        }
        else if ((this._numTypesValid & 0x4) != 0x0) {
            if (JsonParserBase.BI_MIN_LONG.compareTo(this._numberBigInt) > 0 || JsonParserBase.BI_MAX_LONG.compareTo(this._numberBigInt) < 0) {
                this.reportOverflowLong();
            }
            this._numberLong = this._numberBigInt.longValue();
        }
        else if ((this._numTypesValid & 0x8) != 0x0) {
            if (this._numberDouble < -9.223372036854776E18 || this._numberDouble > 9.223372036854776E18) {
                this.reportOverflowLong();
            }
            this._numberLong = (long)this._numberDouble;
        }
        else if ((this._numTypesValid & 0x10) != 0x0) {
            if (JsonParserBase.BD_MIN_LONG.compareTo(this._numberBigDecimal) > 0 || JsonParserBase.BD_MAX_LONG.compareTo(this._numberBigDecimal) < 0) {
                this.reportOverflowLong();
            }
            this._numberLong = this._numberBigDecimal.longValue();
        }
        else {
            this._throwInternal();
        }
        this._numTypesValid |= 0x2;
    }
    
    protected void convertNumberToBigInteger() throws IOException, JsonParseException {
        if ((this._numTypesValid & 0x10) != 0x0) {
            this._numberBigInt = this._numberBigDecimal.toBigInteger();
        }
        else if ((this._numTypesValid & 0x2) != 0x0) {
            this._numberBigInt = BigInteger.valueOf(this._numberLong);
        }
        else if ((this._numTypesValid & 0x1) != 0x0) {
            this._numberBigInt = BigInteger.valueOf(this._numberInt);
        }
        else if ((this._numTypesValid & 0x8) != 0x0) {
            this._numberBigInt = BigDecimal.valueOf(this._numberDouble).toBigInteger();
        }
        else {
            this._throwInternal();
        }
        this._numTypesValid |= 0x4;
    }
    
    protected void convertNumberToDouble() throws IOException, JsonParseException {
        if ((this._numTypesValid & 0x10) != 0x0) {
            this._numberDouble = this._numberBigDecimal.doubleValue();
        }
        else if ((this._numTypesValid & 0x4) != 0x0) {
            this._numberDouble = this._numberBigInt.doubleValue();
        }
        else if ((this._numTypesValid & 0x2) != 0x0) {
            this._numberDouble = (double)this._numberLong;
        }
        else if ((this._numTypesValid & 0x1) != 0x0) {
            this._numberDouble = this._numberInt;
        }
        else {
            this._throwInternal();
        }
        this._numTypesValid |= 0x8;
    }
    
    protected void convertNumberToBigDecimal() throws IOException, JsonParseException {
        if ((this._numTypesValid & 0x8) != 0x0) {
            this._numberBigDecimal = new BigDecimal(this.getText());
        }
        else if ((this._numTypesValid & 0x4) != 0x0) {
            this._numberBigDecimal = new BigDecimal(this._numberBigInt);
        }
        else if ((this._numTypesValid & 0x2) != 0x0) {
            this._numberBigDecimal = BigDecimal.valueOf(this._numberLong);
        }
        else if ((this._numTypesValid & 0x1) != 0x0) {
            this._numberBigDecimal = BigDecimal.valueOf(this._numberInt);
        }
        else {
            this._throwInternal();
        }
        this._numTypesValid |= 0x10;
    }
    
    protected void reportUnexpectedNumberChar(final int ch, final String comment) throws JsonParseException {
        String msg = "Unexpected character (" + JsonParserMinimalBase._getCharDesc(ch) + ") in numeric value";
        if (comment != null) {
            msg = msg + ": " + comment;
        }
        this._reportError(msg);
    }
    
    protected void reportInvalidNumber(final String msg) throws JsonParseException {
        this._reportError("Invalid numeric value: " + msg);
    }
    
    protected void reportOverflowInt() throws IOException, JsonParseException {
        this._reportError("Numeric value (" + this.getText() + ") out of range of int (" + Integer.MIN_VALUE + " - " + Integer.MAX_VALUE + ")");
    }
    
    protected void reportOverflowLong() throws IOException, JsonParseException {
        this._reportError("Numeric value (" + this.getText() + ") out of range of long (" + Long.MIN_VALUE + " - " + Long.MAX_VALUE + ")");
    }
    
    protected char _decodeEscaped() throws IOException, JsonParseException {
        throw new UnsupportedOperationException();
    }
    
    protected final int _decodeBase64Escape(final Base64Variant b64variant, final int ch, final int index) throws IOException, JsonParseException {
        if (ch != 92) {
            throw this.reportInvalidBase64Char(b64variant, ch, index);
        }
        final int unescaped = this._decodeEscaped();
        if (unescaped <= 32 && index == 0) {
            return -1;
        }
        final int bits = b64variant.decodeBase64Char(unescaped);
        if (bits < 0) {
            throw this.reportInvalidBase64Char(b64variant, unescaped, index);
        }
        return bits;
    }
    
    protected final int _decodeBase64Escape(final Base64Variant b64variant, final char ch, final int index) throws IOException, JsonParseException {
        if (ch != '\\') {
            throw this.reportInvalidBase64Char(b64variant, ch, index);
        }
        final char unescaped = this._decodeEscaped();
        if (unescaped <= ' ' && index == 0) {
            return -1;
        }
        final int bits = b64variant.decodeBase64Char(unescaped);
        if (bits < 0) {
            throw this.reportInvalidBase64Char(b64variant, unescaped, index);
        }
        return bits;
    }
    
    protected IllegalArgumentException reportInvalidBase64Char(final Base64Variant b64variant, final int ch, final int bindex) throws IllegalArgumentException {
        return this.reportInvalidBase64Char(b64variant, ch, bindex, null);
    }
    
    protected IllegalArgumentException reportInvalidBase64Char(final Base64Variant b64variant, final int ch, final int bindex, final String msg) throws IllegalArgumentException {
        String base;
        if (ch <= 32) {
            base = "Illegal white space character (code 0x" + Integer.toHexString(ch) + ") as character #" + (bindex + 1) + " of 4-char base64 unit: can only used between units";
        }
        else if (b64variant.usesPaddingChar(ch)) {
            base = "Unexpected padding character ('" + b64variant.getPaddingChar() + "') as character #" + (bindex + 1) + " of 4-char base64 unit: padding only legal as 3rd or 4th character";
        }
        else if (!Character.isDefined(ch) || Character.isISOControl(ch)) {
            base = "Illegal character (code 0x" + Integer.toHexString(ch) + ") in base64 content";
        }
        else {
            base = "Illegal character '" + (char)ch + "' (code 0x" + Integer.toHexString(ch) + ") in base64 content";
        }
        if (msg != null) {
            base = base + ": " + msg;
        }
        return new IllegalArgumentException(base);
    }
    
    static {
        BI_MIN_INT = BigInteger.valueOf(-2147483648L);
        BI_MAX_INT = BigInteger.valueOf(2147483647L);
        BI_MIN_LONG = BigInteger.valueOf(Long.MIN_VALUE);
        BI_MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);
        BD_MIN_LONG = new BigDecimal(JsonParserBase.BI_MIN_LONG);
        BD_MAX_LONG = new BigDecimal(JsonParserBase.BI_MAX_LONG);
        BD_MIN_INT = new BigDecimal(JsonParserBase.BI_MIN_INT);
        BD_MAX_INT = new BigDecimal(JsonParserBase.BI_MAX_INT);
    }
}
