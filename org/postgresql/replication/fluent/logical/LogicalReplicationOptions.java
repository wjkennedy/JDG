// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.replication.fluent.logical;

import java.util.Properties;
import org.postgresql.replication.fluent.CommonOptions;

public interface LogicalReplicationOptions extends CommonOptions
{
    String getSlotName();
    
    Properties getSlotOptions();
}
