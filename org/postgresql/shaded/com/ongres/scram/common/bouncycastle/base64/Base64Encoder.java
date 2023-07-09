// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.bouncycastle.base64;

import java.io.IOException;
import java.io.OutputStream;

public class Base64Encoder implements Encoder
{
    protected final byte[] encodingTable;
    protected byte padding;
    protected final byte[] decodingTable;
    
    protected void initialiseDecodingTable() {
        for (int i = 0; i < this.decodingTable.length; ++i) {
            this.decodingTable[i] = -1;
        }
        for (int i = 0; i < this.encodingTable.length; ++i) {
            this.decodingTable[this.encodingTable[i]] = (byte)i;
        }
    }
    
    public Base64Encoder() {
        this.encodingTable = new byte[] { 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47 };
        this.padding = 61;
        this.decodingTable = new byte[128];
        this.initialiseDecodingTable();
    }
    
    @Override
    public int encode(final byte[] data, final int off, final int length, final OutputStream out) throws IOException {
        final int modulus = length % 3;
        final int dataLength = length - modulus;
        for (int i = off; i < off + dataLength; i += 3) {
            final int a1 = data[i] & 0xFF;
            final int a2 = data[i + 1] & 0xFF;
            final int a3 = data[i + 2] & 0xFF;
            out.write(this.encodingTable[a1 >>> 2 & 0x3F]);
            out.write(this.encodingTable[(a1 << 4 | a2 >>> 4) & 0x3F]);
            out.write(this.encodingTable[(a2 << 2 | a3 >>> 6) & 0x3F]);
            out.write(this.encodingTable[a3 & 0x3F]);
        }
        switch (modulus) {
            case 1: {
                final int d1 = data[off + dataLength] & 0xFF;
                final int b1 = d1 >>> 2 & 0x3F;
                final int b2 = d1 << 4 & 0x3F;
                out.write(this.encodingTable[b1]);
                out.write(this.encodingTable[b2]);
                out.write(this.padding);
                out.write(this.padding);
                break;
            }
            case 2: {
                final int d1 = data[off + dataLength] & 0xFF;
                final int d2 = data[off + dataLength + 1] & 0xFF;
                final int b1 = d1 >>> 2 & 0x3F;
                final int b2 = (d1 << 4 | d2 >>> 4) & 0x3F;
                final int b3 = d2 << 2 & 0x3F;
                out.write(this.encodingTable[b1]);
                out.write(this.encodingTable[b2]);
                out.write(this.encodingTable[b3]);
                out.write(this.padding);
                break;
            }
        }
        return dataLength / 3 * 4 + ((modulus == 0) ? 0 : 4);
    }
    
    private boolean ignore(final char c) {
        return c == '\n' || c == '\r' || c == '\t' || c == ' ';
    }
    
    @Override
    public int decode(final byte[] data, final int off, final int length, final OutputStream out) throws IOException {
        int outLen = 0;
        int end;
        for (end = off + length; end > off && this.ignore((char)data[end - 1]); --end) {}
        if (end == 0) {
            return 0;
        }
        int i;
        int finish;
        for (i = 0, finish = end; finish > off && i != 4; --finish) {
            if (!this.ignore((char)data[finish - 1])) {
                ++i;
            }
        }
        for (i = this.nextI(data, off, finish); i < finish; i = this.nextI(data, i, finish)) {
            final byte b1 = this.decodingTable[data[i++]];
            i = this.nextI(data, i, finish);
            final byte b2 = this.decodingTable[data[i++]];
            i = this.nextI(data, i, finish);
            final byte b3 = this.decodingTable[data[i++]];
            i = this.nextI(data, i, finish);
            final byte b4 = this.decodingTable[data[i++]];
            if ((b1 | b2 | b3 | b4) < 0) {
                throw new IOException("invalid characters encountered in base64 data");
            }
            out.write(b1 << 2 | b2 >> 4);
            out.write(b2 << 4 | b3 >> 2);
            out.write(b3 << 6 | b4);
            outLen += 3;
        }
        final int e0 = this.nextI(data, i, end);
        final int e2 = this.nextI(data, e0 + 1, end);
        final int e3 = this.nextI(data, e2 + 1, end);
        final int e4 = this.nextI(data, e3 + 1, end);
        outLen += this.decodeLastBlock(out, (char)data[e0], (char)data[e2], (char)data[e3], (char)data[e4]);
        return outLen;
    }
    
    private int nextI(final byte[] data, int i, final int finish) {
        while (i < finish && this.ignore((char)data[i])) {
            ++i;
        }
        return i;
    }
    
    @Override
    public int decode(final String data, final OutputStream out) throws IOException {
        int length = 0;
        int end;
        for (end = data.length(); end > 0 && this.ignore(data.charAt(end - 1)); --end) {}
        if (end == 0) {
            return 0;
        }
        int i;
        int finish;
        for (i = 0, finish = end; finish > 0 && i != 4; --finish) {
            if (!this.ignore(data.charAt(finish - 1))) {
                ++i;
            }
        }
        for (i = this.nextI(data, 0, finish); i < finish; i = this.nextI(data, i, finish)) {
            final byte b1 = this.decodingTable[data.charAt(i++)];
            i = this.nextI(data, i, finish);
            final byte b2 = this.decodingTable[data.charAt(i++)];
            i = this.nextI(data, i, finish);
            final byte b3 = this.decodingTable[data.charAt(i++)];
            i = this.nextI(data, i, finish);
            final byte b4 = this.decodingTable[data.charAt(i++)];
            if ((b1 | b2 | b3 | b4) < 0) {
                throw new IOException("invalid characters encountered in base64 data");
            }
            out.write(b1 << 2 | b2 >> 4);
            out.write(b2 << 4 | b3 >> 2);
            out.write(b3 << 6 | b4);
            length += 3;
        }
        final int e0 = this.nextI(data, i, end);
        final int e2 = this.nextI(data, e0 + 1, end);
        final int e3 = this.nextI(data, e2 + 1, end);
        final int e4 = this.nextI(data, e3 + 1, end);
        length += this.decodeLastBlock(out, data.charAt(e0), data.charAt(e2), data.charAt(e3), data.charAt(e4));
        return length;
    }
    
    private int decodeLastBlock(final OutputStream out, final char c1, final char c2, final char c3, final char c4) throws IOException {
        if (c3 == this.padding) {
            if (c4 != this.padding) {
                throw new IOException("invalid characters encountered at end of base64 data");
            }
            final byte b1 = this.decodingTable[c1];
            final byte b2 = this.decodingTable[c2];
            if ((b1 | b2) < 0) {
                throw new IOException("invalid characters encountered at end of base64 data");
            }
            out.write(b1 << 2 | b2 >> 4);
            return 1;
        }
        else if (c4 == this.padding) {
            final byte b1 = this.decodingTable[c1];
            final byte b2 = this.decodingTable[c2];
            final byte b3 = this.decodingTable[c3];
            if ((b1 | b2 | b3) < 0) {
                throw new IOException("invalid characters encountered at end of base64 data");
            }
            out.write(b1 << 2 | b2 >> 4);
            out.write(b2 << 4 | b3 >> 2);
            return 2;
        }
        else {
            final byte b1 = this.decodingTable[c1];
            final byte b2 = this.decodingTable[c2];
            final byte b3 = this.decodingTable[c3];
            final byte b4 = this.decodingTable[c4];
            if ((b1 | b2 | b3 | b4) < 0) {
                throw new IOException("invalid characters encountered at end of base64 data");
            }
            out.write(b1 << 2 | b2 >> 4);
            out.write(b2 << 4 | b3 >> 2);
            out.write(b3 << 6 | b4);
            return 3;
        }
    }
    
    private int nextI(final String data, int i, final int finish) {
        while (i < finish && this.ignore(data.charAt(i))) {
            ++i;
        }
        return i;
    }
}
