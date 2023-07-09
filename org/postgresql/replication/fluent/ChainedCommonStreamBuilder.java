// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.replication.fluent;

import org.postgresql.replication.LogSequenceNumber;
import java.util.concurrent.TimeUnit;

public interface ChainedCommonStreamBuilder<T extends ChainedCommonStreamBuilder<T>>
{
    T withSlotName(final String p0);
    
    T withStatusInterval(final int p0, final TimeUnit p1);
    
    T withStartPosition(final LogSequenceNumber p0);
}
