// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.hostchooser;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import org.postgresql.util.HostSpec;
import java.util.Map;

public class GlobalHostStatusTracker
{
    private static final Map<HostSpec, HostSpecStatus> hostStatusMap;
    
    public static void reportHostStatus(final HostSpec hostSpec, final HostStatus hostStatus) {
        final long now = System.nanoTime() / 1000000L;
        synchronized (GlobalHostStatusTracker.hostStatusMap) {
            HostSpecStatus hostSpecStatus = GlobalHostStatusTracker.hostStatusMap.get(hostSpec);
            if (hostSpecStatus == null) {
                hostSpecStatus = new HostSpecStatus(hostSpec);
                GlobalHostStatusTracker.hostStatusMap.put(hostSpec, hostSpecStatus);
            }
            hostSpecStatus.status = hostStatus;
            hostSpecStatus.lastUpdated = now;
        }
    }
    
    static List<HostSpec> getCandidateHosts(final HostSpec[] hostSpecs, final HostRequirement targetServerType, final long hostRecheckMillis) {
        final List<HostSpec> candidates = new ArrayList<HostSpec>(hostSpecs.length);
        final long latestAllowedUpdate = System.nanoTime() / 1000000L - hostRecheckMillis;
        synchronized (GlobalHostStatusTracker.hostStatusMap) {
            for (final HostSpec hostSpec : hostSpecs) {
                final HostSpecStatus hostInfo = GlobalHostStatusTracker.hostStatusMap.get(hostSpec);
                if (hostInfo == null || hostInfo.lastUpdated < latestAllowedUpdate || targetServerType.allowConnectingTo(hostInfo.status)) {
                    candidates.add(hostSpec);
                }
            }
        }
        return candidates;
    }
    
    static {
        hostStatusMap = new HashMap<HostSpec, HostSpecStatus>();
    }
    
    static class HostSpecStatus
    {
        final HostSpec host;
        HostStatus status;
        long lastUpdated;
        
        HostSpecStatus(final HostSpec host) {
            this.host = host;
        }
        
        @Override
        public String toString() {
            return this.host.toString() + '=' + this.status;
        }
    }
}
