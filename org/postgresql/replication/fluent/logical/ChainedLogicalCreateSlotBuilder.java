// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.replication.fluent.logical;

import org.postgresql.replication.fluent.ChainedCommonCreateSlotBuilder;

public interface ChainedLogicalCreateSlotBuilder extends ChainedCommonCreateSlotBuilder<ChainedLogicalCreateSlotBuilder>
{
    ChainedLogicalCreateSlotBuilder withOutputPlugin(final String p0);
}
