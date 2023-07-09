// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.impl;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.util.ByteArrayBuilder;
import org.codehaus.jackson.io.NumberInput;
import org.codehaus.jackson.Base64Variant;
import org.codehaus.jackson.JsonStreamContext;
import org.codehaus.jackson.JsonParseException;
import java.io.IOException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.JsonParser;

public abstract class JsonParserMinimalBase extends JsonParser
{
    protected static final int INT_TAB = 9;
    protected static final int INT_LF = 10;
    protected static final int INT_CR = 13;
    protected static final int INT_SPACE = 32;
    protected static final int INT_LBRACKET = 91;
    protected static final int INT_RBRACKET = 93;
    protected static final int INT_LCURLY = 123;
    protected static final int INT_RCURLY = 125;
    protected static final int INT_QUOTE = 34;
    protected static final int INT_BACKSLASH = 92;
    protected static final int INT_SLASH = 47;
    protected static final int INT_COLON = 58;
    protected static final int INT_COMMA = 44;
    protected static final int INT_ASTERISK = 42;
    protected static final int INT_APOSTROPHE = 39;
    protected static final int INT_b = 98;
    protected static final int INT_f = 102;
    protected static final int INT_n = 110;
    protected static final int INT_r = 114;
    protected static final int INT_t = 116;
    protected static final int INT_u = 117;
    
    protected JsonParserMinimalBase() {
    }
    
    protected JsonParserMinimalBase(final int features) {
        super(features);
    }
    
    @Override
    public abstract JsonToken nextToken() throws IOException, JsonParseException;
    
    @Override
    public JsonParser skipChildren() throws IOException, JsonParseException {
        if (this._currToken != JsonToken.START_OBJECT && this._currToken != JsonToken.START_ARRAY) {
            return this;
        }
        int open = 1;
        while (true) {
            final JsonToken t = this.nextToken();
            if (t == null) {
                this._handleEOF();
                return this;
            }
            switch (t) {
                case START_OBJECT:
                case START_ARRAY: {
                    ++open;
                    continue;
                }
                case END_OBJECT:
                case END_ARRAY: {
                    if (--open == 0) {
                        return this;
                    }
                    continue;
                }
            }
        }
    }
    
    protected abstract void _handleEOF() throws JsonParseException;
    
    @Override
    public abstract String getCurrentName() throws IOException, JsonParseException;
    
    @Override
    public abstract void close() throws IOException;
    
    @Override
    public abstract boolean isClosed();
    
    @Override
    public abstract JsonStreamContext getParsingContext();
    
    @Override
    public abstract String getText() throws IOException, JsonParseException;
    
    @Override
    public abstract char[] getTextCharacters() throws IOException, JsonParseException;
    
    @Override
    public abstract boolean hasTextCharacters();
    
    @Override
    public abstract int getTextLength() throws IOException, JsonParseException;
    
    @Override
    public abstract int getTextOffset() throws IOException, JsonParseException;
    
    @Override
    public abstract byte[] getBinaryValue(final Base64Variant p0) throws IOException, JsonParseException;
    
    @Override
    public boolean getValueAsBoolean(final boolean defaultValue) throws IOException, JsonParseException {
        if (this._currToken != null) {
            switch (this._currToken) {
                case VALUE_NUMBER_INT: {
                    return this.getIntValue() != 0;
                }
                case VALUE_TRUE: {
                    return true;
                }
                case VALUE_FALSE:
                case VALUE_NULL: {
                    return false;
                }
                case VALUE_EMBEDDED_OBJECT: {
                    final Object value = this.getEmbeddedObject();
                    if (value instanceof Boolean) {
                        return (boolean)value;
                    }
                }
                case VALUE_STRING: {
                    final String str = this.getText().trim();
                    if ("true".equals(str)) {
                        return true;
                    }
                    break;
                }
            }
        }
        return defaultValue;
    }
    
    @Override
    public int getValueAsInt(final int defaultValue) throws IOException, JsonParseException {
        if (this._currToken != null) {
            switch (this._currToken) {
                case VALUE_NUMBER_INT:
                case VALUE_NUMBER_FLOAT: {
                    return this.getIntValue();
                }
                case VALUE_TRUE: {
                    return 1;
                }
                case VALUE_FALSE:
                case VALUE_NULL: {
                    return 0;
                }
                case VALUE_STRING: {
                    return NumberInput.parseAsInt(this.getText(), defaultValue);
                }
                case VALUE_EMBEDDED_OBJECT: {
                    final Object value = this.getEmbeddedObject();
                    if (value instanceof Number) {
                        return ((Number)value).intValue();
                    }
                    break;
                }
            }
        }
        return defaultValue;
    }
    
    @Override
    public long getValueAsLong(final long defaultValue) throws IOException, JsonParseException {
        if (this._currToken != null) {
            switch (this._currToken) {
                case VALUE_NUMBER_INT:
                case VALUE_NUMBER_FLOAT: {
                    return this.getLongValue();
                }
                case VALUE_TRUE: {
                    return 1L;
                }
                case VALUE_FALSE:
                case VALUE_NULL: {
                    return 0L;
                }
                case VALUE_STRING: {
                    return NumberInput.parseAsLong(this.getText(), defaultValue);
                }
                case VALUE_EMBEDDED_OBJECT: {
                    final Object value = this.getEmbeddedObject();
                    if (value instanceof Number) {
                        return ((Number)value).longValue();
                    }
                    break;
                }
            }
        }
        return defaultValue;
    }
    
    @Override
    public double getValueAsDouble(final double defaultValue) throws IOException, JsonParseException {
        if (this._currToken != null) {
            switch (this._currToken) {
                case VALUE_NUMBER_INT:
                case VALUE_NUMBER_FLOAT: {
                    return this.getDoubleValue();
                }
                case VALUE_TRUE: {
                    return 1.0;
                }
                case VALUE_FALSE:
                case VALUE_NULL: {
                    return 0.0;
                }
                case VALUE_STRING: {
                    return NumberInput.parseAsDouble(this.getText(), defaultValue);
                }
                case VALUE_EMBEDDED_OBJECT: {
                    final Object value = this.getEmbeddedObject();
                    if (value instanceof Number) {
                        return ((Number)value).doubleValue();
                    }
                    break;
                }
            }
        }
        return defaultValue;
    }
    
    protected void _decodeBase64(final String str, final ByteArrayBuilder builder, final Base64Variant b64variant) throws IOException, JsonParseException {
        int ptr = 0;
        final int len = str.length();
    Label_0395:
        while (ptr < len) {
            char ch;
            do {
                ch = str.charAt(ptr++);
                if (ptr >= len) {
                    break Label_0395;
                }
            } while (ch <= ' ');
            int bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
                this._reportInvalidBase64(b64variant, ch, 0, null);
            }
            int decodedData = bits;
            if (ptr >= len) {
                this._reportBase64EOF();
            }
            ch = str.charAt(ptr++);
            bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
                this._reportInvalidBase64(b64variant, ch, 1, null);
            }
            decodedData = (decodedData << 6 | bits);
            if (ptr >= len) {
                if (!b64variant.usesPadding()) {
                    decodedData >>= 4;
                    builder.append(decodedData);
                    break;
                }
                this._reportBase64EOF();
            }
            ch = str.charAt(ptr++);
            bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
                if (bits != -2) {
                    this._reportInvalidBase64(b64variant, ch, 2, null);
                }
                if (ptr >= len) {
                    this._reportBase64EOF();
                }
                ch = str.charAt(ptr++);
                if (!b64variant.usesPaddingChar(ch)) {
                    this._reportInvalidBase64(b64variant, ch, 3, "expected padding character '" + b64variant.getPaddingChar() + "'");
                }
                decodedData >>= 4;
                builder.append(decodedData);
            }
            else {
                decodedData = (decodedData << 6 | bits);
                if (ptr >= len) {
                    if (!b64variant.usesPadding()) {
                        decodedData >>= 2;
                        builder.appendTwoBytes(decodedData);
                        break;
                    }
                    this._reportBase64EOF();
                }
                ch = str.charAt(ptr++);
                bits = b64variant.decodeBase64Char(ch);
                if (bits < 0) {
                    if (bits != -2) {
                        this._reportInvalidBase64(b64variant, ch, 3, null);
                    }
                    decodedData >>= 2;
                    builder.appendTwoBytes(decodedData);
                }
                else {
                    decodedData = (decodedData << 6 | bits);
                    builder.appendThreeBytes(decodedData);
                }
            }
        }
    }
    
    protected void _reportInvalidBase64(final Base64Variant b64variant, final char ch, final int bindex, final String msg) throws JsonParseException {
        String base;
        if (ch <= ' ') {
            base = "Illegal white space character (code 0x" + Integer.toHexString(ch) + ") as character #" + (bindex + 1) + " of 4-char base64 unit: can only used between units";
        }
        else if (b64variant.usesPaddingChar(ch)) {
            base = "Unexpected padding character ('" + b64variant.getPaddingChar() + "') as character #" + (bindex + 1) + " of 4-char base64 unit: padding only legal as 3rd or 4th character";
        }
        else if (!Character.isDefined(ch) || Character.isISOControl(ch)) {
            base = "Illegal character (code 0x" + Integer.toHexString(ch) + ") in base64 content";
        }
        else {
            base = "Illegal character '" + ch + "' (code 0x" + Integer.toHexString(ch) + ") in base64 content";
        }
        if (msg != null) {
            base = base + ": " + msg;
        }
        throw this._constructError(base);
    }
    
    protected void _reportBase64EOF() throws JsonParseException {
        throw this._constructError("Unexpected end-of-String in base64 content");
    }
    
    protected void _reportUnexpectedChar(final int ch, final String comment) throws JsonParseException {
        String msg = "Unexpected character (" + _getCharDesc(ch) + ")";
        if (comment != null) {
            msg = msg + ": " + comment;
        }
        this._reportError(msg);
    }
    
    protected void _reportInvalidEOF() throws JsonParseException {
        this._reportInvalidEOF(" in " + this._currToken);
    }
    
    protected void _reportInvalidEOF(final String msg) throws JsonParseException {
        this._reportError("Unexpected end-of-input" + msg);
    }
    
    protected void _reportInvalidEOFInValue() throws JsonParseException {
        this._reportInvalidEOF(" in a value");
    }
    
    protected void _throwInvalidSpace(final int i) throws JsonParseException {
        final char c = (char)i;
        final String msg = "Illegal character (" + _getCharDesc(c) + "): only regular white space (\\r, \\n, \\t) is allowed between tokens";
        this._reportError(msg);
    }
    
    protected void _throwUnquotedSpace(final int i, final String ctxtDesc) throws JsonParseException {
        if (!this.isEnabled(Feature.ALLOW_UNQUOTED_CONTROL_CHARS) || i >= 32) {
            final char c = (char)i;
            final String msg = "Illegal unquoted character (" + _getCharDesc(c) + "): has to be escaped using backslash to be included in " + ctxtDesc;
            this._reportError(msg);
        }
    }
    
    protected char _handleUnrecognizedCharacterEscape(final char ch) throws JsonProcessingException {
        if (this.isEnabled(Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)) {
            return ch;
        }
        if (ch == '\'' && this.isEnabled(Feature.ALLOW_SINGLE_QUOTES)) {
            return ch;
        }
        this._reportError("Unrecognized character escape " + _getCharDesc(ch));
        return ch;
    }
    
    protected static final String _getCharDesc(final int ch) {
        final char c = (char)ch;
        if (Character.isISOControl(c)) {
            return "(CTRL-CHAR, code " + ch + ")";
        }
        if (ch > 255) {
            return "'" + c + "' (code " + ch + " / 0x" + Integer.toHexString(ch) + ")";
        }
        return "'" + c + "' (code " + ch + ")";
    }
    
    protected final void _reportError(final String msg) throws JsonParseException {
        throw this._constructError(msg);
    }
    
    protected final void _wrapError(final String msg, final Throwable t) throws JsonParseException {
        throw this._constructError(msg, t);
    }
    
    protected final void _throwInternal() {
        throw new RuntimeException("Internal error: this code path should never get executed");
    }
    
    protected final JsonParseException _constructError(final String msg, final Throwable t) {
        return new JsonParseException(msg, this.getCurrentLocation(), t);
    }
}
