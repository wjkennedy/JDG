// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson;

import java.util.Arrays;

public final class Base64Variant
{
    static final char PADDING_CHAR_NONE = '\0';
    public static final int BASE64_VALUE_INVALID = -1;
    public static final int BASE64_VALUE_PADDING = -2;
    private final int[] _asciiToBase64;
    private final char[] _base64ToAsciiC;
    private final byte[] _base64ToAsciiB;
    final String _name;
    final boolean _usesPadding;
    final char _paddingChar;
    final int _maxLineLength;
    
    public Base64Variant(final String name, final String base64Alphabet, final boolean usesPadding, final char paddingChar, final int maxLineLength) {
        this._asciiToBase64 = new int[128];
        this._base64ToAsciiC = new char[64];
        this._base64ToAsciiB = new byte[64];
        this._name = name;
        this._usesPadding = usesPadding;
        this._paddingChar = paddingChar;
        this._maxLineLength = maxLineLength;
        final int alphaLen = base64Alphabet.length();
        if (alphaLen != 64) {
            throw new IllegalArgumentException("Base64Alphabet length must be exactly 64 (was " + alphaLen + ")");
        }
        base64Alphabet.getChars(0, alphaLen, this._base64ToAsciiC, 0);
        Arrays.fill(this._asciiToBase64, -1);
        for (int i = 0; i < alphaLen; ++i) {
            final char alpha = this._base64ToAsciiC[i];
            this._base64ToAsciiB[i] = (byte)alpha;
            this._asciiToBase64[alpha] = i;
        }
        if (usesPadding) {
            this._asciiToBase64[paddingChar] = -2;
        }
    }
    
    public Base64Variant(final Base64Variant base, final String name, final int maxLineLength) {
        this(base, name, base._usesPadding, base._paddingChar, maxLineLength);
    }
    
    public Base64Variant(final Base64Variant base, final String name, final boolean usesPadding, final char paddingChar, final int maxLineLength) {
        this._asciiToBase64 = new int[128];
        this._base64ToAsciiC = new char[64];
        this._base64ToAsciiB = new byte[64];
        this._name = name;
        final byte[] srcB = base._base64ToAsciiB;
        System.arraycopy(srcB, 0, this._base64ToAsciiB, 0, srcB.length);
        final char[] srcC = base._base64ToAsciiC;
        System.arraycopy(srcC, 0, this._base64ToAsciiC, 0, srcC.length);
        final int[] srcV = base._asciiToBase64;
        System.arraycopy(srcV, 0, this._asciiToBase64, 0, srcV.length);
        this._usesPadding = usesPadding;
        this._paddingChar = paddingChar;
        this._maxLineLength = maxLineLength;
    }
    
    public String getName() {
        return this._name;
    }
    
    public boolean usesPadding() {
        return this._usesPadding;
    }
    
    public boolean usesPaddingChar(final char c) {
        return c == this._paddingChar;
    }
    
    public boolean usesPaddingChar(final int ch) {
        return ch == this._paddingChar;
    }
    
    public char getPaddingChar() {
        return this._paddingChar;
    }
    
    public byte getPaddingByte() {
        return (byte)this._paddingChar;
    }
    
    public int getMaxLineLength() {
        return this._maxLineLength;
    }
    
    public int decodeBase64Char(final char c) {
        final int ch = c;
        return (ch <= 127) ? this._asciiToBase64[ch] : -1;
    }
    
    public int decodeBase64Char(final int ch) {
        return (ch <= 127) ? this._asciiToBase64[ch] : -1;
    }
    
    public int decodeBase64Byte(final byte b) {
        final int ch = b;
        return (ch <= 127) ? this._asciiToBase64[ch] : -1;
    }
    
    public char encodeBase64BitsAsChar(final int value) {
        return this._base64ToAsciiC[value];
    }
    
    public int encodeBase64Chunk(final int b24, final char[] buffer, int ptr) {
        buffer[ptr++] = this._base64ToAsciiC[b24 >> 18 & 0x3F];
        buffer[ptr++] = this._base64ToAsciiC[b24 >> 12 & 0x3F];
        buffer[ptr++] = this._base64ToAsciiC[b24 >> 6 & 0x3F];
        buffer[ptr++] = this._base64ToAsciiC[b24 & 0x3F];
        return ptr;
    }
    
    public void encodeBase64Chunk(final StringBuilder sb, final int b24) {
        sb.append(this._base64ToAsciiC[b24 >> 18 & 0x3F]);
        sb.append(this._base64ToAsciiC[b24 >> 12 & 0x3F]);
        sb.append(this._base64ToAsciiC[b24 >> 6 & 0x3F]);
        sb.append(this._base64ToAsciiC[b24 & 0x3F]);
    }
    
    public int encodeBase64Partial(final int bits, final int outputBytes, final char[] buffer, int outPtr) {
        buffer[outPtr++] = this._base64ToAsciiC[bits >> 18 & 0x3F];
        buffer[outPtr++] = this._base64ToAsciiC[bits >> 12 & 0x3F];
        if (this._usesPadding) {
            buffer[outPtr++] = ((outputBytes == 2) ? this._base64ToAsciiC[bits >> 6 & 0x3F] : this._paddingChar);
            buffer[outPtr++] = this._paddingChar;
        }
        else if (outputBytes == 2) {
            buffer[outPtr++] = this._base64ToAsciiC[bits >> 6 & 0x3F];
        }
        return outPtr;
    }
    
    public void encodeBase64Partial(final StringBuilder sb, final int bits, final int outputBytes) {
        sb.append(this._base64ToAsciiC[bits >> 18 & 0x3F]);
        sb.append(this._base64ToAsciiC[bits >> 12 & 0x3F]);
        if (this._usesPadding) {
            sb.append((outputBytes == 2) ? this._base64ToAsciiC[bits >> 6 & 0x3F] : this._paddingChar);
            sb.append(this._paddingChar);
        }
        else if (outputBytes == 2) {
            sb.append(this._base64ToAsciiC[bits >> 6 & 0x3F]);
        }
    }
    
    public byte encodeBase64BitsAsByte(final int value) {
        return this._base64ToAsciiB[value];
    }
    
    public int encodeBase64Chunk(final int b24, final byte[] buffer, int ptr) {
        buffer[ptr++] = this._base64ToAsciiB[b24 >> 18 & 0x3F];
        buffer[ptr++] = this._base64ToAsciiB[b24 >> 12 & 0x3F];
        buffer[ptr++] = this._base64ToAsciiB[b24 >> 6 & 0x3F];
        buffer[ptr++] = this._base64ToAsciiB[b24 & 0x3F];
        return ptr;
    }
    
    public int encodeBase64Partial(final int bits, final int outputBytes, final byte[] buffer, int outPtr) {
        buffer[outPtr++] = this._base64ToAsciiB[bits >> 18 & 0x3F];
        buffer[outPtr++] = this._base64ToAsciiB[bits >> 12 & 0x3F];
        if (this._usesPadding) {
            final byte pb = (byte)this._paddingChar;
            buffer[outPtr++] = ((outputBytes == 2) ? this._base64ToAsciiB[bits >> 6 & 0x3F] : pb);
            buffer[outPtr++] = pb;
        }
        else if (outputBytes == 2) {
            buffer[outPtr++] = this._base64ToAsciiB[bits >> 6 & 0x3F];
        }
        return outPtr;
    }
    
    public String encode(final byte[] input) {
        return this.encode(input, false);
    }
    
    public String encode(final byte[] input, final boolean addQuotes) {
        final int inputEnd = input.length;
        final int outputLen = inputEnd + (inputEnd >> 2) + (inputEnd >> 3);
        final StringBuilder sb = new StringBuilder(outputLen);
        if (addQuotes) {
            sb.append('\"');
        }
        int chunksBeforeLF = this.getMaxLineLength() >> 2;
        int inputPtr = 0;
        final int safeInputEnd = inputEnd - 3;
        while (inputPtr <= safeInputEnd) {
            int b24 = input[inputPtr++] << 8;
            b24 |= (input[inputPtr++] & 0xFF);
            b24 = (b24 << 8 | (input[inputPtr++] & 0xFF));
            this.encodeBase64Chunk(sb, b24);
            if (--chunksBeforeLF <= 0) {
                sb.append('\\');
                sb.append('n');
                chunksBeforeLF = this.getMaxLineLength() >> 2;
            }
        }
        final int inputLeft = inputEnd - inputPtr;
        if (inputLeft > 0) {
            int b25 = input[inputPtr++] << 16;
            if (inputLeft == 2) {
                b25 |= (input[inputPtr++] & 0xFF) << 8;
            }
            this.encodeBase64Partial(sb, b25, inputLeft);
        }
        if (addQuotes) {
            sb.append('\"');
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return this._name;
    }
}
