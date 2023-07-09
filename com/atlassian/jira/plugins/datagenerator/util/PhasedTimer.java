// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.util;

import java.util.Iterator;
import com.google.common.collect.Lists;
import java.util.List;
import com.google.common.collect.Maps;
import org.apache.commons.lang.time.StopWatch;
import java.util.Map;

public class PhasedTimer
{
    private final Map<String, StopWatch> stopWatches;
    private StopWatch currentStopWatch;
    
    public PhasedTimer() {
        this.stopWatches = Maps.newLinkedHashMap();
    }
    
    public void startPhase(final String description) {
        this.stop();
        this.currentStopWatch = this.stopWatches.get(description);
        if (this.currentStopWatch == null) {
            this.currentStopWatch = new StopWatch();
            this.stopWatches.put(description, this.currentStopWatch);
            this.currentStopWatch.start();
        }
        else {
            this.currentStopWatch.resume();
        }
    }
    
    public void stop() {
        if (this.currentStopWatch != null) {
            this.currentStopWatch.suspend();
            this.currentStopWatch = null;
        }
    }
    
    public List<String> toStrings() {
        this.stop();
        final List<String> strings = Lists.newArrayListWithCapacity(this.stopWatches.size());
        for (final Map.Entry<String, StopWatch> entry : this.stopWatches.entrySet()) {
            strings.add(" - " + entry.getKey() + ' ' + entry.getValue().toString());
        }
        return strings;
    }
}
