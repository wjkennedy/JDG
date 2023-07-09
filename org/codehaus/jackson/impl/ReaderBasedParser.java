// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.impl;

import org.codehaus.jackson.util.TextBuffer;
import org.codehaus.jackson.util.CharTypes;
import org.codehaus.jackson.util.ByteArrayBuilder;
import org.codehaus.jackson.Base64Variant;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonParseException;
import java.io.IOException;
import java.io.Writer;
import org.codehaus.jackson.io.IOContext;
import org.codehaus.jackson.sym.CharsToNameCanonicalizer;
import org.codehaus.jackson.ObjectCodec;
import java.io.Reader;

public final class ReaderBasedParser extends JsonParserBase
{
    protected Reader _reader;
    protected char[] _inputBuffer;
    protected ObjectCodec _objectCodec;
    protected final CharsToNameCanonicalizer _symbols;
    protected boolean _tokenIncomplete;
    
    public ReaderBasedParser(final IOContext ctxt, final int features, final Reader r, final ObjectCodec codec, final CharsToNameCanonicalizer st) {
        super(ctxt, features);
        this._tokenIncomplete = false;
        this._reader = r;
        this._inputBuffer = ctxt.allocTokenBuffer();
        this._objectCodec = codec;
        this._symbols = st;
    }
    
    @Override
    public ObjectCodec getCodec() {
        return this._objectCodec;
    }
    
    @Override
    public void setCodec(final ObjectCodec c) {
        this._objectCodec = c;
    }
    
    @Override
    public int releaseBuffered(final Writer w) throws IOException {
        final int count = this._inputEnd - this._inputPtr;
        if (count < 1) {
            return 0;
        }
        final int origPtr = this._inputPtr;
        w.write(this._inputBuffer, origPtr, count);
        return count;
    }
    
    @Override
    public Object getInputSource() {
        return this._reader;
    }
    
    @Override
    protected final boolean loadMore() throws IOException {
        this._currInputProcessed += this._inputEnd;
        this._currInputRowStart -= this._inputEnd;
        if (this._reader != null) {
            final int count = this._reader.read(this._inputBuffer, 0, this._inputBuffer.length);
            if (count > 0) {
                this._inputPtr = 0;
                this._inputEnd = count;
                return true;
            }
            this._closeInput();
            if (count == 0) {
                throw new IOException("Reader returned 0 characters when trying to read " + this._inputEnd);
            }
        }
        return false;
    }
    
    protected char getNextChar(final String eofMsg) throws IOException, JsonParseException {
        if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
            this._reportInvalidEOF(eofMsg);
        }
        return this._inputBuffer[this._inputPtr++];
    }
    
    @Override
    protected void _closeInput() throws IOException {
        if (this._reader != null) {
            if (this._ioContext.isResourceManaged() || this.isEnabled(Feature.AUTO_CLOSE_SOURCE)) {
                this._reader.close();
            }
            this._reader = null;
        }
    }
    
    @Override
    protected void _releaseBuffers() throws IOException {
        super._releaseBuffers();
        final char[] buf = this._inputBuffer;
        if (buf != null) {
            this._inputBuffer = null;
            this._ioContext.releaseTokenBuffer(buf);
        }
    }
    
    @Override
    public final String getText() throws IOException, JsonParseException {
        final JsonToken t = this._currToken;
        if (t == JsonToken.VALUE_STRING) {
            if (this._tokenIncomplete) {
                this._tokenIncomplete = false;
                this._finishString();
            }
            return this._textBuffer.contentsAsString();
        }
        return this._getText2(t);
    }
    
    protected final String _getText2(final JsonToken t) {
        if (t == null) {
            return null;
        }
        switch (t) {
            case FIELD_NAME: {
                return this._parsingContext.getCurrentName();
            }
            case VALUE_STRING:
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT: {
                return this._textBuffer.contentsAsString();
            }
            default: {
                return t.asString();
            }
        }
    }
    
    @Override
    public char[] getTextCharacters() throws IOException, JsonParseException {
        if (this._currToken == null) {
            return null;
        }
        switch (this._currToken) {
            case FIELD_NAME: {
                if (!this._nameCopied) {
                    final String name = this._parsingContext.getCurrentName();
                    final int nameLen = name.length();
                    if (this._nameCopyBuffer == null) {
                        this._nameCopyBuffer = this._ioContext.allocNameCopyBuffer(nameLen);
                    }
                    else if (this._nameCopyBuffer.length < nameLen) {
                        this._nameCopyBuffer = new char[nameLen];
                    }
                    name.getChars(0, nameLen, this._nameCopyBuffer, 0);
                    this._nameCopied = true;
                }
                return this._nameCopyBuffer;
            }
            case VALUE_STRING: {
                if (this._tokenIncomplete) {
                    this._tokenIncomplete = false;
                    this._finishString();
                    return this._textBuffer.getTextBuffer();
                }
                return this._textBuffer.getTextBuffer();
            }
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT: {
                return this._textBuffer.getTextBuffer();
            }
            default: {
                return this._currToken.asCharArray();
            }
        }
    }
    
    @Override
    public int getTextLength() throws IOException, JsonParseException {
        if (this._currToken == null) {
            return 0;
        }
        switch (this._currToken) {
            case FIELD_NAME: {
                return this._parsingContext.getCurrentName().length();
            }
            case VALUE_STRING: {
                if (this._tokenIncomplete) {
                    this._tokenIncomplete = false;
                    this._finishString();
                    return this._textBuffer.size();
                }
                return this._textBuffer.size();
            }
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT: {
                return this._textBuffer.size();
            }
            default: {
                return this._currToken.asCharArray().length;
            }
        }
    }
    
    @Override
    public int getTextOffset() throws IOException, JsonParseException {
        if (this._currToken != null) {
            switch (this._currToken) {
                case FIELD_NAME: {
                    return 0;
                }
                case VALUE_STRING: {
                    if (this._tokenIncomplete) {
                        this._tokenIncomplete = false;
                        this._finishString();
                        return this._textBuffer.getTextOffset();
                    }
                    return this._textBuffer.getTextOffset();
                }
                case VALUE_NUMBER_INT:
                case VALUE_NUMBER_FLOAT: {
                    return this._textBuffer.getTextOffset();
                }
            }
        }
        return 0;
    }
    
    @Override
    public byte[] getBinaryValue(final Base64Variant b64variant) throws IOException, JsonParseException {
        if (this._currToken != JsonToken.VALUE_STRING && (this._currToken != JsonToken.VALUE_EMBEDDED_OBJECT || this._binaryValue == null)) {
            this._reportError("Current token (" + this._currToken + ") not VALUE_STRING or VALUE_EMBEDDED_OBJECT, can not access as binary");
        }
        if (this._tokenIncomplete) {
            try {
                this._binaryValue = this._decodeBase64(b64variant);
            }
            catch (final IllegalArgumentException iae) {
                throw this._constructError("Failed to decode VALUE_STRING as base64 (" + b64variant + "): " + iae.getMessage());
            }
            this._tokenIncomplete = false;
        }
        else if (this._binaryValue == null) {
            final ByteArrayBuilder builder = this._getByteArrayBuilder();
            this._decodeBase64(this.getText(), builder, b64variant);
            this._binaryValue = builder.toByteArray();
        }
        return this._binaryValue;
    }
    
    @Override
    public JsonToken nextToken() throws IOException, JsonParseException {
        this._numTypesValid = 0;
        if (this._currToken == JsonToken.FIELD_NAME) {
            return this._nextAfterName();
        }
        if (this._tokenIncomplete) {
            this._skipString();
        }
        int i = this._skipWSOrEnd();
        if (i < 0) {
            this.close();
            return this._currToken = null;
        }
        this._tokenInputTotal = this._currInputProcessed + this._inputPtr - 1L;
        this._tokenInputRow = this._currInputRow;
        this._tokenInputCol = this._inputPtr - this._currInputRowStart - 1;
        this._binaryValue = null;
        if (i == 93) {
            if (!this._parsingContext.inArray()) {
                this._reportMismatchedEndMarker(i, '}');
            }
            this._parsingContext = this._parsingContext.getParent();
            return this._currToken = JsonToken.END_ARRAY;
        }
        if (i == 125) {
            if (!this._parsingContext.inObject()) {
                this._reportMismatchedEndMarker(i, ']');
            }
            this._parsingContext = this._parsingContext.getParent();
            return this._currToken = JsonToken.END_OBJECT;
        }
        if (this._parsingContext.expectComma()) {
            if (i != 44) {
                this._reportUnexpectedChar(i, "was expecting comma to separate " + this._parsingContext.getTypeDesc() + " entries");
            }
            i = this._skipWS();
        }
        final boolean inObject = this._parsingContext.inObject();
        if (inObject) {
            final String name = this._parseFieldName(i);
            this._parsingContext.setCurrentName(name);
            this._currToken = JsonToken.FIELD_NAME;
            i = this._skipWS();
            if (i != 58) {
                this._reportUnexpectedChar(i, "was expecting a colon to separate field name and value");
            }
            i = this._skipWS();
        }
        JsonToken t = null;
        switch (i) {
            case 34: {
                this._tokenIncomplete = true;
                t = JsonToken.VALUE_STRING;
                break;
            }
            case 91: {
                if (!inObject) {
                    this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
                }
                t = JsonToken.START_ARRAY;
                break;
            }
            case 123: {
                if (!inObject) {
                    this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
                }
                t = JsonToken.START_OBJECT;
                break;
            }
            case 93:
            case 125: {
                this._reportUnexpectedChar(i, "expected a value");
            }
            case 116: {
                this._matchToken("true", 1);
                t = JsonToken.VALUE_TRUE;
                break;
            }
            case 102: {
                this._matchToken("false", 1);
                t = JsonToken.VALUE_FALSE;
                break;
            }
            case 110: {
                this._matchToken("null", 1);
                t = JsonToken.VALUE_NULL;
                break;
            }
            case 45:
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57: {
                t = this.parseNumberText(i);
                break;
            }
            default: {
                t = this._handleUnexpectedValue(i);
                break;
            }
        }
        if (inObject) {
            this._nextToken = t;
            return this._currToken;
        }
        return this._currToken = t;
    }
    
    private final JsonToken _nextAfterName() {
        this._nameCopied = false;
        final JsonToken t = this._nextToken;
        this._nextToken = null;
        if (t == JsonToken.START_ARRAY) {
            this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
        }
        else if (t == JsonToken.START_OBJECT) {
            this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
        }
        return this._currToken = t;
    }
    
    @Override
    public String nextTextValue() throws IOException, JsonParseException {
        if (this._currToken != JsonToken.FIELD_NAME) {
            return (this.nextToken() == JsonToken.VALUE_STRING) ? this.getText() : null;
        }
        this._nameCopied = false;
        final JsonToken t = this._nextToken;
        this._nextToken = null;
        if ((this._currToken = t) == JsonToken.VALUE_STRING) {
            if (this._tokenIncomplete) {
                this._tokenIncomplete = false;
                this._finishString();
            }
            return this._textBuffer.contentsAsString();
        }
        if (t == JsonToken.START_ARRAY) {
            this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
        }
        else if (t == JsonToken.START_OBJECT) {
            this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
        }
        return null;
    }
    
    @Override
    public int nextIntValue(final int defaultValue) throws IOException, JsonParseException {
        if (this._currToken != JsonToken.FIELD_NAME) {
            return (this.nextToken() == JsonToken.VALUE_NUMBER_INT) ? this.getIntValue() : defaultValue;
        }
        this._nameCopied = false;
        final JsonToken t = this._nextToken;
        this._nextToken = null;
        if ((this._currToken = t) == JsonToken.VALUE_NUMBER_INT) {
            return this.getIntValue();
        }
        if (t == JsonToken.START_ARRAY) {
            this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
        }
        else if (t == JsonToken.START_OBJECT) {
            this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
        }
        return defaultValue;
    }
    
    @Override
    public long nextLongValue(final long defaultValue) throws IOException, JsonParseException {
        if (this._currToken != JsonToken.FIELD_NAME) {
            return (this.nextToken() == JsonToken.VALUE_NUMBER_INT) ? this.getLongValue() : defaultValue;
        }
        this._nameCopied = false;
        final JsonToken t = this._nextToken;
        this._nextToken = null;
        if ((this._currToken = t) == JsonToken.VALUE_NUMBER_INT) {
            return this.getLongValue();
        }
        if (t == JsonToken.START_ARRAY) {
            this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
        }
        else if (t == JsonToken.START_OBJECT) {
            this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
        }
        return defaultValue;
    }
    
    @Override
    public Boolean nextBooleanValue() throws IOException, JsonParseException {
        if (this._currToken == JsonToken.FIELD_NAME) {
            this._nameCopied = false;
            final JsonToken t = this._nextToken;
            this._nextToken = null;
            if ((this._currToken = t) == JsonToken.VALUE_TRUE) {
                return Boolean.TRUE;
            }
            if (t == JsonToken.VALUE_FALSE) {
                return Boolean.FALSE;
            }
            if (t == JsonToken.START_ARRAY) {
                this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
            }
            else if (t == JsonToken.START_OBJECT) {
                this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
            }
            return null;
        }
        else {
            switch (this.nextToken()) {
                case VALUE_TRUE: {
                    return Boolean.TRUE;
                }
                case VALUE_FALSE: {
                    return Boolean.FALSE;
                }
                default: {
                    return null;
                }
            }
        }
    }
    
    @Override
    public void close() throws IOException {
        super.close();
        this._symbols.release();
    }
    
    protected final JsonToken parseNumberText(int ch) throws IOException, JsonParseException {
        final boolean negative = ch == 45;
        int ptr = this._inputPtr;
        final int startPtr = ptr - 1;
        final int inputLen = this._inputEnd;
        Label_0347: {
            if (negative) {
                if (ptr >= this._inputEnd) {
                    break Label_0347;
                }
                ch = this._inputBuffer[ptr++];
                if (ch > 57 || ch < 48) {
                    this._inputPtr = ptr;
                    return this._handleInvalidNumberStart(ch, true);
                }
            }
            if (ch != 48) {
                int intLen = 1;
                while (ptr < this._inputEnd) {
                    ch = this._inputBuffer[ptr++];
                    if (ch < 48 || ch > 57) {
                        int fractLen = 0;
                        Label_0192: {
                            if (ch == 46) {
                                while (ptr < inputLen) {
                                    ch = this._inputBuffer[ptr++];
                                    if (ch >= 48 && ch <= 57) {
                                        ++fractLen;
                                    }
                                    else {
                                        if (fractLen == 0) {
                                            this.reportUnexpectedNumberChar(ch, "Decimal point not followed by a digit");
                                        }
                                        break Label_0192;
                                    }
                                }
                                break;
                            }
                        }
                        int expLen = 0;
                        if (ch == 101 || ch == 69) {
                            if (ptr >= inputLen) {
                                break;
                            }
                            ch = this._inputBuffer[ptr++];
                            if (ch == 45 || ch == 43) {
                                if (ptr >= inputLen) {
                                    break;
                                }
                                ch = this._inputBuffer[ptr++];
                            }
                            while (ch <= 57 && ch >= 48) {
                                ++expLen;
                                if (ptr >= inputLen) {
                                    break Label_0347;
                                }
                                ch = this._inputBuffer[ptr++];
                            }
                            if (expLen == 0) {
                                this.reportUnexpectedNumberChar(ch, "Exponent indicator not followed by a digit");
                            }
                        }
                        --ptr;
                        this._inputPtr = ptr;
                        final int len = ptr - startPtr;
                        this._textBuffer.resetWithShared(this._inputBuffer, startPtr, len);
                        return this.reset(negative, intLen, fractLen, expLen);
                    }
                    ++intLen;
                }
            }
        }
        this._inputPtr = (negative ? (startPtr + 1) : startPtr);
        return this.parseNumberText2(negative);
    }
    
    private final JsonToken parseNumberText2(final boolean negative) throws IOException, JsonParseException {
        char[] outBuf = this._textBuffer.emptyAndGetCurrentSegment();
        int outPtr = 0;
        if (negative) {
            outBuf[outPtr++] = '-';
        }
        int intLen = 0;
        char c = (this._inputPtr < this._inputEnd) ? this._inputBuffer[this._inputPtr++] : this.getNextChar("No digit following minus sign");
        if (c == '0') {
            c = this._verifyNoLeadingZeroes();
        }
        boolean eof = false;
        while (c >= '0' && c <= '9') {
            ++intLen;
            if (outPtr >= outBuf.length) {
                outBuf = this._textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            outBuf[outPtr++] = c;
            if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
                c = '\0';
                eof = true;
                break;
            }
            c = this._inputBuffer[this._inputPtr++];
        }
        if (intLen == 0) {
            this.reportInvalidNumber("Missing integer part (next char " + JsonParserMinimalBase._getCharDesc(c) + ")");
        }
        int fractLen = 0;
        Label_0325: {
            if (c == '.') {
                outBuf[outPtr++] = c;
                while (true) {
                    while (this._inputPtr < this._inputEnd || this.loadMore()) {
                        c = this._inputBuffer[this._inputPtr++];
                        if (c >= '0') {
                            if (c <= '9') {
                                ++fractLen;
                                if (outPtr >= outBuf.length) {
                                    outBuf = this._textBuffer.finishCurrentSegment();
                                    outPtr = 0;
                                }
                                outBuf[outPtr++] = c;
                                continue;
                            }
                        }
                        if (fractLen == 0) {
                            this.reportUnexpectedNumberChar(c, "Decimal point not followed by a digit");
                        }
                        break Label_0325;
                    }
                    eof = true;
                    continue;
                }
            }
        }
        int expLen = 0;
        if (c == 'e' || c == 'E') {
            if (outPtr >= outBuf.length) {
                outBuf = this._textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            outBuf[outPtr++] = c;
            c = ((this._inputPtr < this._inputEnd) ? this._inputBuffer[this._inputPtr++] : this.getNextChar("expected a digit for number exponent"));
            if (c == '-' || c == '+') {
                if (outPtr >= outBuf.length) {
                    outBuf = this._textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outBuf[outPtr++] = c;
                c = ((this._inputPtr < this._inputEnd) ? this._inputBuffer[this._inputPtr++] : this.getNextChar("expected a digit for number exponent"));
            }
            while (c <= '9' && c >= '0') {
                ++expLen;
                if (outPtr >= outBuf.length) {
                    outBuf = this._textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outBuf[outPtr++] = c;
                if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
                    eof = true;
                    break;
                }
                c = this._inputBuffer[this._inputPtr++];
            }
            if (expLen == 0) {
                this.reportUnexpectedNumberChar(c, "Exponent indicator not followed by a digit");
            }
        }
        if (!eof) {
            --this._inputPtr;
        }
        this._textBuffer.setCurrentLength(outPtr);
        return this.reset(negative, intLen, fractLen, expLen);
    }
    
    private final char _verifyNoLeadingZeroes() throws IOException, JsonParseException {
        if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
            return '0';
        }
        char ch = this._inputBuffer[this._inputPtr];
        if (ch < '0' || ch > '9') {
            return '0';
        }
        if (!this.isEnabled(Feature.ALLOW_NUMERIC_LEADING_ZEROS)) {
            this.reportInvalidNumber("Leading zeroes not allowed");
        }
        ++this._inputPtr;
        if (ch == '0') {
            while (this._inputPtr < this._inputEnd || this.loadMore()) {
                ch = this._inputBuffer[this._inputPtr];
                if (ch < '0' || ch > '9') {
                    return '0';
                }
                ++this._inputPtr;
                if (ch != '0') {
                    break;
                }
            }
        }
        return ch;
    }
    
    protected JsonToken _handleInvalidNumberStart(int ch, final boolean negative) throws IOException, JsonParseException {
        if (ch == 73) {
            if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
                this._reportInvalidEOFInValue();
            }
            ch = this._inputBuffer[this._inputPtr++];
            if (ch == 78) {
                final String match = negative ? "-INF" : "+INF";
                this._matchToken(match, 3);
                if (this.isEnabled(Feature.ALLOW_NON_NUMERIC_NUMBERS)) {
                    return this.resetAsNaN(match, negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
                }
                this._reportError("Non-standard token '" + match + "': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
            }
            else if (ch == 110) {
                final String match = negative ? "-Infinity" : "+Infinity";
                this._matchToken(match, 3);
                if (this.isEnabled(Feature.ALLOW_NON_NUMERIC_NUMBERS)) {
                    return this.resetAsNaN(match, negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
                }
                this._reportError("Non-standard token '" + match + "': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
            }
        }
        this.reportUnexpectedNumberChar(ch, "expected digit (0-9) to follow minus sign, for valid numeric value");
        return null;
    }
    
    protected final String _parseFieldName(final int i) throws IOException, JsonParseException {
        if (i != 34) {
            return this._handleUnusualFieldName(i);
        }
        int ptr = this._inputPtr;
        int hash = 0;
        final int inputLen = this._inputEnd;
        if (ptr < inputLen) {
            final int[] codes = CharTypes.getInputCodeLatin1();
            final int maxCode = codes.length;
            do {
                final int ch = this._inputBuffer[ptr];
                if (ch < maxCode && codes[ch] != 0) {
                    if (ch == 34) {
                        final int start = this._inputPtr;
                        this._inputPtr = ptr + 1;
                        return this._symbols.findSymbol(this._inputBuffer, start, ptr - start, hash);
                    }
                    break;
                }
                else {
                    hash = hash * 31 + ch;
                }
            } while (++ptr < inputLen);
        }
        final int start2 = this._inputPtr;
        this._inputPtr = ptr;
        return this._parseFieldName2(start2, hash, 34);
    }
    
    private String _parseFieldName2(final int startPtr, int hash, final int endChar) throws IOException, JsonParseException {
        this._textBuffer.resetWithShared(this._inputBuffer, startPtr, this._inputPtr - startPtr);
        char[] outBuf = this._textBuffer.getCurrentSegment();
        int outPtr = this._textBuffer.getCurrentSegmentSize();
        while (true) {
            if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
                this._reportInvalidEOF(": was expecting closing '" + (char)endChar + "' for name");
            }
            final int i;
            char c = (char)(i = this._inputBuffer[this._inputPtr++]);
            if (i <= 92) {
                if (i == 92) {
                    c = this._decodeEscaped();
                }
                else if (i <= endChar) {
                    if (i == endChar) {
                        break;
                    }
                    if (i < 32) {
                        this._throwUnquotedSpace(i, "name");
                    }
                }
            }
            hash = hash * 31 + i;
            outBuf[outPtr++] = c;
            if (outPtr >= outBuf.length) {
                outBuf = this._textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
        }
        this._textBuffer.setCurrentLength(outPtr);
        final TextBuffer tb = this._textBuffer;
        final char[] buf = tb.getTextBuffer();
        final int start = tb.getTextOffset();
        final int len = tb.size();
        return this._symbols.findSymbol(buf, start, len, hash);
    }
    
    protected final String _handleUnusualFieldName(final int i) throws IOException, JsonParseException {
        if (i == 39 && this.isEnabled(Feature.ALLOW_SINGLE_QUOTES)) {
            return this._parseApostropheFieldName();
        }
        if (!this.isEnabled(Feature.ALLOW_UNQUOTED_FIELD_NAMES)) {
            this._reportUnexpectedChar(i, "was expecting double-quote to start field name");
        }
        final int[] codes = CharTypes.getInputCodeLatin1JsNames();
        final int maxCode = codes.length;
        boolean firstOk;
        if (i < maxCode) {
            firstOk = (codes[i] == 0 && (i < 48 || i > 57));
        }
        else {
            firstOk = Character.isJavaIdentifierPart((char)i);
        }
        if (!firstOk) {
            this._reportUnexpectedChar(i, "was expecting either valid name character (for unquoted name) or double-quote (for quoted) to start field name");
        }
        int ptr = this._inputPtr;
        int hash = 0;
        final int inputLen = this._inputEnd;
        if (ptr < inputLen) {
            do {
                final int ch = this._inputBuffer[ptr];
                if (ch < maxCode) {
                    if (codes[ch] != 0) {
                        final int start = this._inputPtr - 1;
                        this._inputPtr = ptr;
                        return this._symbols.findSymbol(this._inputBuffer, start, ptr - start, hash);
                    }
                }
                else if (!Character.isJavaIdentifierPart((char)ch)) {
                    final int start = this._inputPtr - 1;
                    this._inputPtr = ptr;
                    return this._symbols.findSymbol(this._inputBuffer, start, ptr - start, hash);
                }
                hash = hash * 31 + ch;
            } while (++ptr < inputLen);
        }
        final int start2 = this._inputPtr - 1;
        this._inputPtr = ptr;
        return this._parseUnusualFieldName2(start2, hash, codes);
    }
    
    protected final String _parseApostropheFieldName() throws IOException, JsonParseException {
        int ptr = this._inputPtr;
        int hash = 0;
        final int inputLen = this._inputEnd;
        if (ptr < inputLen) {
            final int[] codes = CharTypes.getInputCodeLatin1();
            final int maxCode = codes.length;
            do {
                final int ch = this._inputBuffer[ptr];
                if (ch == 39) {
                    final int start = this._inputPtr;
                    this._inputPtr = ptr + 1;
                    return this._symbols.findSymbol(this._inputBuffer, start, ptr - start, hash);
                }
                if (ch < maxCode && codes[ch] != 0) {
                    break;
                }
                hash = hash * 31 + ch;
            } while (++ptr < inputLen);
        }
        final int start2 = this._inputPtr;
        this._inputPtr = ptr;
        return this._parseFieldName2(start2, hash, 39);
    }
    
    protected final JsonToken _handleUnexpectedValue(final int i) throws IOException, JsonParseException {
        switch (i) {
            case 39: {
                if (this.isEnabled(Feature.ALLOW_SINGLE_QUOTES)) {
                    return this._handleApostropheValue();
                }
                break;
            }
            case 78: {
                this._matchToken("NaN", 1);
                if (this.isEnabled(Feature.ALLOW_NON_NUMERIC_NUMBERS)) {
                    return this.resetAsNaN("NaN", Double.NaN);
                }
                this._reportError("Non-standard token 'NaN': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
                break;
            }
            case 43: {
                if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
                    this._reportInvalidEOFInValue();
                }
                return this._handleInvalidNumberStart(this._inputBuffer[this._inputPtr++], false);
            }
        }
        this._reportUnexpectedChar(i, "expected a valid value (number, String, array, object, 'true', 'false' or 'null')");
        return null;
    }
    
    protected final JsonToken _handleApostropheValue() throws IOException, JsonParseException {
        char[] outBuf = this._textBuffer.emptyAndGetCurrentSegment();
        int outPtr = this._textBuffer.getCurrentSegmentSize();
        while (true) {
            if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
                this._reportInvalidEOF(": was expecting closing quote for a string value");
            }
            final int i;
            char c = (char)(i = this._inputBuffer[this._inputPtr++]);
            if (i <= 92) {
                if (i == 92) {
                    c = this._decodeEscaped();
                }
                else if (i <= 39) {
                    if (i == 39) {
                        break;
                    }
                    if (i < 32) {
                        this._throwUnquotedSpace(i, "string value");
                    }
                }
            }
            if (outPtr >= outBuf.length) {
                outBuf = this._textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            outBuf[outPtr++] = c;
        }
        this._textBuffer.setCurrentLength(outPtr);
        return JsonToken.VALUE_STRING;
    }
    
    private String _parseUnusualFieldName2(final int startPtr, int hash, final int[] codes) throws IOException, JsonParseException {
        this._textBuffer.resetWithShared(this._inputBuffer, startPtr, this._inputPtr - startPtr);
        char[] outBuf = this._textBuffer.getCurrentSegment();
        int outPtr = this._textBuffer.getCurrentSegmentSize();
        final int maxCode = codes.length;
        while (true) {
            while (this._inputPtr < this._inputEnd || this.loadMore()) {
                final int i;
                final char c = (char)(i = this._inputBuffer[this._inputPtr]);
                if (i <= maxCode) {
                    if (codes[i] != 0) {
                        break;
                    }
                }
                else if (!Character.isJavaIdentifierPart(c)) {
                    break;
                }
                ++this._inputPtr;
                hash = hash * 31 + i;
                outBuf[outPtr++] = c;
                if (outPtr < outBuf.length) {
                    continue;
                }
                outBuf = this._textBuffer.finishCurrentSegment();
                outPtr = 0;
                continue;
                this._textBuffer.setCurrentLength(outPtr);
                final TextBuffer tb = this._textBuffer;
                final char[] buf = tb.getTextBuffer();
                final int start = tb.getTextOffset();
                final int len = tb.size();
                return this._symbols.findSymbol(buf, start, len, hash);
            }
            continue;
        }
    }
    
    @Override
    protected void _finishString() throws IOException, JsonParseException {
        int ptr = this._inputPtr;
        final int inputLen = this._inputEnd;
        if (ptr < inputLen) {
            final int[] codes = CharTypes.getInputCodeLatin1();
            final int maxCode = codes.length;
            do {
                final int ch = this._inputBuffer[ptr];
                if (ch < maxCode && codes[ch] != 0) {
                    if (ch == 34) {
                        this._textBuffer.resetWithShared(this._inputBuffer, this._inputPtr, ptr - this._inputPtr);
                        this._inputPtr = ptr + 1;
                        return;
                    }
                    break;
                }
            } while (++ptr < inputLen);
        }
        this._textBuffer.resetWithCopy(this._inputBuffer, this._inputPtr, ptr - this._inputPtr);
        this._inputPtr = ptr;
        this._finishString2();
    }
    
    protected void _finishString2() throws IOException, JsonParseException {
        char[] outBuf = this._textBuffer.getCurrentSegment();
        int outPtr = this._textBuffer.getCurrentSegmentSize();
        while (true) {
            if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
                this._reportInvalidEOF(": was expecting closing quote for a string value");
            }
            final int i;
            char c = (char)(i = this._inputBuffer[this._inputPtr++]);
            if (i <= 92) {
                if (i == 92) {
                    c = this._decodeEscaped();
                }
                else if (i <= 34) {
                    if (i == 34) {
                        break;
                    }
                    if (i < 32) {
                        this._throwUnquotedSpace(i, "string value");
                    }
                }
            }
            if (outPtr >= outBuf.length) {
                outBuf = this._textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            outBuf[outPtr++] = c;
        }
        this._textBuffer.setCurrentLength(outPtr);
    }
    
    protected void _skipString() throws IOException, JsonParseException {
        this._tokenIncomplete = false;
        int inputPtr = this._inputPtr;
        int inputLen = this._inputEnd;
        final char[] inputBuffer = this._inputBuffer;
        while (true) {
            if (inputPtr >= inputLen) {
                this._inputPtr = inputPtr;
                if (!this.loadMore()) {
                    this._reportInvalidEOF(": was expecting closing quote for a string value");
                }
                inputPtr = this._inputPtr;
                inputLen = this._inputEnd;
            }
            final int i;
            char c = (char)(i = inputBuffer[inputPtr++]);
            if (i <= 92) {
                if (i == 92) {
                    this._inputPtr = inputPtr;
                    c = this._decodeEscaped();
                    inputPtr = this._inputPtr;
                    inputLen = this._inputEnd;
                }
                else {
                    if (i > 34) {
                        continue;
                    }
                    if (i == 34) {
                        break;
                    }
                    if (i >= 32) {
                        continue;
                    }
                    this._inputPtr = inputPtr;
                    this._throwUnquotedSpace(i, "string value");
                }
            }
        }
        this._inputPtr = inputPtr;
    }
    
    protected final void _skipCR() throws IOException {
        if ((this._inputPtr < this._inputEnd || this.loadMore()) && this._inputBuffer[this._inputPtr] == '\n') {
            ++this._inputPtr;
        }
        ++this._currInputRow;
        this._currInputRowStart = this._inputPtr;
    }
    
    protected final void _skipLF() throws IOException {
        ++this._currInputRow;
        this._currInputRowStart = this._inputPtr;
    }
    
    private final int _skipWS() throws IOException, JsonParseException {
        while (this._inputPtr < this._inputEnd || this.loadMore()) {
            final int i = this._inputBuffer[this._inputPtr++];
            if (i > 32) {
                if (i != 47) {
                    return i;
                }
                this._skipComment();
            }
            else {
                if (i == 32) {
                    continue;
                }
                if (i == 10) {
                    this._skipLF();
                }
                else if (i == 13) {
                    this._skipCR();
                }
                else {
                    if (i == 9) {
                        continue;
                    }
                    this._throwInvalidSpace(i);
                }
            }
        }
        throw this._constructError("Unexpected end-of-input within/between " + this._parsingContext.getTypeDesc() + " entries");
    }
    
    private final int _skipWSOrEnd() throws IOException, JsonParseException {
        while (this._inputPtr < this._inputEnd || this.loadMore()) {
            final int i = this._inputBuffer[this._inputPtr++];
            if (i > 32) {
                if (i != 47) {
                    return i;
                }
                this._skipComment();
            }
            else {
                if (i == 32) {
                    continue;
                }
                if (i == 10) {
                    this._skipLF();
                }
                else if (i == 13) {
                    this._skipCR();
                }
                else {
                    if (i == 9) {
                        continue;
                    }
                    this._throwInvalidSpace(i);
                }
            }
        }
        this._handleEOF();
        return -1;
    }
    
    private final void _skipComment() throws IOException, JsonParseException {
        if (!this.isEnabled(Feature.ALLOW_COMMENTS)) {
            this._reportUnexpectedChar(47, "maybe a (non-standard) comment? (not recognized as one since Feature 'ALLOW_COMMENTS' not enabled for parser)");
        }
        if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
            this._reportInvalidEOF(" in a comment");
        }
        final char c = this._inputBuffer[this._inputPtr++];
        if (c == '/') {
            this._skipCppComment();
        }
        else if (c == '*') {
            this._skipCComment();
        }
        else {
            this._reportUnexpectedChar(c, "was expecting either '*' or '/' for a comment");
        }
    }
    
    private final void _skipCComment() throws IOException, JsonParseException {
        while (this._inputPtr < this._inputEnd || this.loadMore()) {
            final int i = this._inputBuffer[this._inputPtr++];
            if (i <= 42) {
                if (i == 42) {
                    if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
                        break;
                    }
                    if (this._inputBuffer[this._inputPtr] == '/') {
                        ++this._inputPtr;
                        return;
                    }
                    continue;
                }
                else {
                    if (i >= 32) {
                        continue;
                    }
                    if (i == 10) {
                        this._skipLF();
                    }
                    else if (i == 13) {
                        this._skipCR();
                    }
                    else {
                        if (i == 9) {
                            continue;
                        }
                        this._throwInvalidSpace(i);
                    }
                }
            }
        }
        this._reportInvalidEOF(" in a comment");
    }
    
    private final void _skipCppComment() throws IOException, JsonParseException {
        while (this._inputPtr < this._inputEnd || this.loadMore()) {
            final int i = this._inputBuffer[this._inputPtr++];
            if (i < 32) {
                if (i == 10) {
                    this._skipLF();
                    break;
                }
                if (i == 13) {
                    this._skipCR();
                    break;
                }
                if (i == 9) {
                    continue;
                }
                this._throwInvalidSpace(i);
            }
        }
    }
    
    @Override
    protected final char _decodeEscaped() throws IOException, JsonParseException {
        if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
            this._reportInvalidEOF(" in character escape sequence");
        }
        final char c = this._inputBuffer[this._inputPtr++];
        switch (c) {
            case 'b': {
                return '\b';
            }
            case 't': {
                return '\t';
            }
            case 'n': {
                return '\n';
            }
            case 'f': {
                return '\f';
            }
            case 'r': {
                return '\r';
            }
            case '\"':
            case '/':
            case '\\': {
                return c;
            }
            case 'u': {
                int value = 0;
                for (int i = 0; i < 4; ++i) {
                    if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
                        this._reportInvalidEOF(" in character escape sequence");
                    }
                    final int ch = this._inputBuffer[this._inputPtr++];
                    final int digit = CharTypes.charToHex(ch);
                    if (digit < 0) {
                        this._reportUnexpectedChar(ch, "expected a hex-digit for character escape sequence");
                    }
                    value = (value << 4 | digit);
                }
                return (char)value;
            }
            default: {
                return this._handleUnrecognizedCharacterEscape(c);
            }
        }
    }
    
    protected final void _matchToken(final String matchStr, int i) throws IOException, JsonParseException {
        final int len = matchStr.length();
        do {
            if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
                this._reportInvalidEOFInValue();
            }
            if (this._inputBuffer[this._inputPtr] != matchStr.charAt(i)) {
                this._reportInvalidToken(matchStr.substring(0, i), "'null', 'true', 'false' or NaN");
            }
            ++this._inputPtr;
        } while (++i < len);
        if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
            return;
        }
        final char c = this._inputBuffer[this._inputPtr];
        if (c < '0' || c == ']' || c == '}') {
            return;
        }
        if (Character.isJavaIdentifierPart(c)) {
            this._reportInvalidToken(matchStr.substring(0, i), "'null', 'true', 'false' or NaN");
        }
    }
    
    protected byte[] _decodeBase64(final Base64Variant b64variant) throws IOException, JsonParseException {
        final ByteArrayBuilder builder = this._getByteArrayBuilder();
        while (true) {
            if (this._inputPtr >= this._inputEnd) {
                this.loadMoreGuaranteed();
            }
            char ch = this._inputBuffer[this._inputPtr++];
            if (ch > ' ') {
                int bits = b64variant.decodeBase64Char(ch);
                if (bits < 0) {
                    if (ch == '\"') {
                        return builder.toByteArray();
                    }
                    bits = this._decodeBase64Escape(b64variant, ch, 0);
                    if (bits < 0) {
                        continue;
                    }
                }
                int decodedData = bits;
                if (this._inputPtr >= this._inputEnd) {
                    this.loadMoreGuaranteed();
                }
                ch = this._inputBuffer[this._inputPtr++];
                bits = b64variant.decodeBase64Char(ch);
                if (bits < 0) {
                    bits = this._decodeBase64Escape(b64variant, ch, 1);
                }
                decodedData = (decodedData << 6 | bits);
                if (this._inputPtr >= this._inputEnd) {
                    this.loadMoreGuaranteed();
                }
                ch = this._inputBuffer[this._inputPtr++];
                bits = b64variant.decodeBase64Char(ch);
                if (bits < 0) {
                    if (bits != -2) {
                        if (ch == '\"' && !b64variant.usesPadding()) {
                            decodedData >>= 4;
                            builder.append(decodedData);
                            return builder.toByteArray();
                        }
                        bits = this._decodeBase64Escape(b64variant, ch, 2);
                    }
                    if (bits == -2) {
                        if (this._inputPtr >= this._inputEnd) {
                            this.loadMoreGuaranteed();
                        }
                        ch = this._inputBuffer[this._inputPtr++];
                        if (!b64variant.usesPaddingChar(ch)) {
                            throw this.reportInvalidBase64Char(b64variant, ch, 3, "expected padding character '" + b64variant.getPaddingChar() + "'");
                        }
                        decodedData >>= 4;
                        builder.append(decodedData);
                        continue;
                    }
                }
                decodedData = (decodedData << 6 | bits);
                if (this._inputPtr >= this._inputEnd) {
                    this.loadMoreGuaranteed();
                }
                ch = this._inputBuffer[this._inputPtr++];
                bits = b64variant.decodeBase64Char(ch);
                if (bits < 0) {
                    if (bits != -2) {
                        if (ch == '\"' && !b64variant.usesPadding()) {
                            decodedData >>= 2;
                            builder.appendTwoBytes(decodedData);
                            return builder.toByteArray();
                        }
                        bits = this._decodeBase64Escape(b64variant, ch, 3);
                    }
                    if (bits == -2) {
                        decodedData >>= 2;
                        builder.appendTwoBytes(decodedData);
                        continue;
                    }
                }
                decodedData = (decodedData << 6 | bits);
                builder.appendThreeBytes(decodedData);
            }
        }
    }
    
    protected void _reportInvalidToken(final String matchedPart, final String msg) throws IOException, JsonParseException {
        final StringBuilder sb = new StringBuilder(matchedPart);
        while (true) {
            while (this._inputPtr < this._inputEnd || this.loadMore()) {
                final char c = this._inputBuffer[this._inputPtr];
                if (!Character.isJavaIdentifierPart(c)) {
                    this._reportError("Unrecognized token '" + sb.toString() + "': was expecting ");
                    return;
                }
                ++this._inputPtr;
                sb.append(c);
            }
            continue;
        }
    }
}
