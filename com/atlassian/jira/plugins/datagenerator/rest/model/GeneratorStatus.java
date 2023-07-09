// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.rest.model;

import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GeneratorStatus
{
    private final int progress;
    private final String phase;
    private final String task;
    private final List<String> messages;
    private final boolean finished;
    
    public GeneratorStatus() {
        this("", Collections.emptyList(), "", 0, false);
    }
    
    public GeneratorStatus(final String phase, final List<String> messages, final String task, final int progress, final boolean finished) {
        this.messages = messages;
        this.task = task;
        this.phase = phase;
        this.progress = progress;
        this.finished = finished;
    }
    
    public int getProgress() {
        return this.progress;
    }
    
    public String getPhase() {
        return this.phase;
    }
    
    public String getTask() {
        return this.task;
    }
    
    public List<String> getMessages() {
        return this.messages;
    }
    
    public boolean isFinished() {
        return this.finished;
    }
}
