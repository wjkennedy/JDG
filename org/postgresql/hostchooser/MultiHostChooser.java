// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.hostchooser;

import java.util.AbstractList;
import java.util.List;
import java.util.Collections;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import org.postgresql.util.PSQLException;
import org.postgresql.PGProperty;
import java.util.Properties;
import org.postgresql.util.HostSpec;

class MultiHostChooser implements HostChooser
{
    private HostSpec[] hostSpecs;
    private final HostRequirement targetServerType;
    private int hostRecheckTime;
    private boolean loadBalance;
    
    MultiHostChooser(final HostSpec[] hostSpecs, final HostRequirement targetServerType, final Properties info) {
        this.hostSpecs = hostSpecs;
        this.targetServerType = targetServerType;
        try {
            this.hostRecheckTime = PGProperty.HOST_RECHECK_SECONDS.getInt(info) * 1000;
            this.loadBalance = PGProperty.LOAD_BALANCE_HOSTS.getBoolean(info);
        }
        catch (final PSQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public Iterator<CandidateHost> iterator() {
        Iterator<CandidateHost> res = this.candidateIterator();
        if (!res.hasNext()) {
            List<HostSpec> allHosts = Arrays.asList(this.hostSpecs);
            if (this.loadBalance) {
                allHosts = new ArrayList<HostSpec>(allHosts);
                Collections.shuffle(allHosts);
            }
            res = this.withReqStatus(this.targetServerType, allHosts).iterator();
        }
        return res;
    }
    
    private Iterator<CandidateHost> candidateIterator() {
        if (this.targetServerType != HostRequirement.preferSecondary) {
            return this.getCandidateHosts(this.targetServerType).iterator();
        }
        List<CandidateHost> secondaries = this.getCandidateHosts(HostRequirement.secondary);
        final List<CandidateHost> any = this.getCandidateHosts(HostRequirement.any);
        if (secondaries.isEmpty()) {
            return any.iterator();
        }
        if (any.isEmpty()) {
            return secondaries.iterator();
        }
        if (secondaries.get(secondaries.size() - 1).equals(any.get(0))) {
            secondaries = this.rtrim(1, secondaries);
        }
        return this.append(secondaries, any).iterator();
    }
    
    private List<CandidateHost> getCandidateHosts(final HostRequirement hostRequirement) {
        final List<HostSpec> candidates = GlobalHostStatusTracker.getCandidateHosts(this.hostSpecs, hostRequirement, this.hostRecheckTime);
        if (this.loadBalance) {
            Collections.shuffle(candidates);
        }
        return this.withReqStatus(hostRequirement, candidates);
    }
    
    private List<CandidateHost> withReqStatus(final HostRequirement requirement, final List<HostSpec> hosts) {
        return new AbstractList<CandidateHost>() {
            @Override
            public CandidateHost get(final int index) {
                return new CandidateHost(hosts.get(index), requirement);
            }
            
            @Override
            public int size() {
                return hosts.size();
            }
        };
    }
    
    private <T> List<T> append(final List<T> a, final List<T> b) {
        return new AbstractList<T>() {
            @Override
            public T get(final int index) {
                return (index < a.size()) ? a.get(index) : b.get(index - a.size());
            }
            
            @Override
            public int size() {
                return a.size() + b.size();
            }
        };
    }
    
    private <T> List<T> rtrim(final int size, final List<T> a) {
        return new AbstractList<T>() {
            @Override
            public T get(final int index) {
                return a.get(index);
            }
            
            @Override
            public int size() {
                return Math.max(0, a.size() - size);
            }
        };
    }
}
