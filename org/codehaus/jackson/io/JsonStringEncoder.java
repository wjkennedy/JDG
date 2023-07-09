// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.io;

import org.codehaus.jackson.util.CharTypes;
import org.codehaus.jackson.util.BufferRecycler;
import org.codehaus.jackson.util.ByteArrayBuilder;
import org.codehaus.jackson.util.TextBuffer;
import java.lang.ref.SoftReference;

public final class JsonStringEncoder
{
    private static final char[] HEX_CHARS;
    private static final byte[] HEX_BYTES;
    private static final int SURR1_FIRST = 55296;
    private static final int SURR1_LAST = 56319;
    private static final int SURR2_FIRST = 56320;
    private static final int SURR2_LAST = 57343;
    private static final int INT_BACKSLASH = 92;
    private static final int INT_U = 117;
    private static final int INT_0 = 48;
    protected static final ThreadLocal<SoftReference<JsonStringEncoder>> _threadEncoder;
    protected TextBuffer _textBuffer;
    protected ByteArrayBuilder _byteBuilder;
    protected final char[] _quoteBuffer;
    
    public JsonStringEncoder() {
        (this._quoteBuffer = new char[6])[0] = '\\';
        this._quoteBuffer[2] = '0';
        this._quoteBuffer[3] = '0';
    }
    
    public static JsonStringEncoder getInstance() {
        final SoftReference<JsonStringEncoder> ref = JsonStringEncoder._threadEncoder.get();
        JsonStringEncoder enc = (ref == null) ? null : ref.get();
        if (enc == null) {
            enc = new JsonStringEncoder();
            JsonStringEncoder._threadEncoder.set(new SoftReference<JsonStringEncoder>(enc));
        }
        return enc;
    }
    
    public char[] quoteAsString(final String input) {
        TextBuffer textBuffer = this._textBuffer;
        if (textBuffer == null) {
            textBuffer = (this._textBuffer = new TextBuffer(null));
        }
        char[] outputBuffer = textBuffer.emptyAndGetCurrentSegment();
        final int[] escCodes = CharTypes.get7BitOutputEscapes();
        final int escCodeCount = escCodes.length;
        int inPtr = 0;
        final int inputLen = input.length();
        int outPtr = 0;
    Label_0261:
        while (inPtr < inputLen) {
            while (true) {
                final char c = input.charAt(inPtr);
                if (c < escCodeCount && escCodes[c] != 0) {
                    final char d = input.charAt(inPtr++);
                    final int escCode = escCodes[d];
                    final int length = (escCode < 0) ? this._appendNumericEscape(d, this._quoteBuffer) : this._appendNamedEscape(escCode, this._quoteBuffer);
                    if (outPtr + length > outputBuffer.length) {
                        final int first = outputBuffer.length - outPtr;
                        if (first > 0) {
                            System.arraycopy(this._quoteBuffer, 0, outputBuffer, outPtr, first);
                        }
                        outputBuffer = textBuffer.finishCurrentSegment();
                        final int second = length - first;
                        System.arraycopy(this._quoteBuffer, first, outputBuffer, 0, second);
                        outPtr = second;
                    }
                    else {
                        System.arraycopy(this._quoteBuffer, 0, outputBuffer, outPtr, length);
                        outPtr += length;
                    }
                    break;
                }
                if (outPtr >= outputBuffer.length) {
                    outputBuffer = textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outputBuffer[outPtr++] = c;
                if (++inPtr >= inputLen) {
                    break Label_0261;
                }
            }
        }
        textBuffer.setCurrentLength(outPtr);
        return textBuffer.contentsAsArray();
    }
    
    public byte[] quoteAsUTF8(final String text) {
        ByteArrayBuilder byteBuilder = this._byteBuilder;
        if (byteBuilder == null) {
            byteBuilder = (this._byteBuilder = new ByteArrayBuilder(null));
        }
        int inputPtr = 0;
        final int inputEnd = text.length();
        int outputPtr = 0;
        byte[] outputBuffer = byteBuilder.resetAndGetFirstSegment();
    Label_0496:
        while (inputPtr < inputEnd) {
            final int[] escCodes = CharTypes.get7BitOutputEscapes();
            while (true) {
                int ch = text.charAt(inputPtr);
                if (ch <= 127 && escCodes[ch] == 0) {
                    if (outputPtr >= outputBuffer.length) {
                        outputBuffer = byteBuilder.finishCurrentSegment();
                        outputPtr = 0;
                    }
                    outputBuffer[outputPtr++] = (byte)ch;
                    if (++inputPtr >= inputEnd) {
                        break Label_0496;
                    }
                    continue;
                }
                else {
                    if (outputPtr >= outputBuffer.length) {
                        outputBuffer = byteBuilder.finishCurrentSegment();
                        outputPtr = 0;
                    }
                    ch = text.charAt(inputPtr++);
                    if (ch <= 127) {
                        final int escape = escCodes[ch];
                        outputPtr = this._appendByteEscape(ch, escape, byteBuilder, outputPtr);
                        outputBuffer = byteBuilder.getCurrentSegment();
                        break;
                    }
                    if (ch <= 2047) {
                        outputBuffer[outputPtr++] = (byte)(0xC0 | ch >> 6);
                        ch = (0x80 | (ch & 0x3F));
                    }
                    else if (ch < 55296 || ch > 57343) {
                        outputBuffer[outputPtr++] = (byte)(0xE0 | ch >> 12);
                        if (outputPtr >= outputBuffer.length) {
                            outputBuffer = byteBuilder.finishCurrentSegment();
                            outputPtr = 0;
                        }
                        outputBuffer[outputPtr++] = (byte)(0x80 | (ch >> 6 & 0x3F));
                        ch = (0x80 | (ch & 0x3F));
                    }
                    else {
                        if (ch > 56319) {
                            this._throwIllegalSurrogate(ch);
                        }
                        if (inputPtr >= inputEnd) {
                            this._throwIllegalSurrogate(ch);
                        }
                        ch = this._convertSurrogate(ch, text.charAt(inputPtr++));
                        if (ch > 1114111) {
                            this._throwIllegalSurrogate(ch);
                        }
                        outputBuffer[outputPtr++] = (byte)(0xF0 | ch >> 18);
                        if (outputPtr >= outputBuffer.length) {
                            outputBuffer = byteBuilder.finishCurrentSegment();
                            outputPtr = 0;
                        }
                        outputBuffer[outputPtr++] = (byte)(0x80 | (ch >> 12 & 0x3F));
                        if (outputPtr >= outputBuffer.length) {
                            outputBuffer = byteBuilder.finishCurrentSegment();
                            outputPtr = 0;
                        }
                        outputBuffer[outputPtr++] = (byte)(0x80 | (ch >> 6 & 0x3F));
                        ch = (0x80 | (ch & 0x3F));
                    }
                    if (outputPtr >= outputBuffer.length) {
                        outputBuffer = byteBuilder.finishCurrentSegment();
                        outputPtr = 0;
                    }
                    outputBuffer[outputPtr++] = (byte)ch;
                    break;
                }
            }
        }
        return this._byteBuilder.completeAndCoalesce(outputPtr);
    }
    
    public byte[] encodeAsUTF8(final String text) {
        ByteArrayBuilder byteBuilder = this._byteBuilder;
        if (byteBuilder == null) {
            byteBuilder = (this._byteBuilder = new ByteArrayBuilder(null));
        }
        int inputPtr = 0;
        final int inputEnd = text.length();
        int outputPtr = 0;
        byte[] outputBuffer = byteBuilder.resetAndGetFirstSegment();
        int outputEnd = outputBuffer.length;
    Label_0447:
        while (inputPtr < inputEnd) {
            int c;
            for (c = text.charAt(inputPtr++); c <= 127; c = text.charAt(inputPtr++)) {
                if (outputPtr >= outputEnd) {
                    outputBuffer = byteBuilder.finishCurrentSegment();
                    outputEnd = outputBuffer.length;
                    outputPtr = 0;
                }
                outputBuffer[outputPtr++] = (byte)c;
                if (inputPtr >= inputEnd) {
                    break Label_0447;
                }
            }
            if (outputPtr >= outputEnd) {
                outputBuffer = byteBuilder.finishCurrentSegment();
                outputEnd = outputBuffer.length;
                outputPtr = 0;
            }
            if (c < 2048) {
                outputBuffer[outputPtr++] = (byte)(0xC0 | c >> 6);
            }
            else if (c < 55296 || c > 57343) {
                outputBuffer[outputPtr++] = (byte)(0xE0 | c >> 12);
                if (outputPtr >= outputEnd) {
                    outputBuffer = byteBuilder.finishCurrentSegment();
                    outputEnd = outputBuffer.length;
                    outputPtr = 0;
                }
                outputBuffer[outputPtr++] = (byte)(0x80 | (c >> 6 & 0x3F));
            }
            else {
                if (c > 56319) {
                    this._throwIllegalSurrogate(c);
                }
                if (inputPtr >= inputEnd) {
                    this._throwIllegalSurrogate(c);
                }
                c = this._convertSurrogate(c, text.charAt(inputPtr++));
                if (c > 1114111) {
                    this._throwIllegalSurrogate(c);
                }
                outputBuffer[outputPtr++] = (byte)(0xF0 | c >> 18);
                if (outputPtr >= outputEnd) {
                    outputBuffer = byteBuilder.finishCurrentSegment();
                    outputEnd = outputBuffer.length;
                    outputPtr = 0;
                }
                outputBuffer[outputPtr++] = (byte)(0x80 | (c >> 12 & 0x3F));
                if (outputPtr >= outputEnd) {
                    outputBuffer = byteBuilder.finishCurrentSegment();
                    outputEnd = outputBuffer.length;
                    outputPtr = 0;
                }
                outputBuffer[outputPtr++] = (byte)(0x80 | (c >> 6 & 0x3F));
            }
            if (outputPtr >= outputEnd) {
                outputBuffer = byteBuilder.finishCurrentSegment();
                outputEnd = outputBuffer.length;
                outputPtr = 0;
            }
            outputBuffer[outputPtr++] = (byte)(0x80 | (c & 0x3F));
        }
        return this._byteBuilder.completeAndCoalesce(outputPtr);
    }
    
    private int _appendNumericEscape(final int value, final char[] quoteBuffer) {
        quoteBuffer[1] = 'u';
        quoteBuffer[4] = JsonStringEncoder.HEX_CHARS[value >> 4];
        quoteBuffer[5] = JsonStringEncoder.HEX_CHARS[value & 0xF];
        return 6;
    }
    
    private int _appendNamedEscape(final int escCode, final char[] quoteBuffer) {
        quoteBuffer[1] = (char)escCode;
        return 2;
    }
    
    private int _appendByteEscape(int ch, final int escCode, final ByteArrayBuilder byteBuilder, final int ptr) {
        byteBuilder.setCurrentSegmentLength(ptr);
        byteBuilder.append(92);
        if (escCode < 0) {
            byteBuilder.append(117);
            if (ch > 255) {
                final int hi = ch >> 8;
                byteBuilder.append(JsonStringEncoder.HEX_BYTES[hi >> 4]);
                byteBuilder.append(JsonStringEncoder.HEX_BYTES[hi & 0xF]);
                ch &= 0xFF;
            }
            else {
                byteBuilder.append(48);
                byteBuilder.append(48);
            }
            byteBuilder.append(JsonStringEncoder.HEX_BYTES[ch >> 4]);
            byteBuilder.append(JsonStringEncoder.HEX_BYTES[ch & 0xF]);
        }
        else {
            byteBuilder.append((byte)escCode);
        }
        return byteBuilder.getCurrentSegmentLength();
    }
    
    private int _convertSurrogate(final int firstPart, final int secondPart) {
        if (secondPart < 56320 || secondPart > 57343) {
            throw new IllegalArgumentException("Broken surrogate pair: first char 0x" + Integer.toHexString(firstPart) + ", second 0x" + Integer.toHexString(secondPart) + "; illegal combination");
        }
        return 65536 + (firstPart - 55296 << 10) + (secondPart - 56320);
    }
    
    private void _throwIllegalSurrogate(final int code) {
        if (code > 1114111) {
            throw new IllegalArgumentException("Illegal character point (0x" + Integer.toHexString(code) + ") to output; max is 0x10FFFF as per RFC 4627");
        }
        if (code < 55296) {
            throw new IllegalArgumentException("Illegal character point (0x" + Integer.toHexString(code) + ") to output");
        }
        if (code <= 56319) {
            throw new IllegalArgumentException("Unmatched first part of surrogate pair (0x" + Integer.toHexString(code) + ")");
        }
        throw new IllegalArgumentException("Unmatched second part of surrogate pair (0x" + Integer.toHexString(code) + ")");
    }
    
    static {
        HEX_CHARS = CharTypes.copyHexChars();
        HEX_BYTES = CharTypes.copyHexBytes();
        _threadEncoder = new ThreadLocal<SoftReference<JsonStringEncoder>>();
    }
}
