// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.replication;

import java.nio.ByteBuffer;

public final class LogSequenceNumber implements Comparable<LogSequenceNumber>
{
    public static final LogSequenceNumber INVALID_LSN;
    private final long value;
    
    private LogSequenceNumber(final long value) {
        this.value = value;
    }
    
    public static LogSequenceNumber valueOf(final long value) {
        return new LogSequenceNumber(value);
    }
    
    public static LogSequenceNumber valueOf(final String strValue) {
        final int slashIndex = strValue.lastIndexOf(47);
        if (slashIndex <= 0) {
            return LogSequenceNumber.INVALID_LSN;
        }
        final String logicalXLogStr = strValue.substring(0, slashIndex);
        final int logicalXlog = (int)Long.parseLong(logicalXLogStr, 16);
        final String segmentStr = strValue.substring(slashIndex + 1, strValue.length());
        final int segment = (int)Long.parseLong(segmentStr, 16);
        final ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putInt(logicalXlog);
        buf.putInt(segment);
        buf.position(0);
        final long value = buf.getLong();
        return valueOf(value);
    }
    
    public long asLong() {
        return this.value;
    }
    
    public String asString() {
        final ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putLong(this.value);
        buf.position(0);
        final int logicalXlog = buf.getInt();
        final int segment = buf.getInt();
        return String.format("%X/%X", logicalXlog, segment);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final LogSequenceNumber that = (LogSequenceNumber)o;
        return this.value == that.value;
    }
    
    @Override
    public int hashCode() {
        return (int)(this.value ^ this.value >>> 32);
    }
    
    @Override
    public String toString() {
        return "LSN{" + this.asString() + '}';
    }
    
    @Override
    public int compareTo(final LogSequenceNumber o) {
        if (this.value == o.value) {
            return 0;
        }
        return (this.value + Long.MIN_VALUE < o.value + Long.MIN_VALUE) ? -1 : 1;
    }
    
    static {
        INVALID_LSN = valueOf(0L);
    }
}
