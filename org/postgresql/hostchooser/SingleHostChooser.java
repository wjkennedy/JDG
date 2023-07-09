// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.hostchooser;

import java.util.Iterator;
import java.util.Collections;
import org.postgresql.util.HostSpec;
import java.util.Collection;

class SingleHostChooser implements HostChooser
{
    private final Collection<CandidateHost> candidateHost;
    
    SingleHostChooser(final HostSpec hostSpec, final HostRequirement targetServerType) {
        this.candidateHost = Collections.singletonList(new CandidateHost(hostSpec, targetServerType));
    }
    
    @Override
    public Iterator<CandidateHost> iterator() {
        return this.candidateHost.iterator();
    }
}
