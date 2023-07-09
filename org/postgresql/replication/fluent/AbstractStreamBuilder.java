// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.replication.fluent;

import java.util.concurrent.TimeUnit;
import org.postgresql.replication.LogSequenceNumber;

public abstract class AbstractStreamBuilder<T extends ChainedCommonStreamBuilder<T>> implements ChainedCommonStreamBuilder<T>
{
    private static final int DEFAULT_STATUS_INTERVAL;
    protected int statusIntervalMs;
    protected LogSequenceNumber startPosition;
    protected String slotName;
    
    public AbstractStreamBuilder() {
        this.statusIntervalMs = AbstractStreamBuilder.DEFAULT_STATUS_INTERVAL;
        this.startPosition = LogSequenceNumber.INVALID_LSN;
    }
    
    protected abstract T self();
    
    @Override
    public T withStatusInterval(final int time, final TimeUnit format) {
        this.statusIntervalMs = (int)TimeUnit.MILLISECONDS.convert(time, format);
        return this.self();
    }
    
    @Override
    public T withStartPosition(final LogSequenceNumber lsn) {
        this.startPosition = lsn;
        return this.self();
    }
    
    @Override
    public T withSlotName(final String slotName) {
        this.slotName = slotName;
        return this.self();
    }
    
    static {
        DEFAULT_STATUS_INTERVAL = (int)TimeUnit.SECONDS.toMillis(10L);
    }
}
