// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.util;

import org.codehaus.jackson.io.NumberInput;
import java.math.BigDecimal;
import java.util.ArrayList;

public final class TextBuffer
{
    static final char[] NO_CHARS;
    static final int MIN_SEGMENT_LEN = 1000;
    static final int MAX_SEGMENT_LEN = 262144;
    private final BufferRecycler _allocator;
    private char[] _inputBuffer;
    private int _inputStart;
    private int _inputLen;
    private ArrayList<char[]> _segments;
    private boolean _hasSegments;
    private int _segmentSize;
    private char[] _currentSegment;
    private int _currentSize;
    private String _resultString;
    private char[] _resultArray;
    
    public TextBuffer(final BufferRecycler allocator) {
        this._hasSegments = false;
        this._allocator = allocator;
    }
    
    public void releaseBuffers() {
        if (this._allocator == null) {
            this.resetWithEmpty();
        }
        else if (this._currentSegment != null) {
            this.resetWithEmpty();
            final char[] buf = this._currentSegment;
            this._currentSegment = null;
            this._allocator.releaseCharBuffer(BufferRecycler.CharBufferType.TEXT_BUFFER, buf);
        }
    }
    
    public void resetWithEmpty() {
        this._inputStart = -1;
        this._currentSize = 0;
        this._inputLen = 0;
        this._inputBuffer = null;
        this._resultString = null;
        this._resultArray = null;
        if (this._hasSegments) {
            this.clearSegments();
        }
    }
    
    public void resetWithShared(final char[] buf, final int start, final int len) {
        this._resultString = null;
        this._resultArray = null;
        this._inputBuffer = buf;
        this._inputStart = start;
        this._inputLen = len;
        if (this._hasSegments) {
            this.clearSegments();
        }
    }
    
    public void resetWithCopy(final char[] buf, final int start, final int len) {
        this._inputBuffer = null;
        this._inputStart = -1;
        this._inputLen = 0;
        this._resultString = null;
        this._resultArray = null;
        if (this._hasSegments) {
            this.clearSegments();
        }
        else if (this._currentSegment == null) {
            this._currentSegment = this.findBuffer(len);
        }
        final int n = 0;
        this._segmentSize = n;
        this._currentSize = n;
        this.append(buf, start, len);
    }
    
    public void resetWithString(final String value) {
        this._inputBuffer = null;
        this._inputStart = -1;
        this._inputLen = 0;
        this._resultString = value;
        this._resultArray = null;
        if (this._hasSegments) {
            this.clearSegments();
        }
        this._currentSize = 0;
    }
    
    private final char[] findBuffer(final int needed) {
        if (this._allocator != null) {
            return this._allocator.allocCharBuffer(BufferRecycler.CharBufferType.TEXT_BUFFER, needed);
        }
        return new char[Math.max(needed, 1000)];
    }
    
    private final void clearSegments() {
        this._hasSegments = false;
        this._segments.clear();
        final int n = 0;
        this._segmentSize = n;
        this._currentSize = n;
    }
    
    public int size() {
        if (this._inputStart >= 0) {
            return this._inputLen;
        }
        if (this._resultArray != null) {
            return this._resultArray.length;
        }
        if (this._resultString != null) {
            return this._resultString.length();
        }
        return this._segmentSize + this._currentSize;
    }
    
    public int getTextOffset() {
        return (this._inputStart >= 0) ? this._inputStart : 0;
    }
    
    public boolean hasTextAsCharacters() {
        return this._inputStart >= 0 || this._resultArray != null || this._resultString == null;
    }
    
    public char[] getTextBuffer() {
        if (this._inputStart >= 0) {
            return this._inputBuffer;
        }
        if (this._resultArray != null) {
            return this._resultArray;
        }
        if (this._resultString != null) {
            return this._resultArray = this._resultString.toCharArray();
        }
        if (!this._hasSegments) {
            return this._currentSegment;
        }
        return this.contentsAsArray();
    }
    
    public String contentsAsString() {
        if (this._resultString == null) {
            if (this._resultArray != null) {
                this._resultString = new String(this._resultArray);
            }
            else if (this._inputStart >= 0) {
                if (this._inputLen < 1) {
                    return this._resultString = "";
                }
                this._resultString = new String(this._inputBuffer, this._inputStart, this._inputLen);
            }
            else {
                final int segLen = this._segmentSize;
                final int currLen = this._currentSize;
                if (segLen == 0) {
                    this._resultString = ((currLen == 0) ? "" : new String(this._currentSegment, 0, currLen));
                }
                else {
                    final StringBuilder sb = new StringBuilder(segLen + currLen);
                    if (this._segments != null) {
                        for (int i = 0, len = this._segments.size(); i < len; ++i) {
                            final char[] curr = this._segments.get(i);
                            sb.append(curr, 0, curr.length);
                        }
                    }
                    sb.append(this._currentSegment, 0, this._currentSize);
                    this._resultString = sb.toString();
                }
            }
        }
        return this._resultString;
    }
    
    public char[] contentsAsArray() {
        char[] result = this._resultArray;
        if (result == null) {
            result = (this._resultArray = this.buildResultArray());
        }
        return result;
    }
    
    public BigDecimal contentsAsDecimal() throws NumberFormatException {
        if (this._resultArray != null) {
            return new BigDecimal(this._resultArray);
        }
        if (this._inputStart >= 0) {
            return new BigDecimal(this._inputBuffer, this._inputStart, this._inputLen);
        }
        if (this._segmentSize == 0) {
            return new BigDecimal(this._currentSegment, 0, this._currentSize);
        }
        return new BigDecimal(this.contentsAsArray());
    }
    
    public double contentsAsDouble() throws NumberFormatException {
        return NumberInput.parseDouble(this.contentsAsString());
    }
    
    public void ensureNotShared() {
        if (this._inputStart >= 0) {
            this.unshare(16);
        }
    }
    
    public void append(final char c) {
        if (this._inputStart >= 0) {
            this.unshare(16);
        }
        this._resultString = null;
        this._resultArray = null;
        char[] curr = this._currentSegment;
        if (this._currentSize >= curr.length) {
            this.expand(1);
            curr = this._currentSegment;
        }
        curr[this._currentSize++] = c;
    }
    
    public void append(final char[] c, int start, int len) {
        if (this._inputStart >= 0) {
            this.unshare(len);
        }
        this._resultString = null;
        this._resultArray = null;
        final char[] curr = this._currentSegment;
        final int max = curr.length - this._currentSize;
        if (max >= len) {
            System.arraycopy(c, start, curr, this._currentSize, len);
            this._currentSize += len;
            return;
        }
        if (max > 0) {
            System.arraycopy(c, start, curr, this._currentSize, max);
            start += max;
            len -= max;
        }
        do {
            this.expand(len);
            final int amount = Math.min(this._currentSegment.length, len);
            System.arraycopy(c, start, this._currentSegment, 0, amount);
            this._currentSize += amount;
            start += amount;
            len -= amount;
        } while (len > 0);
    }
    
    public void append(final String str, int offset, int len) {
        if (this._inputStart >= 0) {
            this.unshare(len);
        }
        this._resultString = null;
        this._resultArray = null;
        final char[] curr = this._currentSegment;
        final int max = curr.length - this._currentSize;
        if (max >= len) {
            str.getChars(offset, offset + len, curr, this._currentSize);
            this._currentSize += len;
            return;
        }
        if (max > 0) {
            str.getChars(offset, offset + max, curr, this._currentSize);
            len -= max;
            offset += max;
        }
        do {
            this.expand(len);
            final int amount = Math.min(this._currentSegment.length, len);
            str.getChars(offset, offset + amount, this._currentSegment, 0);
            this._currentSize += amount;
            offset += amount;
            len -= amount;
        } while (len > 0);
    }
    
    public char[] getCurrentSegment() {
        if (this._inputStart >= 0) {
            this.unshare(1);
        }
        else {
            final char[] curr = this._currentSegment;
            if (curr == null) {
                this._currentSegment = this.findBuffer(0);
            }
            else if (this._currentSize >= curr.length) {
                this.expand(1);
            }
        }
        return this._currentSegment;
    }
    
    public final char[] emptyAndGetCurrentSegment() {
        this._inputStart = -1;
        this._currentSize = 0;
        this._inputLen = 0;
        this._inputBuffer = null;
        this._resultString = null;
        this._resultArray = null;
        if (this._hasSegments) {
            this.clearSegments();
        }
        char[] curr = this._currentSegment;
        if (curr == null) {
            curr = (this._currentSegment = this.findBuffer(0));
        }
        return curr;
    }
    
    public int getCurrentSegmentSize() {
        return this._currentSize;
    }
    
    public void setCurrentLength(final int len) {
        this._currentSize = len;
    }
    
    public char[] finishCurrentSegment() {
        if (this._segments == null) {
            this._segments = new ArrayList<char[]>();
        }
        this._hasSegments = true;
        this._segments.add(this._currentSegment);
        final int oldLen = this._currentSegment.length;
        this._segmentSize += oldLen;
        final int newLen = Math.min(oldLen + (oldLen >> 1), 262144);
        final char[] curr = this._charArray(newLen);
        this._currentSize = 0;
        return this._currentSegment = curr;
    }
    
    public char[] expandCurrentSegment() {
        final char[] curr = this._currentSegment;
        final int len = curr.length;
        final int newLen = (len == 262144) ? 262145 : Math.min(262144, len + (len >> 1));
        System.arraycopy(curr, 0, this._currentSegment = this._charArray(newLen), 0, len);
        return this._currentSegment;
    }
    
    @Override
    public String toString() {
        return this.contentsAsString();
    }
    
    private void unshare(final int needExtra) {
        final int sharedLen = this._inputLen;
        this._inputLen = 0;
        final char[] inputBuf = this._inputBuffer;
        this._inputBuffer = null;
        final int start = this._inputStart;
        this._inputStart = -1;
        final int needed = sharedLen + needExtra;
        if (this._currentSegment == null || needed > this._currentSegment.length) {
            this._currentSegment = this.findBuffer(needed);
        }
        if (sharedLen > 0) {
            System.arraycopy(inputBuf, start, this._currentSegment, 0, sharedLen);
        }
        this._segmentSize = 0;
        this._currentSize = sharedLen;
    }
    
    private void expand(final int minNewSegmentSize) {
        if (this._segments == null) {
            this._segments = new ArrayList<char[]>();
        }
        char[] curr = this._currentSegment;
        this._hasSegments = true;
        this._segments.add(curr);
        this._segmentSize += curr.length;
        final int oldLen = curr.length;
        int sizeAddition = oldLen >> 1;
        if (sizeAddition < minNewSegmentSize) {
            sizeAddition = minNewSegmentSize;
        }
        curr = this._charArray(Math.min(262144, oldLen + sizeAddition));
        this._currentSize = 0;
        this._currentSegment = curr;
    }
    
    private char[] buildResultArray() {
        if (this._resultString != null) {
            return this._resultString.toCharArray();
        }
        char[] result;
        if (this._inputStart >= 0) {
            if (this._inputLen < 1) {
                return TextBuffer.NO_CHARS;
            }
            result = this._charArray(this._inputLen);
            System.arraycopy(this._inputBuffer, this._inputStart, result, 0, this._inputLen);
        }
        else {
            final int size = this.size();
            if (size < 1) {
                return TextBuffer.NO_CHARS;
            }
            int offset = 0;
            result = this._charArray(size);
            if (this._segments != null) {
                for (int i = 0, len = this._segments.size(); i < len; ++i) {
                    final char[] curr = this._segments.get(i);
                    final int currLen = curr.length;
                    System.arraycopy(curr, 0, result, offset, currLen);
                    offset += currLen;
                }
            }
            System.arraycopy(this._currentSegment, 0, result, offset, this._currentSize);
        }
        return result;
    }
    
    private final char[] _charArray(final int len) {
        return new char[len];
    }
    
    static {
        NO_CHARS = new char[0];
    }
}
