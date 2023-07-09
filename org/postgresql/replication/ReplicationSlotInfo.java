// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.replication;

public final class ReplicationSlotInfo
{
    private final String slotName;
    private final ReplicationType replicationType;
    private final LogSequenceNumber consistentPoint;
    private final String snapshotName;
    private final String outputPlugin;
    
    public ReplicationSlotInfo(final String slotName, final ReplicationType replicationType, final LogSequenceNumber consistentPoint, final String snapshotName, final String outputPlugin) {
        this.slotName = slotName;
        this.replicationType = replicationType;
        this.consistentPoint = consistentPoint;
        this.snapshotName = snapshotName;
        this.outputPlugin = outputPlugin;
    }
    
    public String getSlotName() {
        return this.slotName;
    }
    
    public ReplicationType getReplicationType() {
        return this.replicationType;
    }
    
    public LogSequenceNumber getConsistentPoint() {
        return this.consistentPoint;
    }
    
    public String getSnapshotName() {
        return this.snapshotName;
    }
    
    public String getOutputPlugin() {
        return this.outputPlugin;
    }
}
